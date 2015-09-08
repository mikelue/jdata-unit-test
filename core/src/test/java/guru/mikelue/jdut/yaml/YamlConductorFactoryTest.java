package guru.mikelue.jdut.yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.NClob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;
import java.util.stream.Stream;
import javax.xml.bind.DatatypeConverter;

import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.JdbcVoidFunction;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.jdbc.function.DbStatement;
import guru.mikelue.jdut.operation.DefaultOperatorFactory;
import guru.mikelue.jdut.operation.DefaultOperators;
import guru.mikelue.jdut.operation.OperatorFactory;
import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.test.DoLiquibase;
import guru.mikelue.jdut.vendor.DatabaseVendor;

public class YamlConductorFactoryTest extends AbstractDataSourceTestBase {
	public YamlConductorFactoryTest() {}

	/**
	 * Tests the loading of simple data.
	 */
	@Test @DoLiquibase
	public void conductResourceWithSimpleData() throws SQLException
	{
		YamlConductorFactory factory = YamlConductorFactory.build(
			getDataSource(),
			builder -> builder
				.resourceLoader(ReaderFunctions.loadByClass(getClass()))
		);

		DuetConductor conductor = factory.conductResource(
			"YamlConductorFactoryTest-conductResourceWithSimpleData.yaml"
		);

		conductor.build();

		/**
		 * Asserts the building data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM sd_tab_1",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 4)
			).runJdbc()
		).runJdbc();
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM sd_tab_2",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 6)
			).runJdbc()
		).runJdbc();
		// :~)

		conductor.clean();

		/**
		 * Asserts the cleaned data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM sd_tab_1",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM sd_tab_2",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
		// :~)
	}

	/**
	 * Tests the loading of code.
	 */
	@Test @DoLiquibase
	public void conductResourceWithCode() throws SQLException
	{
		YamlConductorFactory factory = YamlConductorFactory.build(
			getDataSource(),
			builder -> builder
				.resourceLoader(ReaderFunctions.loadByClass(getClass()))
				.namedJdbcFunction(
					"test_clean_func",
					conn -> DbStatement.buildSupplierForStatement(
						conn,
						stat -> stat.executeUpdate("DELETE FROM sc_tab_1 WHERE st_id IN (1, 2)")
					).getJdbc()
				)
		);

		DuetConductor conductor = factory.conductResource(
			"YamlConductorFactoryTest-conductResourceWithCode.yaml"
		);

		conductor.build();
		/**
		 * Asserts the building data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM sc_tab_1",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 3)
			).runJdbc()
		).runJdbc();

		conductor.clean();

		/**
		 * Asserts the cleaning data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM sc_tab_1",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
	}

	/**
	 * Tests the transactional operation(by rollback).
	 */
	@Test @DoLiquibase
	public void conductResourceWithTransaction() throws SQLException
	{
		YamlConductorFactory factory = YamlConductorFactory.build(
			getDataSource(),
			builder -> builder
				.resourceLoader(ReaderFunctions.loadByClass(getClass()))
		);

		DuetConductor conductor = factory.conductResource(
			"YamlConductorFactoryTest-conductResourceWithTransaction.yaml"
		);

		try {
			conductor.build();
		} catch (RuntimeException e) {
			Assert.assertTrue(
				SQLException.class.isInstance(e.getCause())
			);

			/**
			 * Asserts that nothing is inserted
			 */
			JdbcTemplateFactory.buildRunnable(
				() -> getDataSource().getConnection(),
				conn -> DbResultSet.buildRunnable(
					conn, "SELECT COUNT(*) FROM tx_tab_1",
					rs -> new ResultSetAssert(rs)
						.assertNextTrue()
						.assertInt(1, 0)
				).runJdbc()
			).runJdbc();
			// :~)
			return;
		}

		Assert.fail("Should be rollback");
	}

	/**
	 * Tests the operator configuration.
	 */
	@Test @DoLiquibase
	public void conductResourceWithOperation() throws SQLException
	{
		OperatorFactory operatorFactory = DefaultOperatorFactory.build(getDataSource(), builder -> {});

		YamlConductorFactory factory = YamlConductorFactory.build(
			getDataSource(),
			/**
			 * Builds named operators
			 */
			builder -> builder
				.resourceLoader(ReaderFunctions.loadByClass(getClass()))
				.namedOperator("INSERT_V1", operatorFactory.get(DefaultOperators.INSERT))
				.namedOperator("DELETE_V1", operatorFactory.get(DefaultOperators.DELETE))
			// :~)
		);

		DuetConductor conductor = factory.conductResource(
			"YamlConductorFactoryTest-conductResourceWithOperation.yaml"
		);

		conductor.build();
		/**
		 * Asserts the building data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM op_tab_1",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 2)
			).runJdbc()
		).runJdbc();
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM op_tab_2",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 2)
			).runJdbc()
		).runJdbc();
		// :~)

		conductor.clean();

		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM op_tab_1",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM op_tab_2",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
	}

	/**
	 * Tests the decoration of data grain
	 */
	@Test @DoLiquibase
	public void conductResourceWithDecorator() throws SQLException
	{
		YamlConductorFactory factory = YamlConductorFactory.build(
			getDataSource(),
			/**
			 * Builds named operators
			 */
			builder -> builder
				.resourceLoader(ReaderFunctions.loadByClass(getClass()))
				.namedDecorator("global_decorator", rowBuilder ->
					rowBuilder.fieldOfValue("st_value", rowBuilder.<String>getData("st_value").get() + "-G")
				)
				.namedDecorator("local_decorator", rowBuilder ->
					rowBuilder.fieldOfValue("st_value", rowBuilder.<String>getData("st_value").get() + "-L")
				)
			// :~)
		);

		DuetConductor conductor = factory.conductResource(
			"YamlConductorFactoryTest-conductResourceWithDecorator.yaml"
		);

		conductor.build();
		/**
		 * Asserts the building data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT * FROM dc_tab_1 ORDER BY st_id ASC",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertString("st_value", "NP-1-G")

					.assertNextTrue()
					.assertString("st_value", "NP-2-G-L")
			).runJdbc()
		).runJdbc();
		// :~)

		conductor.clean();
	}

	/**
	 * Tests the usage of supplier and cached value by {@link DataField}.
	 */
	@Test @DoLiquibase
	public void conductResourceWithSupplier() throws SQLException
	{
		Supplier<Integer> idGenerator = new Supplier<Integer>() {
			int id = 1;

			@Override
			public Integer get()
			{
				getLogger().debug("Supplier is called: [{}]", id);
				return id++;
			}
		};

		YamlConductorFactory factory = YamlConductorFactory.build(
			getDataSource(),
			/**
			 * Builds named operators
			 */
			builder -> builder
				.resourceLoader(ReaderFunctions.loadByClass(getClass()))
				.namedSupplier("id_generator", idGenerator)
			// :~)
		);

		DuetConductor conductor = factory.conductResource(
			"YamlConductorFactoryTest-conductResourceWithSupplier.yaml"
		);

		conductor.build();
		/**
		 * Asserts the building data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT * FROM sp_tab_1 ORDER BY st_id ASC",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt("st_id", 1)

					.assertNextTrue()
					.assertInt("st_id", 2)
			).runJdbc()
		).runJdbc();
		// :~)

		conductor.clean();
	}

	/**
	 * Tests the construction for direct type of database in value of field.
	 */
	@Test @DoLiquibase
	public void conductResourceWithDbType() throws SQLException
	{
		YamlConductorFactory factory = YamlConductorFactory.build(
			getDataSource(), builder -> builder
				.resourceLoader(ReaderFunctions.loadByClass(getClass()))
		);

		DuetConductor conductor = factory.conductResource(
			"YamlConductorFactoryTest-conductResourceWithDbType.yaml"
		);

		conductor.build();

		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT * FROM dbp_tab_1 ORDER BY dt_id ASC",
				rs -> {
					/**
					 * Asserts the integer values
					 */
					new ResultSetAssert(rs)
					.assertNextTrue()
					.assertByte("dt_bigint", (byte)1)

					.assertNextTrue()
					.assertByte("dt_bigint", (byte)20)

					.assertNextTrue()
					.assertShort("dt_bigint", (short)13020)

					.assertNextTrue()
					.assertInt("dt_bigint", 107020)

					.assertNextTrue()
					.assertLong("dt_bigint", 21474836470L);
					// :~)

					/**
					 * Asserts the text values
					 */
					new ResultSetAssert(rs)
					.assertNextTrue()
					.assertString("dt_string", "CH-1")

					.assertNextTrue()
					.assertString("dt_string", "CH-2")

					.assertNextTrue()
					.assertString("dt_string", "CH-3");
					// :~)

					/**
					 * Asserts the boolean value
					 */
					new ResultSetAssert(rs)
					.assertNextTrue()
					.assertBoolean("dt_boolean", true);
					// :~)

					/**
					 * Asserts the decimal value
					 */
					new ResultSetAssert(rs)
					.assertNextTrue()
					.assertFloat("dt_decimal", 2.0f)
					.assertNextTrue()
					.assertDouble("dt_decimal", 3.5)
					.assertNextTrue()
					.assertBigDecimal("dt_decimal", new BigDecimal("4.7"), new MathContext(2))
					.assertNextTrue()
					.assertBigDecimal("dt_decimal", new BigDecimal("5.2"), new MathContext(2))
					.assertNextTrue()
					.assertBigDecimal("dt_decimal", new BigDecimal("6.2"), new MathContext(2));
					// :~)

					/**
					 * Asserts the binary data
					 */
					byte[] expectedBytes = DatatypeConverter.parseBase64Binary("VEhFIEtpbmcgd2FzIG9uIGhpcyB0aG9ybmU=");

					new ResultSetAssert(rs)
					.assertNextTrue()
					.assertBytes("dt_binary", expectedBytes)
					.assertNextTrue()
					.assertBytes("dt_binary", expectedBytes)
					.assertNextTrue()
					.assertBytes("dt_binary", expectedBytes);
					// :~)

					/**
					 * Asserts date time value
					 */
					new ResultSetAssert(rs)
					.assertNextTrue()
					.assertDate("dt_date", new java.sql.Date(1430796004000L))
					.assertNextTrue()
					.assertTime("dt_time", new java.sql.Time(1452478830000L))
					.assertNextTrue()
					.assertTimestamp("dt_timestamp", new java.sql.Timestamp(1393762810000L));
					// :~)
				}
			).runJdbc()
		).runJdbc();

		conductor.clean();
	}

	@Test(dataProvider="ConductResourceWithDbTypeOfSpecificVendors") @DoLiquibase
	public void conductResourceWithDbTypeOfSpecificVendors(
		String yamlData, int sampleId,
		DatabaseVendor[] excludeVendors,
		JdbcVoidFunction<ResultSet> assertionFunction
	) throws SQLException, IOException {
		if (Stream.of(excludeVendors).anyMatch(excludeVendor -> excludeVendor == getCurrentVendor())) {
			throw new SkipException("Skip vendor: " + getCurrentVendor());
		}

		YamlConductorFactory factory = YamlConductorFactory.build(
			getDataSource(), builder -> builder
				.resourceLoader(ReaderFunctions.loadByClass(getClass()))
		);

		DuetConductor conductor = null;
		try (
			Reader reader = new StringReader(
				YamlTags.DEFAULT_TAGS +
				"---\n" +
				"- !sql!table nsv_1 : [" + yamlData + "]"
			);
		) {
			conductor = factory.conductYaml(reader);
		}

		conductor.build();

		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT * FROM nsv_1 WHERE nt_id = " + sampleId,
				assertionFunction
			).runJdbc()
		).runJdbc();

		conductor.clean();
	}
	@DataProvider(name="ConductResourceWithDbTypeOfSpecificVendors")
	private Object[][] getConductResourceWithDbTypeOfSpecificVendors()
	{
		byte[] expectedBytes = DatatypeConverter.parseBase64Binary("VEhFIEtpbmcgd2FzIG9uIGhpcyB0aG9ybmU=");

		return new Object[][] {
			{ // CLOB
				"{ nt_id: 13, nt_clob: !dbtype!clob \"GABB-3\" }", 13,
				new DatabaseVendor[] { DatabaseVendor.PostgreSql },
				(JdbcVoidFunction<ResultSet>)rs -> {
					Connection conn = rs.getStatement().getConnection();
					Clob clob = conn.createClob();
					clob.setString(1, "GABB-3");

					new ResultSetAssert(rs)
					.assertNextTrue()
					.assertClob("nt_clob", clob);
				}
			},
			{ // BLOB
				"{ nt_id: 1, nt_blob: !dbtype!blob \"VEhFIEtpbmcgd2FzIG9uIGhpcyB0aG9ybmU=\" }", 1,
				new DatabaseVendor[] { DatabaseVendor.PostgreSql },
				(JdbcVoidFunction<ResultSet>)rs -> {
					Connection conn = rs.getStatement().getConnection();
					Blob blob = conn.createBlob();
					blob.setBytes(1, expectedBytes);

					new ResultSetAssert(rs)
					.assertNextTrue()
					.assertBlob("nt_blob", blob);
				}
			},
			{ // NSTRING
				"{ nt_id: 2, nt_nstring: !dbtype!nvarchar \"NSTR-1\" }", 2,
				new DatabaseVendor[] { DatabaseVendor.Derby, DatabaseVendor.PostgreSql },
				(JdbcVoidFunction<ResultSet>)rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertNString("nt_nstring", "NSTR-1")
			},
			{ // NCLOB
				"{ nt_id: 3, nt_nclob: !dbtype!nclob \"NSTR-2\" }", 3,
				new DatabaseVendor[] { DatabaseVendor.Derby, DatabaseVendor.PostgreSql },
				(JdbcVoidFunction<ResultSet>)rs -> {
					Connection conn = rs.getStatement().getConnection();

					NClob nclob = conn.createNClob();
					nclob.setString(1, "NSTR-2");

					new ResultSetAssert(rs)
						.assertNextTrue()
						.assertNClob("nt_nclob", nclob);
				}
			},
		};
	}

	/**
	 * Tests the exception convertion
	 */
	@Test(expectedExceptions=RuntimeException.class, expectedExceptionsMessageRegExp="Test for sqlExceptionConvert")
	public void sqlExceptionConvert() throws IOException
	{
		YamlConductorFactory factory = YamlConductorFactory.build(
			getDataSource()
		);

		try (
			Reader r = new StringReader(
				"%TAG !sql! tag:jdut.mikelue.guru:sql:1.0/\n" +
				"---\n" +
				"- !sql!code { build_operation: !sql!statement \"CREATE TABE g1(g_id ZNTP)\" }"
			);
		) {
			DuetConductor conductor = factory.conductYaml(
				r, builder -> builder.sqlExceptionConvert(e -> new RuntimeException("Test for sqlExceptionConvert"))
			);

			conductor.build();
		}
	}
}
