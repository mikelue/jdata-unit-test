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

// file: classpath:guru/mikelue/jdut/junit4
// 	-> JdutYamlFactoryForClassRuleTest.yaml
@JdutResource
public class JdutYamlFactoryForClassRuleTest extends AbstractDataSourceTestBase {
	public JdutYamlFactoryForClassRuleTest() {}

	@Before
	public void setupTable() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbStatement.buildRunnableForStatement(
				conn, stmt -> {
					stmt.execute("DROP TABLE IF EXISTS class_t1");
					stmt.execute("CREATE TABLE class_t1(t1_id INTEGER)");
				}
			).runJdbc()
		).runJdbc();
	}

	/**
	 * Tests build/clean data on class level.
	 */
	@Test
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
			Description.createSuiteDescription(getClass())
		).evaluate();

		Assert.assertTrue(isAssertAfterBuild.booleanValue());
		assertData(0);
	}

	/**
	 * Tests build/clean data without @JdutResource on class level(nothing happened).
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
			Description.createSuiteDescription(WithoutAnnotation.class)
		).evaluate();

		Assert.assertTrue(isAssertAfterBuild.booleanValue());
		assertData(0);
	}

	private void assertData(int expectedCount) throws SQLException
	 {
		String checkData = "SELECT COUNT(*) FROM class_t1 WHERE t1_id = 33";
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

class WithoutAnnotation {}
