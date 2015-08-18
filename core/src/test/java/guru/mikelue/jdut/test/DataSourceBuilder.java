package guru.mikelue.jdut.test;

import java.util.function.Consumer;

import snaq.db.DBPoolDataSource;

public final class DataSourceBuilder {
	private DataSourceBuilder() {}

	public static DBPoolDataSource buildDefaultHsqlDb(String dbName)
	{
		return buildDefaultConfigOfDataSource(
			dbPool -> {
				dbPool.setDriverClassName("org.hsqldb.jdbcDriver");
				dbPool.setUrl(String.format("jdbc:hsqldb:mem:%s;hsqldb.sqllog=2", dbName));
				dbPool.setUser("sa");
			}
		);
	}

	private static DBPoolDataSource buildDefaultConfigOfDataSource(
		Consumer<DBPoolDataSource> configConsumer
	) {
		DBPoolDataSource ds = new DBPoolDataSource();

		ds.setMaxPool(1);
		ds.setMaxSize(8);

		configConsumer.accept(ds);

		return ds;
	}
}
