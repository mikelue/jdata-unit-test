package guru.mikelue.jdut.testng;

import org.testng.ITestNGMethod;

/**
 * Provides utility for processing instance of {@link TestNGConfig} annotation.
 */
public final class TestNGConfigUtil {
	private TestNGConfigUtil() {}

	/**
	 * Checkes if there is {@link TestNGConfig} with true value of {@link TestNGConfig#oneTimeOnly}.
	 *
	 * @param testNgMethod The method of TestNG
	 *
	 * @return true if the config has true value
	 *
	 * @see #firstTime
	 * @see #lastTime
	 */
	public static boolean oneTimeOnly(ITestNGMethod testNgMethod)
	{
		TestNGConfig testNgConfig = testNgMethod.getConstructorOrMethod().getMethod()
			.getDeclaredAnnotation(TestNGConfig.class);

		return testNgConfig != null &&
			testNgConfig.oneTimeOnly();
	}

	/**
	 * Checkes if this is first time of invocation(by data provider).
	 *
	 * @param testNgMethod The method of TestNG
	 *
	 * @return true if the config is true and the first time
	 *
	 * @see #lastTime
	 */
	public static boolean firstTime(ITestNGMethod testNgMethod)
	{
		return testNgMethod.getParameterInvocationCount() == 0;
	}

	/**
	 * Checkes if this is last time of invocation(by data provider).
	 *
	 * @param testNgMethod The method of TestNG
	 *
	 * @return true if the config is true and the first time
	 *
	 * @see #firstTime
	 */
	public static boolean lastTime(ITestNGMethod testNgMethod)
	{
		return !testNgMethod.hasMoreInvocation();
	}
}
