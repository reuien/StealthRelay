package Item;

import java.util.Date;

public class History {
	private String usrName;
    private String type;
    private Date time;
    private String StreamID_MPC;
    private Long StreamID;
    private Date startTime;
    private Date endTime;
    private String url;
    
	public History(String usrName, String type, Date time, Long streamID, Date startTime, Date endTime, String url) {
		super();
		this.usrName = usrName;
		this.type = type;
		this.time = time;
		this.StreamID = streamID;
		this.startTime = startTime;
		this.endTime = endTime;
		this.url = url;
	}
	
	public History(String usrName, String type, Date time, String StreamID_MPC, Date startTime, Date endTime, String url) {
		super();
		this.usrName = usrName;
		this.type = type;
		this.time = time;
		this.StreamID_MPC = StreamID_MPC;
		this.startTime = startTime;
		this.endTime = endTime;
		this.url = url;
	}
	public History() {
	
	}
	@Override
	public String toString() {
		return  usrName + "," + type + "," + time + "," + StreamID+ "," + startTime + "," + endTime + "," + url ;
	}
	public History(String usrName, String type, Date time, String streamID_MPC, Long streamID, Date startTime,
			Date endTime, String url) {
		super();
		this.usrName = usrName;
		this.type = type;
		this.time = time;
		StreamID_MPC = streamID_MPC;
		StreamID = streamID;
		this.startTime = startTime;
		this.endTime = endTime;
		this.url = url;
	}
	
	public History(String usrName, String type, Date time) {
		super();
		this.usrName = usrName;
		this.type = type;
		this.time = time;
		
	}

	public String getUsrName() {
		return usrName;
	}
	public void setUsrName(String usrName) {
		this.usrName = usrName;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}

	public String getStreamID_MPC() {
		return StreamID_MPC;
	}

	public void setStreamID_MPC(String streamID_MPC) {
		StreamID_MPC = streamID_MPC;
	}
    
    
}
