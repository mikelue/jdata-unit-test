package guru.mikelue.jdut.decorate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.datagrain.DataRowException;
import guru.mikelue.jdut.datagrain.SchemaColumn;
import guru.mikelue.jdut.datagrain.SchemaTable;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.JdbcVoidFunction;
import guru.mikelue.jdut.jdbc.JdbcRunnable;
import guru.mikelue.jdut.jdbc.JdbcSupplier;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.SQLExceptionConvert;

/**
 * Loads database schema and validating rows.<br>
 *
 * To improve performance, this object would cache {@link SchemaTable} by its name.
 */
public class TableSchemaLoadingDecorator implements DataGrainDecorator {
	private Logger logger = LoggerFactory.getLogger(TableSchemaLoadingDecorator.class);

	private final DataSource dataSource;
	private Map<String, SchemaTable> cachedTables = new HashMap<>(32);

	public TableSchemaLoadingDecorator(DataSource newDataSource)
	{
		dataSource = newDataSource;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void decorate(DataRow.Builder rowBuilder)
	{
		if (rowBuilder.getValidated()) {
			return;
		}

		SchemaTable table = rowBuilder.getTable();
		String tableIdentifier = table.getIdentifier();
		if (!cachedTables.containsKey(tableIdentifier)) {
			cachedTables.put(tableIdentifier, loadSchema(table));
		}

		rowBuilder.tableSchema(cachedTables.get(tableIdentifier));

		try {
			rowBuilder.validate();
		} catch (DataRowException e) {
			logger.error("Validation of row[{}] has error", rowBuilder.getTable());
			throw new RuntimeException(e);
		}
	}

	private SchemaTable loadSchema(SchemaTable source)
	{
		JdbcSupplier<SchemaTable> funcForLoadingSchema = JdbcTemplateFactory.buildSupplier(
			() -> dataSource.getConnection(),
			conn -> loadSchema(conn, source)
		);

		try {
			return funcForLoadingSchema.get();
		} catch (SQLException e) {
			logger.error("SQL error while loading schema of table: {}", source.toString());
			throw SQLExceptionConvert.runtimeException(e);
		}
	}

	private SchemaTable loadSchema(Connection conn, SchemaTable sourceTable)
		throws SQLException
	{
		DatabaseMetaData metaData = conn.getMetaData();

		return SchemaTable.build(tableBuilder -> {
			tableBuilder.metaData(metaData);
			tableBuilder.name(sourceTable.getName());

			JdbcRunnable loadColumns = JdbcTemplateFactory.buildRunnable(
				() -> metaData.getColumns(
					sourceTable.getCatalog().orElse(null),
					sourceTable.getSchema().orElse(null),
					tableBuilder.getName(),
					null
				),
				rsColumns -> {
					/**
					 * Builds information of columns
					 */
					while (rsColumns.next()) {
						JdbcVoidFunction<SchemaColumn.Builder> jdbcFunction = columnBuilder -> {
							columnBuilder
								.name(rsColumns.getString("COLUMN_NAME"))
								.jdbcType(JDBCType.valueOf(rsColumns.getInt("DATA_TYPE")))
								.defaultValue(rsColumns.getString("COLUMN_DEF"));

							switch (rsColumns.getInt("NULLABLE")) {
								case DatabaseMetaData.columnNullable:
									columnBuilder.nullable(true);
									break;
								case DatabaseMetaData.columnNoNulls:
									columnBuilder.nullable(false);
									break;
							}
						};

						tableBuilder.column(
							SchemaColumn.build(jdbcFunction.asConsumer())
						);
					}
					// :~)
				}
			);

			loadColumns.asRunnable().run();
		});
	}
}
