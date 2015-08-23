package guru.mikelue.jdut.datagrain;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Container with data of a field.<br>
 *
 * This class would call the supplier only once, and keep the value.
 *
 * @param <T> The type of data
 */
public class DataField<T> {
    private final SchemaColumn column;
	private Supplier<? extends T> dataSupplier;
	private Optional<T> data;

	/**
	 * Clone constructor for different column.
	 *
	 * @param newColumn The new table schema
	 * @param existingDataField existing data field
	 */
	public DataField(SchemaColumn newColumn, DataField<T> existingDataField)
	{
		column = newColumn;
		dataSupplier = existingDataField.dataSupplier;
		data = existingDataField.data;
	}

    /**
     * Constructs a field with column name and data supplier.
     *
     * @param tableSchema The schema of table
     * @param columnName The column definition
     * @param newDataSupplier The data supplier
     */
	public DataField(SchemaTable tableSchema, String columnName, Supplier<? extends T> newDataSupplier)
    {
		this(SchemaColumn.build(builder -> builder.tableSchema(tableSchema).name(columnName)), newDataSupplier, null);
		Validate.notNull(dataSupplier, "Need supplier of data");
    }

    /**
     * Constructs a field with column name and value.
     *
     * @param tableSchema The schema of table
     * @param columnName The column definition
     * @param newData The value of data
     */
	public DataField(SchemaTable tableSchema, String columnName, T newData)
    {
		this(SchemaColumn.build(builder -> builder.tableSchema(tableSchema).name(columnName)), null, Optional.ofNullable(newData));
    }

    /**
     * Constructs a field with column definition and data supplier.
     *
     * @param newColumn The column definition
     * @param newDataSupplier The data supplier
     */
	public DataField(SchemaColumn newColumn, Supplier<? extends T> newDataSupplier)
    {
		this(newColumn, newDataSupplier, null);
		Validate.notNull(dataSupplier, "Need supplier of data");
    }

    /**
     * Constructs a field with column definition and value.
     *
     * @param newColumn The column definition
     * @param newData The value of data
     */
	public DataField(SchemaColumn newColumn, T newData)
    {
		this(newColumn, null, Optional.ofNullable(newData));
    }

	@SuppressWarnings("unchecked")
	private DataField(SchemaColumn newColumn, Supplier<? extends T> newDataSupplier, Optional<T> newData)
    {
    	column = newColumn;
		dataSupplier = newDataSupplier;

		/**
		 * Even if the new data is an object, we still check the type for lambda expression
		 */
		if (newData != null && newData.isPresent() && Supplier.class.isInstance(newData.get())) {
			dataSupplier = (Supplier<? extends T>)newData.get();
			data = null;
		} else {
			data = newData;
		}
		// :~)

		Validate.notNull(column, "Need column definition");
    }

	/**
	 * Gets definition of table.
	 *
	 * @return The definition of table
	 *
	 * @see #getTableName
	 */
	public SchemaTable getTable()
	{
		return column.getTable();
	}

	/**
	 * Gets name of table.
	 *
	 * @return the name of table
	 *
	 * @see #getTable
	 */
	public String getTableName()
	{
		return column.getTableName();
	}

    /**
     * Gets definition of column.
     *
     * @return The definition of column
	 *
	 * @see #getColumnName
     */
    public SchemaColumn getColumn()
    {
		return column;
    }

	/**
	 * Gets name of column.
	 *
	 * @return The name of column
	 *
	 * @see #getColumn
	 */
	public String getColumnName()
	{
		return column.getName();
	}

    /**
     * Gets data or gets by dats {@link Supplier}.
	 *
 	 * This method would call the supplier only once, and keep the value.
     *
     * @return The data this field is holding
     */
    public T getData()
    {
		if (data == null) {
			data = Optional.ofNullable(dataSupplier.get());
		}

		return data.orElse(null);
    }

	/**
	 * Gets data supplier of this field.
	 *
	 * @return The data supplier, may be empty
	 */
	public Optional<Supplier<? extends T>> getDataSupplier()
	{
		return Optional.ofNullable(dataSupplier);
	}

	/**
	 * Turns this field to map entry.
	 *
	 * @return The entry for {@link Map}
	 */
	public Map.Entry<String, DataField<T>> asMapEntry()
	{
		return new ImmutablePair<>(getColumnName(), this);
	}

    @Override
    protected DataField<T> clone()
    {
        return new DataField<>(
            this.column, this.dataSupplier
        );
    }

	/**
	 * Compares the column definition and data.
	 */
	@Override @SuppressWarnings("unchecked")
	public boolean equals(Object obj)
	{
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}

		DataField<T> rhs = (DataField<T>)obj;
		return new EqualsBuilder()
			.appendSuper(super.equals(obj))
			.append(this.column, rhs.column)
			.append(this.getData(), rhs.getData())
			.isEquals();
	}

	/**
	 * Hashes the column definition and data.
	 */
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(598521649, 877176199)
			.append(column)
			.append(getData())
		.toHashCode();
	}
}
