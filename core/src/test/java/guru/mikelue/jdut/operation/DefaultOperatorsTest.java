package guru.mikelue.jdut.operation;

import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.function.DatabaseTransactional;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.test.DoLiquibase;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This test uses {@link DefaultOperatorFactory} without any added customized operators.
 */
public class DefaultOperatorsTest extends AbstractDataSourceTestBase {
	private OperatorFactory operatorFactory;

	public DefaultOperatorsTest() {}

	/**
	 * Tests the default insert.
	 */
	@Test @DoLiquibase
	public void insert() throws SQLException
	{
		final DataGrain dataGrain = DataGrain.build(
			builder -> builder.name("do_insert"),
			builder -> builder
				.implicitColumns("dm_id", "dm_v1", "dm_v2", "dm_v3")
				.addValues(11, 11, "K1", 20)
				.addValues(12, 12, "K2", null) // Null value
				.implicitColumns("dm_id", "dm_v1", "dm_v3")
				.addValues(13, 13, 0) // default Value
		).decorate(getSchemaLoading());

		/**
		 * Executes the tested lambda
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> operatorFactory.get(DefaultOperators.INSERT).operate(conn, dataGrain)
		).runJdbc();
		// :~)

		/**
		 * Asserts data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT * FROM do_insert ORDER BY dm_id ASC",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt("dm_id", 11)
					.assertInt("dm_v1", 11)
					.assertString("dm_v2", "K1")
					.assertInt("dm_v3", 20)

					.assertNextTrue()
					.assertInt("dm_id", 12)
					.assertInt("dm_v1", 12)
					.assertString("dm_v2", "K2")
					.assertInt("dm_v3", 0)
					.assertWasNull()

					.assertNextTrue()
					.assertInt("dm_id", 13)
					.assertInt("dm_v1", 13)
					.assertString("dm_v2", "C1")
					.assertInt("dm_v3", 0)
					.assertWasNotNull()

					.assertNextFalse()
			).runJdbc()
		).runJdbc();
		// :~)
	}

	@Test @DoLiquibase
	public void update() throws SQLException
	{
		final DataGrain dataGrain = DataGrain.build(
			builder -> builder.name("do_update"),
			builder -> builder
				.implicitColumns("du_id", "du_v1")
				.addValues(1, "EXP-1")
				.addValues(2, "EXP-2")
		).decorate(getSchemaLoading());

		/**
		 * Executes the tested lambda
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> operatorFactory.get(DefaultOperators.UPDATE).operate(conn, dataGrain)
		).runJdbc();
		// :~)
;
		/**
		 * Asserts the updated data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT * FROM do_update ORDER BY du_id ASC",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertString("du_v1", "EXP-1")

					.assertNextTrue()
					.assertString("du_v1", "EXP-2")

					/**
					 * Non updated data
					 */
					.assertNextTrue()
					.assertString("du_v1", "GC3")
					// :~)

					.assertNextFalse()
			).runJdbc()
		).runJdbc();
		// :~)
	}

	@Test @DoLiquibase
	public void refresh() throws SQLException
	{
		final DataGrain dataGrain = DataGrain.build(
			builder -> builder.name("do_refresh"),
			builder -> builder
				.implicitColumns("dr_id", "dr_v1")
				.addValues(1, "RPC-1")
				.addValues(2, "RPC-2")
		).decorate(getSchemaLoading());

		/**
		 * Executes the tested lambda
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> operatorFactory.get(DefaultOperators.REFRESH).operate(conn, dataGrain)
		).runJdbc();
		// :~)

		/**
		 * Asserts the refreshed data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT * FROM do_refresh ORDER BY dr_id ASC",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertString("dr_v1", "RPC-1")

					.assertNextTrue()
					.assertString("dr_v1", "RPC-2")

					.assertNextFalse()
			).runJdbc()
		).runJdbc();
		// :~)
	}

	@Test @DoLiquibase
	public void delete() throws SQLException
	{
		final DataGrain dataGrain = DataGrain.build(
			builder -> builder.name("do_delete"),
			builder -> builder
				.implicitColumns("dd_id")
				.addValues(1)
				.addValues(2)
		).decorate(getSchemaLoading());

		/**
		 * Executes the tested lambda
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> operatorFactory.get(DefaultOperators.DELETE).operate(conn, dataGrain)
		).runJdbc();
		// :~)

		/**
		 * Asserts the deleted data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM do_delete",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
		// :~)
	}

	@Test @DoLiquibase
	public void deleteAll() throws SQLException
	{
		final DataGrain dataGrain = DataGrain.build(
			builder -> builder.name("do_delete_all"),
			builder -> builder
				.implicitColumns("dda_id")
				.addValues(1)
				.addValues(2)
		).decorate(getSchemaLoading());

		/**
		 * Executes the tested lambda
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> operatorFactory.get(DefaultOperators.DELETE_ALL).operate(conn, dataGrain)
		).runJdbc();
		// :~)

		/**
		 * Asserts the deleted data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM do_delete_all",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
		// :~)
	}

	@Test @DoLiquibase
	public void truncate() throws SQLException
	{
		final DataGrain dataGrain = DataGrain.build(
			builder -> builder.name("do_truncate"),
			builder -> builder
				.implicitColumns("dt_id")
				.addValues(1)
				.addValues(2)
		).decorate(getSchemaLoading());

		/**
		 * Executes the tested lambda
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> operatorFactory.get(DefaultOperators.TRUNCATE).operate(conn, dataGrain)
		).runJdbc();
		// :~)

		/**
		 * Asserts the deleted data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM do_truncate",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
		// :~)
	}

	@Test @DoLiquibase
	public void transactional() throws SQLException
	{
		final DataGrain dataGrain = DataGrain.build(
			builder -> builder.name("do_itx"),
			builder -> builder
				.implicitColumns("itx_id", "itx_value")
				.addValues(1, "EXP-1")
				.addValues(2, "EXP-2")
		).decorate(getSchemaLoading());

		/**
		 * Executes the tested lambda
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> operatorFactory.get(DefaultOperators.INSERT)
				.surroundedBy(DatabaseTransactional::simple)
				.operate(conn, dataGrain)
		).runJdbc();
		// :~)

		/**
		 * Asserts the added data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT * FROM do_itx ORDER BY itx_id ASC",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt("itx_id", 1)
					.assertString("itx_value", "EXP-1")

					.assertNextTrue()
					.assertInt("itx_id", 2)
					.assertString("itx_value", "EXP-2")
			).runJdbc()
		).runJdbc();
		// :~)
	}

	@Test @DoLiquibase
	public void transactionalRollback() throws SQLException
	{
		final DataGrain dataGrain = DataGrain.build(
			builder -> builder.name("do_irtx"),
			builder -> builder
				.implicitColumns("irtx_id", "irtx_value")
				.addValues(1, "EXP-1")
				.addValues(2, "EXP-2")
				.addValues(1, "EXP-1-1") // Repeated id, rollbacked
		).decorate(getSchemaLoading());

		/**
		 * Executes the tested lambda
		 */
		try {
			JdbcTemplateFactory.buildRunnable(
				() -> getDataSource().getConnection(),
				conn -> operatorFactory.get(DefaultOperators.INSERT)
					.surroundedBy(DatabaseTransactional::simple)
					.operate(conn, dataGrain)
			).runJdbc();
		} catch (Exception e) {
			getLogger().info("Has exception for testing rollback: " + e.getMessage());

			/**
			 * Asserts nothing changed
			 */
			JdbcTemplateFactory.buildRunnable(
				() -> getDataSource().getConnection(),
				conn -> DbResultSet.buildRunnable(
					conn, "SELECT COUNT(*) FROM do_irtx",
					rs -> new ResultSetAssert(rs)
						.assertNextTrue()
						.assertInt(1, 0)
				).runJdbc()
			).runJdbc();
			// :~)

			return;
		}
		// :~)

		fail("No exception thrown for rollback");
	}

	@BeforeAll
	void init()
	{
		operatorFactory = DefaultOperatorFactory.build(getDataSource(), builder -> {});
	}
}
