package guru.mikelue.jdut.decorate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static guru.mikelue.jdut.vendor.DatabaseVendor.H2;

import java.sql.JDBCType;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import guru.mikelue.jdut.annotation.IfDatabaseVendor;
import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.datagrain.SchemaColumn;
import guru.mikelue.jdut.datagrain.SchemaTable;
import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.test.DoLiquibase;

public class TableSchemaLoadingDecoratorTest extends AbstractDataSourceTestBase {
	public TableSchemaLoadingDecoratorTest() {}

	@Test @DoLiquibase @IfDatabaseVendor(match=H2)
	public void decorateWithSchemaInformation()
	{
		final DataGrainDecorator testedDecorator = new TableSchemaLoadingDecorator(
			getDataSource()
		);

		DataRow testedRow = DataRow.build(
			builder -> {
				builder.tableSchema(
					SchemaTable.build(tableBuilder -> tableBuilder.name("green.broccoli"))
				);

				testedDecorator.decorate(builder);
			}
		);

		SchemaTable tableSchema = testedRow.getTable();
		assertEquals(2, tableSchema.getNumberOfColumns());
		assertColumn(
			tableSchema, "size",
			JDBCType.INTEGER, true, false
		);
		assertColumn(
			tableSchema, "width",
			JDBCType.INTEGER, true, false
		);
	}

	/**
	 * Tests the decoration of schema loading.
	 */
	@Test @DoLiquibase
	public void decorate()
	{
		final DataGrainDecorator testedDecorator = new TableSchemaLoadingDecorator(
			getDataSource()
		);

		DataRow testedRow = DataRow.build(
			builder -> {
				builder.tableSchema(
					SchemaTable.build(tableBuilder -> tableBuilder.name("tab_se"))
				);

				testedDecorator.decorate(builder);
			}
		);

		SchemaTable tableSchema = testedRow.getTable();
		assertEquals(2, tableSchema.getNumberOfColumns());
		assertColumn(
			tableSchema, "col_varchar_1",
			JDBCType.VARCHAR, true, false
		);
		assertColumn(
			tableSchema, "col_varchar_2",
			JDBCType.VARCHAR, false, true
		);
	}
	private void assertColumn(
		SchemaTable tableSchema, String columnName, JDBCType expectedType,
		Boolean isNullable, boolean hasDefaultValue
	) {
		SchemaColumn column = tableSchema.getColumn(columnName);

		assertEquals(expectedType, column.getJdbcType().get());
		assertEquals(isNullable, column.getNullable().get());
		assertEquals(hasDefaultValue, column.getHasDefaultValue());
	}

	/**
	 * Tests the loading of keys.
	 */
	@ParameterizedTest
	@MethodSource @DoLiquibase
	public void decorateForKeys(
		String sampleNameOfTable,
		String[] expectedKeys
	) {
		final DataGrainDecorator testedDecorator = new TableSchemaLoadingDecorator(
			getDataSource()
		);

		DataRow testedRow = DataRow.build(
			builder -> {
				builder.tableSchema(
					SchemaTable.build(tableBuilder -> tableBuilder.name(sampleNameOfTable))
				);

				testedDecorator.decorate(builder);
			}
		);

		List<String> testedKeys = testedRow.getTable().getKeys();

		getLogger().warn("TK: {}, EK: {}", testedKeys, expectedKeys);

		/**
		 * Asserts every key
		 */
		IntStream.range(0, expectedKeys.length)
			.forEach(
				i -> assertEquals(
					expectedKeys[i].toLowerCase(),
					testedKeys.get(i).toLowerCase()
				)
			);
		// :~)
	}
	static Arguments[] decorateForKeys()
	{
		return new Arguments[] {
			arguments("tab_has_pk", new String[] { "pk_id_1", "pk_id_2" }), // PK priority to unique
			arguments("tab_has_not_null_unique", new String[] { "nnu_col_1", "nnu_col_2" }), // least unique index
			arguments("tab_has_not_null_unique_2", new String[] { "nnu2_col_1", "nnu2_col_2" }), // not nullable index priority to nullable one
			arguments("tab_has_nothing", new String[0]), // cannot find anything
		};
	}
}
