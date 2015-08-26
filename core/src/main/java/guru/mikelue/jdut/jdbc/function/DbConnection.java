package guru.mikelue.jdut.jdbc.function;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.JdbcRunnable;

/**
 * Functions for {@link Connection}, which are used to wrap existing {@link JdbcFunction} with database features(e.x. transaction).
 *
 * @see JdbcFunction.SurroundOperator
 */
public interface DbConnection {
	/**
	 * Builds a {@link JdbcFunction.SurroundOperator} for {@link #transactional(JdbcFunction, int)}.
	 *
	 * @param <T> The fed object must be type of {@link Connection}
 	 * @param <R> The type of returned object
	 * @param transactionIsolation The isolation of transaction
	 *
	 * @return The function making {@link Connection} to be transactional.
	 *
	 * @see #transactional(JdbcFunction, Optional)
	 */
	public static <T extends Connection, R> JdbcFunction.SurroundOperator<T, R> operateTransactional(int transactionIsolation)
	{
		return operateTransactional(Optional.of(transactionIsolation));
	}
	/**
	 * Builds a {@link JdbcFunction.SurroundOperator} for {@link #transactional(JdbcFunction, Optional)}.
	 *
	 * @param <T> The fed object must be type of {@link Connection}
 	 * @param <R> The type of returned object
	 * @param transactionIsolation The isolation of transaction
	 *
	 * @return The function making {@link Connection} to be transactional.
	 *
	 * @see #transactional(JdbcFunction, Optional)
	 */
	public static <T extends Connection, R> JdbcFunction.SurroundOperator<T, R> operateTransactional(Optional<Integer> transactionIsolation)
	{
		return f -> transactional(f, transactionIsolation);
	}

	/**
	 * Surrounds a function accepting {@link Connection} to be transactional.
	 *
	 * @param <T> The fed object must be type of {@link Connection}
 	 * @param <R> The type of returned object
	 * @param surroundedFunction The function to be surrounded
	 *
	 * @return The function making {@link Connection} to be transactional.
	 *
	 * @see #transactional(JdbcFunction, Optional)
	 */
	public static <T extends Connection, R> JdbcFunction<T, R> transactional(
		JdbcFunction<T, R> surroundedFunction
	) {
		return transactional(surroundedFunction, Optional.empty());
	}
	/**
	 * Surrounds a function accepting {@link Connection} to be transactional.
	 *
	 * @param <T> The fed object must be type of {@link Connection}
 	 * @param <R> The type of returned object
	 * @param surroundedFunction The function to be surrounded
	 * @param transactionIsolation The value of transaction isolation
	 *
	 * @return The function making {@link Connection} to be transactional.
	 *
	 * @see #transactional(JdbcFunction, Optional)
	 * @see Connection for transaction value
	 */
	public static <T extends Connection, R> JdbcFunction<T, R> transactional(
		JdbcFunction<T, R> surroundedFunction,
		int transactionIsolation
	) {
		return transactional(surroundedFunction, transactionIsolation);
	}
	/**
	 * Surrounds a function accepting {@link Connection} to be transactional.
	 *
	 * @param <T> The fed object must be type of {@link Connection}
 	 * @param <R> The type of returned object
	 * @param surroundedFunction The function to be surrounded
	 * @param transactionIsolation The value of transaction isolation
	 *
	 * @return The function making {@link Connection} to be transactional.
	 *
	 * @see #transactional(JdbcFunction, int)
	 * @see Connection for transaction value
	 */
	public static <T extends Connection, R> JdbcFunction<T, R> transactional(
		JdbcFunction<T, R> surroundedFunction,
		Optional<Integer> transactionIsolation
	) {
		return conn -> {
			conn.setAutoCommit(false);

			transactionIsolation.ifPresent(
				txIsolation ->
					((JdbcRunnable)
					 () -> conn.setTransactionIsolation(txIsolation)
					).asRunnable().run()
			);

			R result = null;
			try {
				result = surroundedFunction.apply(conn);
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}

			conn.commit();

			return result;
		};
	}
}
