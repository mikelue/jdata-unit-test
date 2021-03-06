package guru.mikelue.jdut.function;

import java.sql.Connection;
import java.util.Optional;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.function.Transactional;
import guru.mikelue.jdut.operation.DataGrainOperator;

/**
 * As surrounding of operation data grain for transactional wrapping.<br>
 *
 * <h3>Make your {@link DataGrainOperator} transactional:</h3>
 * <pre><code class="java">
 * // operator_1 - An operator to be transactional
 * operator_1 = yourOperator.surroundedBy(DatabaseTransactional::simple);
 *
 * // operator_2 = An operator to be transactional with assigned transaction isoation
 * operator_2 = yourOperator.surroundedBy(new DatabaseTransactional(Connection.TRANSACTION_SERIALIZABLE));
 * </code></pre>
 */
public class DatabaseTransactional implements DataGrainOperator.SurroundOperator {
	private final Optional<Integer> transactionIsolation;

	/**
	 * Builds {@link DataGrainOperator.SurroundOperator} with simple configuration of transaction.
	 *
	 * @param jdbcFunction The function to be surrounded
	 *
	 * @return The transactional function
	 */
	public static DataGrainOperator simple(DataGrainOperator jdbcFunction)
	{
		return new DatabaseTransactional().surround(jdbcFunction);
	}

	/**
	 * Constructs with setting of transaction isolation.
	 *
	 * @param newTransactionIsolation The value of transaction isolation
	 */
	public DatabaseTransactional(int newTransactionIsolation)
	{
		this(Optional.of(newTransactionIsolation));
	}
	/**
	 * Constructs with setting of transaction isolation.
	 *
	 * @param newTransactionIsolation The value of transaction isolation
	 */
	public DatabaseTransactional(Optional<Integer> newTransactionIsolation)
	{
		transactionIsolation = newTransactionIsolation;
	}
	/**
	 * Constructs without setting of transaction isolation.
	 */
	private DatabaseTransactional()
	{
		this(Optional.empty());
	}

	@Override
	public DataGrainOperator surround(DataGrainOperator surroundedOperator)
	{
		return (connection, dataGrain) -> {
			JdbcFunction<Connection, DataGrain> txJdbcFunction = (autoCloseConn) ->
				surroundedOperator.operate(autoCloseConn, dataGrain);
			txJdbcFunction = txJdbcFunction.surroundedBy(
				new Transactional<>(transactionIsolation)
			);

			return txJdbcFunction.applyJdbc(connection);
		};
	}
}
