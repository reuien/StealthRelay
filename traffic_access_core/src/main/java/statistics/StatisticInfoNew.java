package statistics;

import streamHandling.Digest;
import streamHandling.Stream;

import java.util.Date;

public class StatisticInfoNew {
    private final long streamId;
    private Digest digest;
    private Double average;
    private Double std;
    private Double variance;
    private long count1;
    private long count2;
    private long count3;
    private long count4;
    private long count5;
    private long count6;

    private Stream corStream;
    public StatisticInfoNew(Stream corStream, Digest digest) {
        this.streamId = digest.getStreamId();
        this.digest = digest;
        this.average = Average.getAverage(digest);
        this.variance = Variance.getVariance(digest);
        this.std = Math.sqrt(variance);
        this.count1 = digest.getCount1();
        this.count2 = digest.getCount2();
        this.count3 = digest.getCount3();
        this.count4 = digest.getCount4();
        this.count5 = digest.getCount5();
        this.count6 = digest.getCount6();
        this.corStream = corStream;
    }
    public static StatisticInfoNew getStatisticInfo(Stream corStream, Digest digest){
        return new StatisticInfoNew(corStream, digest);
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
                ", <60：" + digest.getCount1() +
                ", 60-69：" + digest.getCount2() +
                ", 70-79：" + digest.getCount3() +
                ", 80-89：" + digest.getCount4() +
                ", 90-99：" + digest.getCount5() +
                ", >=100：" + digest.getCount6() +
                " ";
    }


}
