package guru.mikelue.jdut.testng;

import java.sql.SQLException;

import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.testng.test.AbstractDataSourceTestBase;

@Test(suiteName="ContextScopeListenerSuite", testName="ITestContextYamlFactoryListenerTest")
@Listeners({ITestContextYamlFactoryListenerTest.TestedListener.class})
public class ITestContextYamlFactoryListenerTest extends AbstractDataSourceTestBase {
	public static class TestedListener extends ITestContextYamlFactoryListener {
		@Override
		public void onStart(ITestContext testContext)
		{
			setDataSource(testContext, AbstractDataSourceTestBase.getDataSource(testContext));
			super.onStart(testContext);
		}
		@Override
		public void onFinish(ITestContext testContext)
		{
			super.onFinish(testContext);
			removeDataSource(testContext);
		}
	}

	public ITestContextYamlFactoryListenerTest() {}

	/**
	 * Tests the listener for {@link ITestContext} of building/cleaning data.
	 */
	@Test
	public void buildAndClean(ITestContext testContext) throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource(testContext).getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM tcontext_t1",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
	}
}
