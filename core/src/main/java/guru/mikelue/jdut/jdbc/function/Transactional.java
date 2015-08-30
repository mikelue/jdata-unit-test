package guru.mikelue.jdut.jdbc.function;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.JdbcRunnable;

/**
 * As surrounding of jdbc function for transactional wrapping.<br>
 *
 * @param <T> The type of connection object
 * @param <R> The type of returned value
 */
public final class Transactional<T extends Connection, R> implements JdbcFunction.SurroundOperator<T, R> {
	private final Optional<Integer> transactionIsolation;

	/**
	 * Builds {@link JdbcFunction.SurroundOperator} with simple configuration of transaction.
	 *
	 * @param jdbcFunction The function to be surrounded
	 *
	 * @return The transactional function
	 */
	public static <T extends Connection, R> JdbcFunction<T, R> simple(JdbcFunction<T, R> jdbcFunction)
	{
		return new Transactional<T, R>().surround(jdbcFunction);
	}

	/**
	 * Constructs with setting of transaction isolation.
	 *
	 * @param newTransactionIsolation The value of transaction isolation
	 */
	public Transactional(int newTransactionIsolation)
	{
		this(Optional.of(newTransactionIsolation));
	}
	/**
	 * Constructs with setting of transaction isolation.
	 *
	 * @param newTransactionIsolation The value of transaction isolation
	 */
	public Transactional(Optional<Integer> newTransactionIsolation)
	{
		transactionIsolation = newTransactionIsolation;
	}
	/**
	 * Constructs without setting of transaction isolation.
	 */
	private Transactional()
	{
		this(Optional.empty());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public JdbcFunction<T, R> surround(JdbcFunction<T, R> jdbcFunction)
	{
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
				result = jdbcFunction.applyJdbc(conn);
			} catch (SQLException e) {
				conn.rollback();
				throw e;
			}

			conn.commit();

			return result;
		};
	}
}
