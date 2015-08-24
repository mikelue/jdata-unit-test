package guru.mikelue.jdut.datagrain;

import java.util.List;
import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests RowsBuilderImpl.
 */
public class DataGrainTest {
	public DataGrainTest() {}

	/**
	 * Tests the build of object.
	 */
	@Test
	public void build()
	{
		DataGrain testedDataGrain = DataGrain.build(
			tableBuilder -> tableBuilder
				.name("tb_thing"),
			rowBuilder -> rowBuilder
				.implicitColumns("ct_1", "ct_2")
				/**
				 * Implicit data
				 */
				.addValues(1, "BANANA-01")
				.addValues(2, (Supplier<String>)() -> "BANANA-02")
				// :~)
				/**
				 * Explicit data
				 */
				.addFields(
					rowBuilder.newField("ct_1", 11),
					rowBuilder.newField("ct_2", "PINEAPPLE-02"),
					rowBuilder.newField("ct_3", (String)null)
				)
				.addFields(
					rowBuilder.newField("ct_1", 12),
					rowBuilder.newField("ct_3", () -> "PINEAPPLE-03")
				)
				.addValues(null, null)
				// :~)
		);

		List<DataRow> testedRows = testedDataGrain.getRows();
		testedRows.forEach(
			row -> Assert.assertEquals(row.getTable().getName(), "tb_thing")
		);
		Assert.assertEquals(testedRows.get(0).getData("ct_1"), new Integer(1));
		Assert.assertEquals(testedRows.get(0).getData("ct_2"), "BANANA-01");
		Assert.assertEquals(testedRows.get(1).getData("ct_1"), new Integer(2));
		Assert.assertEquals(testedRows.get(1).getData("ct_2"), "BANANA-02");
		Assert.assertEquals(testedRows.get(2).getData("ct_1"), new Integer(11));
		Assert.assertEquals(testedRows.get(2).getData("ct_2"), "PINEAPPLE-02");
		Assert.assertEquals(testedRows.get(2).getData("ct_3"), (String)null);
		Assert.assertEquals(testedRows.get(3).getData("ct_1"), new Integer(12));
		Assert.assertEquals(testedRows.get(3).getData("ct_3"), "PINEAPPLE-03");
		Assert.assertEquals(testedRows.get(4).getData("ct_1"), (Integer)null);
		Assert.assertEquals(testedRows.get(4).getData("ct_2"), (String)null);
	}

	/**
	 * Tests aggregate.
	 */
	@Test
	public void aggregate()
	{
		DataGrain testedDataGrain = DataGrain.build(
			tableBuilder -> tableBuilder
				.name("tb_thing_1"),
			rowBuilder -> rowBuilder
				.implicitColumns("t1_c1", "t1_c2")
				.addValues(1, "OK-1")
		).aggregate(
			DataGrain.build(
				tableBuilder -> tableBuilder
					.name("tb_thing_2"),
				rowBuilder -> rowBuilder
					.implicitColumns("t2_c1", "t2_c2")
					.addValues(2, "OK-2")
			)
		);

		List<DataRow> testedRows = testedDataGrain.getRows();
		Assert.assertEquals(testedRows.get(0).getTable().getName(), "tb_thing_1");
		Assert.assertEquals(testedRows.get(0).getData("t1_c1"), new Integer(1));
		Assert.assertEquals(testedRows.get(0).getData("t1_c2"), "OK-1");
		Assert.assertEquals(testedRows.get(1).getTable().getName(), "tb_thing_2");
		Assert.assertEquals(testedRows.get(1).getData("t2_c1"), new Integer(2));
		Assert.assertEquals(testedRows.get(1).getData("t2_c2"), "OK-2");
	}

	/**
	 * Tests the decoration of data grain.
	 */
	@Test
	public void decorate()
	{
		DataGrain testedDataGrain = DataGrain.build(
			tableSchema -> tableSchema.name("tab_1"),
			rowsBuilder -> rowsBuilder
				.implicitColumns("col_1", "col_2")
				.addValues("v1", "v2")
				.addValues("v1", "v2") // Another row to be decorated
		);

		testedDataGrain = testedDataGrain.decorate(
			rowBuilder -> rowBuilder
				.fieldOfValue("col_1", "v1-1") // Modifies existing field
				.fieldOfValue("col_3", "v3") // Adds new field
		);

		Assert.assertEquals(testedDataGrain.getRow(0).<String>getData("col_1"), "v1-1"); // Modified field
		Assert.assertEquals(testedDataGrain.getRow(0).<String>getData("col_2"), "v2"); // Nothing changed
		Assert.assertEquals(testedDataGrain.getRow(0).<String>getData("col_3"), "v3"); // Added field
		Assert.assertEquals(testedDataGrain.getRow(1).<String>getData("col_1"), "v1-1"); // Modified field
		Assert.assertEquals(testedDataGrain.getRow(1).<String>getData("col_2"), "v2"); // Nothing changed
		Assert.assertEquals(testedDataGrain.getRow(1).<String>getData("col_3"), "v3"); // Added field
	}
}
