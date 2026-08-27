package streamHandling;

public class FederationPolicy {
    private final String consumerName;
    private final String ownerName;
    private final long federationPolicyId;
    private final long streamId;
    private final long startTime;
    private final long endTime;

    public FederationPolicy(String consumerName, String ownerName, long federationPolicyId, long streamId, long startTime, long endTime) {
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
