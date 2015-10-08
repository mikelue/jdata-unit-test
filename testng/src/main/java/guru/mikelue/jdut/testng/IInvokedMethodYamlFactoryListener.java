package guru.mikelue.jdut.testng;

import java.lang.reflect.Method;
import java.util.Optional;
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
 * This listener uses file name(<code style="color:blue">{@literal <class_name>-<method_name>.yaml}</code>) for resource({@link Class#getResource Class.getResource})
 * of conducting data.
 *
 * <p>Only <b>testing method</b> which is annotated {@link JdutResource @JdutResource} would be prepared for data conduction.</p>
 *
 * <p>By default, the {@link DataSource} would be retrieved from {@link ITestContext} object(which type of {@link IAttributes}).</p>
 *
 * <p>By value of {@link TestNGConfig#oneTimeOnly}, this listener would executes only one time or multiple times
 * for testing method with data provider.</p>
 *
 * <p>It is recommended that client implements {@link #needConductData} to trigger data conduction.</p>
 *
 * @see TestNGConfig
 * @see AnnotationUtil#buildConductorByConvention(YamlConductorFactory, Method)
 */
public class IInvokedMethodYamlFactoryListener extends YamlFactoryListenerBase implements IInvokedMethodListener {
	public IInvokedMethodYamlFactoryListener() {}

	/**
	 * Executes {@link #duetConductorBuild duetConductorBuild} if the method is 1) testing method and 2) annotated with {@link JdutResource}.
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
	 * Executes {@link #duetConductorClean duetConductorClean} if the method is 1) testing method and 2) annotated with {@link JdutResource}.
	 *
	 * @param method The method provided by TestNG framework
	 * @param testResult The result object provided byh TestNG framework
	 */
	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult)
	{
		if (!needConductData(method, testResult)) {
			getLogger().trace("[Clean Data] Not matched context of method: [{}]",
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
	 * Builds the conductor from data source in {@link ITestContext} of {@link ITestResult}.<br>
	 *
	 * This method would build conductor only for testing method with {@link JdutResource} annotation.<br>
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
