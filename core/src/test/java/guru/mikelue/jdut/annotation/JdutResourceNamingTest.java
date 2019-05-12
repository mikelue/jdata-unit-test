package guru.mikelue.jdut.annotation;

import org.testng.Assert;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

public class JdutResourceNamingTest {
	public JdutResourceNamingTest() {}

	/**
	 * Tests the text for generating resource name.
	 */
	@ParameterizedTest
	@MethodSource
	public void namingBySourceClass(
		String sampleFormatting, String expectedResult
	) {
		assertEquals(
			expectedResult,
			JdutResourceNaming.naming(
				sampleFormatting, SampleResourceClass.class,
				".yaml"
			)
		);
	}
	static Arguments[] namingBySourceClass()
	{
		return new Arguments[] {
			arguments("{0}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest$SampleResourceClass.yaml"),
			arguments("{1}", "SampleResourceClass.yaml"),
			arguments("{2}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest.SampleResourceClass.yaml"),
			arguments("{3}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest$SampleResourceClass.yaml"),
		};
	}

	/**
	 * Tests the text for generating resource name.
	 */
	@ParameterizedTest
	@MethodSource
	public void namingByMethod(
		String sampleFormatting, String expectedResult
	) throws NoSuchMethodException {
		assertEquals(
			expectedResult,
			JdutResourceNaming.naming(
				sampleFormatting, SampleResourceClass.class.getMethod("sampleMethod"),
				".yaml"
			)
		);
	}
	static Arguments[] namingByMethod()
	{
		return new Arguments[] {
			arguments("{0}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest$SampleResourceClass.yaml"),
			arguments("{1}", "SampleResourceClass.yaml"),
			arguments("{2}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest.SampleResourceClass.yaml"),
			arguments("{3}", "guru.mikelue.jdut.annotation.JdutResourceNamingTest$SampleResourceClass.yaml"),
			arguments("{4}", "sampleMethod.yaml"),
		};
	}

	public static class SampleResourceClass {
		public void sampleMethod() {}
	}
}
