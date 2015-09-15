package guru.mikelue.jdut;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.decorate.DataGrainDecorator;
import guru.mikelue.jdut.decorate.TableSchemaLoadingDecorator;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.function.DbRelease;
import guru.mikelue.jdut.operation.DataGrainOperator;
import guru.mikelue.jdut.operation.DataRowOperator;
import guru.mikelue.jdut.operation.DataRowsOperator;

/**
 * The main executor of {@link DataGrainOperator} for {@link DataGrain}.<br>
 *
 * <h3>Main functions</h3>
 * <p>This object is responsible for retrieving {@link Connection} from {@link DataSource},
 * and uses the connection to execute any method of {@link #conduct(DataGrain, DataGrainOperator)}.</p>
 *
 * <p>Before operating the action on data grain, this conductor would load database schema by {@link TableSchemaLoadingDecorator} object, which caches loaded schema of tables.</p>
 *
 * <h3>Afterward decorating</h3>
 * <p>Every method provided by this object has an overloading method with additional {@link DataGrainDecorator},
 * the decorator is used after the loading of table schema on the data grain.</p>
 *
 * <p>
 * The building/cleaning actions defined by {@link DuetConductor}, however, doesn't know the decorator has decorated
 * the data grain or not, you should be cautious about the re-decorating behaviour.<br>
 *
 * {@link DataRow} object has {@link DataRow#putAttribute putAttribute} and {@link DataRow#getAttribute getAttribute} method
 * to let you keep supplementary information of the row. These method is useful for implementing {@link DataGrainDecorator}.
 * </p>
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

	/**
	 * Builds JDBC function, which loads schema and executes <em>operator</em> on
	 * <em>dataGrain</em> object.
	 *
	 * @param dataGrain The data grain object to be operated
	 * @param operator The operator to affects database for testing
	 * @param decorator The decorator to decorate the data grain after loading of table chema
	 *
	 * @return The function of JDBC
	 */
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
	/**
	 * Builds JDBC function, which loads schema and executes <em>operator</em> on
	 * <em>dataGrain</em> object.
	 *
	 * @param dataGrain The data grain object to be operated
	 * @param operator The operator to affects database for testing
	 *
	 * @return The function of JDBC
	 */
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
