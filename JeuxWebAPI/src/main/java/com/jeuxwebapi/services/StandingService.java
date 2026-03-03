package com.jeuxwebapi.services;

import com.jeuxwebapi.models.StandingDto;
import com.jeuxwebapi.results.ServiceListResult;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.Tuple;
import java.util.List;
import org.hibernate.reactive.mutiny.Mutiny;

@ApplicationScoped
public class StandingService {
    private static final String STANDINGS_SQL = """
            select *
            from football."getTournamentStandings"(:leagueId, :tournamentId, :stageId)
            """;

    @Inject
    Mutiny.SessionFactory sessionFactory;

    public Uni<ServiceListResult<StandingDto>> getStandings(Long leagueId, Long tournamentId, Long stageId) {
        ServiceListResult<StandingDto> invalidResult = validateRequiredParams(leagueId, tournamentId, stageId);
        if (invalidResult != null) {
            return Uni.createFrom().item(invalidResult);
        }

        return sessionFactory.withSession(session -> session.createNativeQuery(STANDINGS_SQL)
                .setParameter("leagueId", leagueId)
                .setParameter("tournamentId", tournamentId)
                .setParameter("stageId", stageId)
                .getResultList()
                .map(this::toResult));
    }

    private ServiceListResult<StandingDto> toResult(List<?> rows) {
        List<StandingDto> items = rows.stream()
                .map(StandingService::toStandingDto)
                .toList();

        ServiceListResult<StandingDto> result = new ServiceListResult<>();
        result.setResult(true);
        result.setItems(items);
        result.setTotal(items.size());
        return result;
    }

    private static ServiceListResult<StandingDto> validateRequiredParams(Long leagueId, Long tournamentId, Long stageId) {
        if (leagueId != null && tournamentId != null && stageId != null) {
            return null;
        }
        ServiceListResult<StandingDto> result = new ServiceListResult<>();
        result.setResult(false);
        result.setMessage("Параметры leagueId, tournamentId и stageId обязательны.");
        result.setItems(List.of());
        result.setTotal(0);
        return result;
    }

    private static StandingDto toStandingDto(Object rowObject) {
        Object[] row;
        if (rowObject instanceof Object[] values) {
            row = values;
        } else if (rowObject instanceof Tuple tuple) {
            row = tuple.toArray();
        } else {
            throw new IllegalStateException("Неожиданный тип строки standings: " + rowObject.getClass().getName());
        }

        return new StandingDto(
                toLong(row[0]),
                toStringValue(row[1]),
                toInt(row[2]),
                toInt(row[3]),
                toInt(row[4]),
                toInt(row[5]),
                toInt(row[6]),
                toInt(row[7]),
                toInt(row[8]),
                toInt(row[9]),
                toInt(row[10]),
                toInt(row[11]),
                toInt(row[12]),
                toInt(row[13]),
                toInt(row[14]),
                toInt(row[15]),
                toInt(row[16]),
                toInt(row[17]),
                toInt(row[18]),
                toInt(row[19]),
                toInt(row[20]),
                toInt(row[21]),
                toInt(row[22]),
                toInt(row[23]),
                toInt(row[24]),
                toInt(row[25])
        );
    }

    private static long toLong(Object value) {
        return value == null ? 0L : ((Number) value).longValue();
    }

    private static int toInt(Object value) {
        return value == null ? 0 : ((Number) value).intValue();
    }

    private static String toStringValue(Object value) {
        return value == null ? null : value.toString();
    }
}
