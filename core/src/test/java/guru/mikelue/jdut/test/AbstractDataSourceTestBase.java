package guru.mikelue.jdut.test;

import javax.sql.DataSource;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import snaq.db.DBPoolDataSource;

public abstract class AbstractDataSourceTestBase {
	private DBPoolDataSource ds;

	protected AbstractDataSourceTestBase() {}

	@BeforeClass
	protected void initDataSource()
	{
		ds = DataSourceBuilder.buildDefaultHsqlDb(getClass().getName());
	}
	@AfterClass
	protected void releaseDataSource()
	{
		ds.release();
	}

	public DataSource getDataSource()
	{
		return ds;
	}
}
