package guru.mikelue.jdut.junit4;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

import org.apache.commons.lang3.RandomUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.junit4.example.SchemaSetup;
import guru.mikelue.jdut.junit4.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.operation.DefaultOperators;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

// file: classpath:guru/mikelue/jdut/junit4
// 	-> JdutYamlFactoryForClassRuleTest.yaml
public class JdutYamlFactoryTest extends AbstractDataSourceTestBase {
	private static YamlConductorFactory conductorFactory;

	public JdutYamlFactoryTest() {}

	@Rule
	public JdutYamlFactory jdutYamlFactoryForMethodLevel = new JdutYamlFactory(conductorFactory);

	// file: classpath:guru/mikelue/jdut/junit4
	// 	-> JdutYamlFactoryForClassRuleTest-sampleTest.yaml
	@Test @JdutResource
	public void sampleTest() throws SQLException
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

	@BeforeClass
	public static void initYamlFactory()
	{
		final Logger logger = LoggerFactory.getLogger(JdutYamlFactoryTest.class);

		conductorFactory = YamlConductorFactory.build(
			getDataSource(),
			config -> config
				.namedSupplier(
					"random_date", JdutYamlFactoryTest::randomDate
				)
				.namedSupplier(
					"random_duration", JdutYamlFactoryTest::randomDuration
				)
				.namedOperator(
					"insert_and_log",
					(connection, dataGrain) -> {
						logger.info("@@@ BEFORE BUILDING DATA @@@");

						DataGrain result = DefaultOperators.insert(connection, dataGrain);

						logger.info("@@@ AFTER BUILDING DATA @@@");

						return result;
					}
				)
				.namedDecorator(
					"decorator_album",
					(dataRowBuilder) -> {
						dataRowBuilder.fieldOfValue(
							"ab_name",
							dataRowBuilder.getData("ab_name").get() + "(BlueNote)"
						);
					}
				)
		);

		SchemaSetup.buildSchema(getDataSource());
	}
	@AfterClass
	public static void releaseYamlFactory()
	{
		conductorFactory = null;
	}

	private static Date randomDate()
	{
		return Date.valueOf(
			LocalDate.of(
				RandomUtils.nextInt(1930, 1956),
				RandomUtils.nextInt(1, 13),
				RandomUtils.nextInt(1, 26)
			)
		);
	}

	private static int randomDuration()
	{
		return RandomUtils.nextInt(1900, 8801);
	}
}
