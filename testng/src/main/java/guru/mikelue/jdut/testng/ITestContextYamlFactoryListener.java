package guru.mikelue.jdut.testng;

import java.util.Optional;
import javax.sql.DataSource;

import org.testng.IAttributes;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import guru.mikelue.jdut.DuetConductor;

/**
 * This listener uses file name(<code style="color:blue">{@literal <test_name>.yaml}</code>) for resource({@link ClassLoader} of current thread)
 * of conducting data.
 *
 * <p>By default, the {@link DataSource} would be retrieved from {@link ITestContext} object(which type of {@link IAttributes}).</p>
 *
 * <p>It is recommended that client implements {@link #needConductData} to trigger data conduction.</p>
 */
public class ITestContextYamlFactoryListener extends YamlFactoryListenerBase implements ITestListener {
	public ITestContextYamlFactoryListener() {}

	/**
	 * Executes {@link #duetConductorClean duetConductorClean} if the result of {@link #needConductData} is true.
	 *
	 * @param context The context provided by TestNG framework
	 */
	@Override
	public void onFinish(ITestContext context)
	{
		if (!needConductData(context)) {
			if (getLogger().isTraceEnabled()) {
				getLogger().trace("[Build Data] Not matched context: [{} - {}]",
					context.getSuite().getName(),
					context.getName()
				);
			}
			return;
		}

		duetConductorClean(context);
	}
	/**
	 * Executes {@link #duetConductorBuild duetConductorBuild} if the result of {@link #needConductData} is true.
	 *
	 * @param context The context provided by TestNG framework
	 */
	@Override
	public void onStart(ITestContext context)
	{
		if (!needConductData(context)) {
			if (getLogger().isTraceEnabled()) {
				getLogger().trace("[Clean Data] Not matched context: [{} - {}]",
					context.getSuite().getName(),
					context.getName()
				);
			}
			return;
		}

		duetConductorBuild(context);
	}
	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
	@Override
	public void onTestFailure(ITestResult result) {}
	@Override
	public void onTestSkipped(ITestResult result) {}
	@Override
	public void onTestStart(ITestResult result) {}
	@Override
	public void onTestSuccess(ITestResult result) {}

	/**
	 * Builds the conductor from data source in {@link ITestContext}.<br>
	 *
	 * This method uses {@link ITestContext#getName()}.yaml as file name of data conduction.
	 *
	 * @param attributes The object of {@link ITestContext}
	 *
	 * @return The initialized conductor or nothing
	 */
	@Override
	protected Optional<DuetConductor> buildDuetConductor(IAttributes attributes)
	{
		ITestContext context = (ITestContext)attributes;
		String fileName = TestNgResourceNaming.naming("{0}", context, ".yaml");

		getLogger().debug("Build DuetConductor by file name: \"{}\"", fileName);

		return Optional.of(
			buildYamlConductorFactory(attributes)
				.conductResource(fileName)
		);
	}

	/**
	 * Checks whether or not the context should make.<br>
	 *
	 * By default, this method always returns true.
	 *
	 * @param context The context data
	 *
	 * @return true if this context should apply data conduction
	 */
	protected boolean needConductData(ITestContext context)
	{
		return true;
	}
}
