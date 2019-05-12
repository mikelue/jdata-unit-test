package guru.mikelue.jdut.function;

import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import guru.mikelue.jdut.datagrain.DataField;
import guru.mikelue.jdut.datagrain.SchemaColumn;
import guru.mikelue.jdut.datagrain.SchemaTable;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

public class DataFieldPredicatesTest {
	public DataFieldPredicatesTest() {}

	/**
	 * Test the predicates of null value.
	 */
	@ParameterizedTest
	@MethodSource
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

		assertEquals(expectedResult, nullValuePredicate.test(sampleDataField));
	}
	static Arguments[] nullValue()
	{
		return new Arguments[] {
			arguments(30, false),
			arguments(null, true),
		};
	}

	/**
	 * Tests the non-supplier predicate.
	 */
	@ParameterizedTest
	@MethodSource
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
		assertEquals(expectedResult, testedPredicate.test(sampleDataField));
	}
	static Arguments[] nonSupplier()
	{
		return new Arguments[] {
			arguments(67, true),
			arguments((Supplier<Integer>)() -> 56, false)
		};
	}
}
