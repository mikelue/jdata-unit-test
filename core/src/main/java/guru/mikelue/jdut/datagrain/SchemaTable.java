package guru.mikelue.jdut.datagrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The schema of table.
 */
public class SchemaTable {
    private String name;
    private List<String> keys = Collections.emptyList();
    private Map<String, SchemaColumn> columns;
    private Map<Integer, SchemaColumn> columnsOfIndexed;

    /**
     * This object is used with {@link Consumer} by {@link SchemaTable#build}.
     */
    public class Builder {
        private Builder() {}

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
				.map(key -> StringUtils.trimToNull(key))
				.filter(key -> key != null)
				.collect(Collectors.toList());

            return this;
        }
    }

    /**
     * Builds a table schema by {@link Consumer}.
     *
     * @param builderConsumer The building code for table schema
     *
     * @return The result schema of table
     *
     * @see #build(Consumer, SchemaTable)
     */
    public static SchemaTable build(Consumer<Builder> builderConsumer)
    {
        SchemaTable tableSchema = new SchemaTable();
        tableSchema.columns = new HashMap<>(CollectionUsage.HASH_SPACE_OF_COLUMNS);
        tableSchema.columnsOfIndexed = new HashMap<>(CollectionUsage.HASH_SPACE_OF_COLUMNS);

        builderConsumer.accept(tableSchema.new Builder());

        return tableSchema.clone();
    }

    /**
     * Clones a table schema and edit it by {@link Consumer}.
     *
     * @param sourceTable The source of table to be edited
     * @param builderConsumer The building code for table schema
     *
     * @return The modified schema of table
     *
     * @see #build(Consumer)
     */
    public static SchemaTable build(Consumer<Builder> builderConsumer, SchemaTable sourceTable)
    {
		Validate.notNull(builderConsumer, "Need consumer for builder");
		Validate.notNull(sourceTable, "Need viable source schema of table");

        SchemaTable tableSchema = sourceTable.modifiableClone();

        builderConsumer.accept(tableSchema.new Builder());

        return tableSchema.clone();
    }

    private SchemaTable() {}

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
	 * Gets keys of table.
	 *
	 * @return The keys of table
	 */
	public List<String> getKeys()
	{
		return keys;
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
        SchemaColumn column = columns.get(columnName);
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
        SchemaColumn column = columnsOfIndexed.get(columnIndex);
        if (column == null) {
            throw new IllegalArgumentException(String.format("Cannot find column by index: \"%d\"", columnIndex));
        }

        return column;
    }

    /**
     * Safe clone for the fields of this object.
     *
     * @see #build(Consumer, SchemaTable)
     */
    @Override
    protected SchemaTable clone()
    {
        SchemaTable clonedObject = new SchemaTable();

        clonedObject.name = this.name;
        clonedObject.keys = Collections.unmodifiableList(this.keys);
        clonedObject.columns = Collections.unmodifiableMap(this.columns);
        clonedObject.columnsOfIndexed = Collections.unmodifiableMap(this.columnsOfIndexed);

        return clonedObject;
    }

    private SchemaTable modifiableClone()
    {
        SchemaTable clonedObject = new SchemaTable();

        clonedObject.name = this.name;
        clonedObject.keys = new ArrayList<>(this.keys);
        clonedObject.columns = new HashMap<>(this.columns);
        clonedObject.columnsOfIndexed = new HashMap<>(this.columnsOfIndexed);

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
			.append(this.columnsOfIndexed, rhs.columnsOfIndexed)
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
			.append(columnsOfIndexed)
		.toHashCode();
	}
}

