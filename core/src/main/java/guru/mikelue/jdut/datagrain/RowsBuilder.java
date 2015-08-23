package guru.mikelue.jdut.datagrain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Declares the interface to build a bunch of rows.
 */
public interface RowsBuilder {
	/**
	 * Sets the sequence of columns for implicit data.
	 *
	 * @param nameOfColumns The name of columns by sequence of varargs.
	 *
     * @return cascading self
	 *
	 * @see #addValues(Object...)
	 */
	public RowsBuilder implicitColumns(String... nameOfColumns);

    /**
     * Adds a row with values of fields.
     *
     * @param valuesOfField The values of field
     *
     * @return cascading self
     */
    public RowsBuilder addValues(Object... valuesOfField);
    /**
     * Adds a row with implicit data, the sequence of columns is defined by {@link #implicitColumns}.
     *
     * @param dataFields The object of {@link DataField}
     *
     * @return cascading self
     *
     * @see #implicitColumns
     */
    public RowsBuilder addFields(DataField<?>... dataFields);
    /**
     * Builds a data field.
     *
     * @param <T> the type of data for the field
     * @param columnName The name of column
     * @param fieldValue The value of field
     *
     * @return data field
     *
     * @see #addFields(DataField...)
     */
    public <T> DataField<T> newField(String columnName, T fieldValue);
    /**
     * Builds a data field by {@link Supplier}.
     *
     * @param <T> the type of data for the field
     * @param columnName The name of column
     * @param fieldSupplier The value of field
     *
     * @return data field
     *
     * @see #addFields(DataField...)
     */
    public <T> DataField<T> newField(String columnName, Supplier<T> fieldSupplier);
}

/**
 * The definition of columns is not relevant to schema of table.
 */
class RowsBuilderImpl implements RowsBuilder {
	private final SchemaTable tableSchema;
	private List<Map<String, DataField<?>>> rows = new ArrayList<>(CollectionUsage.LIST_SIZE_OF_ROWS);
    private Map<String, SchemaColumn> columns = new HashMap<>(CollectionUsage.HASH_SPACE_OF_COLUMNS);
    private Map<Integer, SchemaColumn> columnsOfIndexed;

	RowsBuilderImpl(SchemaTable newTableSchema)
	{
		tableSchema = newTableSchema;
	}

	@Override
	public RowsBuilder implicitColumns(String... nameOfColumns)
	{
		columnsOfIndexed = new HashMap<>(CollectionUsage.HASH_SPACE_OF_COLUMNS);

		Arrays.asList(nameOfColumns)
			.forEach(new Consumer<String>() {
				int index = 1;

				@Override
				public void accept(String columnName)
				{
					if (!columns.containsKey(columnName)) {
						columns.put(
							columnName,
							SchemaColumn.build(builder -> builder.tableSchema(tableSchema).name(columnName))
						);
					}

					columnsOfIndexed.put(index++, columns.get(columnName));
				}
			});

		return this;
	}

	@Override
    public RowsBuilder addValues(Object... valuesOfField)
	{
		return add(
			Stream.of(valuesOfField)
				.map(
					new Function<Object, DataField<Object>>() {
						int index = 1;

						@Override
						public DataField<Object> apply(Object value)
						{
                            return new DataField<>(
                                columnsOfIndexed.get(index++),
                                value
                            );
						}
					}
				)
		);
	}
	@Override
    public RowsBuilder addFields(DataField<?>... dataFields)
	{
		return add(Stream.of(dataFields));
	}
	@Override
    public <T> DataField<T> newField(String columnName, T fieldValue)
	{
		if (!columns.containsKey(columnName)) {
			columns.put(columnName, SchemaColumn.build(builder -> builder.tableSchema(tableSchema).name(columnName)));
		}

		return new DataField<T>(
			columns.get(columnName), fieldValue
		);
	}
	@Override
    public <T> DataField<T> newField(String columnName, Supplier<T> fieldSupplier)
	{
		if (!columns.containsKey(columnName)) {
			columns.put(columnName, SchemaColumn.build(builder -> builder.tableSchema(tableSchema).name(columnName)));
		}

		return new DataField<T>(
			columns.get(columnName), fieldSupplier
		);
	}

	private RowsBuilder add(Stream<DataField<?>> dataFields)
	{
		rows.add(
			dataFields
				.map(dataField -> dataField.asMapEntry())
				.collect(Collectors.toMap(
					entry -> entry.getKey(),
					entry -> entry.getValue()
				))
		);

		return this;
	}

	List<DataRow> toDataRows()
	{
		final List<DataRow> result = new ArrayList<>(rows.size());
		rows.forEach(
			fieldMap -> result.add(
				DataRow.build(builder -> builder
					.tableSchema(tableSchema)
					.data(fieldMap)
				)
			)
		);

		return result;
	}
}
