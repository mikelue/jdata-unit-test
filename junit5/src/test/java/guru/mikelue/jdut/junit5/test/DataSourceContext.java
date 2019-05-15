package guru.mikelue.jdut.junit5.test;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbStatement;
import guru.mikelue.jdut.vendor.DatabaseVendor;
import snaq.db.DBPoolDataSource;

@Configuration
@PropertySource("classpath:/test-context.properties")
public class DataSourceContext {
	private Logger logger = LoggerFactory.getLogger(DataSourceContext.class);

	public DataSourceContext() {}

	@Autowired
	private Environment env;

	@Bean(destroyMethod="release")
	DataSource buildDataSource()
	{
		DBPoolDataSource dbPool = new DBPoolDataSource();

		dbPool.setMaxPool(1);
		dbPool.setMaxSize(8);
		dbPool.setDriverClassName(env.getProperty("db.driverClassName"));
		dbPool.setUrl(env.getProperty("db.url"));
		dbPool.setUser(env.getProperty("db.username"));
		dbPool.setPassword(env.getProperty("db.password"));

		logger.info(
			"Db connection: \"{}\". Driver: \"{}\"",
			env.getProperty("db.url"),
			env.getProperty("db.driverClassName")
		);

		return dbPool;
	}

	@Bean
	DbSchemaSetup buildDbSchemaSetup()
	{
		logger.warn("Db Schema Setup building ...");
		return new DbSchemaSetup();
	}

	@Bean
	DatabaseVendor buildVender(
		DataSource dataSource
	) {
		return DatabaseVendor.getVendor(dataSource);
	}

	public static PropertySourcesPlaceholderConfigurer buildPropertyPlaceholder()
	{
		return new PropertySourcesPlaceholderConfigurer();
	}
}

class DbSchemaSetup {
	@Autowired
	private DataSource dataSource;

	private Logger logger = LoggerFactory.getLogger(DbSchemaSetup.class);

	private final static String TABLE_ARTIST =
		"CREATE TABLE IF NOT EXISTS ex_artist(" +
		"	at_id IDENTITY PRIMARY KEY," +
		"	at_name VARCHAR(512) NOT NULL," +
		"	at_gender TINYINT NOT NULL DEFAULT 3," +
		"	at_birthday DATE" +
		")";
	private final static String TABLE_ALBUM =
		"CREATE TABLE IF NOT EXISTS ex_album(" +
		"	ab_id IDENTITY PRIMARY KEY," +
		"	ab_name VARCHAR(512) NOT NULL," +
		"	ab_release_date DATE NOT NULL," +
		"	ab_duration_seconds SMALLINT NOT NULL," +
		"	ab_type TINYINT NOT NULL DEFAULT 1," +
		"	ab_at_id INTEGER NOT NULL" +
		")";

	@EventListener
	public void setupSchema(ContextRefreshedEvent e)
	{
		logger.warn("[Schema] setup");

		JdbcTemplateFactory.buildRunnable(
			() -> dataSource.getConnection(),
			conn -> DbStatement.buildRunnableForStatement(
				conn,
				stat -> {
					stat.executeUpdate(TABLE_ALBUM);
					stat.executeUpdate(TABLE_ARTIST);
				}
			).runJdbc()
		).asRunnable().run();
	}
}
