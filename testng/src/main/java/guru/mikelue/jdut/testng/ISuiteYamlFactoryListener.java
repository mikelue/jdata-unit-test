package guru.mikelue.jdut.testng;

import java.util.Optional;
import javax.sql.DataSource;

import org.testng.IAttributes;
import org.testng.ISuite;
import org.testng.ISuiteListener;

import guru.mikelue.jdut.DuetConductor;

/**
 * This listener uses file name(<code style="color:blue">{@literal <suite_name>.yaml}</code> for resource({@link ClassLoader} of current thread)
 * of conducting data.
 *
 * <p>By default, the {@link DataSource} would be retrieved from {@link ISuite} object(which type of {@link IAttributes}).</p>
 *
 * <p>It is recommended that client implements {@link #needConductData} to trigger data conduction.</p>
 */
public class ISuiteYamlFactoryListener extends YamlFactoryListenerBase implements ISuiteListener {
	public ISuiteYamlFactoryListener() {}

	/**
	 * Executes {@link #duetConductorClean duetConductorClean} if the result of {@link #needConductData} is true.
	 *
	 * @param suite The suite provided by TestNG framework
	 */
	@Override
	public void onFinish(ISuite suite)
	{
		if (!needConductData(suite)) {
			getLogger().trace("[Build Data] Not matched suite: [{}]", suite.getName());
			return;
		}

		duetConductorClean(suite);
	}

	/**
	 * Executes {@link #duetConductorBuild duetConductorBuild} if the result of {@link #needConductData} is true.
	 *
	 * @param suite The suite provided by TestNG framework
	 */
	@Override
	public void onStart(ISuite suite)
	{
		if (!needConductData(suite)) {
			getLogger().trace("[Clean Data] Not matched suite: [{}]", suite.getName());
			return;
		}

		duetConductorBuild(suite);
	}

	/**
	 * Builds the conductor from data source in {@link ISuite}.<br>
	 *
	 * @param attributes The object of {@link ISuite}
	 *
	 * @return The initialized conductor or nothing
	 */
	@Override
	protected Optional<DuetConductor> buildDuetConductor(IAttributes attributes)
	{
		ISuite suite = (ISuite)attributes;

		String fileName = TestNgResourceNaming.naming("{1}", suite, ".yaml");
		getLogger().debug("Build DuetConductor by file name: \"{}\"", fileName);

		return Optional.of(
			buildYamlConductorFactory(attributes)
				.conductResource(fileName)
		);
	}

	/**
	 * Checks whether or not the suite should make.<br>
	 *
	 * By default, this method always returns true.
	 *
	 * @param suite The suite data
	 *
	 * @return true if this suite should apply data conduction
	 */
	protected boolean needConductData(ISuite suite)
	{
		return true;
	}
}