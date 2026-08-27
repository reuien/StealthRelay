package streamHandling;

import java.util.Date;

public class Request {
    private final String consumerUsrName;
    private final String ownerUsrName;
    private final long streamID;
    private final Date startTime;
    private final Date endTime;
    private final long granularity;
    private long[] chunkInterval;
    public Request(String consumerUsrName, String ownerUsrName, long streamID, Date startTime, Date endTime, long granularity){
        this.consumerUsrName = consumerUsrName;
        this.ownerUsrName = ownerUsrName;
        this.streamID = streamID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.granularity = granularity;
    }

    public String getConsumerUsrName() {
        return consumerUsrName;
    }

    public String getOwnerUsrName() {
        return ownerUsrName;
    }

    public long getStreamID() {
        return streamID;
    }

    public Date getStartTime() {
        return startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public long getGranularity() {
        return granularity;
    }

}
