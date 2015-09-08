package guru.mikelue.jdut.yaml.node;

import java.sql.Connection;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.operation.DefaultOperators;
import guru.mikelue.jdut.yaml.LoadingYamlException;

/**
 * As the "config" property for a YAML document.
 */
public class ConfigNode {
	private Logger logger = LoggerFactory.getLogger(ConfigNode.class);

	private Optional<Boolean> transactional = Optional.of(false);
	private Optional<Integer> transactionIsolation = Optional.empty();
	private String nameOfBuildOperator = DefaultOperators.INSERT;
	private String nameOfCleanOperator = DefaultOperators.DELETE;
	private Optional<String> decorator = Optional.empty();

	/**
	 * Default configuration for the YAML document.
	 */
	public ConfigNode() {}

	@SuppressWarnings("unchecked")
	public ConfigNode(Object configNode)
	{
		Map<String, Map<String, ?>> mapOfConfig = (Map<String, Map<String, ?>>)configNode;

		mapOfConfig.get("config").forEach(
			(key, value) -> {
				switch (key) {
					case "transaction":
						logger.trace("With transaction: [{}]", value);
						transactional = Optional.of((Boolean)value);
						break;
					case "transaction_isolation":
						logger.trace("With transaction isolation: [{}]", value);
						transactionIsolation = Optional.of(getTransactionIsolation((String)value));
						break;
					case "build_operation":
						nameOfBuildOperator = (String)value;
						break;
					case "clean_operation":
						nameOfCleanOperator = (String)value;
						break;
					case "decorator":
                        decorator = Optional.of((String)value);
						break;
					default:
						throw new LoadingYamlException("Unknown property for \"config\": \"%s\"", key);
				}
			}
		);
	}

	public boolean getTransactionl()
	{
		return transactional.get();
	}
	public Optional<Integer> getTransactionIsolation()
	{
		return transactionIsolation;
	}
	public String getNameOfBuildOperator()
	{
		return nameOfBuildOperator;
	}
	public String getNameOfCleanOperator()
	{
		return nameOfCleanOperator;
	}
	public Optional<String> getDecorator()
	{
		return decorator;
	}

	private int getTransactionIsolation(String isolation)
	{
		switch (isolation.toUpperCase()) {
			case "READ_COMMITTED":
				return Connection.TRANSACTION_READ_COMMITTED;
			case "READ_UNCOMMITTED":
				return Connection.TRANSACTION_READ_UNCOMMITTED;
			case "REPEATABLE_READ":
				return Connection.TRANSACTION_REPEATABLE_READ;
			case "SERIALIZABLE":
				return Connection.TRANSACTION_SERIALIZABLE;
			default:
				throw new LoadingYamlException("Unknown transaction isolation: [%s]", isolation);
		}
	}
}
