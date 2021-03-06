package guru.mikelue.jdut.junit5.test;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class AppContextExtension implements BeforeAllCallback, AfterAllCallback {
	private final static String NAME_APP_CONTEXT = "appContext";

	public static ApplicationContext getAppContext(ExtensionContext junitContext)
	{
		Store store = getStore(junitContext);
		return store.get(NAME_APP_CONTEXT, ApplicationContext.class);
	}

	@Override
	public void beforeAll(ExtensionContext junitContext) throws Exception
	{
		Store store = getStore(junitContext);

		AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		ctx.register(DataSourceContext.class);
		ctx.refresh();
		store.put(NAME_APP_CONTEXT, ctx);

		AbstractDataSourceTestBase testBase = (AbstractDataSourceTestBase)junitContext.getRequiredTestInstance();
		testBase.setAppContext(ctx);
	}

	@Override
	public void afterAll(ExtensionContext junitContext) throws Exception
	{
		AbstractDataSourceTestBase testBase = (AbstractDataSourceTestBase)junitContext.getRequiredTestInstance();
		testBase.setAppContext(null);

		Store store = getStore(junitContext);
		AnnotationConfigApplicationContext appContext = store.remove(NAME_APP_CONTEXT, AnnotationConfigApplicationContext.class);
		appContext.close();
		appContext = null;
	}

	private static Store getStore(ExtensionContext junitContext)
	{
		return junitContext.getStore(Namespace.GLOBAL);
	}
}
