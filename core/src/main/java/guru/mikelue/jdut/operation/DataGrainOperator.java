package guru.mikelue.jdut.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.SQLExceptionConvert;

/**
 * Operator to execute code by fed {@link DataGrain}.
 *
 * @see <a target="_blank" href="https://github.com/mikelue/jdata-unit-test/wiki/Provided-data-operations">Provided operators</a>
 */
@FunctionalInterface
public interface DataGrainOperator {
	/**
	 * The operator for surrounding of {@link DataGrainOperator}.
	 */
	@FunctionalInterface
	public interface SurroundOperator {
		/**
		 * Converts this lambda to {@link UnaryOperator}.
		 *
		 * @return The unary operator
		 */
		default UnaryOperator<DataGrainOperator> asUnaryOperator()
		{
			return operator -> surround(operator);
		}

		/**
		 * Surrounds operator.
		 *
		 * @param surroundedOperator The oprator to be surrounded
		 *
		 * @return The final function
		 */
		public DataGrainOperator surround(DataGrainOperator surroundedOperator);
	}

	/**
	 * Does nothing.
	 *
	 * @param conn The connection object
	 * @param dataGrain The object of data grain
	 *
	 * @return The same data grain
	 */
	static DataGrain none(Connection conn, DataGrain dataGrain) { return dataGrain; }

	/**
	 * Converts this lambda to {@link JdbcFunction}.
	 *
	 * @param dataGrain The data grain to be fed
	 *
	 * @return The JDBC function */
	default JdbcFunction<Connection, DataGrain> toJdbcFunction(DataGrain dataGrain)
	{
		return conn -> operate(conn, dataGrain);
	}

	/**
	 * Converts this operator to {@link BiConsumer} with {@link SQLExceptionConvert} for thrown {@link SQLException}.
	 *
	 * @param <E> The type of runtime exception
	 * @param sqlExceptionConvert The conversion of SQLException
	 *
	 * @return A {@link BiConsumer} instance
	 */
	default <E extends RuntimeException> BiConsumer<Connection, DataGrain> asBiConsumer(SQLExceptionConvert<E> sqlExceptionConvert)
	{
		return (connection, dataGrain) -> {
			try {
				operate(connection, dataGrain);
			} catch (SQLException e) {
				throw sqlExceptionConvert.apply(e);
			}
		};
	}
	/**
	 * Converts this operator to {@link BiConsumer} with default {@link SQLExceptionConvert}.
	 *
	 * @return A {@link BiConsumer} instance
	 */
	default BiConsumer<Connection, DataGrain> asBiConsumer()
	{
		return asBiConsumer(SQLExceptionConvert::runtimeException);
	}

	/**
	 * Surrounds this instance by {@link SurroundOperator}.
	 *
	 * @param surroundOperator The operator to surrounding staff
	 *
	 * @return new operator of data grain
	 */
	default DataGrainOperator surroundedBy(SurroundOperator surroundOperator)
	{
		return surroundOperator.surround(this);
	}

	/**
	 * Operates {@link DataGrain} by {@link Connection}.
	 *
	 * @param connection The connection of database
	 * @param dataGrain The data grain to be operated
	 *
	 * @return The processed data grain
	 *
	 * @throws SQLException The SQL exception defined by JDBC
	 */
	public DataGrain operate(Connection connection, DataGrain dataGrain) throws SQLException;
}
