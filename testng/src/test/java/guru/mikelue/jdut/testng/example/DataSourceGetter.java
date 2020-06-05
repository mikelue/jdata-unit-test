package guru.mikelue.jdut.testng.example;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;

public class DataSourceGetter {
	private final static HikariDataSource dbPool;
	static {
		dbPool = new HikariDataSource();

		dbPool.setDriverClassName("org.h2.Driver");
		dbPool.setJdbcUrl("jdbc:h2:./target/h2/example");
	}

	public static DataSource get()
	{
		return dbPool;
	}
}
