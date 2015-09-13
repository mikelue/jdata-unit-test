package guru.mikelue.jdut.testng;

import java.sql.SQLException;
import javax.sql.DataSource;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.testng.test.DataSourceContext;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.testng.test.AbstractDataSourceTestBase;

@Test(testName="ITestContextYamlFactoryListenerTest")
@Listeners(ITestContextYamlFactoryListenerTest.SampleItestContextListener.class)
public class ITestContextYamlFactoryListenerTest extends AbstractDataSourceTestBase {
	public static class SampleItestContextListener extends ITestContextYamlFactoryListener {
		private AnnotationConfigApplicationContext ctx;

		@Override
		public void onStart(ITestContext context)
		{
			if (ctx != null) {
				return;
			}

			ctx = new AnnotationConfigApplicationContext();
			ctx.register(DataSourceContext.class);
			ctx.refresh();

			YamlFactoryListenerBase.setDataSource(context, ctx.getBean(DataSource.class));

			super.onStart(context);
		}
		@Override
		public void onFinish(ITestContext context)
		{
			super.onFinish(context);

			YamlFactoryListenerBase.removeDataSource(context);

			ctx.close();
			ctx = null;
		}

		@Override
		protected boolean needConductData(ITestContext context)
		{
			return "ITestContextYamlFactoryListenerTest".equals(context.getName());
		}
	}

	public ITestContextYamlFactoryListenerTest() {}

	/**
	 * Tests the listener for {@link ITestContext} of building/cleaning data.
	 */
	@Test
	public void buildAndClean() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM tcontext_t1",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
	}
}
