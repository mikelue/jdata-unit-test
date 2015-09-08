package guru.mikelue.jdut.yaml.node;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import guru.mikelue.jdut.ConductorConfig;
import guru.mikelue.jdut.DataConductException;
import guru.mikelue.jdut.DataConductor;
import guru.mikelue.jdut.DuetFunctions;
import guru.mikelue.jdut.datagrain.DataField;
import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.decorate.DataGrainDecorator;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.operation.DataGrainOperator;
import guru.mikelue.jdut.yaml.LoadingYamlException;

/**
 * Represents the node of table and conveting internal structure to
 * {@link DuetFunctions} object.
 */
public class TableNode implements NodeBase {
	/**
	 * As the name of table.
	 */
	public final static class TableName {
		private final String name;

		public TableName(String newName)
		{
			name = newName;
		}

		@Override
		public int hashCode()
		{
			return new HashCodeBuilder(996547137, 439619991)
				.append(name)
			.toHashCode();
		}

		@Override
		public boolean equals(Object obj)
		{
			if (obj == null) { return false; }
			if (obj == this) { return true; }
			if (obj.getClass() != getClass()) {
				return false;
			}

			TableName rhs = (TableName)obj;
			return new EqualsBuilder()
				.append(this.name, rhs.name)
				.isEquals();
		}
	}

	private final static class Config {
		private Config() {}

		private Optional<String> buildOperation = Optional.empty();
		private Optional<String> cleanOperation = Optional.empty();
	}

	private Logger logger = LoggerFactory.getLogger(TableNode.class);
	private List<?> dataRows = Collections.emptyList();
	private List<String> columns = Collections.emptyList();
	private List<String> keys = Collections.emptyList();
	private Optional<String> decorator = Optional.empty();
	private Config config = new Config();
	private TableName tableName;

	@SuppressWarnings("unchecked")
	public TableNode(Object tableNode)
	{
		Map<TableName, ?> unknownTable = ((Map<TableName, ?>)tableNode);

		tableName = unknownTable.keySet().stream().findFirst().get();
		setData(unknownTable.get(tableName));
	}

	/**
	 * Converts this node to {@link DuetFunctions}.
	 *
	 * @param dataConductor the data conductor
	 * @param conductorConfig the configuration of conductor
	 * @param configNode The node of configuration
	 *
	 * @return The functions to be used by conductor
	 */
	@SuppressWarnings("unchecked")
	public DuetFunctions toDuetFunctions(
		DataConductor dataConductor, ConductorConfig conductorConfig,
		ConfigNode configNode
	) {
		DataGrain dataGrain = DataGrain.build(
			tableBuilder -> tableBuilder
				.name(tableName.name)
				.keys(keys.toArray(new String[0])),
			dataBuilder -> {
				dataBuilder.implicitColumns(columns.toArray(new String[0]));

				dataRows.forEach(
					row -> {
						if (List.class.isInstance(row)) {
							dataBuilder.addValues(((List<Object>)row).toArray(new Object[0]));
							return;
						}
						if (Map.class.isInstance(row)) {
							dataBuilder.addFields(
								((Map<String, Object>)row).entrySet().stream()
									.map(entry -> dataBuilder.newField(entry.getKey(), entry.getValue()))
									.collect(Collectors.toList())
									.toArray(new DataField<?>[0])
							);
							return;
						}

						throw new LoadingYamlException("Unknown type of \"data\"[%s] for table: %s", row.getClass(), tableName.name);
					}
				);
			}
		);

		String nameOfBuilding = config.buildOperation.orElseGet(() -> configNode.getNameOfBuildOperator());
		String nameOfCleaning = config.cleanOperation.orElseGet(() -> configNode.getNameOfCleanOperator());

		logger.debug(
			"Use [{}] for building, [{}] for cleaning, Table: [{}]",
			nameOfBuilding, nameOfCleaning, tableName.name
		);

		/**
		 * Chaining the decorators
		 */
		DataGrainDecorator decoratorObject = rowBuilder -> {};
		if (configNode.getDecorator().isPresent()) {
			decoratorObject = decoratorObject.chain(
				conductorConfig.getDecorator(configNode.getDecorator().get())
					.orElseThrow(
						() -> new LoadingYamlException("Cannot found decorator: \"%s\"", configNode.getDecorator().get())
					)
			);
		}
		if (decorator.isPresent()) {
			decoratorObject = decoratorObject.chain(
				conductorConfig.getDecorator(decorator.get())
					.orElseThrow(
						() -> new LoadingYamlException("Cannot found decorator: \"%s\"", configNode.getDecorator().get())
					)
			);
		}
		// :~)

		return new DuetFunctionsImplOfDataGrain(
			dataConductor,
			dataConductor.buildJdbcFunction(
				dataGrain.decorate(decoratorObject),
				conductorConfig.getOperator(nameOfBuilding).orElseThrow(
					() -> new LoadingYamlException("Cannot found operator: \"%s\"", nameOfBuilding)
				)
			),
			conductorConfig.getOperator(nameOfCleaning).orElseThrow(
				() -> new LoadingYamlException("Cannot found operator: \"%s\"", nameOfBuilding)
			)
		);
	}

	@Override
	public NodeType getNodeType()
	{
		return NodeType.Table;
	}

	@SuppressWarnings("unchecked")
	private void setData(Object unknownData)
	{
		logger.trace("Load table: {}", tableName.name);

		/**
		 * Simple data configuration
		 */
		if (List.class.isInstance(unknownData)) {
			dataRows = (List<?>)unknownData;
			return;
		}
		// :~)

		Map<String, ?> complexData = (Map<String, ?>)unknownData;
		complexData.forEach(
			(key, value) -> {
				switch (key) {
					case "columns":
						Validate.isTrue(List.class.isInstance(value), "\"columns\" need to be !!seq");
						columns = (List<String>)value;
						break;
					case "data":
						Validate.isTrue(List.class.isInstance(value), "\"data\" need to be !!seq");
						dataRows = (List<?>)value;
						break;
					case "keys":
						Validate.isTrue(List.class.isInstance(value), "\"keys\" need to be !!seq");
						keys = (List<String>)value;
						break;
					case "config":
						Validate.isTrue(Map.class.isInstance(value), "\"config\" need to be !!map");
						((Map<String, String>)value).forEach(
							(keyOfConfig, valueOfConfig) -> {
								switch (keyOfConfig) {
									case "build_operation":
										config.buildOperation = Optional.of(valueOfConfig);
										break;
									case "clean_operation":
										config.cleanOperation = Optional.of(valueOfConfig);
										break;
									case "decorator":
										decorator = Optional.of((String)valueOfConfig);
										break;
									default:
										throw new LoadingYamlException("Unknown property[%s] of \"config\" in table: \"%s\"", keyOfConfig, tableName.name);
								}
							}
						);
						break;
					default:
						throw new LoadingYamlException("Unknown property of \"%s\" in table: \"%s\"", key, tableName.name);
				}
			}
		);
	}
}

class DuetFunctionsImplOfDataGrain implements DuetFunctions {
	private DataGrain cleanDataGrain;

	private JdbcFunction<Connection, DataGrain> buildFunction;
	private JdbcFunction<Connection, DataGrain> cleanFunction;

	DuetFunctionsImplOfDataGrain(
		DataConductor dataConductor,
		JdbcFunction<Connection, DataGrain> newBuildFunction,
		DataGrainOperator cleanOperator
	) {
		buildFunction = conn -> {
			DataGrain processedDataGrain = newBuildFunction.applyJdbc(conn)
				.reverse();

			cleanDataGrain = processedDataGrain.reverse();

			return processedDataGrain;
		};

		cleanFunction = conn -> cleanOperator.operate(conn, cleanDataGrain);
	}

	@Override
	public JdbcFunction<Connection, ?> getBuildFunction()
	{
		return buildFunction;
	}

	@Override
	public JdbcFunction<Connection, ?> getCleanFunction()
	{
		if (cleanDataGrain == null) {
			throw new DataConductException("The data grain has not been used for building before used for cleaning");
		}

		return cleanFunction;
	}
}
