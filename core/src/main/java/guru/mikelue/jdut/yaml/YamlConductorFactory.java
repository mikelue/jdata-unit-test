package guru.mikelue.jdut.yaml;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.NClob;
import java.sql.SQLXML;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.AbstractConstruct;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import guru.mikelue.jdut.ConductorConfig;
import guru.mikelue.jdut.DataConductor;
import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.DuetFunctions;
import guru.mikelue.jdut.jdbc.JdbcFunction;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.Transactional;
import guru.mikelue.jdut.operation.DefaultOperatorFactory;
import guru.mikelue.jdut.yaml.node.*;

/**
 * The factory used to build {@link DuetConductor} by data definition of YAML format.
 */
public class YamlConductorFactory {
	private Logger logger = LoggerFactory.getLogger(YamlConductorFactory.class);
	private DataConductor dataConductor;
	private ConductorConfig conductorConfig;
	private Constructor jdutConstructor;

    /**
     * Builds factory by {@link DataSource}.<br>
	 *
	 * By default, this method would build resource loader as {@link ReaderFunctions#currentThreadContext} and
	 * {@link DefaultOperatorFactory} as operator factory.
     *
     * @param dataSource The data source for target database
	 *
	 * @return The initialized factory
     */
    public static YamlConductorFactory build(
        DataSource dataSource
    ) {
		return build(dataSource, builder -> {});
    }

    /**
     * Builds factory by {@link DataSource}.<br>
	 *
	 * By default, this method would build resource loader as {@link ReaderFunctions#currentThreadContext} and
	 * {@link DefaultOperatorFactory} as operator factory.
     *
	 * @param dataSource The data source for target database
     * @param builderConsumer consumer used to set-up this factory
	 *
	 * @return The initialized factory
     */
    public static YamlConductorFactory build(
        DataSource dataSource,
        Consumer<ConductorConfig.Builder> builderConsumer
    ) {
		final ConductorConfig config = ConductorConfig.build(builderConsumer);
		ConductorConfig finalConfig = ConductorConfig.build(
			/**
			 * Initializes default value
			 */
			builder -> {
				if (!config.getResourceLoader().isPresent()) {
					builder.resourceLoader(ReaderFunctions::currentThreadContext);
				}
				if (!config.getOperatorFactory().isPresent()) {
					builder.operatorFactory(
						DefaultOperatorFactory.build(dataSource, factoryBuilder -> {})
					);
				}
			},
			// :~)
			config
		);

		YamlConductorFactory newFactory = new YamlConductorFactory();
		newFactory.conductorConfig = finalConfig;
		newFactory.dataConductor = new DataConductor(dataSource);
		newFactory.jdutConstructor = new JdutConstructor(finalConfig, dataSource);

		return newFactory;
    }

    private YamlConductorFactory() {}

    /**
     * Builds conductor by resource of YAML.
     *
     * @param yamlResourceName The name of resource
	 *
	 * @return The conductor for building and cleaning data
     */
    public DuetConductor conductResource(
        String yamlResourceName
    ) {
        return conductResource(yamlResourceName, builder -> {});
    }
    /**
     * Builds conductor by resource of YAML.
     *
     * @param yamlResourceName The name of resource
     * @param builderConsumer The configuration builder
	 *
	 * @return The conductor for building and cleaning data
     */
    public DuetConductor conductResource(
        String yamlResourceName,
        Consumer<ConductorConfig.Builder> builderConsumer
    ) {
		final ConductorConfig finalConfig = ConductorConfig.build(
			builder -> {
				builderConsumer.accept(builder);
				builder.parent(conductorConfig);
			}
		);

		try (
			Reader yamlReader = finalConfig.getResourceLoader().get().apply(yamlResourceName)
		) {
        	return conductYaml(
				yamlReader, builderConsumer
			);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    /**
     * Builds conductor by reader of YAML.<br>
	 *
	 * This method won't close the reader object.
     *
     * @param yamlReader The reader of yaml content
	 *
	 * @return The conductor for building and cleaning data
     */
    public DuetConductor conductYaml(
        Reader yamlReader
    ) {
        return conductYaml(yamlReader, builder -> {});
    }
    /**
     * Builds conductor by reader of YAML.
	 *
	 * This method won't close the reader object.
     *
     * @param yamlReader The reader of yaml content
     * @param builderConsumer The configuration builder
	 *
	 * @return The conductor for building and cleaning data
     */
    public DuetConductor conductYaml(
        Reader yamlReader,
        Consumer<ConductorConfig.Builder> builderConsumer
    ) {
		final ConductorConfig finalConfig = ConductorConfig.build(
			builder -> {
				builderConsumer.accept(builder);
				builder.parent(conductorConfig);
			}
		);

		List<DuetFunctions> operationsInAllDoc = new ArrayList<>(4);

		Yaml yaml = new Yaml(jdutConstructor);
		for (Object object: yaml.loadAll(yamlReader)) {
			List<?> conductorDoc = (List<?>)object;
			logger.trace("Got [{}] conduct elements.", conductorDoc.size());

			/**
			 * Loads nodes and config
			 */
			ConfigNode configNode = new ConfigNode();

			List<NodeBase> nodes = new ArrayList<>(8);
			for (Object conductElement: conductorDoc) {
				switch (NodeType.getNodeType(conductElement)) {
					case Config:
						configNode = new ConfigNode(conductElement);
						break;
					case Table:
						nodes.add(new TableNode(conductElement));
						break;
					case Code:
						nodes.add((CodeNode)conductElement);
						break;
					case Defines:
						// Do nothing for defines
						break;
					default:
						throw new LoadingYamlException("Unknown node: \"%s\" for conduction", conductElement);
				}
			}
			// :~)

			/**
			 * Builds building and cleaning functions for a document
			 */
			final DuetFunctionsImpleOfDoc operationsInDoc =
				new DuetFunctionsImpleOfDoc();
			operationsInDoc.setTransactional(configNode.getTransactionl());
			operationsInDoc.setTransactionIsolation(configNode.getTransactionIsolation());

			final ConfigNode finalConfigNode = configNode;

			nodes.forEach(
				node -> { switch (node.getNodeType()) {
					case Table:
						operationsInDoc.add(
							((TableNode)node).toDuetFunctions(
								dataConductor, finalConfig,
								finalConfigNode
							)
						);
						break;
					case Code:
						operationsInDoc.add(
							((CodeNode)node).toDuetFunctions()
						);
						break;
					default:
						throw new LoadingYamlException("Unknown node type: \"%s\"", node.getNodeType());
				}}
			);
			// :~)

			operationsInAllDoc.add(operationsInDoc);
		}

		return new DuetConductorImplOfAssembly(dataConductor, operationsInAllDoc);
    }
}

class JdutConstructor extends Constructor {
	private Logger logger = LoggerFactory.getLogger(YamlConductorFactory.class);

	private final static String NAMESPACE_DB_TYPE = "tag:jdut.mikelue.guru:jdbcType:1.8/";
	private final static String NAMESPACE_JDUT = "tag:jdut.mikelue.guru:1.0/";
	private final static String NAMESPACE_SQL = "tag:jdut.mikelue.guru:sql:1.0/";

	private final ConductorConfig conductorConfig;
	private final DataSource dataSource;

	private final Construct sqlConstruct = new SqlConstruct();
	private final Construct jdutConstruct = new JdutConstruct();
	private final Construct dbTypeConstruct = new DbTypeConstruct();

    JdutConstructor(ConductorConfig newConfig, DataSource newDataSource)
	{
		conductorConfig = newConfig;
		dataSource = newDataSource;
    }

	@Override
	protected Construct getConstructor(Node node)
	{
		if (node.getTag().startsWith(NAMESPACE_SQL)) {
			return sqlConstruct;
		}
		if (node.getTag().startsWith(NAMESPACE_JDUT)) {
			return jdutConstruct;
		}
		if (node.getTag().startsWith(NAMESPACE_DB_TYPE)) {
			return dbTypeConstruct;
		}

		return super.getConstructor(node);
	}

	private class DbTypeConstruct extends AbstractConstruct {
		@Override
		public Object construct(Node node)
		{
			String tagValue = node.getTag().getValue().replace(NAMESPACE_DB_TYPE, "")
				.toUpperCase();

			JDBCType targetType = JDBCType.valueOf(tagValue);

			switch (targetType) {
				case BINARY:
				case BLOB:
				case LONGVARBINARY:
				case VARBINARY:
					return makeBinary(targetType, (byte[])JdutConstructor.this.yamlConstructors.get(Tag.BINARY).construct(node));

				case CHAR:
				case LONGNVARCHAR:
				case LONGVARCHAR:
				case NCHAR:
				case NVARCHAR:
				case VARCHAR:
				case CLOB:
				case NCLOB:
				case SQLXML:
					return makeString(targetType, (String)JdutConstructor.this.yamlConstructors.get(Tag.STR).construct(node));

				case BOOLEAN:
					return (Boolean)JdutConstructor.this.yamlConstructors.get(Tag.BOOL).construct(node);

				case BIT:
				case BIGINT:
				case INTEGER:
				case SMALLINT:
				case TINYINT:
					return makeIntegerNumber(targetType, (Number)JdutConstructor.this.yamlConstructors.get(Tag.INT).construct(node));

				case FLOAT:
				case DOUBLE:
				case NUMERIC:
				case DECIMAL:
				case REAL:
					return makeDecimalNumber(targetType, (Double)JdutConstructor.this.yamlConstructors.get(Tag.FLOAT).construct(node));

				// Unsupported type
				//case ARRAY:
				//case DATALINK:
				//case DISTINCT:
				//case JAVA_OBJECT:
				//case REF:
				//case REF_CURSOR:
				//case ROWID:
				//case STRUCT:
				//case OTHER:

				case NULL:
					throw new LoadingYamlException("Just setting null");

				case DATE:
				case TIME:
				case TIMESTAMP:
					return makeTime(targetType, (Date)JdutConstructor.this.yamlConstructors.get(Tag.TIMESTAMP).construct(node));

				default:
					throw new LoadingYamlException("Cannot recgonize tag or not supported. Vaue: \"%s\", JDBCType: [%s]", tagValue, targetType);
			}
		}
		private Object makeTime(JDBCType jdbcType, Date timeValue)
		{
			switch (jdbcType) {
				case DATE:
					return new java.sql.Date(timeValue.getTime());
				case TIME:
					return new java.sql.Time(timeValue.getTime());
				case TIMESTAMP:
					return timeValue;

				default:
					throw new LoadingYamlException("Unsupported time type: [%s]", jdbcType);
			}
		}
		private Object makeBinary(JDBCType jdbcType, byte[] binaryData)
		{
			switch (jdbcType) {
				case BINARY:
				case LONGVARBINARY:
				case VARBINARY:
					return binaryData;

				case BLOB:
					return JdbcTemplateFactory.buildSupplier(
						() -> dataSource.getConnection(),
						conn -> {
							Blob blob = conn.createBlob();
							blob.setBytes(1, binaryData);
							return blob;
						}
					).asSupplier().get();

				default:
					throw new LoadingYamlException("Unsupported binary type: [%s]", jdbcType);
			}
		}
		private Object makeString(JDBCType jdbcType, String value)
		{
			switch (jdbcType) {
				case CHAR:
				case LONGNVARCHAR:
				case LONGVARCHAR:
				case NCHAR:
				case NVARCHAR:
				case VARCHAR:
					return value;
				case CLOB:
					return JdbcTemplateFactory.buildSupplier(
						() -> dataSource.getConnection(),
						conn -> {
							Clob clob = conn.createClob();
							clob.setString(1, value);
							return clob;
						}
					).asSupplier().get();
				case NCLOB:
					return JdbcTemplateFactory.buildSupplier(
						() -> dataSource.getConnection(),
						conn -> {
							NClob nclob = conn.createNClob();
							nclob.setString(1, value);
							return nclob;
						}
					).asSupplier().get();
				case SQLXML:
					return JdbcTemplateFactory.buildSupplier(
						() -> dataSource.getConnection(),
						conn -> {
							SQLXML sqlXml = conn.createSQLXML();
							sqlXml.setString(value);
							return sqlXml;
						}
					).asSupplier().get();
				default:
					throw new LoadingYamlException("Unsupported text type: [%s]", jdbcType);
			}
		}
		private Object makeIntegerNumber(JDBCType jdbcType, Number integerValue)
		{
			switch (jdbcType) {
				case BIT:
				case TINYINT:
					return integerValue.byteValue();
				case SMALLINT:
					return integerValue.shortValue();
				case INTEGER:
					return integerValue.intValue();
				case BIGINT:
					return integerValue.longValue();
				default:
					throw new LoadingYamlException("Unsupported integer type: [%s]", jdbcType);
			}
		}
		private Object makeDecimalNumber(JDBCType jdbcType, Double doubleValue)
		{
			switch (jdbcType) {
				case FLOAT:
					return doubleValue.floatValue();
				case DOUBLE:
					return doubleValue;
				case NUMERIC:
				case DECIMAL:
				case REAL:
					return new BigDecimal(doubleValue);

				default:
					throw new LoadingYamlException("Unsupported decimal type: [%s]", jdbcType);
			}
		}
	}

	private class JdutConstruct extends AbstractConstruct {
		private JdutConstruct() {}

		@Override
		public Object construct(Node node)
		{
			String tagValue = node.getTag().getValue().replace(NAMESPACE_JDUT, "");

			switch (tagValue) {
				case "supplier":
					String supplierName =
						(String)JdutConstructor.this.yamlConstructors.get(Tag.STR).construct(node);

					logger.trace("Load supplier: \"{}\"", supplierName);
					return conductorConfig.getSupplier(supplierName).orElseThrow(
						() -> new LoadingYamlException("Cannot found supplier: \"%s\"", supplierName)
					);
				default:
					throw new LoadingYamlException("Cannot recgonize tag: \"%s\"", tagValue);
			}
		}
	}

	private class SqlConstruct extends AbstractConstruct {
		private SqlConstruct() {}

		@Override
		public Object construct(Node node)
		{
			String tagValue = node.getTag().getValue().replace(NAMESPACE_SQL, "");

			switch (tagValue) {
				case "table":
					return new TableNode.TableName(
						(String)JdutConstructor.this.yamlConstructors.get(Tag.STR).construct(node)
					);
				case "code":
					return new CodeNode(
						JdutConstructor.this.yamlConstructors.get(Tag.MAP).construct(node)
					);
				case "statement":
					String sql = (String)JdutConstructor.this.yamlConstructors.get(Tag.STR).construct(node);
					JdbcFunction<Connection, ?> statementFunc = conn -> JdbcTemplateFactory.buildSupplier(
						() -> conn.createStatement(),
						stat -> {
							logger.trace("Execute statement: [{}]", sql);
							return stat.executeUpdate(sql);
						}
					).getJdbc();

					return statementFunc;
				case "jdbcfunction":
					String functionName = (String)JdutConstructor.this.yamlConstructors.get(Tag.STR).construct(node);
					logger.trace("Load JDBC function: \"{}\"", functionName);
					return conductorConfig.getJdbcFunction(functionName).orElseThrow(
						() -> new LoadingYamlException("Cannot found JDBC function: \"%s\"", functionName)
					);
				default:
					throw new LoadingYamlException("Cannot recgonize tag: \"%s\"", tagValue);
			}
		}
	}
}

class DuetFunctionsImpleOfDoc implements DuetFunctions {
	private Boolean transactional = null;
	private Optional<Integer> transactionIsolation = null;

	final List<DuetFunctions> buildFunctions = new ArrayList<>(4);

	DuetFunctionsImpleOfDoc() {}

	void add(DuetFunctions duetFunctions)
	{
		buildFunctions.add(duetFunctions);
	}

	void setTransactional(boolean flag)
	{
		transactional = flag;
	}
	void setTransactionIsolation(Optional<Integer> newTransactionIsolation)
	{
		transactionIsolation = newTransactionIsolation;
	}

	@Override
	public JdbcFunction<Connection, ?> getBuildFunction()
	{
		JdbcFunction<Connection, ?> mainFunction = conn -> {
			buildFunctions.forEach(duetFunc -> duetFunc.getBuildFunction().asFunction().apply(conn));
			return buildFunctions.size();
		};

		return wrapTransactionIfPresent(mainFunction);
	}

	@Override
	public JdbcFunction<Connection, ?> getCleanFunction()
	{
		final List<DuetFunctions> cleanFunctions = new ArrayList<>(buildFunctions);
		Collections.reverse(cleanFunctions);

		JdbcFunction<Connection, ?> mainFunction = conn -> {
			cleanFunctions.forEach(duetFunc -> duetFunc.getCleanFunction().asFunction().apply(conn));
			return cleanFunctions.size();
		};

		return wrapTransactionIfPresent(mainFunction);
	}

	private JdbcFunction<Connection, ?> wrapTransactionIfPresent(JdbcFunction<Connection, ?> ordinaryFunction)
	{
		if (!transactional) {
			return ordinaryFunction;
		}

		if (transactionIsolation.isPresent()) {
			return ordinaryFunction.surroundedBy(new Transactional<>(transactionIsolation.get()));
		} else {
			return ordinaryFunction.surroundedBy(Transactional::simple);
		}
	}
}

class DuetConductorImplOfAssembly implements DuetConductor {
	private final DataConductor dataConductor;

	final List<DuetFunctions> buildFunctions;
	final List<DuetFunctions> cleanFunctions;

	DuetConductorImplOfAssembly(DataConductor newDataConductor, List<DuetFunctions> newFunctions)
	{
		buildFunctions = newFunctions;

		List<DuetFunctions> reversedFunctions = new ArrayList<>(buildFunctions);
		Collections.reverse(reversedFunctions);
		cleanFunctions = reversedFunctions;

		dataConductor = newDataConductor;
	}

	@Override
	public void build()
	{
		dataConductor.conduct(
			conn -> {
				buildFunctions.forEach(duetFunc -> duetFunc.getBuildFunction().asFunction().apply(conn));
				return buildFunctions.size();
			}
		);
	}

	@Override
	public void clean()
	{
		dataConductor.conduct(
			conn -> {
				cleanFunctions.forEach(duetFunc -> duetFunc.getCleanFunction().asFunction().apply(conn));
				return cleanFunctions.size();
			}
		);
	}
}
