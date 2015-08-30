package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;
import org.testng.Assert;
import org.testng.annotations.Test;

public class JdbcVoidFunctionTest {
	public JdbcVoidFunctionTest() {}

	/**
	 * Tests the {@link JdbcVoidFunction#asConsumer}.<p>
	 */
	@Test
	public void asConsumer()
	{
		final MutableInt surroundingSampleData = new MutableInt(2);

		Consumer<Integer> testedConsumer =
			((JdbcVoidFunction<Integer>)v -> surroundingSampleData.add(v)).asConsumer();

		testedConsumer.accept(3);

		Assert.assertEquals(surroundingSampleData.intValue(), 5);
	}

	/**
	 * Tests the {@link JdbcVoidFunction#asConsumer} with {@link SQLExceptionConvert}.<p>
	 */
	@Test(expectedExceptions=SampleRuntimeException.class)
	public void asConsumerWithSQLExceptionConvert()
	{
		Consumer<Integer> testedConsumer =
			((JdbcVoidFunction<Integer>)v -> { throw new SQLException("JdbcVoidFunction"); } ).asConsumer(
				e -> new SampleRuntimeException(e)
			);

		testedConsumer.accept(20);
	}

	/**
	 * Tests the surrounding by {@link SurroundFunction.SurroundOperator}.
	 */
	@Test
	public void surroundedBy() throws SQLException
	{
		MutableInt surroundedSampleData = new MutableInt(110);
		MutableInt surroundingSampleData = new MutableInt(120);

		JdbcVoidFunction<Integer> sampleFunc = v -> surroundedSampleData.add(v);
		JdbcVoidFunction<Integer> surroundedFunc = JdbcVoidFunction.fromJdbcFunction(sampleFunc.surroundedBy(
			buildSurrounding(surroundingSampleData)
		));

		surroundedFunc.applyJdbc(9);

		Assert.assertEquals(surroundedSampleData.intValue(), 119); // Asserts the execution surrounded function
		Assert.assertEquals(surroundingSampleData.intValue(), 122); // Asserts execution of the surrounding function
	}

	private <T> JdbcFunction.SurroundOperator<Integer, T> buildSurrounding(final MutableInt sampleData)
	{
		return f -> v -> {
			sampleData.increment();

			T result = f.applyJdbc(v);

			sampleData.increment();

			return result;
		};
	}
}
