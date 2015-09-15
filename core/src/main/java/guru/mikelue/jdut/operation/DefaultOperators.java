package guru.mikelue.jdut.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.datagrain.SchemaTable;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbStatement;

/**
 * Defines the name of operators.
 */
public final class DefaultOperators {
	/**
	 * As the name of INSERT(data must not be existing).
	 */
	public final static String INSERT = "INSERT";
	/**
	 * As the name of UPDATE(data must be existing).
	 */
	public final static String UPDATE = "UPDATE";
	/**
	 * As the name of REFRESH(data may be or may not be existing).
	 */
	public final static String REFRESH = "REFRESH";
	/**
	 * As the name of DELETE(delete single data by key filter).
	 */
	public final static String DELETE = "DELETE";
	/**
	 * As the name of DELETE(delete all data of table).
	 */
	public final static String DELETE_ALL = "DELETE_ALL";
	/**
	 * As the name of TRUNCATE(non-undo truncating of data of table).
	 */
	public final static String TRUNCATE = "TRUNCATE";
	/**
	 * As the name of NONE(do nothing).
	 */
	public final static String NONE = "NONE";

	private final static Logger logger = getLogger(DefaultOperators.class);

	private DefaultOperators() {}

	/**
	 * Default operator of insertion. As lambda of {@link DataGrainOperator}.
	 *
	 * @param conn The connection object of initialized
	 * @param dataGrain The data grain to be processed
	 *
	 * @return input data grain
	 *
	 * @throws SQLException as the defined functional interface
	 */
	public static DataGrain insert(Connection conn, DataGrain dataGrain)
		throws SQLException
	{
		logger.debug("Start default INSERT");

		DataGrain result = ((DataRowOperator)DefaultOperators::doInsert).toDataGrainOperator()
			.operate(conn, dataGrain);

		logger.debug("End default INSERT");

		return result;
	}

	/**
	 * Default operator of update. As lambda of {@link DataGrainOperator}.
	 *
	 * @param conn The connection object of initialized
	 * @param dataGrain The data grain to be processed
	 *
	 * @return input data grain
	 *
	 * @throws SQLException as the defined functional interface
	 */
	public static DataGrain update(Connection conn, DataGrain dataGrain)
		throws SQLException
	{
		logger.debug("Start default UPDATE");

		DataGrain result = ((DataRowOperator)DefaultOperators::doUpdate).toDataGrainOperator()
			.operate(conn, dataGrain);

		logger.debug("End default UPDATE");
		return result;
	}

	/**
	 * Default operator of refresh(insert or update). As lambda of {@link DataGrainOperator}.
	 *
	 * @param conn The connection object of initialized
	 * @param dataGrain The data grain to be processed
	 *
	 * @return input data grain
	 *
	 * @throws SQLException as the defined functional interface
	 */
	public static DataGrain refresh(Connection conn, DataGrain dataGrain)
		throws SQLException
	{
		logger.debug("Start default REFRESH");

		DataGrain result = ((DataRowOperator)DefaultOperators::doRefresh).toDataGrainOperator()
			.operate(conn, dataGrain);

		logger.debug("End default REFRESH");
		return result;
	}

	/**
	 * Default operator of removal of a row of data. As lambda of {@link DataGrainOperator}.
	 *
	 * @param conn The connection object of initialized
	 * @param dataGrain The data grain to be processed
	 *
	 * @return input data grain
	 *
	 * @throws SQLException as the defined functional interface
	 */
	public static DataGrain delete(Connection conn, DataGrain dataGrain)
		throws SQLException
	{
		logger.debug("Start default DELETE");

		DataGrain result = ((DataRowOperator)DefaultOperators::doDelete).toDataGrainOperator()
			.operate(conn, dataGrain);

		logger.debug("End default DELETE");
		return result;
	}

	/**
	 * Default operator of removal of all data of a table. As lambda of {@link DataGrainOperator}.<br>
	 *
	 * <p>This operator is wrapped by {@link TableBasedOperator}(not thread-safe).</p>
	 *
	 * @param conn The connection object of initialized
	 * @param dataGrain The data grain to be processed
	 *
	 * @return input data grain
	 *
	 * @throws SQLException as the defined functional interface
	 */
	public static DataGrain deleteAll(Connection conn, DataGrain dataGrain)
		throws SQLException
	{
		logger.debug("Start default DELETE_ALL");

		DataGrainOperator op = new TableBasedOperator(
			(DataRowOperator)DefaultOperators::doDeleteAll
		).toDataGrainOperator();

		DataGrain result = op.operate(conn, dataGrain);

		logger.debug("End default DELETE_ALL");
		return result;
	}

	/**
	 * Default operator of truncating table. As lambda of {@link DataGrainOperator}.
	 *
	 * <p>This operator is wrapped by {@link TableBasedOperator}(not thread-safe).</p>
	 *
	 * @param conn The connection object of initialized
	 * @param dataGrain The data grain to be processed
	 *
	 * @return input data grain
	 *
	 * @throws SQLException as the defined functional interface
	 */
	public static DataGrain truncate(Connection conn, DataGrain dataGrain)
		throws SQLException
	{
		logger.debug("Start default TRUNCATE");

		DataGrainOperator op = new TableBasedOperator(
			(DataRowOperator)DefaultOperators::doTruncate
		).toDataGrainOperator();
		DataGrain result = op.operate(conn, dataGrain);

		logger.debug("End default TRUNCATE");
		return result;
	}

	/**
	 * Default operator of do nothing. As lambda of {@link DataGrainOperator}.
	 *
	 * @param conn The connection object of initialized
	 * @param dataGrain The data grain to be processed
	 *
	 * @return input data grain
	 *
	 * @throws SQLException as the defined functional interface
	 */
	public static DataGrain none(Connection conn, DataGrain dataGrain)
		throws SQLException
	{
		logger.debug("Start default NONE");
		logger.debug("End default NONE");
		return dataGrain;
	}

	/**
	 * Default operator of insertion. As lambda of {@link DataRowOperator}.
	 *
	 * @param conn The connection object of initialized
	 * @param dataRow The data row to be processed
	 *
	 * @return input data row
	 *
	 * @throws SQLException as the defined functional interface
	 */
	public static DataRow doInsert(Connection conn, DataRow dataRow)
		throws SQLException
	{
		SchemaTable table = dataRow.getTable();
		List<String> nameOfColumns = dataRow.getColumns();

		String sql = String.format(
			"INSERT INTO %s(%s) VALUES(%s)",
			table.quoteIdentifier(table.getName()),
			join(
				nameOfColumns,
				columnName -> table.quoteIdentifier(columnName),
				", "
			),
			IntStream.range(0, nameOfColumns.size())
				.mapToObj(i -> "?")
				.collect(Collectors.joining(", "))
		);

		logger.debug("Builds SQL: [{}]", sql);

		DbStatement.buildRunnableForPreparedStatement(
			conn, sql, nameOfColumns, dataRow,
			stat -> stat.executeUpdate()
		).runJdbc();

		return dataRow;
	}

	/**
	 * Default operator of update. As lambda of {@link DataRowOperator}.
	 *
	 * @param conn The connection object of initialized
	 * @param dataRow The data row to be processed
	 *
	 * @return input data row
	 *
	 * @throws SQLException as the defined functional interface
	 */
	public static DataRow doUpdate(
		Connection conn, DataRow dataRow
	) throws SQLException {
		SchemaTable table = dataRow.getTable();
		List<String> nameOfColumns = dataRow.getColumns();
		List<String> keys = table.getKeys();

		/**
		 * Excludes the column in keys to be updated
		 */
		nameOfColumns = nameOfColumns.stream()
			.filter(
				columnName -> !keys.stream().anyMatch(
					key -> key.equals(columnName)
				)
			)
			.collect(Collectors.toList());
		// :~)

		Validate.notEmpty(keys, "Need keys for updating operation");

		String sql = String.format(
			"UPDATE %s SET %s WHERE %s",
			table.quoteIdentifier(table.getName()),
			join(
				nameOfColumns,
				columnName -> String.format(
					"%s = ?",
					table.quoteIdentifier(columnName)
				),
				",\n"
			),
			join(
				keys,
				key -> String.format(
					"%s = ?",
					table.quoteIdentifier(key)
				),
				" AND\n"
			)
		);

		logger.debug("Builds SQL: [{}]", sql);

		List<String> columnsAndKeys = new ArrayList<>(nameOfColumns);
		columnsAndKeys.addAll(keys);

		DbStatement.buildRunnableForPreparedStatement(
			conn, sql, columnsAndKeys, dataRow,
			stat -> stat.executeUpdate()
		).runJdbc();

		return dataRow;
	}

	/**
	 * Default operator of refresh. As lambda of {@link DataRowOperator}.
	 *
	 * @param conn The connection object of initialized
	 * @param dataRow The data row to be processed
	 *
	 * @return input data row
	 *
	 * @throws SQLException as the defined functional interface
	 */
	public static DataRow doRefresh(
		Connection conn, DataRow dataRow
	) throws SQLException {
		SchemaTable table = dataRow.getTable();
		List<String> keys = table.getKeys();

		String queryData = String.format(
			"SELECT COUNT(*) FROM %s WHERE %s",
			table.quoteIdentifier(table.getName()),
			join(
				keys,
				key -> String.format("%s = ?", table.quoteIdentifier(key)),
				" AND\n"
			)
		);

		logger.debug("Builds SQL: [{}]", queryData);

		Boolean dataExisting = DbStatement.buildSupplierForPreparedStatement(
			conn,
			queryData, keys,
			dataRow,
			stat -> JdbcTemplateFactory.buildSupplier(
				() -> stat.executeQuery(),
				rs -> {
					rs.next();
					return rs.getInt(1) > 0;
				}
			).getJdbc()
		).getJdbc();

		if (dataExisting) {
			logger.debug("[Refresh] Do update");
			return doUpdate(conn, dataRow);
		}

		logger.debug("[Refresh] Do insert");
		return doInsert(conn, dataRow);
	}

	/**
	 * Default operator of delete. As lambda of {@link DataRowOperator}.
	 *
	 * @param conn The connection object of initialized
	 * @param dataRow The data row to be processed
	 *
	 * @return input data row
	 *
	 * @throws SQLException as the defined functional interface
	 */
	public static DataRow doDelete(
		Connection conn, DataRow dataRow
	) throws SQLException {
		SchemaTable table = dataRow.getTable();
		List<String> keys = table.getKeys();

		String queryData = String.format(
			"DELETE FROM %s WHERE %s",
			table.quoteIdentifier(table.getName()),
			keys.stream().map(
				key -> String.format("%s = ?", table.quoteIdentifier(key))
			)
			.collect(Collectors.joining(" AND\n"))
		);

		logger.debug("Builds SQL: [{}]", queryData);

		Integer affectedRows = DbStatement.buildSupplierForPreparedStatement(
			conn,
			queryData, keys,
			dataRow,
			stat -> stat.executeUpdate()
		).getJdbc();

		logger.debug("Delete [{}] rows.", affectedRows);

		return dataRow;
	}

	/**
	 * Default operator of delete all. As lambda of {@link DataRowOperator}.
	 *
	 * @param conn The connection object of initialized
	 * @param dataRow The data row to be processed
	 *
	 * @return input data row
	 *
	 * @throws SQLException as the defined functional interface
	 *
	 * @see TableBasedOperator
	 */
	public static DataRow doDeleteAll(
		Connection conn, DataRow dataRow
	) throws SQLException {
		SchemaTable table = dataRow.getTable();

		String queryData = String.format(
			"DELETE FROM %s",
			table.quoteIdentifier(table.getName())
		);

		logger.debug("Builds SQL: [{}]", queryData);

		Integer affectedRows = DbStatement.buildSupplierForPreparedStatement(
			conn,
			queryData, Collections.emptyList(), dataRow,
			stat -> stat.executeUpdate()
		).getJdbc();

		logger.debug("Delete all [{}] rows from table \"{}\".", affectedRows, table.getName());

		return dataRow;
	}

	/**
	 * Default operator of truncate. As lambda of {@link DataRowOperator}.
	 *
	 * @param conn The connection object of initialized
	 * @param dataRow The data row to be processed
	 *
	 * @return input data row
	 *
	 * @throws SQLException as the defined functional interface
	 *
	 * @see TableBasedOperator
	 */
	public static DataRow doTruncate(
		Connection conn, DataRow dataRow
	) throws SQLException {
		SchemaTable table = dataRow.getTable();

		String queryData = String.format(
			"TRUNCATE TABLE %s",
			table.quoteIdentifier(table.getName())
		);

		logger.debug("Builds SQL: [{}]", queryData);
		logger.debug("Truncate table: \"{}\"", table.getName());

		DbStatement.buildRunnableForPreparedStatement(
			conn,
			queryData, Collections.emptyList(), dataRow,
			stat -> stat.executeUpdate()
		).runJdbc();

		return dataRow;
	}

	private static String join(
		List<String> listOfString,
		UnaryOperator<String> mapToString,
		String joinString
	) {
		return listOfString.stream()
			.map(mapToString)
			.collect(Collectors.joining(joinString));
	}
}
