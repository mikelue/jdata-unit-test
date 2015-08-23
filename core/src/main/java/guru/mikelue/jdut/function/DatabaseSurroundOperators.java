package guru.mikelue.jdut.function;

import java.sql.Connection;
import java.util.Optional;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.function.DbConnection;
import guru.mikelue.jdut.jdbc.function.DbRelease;
import guru.mikelue.jdut.operation.DataGrainOperator;
import guru.mikelue.jdut.operation.DataGrainOperator.SurroundOperator;

/**
 * Builds surrounding operator with database operations.
 */
public interface DatabaseSurroundOperators {
	/**
	 * Builds surrounding operator to make <em>operator</em> to be transactional.
	 *
	 * @param transactionIsolation The value of transaction isolation
	 *
	 * @return The operate starts the transaction of connection before executing and commits after executing.
	 */
	public static SurroundOperator operateTransactional(int transactionIsolation)
	{
		return operateTransactional(Optional.of(transactionIsolation));
	}
	/**
	 * Builds surrounding operator to make <em>operator</em> to be transactional.
	 *
	 * @param transactionIsolation The value of transaction isolation
	 *
	 * @return The operate starts the transaction of connection before executing and commits after executing.
	 */
	public static SurroundOperator operateTransactional(Optional<Integer> transactionIsolation)
	{
		return o -> transactional(o, transactionIsolation);
	}

	/**
	 * Surrounds <em>operator</em> to be transactional.
	 *
 	 * @param surroundedOperator The operator to be surrounded
	 *
	 * @return The operate starts the transaction of connection before executing and commits after executing.
	 */
	public static DataGrainOperator transactional(DataGrainOperator surroundedOperator)
	{
		return transactional(surroundedOperator, Optional.empty());
	}
	/**
	 * Surrounds <em>operator</em> to be transactional.
	 *
 	 * @param surroundedOperator The operator to be surrounded
	 * @param transactionIsolation The value of transaction isolation
	 *
	 * @return The operate starts the transaction of connection before executing and commits after executing.
	 */
	public static DataGrainOperator transactional(DataGrainOperator surroundedOperator, int transactionIsolation)
	{
		return transactional(surroundedOperator, Optional.of(transactionIsolation));
	}

	/**
	 * Surrounds <em>operator</em> to be transactional.
	 *
 	 * @param surroundedOperator The operator to be surrounded
	 * @param transactionIsolation The value of transaction isolation
	 *
	 * @return The operate starts the transaction of connection before executing and commits after executing.
	 */
	public static DataGrainOperator transactional(DataGrainOperator surroundedOperator, Optional<Integer> transactionIsolation)
	{
		return (connection, dataGrain) -> {
			JdbcFunction<Connection, DataGrain> autoCloseJdbcFunction = (autoCloseConn) ->
				surroundedOperator.operate(autoCloseConn, dataGrain);
			autoCloseJdbcFunction = autoCloseJdbcFunction.surroundedBy(DbConnection.operateTransactional(transactionIsolation));

			return autoCloseJdbcFunction.apply(connection);
		};
	}

	/**
	 * Surrounds <em>operator</em> to be auto closed.
	 *
 	 * @param surroundedOperator The operator to be surrounded
	 *
	 * @return The operate close the connection after the surrounded operator is returned
	 */
	public static DataGrainOperator autoClose(DataGrainOperator surroundedOperator)
	{
		return (connection, dataGrain) -> {
			JdbcFunction<Connection, DataGrain> autoCloseJdbcFunction = (autoCloseConn) ->
				surroundedOperator.operate(autoCloseConn, dataGrain);
			autoCloseJdbcFunction = autoCloseJdbcFunction.surroundedBy(DbRelease::autoClose);

			return autoCloseJdbcFunction.apply(connection);
		};
	}
}
