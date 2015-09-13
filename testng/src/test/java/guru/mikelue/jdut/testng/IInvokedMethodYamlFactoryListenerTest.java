package guru.mikelue.jdut.testng;

import java.sql.SQLException;
import java.util.Optional;

import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Verifications;
import org.testng.IAttributes;
import org.testng.IInvokedMethod;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.testng.test.AbstractDataSourceTestBase;

@Listeners(IInvokedMethodYamlFactoryListener.class)
public class IInvokedMethodYamlFactoryListenerTest extends AbstractDataSourceTestBase {

	@BeforeTest
	protected static void putDataSourceTest(ITestContext testContext)
	{
		YamlFactoryListenerBase.setDataSource(testContext, getDataSource());
	}
	@AfterTest
	protected static void pullDataSourceTest(ITestContext testContext)
	{
		YamlFactoryListenerBase.removeDataSource(testContext);
	}

	public IInvokedMethodYamlFactoryListenerTest() {}

	/**
	 * Tests the listener for method.
	 */
	@Test
	public void nothingHappened() {}

	/**
	 * Tests the listener for method to load YAML.
	 */
	@Test @JdutResource
	public void loadYaml() throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn, "SELECT COUNT(*) FROM tab_22",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
	}

	@Injectable
	private ITestResult mockTestResult;
	@Injectable
	private IInvokedMethod mockInvokedMethod;
	@Injectable
	private ITestNGMethod mockTestNGMethod;
	@Injectable
	private DuetConductor mockDuetConductor;

	/**
	 * Tests the listener for one time only execution(by mocking).
	 */
	@Test(dataProvider="YamlResourceOneTimeOnly")
	public void yamlResourceOneTimeOnly(
		IInvokedMethodYamlFactoryListener testedListener,
		String methodName, int currentInvocationCount,
		boolean hasBuilding, boolean hasCleaning
	) throws NoSuchMethodException {
		new NonStrictExpectations(testedListener) {{
			testedListener.buildDuetConductor((IAttributes)any);
			result = Optional.of(mockDuetConductor);

			mockInvokedMethod.isTestMethod();
			result = true;

			mockInvokedMethod.getTestMethod();
			result = mockTestNGMethod;

			mockTestNGMethod.getCurrentInvocationCount();
			result = currentInvocationCount;

			mockTestNGMethod.getParameterInvocationCount();
			result = 3;

			mockTestNGMethod.getConstructorOrMethod().getMethod();
			result = IInvokedMethodYamlFactoryListenerTest.class.getMethod(methodName);
		}};

		testedListener.beforeInvocation(
			mockInvokedMethod, mockTestResult
		);
		testedListener.afterInvocation(
			mockInvokedMethod, mockTestResult
		);

		new Verifications() {{
			mockDuetConductor.build();
			times = hasBuilding ? 1 : 0;

			mockDuetConductor.clean();
			times = hasCleaning ? 1 : 0;
		}};
	}
	@DataProvider(name="YamlResourceOneTimeOnly")
	public Object[][] getYamlResourceOneTimeOnly()
	{
		IInvokedMethodYamlFactoryListener testedListener = new IInvokedMethodYamlFactoryListener();

		return new Object[][] {
			{ testedListener, "sampleOneTimeOnly", 1, true, false },
			{ testedListener, "sampleOneTimeOnly", 3, false, true },
			{ testedListener, "sampleMultipleTimes", 1, true, true },
			{ testedListener, "sampleMultipleTimes", 3, true, true },
		};
	}

	@Test(enabled=false)
	@JdutResource @TestNGConfig(oneTimeOnly=true)
	public void sampleOneTimeOnly() {}

	@Test(enabled=false)
	@JdutResource
	public void sampleMultipleTimes() {}
}
