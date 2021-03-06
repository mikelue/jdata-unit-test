package guru.mikelue.jdut.decorate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;

import org.apache.commons.lang3.Validate;

import guru.mikelue.jdut.datagrain.DataField;
import guru.mikelue.jdut.datagrain.DataRow;

/**
 * Builds the replacement by {@link Predicate} of {@link DataField}.<br>
 *
 * <p>By sequence of {@link #buildDataGrainDecorator buildDataGrainDecorator(Consumer&lt;ReplaceFieldDataDecorator.Builder&gt;)}, the first matched {@link Predicate} would be used for replacement.</p>
 */
public class ReplaceFieldDataDecorator implements DataFieldDecorator {
	private List<Predicate<? super DataField<?>>> predicates;
	private List<Object> replacements;

	/**
	 * Used with {@link #buildDataGrainDecorator buildDataGrainDecorator(Consumer&lt;ReplaceFieldDataDecorator.Builder&gt;)}.<br>
	 *
	 * <p>
	 * You could put multiple pair of {@link Predicate Predicate&lt;DataField&gt;} and <em>replaceingValue</em>,
	 * the first matched predicate would be choosed for replacement.
	 * </p>
	 */
	public class Builder {
		protected Builder() {}

		/**
		 * Adds a replacement with checked value of replacing value.
		 *
		 * <p>The instance of <em>replacingValue</em> could be an instance of {@link Supplier}.</p>
		 *
		 * @param checkedObject Checked object
		 * @param replacingValue The value replacing old value
		 *
		 * @return cascading self
		 */
		public Builder replaceWith(Object checkedObject, Object replacingValue)
		{
			Validate.notNull(checkedObject, "The checked value of object cannot be null");

			return replaceWith(
				dataField -> checkedObject.equals(dataField.getData()),
				replacingValue
			);
		}
		/**
		 * Adds a replacement with {@link Predicate} of replacing value.<br>
		 *
		 * <p>The instance of <em>replacingValue</em> could be an instance of {@link Supplier}.</p>
		 *
		 * @param dataFieldPredicate The instance of predication
		 * @param replacingValue The value replacing old value
		 *
		 * @return cascading self
		 */
		public Builder replaceWith(Predicate<? super DataField<?>> dataFieldPredicate, Object replacingValue)
		{
			Validate.notNull(dataFieldPredicate, "Need field predicate");

			predicates.add(dataFieldPredicate);
			replacements.add(replacingValue);
			return this;
		}
	}

	/**
 	 * Builds the decoration by replacement set-up.<br>
	 *
	 * @param builderConsumer The consumer for builder
	 *
	 * @return The decorator with replacement
	 */
	public static DataGrainDecorator buildDataGrainDecorator(Consumer<Builder> builderConsumer)
	{
		Validate.notNull(builderConsumer, "Need consumer of builder");

		ReplaceFieldDataDecorator decorator = new ReplaceFieldDataDecorator();
		decorator.predicates = new ArrayList<>(8);
		decorator.replacements = new ArrayList<>(8);

		Builder builder = decorator.new Builder();
		builderConsumer.accept(builder);

		return decorator.clone().toDataGrainDecorator();
	}

	@Override
	public void decorate(final DataRow.Builder rowBuilder, final DataField<?> dataField)
	{
		IntStream.range(0, replacements.size())
			.filter(index -> predicates.get(index).test(dataField))
			.findFirst()
			.ifPresent(
				matchedIndex -> rowBuilder.fieldOfValue(dataField.getColumnName(), replacements.get(matchedIndex))
			);
	}

	@Override
	protected ReplaceFieldDataDecorator clone()
	{
		ReplaceFieldDataDecorator newDecorator = new ReplaceFieldDataDecorator();
		newDecorator.predicates = Collections.unmodifiableList(this.predicates);
		newDecorator.replacements = Collections.unmodifiableList(this.replacements);

		return newDecorator;
	}

	protected ReplaceFieldDataDecorator() {}
}
