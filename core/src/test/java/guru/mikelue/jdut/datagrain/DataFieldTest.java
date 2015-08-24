package guru.mikelue.jdut.datagrain;

import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class DataFieldTest {
	public DataFieldTest() {}

	/**
	 * Tests the consturctor of object(by supplier).
	 */
	@Test(dataProvider="DataFieldBySupplier")
	public void dataFieldBySupplier(
		Supplier<Integer> sampleSupplier
	) {
		DataField.Factory fieldFactory = new DataField.Factory(
			SchemaTable.build(builder -> builder.name("tc_1"))
		);
		SchemaColumn sampleColumn = SchemaColumn.build(builder -> builder.name("col_33"));

		DataField<Integer> testedField = fieldFactory.composeDataSupplier(
			sampleColumn, sampleSupplier
		);

		Assert.assertEquals(testedField.getColumn(), sampleColumn);
		Assert.assertEquals(testedField.getData(), sampleSupplier.get());
	}
	@DataProvider(name="DataFieldBySupplier")
	private Object[][] getDataFieldBySupplier()
	{
		return new Object[][] {
			{ (Supplier<Integer>)() -> 20 },
			{ (Supplier<Integer>)() -> null },
		};
	}

	/**
	 * Tests the consturctor of object(by value).
	 */
	@Test(dataProvider="DataFieldByValue")
	public void dataFieldByValue(
		Object sampleData, Integer expectedValue
	) {
		DataField.Factory fieldFactory = new DataField.Factory(
			SchemaTable.build(builder -> builder.name("tc_1"))
		);
		SchemaColumn sampleColumn = SchemaColumn.build(builder -> builder.name("col_24"));

		DataField<Object> testedField = fieldFactory.composeData(
			sampleColumn, sampleData
		);

		Assert.assertEquals(testedField.getColumn(), sampleColumn);
		Assert.assertEquals(testedField.getData(), expectedValue);
	}
	@DataProvider(name="DataFieldByValue")
	private Object[][] getDataFieldByValue()
	{
		return new Object[][] {
			{ 84, 84 },
			{ null, null },
			{ (Supplier<Integer>)() -> 77, 77 }, // Tests the auto-detected lambda expression
		};
	}

	/**
	 * Tests the keeping of value by supplier.
	 */
	@Test(dataProvider="KeepValue")
	public void keepValue(
		final Integer sampleValue
	) {
		Supplier<Integer> onceSupplier = new Supplier<Integer>() {
			boolean run = false;

			@Override
			public Integer get()
			{
				if (run) {
					Assert.fail("The supplier has been run");
				}

				run = true;

				return sampleValue;
			}
		};

		DataField.Factory fieldFactory = new DataField.Factory(
			SchemaTable.build(builder -> builder.name("gm_1"))
		);
		DataField<Integer> testedField = fieldFactory.composeDataSupplier(
			SchemaColumn.build(builder -> builder.name("col_once")),
			onceSupplier
		);

		Assert.assertEquals(testedField.getData(), sampleValue);
		testedField.getData(); // Should use keeped value
	}
	@DataProvider(name="KeepValue")
	private Object[][] getKeepValue()
	{
		return new Object[][] {
			{ 77 },
			{ null }
		};
	}
}
