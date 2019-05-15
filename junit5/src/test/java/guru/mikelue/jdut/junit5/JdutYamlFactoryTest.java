package guru.mikelue.jdut.junit5;

import java.sql.SQLException;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.junit5.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.junit5.test.AppContextExtension;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

@JdutResource
public class JdutYamlFactoryTest extends AbstractDataSourceTestBase {
	public JdutYamlFactoryTest() {}

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

@TestInstance(PER_CLASS)
class JdutYamlFactoryByDataSourceTest {
	@RegisterExtension
	static JdutYamlFactory factory = JdutYamlFactory.buildByDataSource(
		JdutYamlFactoryByDataSourceTest::newDataSource
	);

	private static AnnotationConfigApplicationContext ctx;

	@AfterAll
	static void releaseResource()
	{
		ctx.close();
		ctx = null;
	}

	@Test @JdutResource
	void sampleTest() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> newDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM ex_car WHERE car_name LIKE 'KZ%'",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 2)
			).runJdbc()
		).runJdbc();
	}

	private static DataSource newDataSource()
	{
		if (ctx == null) {
			ctx = AppContextExtension.newAppContext();
		}

		return ctx.getBean(DataSource.class);
	}
}

@TestInstance(PER_CLASS)
class JdutYamlFactoryBySupplierTest {
	@RegisterExtension
	static JdutYamlFactory factory = JdutYamlFactory.buildByFactory(
		JdutYamlFactoryBySupplierTest::newConductorFactory
	);

	private static AnnotationConfigApplicationContext ctx;

	@AfterAll
	static void releaseResource()
	{
		ctx.close();
		ctx = null;
	}

	@Test @JdutResource
	void sampleTest() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> ctx.getBean(DataSource.class).getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM ex_car WHERE car_name LIKE 'KZ%'",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 2)
			).runJdbc()
		).runJdbc();
	}

	private static YamlConductorFactory newConductorFactory()
	{
		if (ctx == null) {
			ctx = AppContextExtension.newAppContext();
		}

		return YamlConductorFactory.build(ctx.getBean(DataSource.class));
	}
}
