package streamHandling;

import java.time.Clock;
import java.util.Date;

public class TimeUtilForDC {
    static Clock clock = Clock.systemDefaultZone();
    public static void setClock(Clock clock) {
        TimeUtilForDC.clock = clock;
    }
    public static long getChunkIdAtTime(Date streamStartDate, long chunkSize, Date timestamp) {
        return getChunkIdAtTime(streamStartDate, chunkSize, timestamp.getTime());
    }
    public static long getChunkIdAtTime(Date streamStartDate, long chunkSize, long timestamp) {
        if (streamStartDate.getTime() > timestamp) {
            return -1;
        }
        long timeOffset = timestamp - streamStartDate.getTime();
        return (long) Math.floor((double) timeOffset / chunkSize);
    }

    public static long getChunkStartTime(Date streamStartDate, long chunkSize, long chunkId) {
        return streamStartDate.getTime() + (chunkSize * chunkId);
    }

    public static long getChunkEndTime(Date streamStartDate, long chunkSize, long chunkId) {
        return streamStartDate.getTime() + (chunkSize * (chunkId + 1)) - 1;
    }

    public static void resetClock() {
        clock = Clock.systemDefaultZone();
    }

    public enum Precision {
        ONE_SECOND(1000),
        TEN_SECONDS(10000),
        HALF_MINUTE(30000),
        ONE_MINUTE(60000),
        TEN_MINUTES(600000),
        HALF_HOUR(1800000),
        ONE_HOUR(3600000);

        private final long millis;

        Precision(long s) {
            this.millis = s;
        }

        public long getMillis() {
            return millis;
        }
    }

}
