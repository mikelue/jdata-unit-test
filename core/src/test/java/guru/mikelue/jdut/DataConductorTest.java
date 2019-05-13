package guru.mikelue.jdut;

import java.sql.SQLException;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.operation.DataGrainOperator;
import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.test.DoLiquibase;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.*;

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
	@ParameterizedTest @MethodSource
	@DoLiquibase
	public void conduct(
		boolean hasDecorator
	) throws SQLException {
		MutableBoolean operated = new MutableBoolean(false);
		MutableBoolean decorated = new MutableBoolean(false);

		DataConductor testedConductor = new DataConductor(getDataSource());
		if (hasDecorator) {
            // START SNIPPET: myid
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
            // END SNIPPET: myid
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

		assertTrue(operated.booleanValue());
		assertEquals(hasDecorator, decorated.booleanValue());
	}
	static Arguments[] conduct()
	{
		return new Arguments[] {
			arguments(true), // Has decorator
			arguments(false)
		};
	}
}
