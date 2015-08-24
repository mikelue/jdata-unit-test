package guru.mikelue.jdut.datagrain;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Represents the data of a row.
 *
 * <h2 style="color:blue">Value using {@link Supplier}</h2>
 * In following code snippet, the builder is <em style="color:red">a failure of infinite recursion</em>:
 * <pre>{@code
 *     builder -> builder
 *        .fieldOfValueSupplier("ct_1", () -> 30 + builder.getDataSupplier("ct_1").get().get())
 * }</pre>
 *
 * Since the lazy evaluation of lambda expression, the new lambda expression of "ct_1" would be
 * a self-reference of builder.<br>
 * Instead, you should keep "the old lambda expression":
 * <pre>{@code
 *    builder -> {
 *        final Supplier<Integer> oldSupplier = builder.getDataSupplier("ct_1").get();
 *        builder.fieldOfValueSupplier("ct_1", () -> 30 + oldSupplier.get());
 *    }
 * }</pre>
 */
public class DataRow {
	private SchemaTable tableSchema;
	private Map<String, DataField<?>> data;
	private boolean validated = false;

	/**
	 * Used with {@link DataRow#build}.
	 */
	public class Builder {
		private DataField.Factory fieldFactory = null;

		private Builder() {}

		/**
		 * Sets the table schema.
		 *
		 * @param newTableSchema The table schema
		 *
		 * @return cascading self
		 */
		public Builder tableSchema(SchemaTable newTableSchema)
		{
			Validate.notNull(newTableSchema, "Need viable table schema");

			fieldFactory = new DataField.Factory(newTableSchema);

			/**
			 * Change the table schema of column
			 */
			Map<String, DataField<?>> dataWithNewTableSchema = new HashMap<>(data.size());

			data.entrySet().forEach(
				fieldEntry -> dataWithNewTableSchema.put(
					fieldEntry.getKey(),
					fieldFactory.clone(fieldEntry.getValue())
				)
			);

			tableSchema = newTableSchema;
			return data(dataWithNewTableSchema);
			// :~)
		}
		/**
		 * Sets data of row.
		 *
		 * @param newData the data of row
		 *
		 * @return cascading self
		 */
		public Builder data(Map<String, DataField<?>> newData)
		{
			Validate.notNull(newData, "Need viable data");

			data = newData;
			return this;
		}

		/**
		 * Sets value of a field.
		 *
		 * @param <T> The type of value
		 * @param columnName The name of column
		 * @param value The value instance
		 *
		 * @return cascading self
		 */
		public <T> Builder fieldOfValue(String columnName, T value)
		{
			return field(
				fieldFactory.composeData(columnName, value)
			);
		}
		/**
		 * Sets value supplier of a field.
		 *
		 * @param <T> The type of value
		 * @param columnName The name of column
		 * @param valueSupplier The value instance
		 *
		 * @return cascading self
		 */
		public <T> Builder fieldOfValueSupplier(String columnName, Supplier<T> valueSupplier)
		{
			return field(
				fieldFactory.composeDataSupplier(columnName, valueSupplier)
			);
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
		 * Gets definition of table.
		 *
		 * @return definition of table
		 *
		 * @see #getTableName
		 */
		public SchemaTable getTable()
		{
			return tableSchema;
		}

		/**
		 * Gets value of data.
		 *
		 * @param <T> The type of data
		 * @param columnName The name of column
		 *
		 * @return empty option if the column is not existing or the value is empty
		 */
		public <T> Optional<T> getData(String columnName)
		{
			return this.<T>getDataField(columnName).map(
				dataField -> dataField.getData()
			);
		}

		/**
		 * Gets supplier of data.<br>
		 *
		 * <em style="color:red">Note: be careful of lazy building, which may cause infinite recursion</em>
		 * <pre>{@code
		 *   // Keep the reference of supplier in current lambda of builder
		 *   Supplier<Integer> sourceSupplier = builder.<Integer>getDataSupplier("ct_1").get();
		 *   // Cause infinite recursion(lazy evaluation)
		 *   // Supplier<Integer> wrappedSupplier = () -> 20 + builder.<Integer>getDataSupplier("ct_1").get().get();
		 *   Supplier<Integer> wrappedSupplier = () -> 20 + sourceSupplier.get();
		 *
		 *   builder.fieldOfValueSupplier("col_1", wrappedSupplier);
		 * }</pre>
		 *
		 * @param <T> The type of data for the supplier
		 * @param columnName The name of column
		 *
		 * @return empty option if the column is not existing or the value supplier is empty
		 */
		@SuppressWarnings("unchecked")
		public <T> Optional<Supplier<T>> getDataSupplier(String columnName)
		{
			return this.<T>getDataField(columnName).map(
				dataField -> (Supplier<T>)dataField.getDataSupplier().orElse(null)
			);
		}

		/**
		 * Gets field of data.
		 *
		 * @param <T> The data type of field
		 * @param columnName The name of column
		 *
		 * @return empty option if the column is not existing
		 */
		@SuppressWarnings("unchecked")
		public <T> Optional<DataField<T>> getDataField(String columnName)
		{
			columnName = StringUtils.trimToNull(columnName);
			Validate.notNull(columnName, "Need viable name of column");

			return Optional.ofNullable((DataField<T>)data.get(columnName));
		}

		/**
		 * Gets the fields as {@link Stream}.
		 *
		 * @return The data fields as stream
		 */
		public Stream<DataField<?>> getStreamOfFields()
		{
			return data.values().stream();
		}

		private <T> Builder field(DataField<T> field)
		{
			data.put(field.getColumnName(), field);
			return this;
		}
	}

	private DataRow() {}

	/**
	 * Builds row by {@link Consumer} of {@link Builder}.
	 *
	 * @param builderConsumer The consumer to build row
	 *
	 * @return the built data of row
	 */
	public static DataRow build(Consumer<Builder> builderConsumer)
	{
		DataRow newRow = new DataRow();
		newRow.data = new HashMap<>(CollectionUsage.HASH_SPACE_OF_COLUMNS);
		Builder newBuilder = newRow.new Builder();

		builderConsumer.accept(newBuilder);

		return newRow.clone();
	}

	/**
	 * Builds row by {@link Consumer} of {@link Builder} and existing row.
	 *
	 * @param builderConsumer The consumer to build row
	 * @param existingRow The row to be modified
	 *
	 * @return the built data of row
	 */
	public static DataRow build(Consumer<Builder> builderConsumer, DataRow existingRow)
	{
		DataRow newRow = existingRow.modifiableClone();
		Builder newBuilder = newRow.new Builder();
		newBuilder.tableSchema(existingRow.getTable());

		builderConsumer.accept(newBuilder);

		return newRow.clone();
	}

	/**
	 * Gets the table schema.
	 *
	 * @return table schema
	 */
	public SchemaTable getTable()
	{
		return tableSchema;
	}

	/**
	 * Gets the field by column name.
	 *
	 * @param <T> The type of field
	 * @param columnName The name of column
	 *
	 * @return The field with the column name
	 *
	 * @throws IllegalArgumentException If the is no such field found with the column name
	 *
	 * @see #getData
	 */
	@SuppressWarnings("unchecked")
	public <T> DataField<T> getDataField(String columnName)
	{
		DataField<T> dataField = (DataField<T>)data.get(columnName);
		if (dataField == null) {
			throw new IllegalArgumentException(String.format("Cannot find column: \"%s\"", columnName));
		}

		return dataField;
	}

	/**
	 * Gets the data by column name.
	 *
	 * @param <T> The tyep of data
	 * @param columnName The name of column
	 *
	 * @return The field with the column name
	 *
	 * @throws IllegalArgumentException If the is no such field found with the column name
	 *
	 * @see #getDataField
	 */
	public <T> T getData(String columnName)
	{
		return this.<T>getDataField(columnName).getData();
	}

	/**
	 * Validates the data of row with schema information from {@link #getTable}.
	 *
	 * @throws DataRowException The exception if the validation is failed
	 *
	 * @see #isValidated
	 */
	public void validate() throws DataRowException
	{
		if (validated) {
			return;
		}

		/**
		 * Checks defined data has corresponding definition in database
		 */
		for (DataField<?> dataField: data.values()) {
			if (!tableSchema.hasColumn(dataField.getColumnName())) {
				throw new MissedColumnException(tableSchema, dataField.getColumn());
			}
		}
		// :~)

		validated = true;
	}

	/**
	 * Whether or not this row is validated with schema of table.
	 *
	 * @return true if validated
	 *
	 * @see #validate
	 */
	public boolean isValidated()
	{
		return validated;
	}

	@Override
	public DataRow clone()
	{
		DataRow newRow = new DataRow();
		newRow.tableSchema = this.tableSchema;
		newRow.data = Collections.unmodifiableMap(this.data);

		return newRow;
	}

	private DataRow modifiableClone()
	{
		DataRow newRow = new DataRow();
		newRow.tableSchema = this.tableSchema;
		newRow.data = new HashMap<>(this.data);

		return newRow;
	}

	/**
	 * Hashes the table schema and data.
	 */
	@Override
	public int hashCode()
	{
		return new HashCodeBuilder(82782651, 925373735)
			.append(tableSchema)
			.append(data)
		.toHashCode();
	}

	/**
	 * Compares the table schema and data.
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj == null) { return false; }
		if (obj == this) { return true; }
		if (obj.getClass() != getClass()) {
			return false;
		}

		DataRow rhs = (DataRow)obj;
		return new EqualsBuilder()
			.append(this.tableSchema, rhs.tableSchema)
			.append(this.data, rhs.data)
			.isEquals();
	}
}
