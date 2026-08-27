package sqlConnect;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;


import Item.*;
import Util.MyUtil;

public class FrontEndSQL {

	public static class ProducerBinding {
		private final long producerId;
		private final String producerName;

		public ProducerBinding(long producerId, String producerName) {
			this.producerId = producerId;
			this.producerName = producerName;
		}

		public long getProducerId() {
			return producerId;
		}

		public String getProducerName() {
			return producerName;
		}
	}

	/*拥有者登录*/
	public boolean Owner_Login(String number, String password) {
		Connection conn=null;
		PreparedStatement ps=null;
		ResultSet rs=null;

        try {
        	conn = Connect.getConnection();
            String sql = "SELECT number, password FROM custom WHERE number=? AND password=? AND identity='拥有者'";
            ps = conn.prepareStatement(sql);
            ps.setString(1, number);
            ps.setString(2, password);
            rs = ps.executeQuery();
            boolean matchFound = rs.next();
            return matchFound;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            Connect.dispose();
        }
    }

	/*消费者登录*/
	public boolean Consumer_Login(String number, String password) {
		Connection conn=null;
		PreparedStatement ps=null;
		ResultSet rs=null;

        try {
        	conn = Connect.getConnection();
            String sql = "SELECT number, password FROM custom WHERE number=? AND password=? AND identity='消费者'";
            ps = conn.prepareStatement(sql);
            ps.setString(1, number);
            ps.setString(2, password);
            rs = ps.executeQuery();
            boolean matchFound = rs.next();
            return matchFound;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            Connect.dispose();
        }
    }

	/*eqpanel-查询设备所有信息*/
	public List<Equipment> getEqResults(String owner_id) {
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
        List<Equipment> eqList = new ArrayList<>();

        try {
            conn = Connect.getConnection();
            String sql = "SELECT owner_id, eq_id, name, port, ip FROM eq WHERE owner_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, owner_id);
            if(stmt.execute()) {
            	rs = stmt.executeQuery();
                while (rs.next()) {
                    Equipment eq = new Equipment(rs.getString("owner_id"), rs.getString("eq_id"), rs.getString("name"),
                                    rs.getString("port"), rs.getString("ip"));
                    eqList.add(eq);

                }
            }else {
            	System.out.println("未执行");
            }



        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
            // 可以选择在这里抛出异常，或者返回空列表
            // throw new RuntimeException("Error executing query", e);
        } finally {
            Connect.dispose();
        }

        return eqList;
    }

	/*eqpanel-增加设备*/
	public void insertEqData(String eq_id,String owner_id,String name,String port,String ip) {
		Connection conn=null;
		PreparedStatement ps=null;
		try {
        	conn = Connect.getConnection();
            String sql = "INSERT into eq (eq_id,owner_id,name,port,ip) VALUES (?,?,?,?,?)";
            ps = conn.prepareStatement(sql);
            ps.setString(1, eq_id);
            ps.setString(2, owner_id);
            ps.setString(3, name);
            ps.setString(4, port);
            ps.setString(5, ip);
            int rowsInserted = ps.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new record has been inserted.");
            }

        } catch (SQLException e) {
        	System.err.println("Error inserting data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Connect.dispose();
        }
	}

	/*eapanel-删除设备*/
	public void deleteEqData(JTable table, DefaultTableModel tableModel,String owner_id) {
		int[] selectedRows = table.getSelectedRows();
		Connection conn=null;
		PreparedStatement stmt=null;

		try {
			conn = Connect.getConnection();
			String sql = "DELETE FROM eq WHERE eq_id = ? AND owner_id =?";
			stmt = conn.prepareStatement(sql);

			for (int selectedRow : selectedRows) {
				String idStr = tableModel.getValueAt(selectedRow, 1).toString();
				int id = Integer.parseInt(idStr);
				System.out.println(id);
				stmt.setInt(1, id);
				stmt.setString(2, owner_id);
				stmt.addBatch();
			}
			int[] rowsDeleted = stmt.executeBatch();
			for (int i = 0; i < rowsDeleted.length; i++) {
				if (rowsDeleted[i] > 0) {
					System.out.println("Record with ID " + tableModel.getValueAt(selectedRows[i], 1) + " has been deleted from the database.");
				}
			}

			for (int i = selectedRows.length - 1; i >= 0; i--) {
				tableModel.removeRow(selectedRows[i]);
			}

		} catch (SQLException e) {
			System.err.println("Error inserting data: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
	}


	/*eqpanel-查询选择设备port和ip*/
	public String[] getSelectEq(String eq_id, String owner_id) {
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		String[] strings = new String[3];
		  try {
	            conn = Connect.getConnection();
	            String sql = "SELECT  name,port,ip FROM eq WHERE eq_id = ? AND owner_id = ?";
	            stmt = conn.prepareStatement(sql);
	            stmt.setString(1, eq_id);
				stmt.setString(2, owner_id);
	            rs = stmt.executeQuery();
	                while (rs.next()) {
	                    strings[0]=  rs.getString("name");
	                    strings[1]=  rs.getString("port");
	                    strings[2]=  rs.getString("ip");
	                }
	        } catch (SQLException e) {
	            System.err.println("Error executing query: " + e.getMessage());
	            e.printStackTrace();
	        } finally {
	            Connect.dispose();
	        }
		  return strings;
	}

	/*eqpanel-查询该用户最后一个设备ID*/
	public String serarchLastID(String id) {
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		String eq_id = null;
		try {
			conn = Connect.getConnection();
			String sql = "SELECT  eq_id FROM eq WHERE owner_id = ? ORDER BY eq_id DESC LIMIT 1;";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, id);
			rs = stmt.executeQuery();

			if(rs.next()) {
				eq_id=  rs.getString("eq_id");
			}else {
				eq_id = "0";
			}

		} catch (SQLException e) {
			System.err.println("Error executing query: " + e.getMessage());
			e.printStackTrace();
			// 可以选择在这里抛出异常，或者返回空列表
			// throw new RuntimeException("Error executing query", e);
		} finally {
			Connect.dispose();
		}

		return eq_id;
	}


	/*basicpanel-增加Stream策略*/
	public void insertStream(Stream stream) {
		Connection conn=null;
		PreparedStatement stmt=null;
		try {
        	conn = Connect.getConnection();
            String sql = "INSERT into stream (id,name,description,starttime,endtime,mingranularity,granularity) VALUES (?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, stream.getId());
            stmt.setString(2, stream.getName());
            stmt.setString(3, stream.getDesciption());
            stmt.setLong(4, stream.getStarttime().getTime());
            stmt.setLong(5, stream.getEndtime().getTime());
            stmt.setLong(6, stream.getMingranularity());
            stmt.setLong(7, stream.getGranularity());
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("数据已插入数据库.");
            }

        } catch (SQLException e) {
        	System.err.println("Error inserting data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Connect.dispose();
        }
	}

	/*basicpanel-插入Owner-Stream*/
	public void insertOwner_Stream(String ownerid,Long streamid) {
		Connection conn=null;
		PreparedStatement stmt=null;
		try {
        	conn = Connect.getConnection();
            String sql = "INSERT into owner_stream (owner_id,stream_id) VALUES (?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, ownerid);
            stmt.setLong(2, streamid);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("数据已插入数据库.");
            }

        } catch (SQLException e) {
        	System.err.println("Error inserting data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Connect.dispose();
	        }
	}

	public void upsertStreamProducer(String ownerid, Long streamid, Long producerid, String producername) {
		Connection conn=null;
		PreparedStatement stmt=null;
		try {
        	conn = Connect.getConnection();
			ensureStreamProducerTable(conn);
            String sql = "INSERT INTO stream_producer (stream_id,owner_id,producer_id,producer_name) VALUES (?,?,?,?) " +
					"ON DUPLICATE KEY UPDATE owner_id=VALUES(owner_id), producer_id=VALUES(producer_id), producer_name=VALUES(producer_name)";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, streamid);
            stmt.setString(2, ownerid);
            stmt.setLong(3, producerid);
            stmt.setString(4, producername);
            stmt.executeUpdate();
        } catch (SQLException e) {
        	System.err.println("Error upserting stream producer: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Connect.dispose();
        }
	}

	public ProducerBinding getStreamProducer(String ownerid, Long streamid) {
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		try {
			conn = Connect.getConnection();
			ensureStreamProducerTable(conn);
			String sql = "SELECT producer_id,producer_name FROM stream_producer WHERE owner_id = ? AND stream_id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, ownerid);
			stmt.setLong(2, streamid);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return new ProducerBinding(rs.getLong("producer_id"), rs.getString("producer_name"));
			}
		} catch (SQLException e) {
			System.err.println("Error querying stream producer: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
		return null;
	}

	private void ensureStreamProducerTable(Connection conn) throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS stream_producer (" +
				"stream_id BIGINT NOT NULL," +
				"owner_id VARCHAR(255) NOT NULL," +
				"producer_id BIGINT NOT NULL," +
				"producer_name VARCHAR(255) NOT NULL," +
				"PRIMARY KEY (stream_id)," +
				"INDEX owner_id_idx (owner_id)" +
				") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.executeUpdate();
		}
	}

	/*用户id找名字*/
	public String searchName(String id) {
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		String name = "无此用户" ;
		  try {
	            conn = Connect.getConnection();
	            String sql = "SELECT  usr_name FROM custom WHERE number = ?";
	            stmt = conn.prepareStatement(sql);
	            stmt.setString(1, id);
	            rs = stmt.executeQuery();
	                while (rs.next()) {
	                	name=rs.getString("usr_name");
	                }
	        } catch (SQLException e) {
	            System.err.println("Error executing query: " + e.getMessage());
	            e.printStackTrace();
	        } finally {
	            Connect.dispose();
	        }
		  return name;
	}

	/*Ownermainview-查询Stream策略*/
	public List<Stream> searchStream(String id) {
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		List<Stream> streams = new ArrayList<Stream>();
		try {
            conn = Connect.getConnection();
            String sql = "SELECT  id,name,description,starttime,endtime,mingranularity,granularity FROM owner_stream JOIN stream ON owner_stream.stream_id = stream.id WHERE owner_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, id);
            rs = stmt.executeQuery();
                while (rs.next()) {
                	Stream s = new Stream();
                	s.setId(rs.getLong("id"));
                	s.setName(rs.getString("name"));
                	s.setDesciption(rs.getString("description"));
                	s.setStarttime(new Date(rs.getLong("starttime")));
                	s.setEndtime(new Date(rs.getLong("endtime")));
                	s.setMingranularity(rs.getLong("mingranularity"));
                	s.setGranularity(rs.getLong("granularity"));
                	streams.add(s);
                }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Connect.dispose();
        }
	  return streams;
	}

    //获取指定ID的流
	public streamHandling.Stream getStream(long id) {
		Connection conn;
		PreparedStatement stmt;
		ResultSet rs;
		streamHandling.Stream stream = null;
		try {
			conn = Connect.getConnection();
			String sql = "SELECT  id,name,description,starttime,endtime,mingranularity,granularity FROM stream WHERE id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, id);
			rs = stmt.executeQuery();

			while (rs.next()) {
				Stream s = new Stream();
				long sId = (rs.getLong("id"));
				String sName = (rs.getString("name"));
				String sDesciption = (rs.getString("description"));
				Date startDate = (new Date(rs.getLong("starttime")));
				Date endDate = (new Date(rs.getLong("endtime")));
				long chunkSize = (rs.getLong("mingranularity"));
				long granularity = (rs.getLong("granularity"));
				stream = new streamHandling.Stream(sId, sName, sDesciption, startDate, endDate, chunkSize, granularity);
			}
		} catch (SQLException e) {
			System.err.println("Error executing query: " + e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
			Connect.dispose();
		}
		return stream;
	}

	/*policypanel-获取消费者姓名*/
	public List<String> searchCustom(){
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		List<String> con_anme = new ArrayList<String>();
		try {
            conn = Connect.getConnection();
            String sql = "SELECT  usr_name FROM custom  WHERE identity = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, "消费者");
            rs = stmt.executeQuery();
                while (rs.next()) {
                	con_anme.add(rs.getString("usr_name"));
                }
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Connect.dispose();
        }
		return con_anme;
	}
	/*policypanel插入Policy*/
	public void insertPolicy(PrivacyPolicy policy) {
		Connection conn=null;
		PreparedStatement stmt=null;
		try {
			conn = Connect.getConnection();
			String sql = "INSERT into policy (owner_name,consumer_name,policy_id,stream_id,p_starttime,p_endtime,multiple,p_name) VALUES (?,?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, policy.getUsrName());
			stmt.setString(2, policy.getCustname());
			stmt.setLong(3, policy.getPrivacyPolicyId());
			stmt.setLong(4, policy.getStreamID());
			stmt.setLong(5, policy.getStartTime().getTime());
			stmt.setLong(6, policy.getEndTime().getTime());
			stmt.setLong(7, policy.getMinGranularity());
			stmt.setString(8, policy.getPolicyName());
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0) {
				System.out.println("数据已插入数据库.");
			}

		} catch (SQLException e) {
			System.err.println("Error inserting data: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
	}

	/*信息管理-根据选择策略返回五个信息*/
	/*消费者查询-根据策略id找信息*/
	public String[] getSelected_p(Long streamid) {
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		String[] strings = new String[5];
		try {
			conn = Connect.getConnection();
			String sql = "SELECT  consumer_name,p_starttime,p_endtime,multiple,p_name FROM policy WHERE policy_id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, streamid);
			rs = stmt.executeQuery();
			while (rs.next()) {
				strings[0]=  rs.getString("consumer_name");
				strings[1]=  MyUtil.date2Str(new Date(rs.getLong("p_starttime")));
				strings[2]=  MyUtil.date2Str(new Date(rs.getLong("p_endtime")));
				strings[3]=  String.valueOf(rs.getLong("multiple")) ;
				strings[4]=  String.valueOf(rs.getString("p_name")) ;
			}
		} catch (SQLException e) {
			System.err.println("Error executing query: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
		return strings;
	}

	/*消费者查询-根据mpc策略id找4个信息*/
	public String[] getSelected_mpc(Long streamid) {
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		String[] strings = new String[4];
		try {
			conn = Connect.getConnection();
			String sql = "SELECT  consumer_name,p_starttime,p_endtime,p_name FROM policy_mpc WHERE policy_id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, streamid);
			rs = stmt.executeQuery();
			while (rs.next()) {
				strings[0]=  rs.getString("consumer_name");
				strings[1]=  MyUtil.date2Str(new Date(rs.getLong("p_starttime")));
				strings[2]=  MyUtil.date2Str(new Date(rs.getLong("p_endtime")));
				strings[3]= rs.getString("p_name");
			}
		} catch (SQLException e) {
			System.err.println("Error executing query: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
		return strings;
	}

	/*信息管理-根据流返回策略name+id*/
	public List<String> getSelect_plist(Long streamid){
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		List<String> policyidList = new ArrayList<String>();
		try {
			conn = Connect.getConnection();
			String sql = "SELECT  policy_id,p_name FROM policy WHERE stream_id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, streamid);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString("p_name");
				String id = Long.toString(rs.getLong("policy_id"));
				policyidList.add(name+"+"+id);

			}
		} catch (SQLException e) {
			System.err.println("Error executing query: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
		return policyidList;
	}

	/*信息管理-根据流返回策略name+id*/
	public List<String> getSelect_mpclist(Long streamid){
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		List<String> policyidList = new ArrayList<String>();
		try {
			conn = Connect.getConnection();
			String sql = "SELECT  policy_id,p_name FROM policy_mpc WHERE stream_id = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, streamid);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String name = rs.getString("p_name");
				String idString = Long.toString(rs.getLong("policy_id"));
				policyidList.add(name+"+"+idString);
			}
		} catch (SQLException e) {
			System.err.println("Error executing query: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
		return policyidList;
	}

	/*信息管理-删除选中流策略*/
	public void deletePolicy(Long policyid) {
		Connection conn=null;
		PreparedStatement stmt=null;
		try {
        	conn = Connect.getConnection();
            String sql = "DELETE FROM policy WHERE policy_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, policyid);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("成功删除数据.");
            }else {
            	System.out.println("未找到需要删除的数据.");
            }

        } catch (SQLException e) {
        	System.err.println("Error inserting data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Connect.dispose();
        }
	}

	/*信息管理-删除选中流策略mpc*/
	public void deleteMpcPolicy(Long policyid) {
		Connection conn=null;
		PreparedStatement stmt=null;
		try {
        	conn = Connect.getConnection();
            String sql = "DELETE FROM policy_mpc WHERE policy_id = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setLong(1, policyid);
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("成功删除数据.");
            }else {
            	System.out.println("未找到需要删除的数据.");
            }

        } catch (SQLException e) {
        	System.err.println("Error inserting data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Connect.dispose();
        }
	}

	/*信息管理-删除选中流（包括该流的所有策略）*/
	public void deleteStream(Long streamid) throws SQLException {
//		Connect connect = new Connect();

		Connection conn = Connect.getConnection();
		conn.setAutoCommit(false); // 手动管理事务
		try {
            String deleteQuery = "DELETE FROM policy WHERE stream_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            	stmt.setLong(1, streamid);
            	stmt.executeUpdate();
            }

            deleteQuery = "DELETE FROM stream WHERE id = ?";
            try (PreparedStatement stmt2 = conn.prepareStatement(deleteQuery)) {
            	stmt2.setLong(1, streamid);
            	stmt2.executeUpdate();
            }

            deleteQuery = "DELETE FROM owner_stream WHERE stream_id = ?";
            try (PreparedStatement stmt3 = conn.prepareStatement(deleteQuery)) {
            	stmt3.setLong(1, streamid);
            	stmt3.executeUpdate();
            }

            deleteQuery = "DELETE FROM policy_mpc WHERE stream_id = ?";
            try (PreparedStatement stmt4 = conn.prepareStatement(deleteQuery)) {
            	stmt4.setLong(1, streamid);
            	stmt4.executeUpdate();
            }

            conn.commit(); // 提交事务
            System.out.println("成功删除了所有表中该流信息的数据");

        } catch (SQLException e) {
            conn.rollback(); // 回滚事务
            System.out.println("删除失败，已成功回滚");
            e.printStackTrace();
        }
		finally {
            Connect.dispose();
        }
	}

	/*消费者查询-查询允许该消费者的策略*/
	public List<PrivacyPolicy> searchPolicy(String name){
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		List<PrivacyPolicy> policyList = new ArrayList<PrivacyPolicy>();
		  try {
	            conn = Connect.getConnection();
	            String sql = "SELECT  * FROM policy WHERE consumer_name = ?";
	            stmt = conn.prepareStatement(sql);
	            stmt.setString(1, name);
	            rs = stmt.executeQuery();
	                while (rs.next()) {
	                	PrivacyPolicy policy  = new PrivacyPolicy();
	                	policy.setUsrName(rs.getString("owner_name"));
	                	policy.setCustname(rs.getString("consumer_name"));
	                	policy.setPrivacyPolicyId(rs.getLong("policy_id"));
	                	policy.setStreamID(rs.getLong("stream_id"));
	                	policy.setStartTime(new Date(rs.getLong("p_starttime")));
	                	policy.setEndTime(new Date(rs.getLong("p_endtime")));
	                	policy.setMinGranularity(rs.getLong("multiple"));
	                	policyList.add(policy);
	                }
	        } catch (SQLException e) {
	            System.err.println("Error executing query: " + e.getMessage());
	            e.printStackTrace();
	        } finally {
	            Connect.dispose();
	        }
		  return policyList;
	}

	/*拥有者查询-查询该拥有者创建的策略*/
	public List<PrivacyPolicy> searchPolicyByOwner(String ownerName){
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		List<PrivacyPolicy> policyList = new ArrayList<PrivacyPolicy>();
		  try {
	            conn = Connect.getConnection();
	            String sql = "SELECT * FROM policy WHERE owner_name = ? ORDER BY policy_id DESC";
	            stmt = conn.prepareStatement(sql);
	            stmt.setString(1, ownerName);
	            rs = stmt.executeQuery();
	                while (rs.next()) {
	                	PrivacyPolicy policy  = new PrivacyPolicy();
	                	policy.setUsrName(rs.getString("owner_name"));
	                	policy.setCustname(rs.getString("consumer_name"));
	                	policy.setPrivacyPolicyId(rs.getLong("policy_id"));
	                	policy.setStreamID(rs.getLong("stream_id"));
	                	policy.setStartTime(new Date(rs.getLong("p_starttime")));
	                	policy.setEndTime(new Date(rs.getLong("p_endtime")));
	                	policy.setMinGranularity(rs.getLong("multiple"));
	                	try {
	                		policy.setPolicyName(rs.getString("p_name"));
	                	} catch (SQLException ignored) {
	                	}
	                	policyList.add(policy);
	                }
	        } catch (SQLException e) {
	            System.err.println("Error executing query: " + e.getMessage());
	            e.printStackTrace();
	        } finally {
	            Connect.dispose();
	        }
		  return policyList;
	}

	/*消费者查询-根据流id返回String 流名称+id*/
	public String searchStream_Name_ID(Long id){
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		String s = null;
		  try {
	            conn = Connect.getConnection();
	            String sql = "SELECT  name,id FROM stream WHERE id = ?";
	            stmt = conn.prepareStatement(sql);
	            stmt.setLong(1, id);
	            rs = stmt.executeQuery();
	                while (rs.next()) {
	                	String name = rs.getString("name");
	                	String s_id =Long.toString(rs.getLong("id")) ;
	                	s = name+"*"+s_id;
	                }
	        } catch (SQLException e) {
	            System.err.println("Error executing query: " + e.getMessage());
	            e.printStackTrace();
	        } finally {
	            Connect.dispose();
	        }

		return s;
	}

	/*消费者查询-根据流id,消费者名称找策略name+id*/
	public List<String> getPolicyidlist(Long id,String name){
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		List<String> idList = new ArrayList<String>();
		try {
			conn = Connect.getConnection();
			String sql = "SELECT  policy_id,p_name FROM policy WHERE stream_id = ? and consumer_name=?";
			stmt = conn.prepareStatement(sql);
			stmt.setLong(1, id);
			stmt.setString(2, name);
			rs = stmt.executeQuery();
			while (rs.next()) {
				String pname = rs.getString("p_name");
				String pid = Long.toString(rs.getLong("policy_id"));
				idList.add(pname+"*"+pid);
			}
		} catch (SQLException e) {
			System.err.println("Error executing query: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}

		return idList;
	}

	/*消费者查询-根据流id找最小粒度*/
	public Long getMingranularity(Long id) {
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		Long min_id = null;
		  try {
	            conn = Connect.getConnection();
	            String sql = "SELECT  mingranularity FROM stream WHERE id = ?";
	            stmt = conn.prepareStatement(sql);
	            stmt.setLong(1, id);
	            rs = stmt.executeQuery();
	                while (rs.next()) {
	                	 min_id = rs.getLong("mingranularity");
	                }
	        } catch (SQLException e) {
	            System.err.println("Error executing query: " + e.getMessage());
	            e.printStackTrace();
	        } finally {
	            Connect.dispose();
	        }
		return min_id;
	}

	/*注册-名称是否重复*/
	public boolean NameIsExisted(String name,String identity) {
		Connection conn=null;
		PreparedStatement ps=null;
		ResultSet rs=null;

        try {
        	conn = Connect.getConnection();
            String sql = "SELECT usr_name  FROM custom WHERE usr_name=? and identity=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, name);
            ps.setString(2, identity);
            rs = ps.executeQuery();
            boolean matchFound = rs.next();
            return matchFound;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            Connect.dispose();
        }
	}

	/*注册-账号是否重复*/
	public boolean NumberIsExisted(String number,String identity) {
		Connection conn=null;
		PreparedStatement ps=null;
		ResultSet rs=null;

        try {
        	conn = Connect.getConnection();
            String sql = "SELECT number  FROM custom WHERE number=? and identity=?";
            ps = conn.prepareStatement(sql);
            ps.setString(1, number);
            ps.setString(2, identity);
            rs = ps.executeQuery();
            boolean matchFound = rs.next();
            return matchFound;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            Connect.dispose();
        }
	}

	/*注册插入数据库*/
	public void Owner_Regist(Custom c) {
		Connection conn=null;
		PreparedStatement stmt=null;
		try {
        	conn = Connect.getConnection();
            String sql = "INSERT into custom (number,usr_name,password,identity) VALUES (?,?,?,?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, c.getIdnum());
            stmt.setString(2, c.getName());
            stmt.setString(3, c.getPassword());
            stmt.setString(4, c.getidentity());
            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("数据已插入数据库.");
            }

        } catch (SQLException e) {
        	System.err.println("Error inserting data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Connect.dispose();
        }
	}

	/*policyPanel-MPC策略插入数据库*/
	public void insertMpcPolicy(MPCPolicy policy) {
		Connection conn=null;
		PreparedStatement stmt=null;
		try {
			conn = Connect.getConnection();
			String sql = "INSERT into policy_mpc (owner_name,consumer_name,policy_id,stream_id,p_starttime,p_endtime,mingranularity,p_name) VALUES (?,?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, policy.getOwnerName());
			stmt.setString(2, policy.getConsumerName());
			stmt.setLong(3, policy.getPolicyId());
			stmt.setLong(4, policy.getStreamID());
			stmt.setLong(5, policy.getStartTime().getTime());
			stmt.setLong(6, policy.getEndTime().getTime());
			stmt.setLong(7, policy.getMinGranularity());
			stmt.setString(8, policy.getPolicyName());
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0) {
				System.out.println("数据已插入数据库.");
			}

		} catch (SQLException e) {
			System.err.println("Error inserting data: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
	}
	/*policyPanel-MPC策略插入数据库*/
//	public void insertMpcPolicy(MPCPolicy policy) {
//		Connection conn=null;
//		PreparedStatement stmt=null;
//		try {
//        	conn = Connect.getConnection();
//            String sql = "INSERT into policy_mpc (owner_name,consumer_name,policy_id,stream_id,p_starttime,p_endtime,mingranularity) VALUES (?,?,?,?,?,?,?)";
//            stmt = conn.prepareStatement(sql);
//            stmt.setString(1, policy.getOwnerName());
//            stmt.setString(2, policy.getConsumerName());
//            stmt.setLong(3, policy.getPolicyId());
//            stmt.setLong(4, policy.getStreamID());
//            stmt.setLong(5, policy.getStartTime().getTime());
//            stmt.setLong(6, policy.getEndTime().getTime());
//            stmt.setLong(7, policy.getMinGranularity());
//            int rowsInserted = stmt.executeUpdate();
//            if (rowsInserted > 0) {
//                System.out.println("数据已插入数据库.");
//            }
//
//        } catch (SQLException e) {
//        	System.err.println("Error inserting data: " + e.getMessage());
//            e.printStackTrace();
//        } finally {
//            Connect.dispose();
//        }
//	}

	/*MPCPanel-根据消费者和类型查找MPC策略*/
	public List<MPCPolicy> getMpcPolicies (String name,String type){
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		List<MPCPolicy> policies = new ArrayList<MPCPolicy>();
		try {
			conn = Connect.getConnection();
			String sql = "SELECT *FROM policy_mpc WHERE consumer_name = ? AND stream_id IN (SELECT id FROM stream WHERE description = ?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, name);
			stmt.setString(2, type);
			rs = stmt.executeQuery();
			while (rs.next()) {
				MPCPolicy policy = new MPCPolicy();
				policy.setOwnerName(rs.getString("owner_name"));
				policy.setConsumerName(rs.getString("consumer_name"));
				policy.setPolicyId(rs.getLong("policy_id"));
				policy.setStreamID(rs.getLong("stream_id"));
				policy.setStartTime(new Date(rs.getLong("p_starttime")));
				policy.setEndTime(new Date(rs.getLong("p_endtime")));
				policy.setMinGranularity(rs.getLong("mingranularity"));
				policy.setPolicyName(rs.getString("p_name"));
				policies.add(policy);
			}
		} catch (SQLException e) {
			System.err.println("Error executing query: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
		return policies;
	}

	/*HistoryPanel-单流查询历史记录*/
	public List<History> getHistory (String name,String type){
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		List<History> histories = new ArrayList<History>();
		try {
			conn = Connect.getConnection();
			String sql = "SELECT *FROM history WHERE usrname = ? AND type =? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, name);
			stmt.setString(2, type);
			rs = stmt.executeQuery();
			while (rs.next()) {
				History history = new History();
				history.setUsrName(rs.getString("usrname"));
				history.setType(rs.getString("type"));
				history.setTime(new Date(rs.getLong("time")));
				history.setStreamID(rs.getLong("streamid"));
				history.setStartTime(new Date(rs.getLong("starttime")));
				history.setEndTime(new Date(rs.getLong("endtime")));
				history.setUrl(rs.getString("url"));
				histories.add(history);
			}
		} catch (SQLException e) {
			System.err.println("Error executing query: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
		return histories;



	}

	/*HistoryPanel-联邦查询历史记录*/
	public List<History> getHistory_mpc (String name,String type){
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		List<History> histories = new ArrayList<History>();
		try {
			conn = Connect.getConnection();
			String sql = "SELECT *FROM history WHERE usrname = ? AND type =? ";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, name);
			stmt.setString(2, type);
			rs = stmt.executeQuery();
			while (rs.next()) {
				History history = new History();
				history.setUsrName(rs.getString("usrname"));
				history.setType(rs.getString("type"));
				history.setTime(new Date(rs.getLong("time")));
				history.setStreamID_MPC(rs.getString("streamid_mpc"));
				history.setStartTime(new Date(rs.getLong("starttime")));
				history.setEndTime(new Date(rs.getLong("endtime")));
				history.setUrl(rs.getString("url"));
				histories.add(history);
			}
		} catch (SQLException e) {
			System.err.println("Error executing query: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
		return histories;



	}

	/*HistoryPanel-历史记录插入数据库*/
	public void InsertHistory(History h) {
		Connection conn=null;
		PreparedStatement stmt=null;
		try {
			conn = Connect.getConnection();
			String sql = "INSERT into history (usrname,type,time,streamid,starttime,endtime,url) VALUES (?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, h.getUsrName());
			stmt.setString(2, h.getType());
			stmt.setLong(3, h.getTime().getTime());
			stmt.setLong(4, h.getStreamID());
			stmt.setLong(5, h.getStartTime().getTime());
			stmt.setLong(6, h.getEndTime().getTime());
			stmt.setString(7, h.getUrl());
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0) {
				System.out.println("数据已插入数据库.");
			}

		} catch (SQLException e) {
			System.err.println("Error inserting data: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
	}

	/*HistoryPanel-历史记录插入数据库*/
	public void InsertHistory_MPC(History h) {
		Connection conn=null;
		PreparedStatement stmt=null;
		try {
			conn = Connect.getConnection();
			String sql = "INSERT into history (usrname,type,time,streamid_mpc,starttime,endtime,url) VALUES (?,?,?,?,?,?,?)";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, h.getUsrName());
			stmt.setString(2, h.getType());
			stmt.setLong(3, h.getTime().getTime());
			stmt.setString(4, h.getStreamID_MPC());
			stmt.setLong(5, h.getStartTime().getTime());
			stmt.setLong(6, h.getEndTime().getTime());
			stmt.setString(7, h.getUrl());
			int rowsInserted = stmt.executeUpdate();
			if (rowsInserted > 0) {
				System.out.println("数据已插入数据库.");
			}

		} catch (SQLException e) {
			System.err.println("Error inserting data: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
	}

	/*historypanel-删除历史记录*/
	public void deleteHistory(JTable table, DefaultTableModel tableModel,String name,String type) {
		int[] selectedRows = table.getSelectedRows();
		Connection conn=null;
		PreparedStatement stmt=null;

		try {
			conn = Connect.getConnection();
			String sql = "DELETE FROM history WHERE type = ? AND usrname =? AND time =?";
			stmt = conn.prepareStatement(sql);

			for (int selectedRow : selectedRows) {
				String timeStr = tableModel.getValueAt(selectedRow, 0).toString();


				stmt.setString(1, type);
				stmt.setString(2, name);
				stmt.setLong(3, MyUtil.str2Date(timeStr).getTime());
				stmt.addBatch();

//                System.out.println(type+"\n"+name+"\n"+MyUtil.str2Date(timeStr).getTime());
			}
			int[] rowsDeleted = stmt.executeBatch();
			for (int i = 0; i < rowsDeleted.length; i++) {
				if (rowsDeleted[i] > 0) {
					System.out.println("Record with Stream " + tableModel.getValueAt(selectedRows[i], 1) + " has been deleted from the database.");
				}
			}

			for (int i = selectedRows.length - 1; i >= 0; i--) {
				tableModel.removeRow(selectedRows[i]);
			}





		} catch (SQLException e) {
			System.err.println("Error inserting data: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
	}


	/*historypanel-删除原文件*/
	public void deleteHistoryFile(JTable table, DefaultTableModel tableModel,String name,String type) {
		int[] selectedRows = table.getSelectedRows();
		Connection conn=null;
		PreparedStatement stmt=null;
		ResultSet rs=null;
		List<String> urls = new ArrayList<String>();

		try {
			conn = Connect.getConnection();
			String sql = "SELECT url FROM history WHERE type = ? AND usrname =? AND time =?";
			stmt = conn.prepareStatement(sql);

			for (int selectedRow : selectedRows) {
				String timeStr = tableModel.getValueAt(selectedRow, 0).toString();


				stmt.setString(1, type);
				stmt.setString(2, name);
				stmt.setLong(3, MyUtil.str2Date(timeStr).getTime());
				stmt.addBatch();
				rs = stmt.executeQuery();
				while (rs.next()) {
					urls.add(rs.getString("url"));
				}
			}

			MyUtil.deleteHistoryFile(urls);

		} catch (SQLException e) {
			System.err.println("Error inserting data: " + e.getMessage());
			e.printStackTrace();
		} finally {
			Connect.dispose();
		}
	}


}
