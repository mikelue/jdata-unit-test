package guru.mikelue.jdut.datagrain;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;

import guru.mikelue.jdut.decorate.DataGrainDecorator;

/**
 * Represents the data of rows.<br>
 *
 * <h3>Defining data grains</h3>
 * The simplest by to build a data grain is using {@link #build} method:
 *
 * <pre class="java">{@code
 * DataGrain dataGrain = DataGrain.build(
 *     tableBuilder -> tableBuilder
 *         .name("tab_name_1"),
 *     rowsBuilder -> rowsBuilder
 *         .implicitColumns("col_id", "col_value")
 *         .addValues(1, "v_1")
 *         .addValues(2, "v_2")
 * );
 * }</pre>
 *
 * <h3>Combining data grains</h3>
 * The data grains could be combines(sequentially) by {@link #aggregate} method.
 *
 * <pre class="java">{@code
 * // dataGrain - Initialized object of DataGrain
 * // nextDataGrain - The next data grain
 * dataGrain = dataGrain.aggregate(nextDataGrain);
 * }</pre>
 *
 * <h3>Decoration</h3>
 * You may decorates data grain by {@link DataGrainDecorator}.
 *
 * <pre class="java">{@code
 * // dataGrain - Initialized object of DataGrain
 * // myDecorator - Your decorator
 *
 * dataGrain = dataGrain.decorate(myDecorator)
 * }</pre>
 *
 * @see DataGrainDecorator
 * @see <a target="_blank" href="https://github.com/mikelue/jdata-unit-test/wiki/API-Guideline">API Guideline</a>
 */
public class DataGrain {
	private List<DataRow> rows;

	/**
	 * Builds with setup of table schema and builder of rows.
	 *
	 * @param tableBuilderConsumer The builder for table schema
	 * @param rowsBuilderConsumer The builder for data of rows
	 *
	 * @return DataGrain object
	 */
	public static DataGrain build(
		Consumer<SchemaTable.Builder> tableBuilderConsumer,
		Consumer<RowsBuilder> rowsBuilderConsumer
	) {
		RowsBuilderImpl rowsBuilder = new RowsBuilderImpl(
			SchemaTable.build(tableBuilderConsumer)
		);

		rowsBuilderConsumer.accept(rowsBuilder);
		return new DataGrain(rowsBuilder.toDataRows());
	}

	/**
	 * Constructs this object by list of rows.
	 *
	 * @param rows The data of rows
	 */
    public DataGrain(List<DataRow> rows)
	{
		this.rows = Collections.unmodifiableList(rows);
		Validate.notNull(rows, "Need viable rows");
	}

	/**
	 * Gets a row by index(starts with "0").
	 *
	 * @param index The index of row, starts with "0"
	 *
	 * @return The match data row
	 *
	 * @see #getNumberOfRows
	 */
	public DataRow getRow(int index)
	{
		Validate.inclusiveBetween(0, rows.size() - 1, index, "The index is invalid: [%d]", index);
		return rows.get(index);
	}

	/**
	 * Gets number of rows.
	 *
	 * @return The number of rows
	 *
	 * @see #getRows
	 */
	public int getNumberOfRows()
	{
		return rows.size();
	}

	/**
	 * Gets data of rows.
	 *
	 * @return The data of rows
	 */
	public List<DataRow> getRows()
	{
		return rows;
	}

	/**
	 * Decorators this data grain and generates a new one.
	 *
	 * @param decorator The decorator to modify this data grain
	 *
	 * @return The new data grain
	 */
	public DataGrain decorate(final DataGrainDecorator decorator)
	{
		final List<DataRow> decoratedRows = new ArrayList<>(rows.size());
		rows.forEach(
			row -> decoratedRows.add(DataRow.build(
				builder -> decorator.decorate(builder),
				row
			))
		);

		return new DataGrain(decoratedRows);
	}

	/**
	 * Aggregates another data grain(appending data of current object).
	 *
	 * @param dataGrain The data grain to be aggregated
	 *
	 * @return The result data grain
	 */
	public DataGrain aggregate(DataGrain dataGrain)
	{
		List<DataRow> result = new ArrayList<>(getRows());
		result.addAll(dataGrain.getRows());

		return new DataGrain(result);
	}

	/**
	 * Reverses the data grain.
	 *
	 * @return A new data grain which is reversed(same row of copied data grain)
	 */
	public DataGrain reverse()
	{
		List<DataRow> reversedRows = new ArrayList<>(rows);
		Collections.reverse(reversedRows);

		return new DataGrain(reversedRows);
	}
}

/**
 * The definition of columns is not relevant to schema of table.
 */
class RowsBuilderImpl implements RowsBuilder {
	private final SchemaTable tableSchema;
	private final DataField.Factory fieldFactory;
	private List<Map<String, DataField<?>>> rows = new ArrayList<>(CollectionUsage.LIST_SIZE_OF_ROWS);
    private Map<String, SchemaColumn> columns = new HashMap<>(CollectionUsage.HASH_SPACE_OF_COLUMNS);
    private Map<Integer, SchemaColumn> columnsOfIndexed;

	RowsBuilderImpl(SchemaTable newTableSchema)
	{
		tableSchema = newTableSchema;
		fieldFactory = new DataField.Factory(tableSchema);
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
							SchemaColumn.build(builder -> builder.name(columnName))
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
							return fieldFactory.composeData(
								columnsOfIndexed.get(index++), value
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
			columns.put(columnName, SchemaColumn.build(builder -> builder.name(columnName)));
		}

		return fieldFactory.composeData(
			columns.get(columnName), fieldValue
		);
	}
	@Override
    public <T> DataField<T> newField(String columnName, Supplier<T> fieldSupplier)
	{
		if (!columns.containsKey(columnName)) {
			columns.put(columnName, SchemaColumn.build(builder -> builder.name(columnName)));
		}

		return fieldFactory.composeDataSupplier(
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
