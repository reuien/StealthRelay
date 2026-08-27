package statistics;

import java.util.List;

public class StaticticForFederationNew {
    private long sum;
    private long count;
    private long square;
    private long count1;
    private long count2;
    private long count3;
    private long count4;
    private long count5;
    private long count6;
    private Double average;
    private Double std;
    private Double variance;
    public StaticticForFederationNew(List<Long> decRes) {
        this.sum = decRes.get(0);
        this.count = decRes.get(1);
        this.square = decRes.get(2);
        this.count1 = decRes.get(3);
        this.count2 = decRes.get(4);
        this.count3 = decRes.get(5);
        this.count4 = decRes.get(6);
        this.count5 = decRes.get(7);
        this.count6 = decRes.get(8);
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
    public long getCount1() {
        return count1;
    }
    public long getCount2() {
        return count2;
    }
    public long getCount3() {
        return count3;
    }
    public long getCount4() {
        return count4;
    }
    public long getCount5() {
        return count5;
    }
    public long getCount6() {
        return count6;
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
