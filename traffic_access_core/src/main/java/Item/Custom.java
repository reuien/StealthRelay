package Item;

public class Custom {
	
	private String idnum;
	private String name;
	private String password;
	private String identity;
	
	public Custom(String name,String idnum,String password,String identity) {
		
		this.name = name;
		this.idnum = idnum;
		this.password = password;	
		this.identity = identity;

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

	public String getPassword() {

		return password;

	}

	public void setPassword(String password) {

		this.password = password;

	}
	
	public String getidentity() {

		return identity;

	}
	
	public void setIdentity(String identity) {

		this.identity = identity;

	}
	



	@Override
	public String toString() {
		return name + ","+ idnum + ","+ password +","+identity+".";
	}

}
