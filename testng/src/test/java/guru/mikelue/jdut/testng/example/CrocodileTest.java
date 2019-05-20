// tag::package[]
package guru.mikelue.jdut.testng.example;
// end::package[]

import org.testng.ITestContext;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.testng.ITestContextYamlFactoryListener;

// tag::test_class[]
// File: classpath:CrocodileTest.yaml
@Test(suiteName="CrocodileSuite", testName="CrocodileTest") // <1>
@Listeners(CrocodileTest.CrocodileSuiteListener.class) // <2>
public class CrocodileTest {
	// tag::listener[]
	public static class CrocodileSuiteListener extends ITestContextYamlFactoryListener {
		 @Override // <1>
         public void onStart(ITestContext testContext)
         {
             setDataSource(testContext, DataSourceGetter.get());
             super.onStart(testContext); // <2>
         }
         @Override // <3>
         public void onFinish(ITestContext testContext)
         {
             super.onFinish(testContext); // <4>
             removeDataSource(testContext);
         }
	}
	// end::listener[]

	@Test
	public void crawl()
	{
		// Executes tested code
		// Assertions ...
	}
}
// end::test_class[]
