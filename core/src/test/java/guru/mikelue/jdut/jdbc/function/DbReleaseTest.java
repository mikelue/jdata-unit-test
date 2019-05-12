package guru.mikelue.jdut.jdbc.function;

import mockit.Mocked;
import mockit.Verifications;
import org.junit.jupiter.api.Test;

import guru.mikelue.jdut.jdbc.JdbcFunction;

import static org.junit.jupiter.api.Assertions.*;

public class DbReleaseTest {
	@Mocked
	private AutoCloseable mockedCloseable;

	public DbReleaseTest() {}

	/**
	 * Tests the calling of {@link AutoCloseable#close}.
	 */
	@Test
	public void autoClose()
		throws Exception
	{
		final Integer sampleValue = 78;

		JdbcFunction<AutoCloseable, Integer> testedFunction = mockedObject -> sampleValue;

		testedFunction = testedFunction.surroundedBy(DbRelease::autoClose);

		assertEquals(sampleValue, testedFunction.applyJdbc(mockedCloseable));
		new Verifications() {{
			mockedCloseable.close();
			times = 1;
		}};
	}
}
