package guru.mikelue.jdut.datagrain;

import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class DataRowTest {
	public DataRowTest() {}

	/**
	 * Tests the building of object.
	 */
	@Test
	public void build()
	{
		final SchemaTable sampleTableSchema = SchemaTable.build(builder -> builder.name("gt_apple"));
		final DataField.Factory fieldFactory = new DataField.Factory(sampleTableSchema);
		final Map<String, DataField<?>> sampleData = Stream.of(
			fieldFactory.composeData(
				SchemaColumn.build(builder -> builder.name("col_1")), 30
			).asMapEntry(),
			fieldFactory.composeData(
				SchemaColumn.build(builder -> builder.name("col_2")), "EXP-01"
			).asMapEntry()
		).collect(
			Collectors.toMap(
				entry -> entry.getKey(),
				entry -> entry.getValue()
			)
		);

		DataRow testedRow = DataRow.build(
			builder -> builder
				.tableSchema(sampleTableSchema)
				.data(sampleData)
		);

		assertEquals(sampleTableSchema, testedRow.getTable());
		assertEquals(Integer.valueOf(30), testedRow.<Integer>getDataField("col_1").getData());
		assertEquals("EXP-01", testedRow.<String>getDataField("col_2").getData());

		/**
		 * Asserts the changing of table schema
		 */
		testedRow = DataRow.build(
			builder -> builder
				.tableSchema(SchemaTable.build(tableBuilder -> tableBuilder.name("gt_table2"))),
			testedRow
		);
		assertEquals("gt_table2", testedRow.getTable().getName());
		assertEquals("gt_table2", testedRow.<Integer>getDataField("col_1").getTableName());
		// :~)
	}

	/**
	 * Tests the building from existing row.
	 */
	@Test
	public void buildExistingRow()
	{
		DataRow testedRow = DataRow.build(
			builder -> builder
				.tableSchema(SchemaTable.build(tableBuilder -> tableBuilder.name("gc_apple2")))
				.fieldOfValue("ct_1", 20)
				.fieldOfValue("ct_2", "Exp")
				.fieldOfValueSupplier("ct_3", () -> 77)
				.fieldOfValue("ct_4", 54) // No changed
		);

		testedRow = DataRow.build(
			builder -> {
				Supplier<Integer> sourceSupplier = builder.<Integer>getDataSupplier("ct_3").get();
				Supplier<Integer> wrappedSupplier = () -> 20 + sourceSupplier.get();

				builder
					.fieldOfValue("ct_1", 40)
					.fieldOfValue("ct_2", builder.<String>getData("ct_2").get() + "-77")
					/**
					 * Wrap the value supplier
					 */
					.fieldOfValueSupplier("ct_3", wrappedSupplier);
			},
				// :~)
			testedRow
		);

		assertEquals("gc_apple2", testedRow.getTable().getName());
		assertEquals(Integer.valueOf(40), testedRow.getData("ct_1"));
		assertEquals("Exp-77", testedRow.getData("ct_2"));
		assertEquals(Integer.valueOf(97), testedRow.getData("ct_3"));
		assertEquals(Integer.valueOf(54), testedRow.getData("ct_4"));
	}

	/**
	 * Tests the validation of data row(succeeded).
	 */
	@Test
	public void validateWithSucceeding() throws DataRowException
	{
		DataRow testedRow = DataRow.build(
			builder -> builder
				.tableSchema(SchemaTable.build(tableBuilder -> tableBuilder
					.name("va_02")
					.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("ec_1")))
					.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("ec_2")))
					.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("ec_3")))
				))
				.fieldOfValue("ec_1", 30)
				.fieldOfValue("ec_2", 20)
		);

		testedRow.validate();
		assertTrue(testedRow.isValidated());
	}

	/**
	 * Tests the validatation of data row(failed).
	 */
	@Test
	public void validateWithError() throws DataRowException
	{
		DataRow testedRow = DataRow.build(
			builder -> builder
				.tableSchema(
					SchemaTable.build(
						tableBuilder -> tableBuilder.name("va_01")
				))
				.fieldOfValue("ec_1", 20)
		);

		assertFalse(testedRow.isValidated());

		assertThrows(MissedColumnException.class, testedRow::validate);
	}
}
