package guru.mikelue.jdut.junit5.test;

import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

import javax.sql.DataSource;

import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;

@TestInstance(PER_CLASS)
@ExtendWith({ AppContextExtension.class })
public abstract class AbstractDataSourceTestBase {
	private final Logger logger = LoggerFactory.getLogger(getClass());

	public AbstractDataSourceTestBase() {}

	private ApplicationContext appContext;

	public DataSource getDataSource()
	{
		return getAppContext().getBean(DataSource.class);
	}

	/**
	 * @return the appContext
	 */
	public ApplicationContext getAppContext() {
		return appContext;
	}

	/**
	 * @param appContext the appContext to set
	 */
	public void setAppContext(ApplicationContext appContext) {
		this.appContext = appContext;
	}

	protected Logger getLogger()
	{
		return logger;
	}
}
