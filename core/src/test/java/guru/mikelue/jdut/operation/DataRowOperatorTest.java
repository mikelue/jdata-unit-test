package guru.mikelue.jdut.operation;

import java.sql.SQLException;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.datagrain.DataRow;

public class DataRowOperatorTest {
	public DataRowOperatorTest() {}

	/**
	 * Tests the convertion to {@link DataRowsOperator}.
	 */
	@Test
	public void toDataRowsOperator() throws SQLException
	{
		DataGrain sampleDataGrain = DataGrain.build(
			builder -> builder.name("tab_gc_2"),
			builder -> builder
				.implicitColumns("dc_3", "dc_4")
				.addValues(20, "CK-01")
				.addValues(30, "CK-02")
				.addValues(77, "AC-02")
		);

		List<DataRow> processedResult = ((DataRowOperator)(connection, dataRow) -> dataRow)
			.toDataRowsOperator().operate(null, sampleDataGrain.getRows());

		Assert.assertEquals(processedResult.size(), 3);
	}
}
