package guru.mikelue.jdut.junit4;

import java.sql.SQLException;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.junit4.test.AbstractDataSourceTestBase;

// file: classpath:guru/mikelue/jdut/junit4
// 	-> JdutYamlFactoryForClassRuleTest.yaml
@JdutResource
public class JdutYamlFactoryForClassRuleTest extends AbstractDataSourceTestBase {
	@ClassRule
	public static TestRule prepareData = JdutYamlFactory.buildByDataSource(AbstractDataSourceTestBase::getDataSource);

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
