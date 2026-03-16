package com.jeuxwebapi.services.arrange_strategies;

import com.jeuxwebapi.models.StandingMatchType;
import com.jeuxwebapi.models.TournamentDto;

public class ArrangeStrategyFactory<TSD> {
    private static final String STRATEGY_1992_1994 = "arrange_strategy_1992_1994.json";
    private static final String STRATEGY_1995_2013 = "arrange_strategy_1995_2013.json";
    private static final String STRATEGY_2014_2017 = "arrange_strategy_2014_2017.json";
    private static final String STRATEGY_SINCE_2018 = "arrange_strategy_since_2018.json";

    private final TournamentDto tournament;
    private final StandingMatchType matchType;

    public ArrangeStrategyFactory(TournamentDto tournament) {
        this(tournament, StandingMatchType.ALL);
    }

    public ArrangeStrategyFactory(TournamentDto tournament, StandingMatchType matchType) {
        this.tournament = tournament;
        this.matchType = matchType == null ? StandingMatchType.ALL : matchType;
    }

    public IArrangeStrategy<TSD> create() {
        if (tournament == null) {
            return new ArrangeStrategyBase<>("", matchType);
        }
        return new ArrangeStrategyBase<>(resolveResourceFile(tournament.getStYear()), matchType);
    }

    public static <TSD> IArrangeStrategy<TSD> create(TournamentDto tournament, StandingMatchType matchType) {
        return new ArrangeStrategyFactory<TSD>(tournament, matchType).create();
    }

    private String resolveResourceFile(int stYear) {
        if (stYear >= 1992 && stYear <= 1994) {
            return STRATEGY_1992_1994;
        }
        if (stYear >= 1995 && stYear <= 2013) {
            return STRATEGY_1995_2013;
        }
        if (stYear >= 2014 && stYear <= 2017) {
            return STRATEGY_2014_2017;
        }
        if (stYear >= 2018) {
            return STRATEGY_SINCE_2018;
        }
        return "";
    }
}
