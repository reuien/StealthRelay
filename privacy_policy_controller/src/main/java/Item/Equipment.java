package Item;

public class Equipment  {
	
	private String idnum;
	private String name;
	private String owner_id;
	private String port;
	private String iP;
	
	
	
	
	public Equipment(String owner_id,String idnum, String name, String port,String ip) {
		super();
		this.idnum = idnum;
		this.name = name;
		this.owner_id = owner_id;
		this.port =port;
		this.iP = ip;
	}
	

	
	@Override
	public String toString() {
		return "Equipment [idnum=" + idnum + ", name=" + name + ", owner=" + owner_id + ", port="+port+", ip="+iP+"]";
	}
	public String getIdnum() {
		return idnum;
	}
	public void setIdnum(String idnum) {
		this.idnum = idnum;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getOwner() {
		return owner_id;
	}
	public void setOwner(String owner) {
		this.owner_id = owner;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getiP() {
		return iP;
	}

	public void setiP(String iP) {
		this.iP = iP;
	}

	

}
