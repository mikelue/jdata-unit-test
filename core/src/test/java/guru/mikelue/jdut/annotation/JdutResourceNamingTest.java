package guru.mikelue.jdut.annotation;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class JdutResourceNamingTest {
	public JdutResourceNamingTest() {}

	/**
	 * Tests the text for generating resource name.
	 */
	@Test(dataProvider="NamingBySourceClass")
	public void namingBySourceClass(
		String sampleFormatting, String expectedResult
	) {
		Assert.assertEquals(
			JdutResourceNaming.naming(
				sampleFormatting, SampleResourceClass.class,
				".yaml"
			),
			expectedResult
		);
	}
	@DataProvider(name="NamingBySourceClass")
	private Object[][] getNamingBySourceClass()
	{
		return new Object[][] {
			{ "{0}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest$SampleResourceClass.yaml" },
			{ "{1}", "SampleResourceClass.yaml" },
			{ "{2}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest.SampleResourceClass.yaml" },
			{ "{3}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest$SampleResourceClass.yaml" },
		};
	}

	/**
	 * Tests the text for generating resource name.
	 */
	@Test(dataProvider="NamingByMethod")
	public void namingByMethod(
		String sampleFormatting, String expectedResult
	) throws NoSuchMethodException {
		Assert.assertEquals(
			JdutResourceNaming.naming(
				sampleFormatting, SampleResourceClass.class.getMethod("sampleMethod"),
				".yaml"
			),
			expectedResult
		);
	}
	@DataProvider(name="NamingByMethod")
	private Object[][] getNamingByMethod()
	{
		return new Object[][] {
			{ "{0}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest$SampleResourceClass.yaml" },
			{ "{1}", "SampleResourceClass.yaml" },
			{ "{2}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest.SampleResourceClass.yaml" },
			{ "{3}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest$SampleResourceClass.yaml" },
			{ "{4}", "sampleMethod.yaml" },
		};
	}

	public static class SampleResourceClass {
		public void sampleMethod() {}
	}
}
