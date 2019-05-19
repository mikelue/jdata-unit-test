package guru.mikelue.jdut.testng.test;

import javax.sql.DataSource;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;

public class SuiteListenerForAppContext implements ISuiteListener {
	final static String ATTR_APP_CONTEXT = "_app_context_";

	public static ApplicationContext getApplicationContext(ITestContext testContext)
	{
		return getApplicationContext(testContext.getSuite());
	}

	public static DataSource getDataSource(ITestContext testContext)
	{
		return getApplicationContext(testContext).getBean(DataSource.class);
	}

	public static ApplicationContext getApplicationContext(ISuite suite)
	{
		return (ApplicationContext)suite.getAttribute(
			ATTR_APP_CONTEXT
		);
	}

	public static DataSource getDataSource(ISuite suite)
	{
		return getApplicationContext(suite).getBean(DataSource.class);
	}

	public static ApplicationContext buildAppContext()
	{
		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(DataSourceContext.class);
		ctx.refresh();

		return ctx;
	}

	public SuiteListenerForAppContext() {}

	@Override
	public void onStart(ISuite suite)
	{
		suite.setAttribute(ATTR_APP_CONTEXT, buildAppContext());
	}

	@Override
	public void onFinish(ISuite suite)
	{
		AnnotationConfigApplicationContext ctx = (AnnotationConfigApplicationContext)
			suite.removeAttribute(ATTR_APP_CONTEXT);
		ctx.close();
	}
}
