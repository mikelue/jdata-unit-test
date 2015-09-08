package guru.mikelue.jdut;

import java.sql.SQLException;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.operation.DataGrainOperator;
import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.test.DoLiquibase;

public class DataConductorTest extends AbstractDataSourceTestBase {
	public DataConductorTest() {}

	/**
	 * Tests the operator for certain data grain.<br>
	 * <ol>
	 *   <li>The connection is closed</li>
	 *   <li>The calling of operator</li>
	 *   <li>The using of decorator</li>
	 * </ol>
	 */
	@Test(dataProvider="Conduct") @DoLiquibase
	public void conduct(
		boolean hasDecorator
	) throws SQLException {
		MutableBoolean operated = new MutableBoolean(false);
		MutableBoolean decorated = new MutableBoolean(false);

		DataConductor testedConductor = new DataConductor(getDataSource());
		if (hasDecorator) {
			testedConductor.conduct(
				DataGrain.build(
					builder -> builder.name("tab_1"),
					builder -> builder.implicitColumns("cp_1", "cp_2", "cp_3")
						.addValues(20, "Tk", 88)
						.addValues(20, "Tk", 88)
				),
				(DataGrainOperator)(conn, dataGrain) -> {
					operated.setTrue();
					return dataGrain;
				},
				rowBuilder -> decorated.setTrue()
			);
		} else {
			testedConductor.conduct(
				DataGrain.build(
					builder -> builder.name("tab_1"),
					builder -> builder.implicitColumns("cp_1", "cp_2", "cp_3")
						.addValues(20, "Tk", 88)
						.addValues(20, "Tk", 88)
				),
				(DataGrainOperator)(conn, dataGrain) -> {
					operated.setTrue();
					return dataGrain;
				}
			);
		}

		Assert.assertTrue(operated.booleanValue());
		Assert.assertEquals(decorated.booleanValue(), hasDecorator);
	}
	@DataProvider(name="Conduct")
	private Object[][] getConduct()
	{
		return new Object[][] {
			{ true }, // Has decorator
			{ false }
		};
	}
}
