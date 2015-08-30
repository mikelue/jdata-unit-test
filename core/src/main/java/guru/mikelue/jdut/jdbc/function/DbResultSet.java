package guru.mikelue.jdut.jdbc.function;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.JdbcRunnable;
import guru.mikelue.jdut.jdbc.JdbcSupplier;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.JdbcVoidFunction;

/**
 * Utility used to build {@link JdbcRunnable} or {@link JdbcSupplier}
 * for processing a {@link ResultSet} with some primitive values(e.g., SQL string).
 */
public interface DbResultSet {
	/**
	 * Builds runnable by connection and SQL string.<br>
	 *
	 * This method would use {@link Statement#executeQuery} for getting result set.
	 *
	 * @param conn The connection of database(won't be closed in the lambda)
	 * @param sql The SQL to query data
	 * @param executor The executor of result set from query
	 *
	 * @return The instance of lambda
	 */
	public static JdbcRunnable buildRunnable(
		Connection conn, String sql,
		JdbcVoidFunction<ResultSet> executor
	) {
		return JdbcTemplateFactory.buildRunnable(
			() -> conn.createStatement(),
			stat -> JdbcTemplateFactory.buildRunnable(
				() -> stat.executeQuery(sql),
				rs -> executor.apply(rs)
			).run()
		);
	}

	/**
	 * Builds runnable by {@link Statement} and SQL string.<br>
	 *
	 * This method would use {@link Statement#executeQuery} for getting result set.
	 *
	 * @param statement The statement to be used(won't be closed in the lambda)
	 * @param sql The SQL to query data
	 * @param executor The executor of accepting set from query
	 *
	 * @return The instance of lambda
	 */
	public static JdbcRunnable buildRunnable(
		Statement statement, String sql,
		JdbcVoidFunction<ResultSet> executor
	) {
		return JdbcTemplateFactory.buildRunnable(
			() -> statement.executeQuery(sql),
			rs -> executor.apply(rs)
		);
	}

	/**
	 * Builds supplier by connection and SQL string.<br>
	 *
	 * This method would use {@link Statement#executeQuery} for getting result set.
	 *
	 * @param <T> The type of returned value
	 * @param conn The connection of database(won't be closed in the lambda)
	 * @param sql The SQL to query data
	 * @param supplier The supplier accepting result set from query
	 *
	 * @return The instance of lambda
	 */
	public static <T> JdbcSupplier<T> buildSupplier(
		Connection conn, String sql,
		JdbcFunction<? super ResultSet, ? extends T> supplier
	) {
		return JdbcTemplateFactory.buildSupplier(
			() -> conn.createStatement(),
			stat -> JdbcTemplateFactory.buildSupplier(
				() -> stat.executeQuery(sql),
				rs -> supplier.apply(rs)
			).get()
		);
	}

	/**
	 * Builds supplier by {@link Statement} and SQL string.<br>
	 *
	 * This method would use {@link Statement#executeQuery} for getting result set.
	 *
	 * @param <T> The type of returned value
	 * @param statement The statement to be used(won't be closed in the lambda)
	 * @param sql The SQL to query data
	 * @param supplier The supplier accepting result set from query
	 *
	 * @return The instance of lambda
	 */
	public static <T> JdbcSupplier<T> buildSupplier(
		Statement statement, String sql,
		JdbcFunction<? super ResultSet, ? extends T> supplier
	) {
		return JdbcTemplateFactory.buildSupplier(
			() -> statement.executeQuery(sql),
			rs -> supplier.apply(rs)
		);
	}
}
