package guru.mikelue.jdut;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.decorate.DataGrainDecorator;
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
		return conduct(dataGrain, operator, Optional.empty());
	}

	/**
	 * Gets connection of database and feeds it to <em>operator</em>.
	 *
	 * @param dataGrain The data grain to be processed
	 * @param operator The operator to be executed
	 * @param decorator The decorator used after schema matching
	 *
	 * @return The processed data grain
	 */
	public DataGrain conduct(DataGrain dataGrain, DataGrainOperator operator, DataGrainDecorator decorator)
	{
		return conduct(dataGrain, operator, Optional.of(decorator));
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
	 * @param decorator The decorator used after schema matching
	 *
	 * @return The processed data grain
	 */
	public DataGrain conduct(DataGrain dataGrain, DataRowsOperator operator, DataGrainDecorator decorator)
	{
		return conduct(dataGrain, operator.toDataGrainOperator(), decorator);
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
	 * Gets connection of database and feeds it to <em>operator</em>.
	 *
	 * @param dataGrain The data grain to be processed
	 * @param operator The operator to be executed
	 * @param decorator The decorator used after schema matching
	 *
	 * @return The processed data grain
	 */
	public DataGrain conduct(DataGrain dataGrain, DataRowOperator operator, DataGrainDecorator decorator)
	{
		return conduct(dataGrain, operator.toDataGrainOperator(), decorator);
	}

	private DataGrain conduct(DataGrain dataGrain, DataGrainOperator operator, Optional<DataGrainDecorator> decorator)
	{
		if (decorator.isPresent()) {
			dataGrain = dataGrain.decorate(decorator.get());
		}

		return conduct(
			operator.toJdbcFunction(dataGrain)
		);
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
			throw new DataConductException(e);
		}
	}
}
