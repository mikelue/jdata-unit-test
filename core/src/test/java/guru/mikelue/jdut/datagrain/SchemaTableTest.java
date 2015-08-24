package guru.mikelue.jdut.datagrain;

import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SchemaTableTest {
	public SchemaTableTest() {}

	/**
	 * Tests the building of object.
	 */
	@Test(dataProvider="Build")
	public void build(
		String sampleTableName, String expectedTableName,
		String[] sampleKeys, List<String> expectedKeys
	) {
		SchemaTable testedTableSchema = SchemaTable.build(
			builder -> builder
				.name(sampleTableName)
				.keys(sampleKeys)
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("dc_1")))
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("dc_2")))
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("dc_3")))
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("dc_2"))) // Put again
		);

		Assert.assertEquals(testedTableSchema.getName(), expectedTableName);
		Assert.assertEquals(testedTableSchema.getKeys(), expectedKeys);
		Assert.assertEquals(testedTableSchema.getColumn(0).getName(), "dc_1");
		Assert.assertEquals(testedTableSchema.getColumn(1).getName(), "dc_2");
		Assert.assertEquals(testedTableSchema.getColumn(2).getName(), "dc_3");
	}
	@DataProvider(name="Build")
	private Object[][] get()
	{
		return new Object[][] {
			{ "gt_car", "gt_car",
				new String[] { "col_1", "col_2" },
				Arrays.asList("col_1", "col_2")
			},
			{ "  gt_car  ", "gt_car",
				new String[] { " col_4 ", null, "", " col_3 " },
				Arrays.asList("col_4", "col_3")
			}
		};
	}
}
