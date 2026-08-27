package statistics;

import streamHandling.Digest;
import streamHandling.Stream;

import java.util.Date;

public class StatisticInfo {
    private final long streamId;
    private Digest digest;
    private Double average;
    private Double std;
    private Double variance;
    private Stream corStream;
    public StatisticInfo(Stream corStream, Digest digest) {
        this.streamId = digest.getStreamId();
        this.digest = digest;
        this.average = Average.getAverage(digest);
        this.variance = Variance.getVariance(digest);
        this.std = Math.sqrt(variance);
        this.corStream = corStream;
    }
    public static StatisticInfo getStatisticInfo(Stream corStream, Digest digest){
        return new StatisticInfo(corStream, digest);
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

    public Double getAverage() {
        return average;
    }

    public Double getStd() {
        return std;
    }

    public Double getVariance() {
        return variance;
    }

    public String toString(){
        return "数据块" + digest.getChunkIdFrom()+
                "—" + digest.getChunkIdTo() +
                ", 时间：" + new Date(digest.getStartTime(corStream)) +
                "—" + new Date(digest.getEndTime(corStream)) +
                ", 平均值：" + String.format("%.2f", average) +
                ", 标准差：" + String.format("%.2f", std) +
                ", 方差：" + String.format("%.2f", variance) +
                ", 加和：" + digest.getSum() +
                ", 计数：" + digest.getCount() +
                ", 平方和："+ digest.getSquare() +
                " ";
    }


}
