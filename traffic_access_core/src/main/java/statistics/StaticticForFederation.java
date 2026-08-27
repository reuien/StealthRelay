package statistics;

import java.util.List;

public class StaticticForFederation {
    private long sum;
    private long count;
    private long square;
    private Double average;
    private Double std;
    private Double variance;
    public StaticticForFederation(List<Long> decRes) {
        this.sum = decRes.get(0);
        this.count = decRes.get(1);
        this.square = decRes.get(2);
        this.average = (sum / (double) count);
        this.variance = (square / (double) count) - (sum / (double) count) * (sum / (double) count);
        this.std = Math.sqrt(variance);
    }

    public long getSum() {
        return sum;
    }

    public long getCount() {
        return count;
    }

    public long getSquare() {
        return square;
    }

    public Double getAverage() {
        return average;
    }

    public Double getStd() {
        return std;
    }

    public Double getVariance() {
        return variance;
    }

    @Override
    public String toString() {
        return "StaticticForFederation{" +
                "sum=" + sum +
                ", count=" + count +
                ", square=" + square +
                ", AVE=" + String.format("%.2f", average) +
                ", STD=" + String.format("%.2f", std) +
                ", VAR=" + String.format("%.2f", variance) +
                '}';
    }
}
