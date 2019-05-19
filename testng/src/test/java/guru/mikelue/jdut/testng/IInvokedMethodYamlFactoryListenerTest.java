package guru.mikelue.jdut.testng;

import java.sql.SQLException;

import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.testng.test.AbstractDataSourceTestBase;

@Test(suiteName="MethodScopeListenerSuite")
@Listeners({IInvokedMethodYamlFactoryListener.class})
public class IInvokedMethodYamlFactoryListenerTest extends AbstractDataSourceTestBase {
	@BeforeClass
	static void putDataSourceTest(ITestContext testContext)
	{
		YamlFactoryListenerBase.setDataSource(testContext, getDataSource(testContext));
	}
	@AfterClass
	static void pullDataSourceTest(ITestContext testContext)
	{
		YamlFactoryListenerBase.removeDataSource(testContext);
	}

	public IInvokedMethodYamlFactoryListenerTest() {}

	/**
	 * Tests the listener for method to load YAML.
	 */
	@Test @JdutResource
	public void loadYaml(ITestContext testContext) throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource(testContext).getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM tab_22",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
	}

	/**
	 * Tests the method without @JdutResource.
	 */
	@Test
	public void nothingHappened() {}
}
