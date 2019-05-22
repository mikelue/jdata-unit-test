package guru.mikelue.jdut.junit4.test;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
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

	public static PropertySourcesPlaceholderConfigurer buildPropertyPlaceholder()
	{
		return new PropertySourcesPlaceholderConfigurer();
	}
}
