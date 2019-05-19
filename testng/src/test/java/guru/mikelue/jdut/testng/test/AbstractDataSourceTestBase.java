package guru.mikelue.jdut.testng.test;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;

@Listeners(SuiteListenerForAppContext.class)
public abstract class AbstractDataSourceTestBase {
	private Logger logger = LoggerFactory.getLogger(getClass());

	protected AbstractDataSourceTestBase() {}

	protected static ApplicationContext getApplicationContext(ITestContext testContext)
	{
		return SuiteListenerForAppContext.getApplicationContext(testContext);
	}

	protected static DataSource getDataSource(ITestContext testContext)
	{
		return SuiteListenerForAppContext.getDataSource(testContext);
	}

	protected Logger getLogger()
	{
		return logger;
	}
}
