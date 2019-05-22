package guru.mikelue.jdut.junit4.test;

import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class AbstractDataSourceTestBase {
	private Logger logger = LoggerFactory.getLogger(AbstractDataSourceTestBase.class);

	protected AbstractDataSourceTestBase() {}

	private static AnnotationConfigApplicationContext ctx;

	@ClassRule
	public final static ExternalResource dataSourceResource = new ExternalResource() {
		@Override
		protected void before()
		{
			getApplicationContext();
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

	protected static ApplicationContext getApplicationContext()
	{
		if (ctx == null) {
			ctx = new AnnotationConfigApplicationContext();
			ctx.register(DataSourceContext.class);
			ctx.refresh();
		}

		return ctx;
	}

	protected static DataSource getDataSource()
	{
		return getApplicationContext().getBean(DataSource.class);
	}
}
