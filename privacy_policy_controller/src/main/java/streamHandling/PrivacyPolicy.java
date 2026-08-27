package streamHandling;

import controllerNettyServer.PrivacyController;

import java.util.Date;

public class PrivacyPolicy {
    private final String consumerUsrName;
    private final String ownerUsrName;
    private final long privacyPolicyId;
    private final long streamId;
    private final Date startTime;
    private final Date endTime;
    private final long startChunkId;
    private final long endChunkId;
    private final long minGranularity;
    private long[] chunkInterval;

    public PrivacyPolicy(String consumerUsrName, String ownerUsrName, long privacyPolicyId, long streamId,
                         Date startTime, Date endTime, long minGranularity){
        this.consumerUsrName = consumerUsrName;
        this.ownerUsrName = ownerUsrName;
        this.privacyPolicyId = privacyPolicyId;
        this.streamId = streamId;
        this.startTime = startTime;
        this.endTime = endTime;
        Stream stream = PrivacyController.getStream(streamId);
        this.startChunkId = TimeUtil.getChunkIdAtTime(stream, startTime);
        this.endChunkId = TimeUtil.getChunkIdAtTime(stream, endTime);
        this.minGranularity = minGranularity;
        this.chunkInterval = new long[2];
        chunkInterval[0] = startChunkId;
        chunkInterval[1] = endChunkId;
    }

    /*
    private final List<Long> correspondingStreamIDs;
    public stream.PrivacyPolicy(long usrId, long ppId, String topic, long precision, Date startTime, Date endTime) throws Exception {
        this.usrId = usrId;
        this.ppId = ppId;
        this.name = "name";
        this.description = "description";
        this.topic = topic;
        this.precision = precision;
        this.startTime = startTime;
        this.endTime = endTime;
        this.correspondingStreamIDs = computeCorrespondingStreamIDs(startTime, endTime);
        this.messageNumberToAggr = computeMessageNumberToAggr(precision);
    }
    public List<Long> computeCorrespondingStreamIDs(Date startTime, Date endTime){
        List<Long> IDs = new ArrayList<>();
        IDs.add(1L);
        return IDs;
    }
    public long computeMessageNumberToAggr(long precision, long correspondingStreamID) throws Exception {
        Stream.Stream cs = new Stream.Stream("a","a",1L,1000,new Date());
        long timeDelta = cs.getTimeDelta();
        long number = precision/timeDelta;
        return number;
    }
    */

    public String getOwnerUsrName() {
        return ownerUsrName;
    }
    public String getConsumerUsrName() {
        return consumerUsrName;
    }
    public long getPrivacyPolicyId() {
        return privacyPolicyId;
    }
    public long getStreamId() {
        return streamId;
    }
    public Date getStartTime() {
        return startTime;
    }
    public Date getEndTime() {
        return endTime;
    }
    public long getMinGranularity() {
        return minGranularity;
    }
    public long getStartChunkId() {
        return startChunkId;
    }
    public long getEndChunkId() {
        return endChunkId;
    }
    public long[] getChunkInterval(){
        return chunkInterval;
    }

}
