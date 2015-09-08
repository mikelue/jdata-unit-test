package guru.mikelue.jdut;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.decorate.DataGrainDecorator;
import guru.mikelue.jdut.decorate.TableSchemaLoadingDecorator;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.function.DbRelease;
import guru.mikelue.jdut.operation.DataGrainOperator;
import guru.mikelue.jdut.operation.DataRowOperator;
import guru.mikelue.jdut.operation.DataRowsOperator;

/**
 * The main executor for testing data.
 */
public class DataConductor {
	private Logger logger = LoggerFactory.getLogger(DataConductor.class);
	private final DataSource dataSource;
	private final DataGrainDecorator schemaLoadingDecorator;

	/**
	 * Constructs this object with a valid {@link DataSource} object.
	 *
	 * @param newDataSource The initialized object of data source
	 */
	public DataConductor(DataSource newDataSource)
	{
		dataSource = newDataSource;
		schemaLoadingDecorator = new TableSchemaLoadingDecorator(dataSource);
	}

	public JdbcFunction<Connection, DataGrain> buildJdbcFunction(
		DataGrain dataGrain, DataGrainOperator operator,
		DataGrainDecorator decorator
	) {
		return conn -> {
			DataGrain dataGrainOfSchemaLoaded = dataGrain.decorate(schemaLoadingDecorator);

			if (decorator != null) {
				dataGrainOfSchemaLoaded = dataGrain.decorate(decorator);
			}

			return operator.toJdbcFunction(dataGrainOfSchemaLoaded)
				.asFunction().apply(conn);
		};
	}
	public JdbcFunction<Connection, DataGrain> buildJdbcFunction(
		DataGrain dataGrain, DataGrainOperator operator
	) {
		return buildJdbcFunction(dataGrain, operator, null);
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
		dataGrain = dataGrain.decorate(schemaLoadingDecorator);

		if (decorator.isPresent()) {
			dataGrain = dataGrain.decorate(decorator.get());
		}

		return conduct(
			operator.toJdbcFunction(dataGrain)
		);
	}

	/**
	 * Executes a {@link JdbcFunction}, the connection would be put into {@link ConductorContext}.
	 *
	 * @param <T> The type of returned value
	 * @param jdbcFunction The JDBC function to be executed
	 *
	 * @return The result of function
	 */
	public <T> T conduct(JdbcFunction<Connection, T> jdbcFunction)
	{
		try {
			return jdbcFunction
				.surroundedBy(
					f -> conn -> {
						logger.debug("Put connection to context: [{}]", conn);
						ConductorContext.setCurrentConnection(conn);

						try {
							return f.applyJdbc(conn);
						} finally {
							logger.debug("Remove connection from context: [{}]", conn);
							ConductorContext.cleanCurrentConnection();
						}
					}
				)
				.surroundedBy(DbRelease::autoClose)
				.applyJdbc(dataSource.getConnection());
		} catch (SQLException e) {
			throw new DataConductException(e);
		}
	}
}
