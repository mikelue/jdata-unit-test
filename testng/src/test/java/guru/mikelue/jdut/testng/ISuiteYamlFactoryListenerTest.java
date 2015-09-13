package guru.mikelue.jdut.testng;

import java.sql.SQLException;
import javax.sql.DataSource;

import mockit.Injectable;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.ISuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.testng.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.testng.test.DataSourceContext;

@Test(suiteName="ISuiteYamlFactoryListenerTestSuite")
@Listeners({ISuiteYamlFactoryListenerTest.DataSourceISuiteListener.class})
public class ISuiteYamlFactoryListenerTest extends AbstractDataSourceTestBase {
	@Injectable
	private ISuite mockSuite;
	@Injectable
	private DuetConductor mockDuetConductor;

	public ISuiteYamlFactoryListenerTest() {}

	public static class DataSourceISuiteListener extends ISuiteYamlFactoryListener {
		private AnnotationConfigApplicationContext ctx;

		@Override
		public void onStart(ISuite suite)
		{
			if (ctx != null) {
				return;
			}

			ctx = new AnnotationConfigApplicationContext();
			ctx.register(DataSourceContext.class);
			ctx.refresh();

			YamlFactoryListenerBase.setDataSource(suite, ctx.getBean(DataSource.class));

			super.onStart(suite);
		}
		@Override
		public void onFinish(ISuite suite)
		{
			super.onFinish(suite);

			YamlFactoryListenerBase.removeDataSource(suite);

			ctx.close();
			ctx = null;
		}

		@Override
		protected boolean needConductData(ISuite suite)
		{
			return "ISuiteYamlFactoryListenerTestSuite".equals(suite.getName());
		}
	}

	/**
	 * Tests the suite processing.
	 */
	@Test
	public void suiteTest() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM suite_t1",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
	}
}
