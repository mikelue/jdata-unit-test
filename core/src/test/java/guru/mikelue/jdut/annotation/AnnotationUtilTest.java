package guru.mikelue.jdut.annotation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.vendor.DatabaseVendor;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

public class AnnotationUtilTest extends AbstractDataSourceTestBase {
	public AnnotationUtilTest() {}

	/**
	 * Tests the matching for annotation {@link IfDatabaseVendor}.
	 */
	@ParameterizedTest
	@MethodSource
	public void matchDatabaseVendor(
		String sampleMethodName, DatabaseVendor checkedVendor,
		boolean expectedResult
	) throws NoSuchMethodException {
		IfDatabaseVendor sampleValueOfAnnotation = SampleForIfDatabaseVendor.class.getMethod(sampleMethodName)
			.getAnnotation(IfDatabaseVendor.class);

		assertEquals(
			expectedResult,
			AnnotationUtil.matchDatabaseVendor(
				checkedVendor, sampleValueOfAnnotation
			)
		);
	}
	static Arguments[] matchDatabaseVendor()
	{
		return new Arguments[] {
			arguments("nullMethod", DatabaseVendor.H2, true),
			arguments("defaultMethod", DatabaseVendor.H2, true),
			arguments("matchOne", DatabaseVendor.H2, true),
			arguments("matchOne", DatabaseVendor.MySql, false),
			arguments("notMatchOne", DatabaseVendor.H2, false),
			arguments("notMatchOne", DatabaseVendor.MySql, true),
			arguments("contradict", DatabaseVendor.H2, false),
			arguments("contradict", DatabaseVendor.MySql, false),
			arguments("multiple", DatabaseVendor.Oracle, true),
			arguments("multiple", DatabaseVendor.H2, false),
			arguments("multiple", DatabaseVendor.PostgreSql, false),
		};
	}

	/**
	 * Tests the building of duet conductor by convention.
	 */
	@ParameterizedTest
	@MethodSource
	public void buildConductorByConventionAndMethod(
		String sampleMethodName,
		boolean hasConductor
	) throws NoSuchMethodException {
		YamlConductorFactory sampleFactory = YamlConductorFactory.build(getDataSource());

		assertEquals(
			hasConductor,
			AnnotationUtil.buildConductorByConvention(
				sampleFactory, AnnotationUtilTest.class.getMethod(sampleMethodName)
			).isPresent()
		);
	}
	static Arguments[] buildConductorByConventionAndMethod()
	{
		return new Arguments[] {
			arguments("withJdutResource", true),
			arguments("withoutJdutResource", false),
		};
	}

	@JdutResource
	public void withJdutResource() {}
	public void withoutJdutResource() {}

	/**
	 * Tests the building of duet conductor by convention.
	 */
	@ParameterizedTest
	@MethodSource
	public void buildConductorByConventionAndClass(
		Class<?> sampleClass,
		boolean hasConductor
	) {
		YamlConductorFactory sampleFactory = YamlConductorFactory.build(getDataSource());

		assertEquals(
			hasConductor,
			AnnotationUtil.buildConductorByConvention(
				sampleFactory, sampleClass
			).isPresent()
		);
	}
	static Arguments[] buildConductorByConventionAndClass()
	{
		return new Arguments[] {
			arguments(WithAnnotation.class, true),
			arguments(WithoutAnnotation.class, false)
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
