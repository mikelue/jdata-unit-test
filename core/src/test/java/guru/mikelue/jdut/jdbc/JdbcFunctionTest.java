package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;
import java.util.function.Function;

import org.testng.Assert;
import org.testng.annotations.Test;

public class JdbcFunctionTest {
	public JdbcFunctionTest() {}

	/**
	 * Tests the {@link JdbcFunction#asFunction}.<p>
	 */
	@Test
	public void asFunction()
	{
		Integer sampleValue = 20;
		Integer addedValue = 5;

		Function<Integer, Integer> testedFunction =
			((JdbcFunction<Integer, Integer>)v -> v + addedValue).asFunction();

		Assert.assertEquals(testedFunction.apply(sampleValue), new Integer(sampleValue + addedValue));
	}

	/**
	 * Tests the {@link JdbcFunction#asFunction} with {@link SQLExceptionConvert}.<p>
	 */
	@Test(expectedExceptions=SampleRuntimeException.class)
	public void asFunctionWithSQLExceptionConvert()
	{
		Function<Integer, Integer> testedFunction =
			((JdbcFunction<Integer, Integer>)v -> { throw new SQLException("JdbcFunction"); } ).asFunction(
				e -> new SampleRuntimeException(e)
			);

		testedFunction.apply(20);
	}

	/**
	 * Tests the surrounding by {@link SurroundFunction.SurroundOperator}.
	 */
	@Test
	public void surroundedBy() throws SQLException
	{
		JdbcFunction<Integer, Integer> sampleFunc = v -> v + 4;
		JdbcFunction<Integer, Integer> surroundedFunc = sampleFunc.surroundedBy(
			f -> v -> f.applyJdbc(v * 3)
		);

		Assert.assertEquals(surroundedFunc.applyJdbc(7), new Integer(25));
	}
}
