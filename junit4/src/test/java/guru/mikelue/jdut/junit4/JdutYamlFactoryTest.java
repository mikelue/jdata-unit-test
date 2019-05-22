package guru.mikelue.jdut.junit4;

import java.sql.SQLException;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.jdbc.function.DbStatement;
import guru.mikelue.jdut.junit4.test.AbstractDataSourceTestBase;

public class JdutYamlFactoryTest extends AbstractDataSourceTestBase {
	public JdutYamlFactoryTest() {}

	@Before
	public void setupTable() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbStatement.buildRunnableForStatement(
				conn, stmt -> {
					stmt.execute("DROP TABLE IF EXISTS method_t1");
					stmt.execute("CREATE TABLE method_t1(t1_id INTEGER)");
				}
			).runJdbc()
		).runJdbc();
	}

	/**
	 * Tests build/clean data on method level.
	 */
	// file: classpath:guru/mikelue/jdut/junit4
	// 	-> JdutYamlFactoryTest-sampleTest.yaml
	@Test @JdutResource
	public void sampleTest() throws Throwable, SQLException
	{
		final TestRule testedRule = JdutYamlFactory.buildByDataSource(AbstractDataSourceTestBase::getDataSource);

		final MutableBoolean isAssertAfterBuild = new MutableBoolean(false);
		testedRule.apply(
			new Statement() {
				@Override
				public void evaluate() throws Throwable
				{
					isAssertAfterBuild.setTrue();
					assertData(2);
				}
			},
			Description.createTestDescription(getClass(), "sampleTest", getClass().getMethod("sampleTest").getAnnotation(JdutResource.class))
		).evaluate();

		Assert.assertTrue(isAssertAfterBuild.booleanValue());
		assertData(0);
	}

	/**
	 * Tests build/clean data without @JdutResource(nothing happened).
	 */
	@Test
	public void sampleTestWithoutAnnotation() throws Throwable, SQLException
	{
		final TestRule testedRule = JdutYamlFactory.buildByDataSource(AbstractDataSourceTestBase::getDataSource);

		final MutableBoolean isAssertAfterBuild = new MutableBoolean(false);
		testedRule.apply(
			new Statement() {
				@Override
				public void evaluate() throws Throwable
				{
					isAssertAfterBuild.setTrue();
					assertData(0);
				}
			},
			Description.createTestDescription(getClass(), "sampleTestWithoutAnnotation")
		).evaluate();

		Assert.assertTrue(isAssertAfterBuild.booleanValue());
		assertData(0);
	}

	private void assertData(int expectedCount) throws SQLException
	{
		String checkData = "SELECT COUNT(*) FROM method_t1 WHERE t1_id = 33";
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
