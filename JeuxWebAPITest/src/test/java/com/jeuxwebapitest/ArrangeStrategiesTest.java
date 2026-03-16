package com.jeuxwebapitest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jeuxwebapi.models.StandingMatchType;
import com.jeuxwebapi.models.TournamentDto;
import com.jeuxwebapi.services.arrange_strategies.ArrangeStrategyBase;
import com.jeuxwebapi.services.arrange_strategies.ArrangeStrategyFactory;
import com.jeuxwebapi.services.arrange_strategies.IArrangeStrategy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.Test;

public class ArrangeStrategiesTest {
    @Test
    void strategy1992To1994HasGroupKeyWithThreeFieldsBeforePersonalPlays() {
        IArrangeStrategy<TestStanding> strategy = new ArrangeStrategyFactory<TestStanding>(
                tournamentWithYear(1993),
                StandingMatchType.ALL
        ).create();

        Object key = strategy.getGroupKeyFunction().apply(sampleStanding());
        assertTrue(key instanceof List<?>);
        assertEquals(List.of(10, 5, 2), key);
    }

    @Test
    void strategy1995To2013HasGroupKeyWithOnlyPointsBeforePersonalPlays() {
        IArrangeStrategy<TestStanding> strategy = new ArrangeStrategyFactory<TestStanding>(
                tournamentWithYear(2001),
                StandingMatchType.ALL
        ).create();

        Object key = strategy.getGroupKeyFunction().apply(sampleStanding());
        assertTrue(key instanceof List<?>);
        assertEquals(List.of(10), key);
    }

    @Test
    void strategy2014To2017UsesSubOrderAfterWinsInMainComparator() {
        IArrangeStrategy<TestStanding> strategy = new ArrangeStrategyFactory<TestStanding>(
                tournamentWithYear(2016),
                StandingMatchType.ALL
        ).create();

        List<TestStanding> sorted = sortedWith(strategy.getMainOrderComparator(), standingA(), standingB());
        assertEquals(List.of("A", "B"), names(sorted));
    }

    @Test
    void strategySince2018UsesSubOrderRightAfterPointsInMainComparator() {
        IArrangeStrategy<TestStanding> strategy = new ArrangeStrategyFactory<TestStanding>(
                tournamentWithYear(2018),
                StandingMatchType.ALL
        ).create();

        List<TestStanding> sorted = sortedWith(strategy.getMainOrderComparator(), standingA(), standingB());
        assertEquals(List.of("B", "A"), names(sorted));
    }

    @Test
    void groupOrderComparatorUsesPersonalPlaysItemsOrder() {
        IArrangeStrategy<TestStanding> strategy = new ArrangeStrategyBase<>(
                "arrange_strategy_since_2018.json",
                StandingMatchType.ALL
        );

        TestStanding strongerInPersonalPlays = new TestStanding("A", 9, 4, 4, 8, 1, 2);
        TestStanding weakerInPersonalPlays = new TestStanding("B", 9, 4, 1, 8, 1, 1);

        List<TestStanding> sorted = sortedWith(
                strategy.getGroupOrderComparator(),
                weakerInPersonalPlays,
                strongerInPersonalPlays
        );
        assertEquals(List.of("A", "B"), names(sorted));
    }

    @Test
    void missingRulesReturnEmptyKeyAndNeutralComparators() {
        IArrangeStrategy<TestStanding> strategy = new ArrangeStrategyBase<>(
                "missing_resource.json",
                StandingMatchType.ALL
        );

        Object key = strategy.getGroupKeyFunction().apply(sampleStanding());
        assertEquals(List.of(), key);
        assertEquals(0, strategy.getGroupOrderComparator().compare(standingA(), standingB()));
        assertEquals(0, strategy.getMainOrderComparator().compare(standingA(), standingB()));
    }

    private static TournamentDto tournamentWithYear(int stYear) {
        TournamentDto tournament = new TournamentDto();
        tournament.setStYear(stYear);
        return tournament;
    }

    private static TestStanding sampleStanding() {
        return new TestStanding("S", 10, 5, 2, 8, 1, 0);
    }

    private static TestStanding standingA() {
        return new TestStanding("A", 10, 5, 1, 8, 1, 2);
    }

    private static TestStanding standingB() {
        return new TestStanding("B", 10, 4, 10, 8, 1, 1);
    }

    @SafeVarargs
    private static List<TestStanding> sortedWith(Comparator<TestStanding> comparator, TestStanding... standings) {
        List<TestStanding> items = new ArrayList<>(List.of(standings));
        items.sort(comparator);
        return items;
    }

    private static List<String> names(List<TestStanding> standings) {
        return standings.stream().map(TestStanding::getName).toList();
    }

    @SuppressWarnings("unused")
    private static final class TestStanding {
        private final String name;
        private final int points;
        private final int wins;
        private final int diff;
        private final int scored;
        private final int gscored;
        private final int subOrder;

        private TestStanding(String name, int points, int wins, int diff, int scored, int gscored, int subOrder) {
            this.name = name;
            this.points = points;
            this.wins = wins;
            this.diff = diff;
            this.scored = scored;
            this.gscored = gscored;
            this.subOrder = subOrder;
        }

        public String getName() {
            return name;
        }

        public int getPoints() {
            return points;
        }

        public int getWins() {
            return wins;
        }

        public int getDiff() {
            return diff;
        }

        public int getScored() {
            return scored;
        }

        public int getGscored() {
            return gscored;
        }

        public int getSubOrder() {
            return subOrder;
        }
    }
}
