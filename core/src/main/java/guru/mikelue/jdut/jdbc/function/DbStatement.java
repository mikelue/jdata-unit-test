package guru.mikelue.jdut.jdbc.function;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

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
		JdbcVoidFunction<PreparedStatement> executor
	) {
		return JdbcTemplateFactory.buildRunnable(
			() -> conn.prepareStatement(sql),
			stat -> {
				nameOfColumns.forEach(
					new IndexedParameterSetter(stat, dataRow)
						.asConsumer()
				);

				executor.apply(stat);
			}
		);
	}

	/**
	 * Builds supplier to set-up {@link PreparedStatement} and fed it to void function.
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
					new IndexedParameterSetter(stat, dataRow)
						.asConsumer()
				);

				return supplier.apply(stat);
			}
		);
	}

	private static class IndexedParameterSetter implements JdbcVoidFunction<String> {
		private PreparedStatement statement;
		private DataRow dataRow;
		private int paramIndex = 1;

		private IndexedParameterSetter(
			PreparedStatement newStatement, DataRow newDataRow
		) {
			statement = newStatement;
			dataRow = newDataRow;
		}

		@Override
		public void apply(String columnName) throws SQLException
		{
			PreparedStatements.setParameter(
				statement, dataRow, columnName,
				paramIndex++
			);
		}
	}
}
