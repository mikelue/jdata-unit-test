package guru.mikelue.jdut.decorate;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.datagrain.DataRowException;
import guru.mikelue.jdut.datagrain.SchemaColumn;
import guru.mikelue.jdut.datagrain.SchemaTable;
import guru.mikelue.jdut.jdbc.JdbcSupplier;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.JdbcVoidFunction;
import guru.mikelue.jdut.jdbc.SQLExceptionConvert;
import guru.mikelue.jdut.jdbc.util.MetaDataWorker;

/**
 * Loads database schema and validating rows.<br>
 *
 * To improve performance, this object would cache {@link SchemaTable} by its name,
 * but this class is thread-safe for the caching mechanism.
 */
public class TableSchemaLoadingDecorator implements DataGrainDecorator {
	private final static Logger logger = LoggerFactory.getLogger(TableSchemaLoadingDecorator.class);

	private final DataSource dataSource;
	private Map<String, SchemaTable> cachedTables = new ConcurrentHashMap<>(32);

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

		/**
		 * Loads scehma from cache or JDBC meta-data
		 */
		SchemaTable table = rowBuilder.getTable();
		String tableIdentifier = table.getFullTableName();
		logger.debug("Decorate table for loading schema: [{}]", tableIdentifier);

		if (!cachedTables.containsKey(tableIdentifier)) {
			logger.debug("First time of loading schema");
			SchemaTable newTableSchema = loadSchema(table);

			cachedTables.put(tableIdentifier, newTableSchema);
		}
		// :~)

		rowBuilder.tableSchema(cachedTables.get(tableIdentifier));

		/**
		 * Validates the row data if it follow the definitions of schema.
		 */
		try {
			rowBuilder.validate();
		} catch (DataRowException e) {
			logger.error("Validation of row[{}] has error", rowBuilder.getTable());
			throw new RuntimeException(e);
		}
		// :~)
	}

	private SchemaTable loadSchema(SchemaTable source)
	{
		JdbcSupplier<SchemaTable> funcForLoadingSchema = JdbcTemplateFactory.buildSupplier(
			() -> dataSource.getConnection(),
			conn -> loadSchema(conn, source)
		);

		try {
			return funcForLoadingSchema.getJdbc();
		} catch (SQLException e) {
			logger.error("SQL error while loading schema of table: {}", source.toString());
			throw SQLExceptionConvert.runtimeException(e);
		}
	}

	private SchemaTable loadSchema(Connection conn, SchemaTable sourceTable)
		throws SQLException
	{
		DatabaseMetaData metaData = conn.getMetaData();
		MetaDataWorker metaDataWorker = new MetaDataWorker(metaData);
		SchemaAndTableName cananicalName = processSchemaAndTableName(metaDataWorker, sourceTable);

		logger.debug("Load schema for: {}", cananicalName);

		/**
		 * In order to respect the case-sensitive of identifiers,
		 * this new table doesn't clone from old table schema
		 */
		return SchemaTable.build(tableBuilder -> {
			tableBuilder.metaDataWorker(metaDataWorker);
			tableBuilder.schema(cananicalName.schema);
			tableBuilder.name(cananicalName.table);
			tableBuilder.keys(sourceTable.getKeys().toArray(new String[0]));

			Map<String, SchemaColumn> loadedColumns = loadColumns(cananicalName, metaData);
			for (SchemaColumn column: loadedColumns.values()) {
				tableBuilder.column(column);
			}

			/**
			 * Loads keys if there is no set one
			 */
			if (sourceTable.getKeys().isEmpty()) {
				logger.debug("Fetch keys for table: \"{}\"", sourceTable.getName());

				String[] keys = loadKeys(loadedColumns, metaData, cananicalName);
				tableBuilder.keys(keys);
			}
			// :~)
		});
		// :~)
	}

	private SchemaAndTableName processSchemaAndTableName(MetaDataWorker metaDataWorker, SchemaTable sourceTable)
		throws SQLException
	{
		String catalog = sourceTable.getCatalog()
			.map(metaDataWorker::processIdentifier)
			.orElse(null);
		String sourceTableName = metaDataWorker.processIdentifier(
			sourceTable.getName()
		);

		if (
			!metaDataWorker.supportsSchemasInTableDefinitions() ||
			!sourceTableName.contains(".") ||
			sourceTable.getSchema().isPresent()
		) {
			return new SchemaAndTableName(
				catalog,
				metaDataWorker.processIdentifier(sourceTable.getSchema().orElse(null)),
				sourceTableName
			);
		}

		String[] schemaAndTableName = sourceTableName.split("\\.");
		if (schemaAndTableName.length > 2) {
			throw new RuntimeException(
				String.format("Cannot recgonize schema and table name: \"%s\"", sourceTableName)
			);
		}

		return new SchemaAndTableName(
			catalog,
			metaDataWorker.processIdentifier(schemaAndTableName[0]),
			metaDataWorker.processIdentifier(schemaAndTableName[1])
		);
	}

	private Map<String, SchemaColumn> loadColumns(
		SchemaAndTableName cananicalName,
		DatabaseMetaData metaData
	) {
		logger.debug("Load columns for: {}", cananicalName);

		JdbcSupplier<Map<String, SchemaColumn>> jdbcGetColumns = JdbcTemplateFactory.buildSupplier(
			() -> metaData.getColumns(
				cananicalName.catalog,
				cananicalName.schema, cananicalName.table,
				null
			),
			/**
			 * Builds information of columns
			 */
			(ResultSet rsColumns) -> {
				Map<String, SchemaColumn> columns = new HashMap<>();

				while (rsColumns.next()) {
					String columnName = rsColumns.getString("COLUMN_NAME");
					JDBCType jdbcType = JDBCType.valueOf(rsColumns.getInt("DATA_TYPE"));

					logger.debug("Loading meta-data of columns: \"{}\". Type: [{}]",
						columnName, jdbcType
					);

					JdbcVoidFunction<SchemaColumn.Builder> columnBuilder = builder -> {
						builder
							.name(columnName)
							.jdbcType(jdbcType)
							.defaultValue(rsColumns.getString("COLUMN_DEF"));

						try {
							String autoIncremental = rsColumns.getString("IS_AUTOINCREMENT");
							if (autoIncremental != null) {
								builder.autoIncremental("YES".equals(autoIncremental) ? true : false);
							}
						} catch (SQLException e) {
							logger.info("This database doesn't have \"IS_AUTOINCREMENT\" meta data of JDBC");
						}

						switch (rsColumns.getInt("NULLABLE")) {
							case DatabaseMetaData.columnNullable:
								builder.nullable(true);
								break;
							case DatabaseMetaData.columnNoNulls:
								builder.nullable(false);
								break;
						}
					};

					SchemaColumn loadedColumn =
						SchemaColumn.build(columnBuilder.asConsumer());
					columns.put(loadedColumn.getName(), loadedColumn);
				}
				// :~)

				return columns;
			}
		);

		return jdbcGetColumns.asSupplier().get();
	}

	private String[] loadKeys(
		Map<String, SchemaColumn> columnsInfo,
		DatabaseMetaData metaData,
		SchemaAndTableName cananicalName
	) {
		logger.debug("Load keys for: {}", cananicalName);

		List<String> keys = null;

		/**
		 * Loads keys by DatabaseMetaData#getPrimaryKeys
		 */
		JdbcSupplier<List<String>> loadKeysByPk = JdbcTemplateFactory.buildSupplier(
			() -> metaData.getPrimaryKeys(
				cananicalName.catalog,
				cananicalName.schema,
				cananicalName.table
			),
			rs -> {
				List<String> loadedKeys = new ArrayList<>(4);

				while (rs.next()) {
					String pkName = rs.getString("COLUMN_NAME");

					logger.debug("Fetch key by primary key: \"{}\"", pkName);
					loadedKeys.add(pkName);
				}

				return loadedKeys;
			}
		);
		keys = loadKeysByPk.asSupplier().get();
		if (!keys.isEmpty()) {
			return keys.toArray(new String[0]);
		}
		// :~)

		logger.debug("Deduce keys for: {}", cananicalName);
		String[] deducedKeys = deduceKeys(
			columnsInfo,
			() -> metaData.getIndexInfo(
				cananicalName.catalog,
				cananicalName.schema,
				cananicalName.table,
				true, true
			)
		).toArray(new String[0]);

		return deducedKeys;
	}

	private List<String> deduceKeys(
		Map<String, SchemaColumn> columnsInfo,
		JdbcSupplier<ResultSet> rsUniqueIndexSupplier
	) {
		JdbcSupplier<List<String>> supplier = JdbcTemplateFactory.buildSupplier(
			rsUniqueIndexSupplier,
			rs -> {
				Map<String, List<String>> indexes = new HashMap<>(4);
				Map<String, Integer> numberOfNullableColumns = new HashMap<>(4);

				while (rs.next()) {
					String columnName = rs.getString("COLUMN_NAME");
					if (columnName == null) {
						continue;
					}

					String indexName = rs.getString("INDEX_NAME");
					SchemaColumn schemaColumn = columnsInfo.get(columnName);

					logger.debug(
						"Collected information of unique index: \"{}\" and column: \"{}\". Nullable: [{}]",
						indexName, columnName, schemaColumn.getNullable().get()
					);

					/**
					 * Puts index information into map
					 */
					if (!indexes.containsKey(indexName)) {
						indexes.put(indexName, new ArrayList<>(4));
						numberOfNullableColumns.put(indexName, 0);
					}

					indexes.get(indexName).add(columnName);
					numberOfNullableColumns.put(
						indexName,
						numberOfNullableColumns.get(indexName) +
						( schemaColumn.getNullable().get() ?  1 : 0 )
					);
					// :~)
				}

				/**
				 * Fetch the least columns with non-null value
				 */
				Optional<Map.Entry<String, Integer>> notNullIndexWithLeastColumns = numberOfNullableColumns.entrySet().stream()
					.filter(
						// Without nullable column
						indexEntry -> indexEntry.getValue() == 0
					)
					.min((entryLeft, entryRight) ->
						Integer.compare(
							indexes.get(entryLeft.getKey()).size(),
							indexes.get(entryRight.getKey()).size()
						)
					);

				if (notNullIndexWithLeastColumns.isPresent()) {
					List<String> nonNullKeys = indexes.get(
						notNullIndexWithLeastColumns.get().getKey()
					);
					logger.debug("Got non null keys: {}", nonNullKeys);
					return nonNullKeys;
				}
				// :~)

				/**
				 * Fetch the least nullable value columns of index
				 */
				Optional<Map.Entry<String, Integer>> nullableIndexWithLeastColumns = numberOfNullableColumns.entrySet().stream()
					.min((entryLeft, entryRight) ->
						entryLeft.getValue().compareTo(
							entryRight.getValue()
						)
					);

				if (nullableIndexWithLeastColumns.isPresent()) {
					List<String> nullableKeys = indexes.get(
						nullableIndexWithLeastColumns.get().getKey()
					);
					logger.debug("Got nullable keys: {}", nullableKeys);
					return nullableKeys;
				}
				// :~)

				return Collections.<String>emptyList();
			}
		);

		return supplier.asSupplier().get();
	}
}

class SchemaAndTableName {
	String catalog;
	String schema;
	String table;

	SchemaAndTableName(String newCatalog, String newSchema, String newTable)
	{
		catalog = newCatalog;
		schema = newSchema;
		table = newTable;
	}

	@Override
	public String toString()
	{
		return String.format(
			"Cananical name(<catalog>.<schema>.<table>): [%s.%s.%s]",
			catalog == null ? "<null>" : catalog,
			schema == null ? "<null>" : schema,
			table
		);
	}
}
