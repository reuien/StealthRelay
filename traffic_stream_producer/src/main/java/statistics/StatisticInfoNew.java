package statistics;

import streamHandling.Digest;

public class StatisticInfoNew {
    private final long streamId;
    private Digest digest;
    private Double average;
    private Double std;
    private Double variance;
    public StatisticInfoNew(Digest digest) {
        this.streamId = digest.getStreamId();
        this.digest = digest;
        this.average = Average.getAverage(digest);
        this.variance = Variance.getVariance(digest);
        this.std = Math.sqrt(variance);
    }
    public static StatisticInfoNew getStatisticInfo(Digest digest){
        return new StatisticInfoNew(digest);
    }
/*    public StatisticInfo(List<Digest> digests) {
        this.streamId = digests.get(0).getCorrespondingStreamId();
        this.digests = digests;
        this.average = Average.getAverage(digests);
        this.variance = Variance.getVariance(digests);
        this.std = Math.sqrt(variance);
    }
    public StatisticInfo getStatisticInfo(List<Digest> digests){
        return new StatisticInfo(digests);
    }
    */

    public String toString(){
        return "统计信息: { " +
                "流：" + streamId +
                ", 数据块From：" + digest.getChunkIdFrom()+
                ", To：" + digest.getChunkIdTo() +
                ", AVE=" + String.format("%.2f", average) +
                ", STD=" + String.format("%.2f", std) +
                ", VAR=" + String.format("%.2f", variance) +
                ", 摘要: " +
                "sum="+digest.getSum()+", "+
                "count="+digest.getCount()+", "+
                "square="+digest.getSquare()+" "+
                " }";
    }


}
