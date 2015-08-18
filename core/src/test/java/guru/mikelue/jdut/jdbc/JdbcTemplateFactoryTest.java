package guru.mikelue.jdut.jdbc;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.testng.Assert;
import org.testng.annotations.Test;

import guru.mikelue.jdut.test.AbstractDataSourceTestBase;

public class JdbcTemplateFactoryTest extends AbstractDataSourceTestBase {
	public JdbcTemplateFactoryTest() {}

	/**
	 * Tests the surrounding.<p>
	 */
	@Test
	public void surround()
		throws SQLException
	{
		JdbcFunction<Integer, Integer> testedFunc = v -> v + 2;

		testedFunc = JdbcTemplateFactory.surround(
			testedFunc,
			surroundingList -> {
				surroundingList.add(f -> v -> f.apply(v * 2));
				surroundingList.add(f -> v -> f.apply(v + 3));
			}
		);

		Assert.assertEquals(testedFunc.apply(9), new Integer(23));
	}

	/**
	 * Tests the surrounding.<p>
	 */
	@Test
	public void surroundByVarArgs()
		throws SQLException
	{
		JdbcFunction<Integer, Integer> testedFunc = v -> v + 2;

		testedFunc = JdbcTemplateFactory.surround(
			testedFunc,
			f -> v -> f.apply(v * 3),
			f -> v -> f.apply(v + 2)
		);

		Assert.assertEquals(testedFunc.apply(8), new Integer(28));
	}

	/**
	 * Tests the building of {@link JdbcRunnable} from {@link JdbcFunction}.
	 */
	@Test
	public void buildRunnableAndSuplier()
		throws SQLException
	{
		MutableBoolean surroundingExecutedForRunnable = new MutableBoolean(false);
		MutableBoolean surroundingExecutedForSupplier = new MutableBoolean(false);

		final Connection testedConn = getDataSource().getConnection();

		/**
		 * Adds data
		 */
		JdbcTemplateFactory.buildRunnable(
			() -> testedConn,
			conn -> {
				JdbcTemplateFactory.buildRunnable(
					() -> conn.createStatement(),
					stat -> {
						stat.executeUpdate("CREATE TABLE tab_car (tc_id INT, tc_name VARCHAR(64))");
						stat.executeUpdate("INSERT INTO tab_car VALUES(1, 'GTO-98')");
					},
					surroundingList -> surroundingList.add(
						f -> stat -> {
							surroundingExecutedForRunnable.setTrue();
							return f.apply(stat);
						}
					)
				).run();
			}
		).run();
		// :~)

		Assert.assertTrue(testedConn.isClosed()); // Ensure the closing of resource
		Assert.assertTrue(surroundingExecutedForRunnable.booleanValue());

		/**
		 * Gets data
		 */
		String testedValue = JdbcTemplateFactory.buildSupplier(
			() -> getDataSource().getConnection(),
			conn -> {
				return JdbcTemplateFactory.buildSupplier(
					() -> conn.prepareStatement("SELECT * FROM tab_car WHERE tc_id = ?"),
					stat -> {
						stat.setInt(1, 1);

						return JdbcTemplateFactory.buildSupplier(
							() -> stat.executeQuery(),
							rs -> {
								rs.next();
								return rs.getString("tc_name");
							},
							surroundingList -> surroundingList.add(
								f -> rs -> {
									surroundingExecutedForSupplier.setTrue();
									return f.apply(rs);
								}
							)
						).get();
					}
				).get();
			}
		).get();
		// :~)

		Assert.assertEquals(testedValue, "GTO-98");
		Assert.assertTrue(surroundingExecutedForSupplier.booleanValue());
	}
}
