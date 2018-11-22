package guru.mikelue.jdut.jdbc.function;

import mockit.Mocked;
import mockit.Verifications;
import org.testng.Assert;
import org.testng.annotations.Test;

import guru.mikelue.jdut.jdbc.JdbcFunction;

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

		Assert.assertEquals(testedFunction.applyJdbc(mockedCloseable), sampleValue);
		new Verifications() {{
			mockedCloseable.close();
			times = 1;
		}};
	}
}
