package guru.mikelue.jdut.test;

import java.lang.reflect.Method;
import javax.sql.DataSource;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.internal.ConstructorOrMethod;
import org.testng.internal.TestNGMethod;

import guru.mikelue.jdut.annotation.AnnotationUtil;
import guru.mikelue.jdut.annotation.IfDatabaseVendor;
import guru.mikelue.jdut.decorate.DataGrainDecorator;
import guru.mikelue.jdut.decorate.TableSchemaLoadingDecorator;
import guru.mikelue.jdut.jdbc.JdbcRunnable;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.vendor.DatabaseVendor;

@Listeners({AbstractDataSourceTestBase.IfDatabaseVendorConfigurableListener.class, AbstractDataSourceTestBase.IfDatabaseVendorTestListener.class})
@Test(testName="DataSourceTest", groups="DataSourceGroup")
public abstract class AbstractDataSourceTestBase {
	/**
	 * Used to skip configurations
	 */
	public static class IfDatabaseVendorConfigurableListener implements IConfigurable {
		private Logger logger = LoggerFactory.getLogger(IfDatabaseVendorConfigurableListener.class);

		@Override
		public void run(IConfigureCallBack callBack, ITestResult testResult)
		{
			ITestNGMethod targetMethod = testResult.getMethod();
			if (!needSkip(targetMethod)) {
				callBack.runConfigurationMethod(testResult);
			}

			logger.debug("Skip configuration [{}].", targetMethod.getMethodName());
		}
	}
	/**
	 * Used to skip tests
	 */
	public static class IfDatabaseVendorTestListener implements IInvokedMethodListener {
		private Logger logger = LoggerFactory.getLogger(IfDatabaseVendorTestListener.class);

		public IfDatabaseVendorTestListener() {}

		@Override
		public void beforeInvocation(IInvokedMethod method, ITestResult testResult)
		{
			if (!method.isTestMethod()) {
				return;
			}

			ITestNGMethod targetMethod = method.getTestMethod();
			if (needSkip(targetMethod)) {
				logger.debug("Skip test [{}].", targetMethod.getMethodName());
				throw new SkipException("Skip test because the vendor of database is not matched.");
			}
		}

		@Override
		public void afterInvocation(IInvokedMethod method, ITestResult testResult) {}
	}

	private static Logger mainLogger = LoggerFactory.getLogger(AbstractDataSourceTestBase.class);
	private Logger logger = LoggerFactory.getLogger(getClass());

	private static AnnotationConfigApplicationContext ctx;
	private static DataGrainDecorator schemaLoading;

	protected AbstractDataSourceTestBase() {}

	@BeforeTest
	public static void initContext()
	{
		ctx = new AnnotationConfigApplicationContext();
		ctx.register(DataSourceContext.class);
		ctx.refresh();

		schemaLoading = new TableSchemaLoadingDecorator(getDataSource());
	}
	@AfterTest
	public static void releaseContext(ITestContext testContext)
	{
		ctx.close();
	}

	@BeforeMethod(firstTimeOnly=true)
	protected void doLiquibaseBefore(Method method)
	{
		DoLiquibase doLiquibase = method.getAnnotation(DoLiquibase.class);
		if (doLiquibase != null && doLiquibase.update()) {
			updateLiquibase(method);
		}
	}
	@AfterMethod(lastTimeOnly=true)
	protected void doLiquibaseAfter(Method method)
	{
		DoLiquibase doLiquibase = method.getAnnotation(DoLiquibase.class);
		if (doLiquibase != null && doLiquibase.rollback()) {
			rollbackLiquibase(method);
		}
	}

	protected void liquibaseExecutor(String fileNameOfChangeLog, LiquibaseConsumer liquibaseConsumer)
	{
		JdbcRunnable executeLiquibase = JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
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
					mainLogger.info("[Liquibase] Change log: \"{}\"", fileNameOfChangeLog);

					liquibase.setChangeLogParameter("testClassName", getClass().getName());

					liquibaseConsumer.accept(liquibase);
				} catch (LiquibaseException e) {
					throw new RuntimeException(e);
				}
			}
		);

		executeLiquibase.asRunnable().run();
	}

	/**
	 * Updates the database by change log of {@code <package_path>/ClassName.xml} and {@link LabelExpression label} of method name.
	 *
	 * @param method The method of tested
	 */
	protected void updateLiquibase(Method method)
	{
		liquibaseExecutor(
			getFileNameOfChangeSet(),
			liquibase -> {
				mainLogger.info("Update with label: \"{}\"", method.getName());
				liquibase.update(
					new Contexts(), new LabelExpression(method.getName())
				);
			}
		);
	}
	/**
	 * Rollbacks(8 change set) the database by change log of {@code <package_path>/ClassName.xml} and {@link LabelExpression label} of method name.
	 *
	 * @param method The method of tested
	 */
	protected void rollbackLiquibase(Method method)
	{
		liquibaseExecutor(
			getFileNameOfChangeSet(),
			liquibase -> {
				mainLogger.info("Rollback with label: \"{}\"", method.getName());
				liquibase.rollback(
					8, new Contexts(), new LabelExpression(method.getName())
				);
			}
		);
	}

	protected Logger getLogger()
	{
		return logger;
	}

	protected DataGrainDecorator getSchemaLoadingDecorator()
	{
		return schemaLoading;
	}

	protected static DatabaseVendor getCurrentVendor()
	{
		return ctx.getBean(DatabaseVendor.class);
	}

	protected static ApplicationContext getApplicationContext()
	{
		return ctx;
	}

	protected static DataSource getDataSource()
	{
		return ctx.getBean(DataSource.class);
	}

	private String getFileNameOfChangeSet()
	{
		return String.format("%s.xml", getClass().getName().replace(".", "/"));
	}

	private static boolean needSkip(ITestNGMethod method)
	{
		/**
		 * Checks:
		 * 1) IfDatabaseVendor annotation on class declaration
		 * 2) IfDatabaseVendor annotation on method declaration
		 */
		ConstructorOrMethod targetMethod = method.getConstructorOrMethod();
		IfDatabaseVendor vendorCondition = targetMethod
				.getDeclaringClass()
				.getAnnotation(IfDatabaseVendor.class);

		if (vendorCondition == null) {
			vendorCondition = targetMethod
				.getMethod()
				.getAnnotation(IfDatabaseVendor.class);

			if (vendorCondition == null) {
				return false;
			}
		}
		// :~)

		DatabaseVendor currentVendor = ctx.getBean(DatabaseVendor.class);
		boolean matchDatabaseVendor = AnnotationUtil.matchDatabaseVendor(currentVendor, vendorCondition);
		if (!matchDatabaseVendor) {
			mainLogger.debug("The needed vendor of database [{}] is not matched. Current vendor: [{}]", vendorCondition.match(), currentVendor.name());
		}

		return !matchDatabaseVendor;
	}
}

@FunctionalInterface
interface LiquibaseConsumer {
	void accept(Liquibase liquibase) throws LiquibaseException;
}
