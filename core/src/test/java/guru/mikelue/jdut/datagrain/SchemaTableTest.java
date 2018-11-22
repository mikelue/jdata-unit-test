package guru.mikelue.jdut.datagrain;

import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import mockit.Mocked;
import mockit.Expectations;

public class SchemaTableTest {
	public SchemaTableTest() {}

	/**
	 * Tests the building of object.
	 */
	@Test(dataProvider="Build")
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

		Assert.assertEquals(testedTableSchema.getName(), expectedTableName);
		Assert.assertEquals(testedTableSchema.getKeys(), expectedKeys);

		/**
		 * Asserts the index of column
		 */
		Assert.assertEquals(testedTableSchema.getColumn(0).getName(), "dc_1");
		Assert.assertEquals(testedTableSchema.getColumn(1).getName(), "DC_2");
		Assert.assertEquals(testedTableSchema.getColumn(2).getName(), "Dc_3");
		// :~)
		/**
		 * Asserts the case of column name(insensitive)
		 */
		Assert.assertEquals(testedTableSchema.getColumn("DC_1").getName(), "dc_1");
		Assert.assertEquals(testedTableSchema.getColumn("dc_2").getName(), "DC_2");
		Assert.assertEquals(testedTableSchema.getColumn("dc_3").getName(), "Dc_3");
		// :~)
	}
	@DataProvider(name="Build")
	private Object[][] get()
	{
		return new Object[][] {
			{ "gt_car", "gt_car",
				new String[] { "col_1", "col_2" },
				Arrays.asList("col_1", "col_2")
			},
			{ "  gt_car  ", "gt_car",
				new String[] { " col_4 ", null, "", " col_3 " },
				Arrays.asList("col_4", "col_3")
			}
		};
	}

	@Mocked
	private DatabaseMetaData mockMetaData;

	/**
	 * Tests the case for column's name.
	 */
	@Test(dataProvider="HasColumn")
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

		Assert.assertEquals(testedTableSchema.hasColumn(checkingColumnName), expectedResult);
	}
	@DataProvider(name="HasColumn")
	private Object[][] getHasColumn()
	{
		return new Object[][] {
			/**
			 * Case insensitive
			 */
			{ false, false, true, false,
				"gc_1", "gc_1", true
			},
			{ false, false, true, false,
				"gc_1", "GC_1", true
			},
			// :~)
			/**
			 * Case insensitive(stores lower case)
			 */
			{ false, true, false, false,
				"GC_1", "gc_1", true
			},
			{ false, true, false, false,
				"GC_1", "gc_1", true
			},
			// :~)
			/**
			 * Case insensitive(stores upper case)
			 */
			{ true, false, false, false,
				"GC_1", "gc_1", true
			},
			{ true, false, false, false,
				"gc_1", "GC_1", true
			},
			// :~)
			/**
			 * Case sensitive
			 */
			{ false, false, false, true,
				"gc_1", "gc_1", true
			},
			{ false, false, false, true,
				"GC_1", "GC_1", true
			},
			{ false, false, false, true,
				"gc_1", "GC_1", false
			},
			// :~)
		};
	}
}
