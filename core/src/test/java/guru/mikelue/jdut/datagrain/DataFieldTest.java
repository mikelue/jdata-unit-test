package guru.mikelue.jdut.datagrain;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

public class DataFieldTest {
	public DataFieldTest() {}

	/**
	 * Tests the consturctor of object(by supplier).
	 */
	@ParameterizedTest
	@MethodSource
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

		assertEquals(sampleColumn, testedField.getColumn());
		assertEquals(sampleSupplier.get(), testedField.getData());
	}
	static Arguments[] dataFieldBySupplier()
	{
		return new Arguments[] {
			arguments((Supplier<Integer>)() -> 20),
			arguments((Supplier<Integer>)() -> null),
		};
	}

	/**
	 * Tests the consturctor of object(by value).
	 */
	@ParameterizedTest
	@MethodSource
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

		assertEquals(sampleColumn, testedField.getColumn());
		assertEquals(expectedValue, testedField.getData());
	}
	static Arguments[] dataFieldByValue()
	{
		return new Arguments[] {
			arguments(84, 84),
			arguments(null, null),
			arguments((Supplier<Integer>)() -> 77, 77), // Tests the auto-detected lambda expression
		};
	}

	/**
	 * Tests the keeping of value by supplier.
	 */
	@ParameterizedTest
	@MethodSource
	public void keepValue(
		final Optional<Integer> sampleValue
	) {
		Supplier<Integer> onceSupplier = new Supplier<Integer>() {
			boolean run = false;

			@Override
			public Integer get()
			{
				if (run) {
					fail("The supplier has been run");
				}

				run = true;

				return sampleValue.orElse(null);
			}
		};

		DataField.Factory fieldFactory = new DataField.Factory(
			SchemaTable.build(builder -> builder.name("gm_1"))
		);
		DataField<Integer> testedField = fieldFactory.composeDataSupplier(
			SchemaColumn.build(builder -> builder.name("col_once")),
			onceSupplier
		);

		assertEquals(sampleValue.orElse(null), testedField.getData());
		testedField.getData(); // Should use keeped value
	}
	static Arguments[] keepValue()
	{
		return new Arguments[] {
			arguments(Optional.of(77)),
			arguments(Optional.<Integer>empty())
		};
	}
}
