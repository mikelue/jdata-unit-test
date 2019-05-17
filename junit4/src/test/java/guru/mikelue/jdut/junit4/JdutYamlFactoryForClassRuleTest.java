package guru.mikelue.jdut.junit4;

import java.sql.SQLException;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.junit4.test.AbstractDataSourceTestBase;

@JdutResource
public class JdutYamlFactoryForClassRuleTest extends AbstractDataSourceTestBase {
	@ClassRule
	public static TestRule rule = new TestRule() {
		@Override
		public Statement apply(Statement base, Description description)
		{
			return new Statement() {
				@Override
				public void evaluate() throws Throwable
				{
					TestRule jdutRule = JdutYamlFactory.buildByDataSource(AbstractDataSourceTestBase::getDataSource);

					jdutRule.apply(base, description)
						.evaluate();
				}
			};
		}
	};

	public JdutYamlFactoryForClassRuleTest() {}

	@Test
	public void sampleTest() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM ex_album WHERE ab_type = 3",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 2)
			).runJdbc()
		).runJdbc();
	}
}
