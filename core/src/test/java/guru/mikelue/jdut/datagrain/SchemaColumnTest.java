package guru.mikelue.jdut.datagrain;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SchemaColumnTest {
	public SchemaColumnTest() {}

	/**
	 * Tests the building of object.
	 */
	@Test(dataProvider="Build")
	public void build(
		final Integer sampleSqlType
	) {
		final String sampleName = "vc_1";

		SchemaColumn testedColumn = SchemaColumn.build(
			builder -> builder
				.name(sampleName)
				.sqlType(sampleSqlType)
		);

		Assert.assertEquals(testedColumn.getName(), sampleName);
		Assert.assertEquals(testedColumn.getSqlType().orElse(null), sampleSqlType);
	}
	@DataProvider(name="Build")
	private Object[][] getBuild()
	{
		return new Object[][] {
			{ 5 },
			{ null }
		};
	}
}
