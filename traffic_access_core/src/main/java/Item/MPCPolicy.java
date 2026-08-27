package Item;

import java.util.Date;

public class MPCPolicy {
	private String ownerName;
    private String consumerName;
	private String policyName;
    private long policyId;
    private long streamID;
    private Date startTime;
    private Date endTime;
    private long minGranularity;
    
    
    
	public MPCPolicy(String ownerName, String consumerName, long policyId, long streamID, Date startTime, Date endTime,
					 long minGranularity) {
		super();
		this.ownerName = ownerName;
		this.consumerName = consumerName;
		this.policyId = policyId;
		this.streamID = streamID;
		this.startTime = startTime;
		this.endTime = endTime;
		this.minGranularity = minGranularity;
	}

	public String getPolicyName() {
		return policyName;
	}

	public void setPolicyName(String policyName) {
		this.policyName = policyName;
	}

	public MPCPolicy() {
		// TODO Auto-generated constructor stub
	}

	public String getOwnerName() {
		return ownerName;
	}
	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}
	public String getConsumerName() {
		return consumerName;
	}
	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}
	public long getPolicyId() {
		return policyId;
	}
	public void setPolicyId(long policyId) {
		this.policyId = policyId;
	}
	public long getStreamID() {
		return streamID;
	}
	public void setStreamID(long streamID) {
		this.streamID = streamID;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	public long getMinGranularity() {
		return minGranularity;
	}
	public void setMinGranularity(long minGranularity) {
		this.minGranularity = minGranularity;
	}
    
    
}
