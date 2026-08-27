package streamHandling;

import java.io.Serializable;
import java.util.Date;

public class DataPoint implements Serializable, Comparable<DataPoint> {
    private final Date timestamp;
    private final long value;
    public DataPoint(Date timestamp, long value) {
        this.timestamp = timestamp;
        this.value = value;
    }
    public Date getTimestamp() {
        return timestamp;
    }
    public long getValue() {
        return value;
    }
    @Override
    public String toString() {
        return "DataPoint{" +
                "timestamp=" + timestamp +
                ", value=" + value +
                '}';
    }
    @Override
    public int compareTo(DataPoint dataPoint) {
        long timeDelta = this.timestamp.getTime() - dataPoint.getTimestamp().getTime();

        if (timeDelta == 0) {
            long valueDelta = this.value - dataPoint.getValue();
            if (valueDelta > Integer.MAX_VALUE) {
                return +1;
            } else if (valueDelta < Integer.MIN_VALUE) {
                return -1;
            } else {
                return (int) valueDelta;
            }
        }

        if (timeDelta > Integer.MAX_VALUE) {
            return +1;
        } else if (timeDelta < Integer.MIN_VALUE) {
            return -1;
        } else {
            return (int) timeDelta;
        }
    }

}
