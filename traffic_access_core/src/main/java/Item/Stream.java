package Item;

import java.io.Serializable;
import java.util.Date;

public class Stream implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2553319692772341563L;
	private long id;
	private String name;
	private String desciption;
	private Date starttime ;
	private Date endtime ;
	private long mingranularity;
	private long granularity;
	

	
	public Stream(long id, String name, String desciption, Date starttime, Date endtime, long mingranularity,
			long granularity) {
		super();
		this.id = id;
		this.name = name;
		this.desciption = desciption;
		this.starttime = starttime;
		this.endtime = endtime;
		this.mingranularity = mingranularity;
		this.granularity = granularity;
	}
	


	public Stream() {
		// TODO Auto-generated constructor stub
	}


	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesciption() {
		return desciption;
	}
	public void setDesciption(String desciption) {
		this.desciption = desciption;
	}
	public Date getStarttime() {
		return starttime;
	}
	public void setStarttime(Date starttime) {
		this.starttime = starttime;
	}
	public Date getEndtime() {
		return endtime;
	}
	public void setEndtime(Date endtime) {
		this.endtime = endtime;
	}
	public long getMingranularity() {
		return mingranularity;
	}
	public void setMingranularity(long mingranularity) {
		this.mingranularity = mingranularity;
	}
	public long getGranularity() {
		return granularity;
	}
	public void setGranularity(long granularity) {
		this.granularity = granularity;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public String toString() {
		return  id + "," + name + "," + desciption + "," + starttime+ "," + endtime + "," + mingranularity + "," + granularity;
	}
	
	
	
	
}
