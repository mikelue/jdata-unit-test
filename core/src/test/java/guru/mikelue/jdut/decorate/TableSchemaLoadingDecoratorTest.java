package guru.mikelue.jdut.decorate;

import java.sql.JDBCType;
import java.util.List;
import java.util.stream.IntStream;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.datagrain.SchemaColumn;
import guru.mikelue.jdut.datagrain.SchemaTable;
import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.test.DoLiquibase;

public class TableSchemaLoadingDecoratorTest extends AbstractDataSourceTestBase {
	public TableSchemaLoadingDecoratorTest() {}

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
		Assert.assertEquals(tableSchema.getNumberOfColumns(), 2);
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

		Assert.assertEquals(column.getJdbcType().get(), expectedType);
		Assert.assertEquals(column.getNullable().get(), isNullable);
		Assert.assertEquals(column.getHasDefaultValue(), hasDefaultValue);
	}

	/**
	 * Tests the loading of keys.
	 */
	@Test(dataProvider="DecorateForKeys") @DoLiquibase
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

		/**
		 * Asserts every key
		 */
		IntStream.range(0, expectedKeys.length)
			.forEach(
				i -> Assert.assertEquals(
					testedKeys.get(i).toLowerCase(),
					expectedKeys[i].toLowerCase()
				)
			);
		// :~)
	}
	@DataProvider(name="DecorateForKeys")
	private Object[][] getDecorateForKeys()
	{
		return new Object[][] {
			{ "tab_has_pk", new String[] { "pk_id_1", "pk_id_2" } }, // PK priority to unique
			{ "tab_has_not_null_unique", new String[] { "nnu_col_1", "nnu_col_2" } }, // least unique index
			{ "tab_has_not_null_unique_2", new String[] { "nnu2_col_1", "nnu2_col_2" } }, // not nullable index priority to nullable one
			{ "tab_has_nothing", new String[0] }, // cannot find anything
		};
	}
}
