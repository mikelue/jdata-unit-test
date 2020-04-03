package guru.mikelue.jdut.datagrain;

import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.util.Optional;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * The definition of column of a table.
 */
public class SchemaColumn {
    private String name;
    private Optional<JDBCType> jdbcType = Optional.empty();
	private Optional<Boolean> nullable = Optional.empty();
	private Optional<String> defaultValue = Optional.empty();
	private Optional<Boolean> autoIncremental = Optional.empty();

    /**
     * This object is used with {@link Consumer} by {@link SchemaColumn#build}.
     */
    public class Builder {
		private Builder() {}

        /**
         * Sets the name of column.
         *
         * @param newName The name of table
         *
         * @return cascading self
         */
        public Builder name(String newName)
        {
			newName = StringUtils.trimToNull(newName);

            name = newName;
            return this;
        }
        /**
         * Sets the sql type of column.
         *
         * @param newJdbcType The value(nullable) of sql type, see {@link java.sql.Types}.
         *
         * @return cascading self
         *
         * @see java.sql.Types
         */
        public Builder jdbcType(JDBCType newJdbcType)
        {
            jdbcType = Optional.ofNullable(newJdbcType);
            return this;
        }

		/**
		 * Sets whether or not this column is nullable
		 *
		 * @param newNullable The value of nullable
		 *
		 * @return cascading self
		 */
		public Builder nullable(boolean newNullable)
		{
			nullable = Optional.of(newNullable);
			return this;
		}

		/**
		 * Sets whether or not this columns is auto-incremental.
		 *
		 * @param newAutoIncremental The value of auto-incremental
		 *
		 * @return cascading self
		 */
		public Builder autoIncremental(Boolean newAutoIncremental)
		{
			autoIncremental = Optional.ofNullable(newAutoIncremental);
			return this;
		}

		/**
		 * Sets the default value of column.
		 *
		 * @param newDefaultValue the flag of having default value
		 *
		 * @return cascading self
		 */
		public Builder defaultValue(String newDefaultValue)
		{
			defaultValue = Optional.ofNullable(newDefaultValue);
			return this;
		}

		private void validate()
		{
			Validate.notNull(name, "Need viable name of column");
		}
    }

    private SchemaColumn() {}

    /**
     * Builds a new column.
     *
     * @param builderConsumer The editor for column
     *
     * @return The new definition of column
     *
     * @see #build(Consumer, SchemaColumn)
     */
    public static SchemaColumn build(Consumer<Builder> builderConsumer)
    {
        SchemaColumn newColumn = new SchemaColumn();
		Builder newBuilder = newColumn.new Builder();

        builderConsumer.accept(newBuilder);
		newBuilder.validate();

        return newColumn.clone();
    }

    /**
     * Gets name of column
     *
     * @return The name of column
     */
    public String getName()
    {
        return name;
    }

    /**
     * Gets the value(optional) of sql type.
     *
     * @return The value of sql type
	 *
	 * @see DatabaseMetaData#getColumns getColumns.getInt("DATA_TYPE")
     */
    public Optional<JDBCType> getJdbcType()
    {
        return jdbcType;
    }

	/**
	 * Whether or not this column is nullable.
	 *
	 * @return may not be initialized
	 *
	 * @see DatabaseMetaData#getColumns getColumns.getInt("NULLABLE")
	 */
	public Optional<Boolean> getNullable()
	{
		return nullable;
	}

	/**
	 * Gets value of default data.
	 *
	 * @return The optional default value
	 *
	 * @see DatabaseMetaData#getColumns getColumns.getString("COLUMN_DEF")
	 */
	public Optional<String> getDefaultValue()
	{
		return defaultValue;
	}

	/**
	 * Checks whether or not this column is auto-incremental.
	 *
	 * @return The flag may be unknown
	 */
	public Optional<Boolean> getAutoIncremental()
	{
		return autoIncremental;
	}

	/**
	 * Whether or not this column has default value.
	 *
	 * @return may not be initialized
	 *
	 * @see DatabaseMetaData#getColumns getColumns.getString("COLUMN_DEF")
	 */
	public boolean getHasDefaultValue()
	{
		return defaultValue.isPresent();
	}

    @Override
    protected SchemaColumn clone()
    {
        SchemaColumn clonedObject = new SchemaColumn();
        clonedObject.name = this.name;
        clonedObject.jdbcType = this.jdbcType;
		clonedObject.nullable = this.nullable;
		clonedObject.defaultValue = this.defaultValue;
		clonedObject.autoIncremental = this.autoIncremental;

        return clonedObject;
    }

	/**
	 * Hashed code by name of column.
	 */
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(341242957, 596024461)
			.append(name)
		.toHashCode();
	}

	/**
	 * Only compares the name of column.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}

		SchemaColumn rhs = (SchemaColumn)obj;
		return new EqualsBuilder()
			.append(this.name, rhs.name)
			.isEquals();
	}
}
