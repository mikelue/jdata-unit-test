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
    private final SchemaTable tableSchema;
    private final SchemaColumn column;
	private Supplier<? extends T> dataSupplier;
	private Optional<T> data;

	/**
	 * Utility factory to compose fields for a table.
	 */
	public static class Factory {
		private final SchemaTable tableSchema;

		/**
		 * Constructs with a table schema.
		 *
		 * @param newTableSchema The table schema
		 */
		public Factory(SchemaTable newTableSchema)
		{
			tableSchema = newTableSchema;
			Validate.notNull(tableSchema, "Need viable table schema");
		}

		/**
		 * Clones the data field within this table schema.
		 *
		 * @param <T> The data type of field
		 * @param sourceDataField The data field to be cloned
		 *
		 * @return data field for this table
		 */
		public <T> DataField<T> clone(DataField<T> sourceDataField)
		{
			return new DataField<T>(
				tableSchema,
				sourceDataField.column,
				sourceDataField.dataSupplier, sourceDataField.data
			);
		}

		/**
		 * Composes data field with column's name and data.
		 *
		 * @param <T> The data type of field
		 * @param columnName The name of column
		 * @param data The data of the column
		 *
		 * @return data field for this table
		 *
		 * @see #composeDataSupplier(String, Supplier)
		 * @see #composeDataSupplier(SchemaColumn, Supplier)
		 */
		public <T> DataField<T> composeData(String columnName, T data)
		{
			return composeData(
				SchemaColumn.build(builder -> builder.name(columnName)),
				data
			);
		}
		/**
		 * Composes data field with column's name and data supplier.
		 *
		 * @param <T> The data type of field
		 * @param columnName The name of column
		 * @param dataSupplier The data supplier of the column
		 *
		 * @return data field for this table
		 *
		 * @see #composeData(String, Object)
		 * @see #composeData(SchemaColumn, Object)
		 */
		public <T> DataField<T> composeDataSupplier(String columnName, Supplier<? extends T> dataSupplier)
		{
			return composeDataSupplier(
				SchemaColumn.build(builder -> builder.name(columnName)),
				dataSupplier
			);
		}
		/**
		 * Composes data field with column's name and data supplier.
		 *
		 * @param <T> The data type of field
		 * @param column The column definition
		 * @param data The data of the column
		 *
		 * @return data field for this table
		 *
		 * @see #composeDataSupplier(String, Supplier)
		 * @see #composeDataSupplier(SchemaColumn, Supplier)
		 */
		public <T> DataField<T> composeData(SchemaColumn column, T data)
		{
			return new DataField<T>(
				tableSchema, column, null, Optional.ofNullable(data)
			);
		}
		/**
		 * Composes data field with column's name and data supplier.
		 *
		 * @param <T> The data type of field
		 * @param column The column definition
		 * @param dataSupplier The data supplier of the column
		 *
		 * @return data field for this table
		 *
		 * @see #composeData(String, Object)
		 * @see #composeData(SchemaColumn, Object)
		 */
		public <T> DataField<T> composeDataSupplier(SchemaColumn column, Supplier<? extends T> dataSupplier)
		{
			return new DataField<T>(
				tableSchema, column, dataSupplier, null
			);
		}
	}

	@SuppressWarnings("unchecked")
	private DataField(
		SchemaTable newTableSchema,
		SchemaColumn newColumn, Supplier<? extends T> newDataSupplier, Optional<T> newData
	) {
		tableSchema = newTableSchema;
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

		Validate.notNull(tableSchema, "Need table schema");
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
		return tableSchema;
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
		return tableSchema.getName();
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
			this.tableSchema, this.column,
			this.dataSupplier, this.data
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
