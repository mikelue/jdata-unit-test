package guru.mikelue.jdut.function;

import java.util.function.Predicate;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.datagrain.SchemaTable;

public class DataRowBuilderPredicatesTest {
	public DataRowBuilderPredicatesTest() {}

	/**
	 * Test the predicates of null value.
	 */
	@Test(dataProvider="NotExistingColumn")
	public void notExistingColumn(
		final String sampleColumnName,
		boolean expectedResult
	) {
		final String tableName = "tab_p1";

		Predicate<DataRow.Builder> testedPredicate = DataRowBuilderPredicates.notExistingColumn(
			tableName, "kt_1"
		);

		DataRow.build(rowBuilder -> {
			rowBuilder
				.tableSchema(
					SchemaTable.build(tableBuilder -> tableBuilder.name(tableName))
				)
				.field(sampleColumnName, 20)
				.field("ktc_0", "String-V1");

			Assert.assertEquals(testedPredicate.test(rowBuilder), expectedResult);
		});
	}
	@DataProvider(name="NotExistingColumn")
	private Object[][] getNullValue()
	{
		return new Object[][] {
			{ "kt_1", false },
			{ "kt_2", true },
		};
	}
}
