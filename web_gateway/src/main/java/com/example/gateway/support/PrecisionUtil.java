package com.example.gateway.support;

import streamHandling.TimeUtil;

public final class PrecisionUtil {

    private PrecisionUtil() {}

    public static TimeUtil.Precision fromMillis(long millis) {
        TimeUtil.Precision best = TimeUtil.Precision.ONE_SECOND;
        for (TimeUtil.Precision p : TimeUtil.Precision.values()) {
            if (p.getMillis() == millis) {
                return p;
            }
            if (p.getMillis() <= millis && p.getMillis() > best.getMillis()) {
                best = p;
            }
        }
        return best;
    }

    public static TimeUtil.Precision fromName(String name) {
        if (name == null) {
            return TimeUtil.Precision.ONE_SECOND;
        }
        try {
            return TimeUtil.Precision.valueOf(name.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return TimeUtil.Precision.ONE_SECOND;
        }
    }

    public static long toMillis(TimeUtil.Precision p) {
        return p == null ? 0 : p.getMillis();
    }
}
