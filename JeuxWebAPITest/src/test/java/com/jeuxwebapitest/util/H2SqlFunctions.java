package com.jeuxwebapitest.util;

import java.sql.Date;
import java.sql.Timestamp;

public final class H2SqlFunctions {
    private H2SqlFunctions() {
    }

    public static Date date(Timestamp timestamp) {
        if (timestamp == null) {
            return null;
        }
        return new Date(timestamp.getTime());
    }
}
