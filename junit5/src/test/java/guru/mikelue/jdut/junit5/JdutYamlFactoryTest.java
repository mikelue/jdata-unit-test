package guru.mikelue.jdut.junit5;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.junit5.test.AbstractDataSourceTestBase;

// file: classpath:guru/mikelue/jdut/junit4
// 	-> JdutYamlFactoryForClassRuleTest.yaml
@JdutResource
public class JdutYamlFactoryTest extends AbstractDataSourceTestBase {
	@SuppressWarnings("unused")
	private Logger logger = LoggerFactory.getLogger(JdutYamlFactoryTest.class);

	public JdutYamlFactoryTest() {}

	// file: classpath:guru/mikelue/jdut/junit4
	// 	-> JdutYamlFactoryForClassRuleTest-sampleTestByMethod.yaml
	@Test @JdutResource
	void sampleTestByMethod() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM ex_album WHERE ab_type = 1",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 2)
			).runJdbc()
		).runJdbc();
	}

	@Test
	void sampleTestByClass() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM ex_album WHERE ab_type = 11",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 2)
			).runJdbc()
		).runJdbc();
	}
}
