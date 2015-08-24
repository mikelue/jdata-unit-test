package guru.mikelue.jdut.function;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import guru.mikelue.jdut.datagrain.DataField;
import guru.mikelue.jdut.datagrain.SchemaColumn;
import guru.mikelue.jdut.datagrain.SchemaTable;

public class DataFieldPredicatesTest {
	public DataFieldPredicatesTest() {}

	/**
	 * Test the predicates of null value.
	 */
	@Test(dataProvider="NullValue")
	public void nullValue(
		Integer sampleValue,
		boolean expectedResult
	) {
		Predicate<DataField<Integer>> nullValuePredicate = DataFieldPredicates.nullValue(
			"tab_p1", "col_1"
		);

		DataField<Integer> sampleDataField = new DataField.Factory(
			SchemaTable.build(tableBuilder -> tableBuilder.name("tab_p1"))
		).composeData(
			SchemaColumn.build(builder -> builder.name("col_1")),
			sampleValue
		);

		Assert.assertEquals(nullValuePredicate.test(sampleDataField), expectedResult);
	}
	@DataProvider(name="NullValue")
	private Object[][] getNullValue()
	{
		return new Object[][] {
			{ 30, false },
			{ null, true },
		};
	}

	/**
	 * Tests the non-supplier predicate.
	 */
	@Test(dataProvider="NonSupplier")
	public void nonSupplier(
		Object sampleData, boolean expectedResult
	) {
		Predicate<DataField<Object>> testedPredicate = DataFieldPredicates::nonSupplier;

		DataField<Object> sampleDataField = new DataField.Factory(
			SchemaTable.build(tableBuilder -> tableBuilder.name("tab_p1"))
		).composeData(
			SchemaColumn.build(builder -> builder.name("col_1")),
			sampleData
		);
		Assert.assertEquals(testedPredicate.test(sampleDataField), expectedResult);
	}
	@DataProvider(name="NonSupplier")
	private Object[][] getNonSupplier()
	{
		return new Object[][] {
			{ 67, true },
			{ (Supplier<Integer>)() -> 56, false }
		};
	}
}
