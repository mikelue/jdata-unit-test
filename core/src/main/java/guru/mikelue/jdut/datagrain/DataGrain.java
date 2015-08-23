package guru.mikelue.jdut.datagrain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.lang3.Validate;

import guru.mikelue.jdut.decorate.DataGrainDecorator;

/**
 * Represents the data of rows.<br>
 *
 * You may decorates data grain by {@link DataGrainDecorator}.
 * <pre>{@code
 *     // dataGrain - Initialized object of DataGrain
 *     // myDecorator - Your decorator
 *
 *     dataGrain = dataGrain.decorate(myDecorator)
 * }</pre>
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

    private DataGrain(List<DataRow> rows)
	{
		this.rows = Collections.unmodifiableList(rows);
	}

	/**
	 * Gets a row by index(starts with "0").
	 *
	 * @param index The index of row, starts with "0"
	 *
	 * @return The match data row
	 */
	public DataRow getRow(int index)
	{
		Validate.inclusiveBetween(0, rows.size() - 1, index, "The index is invalid: [%d]", index);
		return rows.get(index);
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
}
