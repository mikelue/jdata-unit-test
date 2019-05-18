package guru.mikelue.jdut.junit4.example;

import javax.sql.DataSource;

import snaq.db.DBPoolDataSource;

public class DataSourceGetter {
	private final static DBPoolDataSource dbPool;
	static {
		dbPool = new DBPoolDataSource();

		dbPool.setMaxPool(1);
		dbPool.setMaxSize(8);
		dbPool.setDriverClassName("org.h2.Driver");
		dbPool.setUrl("jdbc:h2:./target/h2/example");
	}

	public static DataSource get()
	{
		return dbPool;
	}
}
