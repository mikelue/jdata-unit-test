package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

		assertEquals(Integer.valueOf(sampleValue + addedValue), testedFunction.apply(sampleValue));
	}

	/**
	 * Tests the {@link JdbcFunction#asFunction} with {@link SQLExceptionConvert}.<p>
	 */
	@Test
	public void asFunctionWithSQLExceptionConvert()
	{
		Function<Integer, Integer> testedFunction =
			((JdbcFunction<Integer, Integer>)v -> { throw new SQLException("JdbcFunction"); } ).asFunction(
				e -> new SampleRuntimeException(e)
			);

		assertThrows(SampleRuntimeException.class,
			() -> testedFunction.apply(20)
		);
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

		assertEquals(Integer.valueOf(25), surroundedFunc.applyJdbc(7));
	}
}
