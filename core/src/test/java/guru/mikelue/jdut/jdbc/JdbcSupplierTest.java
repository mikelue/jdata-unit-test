package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

		assertEquals(testedSupplier.get(), sampleValue);
	}

	/**
	 * Tests the {@link JdbcSupplier#asSupplier} with {@link SQLExceptionConvert}.<p>
	 */
	@Test
	public void asSupplierWithSQLExceptionConvert()
	{
		Supplier<Integer> testedSupplier =
			((JdbcSupplier<Integer>)() -> { throw new SQLException("S"); } ).asSupplier(
				e -> new SampleRuntimeException(e)
			);

		assertThrows(SampleRuntimeException.class, testedSupplier::get);
	}
}
