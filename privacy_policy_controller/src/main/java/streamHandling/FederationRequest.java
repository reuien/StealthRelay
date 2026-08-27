package streamHandling;

import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.util.ArrayList;

public class FederationRequest implements Serializable {
    private final String consumerName;
    private final ArrayList<Pair<String, Long>> nameAndStreamList;
    private final long fromTime;
    private final long toTime;

    public FederationRequest(String consumerName, ArrayList<Pair<String, Long>> nameAndStreamList, long fromTime, long toTime){
        this.consumerName = consumerName;
        this.nameAndStreamList = nameAndStreamList;
        this.fromTime = fromTime;
        this.toTime = toTime;
    }

    public String getConsumerName() {
        return consumerName;
    }

    public ArrayList<Pair<String, Long>> getNameAndStreamList() {
        return nameAndStreamList;
    }

    public long getFromTime() {
        return fromTime;
    }

    public long getToTime() {
        return toTime;
    }

}
