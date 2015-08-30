package guru.mikelue.jdut.test;

import java.lang.reflect.Method;
import java.util.stream.Stream;
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
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import guru.mikelue.jdut.decorate.DataGrainDecorator;
import guru.mikelue.jdut.decorate.TableSchemaLoadingDecorator;
import guru.mikelue.jdut.jdbc.JdbcRunnable;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.vendor.DatabaseVendor;

@Test(suiteName="DataSourceSuite", groups="DataSourceGroup")
public abstract class AbstractDataSourceTestBase {
	private Logger mainLogger = LoggerFactory.getLogger(AbstractDataSourceTestBase.class);
	private Logger logger = LoggerFactory.getLogger(getClass());

	private static AnnotationConfigApplicationContext ctx;

	private DataGrainDecorator schemaLoading;

	protected AbstractDataSourceTestBase() {}

	@BeforeSuite
	public static void initDataSourceSuite()
	{
		ctx = new AnnotationConfigApplicationContext();
		ctx.register(DataSourceContext.class);
		ctx.refresh();
	}
	@AfterSuite
	public static void closeDataSourceSuite()
	{
		ctx.stop();
	}

	@BeforeClass
	protected void initDataSourceClass()
	{
		schemaLoading = new TableSchemaLoadingDecorator(getDataSource());
	}

	@BeforeMethod(firstTimeOnly=true)
	protected void checkVendorCondition(Method method)
	{
		IfVendor vendorCondition = method.getAnnotation(IfVendor.class);
		if (vendorCondition == null) {
			return;
		}

		DatabaseVendor currentVendor = ctx.getBean(DatabaseVendor.class);

		boolean matched = Stream.of(vendorCondition.match())
			.anyMatch(
				vendor -> currentVendor.equals(vendor) || vendor == DatabaseVendor.Unknown
			);
		if (!matched) {
			throw new SkipException("Skip test because not matched needed database");
		}

		boolean notMatched = Stream.of(vendorCondition.notMatch())
			.filter(vendor -> vendor != DatabaseVendor.Unknown)
			.anyMatch(
				vendor -> currentVendor.equals(vendor) && vendor != DatabaseVendor.Unknown
			);
		if (notMatched) {
			throw new SkipException("Skip test because not matched needed database");
		}
	}

	@BeforeMethod(firstTimeOnly=true, dependsOnMethods="checkVendorCondition")
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
}

@FunctionalInterface
interface LiquibaseConsumer {
	void accept(Liquibase liquibase) throws LiquibaseException;
}
