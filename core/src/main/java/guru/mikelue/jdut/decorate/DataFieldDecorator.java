package guru.mikelue.jdut.decorate;

import java.util.function.BiPredicate;

import guru.mikelue.jdut.datagrain.DataField;
import guru.mikelue.jdut.datagrain.DataRow;

/**
 * Likes {@link DataFieldDecorator}, with iteration of data fields.
 */
@FunctionalInterface
public interface DataFieldDecorator {
	/**
	 * Turns this decorator to {@link DataGrainDecorator}.
	 *
	 * @return new instance of decorator of data grain
	 */
	default DataGrainDecorator toDataGrainDecorator()
	{
		return rowBuilder -> rowBuilder.getStreamOfFields()
			.forEach(
				dataField -> decorate(rowBuilder, dataField)
			);
	}

	/**
	 * Builds a decorator which executes decoration if testing of <em>dataFieldPredicate</em> is true.
	 *
	 * @param dataFieldPredicate The predicate of row builder
	 *
	 * @return new decorator with checking of <em>dataFieldPredicate</em>
	 */
	default DataFieldDecorator predicate(BiPredicate<DataRow.Builder, DataField<?>> dataFieldPredicate)
	{
		return (rowBuilder, dataField) -> {
			if (dataFieldPredicate.test(rowBuilder, dataField)) {
				decorate(rowBuilder, dataField);
			}
		};
	}

	/**
	 * Iterates the data field.
	 *
	 * @param rowBuilder used to modify the data of row
	 * @param dataField iterated field currently
	 */
	public void decorate(DataRow.Builder rowBuilder, DataField<?> dataField);
}
