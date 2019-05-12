package guru.mikelue.jdut.datagrain;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import mockit.Mocked;
import mockit.Expectations;

import static org.junit.jupiter.api.Assertions.*;

public class SchemaTableTest {
	public SchemaTableTest() {}

	/**
	 * Tests the building of object.
	 */
	@ParameterizedTest
	@MethodSource
	public void build(
		String sampleTableName, String expectedTableName,
		String[] sampleKeys, List<String> expectedKeys
	) {
		SchemaTable testedTableSchema = SchemaTable.build(
			builder -> builder
				.name(sampleTableName)
				.keys(sampleKeys)
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("dc_1")))
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("DC_2"))) // Tests case sensitivity(default is not case sensitive)
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("Dc_3")))
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("DC_2"))) // Put again
		);

		assertEquals(testedTableSchema.getName(), expectedTableName);
		assertEquals(testedTableSchema.getKeys(), expectedKeys);

		/**
		 * Asserts the index of column
		 */
		assertEquals(testedTableSchema.getColumn(0).getName(), "dc_1");
		assertEquals(testedTableSchema.getColumn(1).getName(), "DC_2");
		assertEquals(testedTableSchema.getColumn(2).getName(), "Dc_3");
		// :~)
		/**
		 * Asserts the case of column name(insensitive)
		 */
		assertEquals(testedTableSchema.getColumn("DC_1").getName(), "dc_1");
		assertEquals(testedTableSchema.getColumn("dc_2").getName(), "DC_2");
		assertEquals(testedTableSchema.getColumn("dc_3").getName(), "Dc_3");
		// :~)
	}
	static Arguments[] build()
	{
		return new Arguments[] {
			a("gt_car", "gt_car",
				new String[] { "col_1", "col_2" },
				Arrays.asList("col_1", "col_2")
			),
			a("  gt_car  ", "gt_car",
				new String[] { " col_4 ", null, "", " col_3 " },
				Arrays.asList("col_4", "col_3")
			)
		};
	}

	@Mocked
	DatabaseMetaData mockMetaData;

	/**
	 * Tests the case for column's name.
	 */
	@ParameterizedTest
	@MethodSource
	public void hasColumn(
		boolean storesUpperCaseIdentifiers,
		boolean storesLowerCaseIdentifiers,
		boolean storesMixedCaseIdentifiers,
		boolean supportsMixedCaseIdentifiers,
		String sampleColumnName, String checkingColumnName,
		boolean expectedResult
	) throws SQLException {
		/**
		 * Mocks the value from meta data
		 */
		new Expectations() {{
			mockMetaData.storesUpperCaseIdentifiers();
			result = storesUpperCaseIdentifiers;
			mockMetaData.storesLowerCaseIdentifiers();
			result = storesLowerCaseIdentifiers;
			mockMetaData.storesMixedCaseIdentifiers();
			result = storesMixedCaseIdentifiers;
			mockMetaData.supportsMixedCaseIdentifiers();
			result = supportsMixedCaseIdentifiers;
		}};
		// :~)

		SchemaTable testedTableSchema = SchemaTable.build(
			builder -> builder
				.name("tab_99")
				.metaData(mockMetaData)
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name(sampleColumnName)))
		);

		assertEquals(testedTableSchema.hasColumn(checkingColumnName), expectedResult);
	}
	static Arguments[] hasColumn()
	{
		return new Arguments[] {
			/**
			 * Case insensitive
			 */
			a(false, false, true, false,
				"gc_1", "gc_1", true
			),
			a(false, false, true, false,
				"gc_1", "GC_1", true
			),
			// :~)
			/**
			 * Case insensitive(stores lower case)
			 */
			a(false, true, false, false,
				"GC_1", "gc_1", true
			),
			a(false, true, false, false,
				"GC_1", "gc_1", true
			),
			// :~)
			/**
			 * Case insensitive(stores upper case)
			 */
			a(true, false, false, false,
				"GC_1", "gc_1", true
			),
			a(true, false, false, false,
				"gc_1", "GC_1", true
			),
			// :~)
			/**
			 * Case sensitive
			 */
			a(false, false, false, true,
				"gc_1", "gc_1", true
			),
			a(false, false, false, true,
				"GC_1", "GC_1", true
			),
			a(false, false, false, true,
				"gc_1", "GC_1", false
			),
			// :~)
		};
	}

	private static Arguments a(Object... args)
	{
		return Arguments.arguments(args);
	}
}
