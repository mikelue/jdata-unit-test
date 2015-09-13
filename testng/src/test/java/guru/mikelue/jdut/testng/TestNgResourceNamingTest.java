package guru.mikelue.jdut.testng;

import mockit.Injectable;
import mockit.NonStrictExpectations;
import org.testng.Assert;
import org.testng.ISuite;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class TestNgResourceNamingTest {
	@Injectable
	private ITestContext mockTestContext;
	@Injectable
	private ITestNGMethod mockTestMethod;
	@Injectable
	private ISuite mockSuite;

	public TestNgResourceNamingTest() {}

	/**
	 * Tests the generating naming of resource by {@link ITestContext}.
	 */
	@Test(dataProvider="NamingByITestContext")
	public void namingByITestContext(
		String sampleFormat,
		String sampleName, String sampleHost, String sampleSuiteName,
		String expectedResult
	) {
		new NonStrictExpectations() {{
			mockTestContext.getName();
			result = sampleName;
			mockTestContext.getHost();
			result = sampleHost;

			mockTestContext.getSuite().getName();
			result = sampleSuiteName;
		}};

		Assert.assertEquals(
			TestNgResourceNaming.naming(
				sampleFormat, mockTestContext, ".yaml"
			),
			expectedResult
		);
	}
	@DataProvider(name="NamingByITestContext")
	private Object[][] getNamingByITestContext()
	{
		return new Object[][] {
			{ "{0}", "AbTest", "", "", "AbTest.yaml" },
			{ "{1}", "", "MyHost", "", "MyHost.yaml" },
			{ "{2}", "", "", "MySuite", "MySuite.yaml" },
		};
	}

	/**
	 * Tests the generating naming of resource by {@link ITestNGMethod}.
	 */
	@Test(dataProvider="NamingByITestNGMethod")
	public void namingByITestNGMethod(
		String sampleFormat, String expectedResult
	) throws NoSuchMethodException {
		new NonStrictExpectations() {{
			mockTestMethod.getConstructorOrMethod().getMethod();
			result = TestNgResourceNamingTest.class.getMethod("sampleMethod");
		}};

		Assert.assertEquals(
			TestNgResourceNaming.naming(
				sampleFormat, mockTestMethod, ".yaml"
			),
			expectedResult
		);
	}
	@DataProvider(name="NamingByITestNGMethod")
	private Object[][] getNamingByITestNGMethod()
	{
		return new Object[][] {
			{ "{0}", "guru.mikelue.jdut.testng.TestNgResourceNamingTest.yaml" },
			{ "{1}", "TestNgResourceNamingTest.yaml" },
			{ "{2}", "guru.mikelue.jdut.testng.TestNgResourceNamingTest.yaml" },
			{ "{3}", "guru.mikelue.jdut.testng.TestNgResourceNamingTest.yaml" },
			{ "{4}", "sampleMethod.yaml" },
		};
	}

	@Test(enabled=false)
	public void sampleMethod() {}

	/**
	 * Tests the generating naming of resource by {@link ISuite}.
	 */
	@Test(dataProvider="GetNamingByISuite")
	public void getNamingByISuite(
		String sampleFormat,
		String sampleHost, String sampleName,
		String expectedResult
	) {
		new NonStrictExpectations() {{
			mockSuite.getHost();
			result = sampleHost;

			mockSuite.getName();
			result = sampleName;
		}};

		Assert.assertEquals(
			TestNgResourceNaming.naming(
				sampleFormat, mockSuite, ".yaml"
			),
			expectedResult
		);
	}
	@DataProvider(name="GetNamingByISuite")
	private Object[][] getGetNamingByISuite()
	{
		return new Object[][] {
			{ "{0}", "a.b.c", "", "a.b.c.yaml" },
			{ "{1}", "", "MySuite", "MySuite.yaml" },
		};
	}
}
