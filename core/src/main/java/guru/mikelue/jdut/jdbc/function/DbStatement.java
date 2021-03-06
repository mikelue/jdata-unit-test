package guru.mikelue.jdut.jdbc.function;

import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.function.Function;

import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.JdbcRunnable;
import guru.mikelue.jdut.jdbc.JdbcSupplier;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.JdbcVoidFunction;
import guru.mikelue.jdut.jdbc.util.PreparedStatements;

/**
 * Utility used to build {@link JdbcRunnable} or {@link JdbcSupplier}
 * for processing a {@link Statement} or {@link PreparedStatement} with other dependencies(e.g., {@link DataRow}).
 */
public final class DbStatement {
	private DbStatement() {}

	/**
	 * Builds runnable to set-up {@link PreparedStatement} and fed it to void function.
	 *
	 * @param conn The connection of database(won't be closed in the lambda)
	 * @param sql The SQL for executing statement
	 * @param nameOfColumns The sequence of columns used to call setXXX() of statement
	 * @param dataRow The data to be set into statement
	 * @param executor The executor for built statement
	 *
	 * @return The instance of lambda
	 */
	public static JdbcRunnable buildRunnableForPreparedStatement(
		Connection conn,
		String sql, List<String> nameOfColumns,
		DataRow dataRow,
		JdbcVoidFunction<? super PreparedStatement> executor
	) {
		return JdbcTemplateFactory.buildRunnable(
			() -> conn.prepareStatement(sql),
			stat -> {
				nameOfColumns.forEach(
					new DataRowParameterSetter(stat, dataRow)
						.asConsumer()
				);

				executor.applyJdbc(stat);
			}
		);
	}

	/**
	 * Builds runnable to set-up {@link PreparedStatement} and fed it to void function.
	 *
	 * @param conn The connection of database(won't be closed in the lambda)
	 * @param sql The SQL for executing statement
	 * @param values The sequence of values used to call setXXX() of statement
	 * @param jdbcTypeMapping The type of JDBC mapping by type of data
	 * @param executor The executor for built statement
	 *
	 * @return The instance of lambda
	 *
	 * @see PreparedStatements#setParameter(PreparedStatement, Object, JDBCType, int)
	 */
	public static JdbcRunnable buildRunnableForPreparedStatement(
		Connection conn,
		String sql,
		List<Object> values, Function<Object, JDBCType> jdbcTypeMapping,
		JdbcVoidFunction<? super PreparedStatement> executor
	) {
		return JdbcTemplateFactory.buildRunnable(
			() -> conn.prepareStatement(sql),
			stat -> {
				values.forEach(
					new ValueParameterSetter(stat, jdbcTypeMapping)
						.asConsumer()
				);

				executor.applyJdbc(stat);
			}
		);
	}

	/**
	 * Builds supplier to set-up {@link PreparedStatement} and fed it to function.
	 *
	 * @param <T> The type of returned value
	 * @param conn The connection of database(won't be closed in the lambda)
	 * @param sql The SQL for executing statement
	 * @param values The sequence of values used to call setXXX() of statement
	 * @param jdbcTypeMapping The type of JDBC mapping by type of data
	 * @param supplier The supplier for built statement
	 *
	 * @return The instance of lambda
	 *
	 * @see PreparedStatements#setParameter(PreparedStatement, Object, JDBCType, int)
	 */
	public static <T> JdbcSupplier<T> buildSupplierForPreparedStatement(
		Connection conn,
		String sql,
		List<Object> values, Function<Object, JDBCType> jdbcTypeMapping,
		JdbcFunction<? super PreparedStatement, ? extends T> supplier
	) {
		return JdbcTemplateFactory.buildSupplier(
			() -> conn.prepareStatement(sql),
			stat -> {
				values.forEach(
					new ValueParameterSetter(stat, jdbcTypeMapping)
						.asConsumer()
				);

				return supplier.applyJdbc(stat);
			}
		);
	}

	/**
	 * Builds supplier to set-up {@link PreparedStatement} and fed it to function.
	 *
	 * @param <T> The type of returned value
	 * @param conn The connection of database(won't be closed in the lambda)
	 * @param sql The SQL for executing statement
	 * @param nameOfColumns The sequence of columns used to call setXXX() of statement
	 * @param dataRow The data to be set into statement
	 * @param supplier The supplier for built statement
	 *
	 * @return The instance of lambda
	 */
	public static <T> JdbcSupplier<T> buildSupplierForPreparedStatement(
		Connection conn,
		String sql, List<String> nameOfColumns,
		DataRow dataRow,
		JdbcFunction<? super PreparedStatement, ? extends T> supplier
	) {
		return JdbcTemplateFactory.buildSupplier(
			() -> conn.prepareStatement(sql),
			stat -> {
				nameOfColumns.forEach(
					new DataRowParameterSetter(stat, dataRow)
						.asConsumer()
				);

				return supplier.applyJdbc(stat);
			}
		);
	}

	/**
	 * Builds runnable to set-up {@link Statement} and fed it to void function.
	 *
	 * @param conn The connection of database(won't be closed in the lambda)
	 * @param executor The executor for built statement
	 *
	 * @return The instance of lambda
	 */
	public static JdbcRunnable buildRunnableForStatement(
		Connection conn,
		JdbcVoidFunction<? super Statement> executor
	) {
		return JdbcTemplateFactory.buildRunnable(
			() -> conn.createStatement(),
			stat -> executor.applyJdbc(stat)
		);
	}

	/**
	 * Builds supplier to set-up {@link Statement} and fed it to function.
	 *
	 * @param <T> The type of returned value
	 * @param conn The connection of database(won't be closed in the lambda)
	 * @param supplier The supplier for built statement
	 *
	 * @return The instance of lambda
	 */
	public static <T> JdbcSupplier<T> buildSupplierForStatement(
		Connection conn,
		JdbcFunction<? super Statement, ? extends T> supplier
	) {
		return JdbcTemplateFactory.buildSupplier(
			() -> conn.createStatement(),
			stat -> supplier.applyJdbc(stat)
		);
	}

	private static class DataRowParameterSetter implements JdbcVoidFunction<String> {
		private PreparedStatement statement;
		private DataRow dataRow;
		private int paramIndex = 1;

		private DataRowParameterSetter(
			PreparedStatement newStatement, DataRow newDataRow
		) {
			statement = newStatement;
			dataRow = newDataRow;
		}

		@Override
		public void applyJdbc(String columnName) throws SQLException
		{
			PreparedStatements.setParameter(
				statement, dataRow, columnName,
				paramIndex++
			);
		}
	}

	private static class ValueParameterSetter implements JdbcVoidFunction<Object> {
		private PreparedStatement statement;
		private Function<Object, JDBCType> jdbcTypeMapping;
		private int paramIndex = 1;

		private ValueParameterSetter(
			PreparedStatement newStatement,
			Function<Object, JDBCType> newJdbcTypeMapping
		) {
			statement = newStatement;
			jdbcTypeMapping = newJdbcTypeMapping;
		}

		@Override
		public void applyJdbc(Object data) throws SQLException
		{
			PreparedStatements.setParameter(
				statement, data, jdbcTypeMapping.apply(data), paramIndex++
			);
		}
	}
}
