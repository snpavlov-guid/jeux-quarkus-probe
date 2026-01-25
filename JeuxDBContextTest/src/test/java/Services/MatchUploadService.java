package Services;

import Entities.League;
import Entities.QLeague;
import Entities.QTournament;
import Entities.Tournament;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
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

    public Tournament UpsertTournament(Tournament turnir) {
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
            return existing;
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
}
