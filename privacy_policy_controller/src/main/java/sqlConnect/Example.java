package sqlConnect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
public class Example { 
	public static void main(String args[]) 
	{
		try
		{ 
			Class.forName("com.mysql.cj.jdbc.Driver");   //加载MYSQL JDBC驱动程序  
			//Class.forName("org.gjt.mm.mysql.Driver"); 
			System.out.println("成功加载Mysql驱动程序!"); 
		 } 
		catch (Exception e) 
		{
			System.out.print("加载Mysql驱动程序时出错!"); 
		    e.printStackTrace(); 
		}
		try 
		{ 
			Connection connect = Connect.getConnection();
			System.out.println("成功连接Mysql服务器!");
			Statement stmt = connect.createStatement(); 
			ResultSet rs = stmt.executeQuery("select * from custom ");  //user 为你表的名称 
		    while (rs.next())
			 { 
				 System.out.println(rs.getString("usr_name"));
		     }
		    connect.close();
         } 
		catch (Exception e) 
		{ 
			System.out.print("获取数据错误!");
			e.printStackTrace(); 
		}
	}
}
