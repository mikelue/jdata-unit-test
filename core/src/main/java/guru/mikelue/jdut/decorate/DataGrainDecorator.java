package guru.mikelue.jdut.decorate;

import java.util.function.Predicate;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.datagrain.DataRow;

/**
 * This interface should be used with {@link DataGrain#decorate}.
 *
 * @see <a target="_blank" href="https://github.com/mikelue/jdata-unit-test/wiki/Provided-data-grain-decoration">Provided decorators</a>
 */
@FunctionalInterface
public interface DataGrainDecorator {
	/**
	 * Chains another decorator, which is executed after current decorator.
	 *
	 * @param nextDecorator The next decorator to be executed
	 *
	 * @return new decorator
	 */
	default DataGrainDecorator chain(DataGrainDecorator nextDecorator)
	{
		return rowBuilder -> {
			decorate(rowBuilder);
			nextDecorator.decorate(rowBuilder);
		};
	}

	/**
	 * Builds a decorator which executes decoration if testing of <em>rowBuilderPredicate</em> is true.
	 *
	 * @param rowBuilderPredicate The predicate of row builder
	 *
	 * @return new decorator with checking of <em>rowBuilderPredicate</em>
	 */
	default DataGrainDecorator predicate(Predicate<DataRow.Builder> rowBuilderPredicate)
	{
		return rowBuilder -> {
			if (rowBuilderPredicate.test(rowBuilder)) {
				decorate(rowBuilder);
			}
		};
	}

	/**
	 * Decorates the row, which contains the field information.
	 *
	 * @param rowBuilder The builder of row
	 */
	public void decorate(DataRow.Builder rowBuilder);
}
