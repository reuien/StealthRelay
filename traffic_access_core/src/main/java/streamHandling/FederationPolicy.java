package streamHandling;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FederationPolicy {
    private final String consumerName;
    private final String ownerName;
    private final long federationPolicyId;
    private final long streamId;
    private final long startTime;
    private final long endTime;

    @JsonCreator
    public FederationPolicy(@JsonProperty("consumerName")String consumerName, @JsonProperty("ownerName")String ownerName,
                            @JsonProperty("federationPolicyId")long federationPolicyId, @JsonProperty("streamId")long streamId,
                            @JsonProperty("startTime")long startTime, @JsonProperty("endTime")long endTime) {
        this.consumerName = consumerName;
        this.ownerName = ownerName;
        this.federationPolicyId = federationPolicyId;
        this.streamId = streamId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public long getFederationPolicyId() {
        return federationPolicyId;
    }

    public long getStreamId() {
        return streamId;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getEndTime() {
        return endTime;
    }

}
