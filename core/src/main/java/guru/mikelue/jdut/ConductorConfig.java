package guru.mikelue.jdut;

import java.io.Reader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import guru.mikelue.jdut.decorate.DataGrainDecorator;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.SQLExceptionConvert;
import guru.mikelue.jdut.operation.DataGrainOperator;
import guru.mikelue.jdut.operation.OperatorFactory;

/**
 * Defines the configuration for building block of conduction.<br>
 *
 * This object supports hierarchy, if the getXXX method cannot found one, try fetch from parent.
 *
 * <h3>Building blocks</h3>
 * <ol>
 * 	<li>The loader of resource</li>
 * 	<li>The {@link SQLExceptionConvert SQLExceptionConvert}</li>
 * 	<li>The {@link OperatorFactory operation factory}</li>
 * 	<li>The named {@link DataGrainOperator operators}</li>
 * 	<li>The named {@link DataGrainDecorator decorations}</li>
 * 	<li>The named {@link JdbcFunction JdbcFunction}</li>
 * </ol>
 */
public class ConductorConfig {
	/**
	 * This object is fed by {@link ConductorConfig#build ConductorConfig.build} through {@link Consumer}.
	 */
	public class Builder {
		private Builder() {};

		/**
		 * Sets the parent configuration
		 *
		 * @param newParent The parent configuration, could be null
		 *
		 * @return cascading self
		 */
		public Builder parent(ConductorConfig newParent)
		{
			parent = Optional.ofNullable(newParent);
			return this;
		}

		/**
		 * Sets the resource reader
		 *
		 * @param newResourceLoader The loader of resource
		 *
		 * @return cascading self
		 */
		public Builder resourceLoader(Function<String, Reader> newResourceLoader)
		{
			resourceLoader = Optional.ofNullable(newResourceLoader);
			return this;
		}

		/**
		 * Sets the conversion for {@link SQLException}.
		 *
		 * @param <E> The type of {@link RuntimeException} to be the result exception
		 * @param newSqlExceptionConvert The conversion for {@link SQLException}
		 *
		 * @return cascading self
		 */
		public <E extends RuntimeException> Builder sqlExceptionConvert(SQLExceptionConvert<E> newSqlExceptionConvert)
		{
			sqlExceptionConvert = Optional.ofNullable(newSqlExceptionConvert);
			return this;
		}

		/**
		 * Sets operator factory.
		 *
		 * @param newOperatorFactory The operator factory, could be null
		 *
		 * @return cascading self
		 */
		public Builder operatorFactory(OperatorFactory newOperatorFactory)
		{
			operatorFactory = Optional.ofNullable(newOperatorFactory);
			return this;
		}

		/**
		 * Puts named operator
		 *
		 * @param name The name of operator used to be fetched
		 * @param operator The target operator
		 *
		 * @return cascading self
		 */
		public Builder namedOperator(String name, DataGrainOperator operator)
		{
			name = StringUtils.trimToNull(name);
			Validate.notNull(name, "Need viable name of operator");
			Validate.notNull(operator, "Need viable operator");

			namedOperators.put(name, operator);
			return this;
		}

		/**
		 * Puts named supplier
		 *
		 * @param name The name of operator used to be fetched
		 * @param supplier The supplier matching the name
		 *
		 * @return cascading self
		 */
		public Builder namedSupplier(String name, Supplier<?> supplier)
		{
			name = StringUtils.trimToNull(name);
			Validate.notNull(name, "Need viable name of supplier");
			Validate.notNull(supplier, "Need viable supplier");

			namedSuppliers.put(name, supplier);
			return this;
		}

		/**
		 * Puts named JDBC function.
		 *
		 * @param name The name of function used to be fetched
		 * @param jdbcFunction The target function of JDBC
		 *
		 * @return cascading self
		 */
		@SuppressWarnings("unchecked")
		public Builder namedJdbcFunction(String name, JdbcFunction<? extends Connection, ?> jdbcFunction)
		{
			name = StringUtils.trimToNull(name);
			Validate.notNull(name, "Need viable name of JDBC function");
			Validate.notNull(jdbcFunction, "Need viable operator");

			namedJdbcFunctions.put(name, (JdbcFunction<Connection, ?>)jdbcFunction);
			return this;
		}

		/**
		 * Puts named decorator.
		 *
		 * @param name The name of decorator used to be fetched
		 * @param decorator target The decorator
		 *
		 * @return cascading self
		 */
		public Builder namedDecorator(String name, DataGrainDecorator decorator)
		{
			name = StringUtils.trimToNull(name);
			Validate.notNull(name, "Need viable name of JDBC function");
			Validate.notNull(decorator, "Need viable decorator");

			namedDecorators.put(name, decorator);
			return this;
		}
	}

	/**
	 * Builds configuration with {@link Consumer} of builder.
	 *
	 * @param builderConsumer the consumer of builder
	 *
	 * @return The initialized configuration
	 *
	 * @see Builder
	 */
	public static ConductorConfig build(Consumer<Builder> builderConsumer)
	{
		ConductorConfig config = new ConductorConfig();
		ConductorConfig.Builder newBuilder = config.new Builder();

		builderConsumer.accept(newBuilder);

		return config.clone();
	}

	/**
	 * Builds configuration with {@link Consumer} of builder and another configuration.
	 *
	 * @param builderConsumer the consumer of builder
	 * @param clonedConfig The cloned configuration
	 *
	 * @return The initialized configuration
	 *
	 * @see Builder
	 */
	public static ConductorConfig build(Consumer<Builder> builderConsumer, ConductorConfig clonedConfig)
	{
		ConductorConfig newConfig = clonedConfig.modifiableClone();
		ConductorConfig.Builder newBuilder = newConfig.new Builder();

		builderConsumer.accept(newBuilder);

		return newConfig.clone();
	}

	private Optional<ConductorConfig> parent = Optional.empty();

	private Map<String, DataGrainOperator> namedOperators = new HashMap<>(4);
	private Map<String, JdbcFunction<Connection, ?>> namedJdbcFunctions = new HashMap<>(4);
	private Map<String, DataGrainDecorator> namedDecorators = new HashMap<>(4);
	private Map<String, Supplier<?>> namedSuppliers = new HashMap<>(4);
	private Optional<Function<String, Reader>> resourceLoader = Optional.empty();
	private Optional<OperatorFactory> operatorFactory = Optional.empty();
	private Optional<SQLExceptionConvert<?>> sqlExceptionConvert = Optional.empty();

	private ConductorConfig() {}

	/**
	 * Gets loader of resource(optional).
	 *
	 * @return The loader of resource or parent's one
	 */
	public Optional<Function<String, Reader>> getResourceLoader()
	{
		if (!resourceLoader.isPresent() && parent.isPresent()) {
			return parent.get().getResourceLoader();
		}

		return resourceLoader;
	}

	/**
	 * Gets factory of operator(optional).
	 *
	 * @return The factory of operators
	 */
	public Optional<OperatorFactory> getOperatorFactory()
	{
		return operatorFactory;
	}

	/**
	 * Gets operator by various sources.
	 *
	 * Priority of fetching:
	 * <ol>
	 * 	<li>Fetches from named operator</li>
	 * 	<li>Fetches by operator factory(if provided)</li>
	 * 	<li>Fetches from named operator of parent</li>
	 * 	<li>Fetches by operator factory of parent(if provided)</li>
	 * </ol>
	 *
	 * @param name The name of operator
	 *
	 * @return matched operator
	 */
	public Optional<DataGrainOperator> getOperator(String name)
	{
		if (namedOperators.containsKey(name)) {
			return Optional.of(namedOperators.get(name));
		}

		if (operatorFactory.isPresent()) {
			DataGrainOperator operator = operatorFactory.get().get(name);
			if (operator != null) {
				return Optional.of(operator);
			}
		}

		if (parent.isPresent()) {
			return parent.get().getOperator(name);
		}

		return Optional.empty();
	}

	/**
	 * Gets decorator by various sources.
	 *
	 * Priority of fetching:
	 * <ol>
	 * 	<li>Fetches from named decorator</li>
	 * 	<li>Fetches from named decorator of parent</li>
	 * </ol>
	 *
	 * @param name The name of decorator
	 *
	 * @return matched decorator
	 */
	public Optional<DataGrainDecorator> getDecorator(String name)
	{
		if (namedDecorators.containsKey(name)) {
			return Optional.of(namedDecorators.get(name));
		}

		if (parent.isPresent()) {
			return parent.get().getDecorator(name);
		}

		return Optional.empty();
	}

	/**
	 * Gets supplier by various sources.
	 *
	 * Priority of fetching:
	 * <ol>
	 * 	<li>Fetches from named supplier</li>
	 * 	<li>Fetches from named supplier of parent</li>
	 * </ol>
	 *
	 * @param name The name of supplier
	 *
	 * @return matched supplier
	 */
	public Optional<Supplier<?>> getSupplier(String name)
	{
		if (namedSuppliers.containsKey(name)) {
			return Optional.of(namedSuppliers.get(name));
		}

		if (parent.isPresent()) {
			return parent.get().getSupplier(name);
		}

		return Optional.empty();
	}

	/**
	 * Gets JDBC function by various sources.
	 *
	 * Priority of fetching:
	 * <ol>
	 * 	<li>Fetches from named JDBC function</li>
	 * 	<li>Fetches from named JDBC function of parent</li>
	 * </ol>
	 *
	 * @param name The name of JDBC function
	 *
	 * @return matched JDBC function
	 */
	public Optional<JdbcFunction<Connection, ?>> getJdbcFunction(String name)
	{
		if (namedJdbcFunctions.containsKey(name)) {
			return Optional.of(namedJdbcFunctions.get(name));
		}

		if (parent.isPresent()) {
			return parent.get().getJdbcFunction(name);
		}

		return Optional.empty();
	}

	/**
	 * Gets the function for conversion of {@link SQLException}.
	 *
	 * @return The optional object of lambda
	 */
	public Optional<SQLExceptionConvert<?>> getSqlExceptionConvert()
	{
		if (sqlExceptionConvert.isPresent()) {
			return sqlExceptionConvert;
		}

		if (parent.isPresent()) {
			return parent.get().getSqlExceptionConvert();
		}

		return Optional.empty();
	}

	@Override
	protected ConductorConfig clone()
	{
		ConductorConfig newConfig = commonClone();
		newConfig.namedOperators = Collections.unmodifiableMap(this.namedOperators);
		newConfig.namedJdbcFunctions = Collections.unmodifiableMap(this.namedJdbcFunctions);
		newConfig.namedDecorators = Collections.unmodifiableMap(this.namedDecorators);
		newConfig.namedSuppliers = Collections.unmodifiableMap(this.namedSuppliers);

		return newConfig;
	}

	private ConductorConfig modifiableClone()
	{
		ConductorConfig newConfig = commonClone();
		newConfig.namedOperators = new HashMap<>(this.namedOperators);
		newConfig.namedJdbcFunctions = new HashMap<>(this.namedJdbcFunctions);
		newConfig.namedDecorators = new HashMap<>(this.namedDecorators);
		newConfig.namedSuppliers = new HashMap<>(this.namedSuppliers);

		return newConfig;
	}

	private ConductorConfig commonClone()
	{
		ConductorConfig newConfig = new ConductorConfig();
		newConfig.parent = this.parent;
		newConfig.resourceLoader = this.resourceLoader;
		newConfig.operatorFactory = this.operatorFactory;
		newConfig.sqlExceptionConvert = this.sqlExceptionConvert;

		return newConfig;
	}
}
