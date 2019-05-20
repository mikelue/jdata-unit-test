// tag::package[]
package guru.mikelue.jdut.testng.example;
// end::package[]

import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.testng.IInvokedMethodYamlFactoryListener;
import guru.mikelue.jdut.testng.TestNGConfig;
import guru.mikelue.jdut.testng.YamlFactoryListenerBase;

// tag::test_class[]
@Test(suiteName="TurtleSuite")
@Listeners(IInvokedMethodYamlFactoryListener.class) // <1>
public class TurtleTest {
	public TurtleTest() {}

	@BeforeClass // <2>
	void setupDataSource(ITestContext context)
	{
		YamlFactoryListenerBase.setDataSource(context, DataSourceGetter.get());
	}
	@AfterClass // <3>
	void releaseDataSource(ITestContext context)
	{
		YamlFactoryListenerBase.removeDataSource(context);
	}

	// File: classpath:guru/mikelue/jdut/testng/example
	// 		-> TurtleTest-crawl.yaml
	@Test @JdutResource // <4>
	public void crawl()
	{
		// Executes tested code
		// Assertions ...
	}

	// tag::multi_times[]
	// clFile: classpath:guru/mikelue/jdut/testng/example
	// 		-> TurtleTest-sleep.yaml
	@Test(dataProvider="sleep") @JdutResource
	@TestNGConfig(oneTimeOnly=true) // <1> <2>
	public void sleep(int time)
	{
		// Executes tested code
		// Assertions ...
	}
	@DataProvider
	Object[][] sleep()
	{
		return new Object[][] {
			{ 20 }, { 40 }, { 60}, { 80 },
		};
	}
	// end::multi_times[]
}
// end::test_class[]
