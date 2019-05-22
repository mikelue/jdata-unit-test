package guru.mikelue.jdut.testng.test;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;

@Test(suiteName="ListenerTestSuite")
public abstract class AbstractDataSourceTestBase {
	private Logger logger = LoggerFactory.getLogger(getClass());

	protected AbstractDataSourceTestBase() {}

	private static AnnotationConfigApplicationContext ctx;

	@BeforeSuite
	public static void setupAppContext()
	{
		ctx = buildApplicationContext();
	}
	@AfterSuite
	public static void releaseAppContext()
	{
		ctx.close();
		ctx = null;
	}

	public static AnnotationConfigApplicationContext buildApplicationContext()
	{
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(DataSourceContext.class);
		ctx.refresh();
		return ctx;
	}

	protected static ApplicationContext getApplicationContext()
	{
		return ctx;
	}

	protected static DataSource getDataSource()
	{
		return ctx.getBean(DataSource.class);
	}

	protected Logger getLogger()
	{
		return logger;
	}
}
