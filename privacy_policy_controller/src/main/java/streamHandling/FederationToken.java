package streamHandling;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class FederationToken implements Serializable {
    private final String consumerName;
    private final List<Long> streamIdList;
    private final List<Long> streamStartTimeList;
    private final List<Long> chunkSizeList;
    private final ArrayList<Long> federationKeys;


    public FederationToken(String consumerName, List<Long> streamIdList, List<Long> streamStartTimeList, List<Long> chunkSizeList, ArrayList<Long> federationKeys){
        this.consumerName = consumerName;
        this.streamIdList = streamIdList;
        this.streamStartTimeList = streamStartTimeList;
        this.chunkSizeList = chunkSizeList;
        this.federationKeys = federationKeys;
    }

    public String getConsumerName(){ return consumerName; }

    public List<Long> getStreamIdList() {
        return streamIdList;
    }

    public List<Long> getStreamStartTimeList() {
        return streamStartTimeList;
    }

    public List<Long> getChunkSizeList() {
        return chunkSizeList;
    }

    public ArrayList<Long> getFederationKeys() {
        return federationKeys;
    }

}
