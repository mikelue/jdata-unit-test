package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;
import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JdbcSupplierTest {
	public JdbcSupplierTest() {}

	/**
	 * Tests the {@link JdbcSupplier#asSupplier}.<p>
	 */
	@Test
	public void asSupplier()
	{
		Integer sampleValue = 20;

		Supplier<Integer> testedSupplier =
			((JdbcSupplier<Integer>)() -> sampleValue).asSupplier();

		Assert.assertEquals(testedSupplier.get(), sampleValue);
	}

	/**
	 * Tests the {@link JdbcSupplier#asSupplier} with {@link SQLExceptionConvert}.<p>
	 */
	@Test(expectedExceptions=SampleRuntimeException.class)
	public void asSupplierWithSQLExceptionConvert()
	{
		Supplier<Integer> testedSupplier =
			((JdbcSupplier<Integer>)() -> { throw new SQLException("S"); } ).asSupplier(
				e -> new SampleRuntimeException(e)
			);

		testedSupplier.get();
	}
}
