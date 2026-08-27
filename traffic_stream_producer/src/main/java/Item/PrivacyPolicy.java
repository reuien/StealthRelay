package Item;

import java.io.Serializable;
import java.util.Date;

public class PrivacyPolicy	implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1094740780592045357L;
	private String usrName;
    private String custname;
    private long PrivacyPolicyId;
    private long StreamID;
    private Date startTime;
    private Date endTime;
    private long minGranularity;

    public PrivacyPolicy(String usrName,String custname ,long PrivacyPolicyId,long StreamID, Date startTime, Date endTime, long minGranularity){
        this.usrName = usrName;
        this.custname = custname;
        this.PrivacyPolicyId = PrivacyPolicyId;
		this.StreamID = StreamID;
        this.startTime = startTime;
        this.endTime = endTime;
        this.minGranularity = minGranularity;
       
    }


	public PrivacyPolicy() {
		
		// TODO Auto-generated constructor stub
	}

	public String getUsrName() {
		return usrName;
	}

	public void setUsrName(String usrName) {
		this.usrName = usrName;
	}

	public String getCustname() {
		return custname;
	}

	public void setCustname(String custname) {
		this.custname = custname;
	}

	public long getPrivacyPolicyId() {
		return PrivacyPolicyId;
	}

	public void setPrivacyPolicyId(long privacyPolicyId) {
		PrivacyPolicyId = privacyPolicyId;
	}

	public long getStreamID() {
		return StreamID;
	}

	public void setStreamID(long streamID) {
		StreamID = streamID;
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
	
	@Override
	public String toString() {
		return  usrName + "," + custname + "," + PrivacyPolicyId + "," + StreamID+ "," + startTime + "," + endTime + "," + minGranularity;
	}



  

}
