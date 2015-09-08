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
import guru.mikelue.jdut.annotation.IfDatabaseVendor;
import guru.mikelue.jdut.vendor.DatabaseVendor;

public class IdentityInsertOperatorTest extends AbstractDataSourceTestBase {
	public IdentityInsertOperatorTest() {}

	/**
	 * Tests the disabling identity function for MS SQL server.
	 */
	@Test @DoLiquibase @IfDatabaseVendor(match=DatabaseVendor.MsSql)
	public void operate() throws SQLException
	{
		final DataGrain dataGrain = DataGrain.build(
			table -> table.name("tab_has_ai"),
			data -> data
				.implicitColumns("ha_id", "ha_value")
				.addValues(1, "GP-1")
				.addValues(2, "GP-2")
		)
		.aggregate(
			/**
			 * Without auto-incremental
			 */
			DataGrain.build(
				table -> table.name("tab_has_no_ai"),
				data -> data
					.implicitColumns("hna_id", "hna_value")
					.addValues(1, "GPC-1")
					.addValues(2, "GPC-2")
			)
			// :~)
		)
		.decorate(getSchemaLoadingDecorator());

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
			conn -> {
				DbResultSet.buildRunnable(
					conn, "SELECT * FROM tab_has_ai ORDER BY ha_id ASC",
					rs -> new ResultSetAssert(rs)
						.assertNextTrue()
						.assertInt("ha_id", 1)
						.assertString("ha_value", "GP-1")

						.assertNextTrue()
						.assertInt("ha_id", 2)
						.assertString("ha_value", "GP-2")

						.assertNextFalse()
				).runJdbc();

				DbResultSet.buildRunnable(
					conn, "SELECT * FROM tab_has_no_ai ORDER BY hna_id ASC",
					rs -> new ResultSetAssert(rs)
						.assertNextTrue()
						.assertInt("hna_id", 1)
						.assertString("hna_value", "GPC-1")

						.assertNextTrue()
						.assertInt("hna_id", 2)
						.assertString("hna_value", "GPC-2")

						.assertNextFalse()
				).runJdbc();
			}
		).runJdbc();
		// :~)
	}
}
