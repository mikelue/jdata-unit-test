package guru.mikelue.jdut.datagrain;

import java.sql.JDBCType;

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
		final JDBCType sampleJdbcType
	) {
		final String sampleName = "vc_1";

		SchemaColumn testedColumn = SchemaColumn.build(
			builder -> builder
				.name(sampleName)
				.jdbcType(sampleJdbcType)
		);

		Assert.assertEquals(testedColumn.getName(), sampleName);
		Assert.assertEquals(testedColumn.getJdbcType().orElse(null), sampleJdbcType);
	}
	@DataProvider(name="Build")
	private Object[][] getBuild()
	{
		return new Object[][] {
			{ JDBCType.BIGINT },
			{ null }
		};
	}
}
