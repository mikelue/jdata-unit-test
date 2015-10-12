package guru.mikelue.jdut.junit4.test;

import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import guru.mikelue.jdut.decorate.DataGrainDecorator;
import guru.mikelue.jdut.vendor.DatabaseVendor;

public abstract class AbstractDataSourceTestBase {
	private Logger logger = LoggerFactory.getLogger(getClass());

	private static AnnotationConfigApplicationContext ctx;

	private DataGrainDecorator schemaLoading;

	protected AbstractDataSourceTestBase() {}

	@ClassRule
	public static ExternalResource dataSourceResource = new ExternalResource() {
		@Override
		protected void before() throws Throwable
		{
			if (ctx != null) {
				return;
			}

			ctx = new AnnotationConfigApplicationContext();
			ctx.register(DataSourceContext.class);
			ctx.refresh();
		}

		@Override
		protected void after()
		{
			ctx.close();
			ctx = null;
		}
	};

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
