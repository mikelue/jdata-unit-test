package guru.mikelue.jdut.function;

import java.sql.Connection;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.function.DbRelease;
import guru.mikelue.jdut.operation.DataGrainOperator;

/**
 * Builds surrounding operator with database operations.
 */
public interface DatabaseSurroundOperators {
	/**
	 * Surrounds <em>operator</em> to be auto closed.
	 *
 	 * @param surroundedOperator The operator to be surrounded
	 *
	 * @return The operate close the connection after the surrounded operator is returned
	 *
	 * @see DataGrainOperator.SurroundOperator
	 */
	public static DataGrainOperator autoClose(DataGrainOperator surroundedOperator)
	{
		return (connection, dataGrain) -> {
			JdbcFunction<Connection, DataGrain> autoCloseJdbcFunction = (autoCloseConn) ->
				surroundedOperator.operate(autoCloseConn, dataGrain);
			autoCloseJdbcFunction = autoCloseJdbcFunction.surroundedBy(DbRelease::autoClose);

			return autoCloseJdbcFunction.applyJdbc(connection);
		};
	}
}
