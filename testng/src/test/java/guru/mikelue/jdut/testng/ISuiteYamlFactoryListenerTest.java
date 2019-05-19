package guru.mikelue.jdut.testng;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.testng.test.SuiteListenerForAppContext;

@Test(suiteName="ISuiteYamlFactoryListenerTestSuite")
@Listeners({ISuiteYamlFactoryListenerTest.TestedListener.class})
public class ISuiteYamlFactoryListenerTest {
	public static class TestedListener extends ISuiteYamlFactoryListener {
		@Override
		public void onStart(ISuite suite)
		{
			ApplicationContext appContext = SuiteListenerForAppContext.buildAppContext();
			setDataSource(suite, appContext.getBean(DataSource.class));
			super.onStart(suite);
		}
		@Override
		public void onFinish(ISuite suite)
		{
			super.onFinish(suite);
			removeDataSource(suite);
		}
	}

	public ISuiteYamlFactoryListenerTest() {}

	/**
	 * Tests the suite processing.
	 */
	@Test
	public void suiteTest(ITestContext testContext) throws SQLException
	{
		final DataSource dataSource = YamlFactoryListenerBase.getDataSource(testContext.getSuite());

		JdbcTemplateFactory.buildRunnable(
			() -> dataSource.getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM suite_t1",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
	}
}
