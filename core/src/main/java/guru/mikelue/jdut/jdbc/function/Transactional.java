package guru.mikelue.jdut.jdbc.function;

import java.sql.Connection;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.jdbc.JdbcFunction;

/**
 * As surrounding of JDBC function for transactional wrapping.<br>
 *
 * <p>For example:</p>
 * <pre>{@code
 * // function_1 - An instance of JdbcFunction
 * function_1 = function_1.surroundedBy(Transactional::simple);
 *
 * // function_2 - An instance of JdbcFunction
 * function_2 = function_2.surroundedBy(new Transaction(Connection.TRANSACTION_READ_COMMITTED));
 * }</pre>
 *
 * @param <T> The type of connection object
 * @param <R> The type of returned value
 */
public final class Transactional<T extends Connection, R> implements JdbcFunction.SurroundOperator<T, R> {
	private Logger logger = LoggerFactory.getLogger(Transactional.class);
	private final Optional<Integer> transactionIsolation;

	/**
	 * Builds {@link JdbcFunction.SurroundOperator} with simple configuration of transaction.
	 *
 	 * @param <T> The type of connection object
 	 * @param <R> The type of returned value
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
			boolean oldAutoCommit = conn.getAutoCommit();
			if (oldAutoCommit) {
				logger.debug("Set auto commit to false");
				conn.setAutoCommit(false);
			}

			int oldIsolation = conn.getTransactionIsolation();
			boolean needResetIsolation =
				transactionIsolation.isPresent() &&
				transactionIsolation.get() != oldIsolation;
			if (needResetIsolation) {
				logger.debug("Set transaction isolation to: [{}]", transactionIsolation.get());
				conn.setTransactionIsolation(transactionIsolation.get());
			}

			R result = null;
			try {
				result = jdbcFunction.applyJdbc(conn);
			} catch (Exception e) {
				try {
					logger.info("Before rollback transaction: [ {} ]", e.getMessage());
					conn.rollback();
					logger.debug("After rollback transaction");
				} finally {
					if (oldAutoCommit) {
						logger.debug("Set auto commit to back to true");
						conn.setAutoCommit(oldAutoCommit);
					}
					if (needResetIsolation) {
						logger.debug("Set back transaction isolation to: [{}]", oldIsolation);
						conn.setTransactionIsolation(oldIsolation);
					}
				}

				throw e;
			}

			try {
				logger.debug("Before commit transaction");
				conn.commit();
				logger.debug("After commit transaction");
			} finally {
				if (oldAutoCommit) {
					logger.debug("Set auto commit to back to true");
					conn.setAutoCommit(oldAutoCommit);
				}
				if (needResetIsolation) {
					logger.debug("Set back transaction isolation to: [{}]", oldIsolation);
					conn.setTransactionIsolation(oldIsolation);
				}
			}

			return result;
		};
	}
}
