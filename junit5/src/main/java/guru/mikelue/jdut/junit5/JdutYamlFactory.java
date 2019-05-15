package guru.mikelue.jdut.junit5;

import java.io.Reader;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.ConductorConfig;
import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.annotation.JdutResourceNaming;
import guru.mikelue.jdut.yaml.ReaderFunctions;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

/**
 * This factory sets up loading convention of <a href="https://yaml.org/">YAML</a> file,
 * in order to prepare and to clean up data for testing.
 *
 * <p>This class can be used in {@link org.junit.jupiter.api.extension.ExtendWith} or JUnit 5 <a href="https://junit.org/junit5/docs/current/user-guide/#extensions">extension mechanism</a>.
 * And <em>writing a class inheriting this one</em> to apply the conventions of loading YAML file.
 * </p>
 *
 * <p>To use this class, you should implement {@link #getYamlConductorFactory} method to set-up
 * {@link DataSource} and related affairs of your test data.
 * </p>
 *
 * <p>By {@link BeforeEachCallback} and {@link AfterAllCallback},
 * a testing class loads and cleans up data while a {@link JdutResource} is applied on it.
 * By {@link BeforeEachCallback} and {@link AfterEachCallback},
 * a testing method loads and cleans up data while a {@link JdutResource} is applied on it.</p>
 *
 * Example of your implementation:
 * <pre><code class="java">
 * public class YourFactory extends JdutYamlFactory {
 *     &#64;Override
 *     protected YamlConductorFactory getYamlConductorFactory(ExtensionContext context, Event event)
 *     {
 *         // Your implementation
 *         return null;
 *     }
 * }
 * </code></pre>
 *
 * Usage on your testing class:
 * <pre><code class="java">
 * package guru.mikelue.nova;
 *
 * // Loading: classpath:guru/mikelue/nova/Hydrogen.yaml
 * &#64;JdutResource
 * &#64;ExtendWith(YourFactory.class)
 * public class Hydrogen {
 *      // Loading: classpath:guru/mikelue/nova/Hydrogen-explode.yaml
 *     &#64;Test
 *     &#64;JdutResource
 *     void explode()
 *     {
 *     }
 * }
 * </code></pre>
 *
 * @see #buildDuetConductor
 * @see #needConductData
 */
public abstract class JdutYamlFactory implements BeforeAllCallback, AfterAllCallback,
	BeforeEachCallback, AfterEachCallback {

	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * Builds a new object by {@link Supplier} of {@link YamlConductorFactory}.
	 *
	 * @param supplier The functional interface of supplying {@link YamlConductorFactory}
	 *
	 * @return new object
	 */
	public static JdutYamlFactory buildByFactory(Supplier<YamlConductorFactory> supplier)
	{
		return new JdutYamlFactory() {
			@Override
			protected YamlConductorFactory getYamlConductorFactory(ExtensionContext context, Event event)
			{
				return supplier.get();
			}
		};
	}

	/**
	 * Builds a new object by {@link Supplier} of {@link DataSource}.
	 *
	 * @param supplier The functional interface of supplying {@link DataSource}
	 *
	 * @return new object
	 */
	public static JdutYamlFactory buildByDataSource(Supplier<DataSource> supplier)
	{
		return new JdutYamlFactory() {
			@Override
			protected YamlConductorFactory getYamlConductorFactory(ExtensionContext context, Event event)
			{
				return YamlConductorFactory.build(supplier.get());
			}
		};
	}

	/**
	 * Constructs a builder for {@link ConductorConfig} by default convention for resource loading(from testing class).
	 *
	 * @param context The context provided by JUnit 5
	 *
	 * @return new builder
	 */
	public static Consumer<ConductorConfig.Builder> defaultBuilderOfConductorConfig(ExtensionContext context)
	{
		Class<?> targetClass = context.getRequiredTestClass();
		Function<String, Reader> readerByClass = ReaderFunctions.loadByClass(targetClass);

		return builder -> builder.resourceLoader(readerByClass);
	}

	/**
	 * Defins the class-level or method-level event of loading test data.
	 */
	public enum Event {
		/**
		 * MethodLevel-level of loading test data.
		 *
		 * The resource URL would be: <strong>classpath:{package}/{classname}-{methodname}.yaml</strong>.
		 */
		MethodLevel(new ConductHandlerByMethod()),
		/**
		 * Class-level of loading test data.
		 *
		 * The resource URL would be: <strong>classpath:{package}/{classname}.yaml</strong>.
		 */
		ClassLevel(new ConductHandlerByClass());

		private final ConductHandler handler;

		private Event(ConductHandler newHandler) {
			this.handler = newHandler;
		}

		/**
		 * Retrieves target element({@link Class} or {@link MethodLevel}) from {@link ExtensionContext}.
		 *
		 * @param context The context provided by JUnit 5
		 *
		 * @return target element
		 */
		public AnnotatedElement getTargetElement(ExtensionContext context)
		{
			return handler.getTargetElement(context);
		}
		/**
		 * Retrieves URL for resource(YAML file) by context and current event.
		 *
		 * @param context The context provided by JUnit 5
		 *
		 * @return target element
		 */
		public String getResourceUrl(ExtensionContext context)
		{
			return handler.getResourceLocation(context);
		}
	};

	/**
	 * The unified usage of storage for {@link DuetConductor}.
	 */
	private static class StoreUsage {
		private final ExtensionContext context;
		private final Event event;

		private final static String NAME_DUET_CONDUCTOR = "duet_conductor";

		StoreUsage(ExtensionContext newContext, Event newEvent)
		{
			context = newContext;
			event = newEvent;
		}
		Optional<DuetConductor> removeDuetConductor()
		{
			return Optional.ofNullable(
				getStore().remove(NAME_DUET_CONDUCTOR, DuetConductor.class)
			);
		}
		void setDuetConductor(DuetConductor newConductor)
		{
			getStore().put(NAME_DUET_CONDUCTOR, newConductor);
		}

		private Store getStore()
		{
			return context.getStore(Namespace.create(event.getTargetElement(context)));
		}
	}

	/**
	 * This class can only be used by implementation of subclass.
	 */
	protected JdutYamlFactory() {}

	/**
	 * Checks if {@link JdutResource} is on testing class.
	 *
	 * If it is, loading the YAML file from <em>classpaht:{package}/{classname}.yaml</em>.
	 *
	 * For example:
	 * <pre><code class="java">
	 * package guru.mikelue.cassia;
	 *
	 * // Loading: classpath:guru/mikelue/cassia/Verum.yaml
	 * &#64;JdutResource
	 * public class Verum {
	 * }
	 * </code></pre>
	 *
	 * <p>This callback would put the {@link DuetConductor} in class-{@link Namespace} {@link Store}.
	 * The {@link DuetConductor} is constructed from {@link #getYamlConductorFactory}.
	 * </p>
	 *
	 * @param context The context provided by JUnit 5
	 *
	 * @throws Exception See {@link BeforeAllCallback#beforeAll}
	 */
	@Override
	public void beforeAll(ExtensionContext context) throws Exception
	{
		buildConductorByNeeded(context, Event.ClassLevel).ifPresent(
			conductor -> {
				new StoreUsage(context, Event.ClassLevel).setDuetConductor(conductor);
				conductor.build();
			}
		);
	}
	/**
	 * Checks if any {@link DuetConductor} in {@link Store}.
	 *
	 * If it is, use the conductor to clean up data.
	 *
	 * For example:
	 * <pre><code class="java">
	 * package guru.mikelue.cassia;
	 *
	 * // Loading: classpath:guru/mikelue/cassia/Verum.yaml
	 * &#64;JdutResource
	 * public class Verum {
	 * }
	 * </code></pre>
	 *
	 * <p>This callback would remove the {@link DuetConductor} in class-{@link Namespace} {@link Store}.</p>
	 *
	 * @param context The context provided by JUnit 5
	 *
	 * @throws Exception See {@link BeforeAllCallback#beforeAll}
	 */
	@Override
	public void afterAll(ExtensionContext context) throws Exception
	{
		new StoreUsage(context, Event.ClassLevel).removeDuetConductor()
			.ifPresent(conductor -> conductor.clean());
	}

	/**
	 * Checks if {@link JdutResource} is on testing method.
	 *
	 * If it is, loading the YAML file from <em>classpaht:{package}/{classname}-{methodname}.yaml</em>.
	 *
	 * For example:
	 * <pre><code class="java">
	 * package guru.mikelue.cassia;
	 *
	 * public class Verum {
	 *     // Loading: classpath:guru/mikelue/cassia/Verum-grind.yaml
	 *     &#64;Test &#64;JdutResource
	 *     void grind()
	 *     {
	 *     }
	 * }
	 * </code></pre>
	 *
	 * <p>This callback would put the {@link DuetConductor} in method-{@link Namespace} {@link Store}.
	 * The {@link DuetConductor} is constructed from {@link #getYamlConductorFactory}.
	 * </p>
	 *
	 * @param context The context provided by JUnit 5
	 *
	 * @throws Exception See {@link BeforeAllCallback#beforeAll}
	 */
	@Override
	public void beforeEach(ExtensionContext context) throws Exception
	{
		buildConductorByNeeded(context, Event.MethodLevel).ifPresent(
			conductor -> {
				new StoreUsage(context, Event.MethodLevel).setDuetConductor(conductor);
				conductor.build();
			}
		);
	}
	/**
	 * Checks if any {@link DuetConductor} in {@link Store}.
	 *
	 * If it is, use the conductor to clean up data.
	 *
	 * For example:
	 * <pre><code class="java">
	 * package guru.mikelue.cassia;
	 *
	 * public class Verum {
	 *     // Loading: classpath:guru/mikelue/cassia/Verum-grind.yaml
	 *     &#64;Test &#64;JdutResource
	 *     void grind()
	 *     {
	 *     }
	 * }
	 * </code></pre>
	 *
	 * <p>This callback would remove the {@link DuetConductor} in method-{@link Namespace} {@link Store}.</p>
	 *
	 * @param context The context provided by JUnit 5
	 *
	 * @throws Exception See {@link BeforeAllCallback#beforeAll}
	 */
	@Override
	public void afterEach(ExtensionContext context) throws Exception
	{
		new StoreUsage(context, Event.MethodLevel).removeDuetConductor()
			.ifPresent(conductor -> conductor.clean());
	}

	protected abstract YamlConductorFactory getYamlConductorFactory(ExtensionContext context, Event event);

	/**
	 * The retrieved {@link Logger} has the name of implementing class.
	 *
	 * @return A logger having the name of implementing class.
	 */
	final protected Logger getLogger()
	{
		return logger;
	}

	/**
	 * Checks if the test should conduct data.
	 *
	 * You can override this method to control whether or not to conduct data for testing.
	 *
	 * @param context The context provided by JUnit 5
	 * @param event The event for scope of current triggered.
	 *
	 * @return true if the conduction should be executed
	 */
	protected boolean needConductData(ExtensionContext context, Event event)
	{
		return true;
	}

	/**
	 * Builds the {@link DuetConductor} by default convention({@link #defaultBuilderOfConductorConfig}).
	 *
	 * You can override this method to customize your own arsenal of test data.
	 *
	 * @param context The context provided by JUnit 5
	 * @param event The event for scope of current triggered.
	 *
	 * @return The initialized conductor for testing
	 */
	protected DuetConductor buildDuetConductor(ExtensionContext context, Event event)
	{
		return getYamlConductorFactory(context, event).conductResource(
			event.getResourceUrl(context),
			defaultBuilderOfConductorConfig(context)
		);
	}

	private Optional<DuetConductor> buildConductorByNeeded(ExtensionContext context, Event event)
	{
		if (!needConductData(context, event)) {
			return Optional.<DuetConductor>empty();
		}

		Optional<JdutResource> jdutResource = Optional.ofNullable(
			event.getTargetElement(context).getAnnotation(JdutResource.class)
		);
		if (!jdutResource.isPresent()) {
			return Optional.<DuetConductor>empty();
		}

		return Optional.of(buildDuetConductor(context, event));
	}
}

interface ConductHandler {
	AnnotatedElement getTargetElement(ExtensionContext context);
	String getResourceLocation(ExtensionContext context);
}

final class ConductHandlerByClass implements ConductHandler {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public String getResourceLocation(ExtensionContext context)
	{
		Class<?> testingClass = context.getRequiredTestClass();

		String resourceLocation = JdutResourceNaming.naming("{1}", testingClass, ".yaml");
		logger.debug("YAML resource location: {}", resourceLocation);

		return resourceLocation;
	}
	@Override
	public AnnotatedElement getTargetElement(ExtensionContext context)
	{
		return context.getRequiredTestClass();
	}
}

final class ConductHandlerByMethod implements ConductHandler {
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public String getResourceLocation(ExtensionContext context)
	{
		Class<?> testingClass = context.getRequiredTestInstance().getClass();
		Method targetMethod = context.getRequiredTestMethod();

		String resourceLocation = String.format("%s-%s.yaml", testingClass.getSimpleName(), targetMethod.getName());
		logger.debug("YAML resource location: {}", resourceLocation);

		return resourceLocation;
	}
	@Override
	public AnnotatedElement getTargetElement(ExtensionContext context)
	{
		return context.getRequiredTestMethod();
	}
}
