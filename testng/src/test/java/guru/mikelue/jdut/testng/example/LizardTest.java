// tag::package[]
package guru.mikelue.jdut.testng.example;
// end::package[]

import org.testng.ISuite;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import guru.mikelue.jdut.testng.ISuiteYamlFactoryListener;

// tag::test_class[]
// File: LizardSuite.yaml
@Test(suiteName="LizardSuite") // <1>
@Listeners(LizardTest.LizardSuiteListener.class) // <2>
public class LizardTest {
	// tag::listener[]
	public static class LizardSuiteListener extends ISuiteYamlFactoryListener {
		 @Override // <1>
         public void onStart(ISuite suite)
         {
             setDataSource(suite, DataSourceGetter.get());
             super.onStart(suite); // <2>
         }
         @Override // <3>
         public void onFinish(ISuite suite)
         {
             super.onFinish(suite); // <4>
             removeDataSource(suite);
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
