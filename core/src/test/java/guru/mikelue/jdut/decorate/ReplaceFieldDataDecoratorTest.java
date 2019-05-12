package guru.mikelue.jdut.decorate;

import org.junit.jupiter.api.Test;

import guru.mikelue.jdut.datagrain.DataGrain;

import static org.junit.jupiter.api.Assertions.*;

public class ReplaceFieldDataDecoratorTest {
	public ReplaceFieldDataDecoratorTest() {}

	/**
	 * Tests the replacement decorator.
	 */
	@Test
	public void build()
	{
		DataGrainDecorator testedDecorator = ReplaceFieldDataDecorator.buildDataGrainDecorator(
			builder -> builder
				.replaceWith(dataField -> dataField.getColumnName().equals("gc_1"), "MT-01")
				.replaceWith("[V1]", 20)
				.replaceWith("[NULL]", null) // Replaces to null value
				.replaceWith("[V3]", 100) // Unused
		);

		DataGrain sampleDataGrain = DataGrain.build(
			tableSchema -> tableSchema.name("tab_008"),
			rowsBuilder -> rowsBuilder
				.implicitColumns("gc_1", "gc_2", "gc_3", "gc_4")
				.addValues("MT-UN-01", "[V1]", 98, "[NULL]")
		).decorate(testedDecorator);

		assertEquals("MT-01", sampleDataGrain.getRow(0).getData("gc_1"));
		assertEquals(Integer.valueOf(20), sampleDataGrain.getRow(0).getData("gc_2"));
		assertEquals(Integer.valueOf(98), sampleDataGrain.getRow(0).getData("gc_3"));
		assertEquals((Integer)null, sampleDataGrain.getRow(0).getData("gc_4"));
	}
}
