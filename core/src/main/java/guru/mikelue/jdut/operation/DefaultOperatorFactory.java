package guru.mikelue.jdut.operation;

import java.sql.DatabaseMetaData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import javax.sql.DataSource;

import guru.mikelue.jdut.function.OperatorPredicate;
import guru.mikelue.jdut.jdbc.JdbcSupplier;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.vendor.DatabaseVendor;
import guru.mikelue.jdut.vendor.mssql.MsSql;

/**
 * This implementation would cache the found operator in order to improve performance for looking for
 * corresponding operators.
 */
public class DefaultOperatorFactory implements OperatorFactory {
	private DataSource dataSource;

	// Used for caching fetched operator
	private Map<String, DataGrainOperator> cachedOperator = new HashMap<>(8);

	private List<OperatorPredicate> predicates = new ArrayList<>(8);
	private List<Map<String, DataGrainOperator>> operators = new ArrayList<>(8);

	/**
	 * Used to configure this factory to support operator-inspection.
	 */
	public class Builder {
		private List<OperatorPredicate> predicates = new ArrayList<>(8);
		private List<Map<String, DataGrainOperator>> operators = new ArrayList<>(8);

		private Builder() {}

		/**
		 * Adds a predicate and target operator if the predicate {@link BiPredicate#test testing} is true.
		 *
		 * @param predicate The predicate to be checked
		 * @param matchedOperators The target operator
		 *
		 * @return cascading self
		 */
		public Builder add(OperatorPredicate predicate, Map<String, DataGrainOperator> matchedOperators)
		{
			predicates.add(predicate);
			operators.add(Collections.unmodifiableMap(matchedOperators));

			return this;
		}
	}

	/**
	 * Builds with data source and builder of this object.
	 *
	 * @param newDataSource The data source
	 * @param builderConsumer The builder to set-up operator inspector
	 *
	 * @return The initialized factory
	 */
	public static OperatorFactory build(DataSource newDataSource, Consumer<Builder> builderConsumer)
	{
		DefaultOperatorFactory newFactory = new DefaultOperatorFactory();
		newFactory.dataSource = newDataSource;
		DefaultOperatorFactory.Builder newBuilder = newFactory.new Builder();

		builderConsumer.accept(newBuilder);

		/**
		 * Adds build-in vendor-specific operators
		 */
		newBuilder.add(
			DatabaseVendor.buildOperatorPredicate(DatabaseVendor.MsSql),
			MsSql.DEFAULT_OPERATORS
		);
		// :~)

		/**
		 * Puts the customized operators in front of build-ins.
		 */
		newBuilder.predicates.addAll(newFactory.predicates);
		newBuilder.operators.addAll(newFactory.operators);
		// :~)

		newFactory.predicates = newBuilder.predicates;
		newFactory.operators = newBuilder.operators;

		return newFactory.clone();
	}

	private final static Map<String, DataGrainOperator> DEFINED;

	static {
		Map<String, DataGrainOperator> operators = new HashMap<>(8);

		operators.put(DefaultOperators.INSERT, DefaultOperators::insert);
		operators.put(DefaultOperators.UPDATE, DefaultOperators::update);
		operators.put(DefaultOperators.REFRESH, DefaultOperators::refresh);
		operators.put(DefaultOperators.DELETE, DefaultOperators::delete);
		operators.put(DefaultOperators.DELETE_ALL, DefaultOperators::deleteAll);
		operators.put(DefaultOperators.TRUNCATE, DefaultOperators::truncate);
		operators.put(DefaultOperators.NONE, DefaultOperators::none);

		DEFINED = Collections.unmodifiableMap(operators);
	}

	private DefaultOperatorFactory() {}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataGrainOperator get(String name)
	{
		if (!cachedOperator.containsKey(name)) {
			/**
			 * Finds first matched predicates by meta data of database
			 */
			OptionalInt matchedIndex = JdbcTemplateFactory.buildSupplier(
				() -> dataSource.getConnection(),
				conn -> {
					final DatabaseMetaData metaData = conn.getMetaData();

					return IntStream.range(0, predicates.size())
						.filter(
							i -> ((JdbcSupplier<Boolean>) () ->
								predicates.get(i).test(metaData)
							)
								.asSupplier().get()
						)
						.findFirst();
				}
			).asSupplier().get();
			// :~)

			/**
			 * Adds operator to cache if the there is matched operator.
			 *
			 * 1) The defined operators may not be matched
			 * 2) The customized ones may not be matched, try to find one from defined operators
			 */
			DataGrainOperator operator = null;
			if (matchedIndex.isPresent()) {
				operator = operators.get(matchedIndex.getAsInt()).get(name);
			}
			if (operator == null) {
				operator = DEFINED.get(name);
			}

			cachedOperator.put(name, operator);
			// :~)
		}

		return cachedOperator.get(name);
	}

	@Override
	protected DefaultOperatorFactory clone()
	{
		DefaultOperatorFactory newFactory = new DefaultOperatorFactory();
		newFactory.dataSource = this.dataSource;
		newFactory.predicates = Collections.unmodifiableList(this.predicates);
		newFactory.operators = Collections.unmodifiableList(this.operators);

		return newFactory;
	}
}
