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
		String tableIdentifier = table.getFullTableName();
		logger.debug("Current table: [{}]", tableIdentifier);

		if (!cachedTables.containsKey(tableIdentifier)) {
			SchemaTable newTableSchema = loadSchema(table);

			cachedTables.put(tableIdentifier, newTableSchema);
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

		logger.debug("Loading schema of table: \"{}\"", sourceTable.getName());

		/**
		 * In order to respect the case-sensitive of identifiers,
		 * this new table doesn't clone from old table schema
		 */
		return SchemaTable.build(tableBuilder -> {
			tableBuilder.metaData(metaData);
			tableBuilder.name(sourceTable.getName());
			tableBuilder.keys(sourceTable.getKeys().toArray(new String[0]));

			loadColumns(tableBuilder, sourceTable, metaData);

			/**
			 * Loads keys if there is no set one
			 */
			if (sourceTable.getKeys().isEmpty()) {
				logger.debug("Fetch keys for table: \"{}\"", sourceTable.getName());
				loadKeys(tableBuilder, metaData, sourceTable);
			}
			// :~)
		});
		// :~)
	}

	private void loadColumns(
		SchemaTable.Builder tableBuilder, SchemaTable sourceTable,
		DatabaseMetaData metaData
	) {
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
					String columnName = rsColumns.getString("COLUMN_NAME");
					JDBCType jdbcType = JDBCType.valueOf(rsColumns.getInt("DATA_TYPE"));

					logger.debug("Loading schema of columns: \"{}\". Type: [{}]",
						columnName, jdbcType
					);

					JdbcVoidFunction<SchemaColumn.Builder> jdbcFunction = columnBuilder -> {
						columnBuilder
							.name(columnName)
							.jdbcType(jdbcType)
							.defaultValue(rsColumns.getString("COLUMN_DEF"));

						try {
							String autoIncremental = rsColumns.getString("IS_AUTOINCREMENT");
							if (autoIncremental != null) {
								columnBuilder.autoIncremental("YES".equals(autoIncremental) ? true : false);
							}
						} catch (SQLException e) {
							logger.info("This database doesn't have \"IS_AUTOINCREMENT\" meta data of JDBC");
						}

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
	}

	private void loadKeys(
		SchemaTable.Builder tableBuilder, DatabaseMetaData metaData,
		SchemaTable sourceTable
	) {
		List<String> keys = null;

		/**
		 * Loads keys by DatabaseMetaData#getPrimaryKeys
		 */
		JdbcSupplier<List<String>> loadKeysByPk = JdbcTemplateFactory.buildSupplier(
			() -> metaData.getPrimaryKeys(
				sourceTable.getCatalog().orElse(null),
				sourceTable.getSchema().orElse(null),
				tableBuilder.getName()
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
			tableBuilder.keys(keys.toArray(new String[0]));
			return;
		}
		// :~)

		tableBuilder.keys(
			fetchBestKeys(
				tableBuilder,
				() -> metaData.getIndexInfo(
					sourceTable.getCatalog().orElse(null),
					sourceTable.getSchema().orElse(null),
					tableBuilder.getName(),
					true, true
				)
			).toArray(new String[0])
		);
	}

	private List<String> fetchBestKeys(
		SchemaTable.Builder tableBuilder,
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
					SchemaColumn schemaColumn = tableBuilder.getColumn(columnName);

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
