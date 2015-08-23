package guru.mikelue.jdut;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.function.DatabaseSurroundOperators;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.function.DbRelease;
import guru.mikelue.jdut.operation.DataGrainOperator;
import guru.mikelue.jdut.operation.DataRowOperator;
import guru.mikelue.jdut.operation.DataRowsOperator;

/**
 * The main executor for testing data.
 */
public class DataConductor {
	private final DataSource dataSource;

	/**
	 * Constructs this object with a valid {@link DataSource} object.
	 *
	 * @param newDataSource The initialized object of data source
	 */
	public DataConductor(DataSource newDataSource)
	{
		dataSource = newDataSource;
	}

	/**
	 * Gets connection of database and feeds it to <em>operator</em>.
	 *
	 * @param dataGrain The data grain to be processed
	 * @param operator The operator to be executed
	 *
	 * @return The processed data grain
	 */
	public DataGrain conduct(DataGrain dataGrain, DataGrainOperator operator)
	{
		operator = operator.surroundedBy(DatabaseSurroundOperators::autoClose);
		try {
			return operator.operate(dataSource.getConnection(), dataGrain);
		} catch (SQLException e) {
			throw new DataConducteException(e);
		}
	}
	/**
	 * Gets connection of database and feeds it to <em>operator</em>.
	 *
	 * @param dataGrain The data grain to be processed
	 * @param operator The operator to be executed
	 *
	 * @return The processed data grain
	 */
	public DataGrain conduct(DataGrain dataGrain, DataRowsOperator operator)
	{
		return conduct(dataGrain, operator.toDataGrainOperator());
	}
	/**
	 * Gets connection of database and feeds it to <em>operator</em>.
	 *
	 * @param dataGrain The data grain to be processed
	 * @param operator The operator to be executed
	 *
	 * @return The processed data grain
	 */
	public DataGrain conduct(DataGrain dataGrain, DataRowOperator operator)
	{
		return conduct(dataGrain, operator.toDataGrainOperator());
	}

	/**
	 * Executes a {@link JdbcFunction}.
	 *
	 * @param <T> The type of returned value
	 * @param jdbcFunction The JDBC function to be executed
	 *
	 * @return The result of function
	 */
	public <T> T conduct(JdbcFunction<Connection, T> jdbcFunction)
	{
		try {
			return jdbcFunction.surroundedBy(DbRelease::autoClose)
				.apply(dataSource.getConnection());
		} catch (SQLException e) {
			throw new DataConducteException(e);
		}
	}
}
