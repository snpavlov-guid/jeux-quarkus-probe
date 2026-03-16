package com.jeuxwebapi.services.arrange_strategies;

import java.util.Comparator;
import java.util.function.Function;

public interface IArrangeStrategy<TSD> {
    Comparator<TSD> getMainOrderComparator();
    Comparator<TSD> getGroupOrderComparator();
    Function<TSD, ?> getGroupKeyFunction();
}