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

@Test(suiteName="ISuiteYamlFactoryListenerTestSuite")
public class ISuiteYamlFactoryListenerTest extends AbstractDataSourceTestBase {
	private final static ISuiteYamlFactoryListener testedListener = new ISuiteYamlFactoryListener();

	public ISuiteYamlFactoryListenerTest() {}

	@BeforeMethod
	void setupTable() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbStatement.buildRunnableForStatement(
				conn, stmt -> {
					stmt.execute("DROP TABLE IF EXISTS suite_t1");
					stmt.execute("CREATE TABLE suite_t1(t1_id INTEGER)");
				}
			).runJdbc()
		).runJdbc();
	}

	/**
	 * Tests the suite processing.
	 */
	@Test
	public void suiteTest(ITestContext testContext) throws SQLException
	{
		YamlFactoryListenerBase.setDataSource(testContext.getSuite(), getDataSource());

		testedListener.onStart(testContext.getSuite());
		assertData(1);

		testedListener.onFinish(testContext.getSuite());
		assertData(2);

		YamlFactoryListenerBase.removeDataSource(testContext.getSuite());
	}

	private void assertData(int expectedCount) throws SQLException
	{
		String checkData = "SELECT COUNT(*) FROM suite_t1 WHERE t1_id = 27";

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
