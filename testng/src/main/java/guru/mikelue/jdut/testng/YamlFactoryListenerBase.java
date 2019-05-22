package guru.mikelue.jdut.testng;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IAttributes;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

/**
 * Defines the basic, overridable method for constructing listeners.<br>
 *
 * <p>This listener uses {@link ConcurrentHashMap} for building mapping between {@link IAttributes} and {@link DuetConductor},
 * so you should use the implementation of this listener in multi-thread environment with discretion.</p>
 *
 * The value of <strong>hash key</strong> on {@link IAttributes}:
 * <ul>
 *   <li>For {@link ITestContext} - Use suite name and test name</li>
 *   <li>For {@link ISuite} - Use suite name</li>
 *   <li>For {@link ITestResult} - Use canonical class name and method name</li>
 * </ul>
 *
 * <h3>Main methods</h3>
 * <ul>
 * 	<li>{@link #buildDuetConductor} - Client must be implementing this method</li>
 * 	<li>{@link #buildYamlConductorFactory} - Gets/builder singleton instance for {@link YamlConductorFactory}</li>
 * </ul>
 *
 * @see ISuiteYamlFactoryListener
 * @see ITestContextYamlFactoryListener
 * @see IInvokedMethodYamlFactoryListener
 */
public abstract class YamlFactoryListenerBase {
	private Map<Integer, Optional<DuetConductor>> conductors = new ConcurrentHashMap<>(4);

	private Logger logger = LoggerFactory.getLogger(getClass());
	private static Logger staticLogger = LoggerFactory.getLogger(YamlFactoryListenerBase.class);

	private final static String DATA_SOURCE = "_jdut_data_source_";

	/**
	 * Sets the attribute for data source.
	 *
	 * @param attributes The attributes to be set
	 * @param dataSource The initialized data source
	 */
	public static void setDataSource(IAttributes attributes, DataSource dataSource)
	{
		if (staticLogger.isDebugEnabled()) {
			staticLogger.debug("Set data source[{}] to: [{}]", dataSource, formatIAttritubes(attributes));
		}
		attributes.setAttribute(DATA_SOURCE, dataSource);
	}
	/**
	 * Gets the data source from attribute
	 *
	 * @param attributes The attribute may contain data source
	 *
	 * @return The put data source
	 */
	public static DataSource getDataSource(IAttributes attributes)
	{
		if (staticLogger.isDebugEnabled()) {
			staticLogger.debug("Find data source from: {}", formatIAttritubes(attributes));
		}
		DataSource dataSource = (DataSource)attributes.getAttribute(DATA_SOURCE);
		Validate.notNull(dataSource, "Cannot find data source");

		return dataSource;
	}
	/**
	 * Removes the data source from attribute
	 *
	 * @param attributes The attribute may contain data source
	 *
	 * @return The data source put by {@link #setDataSource}
	 */
	public static DataSource removeDataSource(IAttributes attributes)
	{
		if (staticLogger.isDebugEnabled()) {
			staticLogger.debug("Remove data source from: [{}]", formatIAttritubes(attributes));
		}
		return (DataSource)attributes.removeAttribute(DATA_SOURCE);
	}

	protected YamlFactoryListenerBase() {}

	/**
	 * Uses {@link #getDataSource} to build {@link YamlConductorFactory} or return a single instance of it.
	 *
	 * @param testResult This object contains both testing method and context
	 *
	 * @return The singleton factory of YAML
	 */
	private YamlConductorFactory yamlFactory = null;
	protected YamlConductorFactory buildYamlConductorFactory(IAttributes attributes)
	{
		if (yamlFactory == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Builds YamlConductorFactory by default implementation. Data source from: [{}]",
					formatIAttritubes(attributes));
			}
			yamlFactory = YamlConductorFactory.build(
				getDataSource(attributes)
			);
		}

		return yamlFactory;
	}

	/**
	 * By {@link #buildDuetConductor}, executes {@link DuetConductor#build}.<br>
	 *
	 * The conductor would be a new one from {@link #buildDuetConductor}, which would be put into fed {@link IAttributes}.
	 *
	 * @param attributes The attribute object of TestNG
	 */
	protected final void duetConductorBuild(IAttributes attributes)
	{
		Optional<DuetConductor> conductor = buildDuetConductor(attributes);
		conductor.ifPresent(
			workingConductor -> {
				if (logger.isDebugEnabled()) {
					logger.debug("Build data for: {}", formatIAttritubes(attributes));
				}
				workingConductor.build();
			}
		);

		logger.debug("Puts DuetConductor to it");

		conductors.put(buildContextKey(attributes), conductor);
	}
	/**
	 * Executes {@link DuetConductor#clean} keep in {@link IAttributes}, which is saved by {@link #duetConductorBuild}.<br>
	 *
	 * The conductor would be cleaned.
	 *
	 * @param attributes The attribute object of TestNG
	 */
	protected final void duetConductorClean(IAttributes attributes)
	{
		int contextKey = buildContextKey(attributes) ;

		if (!conductors.containsKey(contextKey)) {
			logger.warn("No conductor in attribute: {}", formatIAttritubes(attributes));
			return;
		}

		Optional<DuetConductor> conductor = conductors.get(contextKey);

		conductor.ifPresent(
			workingConductor -> {
				if (logger.isDebugEnabled()) {
					logger.debug("Clean data for: {}", formatIAttritubes(attributes));
				}
				workingConductor.clean();
			}
		);

		conductors.remove(contextKey);
		logger.debug("Remove DuetConductor from it");
	}

	/**
	 * Gets the logger with name of implementing class.
	 *
	 * @return The logger
	 */
	protected Logger getLogger()
	{
		return logger;
	}

	/**
	 * Builds {@link DuetConductor}(as {@link Optional}), client should implements this method.
	 *
	 * @param attributes The attribute object used for retrieve testing environment
	 *
	 * @return The initialized conductor for testing
	 */
	protected abstract Optional<DuetConductor> buildDuetConductor(IAttributes attributes);

	private static int buildContextKey(IAttributes attr)
	{
		if (ITestContext.class.isInstance(attr)) {
			ITestContext context = (ITestContext)attr;
			return new HashCodeBuilder()
				.append(context.getSuite().getName())
				.append(context.getName())
				.toHashCode();
		} else if (ISuite.class.isInstance(attr)) {
			ISuite suite = (ISuite)attr;
			return new HashCodeBuilder()
				.append(suite.getName())
				.toHashCode();
		} else if (ITestResult.class.isInstance(attr)) {
			ITestNGMethod method = ((ITestResult)attr).getMethod();

			return new HashCodeBuilder()
				.append(method.getRealClass().getCanonicalName())
				.append(method.getMethodName())
				.toHashCode();
		}

		throw new IllegalArgumentException(String.format("Cannot figure out the HASH CODE of type: %s", attr.getClass()));
	}
	private static String formatIAttritubes(IAttributes attr)
	{
		if (ITestContext.class.isInstance(attr)) {
			ITestContext context = (ITestContext)attr;
			return String.format(
				"ITestContext >> Suite: [%s]. Test [%s].",
				context.getSuite().getName(),
				context.getName()
			);
		} else if (ISuite.class.isInstance(attr)) {
			ISuite suite = (ISuite)attr;
			return String.format(
				"ISuite >> Suite: [%s].", suite.getName()
			);
		} else if (ITestResult.class.isInstance(attr)) {
			ITestResult result = (ITestResult)attr;
			ITestNGMethod method = result.getMethod();
			ITestContext context = result.getTestContext();

			return String.format(
				"ITestResult >> Suite: [%s]. Test: [%s]. Class.Method: [%s].[%s]",
				context.getSuite().getName(), context.getName(),
				method.getRealClass().getSimpleName(),
				method.getMethodName()
			);
		}

		return attr.toString();
	}
}
