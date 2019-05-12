package guru.mikelue.jdut.jdbc;

import java.sql.SQLException;
import java.util.function.Consumer;

import org.apache.commons.lang3.mutable.MutableInt;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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

		assertEquals(5, surroundingSampleData.intValue());
	}

	/**
	 * Tests the {@link JdbcVoidFunction#asConsumer} with {@link SQLExceptionConvert}.<p>
	 */
	@Test
	public void asConsumerWithSQLExceptionConvert()
	{
		Consumer<Integer> testedConsumer =
			((JdbcVoidFunction<Integer>)v -> { throw new SQLException("JdbcVoidFunction"); } ).asConsumer(
				e -> new SampleRuntimeException(e)
			);

		assertThrows(SampleRuntimeException.class,
			() -> testedConsumer.accept(20)
		);
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

		assertEquals(119, surroundedSampleData.intValue()); // Asserts the execution surrounded function
		assertEquals(122, surroundingSampleData.intValue()); // Asserts execution of the surrounding function
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
