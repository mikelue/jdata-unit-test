package guru.mikelue.jdut.operation;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;

import guru.mikelue.jdut.datagrain.DataGrain;

import static org.junit.jupiter.api.Assertions.*;

public class DataRowsOperatorTest {
	public DataRowsOperatorTest() {}

	/**
	 * Tests the convertion to {@link DataGrainOperator}.
	 */
	@Test
	public void toDataGrainOperator() throws SQLException
	{
		DataGrain sampleDataGrain = DataGrain.build(
			builder -> builder.name("tab_gc_1"),
			builder -> builder
				.implicitColumns("dc_1", "dc_2")
				.addValues(20, "CK-01")
				.addValues(30, "CK-02")
		);

		DataGrain processedResult = ((DataRowsOperator)(connection, dataRows) -> dataRows)
			.toDataGrainOperator().operate(null, sampleDataGrain);

		assertEquals(2, processedResult.getNumberOfRows());
	}
}
