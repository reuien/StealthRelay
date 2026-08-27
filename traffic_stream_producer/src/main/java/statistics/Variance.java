package statistics;

import streamHandling.Digest;

public class Variance {
    static long sum = 0;
    static long count = 0;
    static long square = 0;

    public static Double getVariance(Digest digest) {
        sum = digest.getSum();
        count = digest.getCount();
        square = digest.getSquare();
        if (count == 0) {
            return null;
        }
        return (square / (double) count) - (sum / (double) count) * (sum / (double) count);
    }

}
