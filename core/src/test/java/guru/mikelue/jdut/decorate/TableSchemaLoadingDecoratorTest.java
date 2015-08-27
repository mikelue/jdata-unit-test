package guru.mikelue.jdut.decorate;

import java.lang.reflect.Method;
import java.sql.JDBCType;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.datagrain.SchemaColumn;
import guru.mikelue.jdut.datagrain.SchemaTable;
import guru.mikelue.jdut.test.AbstractDataSourceTestBase;

public class TableSchemaLoadingDecoratorTest extends AbstractDataSourceTestBase {
	public TableSchemaLoadingDecoratorTest() {}

	/**
	 * Tests the decoration of schema loading.
	 */
	@Test
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

	@BeforeMethod
	private void buildSchema(Method method)
	{
		switch (method.getName()) {
			case "decorate":
				updateLiquibase(method);
				break;
		}
	}
	@AfterMethod
	private void cleanSchema(Method method)
	{
		switch (method.getName()) {
			case "decorate":
				rollbackLiquibase(method);
				break;
		}
	}
}
