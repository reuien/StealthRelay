package statistics;

import streamHandling.Digest;

public class Average {
    static long sum = 0;
    static long count = 0;

    public static Double getAverage(Digest digest) {
        sum = digest.getSum();
        count = digest.getCount();
        if (count == 0) {
            return null;
        }
        return (sum / (double) count);
    }

}
