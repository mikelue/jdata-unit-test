package guru.mikelue.jdut.testng.test;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import guru.mikelue.jdut.decorate.DataGrainDecorator;
import guru.mikelue.jdut.vendor.DatabaseVendor;

@Test(testName="DataSourceTest", groups="DataSourceGroup")
public abstract class AbstractDataSourceTestBase {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private static AnnotationConfigApplicationContext ctx;

	private DataGrainDecorator schemaLoading;

	protected AbstractDataSourceTestBase() {}

	@BeforeTest
	protected static void initDataSourceTest()
	{
		if (ctx != null) {
			return;
		}

		ctx = new AnnotationConfigApplicationContext();
		ctx.register(DataSourceContext.class);
		ctx.refresh();
	}
	@AfterTest
	protected static void closeDataSourceTest()
	{
		ctx.close();
		ctx = null;
	}

	protected Logger getLogger()
	{
		return logger;
	}

	protected DataGrainDecorator getSchemaLoadingDecorator()
	{
		return schemaLoading;
	}

	protected static DatabaseVendor getCurrentVendor()
	{
		return ctx.getBean(DatabaseVendor.class);
	}

	protected static ApplicationContext getApplicationContext()
	{
		return ctx;
	}

	protected static DataSource getDataSource()
	{
		return ctx.getBean(DataSource.class);
	}
}
