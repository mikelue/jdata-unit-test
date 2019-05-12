package guru.mikelue.jdut.function;

import java.util.function.Supplier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

public class ValueSuppliersTest {
	public ValueSuppliersTest() {}

	/**
	 * Tests the cached value of value supplier.
	 */
	@ParameterizedTest
	@MethodSource
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
						fail("The supplier is evaluated again");
					}

					run = true;

					return sampleValue;
				}
			}
		);

		assertEquals(sampleValue, testedSupplier.get());
		testedSupplier.get(); // Should use cached value
	}
	static Arguments[] cachedValue()
	{
		return new Arguments[] {
			arguments(30),
			arguments((Object)null),
		};
	}
}
