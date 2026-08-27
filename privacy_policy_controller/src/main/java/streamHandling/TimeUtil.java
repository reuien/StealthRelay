package streamHandling;

import java.time.Clock;
import java.util.*;

public class TimeUtil {
    static Clock clock = Clock.systemDefaultZone();
    public static void setClock(Clock clock) {
        TimeUtil.clock = clock;
    }
    public static long getChunkIdAtTime(Stream associatedStream, Date timestamp) {
        return getChunkIdAtTime(associatedStream, timestamp.getTime());
    }
    public static long getChunkIdAtTime(Stream associatedStream, long timestamp) {
        if (associatedStream.getStartDate().getTime() > timestamp) {
            return -1;
        }
        long timeOffset = timestamp - associatedStream.getStartDate().getTime();
        return (long) Math.floor((double) timeOffset / associatedStream.getChunkSize());
    }

    public static Date getDateAtLastFullMinute() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(clock.millis());
        date.set(Calendar.MINUTE, date.get(Calendar.MINUTE));
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        return date.getTime();
    }

    public static long getChunkStartTime(long streamStartTime, long chunkSize, long chunkId) {
        return streamStartTime + (chunkSize * chunkId);
    }

    public static long getChunkEndTime(long streamStartTime, long chunkSize, long chunkId) {
        return streamStartTime + (chunkSize * (chunkId + 1)) - 1;
    }

    public static long getChunkStartTime(Stream correspondingStream, long chunkId) {
        return correspondingStream.getStartDate().getTime() + (correspondingStream.getChunkSize() * chunkId);
    }

    public static long getChunkEndTime(Stream correspondingStream, long chunkId) {
        return correspondingStream.getStartDate().getTime() + (correspondingStream.getChunkSize() * (chunkId + 1)) - 1;
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

        public static List<Precision> getGreaterPrecisions(Precision precision) {

            List<Precision> precisions = new ArrayList<>();
            for (Precision otherPrecision : Precision.values()) {
                if (otherPrecision.getMillis() > precision.getMillis()) {
                    precisions.add(otherPrecision);
                }
            }
            return precisions;
        }

        public long getMillis() {
            return millis;
        }
    }

    public static Precision[] getHigherPrecisions(Precision precision) {
        Precision[] precisions = Precision.values();
        int selectedOrdinal = precision.ordinal();
        return Arrays.stream(precisions)
                .filter(p -> p.ordinal() > selectedOrdinal)
                .toArray(Precision[]::new);
    }

}
