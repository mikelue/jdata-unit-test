package guru.mikelue.jdut.testng;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang3.Validate;
import org.testng.IAttributes;

import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

/**
 * Defines the basic, overridable method for constructing listeners.<br>
 *
 * <p>This listener uses {@link ConcurrentHashMap} for building mapping between {@link IAttributes} and {@link DuetConductor},
 * so you should use the implementation of this listener in multi-thread environment with discretion.</p>
 *
 * <h3>Main methods</h3>
 * <ul>
 * 	<li>{@link #buildDuetConductor} - Client must be implementing this method</li>
 * 	<li>{@link #buildDataSource} - Gets/build data source</li>
 * 	<li>{@link #buildYamlConductorFactory} - Gets/builder singleton instance for {@link YamlConductorFactory}</li>
 * </ul>
 *
 * @see ISuiteYamlFactoryListener
 * @see ITestContextYamlFactoryListener
 * @see IInvokedMethodYamlFactoryListener
 */
public abstract class YamlFactoryListenerBase {
	private Map<IAttributes, Optional<DuetConductor>> conductors = new ConcurrentHashMap<>(4);

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
		staticLogger.debug("Set data source[{}] to IAttributes: [{}]", dataSource, attributes);
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
		DataSource dataSource = (DataSource)attributes.getAttribute(DATA_SOURCE);
		Validate.notNull(dataSource, "Cannot find data source");

		return dataSource;
	}
	/**
	 * Removes the data source from attribute
	 *
	 * @param attributes The attribute may contain data source
	 */
	public static void removeDataSource(IAttributes attributes)
	{
		staticLogger.debug("Remove data source from IAttributes: [{}]", attributes);
		attributes.removeAttribute(DATA_SOURCE);
	}

	protected YamlFactoryListenerBase() {}

	/**
	 * Gets data source by attribute.
	 *
	 * @param attributes The attributes object
	 *
	 * @return The initialized data source put from other place
	 *
	 * @see #setDataSource
	 * @see #getDataSource
	 */
	protected DataSource buildDataSource(IAttributes attributes)
	{
		return getDataSource(attributes);
	}

	/**
	 * Uses {@link #buildDataSource} to build {@link YamlConductorFactory} or return a single instance of it.
	 *
	 * @param testResult This object contains both testing method and context
	 *
	 * @return The singleton factory of YAML
	 */
	private YamlConductorFactory yamlFactory = null;
	protected YamlConductorFactory buildYamlConductorFactory(IAttributes attributes)
	{
		if (yamlFactory == null) {
			logger.debug("Builds YamlConductorFactory by default implementation. IAttributes: [{}]", attributes);
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
			workingConductor -> workingConductor.build()
		);

		logger.debug("Puts DuetConductor[{}] to IAttributes: [{}]", conductor, attributes);

		conductors.put(attributes, conductor);
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
		if (!conductors.containsKey(attributes)) {
			return;
		}

		Optional<DuetConductor> conductor = conductors.get(attributes);

		try {
			conductor.ifPresent(
				workingConductor -> workingConductor.clean()
			);
		} finally {
			logger.debug("Remove DuetConductor[{}] from IAttributes: [{}]", conductor, attributes);
			conductors.remove(attributes);
		}
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
}
