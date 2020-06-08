package guru.mikelue.jdut.datagrain;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import guru.mikelue.jdut.jdbc.util.MetaDataWorker;

/**
 * The schema of table.<br>
 *
 * <h3>Building of data</h3>
 * While you are building data for testing, this object defines necessary to declare the data of a table.<br>
 * The name of table and keys(for deletion) is used by operators of data grain.
 *
 * <h3>Validation of data</h3>
 * After the schema information is fetched from real database, this object contains the real schema defined in database.
 * And, the auto-inspected keys if there is no one while building data.
 */
public class SchemaTable {
	private final static String NULL_NAMING = "<null>";

	private Optional<String> catalog = Optional.empty();
	private Optional<String> schema = Optional.empty();

    private String name;
	private String quotedTableName;

	private MetaDataWorker metaDataWorker;

    private List<String> keys = Collections.emptyList();
    private Map<String, SchemaColumn> columns;
    private Map<String, Integer> nameToIndex;
    private Map<Integer, String> indexToName;

    /**
     * This object is used with {@link Consumer} by {@link SchemaTable#build}.
     */
    public class Builder {
        private Builder() {}

		/**
		 * Sets meta-data work.
		 *
		 * @param newWorker The worker of meta data
		 *
		 * @return cascading self
		 */
		public Builder metaDataWorker(MetaDataWorker newWorker)
		{
			metaDataWorker = newWorker;
			return this;
		}

		/**
		 * Sets the catalog.
		 *
		 * @param newCatalog The value of catalog
		 *
		 * @return cascading self
		 *
		 * @see DatabaseMetaData#getColumns
		 */
		public Builder catalog(String newCatalog)
		{
			catalog = Optional.ofNullable(StringUtils.trimToNull(newCatalog))
				.map(SchemaTable.this::treatIdentifier);
			return this;
		}

		/**
		 * Sets the schema.
		 *
		 * @param newSchema The value of schema
		 *
		 * @return cascading self
		 *
		 * @see DatabaseMetaData#getColumns
		 */
		public Builder schema(String newSchema)
		{
			schema = Optional.ofNullable(StringUtils.trimToNull(newSchema))
				.map(SchemaTable.this::treatIdentifier);

			return this;
		}

        /**
         * Sets the name of table.
         *
         * @param newName The name of table
         *
         * @return cascading self
         */
        public Builder name(String newName)
        {
			name = StringUtils.trimToNull(newName);
			Validate.notNull(name, "Need table name");

			name = SchemaTable.this.treatIdentifier(name);

            return this;
        }

        /**
         * Sets the keys of table.
         *
         * @param newKeys The keys of table
         *
         * @return cascading self
         */
        public Builder keys(String... newKeys)
        {
			keys = Stream.of(newKeys)
				.map(key -> {
					String processedKey = StringUtils.trimToNull(key);
					if (processedKey == null) {
						return null;
					}

					return SchemaTable.this.treatIdentifier(processedKey);
				})
				.filter(key -> key != null)
				.collect(Collectors.toList());

            return this;
        }

		/**
		 * Adds builder for a column.
		 *
		 * @param column The added column
		 *
		 * @return cascading self
		 */
		public Builder column(SchemaColumn column)
		{
			String columnName = SchemaTable.this.treatIdentifier(column.getName());

			if (!nameToIndex.containsKey(columnName)) {
				nameToIndex.put(columnName, nameToIndex.size());
				indexToName.put(nameToIndex.get(columnName), columnName);
			}

			columns.put(columnName, column);

			return this;
		}

		private void initQuotedName()
		{
			if (metaDataWorker == null) {
				quotedTableName = name;
				return;
			}

			if (metaDataWorker.supportsSchemasInDataManipulation()) {
				quotedTableName = schema
					.map(s -> metaDataWorker.quoteIdentifier(s) + ".")
					.orElse("") +
					metaDataWorker.quoteIdentifier(name);
			} else {
				quotedTableName = metaDataWorker.quoteIdentifier(name);
			}
		}
    }

    /**
     * Builds a table schema by {@link Consumer}.
     *
     * @param builderConsumer The building code for table schema
     *
     * @return The result schema of table
     */
    public static SchemaTable build(Consumer<Builder> builderConsumer)
    {
        SchemaTable tableSchema = new SchemaTable();
		SchemaTable.Builder tableBuilder = tableSchema.new Builder();
		tableSchema.columns = new HashMap<>(CollectionUsage.HASH_SPACE_OF_COLUMNS);
		tableSchema.indexToName = new HashMap<>(CollectionUsage.HASH_SPACE_OF_COLUMNS);
		tableSchema.nameToIndex = new HashMap<>(CollectionUsage.HASH_SPACE_OF_COLUMNS);

        builderConsumer.accept(tableBuilder);
		tableBuilder.initQuotedName();

		return tableSchema.clone();
    }

    private SchemaTable() {}

	/**
	 * Gets the worker of meta-data.
	 */
	public MetaDataWorker getMetaDataWorker()
	{
		return metaDataWorker;
	}

    /**
     * Gets name of table.
     *
     * @return The name of table
     */
    public String getName()
    {
        return name;
    }

	/**
	 * Gets quoted string of full name(may be include schema) of table.
	 *
	 * If the {@link Builder#metaDataWorker(MetaDataWorker)} is not set,
	 * the value returned by this method would be as same as {@link #getName}.
	 *
	 * @return The quoted name of table
	 */
	public String getQuotedFullName()
	{
		return quotedTableName;
	}

	/**
	 * Gets optional value of schema.
	 *
	 * @return the schema
	 */
	public Optional<String> getSchema()
	{
		return schema;
	}

	/**
	 * Gets optional value of schema.
	 *
	 * @return the schema
	 */
	public Optional<String> getCatalog()
	{
		return catalog;
	}

	/**
	 * Gets keys of table.
	 *
	 * @return The keys of table
	 */
	public List<String> getKeys()
	{
		return keys;
	}

	/**
	 * Gets number of columns.
	 *
	 * @return The number of columns
	 */
	public int getNumberOfColumns()
	{
		return columns.size();
	}

	/**
	 * Gets the columns(sorted by added sequence).
	 *
	 * @return The columns
	 */
	public List<SchemaColumn> getColumns()
	{
		List<SchemaColumn> result = new ArrayList<>(getNumberOfColumns());
		IntStream.range(0, getNumberOfColumns())
			.forEach(
				i -> result.add(getColumn(i))
			);

		return result;
	}

	/**
	 * Whether or not has a column by name.
	 *
	 * @param columnName The name of column
	 *
	 * @return true if the column is existing
	 */
	public boolean hasColumn(String columnName)
	{
		return columns.containsKey(treatIdentifier(columnName));
	}

    /**
     * Gets the column definition by name.
     *
     * @param columnName The name of column
     *
     * @return The column definition
     *
     * @throws IllegalArgumentException If the name of column cannot be found for definition
     */
    public SchemaColumn getColumn(String columnName)
    {
        SchemaColumn column = columns.get(treatIdentifier(columnName));
        if (column == null) {
            throw new IllegalArgumentException(String.format("Cannot find column: \"%s\"", columnName));
        }

        return column;
    }

    /**
     * Gets the column definition by column index.
     *
     * @param columnIndex The index of column
     *
     * @return The column definition
     *
     * @throws IllegalArgumentException If the name of column cannot be found for definition
     */
    public SchemaColumn getColumn(int columnIndex)
    {
        String columnName = indexToName.get(columnIndex);
        if (columnName == null) {
            throw new IllegalArgumentException(String.format("Cannot find column by index: \"%d\"", columnIndex));
        }

        return getColumn(columnName);
    }

	/**
	 * Converts the case of <em>identifier</em> by meta-data of database.
	 *
	 * @param identifier The identifier to be processed
	 *
	 * @return The processed identifier
	 */
	public String treatIdentifier(String identifier)
	{
		identifier = StringUtils.trimToEmpty(identifier);
		if (StringUtils.isEmpty(identifier)) {
			return identifier;
		}

		if (metaDataWorker == null) {
			return identifier.toLowerCase();
		}

		return metaDataWorker.processIdentifier(identifier);
	}

	/**
	 * Use {@link DatabaseMetaData#getIdentifierQuoteString} to quote <em>identifier</em>.
	 *
	 * @param identifier The identifier to be quoted
	 *
	 * @return The quoted identifier
	 */
	public String quoteIdentifier(String identifier)
	{
		if (metaDataWorker == null) {
			return identifier;
		}

		return metaDataWorker.quoteIdentifier(identifier);
	}

	/**
	 * Gets name of table with catalog and schema and keys.<br>
	 *
	 * Use <em>{@code "<null>"}</em> if the value is not set
	 *
	 * @return The string can be used in collection
	 */
	public String getFullTableName()
	{
		return String.format(
			"%s.%s.%s|%s",
			catalog.orElse(NULL_NAMING), schema.orElse(NULL_NAMING),
			name,
			keys.stream().collect(
				Collectors.joining(",")
			)
		);
	}

    /**
     * Safe clone for the fields of this object.
     */
    @Override
    protected SchemaTable clone()
    {
        SchemaTable clonedObject = safeClone();

        clonedObject.keys = Collections.unmodifiableList(this.keys);
        clonedObject.columns = Collections.unmodifiableMap(this.columns);
        clonedObject.nameToIndex = Collections.unmodifiableMap(this.nameToIndex);
        clonedObject.indexToName = Collections.unmodifiableMap(this.indexToName);

        return clonedObject;
    }

	private SchemaTable safeClone()
	{
        SchemaTable clonedObject = new SchemaTable();
		clonedObject.quotedTableName = this.quotedTableName;
        clonedObject.name = this.name;
		clonedObject.schema = this.schema;
		clonedObject.catalog = this.catalog;
		clonedObject.metaDataWorker = this.metaDataWorker;
		return clonedObject;
	}

	/**
	 * Compares the name of table, columns, keys, and indexed columns.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}

		SchemaTable rhs = (SchemaTable)obj;
		return new EqualsBuilder()
			.append(this.name, rhs.name)
			.append(this.columns, rhs.columns)
			.append(this.keys, rhs.keys)
			.isEquals();
	}

	/**
	 * Hashes the name of table, columns, keys, and indexed columns.
	 */
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(494534511, 401552629)
			.append(name)
			.append(keys)
			.append(columns)
		.toHashCode();
	}

	@Override
	public String toString()
	{
		return String.format(
			"Table: %s.%s.\"%s\". Columns [%s]",
			catalog.orElse(NULL_NAMING),
			schema.orElse(NULL_NAMING),
			name,
			columns.values().stream()
				.map(column -> column.getName())
				.collect(Collectors.joining(",", "\"", "\""))
		);
	}
}

