package guru.mikelue.jdut.junit4;

import java.io.Reader;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.sql.DataSource;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.ConductorConfig;
import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.annotation.JdutResourceNaming;
import guru.mikelue.jdut.yaml.ReaderFunctions;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

/**
 * This {@link TestRule} is used to construct the data conduction by {@link YamlConductorFactory}.
 *
 * <p>Since {@link TestRule} would initialize new instance of rule for every test(method),
 * this implementation for {@link YamlConductorFactory} is set by constructor of this object.</p>
 *
 * <p>This rule could be used with {@link Rule} or {@link ClassRule}.</p>
 *
 * <pre><code class="java">
 * &#64;ClassRule
 * public final static TestRule dataConductor = JdutYamlFactory.buildByDataSource(DataSourceGeter::get);
 * </code></pre>
 *
 * @see #buildDuetConductor
 */
public class JdutYamlFactory implements TestRule {
	private Logger logger = LoggerFactory.getLogger(JdutYamlFactory.class);

	private final YamlConductorFactory yamlFactory;

	/**
	 * Constructs this object by {@link Supplier} of {@link DataSource}.
	 *
	 * @param supplier Provides {@link DataSource}
	 *
	 * @return new object
	 */
	public static JdutYamlFactory buildByDataSource(Supplier<DataSource> supplier)
	{
		return new JdutYamlFactory(supplier.get());
	}

	/**
	 * Constructs this object by {@link Supplier} of {@link YamlConductorFactory}.
	 *
	 * @param supplier Provides {@link YamlConductorFactory}
	 *
	 * @return new object
	 */
	public static JdutYamlFactory buildByFactory(Supplier<YamlConductorFactory> supplier)
	{
		return new JdutYamlFactory(supplier.get());
	}

	/**
	 * Constructs a builder for {@link ConductorConfig} by default convention for resource loading(from testing class).
	 *
	 * @param desc The test information provided by JUnit 4
	 *
	 * @return new builder
	 */
	public static Consumer<ConductorConfig.Builder> defaultBuilderOfConductorConfig(Description desc)
	{
		Class<?> targetClass = desc.getTestClass();
		Function<String, Reader> readerByClass = ReaderFunctions.loadByClass(targetClass);

		return builder -> builder.resourceLoader(readerByClass);
	}

	/**
	 * Constructs this object by data source.
	 *
	 * @param dataSource The object of data source
	 *
	 * @see #JdutYamlFactory(YamlConductorFactory)
	 */
	public JdutYamlFactory(DataSource dataSource)
	{
		this(YamlConductorFactory.build(dataSource));
	}
	/**
	 * Constructs this object by a instance of {@link YamlConductorFactory}.
	 *
	 * @param yamlConductorFacotry The instance of factory for conducting data
	 */
	public JdutYamlFactory(YamlConductorFactory yamlConductorFacotry)
	{
		this.yamlFactory = yamlConductorFacotry;
	}

	/**
	 * This method would check the {@link JdutResource} on <b>test method</b>,
	 * if conditions match, the conduction of data would be triggered.
	 *
	 * <p>This method would call {@link #buildDuetConductor} to build {@link DuetConductor} if the conditions is matched.</p>
	 *
	 * @param base The original test to be executed
	 * @param description The context of test
	 *
	 * @return The statement with data conduction
	 */
	@Override
	public Statement apply(Statement base, Description description)
	{
		if (!needConductData(description)) {
			logger.debug("The need of conduct data is false");
			return base;
		}

		JdutResource jdutResource = description.getAnnotation(JdutResource.class);
		if (jdutResource == null) {
			return base;
		}

		return new Statement() {
			private DuetConductor duetConductor = buildDuetConductor(description);

			@Override
			public void evaluate() throws Throwable
			{
				try {
					duetConductor.build();

					base.evaluate();
				} finally {
					duetConductor.clean();
					duetConductor = null;
				}
			}
		};
	}

	/**
	 * Checks if the test should conduct data.
	 *
	 * @param description The context of test
	 *
	 * @return true if the conduction should be executed
	 */
	protected boolean needConductData(Description description)
	{
		return true;
	}

	/**
	 * Builds {@link DuetConductor}({@link Optional}) by default convention.
	 *
	 * <ul>
	 * 	<li>When used by {@link Rule}, the naming is <code style="color:blue">{@literal <class_name>-<method_name>.yaml}</code>).</li>
	 * 	<li>When used by {@link ClassRule}, the naming is <code style="color:blue">{@literal <class_name>.yaml}</code>).</li>
	 * </ul>
	 *
	 * <p>This method is called by {@link #apply apply} if the method is annotated {@link JdutResource}.</p>
	 *
	 * @param description the description of test
	 *
	 * @return The initialized conductor for testing
	 */
	protected DuetConductor buildDuetConductor(Description description)
	{
		logger.debug(
			"Build conductor. Class: \"{}\". Method: \"{}\"",
			description.getClassName(), description.getMethodName()
		);

		Class<?> testClass = description.getTestClass();
		Consumer<ConductorConfig.Builder> config = defaultBuilderOfConductorConfig(description);

		/**
		 * This method is used for @ClassRule
		 */
		if (description.getMethodName() == null) {
			return yamlFactory.conductResource(
				JdutResourceNaming.naming("{1}", testClass, ".yaml"), config
			);
		}
		// :~)

		return yamlFactory.conductResource(
			String.format("%s-%s.yaml", testClass.getSimpleName(), description.getMethodName()),
			config
		);
	}
}
