package guru.mikelue.jdut.testng;

import java.sql.SQLException;

import org.testng.ITestContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.jdbc.function.DbStatement;
import guru.mikelue.jdut.testng.test.AbstractDataSourceTestBase;

@Test(testName="ITestContextYamlFactoryListenerTest")
public class ITestContextYamlFactoryListenerTest extends AbstractDataSourceTestBase {
	private final static ITestContextYamlFactoryListener testedListener = new ITestContextYamlFactoryListener();

	public ITestContextYamlFactoryListenerTest() {}

	@BeforeMethod
	void setupTable() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbStatement.buildRunnableForStatement(
				conn, stmt -> {
					stmt.execute("DROP TABLE IF EXISTS tcontext_t1");
					stmt.execute("CREATE TABLE tcontext_t1(t1_id INTEGER)");
				}
			).runJdbc()
		).runJdbc();
	}

	/**
	 * Tests the listener for {@link ITestContext} of building/cleaning data.
	 */
	@Test
	public void buildAndClean(ITestContext testContext) throws SQLException
	{
		YamlFactoryListenerBase.setDataSource(testContext, getDataSource());

		testedListener.onStart(testContext);
		assertData(1);

		testedListener.onFinish(testContext);
		assertData(2);

		YamlFactoryListenerBase.removeDataSource(testContext);
	}

	private static void assertData(int expectedCount) throws SQLException
	{
		String checkData = "SELECT COUNT(*) FROM tcontext_t1 WHERE t1_id = 20";

		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, checkData,
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, expectedCount)
			).runJdbc()
		).runJdbc();
	}
}
