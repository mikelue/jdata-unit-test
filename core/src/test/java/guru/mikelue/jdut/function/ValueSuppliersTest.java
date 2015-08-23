package guru.mikelue.jdut.function;

import java.util.function.Supplier;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ValueSuppliersTest {
	public ValueSuppliersTest() {}

	/**
	 * Tests the cached value of value supplier.
	 */
	@Test(dataProvider="CachedValue")
	public void cachedValue(
		final Integer sampleValue
	) {
		Supplier<Integer> testedSupplier = ValueSuppliers.cachedValue(
			new Supplier<Integer>() {
				boolean run = false;

				@Override
				public Integer get()
				{
					if (run) {
						Assert.fail("The supplier is evaluated again");
					}

					run = true;

					return sampleValue;
				}
			}
		);

		Assert.assertEquals(testedSupplier.get(), sampleValue);
		testedSupplier.get(); // Should use cached value
	}
	@DataProvider(name="CachedValue")
	private Object[][] getCachedValue()
	{
		return new Object[][] {
			{ 30 },
			{ null },
		};
	}
}
