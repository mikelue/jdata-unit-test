package guru.mikelue.jdut.function;

import java.util.function.Predicate;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.datagrain.SchemaTable;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

public class DataRowBuilderPredicatesTest {
	public DataRowBuilderPredicatesTest() {}

	/**
	 * Test the predicates of null value.
	 */
	@ParameterizedTest
	@MethodSource
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
				.fieldOfValue(sampleColumnName, 20)
				.fieldOfValue("ktc_0", "String-V1");

			assertEquals(expectedResult, testedPredicate.test(rowBuilder));
		});
	}
	static Arguments[] notExistingColumn()
	{
		return new Arguments[] {
			arguments("kt_1", false),
			arguments("kt_2", true),
		};
	}
}
