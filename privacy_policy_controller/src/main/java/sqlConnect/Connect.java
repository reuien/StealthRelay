package sqlConnect;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Properties;

public class Connect {
	    private static final Properties DB_PROPERTIES = loadDatabaseProperties();
	    private static String driverName = "com.mysql.cj.jdbc.Driver";
	    private static String url = getConfig("DB_URL", "jdbc:mysql://localhost:3306/attempt?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
	    private static String userName = getConfig("DB_USER", "root");
	    private static String password = getConfig("DB_PASSWORD", "");
	    private static Connection conn;
		private static PreparedStatement stmt;
		private static ResultSet rs;

	private static String getConfig(String key, String defaultValue) {
		String configuredValue = System.getProperty(key);
		if (configuredValue == null || configuredValue.trim().isEmpty()) {
			configuredValue = System.getenv(key);
		}
		if (configuredValue == null || configuredValue.trim().isEmpty()) {
			configuredValue = DB_PROPERTIES.getProperty(key);
		}
		if (configuredValue == null || configuredValue.trim().isEmpty()) {
			return defaultValue;
		}
		return configuredValue.trim();
	}

	private static Properties loadDatabaseProperties() {
		Properties properties = new Properties();
		Path currentDir = Paths.get("").toAbsolutePath();
		while (currentDir != null) {
			Path configPath = findDatabaseConfig(currentDir);
			if (Files.isRegularFile(configPath)) {
				try (InputStream inputStream = Files.newInputStream(configPath)) {
					properties.load(inputStream);
				} catch (IOException e) {
					System.out.println("读取数据库配置失败: " + configPath);
					e.printStackTrace();
				}
				break;
			}
			currentDir = currentDir.getParent();
		}
		return properties;
	}

	private static Path findDatabaseConfig(Path currentDir) {
		Path configPath = currentDir.resolve("config").resolve("database.properties");
		if (Files.isRegularFile(configPath)) {
			return configPath;
		}
		return currentDir.resolve("pcsig-alfred").resolve("config").resolve("database.properties");
	}
 
	public Connect() {
		loadDriver();
	}
 
	public static Connection getConnection() throws SQLException {
		loadDriver();
		return DriverManager.getConnection(url, userName, password);//使用DriverManger获取数据库连接
	}

	private static void loadDriver() {
		try {
			Class.forName(driverName);//加载数据库
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("加载Mysql驱动程序时出错!", e);
		}
	}
	
	
	
	
	
	public static void dispose() {
		try {
			if (conn != null) {
				conn.close();
			}
			if (stmt != null) {
				stmt.close();
			}
			if (rs != null) {
				rs.close();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
