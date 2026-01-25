package Services;

import Entities.League;
import Entities.Match;
import Entities.QLeague;
import Entities.QMatch;
import Entities.QStage;
import Entities.QTeam;
import Entities.QTournament;
import Entities.Stage;
import Entities.Team;
import Entities.Tournament;
import Models.RawMatchInfo;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.time.temporal.ChronoField;
import org.hibernate.Session;

public class MatchUploadService {
    private final Session session;

    public MatchUploadService(Session session) {
        this.session = session;
    }

    public League UpsertLeague(String leagueName) {
        QLeague qLeague = QLeague.league;
        JPAQueryFactory queryFactory = new JPAQueryFactory(session);

        League existing = queryFactory
                .selectFrom(qLeague)
                .where(qLeague.Name.equalsIgnoreCase(leagueName))
                .fetchFirst();

        if (existing != null) {
            return existing;
        }

        queryFactory
                .insert(qLeague)
                .columns(qLeague.Name)
                .values(leagueName)
                .execute();

        League created = queryFactory
                .selectFrom(qLeague)
                .where(qLeague.Name.equalsIgnoreCase(leagueName))
                .fetchFirst();

        return created;
    }

    public Tournament UpgradeTournament(Tournament turnir) {
        QTournament qTournament = QTournament.tournament;
        Long leagueId = turnir.getLeagueId();
        League league = turnir.getLeague();
        Integer fnYear = turnir.getFnYear();
        JPAQueryFactory queryFactory = new JPAQueryFactory(session);

        if (leagueId == null && league != null) {
            leagueId = league.getId();
            turnir.setLeagueId(leagueId);
        }

        if (leagueId == null) {
            throw new IllegalArgumentException("Tournament LeagueId is required");
        }

        BooleanBuilder predicate = new BooleanBuilder()
                .and(qTournament.LeagueId.eq(leagueId))
                .and(qTournament.Name.equalsIgnoreCase(turnir.getName()))
                .and(qTournament.StYear.eq(turnir.getStYear()));

        if (fnYear != null) {
            predicate.and(qTournament.FnYear.eq(fnYear));
        }

        Tournament existing = queryFactory
                .selectFrom(qTournament)
                .where(predicate)
                .fetchFirst();

        if (existing != null) {
            queryFactory
                    .delete(qTournament)
                    .where(qTournament.Id.eq(existing.getId()))
                    .execute();
        }

        if (fnYear == null) {
            queryFactory
                    .insert(qTournament)
                    .columns(qTournament.LeagueId, qTournament.Name, qTournament.StYear)
                    .values(leagueId, turnir.getName(), turnir.getStYear())
                    .execute();
        } else {
            queryFactory
                    .insert(qTournament)
                    .columns(qTournament.LeagueId, qTournament.Name, qTournament.StYear, qTournament.FnYear)
                    .values(leagueId, turnir.getName(), turnir.getStYear(), fnYear)
                    .execute();
        }

        return queryFactory
                .selectFrom(qTournament)
                .where(predicate)
                .fetchFirst();
    }

    public boolean SaveTournamentData(Tournament turnir, List<Stage> stages) {
        final int batchSize = 50;

        try {
            int stageIndex = 0;
            for (Stage stage : stages) {
                session.persist(stage);
                stageIndex++;
                if (stageIndex % batchSize == 0) {
                    session.flush();
                    session.clear();
                }
            }
            session.flush();

            int matchIndex = 0;
            for (Stage stage : stages) {
                Long stageId = stage.getId() > 0 ? stage.getId() : null;
                for (Match match : stage.getMatches()) {
                    if (match.getStageId() == null) {
                        match.setStageId(stageId);
                    }
                    session.persist(match);
                    matchIndex++;
                    if (matchIndex % batchSize == 0) {
                        session.flush();
                        session.clear();
                    }
                }
            }
            session.flush();

            return true;
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    public boolean SaveTeams(List<Team> teams) {
        if (teams == null || teams.isEmpty()) {
            return true;
        }

        JPAQueryFactory queryFactory = new JPAQueryFactory(session);
        QTeam qTeam = QTeam.team;

        try {
            final int batchSize = 50;
            Map<String, Team> teamsByName = new HashMap<>();
            List<String> nameKeys = new ArrayList<>(teams.size());

            for (Team team : teams) {
                String name = team.getName();
                String key = name.toLowerCase(Locale.ROOT);
                teamsByName.put(key, team);
                nameKeys.add(key);
            }

            List<Tuple> existing = queryFactory
                    .select(qTeam.Id, qTeam.Name)
                    .from(qTeam)
                    .where(qTeam.Name.lower().in(nameKeys))
                    .fetch();

            Map<String, Long> existingIds = new HashMap<>();
            for (Tuple row : existing) {
                String name = row.get(qTeam.Name);
                Long id = row.get(qTeam.Id);
                if (name != null && id != null) {
                    existingIds.put(name.toLowerCase(Locale.ROOT), id);
                }
            }

            List<Team> toInsert = new ArrayList<>();
            List<String> insertedKeys = new ArrayList<>();
            for (Map.Entry<String, Team> entry : teamsByName.entrySet()) {
                if (!existingIds.containsKey(entry.getKey())) {
                    toInsert.add(entry.getValue());
                    insertedKeys.add(entry.getKey());
                }
            }

            if (!toInsert.isEmpty()) {
                int insertIndex = 0;
                for (Team team : toInsert) {
                    session.persist(team);
                    insertIndex++;
                    if (insertIndex % batchSize == 0) {
                        session.flush();
                        session.clear();
                    }
                }
                session.flush();

                for (Team team : toInsert) {
                    String name = team.getName();
                    Long id = team.getId();
                    if (name != null && id != null) {
                        existingIds.put(name.toLowerCase(Locale.ROOT), id);
                    }
                }
            }

            for (Team team : teams) {
                Long id = existingIds.get(team.getName().toLowerCase(Locale.ROOT));
                if (id != null) {
                    team.setId(id);
                }
            }

            return true;
        } catch (RuntimeException exception) {
            throw exception;
        }
    }

    public boolean UploadTournament(long leagueId, String turnirName, String turnirFilePath) {
        Tournament tournament = buildTournament(leagueId, turnirName, turnirFilePath);
        List<RawMatchInfo> rawMatches = readRawMatches(turnirFilePath);
        Map<String, Team> teamsByName = new HashMap<>();
        Map<String, Stage> stagesByName = new HashMap<>();

        for (RawMatchInfo rawMatch : rawMatches) {
            String hTeamName = rawMatch.getHTeam();
            String hTeamKey = hTeamName.toLowerCase(Locale.ROOT);
            if (!teamsByName.containsKey(hTeamKey)) {
                Team team = new Team();
                team.setName(hTeamName);
                team.setLogoUrl(rawMatch.getHTeamLogo());
                team.setShortName("");
                teamsByName.put(hTeamKey, team);
            }

            String gTeamName = rawMatch.getGTeam();
            String gTeamKey = gTeamName.toLowerCase(Locale.ROOT);
            if (!teamsByName.containsKey(gTeamKey)) {
                Team team = new Team();
                team.setName(gTeamName);
                team.setLogoUrl(rawMatch.getGTeamLogo());
                team.setShortName("");
                teamsByName.put(gTeamKey, team);
            }

            String stageName = rawMatch.getStage();
            String stageKey = stageName.toLowerCase(Locale.ROOT);
            if (!stagesByName.containsKey(stageKey)) {
                Stage stage = new Stage();
                stage.setName(stageName);
                stagesByName.put(stageKey, stage);
            }

            Stage stage = stagesByName.get(stageKey);
            Match match = mapMatch(rawMatch);
            stage.getMatches().add(match);

        }

        List<Team> teams = new ArrayList<>(teamsByName.values());
        List<Stage> stages = new ArrayList<>(stagesByName.values());
        int totalMatches = 0;
        for (Stage stage : stages) {
            totalMatches += stage.getMatches().size();
        }
        
        org.hibernate.Transaction transaction = session.beginTransaction();
        try {
            if (!SaveTeams(teams)) {
                transaction.rollback();
                return false;
            }
            Tournament savedTournament = UpgradeTournament(tournament);
            
            if (savedTournament == null) {
                transaction.rollback();
                return false;
            }
            for (Stage stage : stages) {
                stage.setLeagueId(leagueId);
                stage.setTournamentId(savedTournament.getId());
                for (Match match : stage.getMatches()) {
                    adjustMatch(match, savedTournament.getLeagueId(), savedTournament.getId(), stage, teamsByName);
                }
            }
            if (!SaveTournamentData(savedTournament, stages)) {
                transaction.rollback();
                return false;
            }
            transaction.commit();
            return true;
        } catch (RuntimeException exception) {
            transaction.rollback();
            throw exception;
        }
    }

    private Match mapMatch(RawMatchInfo rawMatch) {
        Match match = new Match();
        match.setTour(rawMatch.getTour());
        match.setRound("");
        match.setDate(parseRawMatchDate(rawMatch.getDate()));
        match.setHScore(rawMatch.getHTeamScore());
        match.setGScore(rawMatch.getGTeamScore());

        Team hTeam = new Team();
        hTeam.setName(rawMatch.getHTeam());
        match.setHTeam(hTeam);

        Team gTeam = new Team();
        gTeam.setName(rawMatch.getGTeam());
        match.setGTeam(gTeam);

        return match;
    }

    private void adjustMatch(Match match, Long leagueId, Long tournamentId, Stage stage, Map<String, Team> teamsByName) {
        match.setLeagueId(leagueId);
        match.setTournamentId(tournamentId);

        String hTeamName = match.getHTeam().getName();
        Team hTeam = teamsByName.get(hTeamName.toLowerCase(Locale.ROOT));
        if (hTeam != null) {
            match.setHTeamId(hTeam.getId());
        }

        String gTeamName = match.getGTeam().getName();
        Team gTeam = teamsByName.get(gTeamName.toLowerCase(Locale.ROOT));
        if (gTeam != null) {
            match.setGTeamId(gTeam.getId());
        }
    }

    private OffsetDateTime parseRawMatchDate(String rawDate) {
        DateTimeFormatter fourDigitWithSeconds = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss", Locale.forLanguageTag("ru-RU"));
        DateTimeFormatter fourDigitWithMinutes = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.forLanguageTag("ru-RU"));
        DateTimeFormatter fourDigitDate = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.forLanguageTag("ru-RU"));
        DateTimeFormatter twoDigitWithMinutes = new DateTimeFormatterBuilder()
                .appendPattern("dd.MM.")
                .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
                .appendPattern(" HH:mm")
                .toFormatter(Locale.forLanguageTag("ru-RU"));
        DateTimeFormatter twoDigitDate = new DateTimeFormatterBuilder()
                .appendPattern("dd.MM.")
                .appendValueReduced(ChronoField.YEAR, 2, 2, 2000)
                .toFormatter(Locale.forLanguageTag("ru-RU"));

        if (rawDate == null || rawDate.isBlank()) {
            return null;
        }
        if (!isLikelyDate(rawDate)) {
            return null;
        }
        String normalized = rawDate.replace(",", "").trim();
        DateTimeFormatter[] formatters = new DateTimeFormatter[] {
                fourDigitWithSeconds,
                fourDigitWithMinutes,
                fourDigitDate,
                twoDigitWithMinutes,
                twoDigitDate
        };

        for (DateTimeFormatter formatter : formatters) {
            try {
                LocalDateTime dateTime = LocalDateTime.parse(normalized, formatter);
                return dateTime.atZone(ZoneId.of("Europe/Moscow")).toOffsetDateTime();
            } catch (DateTimeParseException ignored) {
                // Try next pattern
            }
            try {
                LocalDate date = LocalDate.parse(normalized, formatter);
                return date.atStartOfDay(ZoneId.of("Europe/Moscow")).toOffsetDateTime();
            } catch (DateTimeParseException ignored) {
                // Try next pattern
            }
        }

        return null;
    }

    private boolean isLikelyDate(String rawDate) {
        int digitCount = 0;
        boolean hasDot = false;
        for (int i = 0; i < rawDate.length(); i++) {
            char ch = rawDate.charAt(i);
            if (ch >= '0' && ch <= '9') {
                digitCount++;
            } else if (ch == '.') {
                hasDot = true;
            }
        }
        return digitCount >= 4 && hasDot;
    }

    private Tournament buildTournament(long leagueId, String turnirName, String turnirFilePath) {
        String fileName = java.nio.file.Paths.get(turnirFilePath).getFileName().toString();
        if (fileName.endsWith(".json")) {
            fileName = fileName.substring(0, fileName.length() - ".json".length());
        }
        String seasonPart = fileName.replaceFirst("^Champ_", "");
        Integer stYear;
        Integer fnYear = null;
        if (seasonPart.contains("-")) {
            String[] parts = seasonPart.split("-", 2);
            stYear = Integer.parseInt(parts[0]);
            fnYear = Integer.parseInt(parts[1]);
        } else {
            stYear = Integer.parseInt(seasonPart);
        }

        Tournament tournament = new Tournament();
        tournament.setLeagueId(leagueId);
        tournament.setName(turnirName);
        tournament.setStYear(stYear);
        tournament.setFnYear(fnYear);
        return tournament;
    }

    private List<RawMatchInfo> readRawMatches(String turnirFilePath) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        try {
            return mapper.readValue(new File(turnirFilePath), new TypeReference<List<RawMatchInfo>>() {});
        } catch (IOException exception) {
            throw new RuntimeException("Failed to read raw matches from " + turnirFilePath, exception);
        }
    }

}
