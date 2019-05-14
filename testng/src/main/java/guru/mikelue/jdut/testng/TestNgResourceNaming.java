package guru.mikelue.jdut.testng;

import java.lang.reflect.Method;
import java.text.MessageFormat;

import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;

import guru.mikelue.jdut.annotation.JdutResourceNaming;

/**
 * <p>Provides generating of resource name by format string({@link MessageFormat}) and properties of {@link ITestContext}, {@link ITestNGMethod}, or {@link ISuite}.</p>
 *
 * For example:<br>
 * <pre><code class="java">
 * // objISuite - The object of org.testng.ISuite
 * String fileName = TestNgResourceNaming.naming("{1}", objISuite, ".yaml");
 * </code></pre>
 *
 */
public final class TestNgResourceNaming {
	private TestNgResourceNaming() {}

	/**
	 * Generating naming by {@link MessageFormat} and {@link ITestContext}.<br>
	 *
	 * The parameters for formatting:
	 * <ul>
	 * 	<li>0 - {@link ISuite#getHost()}</li>
	 * 	<li>1 - {@link ISuite#getName()}</li>
	 * </ul>
	 *
	 * @param format The format used in {@link MessageFormat#format(String, Object...)}
	 * @param testSuite The context of TestNG
	 *
	 * @return formatted string
	 */
	public static String naming(String format, ISuite testSuite)
	{
		return MessageFormat.format(
			format,
			testSuite.getHost(),
			testSuite.getName()
		);
	}

	/**
	 * Generating naming by {@link MessageFormat}, {@link ITestContext} and suffix.<br>
	 *
	 * The parameters for formatting:
	 * <ul>
	 * 	<li>0 - {@link ISuite#getHost()}</li>
	 * 	<li>1 - {@link ISuite#getName()}</li>
	 * </ul>
	 *
	 * @param format The format used in {@link MessageFormat#format(String, Object...)}
	 * @param testSuite The context of TestNG
	 * @param suffix The suffix of resource
	 *
	 * @return formatted string
	 */
	public static String naming(String format, ISuite testSuite, String suffix)
	{
		return naming(
			format + suffix, testSuite
		);
	}

	/**
	 * Generating naming by {@link MessageFormat} and {@link ITestContext}.<br>
	 *
	 * The parameters for formatting:
	 * <ul>
	 * 	<li>0 - {@link ITestContext#getName()}</li>
	 * 	<li>1 - {@link ITestContext#getHost()}</li>
	 * 	<li>2 - {@link ISuite#getName()}</li>
	 * </ul>
	 *
	 * @param format The format used in {@link MessageFormat#format(String, Object...)}
	 * @param testContext The context of TestNG
	 *
	 * @return formatted string
	 */
	public static String naming(String format, ITestContext testContext)
	{
		return MessageFormat.format(
			format,
			testContext.getName(),
			testContext.getHost(),
			testContext.getSuite().getName()
		);
	}

	/**
	 * Generating naming by {@link MessageFormat}, {@link ITestContext} and suffix.<br>
	 *
	 * The parameters for formatting:
	 * <ul>
	 * 	<li>0 - {@link ITestContext#getName()}</li>
	 * 	<li>1 - {@link ITestContext#getHost()}</li>
	 * 	<li>2 - {@link ISuite#getName()}</li>
	 * </ul>
	 *
	 * @param format The format used in {@link MessageFormat#format(String, Object...)}
	 * @param testContext The context of TestNG
	 * @param suffix The suffix of resource
	 *
	 * @return formatted string
	 */
	public static String naming(String format, ITestContext testContext, String suffix)
	{
		return naming(
			format + suffix, testContext
		);
	}

	/**
	 * Generating naming by {@link MessageFormat} and {@link ITestNGMethod}.<br>
	 *
	 * The parameters for formatting:
	 * <ul>
	 * 	<li>0 - {@link Class#getName()}</li>
	 * 	<li>1 - {@link Class#getSimpleName()}</li>
	 * 	<li>2 - {@link Class#getTypeName()}</li>
	 * 	<li>3 - {@link Class#getCanonicalName()}</li>
	 * 	<li>4 - {@link Method#getName()}</li>
	 * </ul>
	 *
	 * @param format The format used in {@link MessageFormat#format(String, Object...)}
	 * @param testNgMethod The context of TestNG
	 *
	 * @return formatted string
	 */
	public static String naming(String format, ITestNGMethod testNgMethod)
	{
		Method method = testNgMethod.getConstructorOrMethod().getMethod();
		return JdutResourceNaming.naming(format, method);
	}

	/**
	 * Generating naming by {@link MessageFormat}, {@link ITestNGMethod} and suffix.<br>
	 *
	 * The parameters for formatting:
	 * <ul>
	 * 	<li>0 - {@link Class#getName()}</li>
	 * 	<li>1 - {@link Class#getSimpleName()}</li>
	 * 	<li>2 - {@link Class#getTypeName()}</li>
	 * 	<li>3 - {@link Class#getCanonicalName()}</li>
	 * 	<li>4 - {@link Method#getName()}</li>
	 * </ul>
	 *
	 * @param format The format used in {@link MessageFormat#format(String, Object...)}
	 * @param testNgMethod The context of TestNG
	 * @param suffix The suffix of resource
	 *
	 * @return formatted string
	 */
	public static String naming(String format, ITestNGMethod testNgMethod, String suffix)
	{
		Method method = testNgMethod.getConstructorOrMethod().getMethod();
		return JdutResourceNaming.naming(format, method, suffix);
	}
}
