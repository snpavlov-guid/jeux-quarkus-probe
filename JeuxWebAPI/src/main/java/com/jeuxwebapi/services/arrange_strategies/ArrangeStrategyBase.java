package com.jeuxwebapi.services.arrange_strategies;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeuxwebapi.models.StandingMatchType;
import java.beans.Introspector;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class ArrangeStrategyBase<TSD> implements IArrangeStrategy<TSD> {
    private static final String ARRANGE_STRATEGIES_RESOURCE_FOLDER = "arrange_strategies/";
    private static final String PERSONAL_PLAYS_FIELD = "personalPlays";
    private static final String PERSONAL_PLAYS_ITEMS_FIELD = "items";
    private static final String PERSONAL_PLAYS_ORDER_FIELD = "order";
    private static final String SUB_ORDER_FIELD = "subOrder";
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Comparator<Object> NATURAL_ORDER = (left, right) -> {
        if (left == right) {
            return 0;
        }
        if (left == null) {
            return 1;
        }
        if (right == null) {
            return -1;
        }
        if (left instanceof Comparable<?> comparable) {
            @SuppressWarnings("unchecked")
            Comparable<Object> typedComparable = (Comparable<Object>) comparable;
            return typedComparable.compareTo(right);
        }
        return String.valueOf(left).compareTo(String.valueOf(right));
    };
    private static final Map<Class<?>, Map<String, Method>> GETTERS_CACHE = new ConcurrentHashMap<>();

    private final List<SortRule> groupKeyRules;
    private final List<SortRule> groupOrderRules;
    private final List<SortRule> mainOrderRules;

    public ArrangeStrategyBase(String resourceFileName) {
        this(resourceFileName, StandingMatchType.ALL);
    }

    public ArrangeStrategyBase(String resourceFileName, StandingMatchType matchType) {
        StandingMatchType effectiveMatchType = matchType == null ? StandingMatchType.ALL : matchType;
        ParsedRules parsedRules = parseRules(resourceFileName, effectiveMatchType);
        this.groupKeyRules = parsedRules.groupKeyRules;
        this.groupOrderRules = parsedRules.groupOrderRules;
        this.mainOrderRules = parsedRules.mainOrderRules;
    }

    @Override
    public Comparator<TSD> getMainOrderComparator() {
        return buildComparator(mainOrderRules);
    }

    @Override
    public Comparator<TSD> getGroupOrderComparator() {
        return buildComparator(groupOrderRules);
    }

    @Override
    public Function<TSD, ?> getGroupKeyFunction() {
        if (groupKeyRules.isEmpty()) {
            return ignored -> List.of();
        }

        return item -> {
            if (item == null) {
                return List.of();
            }
            List<Object> key = new ArrayList<>(groupKeyRules.size());
            for (SortRule rule : groupKeyRules) {
                key.add(readFieldValue(item, rule.fieldName));
            }
            return List.copyOf(key);
        };
    }

    private Comparator<TSD> buildComparator(List<SortRule> rules) {
        if (rules.isEmpty()) {
            return (left, right) -> 0;
        }

        Comparator<TSD> comparator = (left, right) -> 0;
        for (SortRule rule : rules) {
            Comparator<TSD> fieldComparator = Comparator.comparing(
                    item -> readFieldValue(item, rule.fieldName),
                    NATURAL_ORDER
            );
            comparator = comparator.thenComparing(rule.ascending ? fieldComparator : fieldComparator.reversed());
        }
        return comparator;
    }

    private Object readFieldValue(TSD item, String fieldName) {
        if (item == null) {
            return null;
        }
        Method getter = resolveGetter(item.getClass(), fieldName);
        if (getter == null) {
            return null;
        }

        try {
            return getter.invoke(item);
        } catch (ReflectiveOperationException ignored) {
            return null;
        }
    }

    private Method resolveGetter(Class<?> type, String fieldName) {
        Map<String, Method> getters = GETTERS_CACHE.computeIfAbsent(type, ArrangeStrategyBase::buildGettersMap);
        return getters.get(normalize(fieldName));
    }

    private static Map<String, Method> buildGettersMap(Class<?> type) {
        Map<String, Method> getters = new HashMap<>();
        try {
            for (var descriptor : Introspector.getBeanInfo(type).getPropertyDescriptors()) {
                Method readMethod = descriptor.getReadMethod();
                if (readMethod == null) {
                    continue;
                }
                readMethod.setAccessible(true);
                getters.put(normalize(descriptor.getName()), readMethod);
            }
        } catch (Exception ignored) {
            // Keep map empty if introspection fails.
        }
        return getters;
    }

    private ParsedRules parseRules(String resourceFileName, StandingMatchType matchType) {
        if (resourceFileName == null || resourceFileName.isBlank()) {
            return ParsedRules.empty();
        }

        JsonNode rootNode = readRootNode(resourceFileName);
        if (rootNode == null || !rootNode.isObject()) {
            return ParsedRules.empty();
        }

        JsonNode matchTypeNode = rootNode.path(matchType.name());
        if (!matchTypeNode.isArray()) {
            return ParsedRules.empty();
        }

        List<SortRule> groupKey = new ArrayList<>();
        List<SortRule> groupOrder = new ArrayList<>();
        List<SortRule> mainOrder = new ArrayList<>();
        boolean personalPlaysReached = false;

        for (JsonNode topLevelRuleNode : matchTypeNode) {
            if (!topLevelRuleNode.isObject()) {
                continue;
            }

            JsonNode personalPlaysNode = topLevelRuleNode.get(PERSONAL_PLAYS_FIELD);
            if (personalPlaysNode != null && personalPlaysNode.isObject()) {
                personalPlaysReached = true;
                groupOrder = parseItemsRules(personalPlaysNode.get(PERSONAL_PLAYS_ITEMS_FIELD));
                boolean subOrderAscending = parseDirection(
                        personalPlaysNode.get(PERSONAL_PLAYS_ORDER_FIELD),
                        true
                );
                mainOrder.add(new SortRule(SUB_ORDER_FIELD, subOrderAscending));
                continue;
            }

            List<SortRule> simpleRules = parseSimpleRuleObject(topLevelRuleNode);
            mainOrder.addAll(simpleRules);
            if (!personalPlaysReached) {
                groupKey.addAll(simpleRules);
            }
        }

        return new ParsedRules(List.copyOf(groupKey), List.copyOf(groupOrder), List.copyOf(mainOrder));
    }

    private List<SortRule> parseItemsRules(JsonNode itemsNode) {
        if (itemsNode == null || !itemsNode.isArray()) {
            return List.of();
        }

        List<SortRule> itemsRules = new ArrayList<>();
        for (JsonNode itemNode : itemsNode) {
            if (itemNode == null || !itemNode.isObject()) {
                continue;
            }
            itemsRules.addAll(parseSimpleRuleObject(itemNode));
        }
        return List.copyOf(itemsRules);
    }

    private List<SortRule> parseSimpleRuleObject(JsonNode ruleObjectNode) {
        List<SortRule> rules = new ArrayList<>();
        var fieldsIterator = ruleObjectNode.fields();
        while (fieldsIterator.hasNext()) {
            Map.Entry<String, JsonNode> fieldEntry = fieldsIterator.next();
            String fieldName = fieldEntry.getKey();
            if (PERSONAL_PLAYS_FIELD.equals(fieldName)) {
                continue;
            }
            boolean ascending = parseDirection(fieldEntry.getValue(), false);
            rules.add(new SortRule(fieldName, ascending));
        }
        return List.copyOf(rules);
    }

    private boolean parseDirection(JsonNode directionNode, boolean defaultAscending) {
        if (directionNode == null || !directionNode.isTextual()) {
            return defaultAscending;
        }

        String direction = directionNode.asText();
        if ("asc".equalsIgnoreCase(direction)) {
            return true;
        }
        if ("desc".equalsIgnoreCase(direction)) {
            return false;
        }
        return defaultAscending;
    }

    private JsonNode readRootNode(String resourceFileName) {
        String directPath = resourceFileName;
        String withFolderPath = ARRANGE_STRATEGIES_RESOURCE_FOLDER + resourceFileName;

        JsonNode direct = readJsonResource(directPath);
        if (direct != null) {
            return direct;
        }
        return readJsonResource(withFolderPath);
    }

    private JsonNode readJsonResource(String resourcePath) {
        try (InputStream stream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return null;
            }
            return OBJECT_MAPPER.readTree(stream);
        } catch (IOException ignored) {
            return null;
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.replace("_", "").toLowerCase();
    }

    private record SortRule(String fieldName, boolean ascending) {
    }

    private record ParsedRules(
            List<SortRule> groupKeyRules,
            List<SortRule> groupOrderRules,
            List<SortRule> mainOrderRules
    ) {
        private static ParsedRules empty() {
            return new ParsedRules(List.of(), List.of(), List.of());
        }
    }
}
