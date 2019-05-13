package guru.mikelue.jdut.test;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import javax.sql.DataSource;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import guru.mikelue.jdut.annotation.AnnotationUtil;
import guru.mikelue.jdut.annotation.IfDatabaseVendor;
import guru.mikelue.jdut.decorate.DataGrainDecorator;
import guru.mikelue.jdut.decorate.TableSchemaLoadingDecorator;
import guru.mikelue.jdut.jdbc.JdbcRunnable;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.vendor.DatabaseVendor;

@TestInstance(Lifecycle.PER_CLASS)
@ExtendWith({
	AbstractDataSourceTestBase.IocContainer.class,
	AbstractDataSourceTestBase.DatabaseVendorCondition.class,
	AbstractDataSourceTestBase.LiquibaseSetup.class,
})
public abstract class AbstractDataSourceTestBase {
	private static AbstractDataSourceTestBase getBaseInstance(ExtensionContext junitContext)
	{
		return (AbstractDataSourceTestBase)junitContext.getRequiredTestInstance();
	}

	public static class IocContainer implements AfterAllCallback, TestInstancePostProcessor {
		private ConfigurableApplicationContext appContext;
		private DataGrainDecorator schemaLoading;

		private final Logger logger = LoggerFactory.getLogger(IocContainer.class);

		public IocContainer()
		{
			AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
			ctx.register(DataSourceContext.class);
			ctx.refresh();

			appContext = ctx;

			schemaLoading = new TableSchemaLoadingDecorator(
				ctx.getBean(DataSource.class)
			);
		}

		@Override
		public void afterAll(ExtensionContext junitContext) throws Exception
		{
			logger.info("Release Application Context: {}", DataSourceContext.class);

			appContext.close();
		}

		@Override
		public void postProcessTestInstance(Object instance, ExtensionContext junitContext) throws Exception
		{
			AbstractDataSourceTestBase typedBase = (AbstractDataSourceTestBase)instance;
			typedBase.appContext = appContext;
			typedBase.schemaLoading = schemaLoading;
		}
	}

	public static class DatabaseVendorCondition implements ExecutionCondition {
		private final Logger logger = LoggerFactory.getLogger(DatabaseVendorCondition.class);

		@Override
		public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
			/**
			 * Checks the @IfDatabaseVendor on class level
			 */
			Optional<Object> targetObject = context.getTestInstance();
			if (targetObject.isPresent() && needSkip(context, targetObject.get().getClass())) {
				return ConditionEvaluationResult.disabled("Disabled by not matched database vendor(on class level)");
			}
			// :~)

			/**
			 * Checks the @IfDatabaseVendor on method level
			 */
			Optional<Method> targetMethod = context.getTestMethod();
			if (targetMethod.isPresent() && needSkip(context, targetMethod.get())) {
				return ConditionEvaluationResult.disabled("Disabled by not matched database vendor(on method level)");
			}
			// :~)

			return ConditionEvaluationResult.enabled("Enabled by database vendor or no vendor requirement");
		}

		private boolean needSkip(ExtensionContext junitContext, AnnotatedElement annotatedEle)
		{
			Optional<IfDatabaseVendor> requiredVendors = AnnotationSupport.findAnnotation(annotatedEle, IfDatabaseVendor.class);
			if (!requiredVendors.isPresent()) {
				return false;
			}

			DatabaseVendor currentVendor = getBaseInstance(junitContext).getCurrentVendor();

			boolean matchDatabaseVendor = AnnotationUtil.matchDatabaseVendor(currentVendor, requiredVendors.get());
			if (!matchDatabaseVendor) {
				logger.debug("The needed vendor of database [{}] is not matched. Current vendor: [{}]", requiredVendors.get(), currentVendor.name());
			}

			return !matchDatabaseVendor;
		}
	}

	public static class LiquibaseSetup implements BeforeEachCallback, AfterEachCallback {
		private final Logger logger = LoggerFactory.getLogger(LiquibaseSetup.class);

		@Override
		public void beforeEach(ExtensionContext junitContext) throws Exception
		{
			Method testingMethod = junitContext.getRequiredTestMethod();
			Optional<DoLiquibase> performLiquibase = AnnotationSupport.findAnnotation(testingMethod, DoLiquibase.class);

			performLiquibase.ifPresent(
				liquibaseSetting -> {
					if (liquibaseSetting.update()) {
						updateLiquibase(testingMethod, getDataSource(junitContext));
					}
				}
			);
		}

		@Override
		public void afterEach(ExtensionContext junitContext) throws Exception
		{
			Method testingMethod = junitContext.getRequiredTestMethod();

			Optional<DoLiquibase> performLiquibase = AnnotationSupport.findAnnotation(testingMethod, DoLiquibase.class);

			performLiquibase.ifPresent(
				liquibaseSetting -> {
					if (liquibaseSetting.rollback()) {
						rollbackLiquibase(testingMethod, getDataSource(junitContext));
					}
				}
			);
		}

		private DataSource getDataSource(ExtensionContext junitContext)
		{
			return getBaseInstance(junitContext).getDataSource();
		}

		/**
		 * Updates the database by change log of {@code <package_path>/ClassName.xml} and {@link LabelExpression label} of method name.
		 *
		 * @param method The method of tested
		 */
		private void updateLiquibase(Method method, DataSource dataSource)
		{
			liquibaseExecutor(
				getFileNameOfChangeSet(method),
				liquibase -> {
					logger.info("Update with label: \"{}\"", method.getName());
					liquibase.update(
						new Contexts(), new LabelExpression(method.getName())
					);
				},
				dataSource
			);
		}
		/**
		 * Rollbacks(8 change set) the database by change log of {@code <package_path>/ClassName.xml} and {@link LabelExpression label} of method name.
		 *
		 * @param method The method of tested
		 */
		private void rollbackLiquibase(Method method, DataSource dataSource)
		{
			liquibaseExecutor(
				getFileNameOfChangeSet(method),
				liquibase -> {
					logger.info("Rollback with label: \"{}\"", method.getName());
					liquibase.rollback(
						8, new Contexts(), new LabelExpression(method.getName())
					);
				},
				dataSource
			);
		}

		private void liquibaseExecutor(String fileNameOfChangeLog, LiquibaseConsumer liquibaseConsumer, DataSource dataSource)
		{
			JdbcRunnable executeLiquibase = JdbcTemplateFactory.buildRunnable(
				() -> dataSource.getConnection(),
				conn -> {
					Liquibase liquibase = null;
					try {
						liquibase = new Liquibase(
							fileNameOfChangeLog, new ClassLoaderResourceAccessor(),
							DatabaseFactory.getInstance().findCorrectDatabaseImplementation(
								new JdbcConnection(conn)
							)
						);
						if (liquibase.listLocks().length > 0) {
							liquibase.forceReleaseLocks();
						}
						logger.info("[Liquibase] Change log: \"{}\"", fileNameOfChangeLog);

						liquibase.setChangeLogParameter("testClassName", getClass().getName());

						liquibaseConsumer.accept(liquibase);
					} catch (LiquibaseException e) {
						throw new RuntimeException(e);
					}
				}
			);

			executeLiquibase.asRunnable().run();
		}

		private String getFileNameOfChangeSet(Method testingMethod)
		{
			return String.format("%s.xml", testingMethod.getDeclaringClass().getName().replace(".", "/"));
		}
	}

	private Logger logger = LoggerFactory.getLogger(getClass());

	private ApplicationContext appContext;
	private DataGrainDecorator schemaLoading;

	protected AbstractDataSourceTestBase() {}

	protected DatabaseVendor getCurrentVendor()
	{
		return appContext.getBean(DatabaseVendor.class);
	}

	protected DataSource getDataSource()
	{
		return appContext.getBean(DataSource.class);
	}

	/**
	 * @return the schemaLoading for current database
	 */
	protected DataGrainDecorator getSchemaLoading() {
		return schemaLoading;
	}

	/**
	 * @return the logger with name of final class
	 */
	protected Logger getLogger()
	{
		return logger;
	}
}

@FunctionalInterface
interface LiquibaseConsumer {
	void accept(Liquibase liquibase) throws LiquibaseException;
}
