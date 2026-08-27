package streamHandling;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class PrivacyPolicy {
    private final String consumerUsrName;
    private final String ownerUsrName;
    private final long privacyPolicyId;
    private final Stream correspondingStream;
    private final Date startTime;
    private final Date endTime;
    private final long startChunkId;
    private final long endChunkId;
    private final long minGranularity;
    private long[] chunkInterval;

    @JsonCreator
    public PrivacyPolicy(@JsonProperty("consumerUsrName")String consumerUsrName, @JsonProperty("ownerUsrName")String ownerUsrName,
                         @JsonProperty("privacyPolicyId")long privacyPolicyId, @JsonProperty("correspondingStream")Stream correspondingStream,
                         @JsonProperty("startTime")Date startTime, @JsonProperty("endTime")Date endTime,
                         @JsonProperty("minGranularity")long minGranularity) throws Exception {
        this.consumerUsrName = consumerUsrName;
        this.ownerUsrName = ownerUsrName;
        this.privacyPolicyId = privacyPolicyId;
        this.correspondingStream = correspondingStream;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startChunkId = TimeUtil.getChunkIdAtTime(correspondingStream, startTime);
        this.endChunkId = TimeUtil.getChunkIdAtTime(correspondingStream, endTime);
        this.minGranularity = minGranularity;
        this.chunkInterval = new long[2];
        chunkInterval[0] = startChunkId;
        chunkInterval[1] = endChunkId;
    }

    public String getOwnerUsrName() {
        return ownerUsrName;
    }
    public String getConsumerUsrName() {
        return consumerUsrName;
    }
    public long getPrivacyPolicyId() {
        return privacyPolicyId;
    }
    public Stream getCorrespondingStream() {
        return correspondingStream;
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
