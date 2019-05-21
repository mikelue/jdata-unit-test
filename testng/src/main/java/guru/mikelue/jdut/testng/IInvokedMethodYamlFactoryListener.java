package guru.mikelue.jdut.testng;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.testng.IAttributes;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.annotation.AnnotationUtil;
import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

/**
 * This listener uses conventions of file name by (<code style="color:blue">{@literal classpath:<package>/<class_name>-<method_name>.yaml}</code>) for conducting data.
 *
 * <p>Only <b>testing method</b> which is annotated {@link JdutResource @JdutResource} would be prepared for data conduction.</p>
 *
 * <p>By default, the {@link DataSource} would be retrieved from {@link ITestContext} object(which type of {@link IAttributes}).</p>
 *
 * <p>By value of {@link TestNGConfig#oneTimeOnly}, this listener would executes only one time or multiple times
 * for testing method with data provider.</p>
 *
 * <p>Example by defulat conventions:</p>
 * <pre><code class="java">
 * package guru.mikelue.jdut.testng.example;
 *
 * &#64;Test(testName="ReefSharkTest")
 * &#64;Listeners(IInvokedMethodYamlFactoryListener.class)
 * public class ReefSharkTest {
 *     // Puts data source into test context
 *     &#64;BeforeTest
 *     protected static void putDataSourceTest(ITestContext testContext)
 *     {
 *         YamlFactoryListenerBase.setDataSource(testContext, getDataSource());
 *     }
 *     // Removes data source from test context
 *     &#64;AfterTest
 *     protected static void pullDataSourceTest(ITestContext testContext)
 *     {
 *         YamlFactoryListenerBase.removeDataSource(testContext);
 *     }
 *
 *     // File: guru/mikelue/jdut/testng/example
 *     //    -&gt; ReefSharkTest-traceVictim.yaml
 *     &#64;Test &#64;JdutResource
 *     public void traceVictim()
 *     {
 *     }
 *
 *     // File: guru/mikelue/jdut/testng/example
 *     //    -&gt; ReefSharkTest-bite.yaml
 *     &#64;Test(dataProvider="bite") &#64;TestNGConfig(oneTimeOnly=true) &#64;JdutResource
 *     public void bite(int speed)
 *     {
 *         // Executes tested code
 *         // Assertions ...
 *     }
 *     &#64;DataProvider
 *     Object[][] bite()
 *     {
 *         return new Object[][] {
 *             { 0 }, { 30 }, { 2000 },
 *         };
 *     }
 * }
 * </code></pre>
 *
 * <p>You can override {@link YamlFactoryListenerBase#buildYamlConductorFactory} to customize the object of {@link DataSource}.</p>
 *
 * @see YamlFactoryListenerBase#buildYamlConductorFactory
 * @see TestNGConfig
 * @see AnnotationUtil#buildConductorByConvention(YamlConductorFactory, Method)
 */
public class IInvokedMethodYamlFactoryListener extends YamlFactoryListenerBase implements IInvokedMethodListener {
	public IInvokedMethodYamlFactoryListener() {}

	/**
	 * Executes {@link #duetConductorBuild duetConductorBuild} if and only if the will-be invoked method is test one.<br>
	 *
	 * Configuration methods <strong>won't be applied</strong> by this listener.<br>
	 *
	 * @param method The method provided by TestNG framework
	 * @param testResult The result object provided byh TestNG framework
	 */
	@Override
	public void beforeInvocation(IInvokedMethod method, ITestResult testResult)
	{
		if (!needConductData(method, testResult)) {
			getLogger().trace("[Build Data] Not matched context of method: [{}]",
				method
			);
			return;
		}

		ITestNGMethod testNgMethod = method.getTestMethod();

		if (!method.isTestMethod() ||
			!AnnotationUtil.hasJdutResourceAnnotation(testNgMethod.getConstructorOrMethod().getMethod())
		) {
			return;
		}

		if (TestNGConfigUtil.oneTimeOnly(testNgMethod) && !TestNGConfigUtil.firstTime(testNgMethod)) {
			if (getLogger().isTraceEnabled()) {
				getLogger().trace("[Building Data] Not first time for method: [{}]",
					testNgMethod.getConstructorOrMethod().getMethod().getName()
				);
			}
			return;
		}

		duetConductorBuild(testResult);
	}
	/**
	 * Executes {@link #duetConductorBuild duetConductorBuild} if and only if {@link beforeInvocation} has set-up a conductor.<br>
	 *
	 * Configuration methods <strong>won't be applied</strong> by this listener.
	 *
	 * @param method The method provided by TestNG framework
	 * @param testResult The result object provided byh TestNG framework
	 */
	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult)
	{
		ITestNGMethod testNgMethod = method.getTestMethod();

		if (!method.isTestMethod() ||
			!AnnotationUtil.hasJdutResourceAnnotation(testNgMethod.getConstructorOrMethod().getMethod())
		) {
			return;
		}

		if (TestNGConfigUtil.oneTimeOnly(testNgMethod) && !TestNGConfigUtil.lastTime(testNgMethod)) {
			if (getLogger().isTraceEnabled()) {
				getLogger().trace("[Clean Data] Not last time for method: [{}]",
					testNgMethod.getConstructorOrMethod().getMethod().getName()
				);
			}
			return;
		}

		duetConductorClean(testResult);
	}

	/**
	 * Builds the conductor from data source {@link ITestResult#getTestContext} of <em>attributes</em>(as {@link ITestResult}).<br>
	 *
	 * This method would build conductor only for testing method with {@link JdutResource} annotation.
	 *
	 * @param attributes The object of {@link ITestResult}
	 *
	 * @return The initialized conductor or nothing
	 */
	@Override
	protected Optional<DuetConductor> buildDuetConductor(IAttributes attributes)
	{
		ITestResult result = (ITestResult)attributes;

		Method method = result.getMethod().getConstructorOrMethod().getMethod();

		if (getLogger().isDebugEnabled()) {
			getLogger().debug(
				"Build DuetConductor by file name: \"{}\"",
				TestNgResourceNaming.naming(
					"{1}-{4}",
					result.getMethod(),
					".yaml"
				)
			);
		}

		return AnnotationUtil.buildConductorByConvention(buildYamlConductorFactory(result.getTestContext()), method);
	}

	/**
	 * Checks whether or not the context should make.<br>
	 *
	 * By default, this method always returns true.
	 *
	 * @param method The method provided by TestNG framework
	 * @param testResult The result object provided byh TestNG framework
	 *
	 * @return true if this context should apply data conduction
	 */
	protected boolean needConductData(IInvokedMethod method, ITestResult testResult)
	{
		return true;
	}
}
