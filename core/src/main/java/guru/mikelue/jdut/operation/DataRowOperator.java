package guru.mikelue.jdut.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.operation.DataGrainOperator.SurroundOperator;

/**
 * Operator to execute code by fed {@link DataRow}.
 */
@FunctionalInterface
public interface DataRowOperator {
	/**
	 * Does nothing.
	 *
	 * @param conn The connection object
	 * @param dataRow The object of data row
	 *
	 * @return The same data row
	 */
	static DataRow none(Connection conn, DataRow dataRow) { return dataRow; }

	/**
	 * Converts this instance to {@link DataGrainOperator}.
	 *
	 * @return The operator for {@link DataGrain}
	 */
	default DataGrainOperator toDataGrainOperator()
	{
		return toDataRowsOperator().toDataGrainOperator();
	}

	/**
	 * Converts this instance to {@link DataRowsOperator}.
	 *
	 * @return The operator for {@link List} of {@link DataRow}
	 */
	default DataRowsOperator toDataRowsOperator()
	{
		return (connection, dataRows) -> {
			List<DataRow> resultRows = new ArrayList<>(dataRows.size());
			for (DataRow dataRow: dataRows) {
				resultRows.add(
					operate(connection, dataRow)
				);
			}

			return resultRows;
		};
	}

	/**
	 * Surrounds this instance by {@link SurroundOperator}.
	 *
	 * @param surroundOperator The operator to surrounding staff
	 *
	 * @return new operator of data grain
	 */
	default DataGrainOperator surroundedBy(SurroundOperator surroundOperator)
	{
		return this.toDataGrainOperator().surroundedBy(surroundOperator);
	}

	/**
	 * Operates {@link DataRow} by {@link Connection}.
	 *
	 * @param connection The connection of database
	 * @param dataRow The data grain to be operated
	 *
	 * @return The processed data row
	 *
	 * @throws SQLException The SQL exception defined by JDBC
	 */
	public DataRow operate(Connection connection, DataRow dataRow) throws SQLException;
}
