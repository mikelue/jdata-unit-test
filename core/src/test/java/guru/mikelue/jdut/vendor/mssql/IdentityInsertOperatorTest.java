package guru.mikelue.jdut.vendor.mssql;

import java.sql.SQLException;

import org.testng.annotations.Test;

import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.operation.DefaultOperators;
import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.test.DoLiquibase;
import guru.mikelue.jdut.test.IfVendor;
import guru.mikelue.jdut.vendor.DatabaseVendor;

public class IdentityInsertOperatorTest extends AbstractDataSourceTestBase {
	public IdentityInsertOperatorTest() {}

	/**
	 * Tests the disabling identity function for MS SQL server.
	 */
	@Test @DoLiquibase @IfVendor(match=DatabaseVendor.MsSql)
	public void operate() throws SQLException
	{
		final DataGrain dataGrain = DataGrain.build(
			table -> table.name("tab_did"),
			data -> data
				.implicitColumns("ti_id", "ti_value")
				.addValues(1, "GP-1")
				.addValues(2, "GP-2")
		).decorate(getSchemaLoadingDecorator());

		/**
		 * Uses insert operation for testing
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> MsSql.DEFAULT_OPERATORS.get(
				DefaultOperators.INSERT
			).operate(conn, dataGrain)
		).runJdbc();
		// :~)

		/**
		 * Asserts data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT * FROM tab_did ORDER BY ti_id ASC",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt("ti_id", 1)
					.assertString("ti_value", "GP-1")

					.assertNextTrue()
					.assertInt("ti_id", 2)
					.assertString("ti_value", "GP-2")

					.assertNextFalse()
			).runJdbc()
		).runJdbc();
		// :~)
	}
}
