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
import org.testng.annotations.AfterGroups;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.Test;

import guru.mikelue.jdut.jdbc.JdbcRunnable;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;

@Test(groups="DataSource")
public abstract class AbstractDataSourceTestBase {
	private Logger logger = LoggerFactory.getLogger(AbstractDataSourceTestBase.class);
	private static AnnotationConfigApplicationContext ctx;

	protected AbstractDataSourceTestBase() {}

	@BeforeGroups(groups="DataSource")
	public static void initSpringContext()
	{
		ctx = new AnnotationConfigApplicationContext();
		ctx.register(DataSourceContext.class);
		ctx.refresh();
	}
	@AfterGroups(groups="DataSource")
	public static void closeSpringContext()
	{
		ctx.stop();
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
				logger.info("Update with label: \"{}\"", method.getName());
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
				logger.info("Rollback with label: \"{}\"", method.getName());
				liquibase.rollback(
					8, new Contexts(), new LabelExpression(method.getName())
				);
			}
		);
	}

	protected ApplicationContext getApplicationContext()
	{
		return ctx;
	}

	protected DataSource getDataSource()
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
