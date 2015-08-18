package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JdbcRunnableTest {
	public JdbcRunnableTest() {}

	/**
	 * Tests the {@link JdbcRunnable#asRunnable}.<p>
	 */
	@Test
	public void asRunnable()
	{
		MutableBoolean hasRun = new MutableBoolean(false);

		Runnable testedRun =
			((JdbcRunnable)() -> hasRun.setTrue()).asRunnable();

		testedRun.run();

		Assert.assertTrue(hasRun.booleanValue());
	}

	/**
	 * Tests the {@link JdbcRunnable#asRunnable} with {@link SQLExceptionConvert}.<p>
	 */
	@Test(expectedExceptions=SampleRuntimeException.class)
	public void asRunnableWithSQLExceptionConvert()
	{
		Runnable testedRun =
			((JdbcRunnable)() -> { throw new SQLException("JdbcRunnable"); }).asRunnable(
				e -> new SampleRuntimeException(e)
			);

		testedRun.run();
	}
}
