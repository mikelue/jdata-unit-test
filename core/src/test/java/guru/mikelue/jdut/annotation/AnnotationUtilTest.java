package guru.mikelue.jdut.annotation;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.vendor.DatabaseVendor;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

public class AnnotationUtilTest extends AbstractDataSourceTestBase {
	public AnnotationUtilTest() {}

	/**
	 * Tests the matching for annotation {@link IfDatabaseVendor}.
	 */
	@Test(dataProvider="MatchDatabaseVendor")
	public void matchDatabaseVendor(
		String sampleMethodName, DatabaseVendor checkedVendor,
		boolean expectedResult
	) throws NoSuchMethodException {
		IfDatabaseVendor sampleValueOfAnnotation = SampleForIfDatabaseVendor.class.getMethod(sampleMethodName)
			.getAnnotation(IfDatabaseVendor.class);

		Assert.assertEquals(
			AnnotationUtil.matchDatabaseVendor(
				checkedVendor, sampleValueOfAnnotation
			),
			expectedResult
		);
	}
	@DataProvider(name="MatchDatabaseVendor")
	private Object[][] getMatchDatabaseVendor()
	{
		return new Object[][] {
			{ "nullMethod", DatabaseVendor.H2, true },
			{ "defaultMethod", DatabaseVendor.H2, true },
			{ "matchOne", DatabaseVendor.H2, true },
			{ "matchOne", DatabaseVendor.MySql, false },
			{ "notMatchOne", DatabaseVendor.H2, false },
			{ "notMatchOne", DatabaseVendor.MySql, true },
			{ "contradict", DatabaseVendor.H2, false },
			{ "contradict", DatabaseVendor.MySql, false },
			{ "multiple", DatabaseVendor.Oracle, true },
			{ "multiple", DatabaseVendor.H2, false },
			{ "multiple", DatabaseVendor.PostgreSql, false },
		};
	}

	/**
	 * Tests the building of duet conductor by convention.
	 */
	@Test(dataProvider="BuildConductorByConventionAndMethod")
	public void buildConductorByConventionAndMethod(
		String sampleMethodName,
		boolean hasConductor
	) throws NoSuchMethodException {
		YamlConductorFactory sampleFactory = YamlConductorFactory.build(getDataSource());

		Assert.assertEquals(
			AnnotationUtil.buildConductorByConvention(
				sampleFactory, AnnotationUtilTest.class.getMethod(sampleMethodName)
			).isPresent(),
			hasConductor
		);
	}
	@DataProvider(name="BuildConductorByConventionAndMethod")
	private Object[][] getBuildConductorByConventionAndMethod()
	{
		return new Object[][] {
			{ "withJdutResource", true },
			{ "withoutJdutResource", false }
		};
	}

	@Test(enabled=false) @JdutResource
	public void withJdutResource() {}
	@Test(enabled=false)
	public void withoutJdutResource() {}

	/**
	 * Tests the building of duet conductor by convention.
	 */
	@Test(dataProvider="BuildConductorByConventionAndClass")
	public void buildConductorByConventionAndClass(
		Class<?> sampleClass,
		boolean hasConductor
	) {
		YamlConductorFactory sampleFactory = YamlConductorFactory.build(getDataSource());

		Assert.assertEquals(
			AnnotationUtil.buildConductorByConvention(
				sampleFactory, sampleClass
			).isPresent(),
			hasConductor
		);
	}
	@DataProvider(name="BuildConductorByConventionAndClass")
	private Object[][] getBuildConductorByConventionAndClass()
	{
		return new Object[][] {
			{ WithAnnotation.class, true },
			{ WithoutAnnotation.class, false }
		};
	}
}

@JdutResource
class WithAnnotation {}
class WithoutAnnotation {}

interface SampleForIfDatabaseVendor {
	void nullMethod();
	@IfDatabaseVendor
	void defaultMethod();
	@IfDatabaseVendor(match=DatabaseVendor.H2)
	void matchOne();
	@IfDatabaseVendor(notMatch=DatabaseVendor.H2)
	void notMatchOne();
	@IfDatabaseVendor(match=DatabaseVendor.H2, notMatch=DatabaseVendor.H2)
	void contradict();
	@IfDatabaseVendor(match={DatabaseVendor.Oracle, DatabaseVendor.MsSql}, notMatch={DatabaseVendor.H2, DatabaseVendor.Derby})
	void multiple();
}
