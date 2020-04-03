package guru.mikelue.jdut.datagrain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import guru.mikelue.jdut.jdbc.util.MetaDataWorker;

import mockit.Delegate;
import mockit.Expectations;
import mockit.Mocked;

public class SchemaTableTest {
	public SchemaTableTest() {}

	/**
	 * Tests the process for quoted full name
	 */
	@ParameterizedTest
	@MethodSource
	void buildForQuotedFullName(
		boolean metaDataWorker,
		String sampleSchema, String sampleTable,
		String expectedResult,
		@Mocked MetaDataWorker mockMetaDataWorker
	) {
		if (metaDataWorker) {
			new Expectations() {{
				mockMetaDataWorker.processIdentifier(anyString);
				result = new Delegate<Object>() {
					@SuppressWarnings("unused")
					String processIdentifier(String source)
					{
						return source;
					}
				};

				mockMetaDataWorker.quoteIdentifier(anyString);
				result = new Delegate<Object>() {
					@SuppressWarnings("unused")
					String quoteIdentifier(String source)
					{
						return String.format("\"%s\"", source);
					}
				};

				mockMetaDataWorker.supportsSchemasInDataManipulation();
				result = true;
			}};
		}

		SchemaTable testedTableSchema = SchemaTable.build(
			builder -> builder
				.metaDataWorker(metaDataWorker ? mockMetaDataWorker : null)
				.schema(sampleSchema)
				.name(sampleTable)
		);

		assertThat(testedTableSchema.getQuotedFullName())
			.isEqualTo(expectedResult);
	}
	static Arguments[] buildForQuotedFullName()
	{
		return new Arguments[] {
			arguments(true, "", "hammer", "\"hammer\""),
			arguments(true, "black", "mallet", "\"black\".\"mallet\""),
			arguments(false, "", "hammer", "hammer"),
			arguments(false, "black", "mallet", "mallet"),
		};
	}

	/**
	 * Tests the building of object.
	 */
	@ParameterizedTest
	@MethodSource
	void build(
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
		assertEquals(testedTableSchema.getQuotedFullName(), expectedTableName);
		assertEquals(testedTableSchema.getKeys(), expectedKeys);
	}
	static Arguments[] build()
	{
		return new Arguments[] {
			arguments("gt_car", "gt_car",
				new String[] { "col_1", "col_2" },
				Arrays.asList("col_1", "col_2")
			),
			arguments("  gt_car  ", "gt_car",
				new String[] { " col_4 ", null, "", " col_3 " },
				Arrays.asList("col_4", "col_3")
			)
		};
	}

	/**
	 * Tests the building of columns
	 */
	@Test
	void buildForColumns()
	{
		SchemaTable testedTableSchema = SchemaTable.build(
			builder -> builder
				.name("ladder")
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("cell_number")))
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("color")))
				.column(SchemaColumn.build(columnBuilder -> columnBuilder.name("min_height")))
		);

		/**
		 * Asserts the columns by name
		 */
		assertThat(testedTableSchema.getColumn("cell_number"))
			.isNotNull();
		assertThat(testedTableSchema.getColumn("color").getName())
			.isNotNull();
		assertThat(testedTableSchema.getColumn("min_height").getName())
			.isNotNull();
		// :~)

		/**
		 * Asserts the columns by index
		 */
		assertThat(testedTableSchema.getColumn(0).getName())
			.isEqualTo("cell_number");
		assertThat(testedTableSchema.getColumn(1).getName())
			.isEqualTo("color");
		assertThat(testedTableSchema.getColumn(2).getName())
			.isEqualTo("min_height");
		// :~)
	}
}
