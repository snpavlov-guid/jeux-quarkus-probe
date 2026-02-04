package Services;

import Entities.League;
import Entities.Match;
import Entities.Stage;
import Entities.Team;
import Entities.Tournament;
import Models.RawMatchInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.smallrye.mutiny.Uni;
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
import org.hibernate.reactive.mutiny.Mutiny;

public class MatchUploadService {
    private final Mutiny.Session session;

    public MatchUploadService(Mutiny.Session session) {
        this.session = session;
    }

    public Uni<League> UpsertLeague(String leagueName) {
        String normalized = leagueName == null ? "" : leagueName.toLowerCase(Locale.ROOT);
        return session.createQuery(
                        "from League l where lower(l.Name) = :name",
                        League.class
                )
                .setParameter("name", normalized)
                .getResultList()
                .map(list -> list.isEmpty() ? null : list.get(0))
                .chain(existing -> {
                    if (existing != null) {
                        return Uni.createFrom().item(existing);
                    }
                    League league = new League();
                    league.setName(leagueName);
                    return session.persist(league)
                            .chain(session::flush)
                            .replaceWith(league);
                });
    }

    public Uni<Tournament> UpgradeTournament(Tournament turnir) {
        Long leagueId = turnir.getLeagueId();
        League league = turnir.getLeague();
        Integer fnYear = turnir.getFnYear();

        if (leagueId == null && league != null) {
            leagueId = league.getId();
            turnir.setLeagueId(leagueId);
        }

        if (leagueId == null) {
            throw new IllegalArgumentException("Tournament LeagueId is required");
        }
        final Long finalLeagueId = leagueId;

        String baseQuery = "from Tournament t where t.LeagueId = :leagueId and lower(t.Name) = :name and t.StYear = :stYear";
        String queryText = fnYear == null ? baseQuery + " and t.FnYear is null" : baseQuery + " and t.FnYear = :fnYear";
        Mutiny.SelectionQuery<Tournament> query = session.createQuery(queryText, Tournament.class)
                .setParameter("leagueId", finalLeagueId)
                .setParameter("name", turnir.getName().toLowerCase(Locale.ROOT))
                .setParameter("stYear", turnir.getStYear());
        if (fnYear != null) {
            query.setParameter("fnYear", fnYear);
        }

        return query.getResultList()
                .map(list -> list.isEmpty() ? null : list.get(0))
                .chain(existing -> {
                    Uni<Void> deleteStep = existing == null
                            ? Uni.createFrom().voidItem()
                            : session.remove(existing).chain(session::flush);

                    Tournament toPersist = new Tournament();
                    toPersist.setLeagueId(finalLeagueId);
                    toPersist.setName(turnir.getName());
                    toPersist.setStYear(turnir.getStYear());
                    toPersist.setFnYear(fnYear);

                    return deleteStep
                            .chain(() -> session.persist(toPersist))
                            .chain(session::flush)
                            .replaceWith(toPersist);
                });
    }

    public Uni<Boolean> SaveTournamentData(Tournament turnir, List<Stage> stages) {
        Uni<Void> chain = Uni.createFrom().voidItem();
        for (Stage stage : stages) {
            chain = chain.chain(() -> session.persist(stage));
        }
        return chain
                .chain(session::flush)
                .chain(() -> {
                    Uni<Void> matchChain = Uni.createFrom().voidItem();
                    for (Stage stage : stages) {
                        Long stageId = stage.getId();
                        for (Match match : stage.getMatches()) {
                            if (match.getStageId() == null) {
                                match.setStageId(stageId);
                            }
                            matchChain = matchChain.chain(() -> session.persist(match));
                        }
                    }
                    return matchChain;
                })
                .chain(session::flush)
                .replaceWith(true);
    }

    public Uni<Boolean> SaveTeams(List<Team> teams) {
        if (teams == null || teams.isEmpty()) {
            return Uni.createFrom().item(true);
        }

        Map<String, Team> teamsByName = new HashMap<>();
        List<String> nameKeys = new ArrayList<>(teams.size());
        for (Team team : teams) {
            String name = team.getName();
            String key = name.toLowerCase(Locale.ROOT);
            teamsByName.put(key, team);
            nameKeys.add(key);
        }

        return session.createQuery(
                        "select t from Team t where lower(t.Name) in :names",
                        Team.class
                )
                .setParameter("names", nameKeys)
                .getResultList()
                .chain(existing -> {
                    Map<String, Long> existingIds = new HashMap<>();
                    for (Team team : existing) {
                        if (team.getName() != null && team.getId() > 0) {
                            existingIds.put(team.getName().toLowerCase(Locale.ROOT), team.getId());
                        }
                    }

                    List<Team> toInsert = new ArrayList<>();
                    for (Map.Entry<String, Team> entry : teamsByName.entrySet()) {
                        if (!existingIds.containsKey(entry.getKey())) {
                            toInsert.add(entry.getValue());
                        }
                    }

                    Uni<Void> insertChain = Uni.createFrom().voidItem();
                    for (Team team : toInsert) {
                        insertChain = insertChain.chain(() -> session.persist(team));
                    }

                    return insertChain
                            .chain(session::flush)
                            .replaceWith(() -> {
                                for (Team team : toInsert) {
                                    if (team.getName() != null && team.getId() > 0) {
                                        existingIds.put(team.getName().toLowerCase(Locale.ROOT), team.getId());
                                    }
                                }
                                for (Team team : teams) {
                                    Long id = existingIds.get(team.getName().toLowerCase(Locale.ROOT));
                                    if (id != null) {
                                        team.setId(id);
                                    }
                                }
                                return true;
                            });
                });
    }

    public Uni<Boolean> UploadTournament(long leagueId, String turnirName, String turnirFilePath) {
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

        return session.withTransaction(tx -> SaveTeams(teams)
                .chain(savedTeams -> {
                    if (!savedTeams) {
                        return Uni.createFrom().item(false);
                    }
                    return UpgradeTournament(tournament)
                            .chain(savedTournament -> {
                                if (savedTournament == null) {
                                    return Uni.createFrom().item(false);
                                }
                                for (Stage stage : stages) {
                                    stage.setLeagueId(leagueId);
                                    stage.setTournamentId(savedTournament.getId());
                                    for (Match match : stage.getMatches()) {
                                        adjustMatch(match, savedTournament.getLeagueId(), savedTournament.getId(), stage, teamsByName);
                                    }
                                }
                                return SaveTournamentData(savedTournament, stages);
                            });
                }));
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
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        try {
            return mapper.readValue(new File(turnirFilePath), new TypeReference<List<RawMatchInfo>>() {});
        } catch (IOException exception) {
            throw new RuntimeException("Failed to read raw matches from " + turnirFilePath, exception);
        }
    }

}
