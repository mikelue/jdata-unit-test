package guru.mikelue.jdut.datagrain;

import java.sql.JDBCType;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

public class SchemaColumnTest {
	public SchemaColumnTest() {}

	/**
	 * Tests the building of object.
	 */
	@ParameterizedTest
	@MethodSource
	public void build(
		final JDBCType sampleJdbcType
	) {
		final String sampleName = "vc_1";

		SchemaColumn testedColumn = SchemaColumn.build(
			builder -> builder
				.name(sampleName)
				.jdbcType(sampleJdbcType)
		);

		assertEquals(testedColumn.getName(), sampleName);
		assertEquals(testedColumn.getJdbcType().orElse(null), sampleJdbcType);
	}
	static Arguments[] build()
	{
		return new Arguments[] {
			arguments(JDBCType.BIGINT),
			arguments((Object)null)
		};
	}
}
