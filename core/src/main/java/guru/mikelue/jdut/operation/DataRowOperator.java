package guru.mikelue.jdut.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.datagrain.DataRow;

/**
 * Operator to execute code by fed {@link DataRow}.
 */
@FunctionalInterface
public interface DataRowOperator {
	/**
	 * The operator for surrounding of {@link DataRowOperator}.
	 */
	public interface SurroundOperator extends UnaryOperator<DataRowOperator> {
		/**
		 * Converts this operator ot {@link UnaryOperator}.
		 *
		 * @return The unary operator
		 */
		default UnaryOperator<DataRowOperator> asUnaryOperator()
		{
			return operator -> surround(operator);
		}

		/**
		 * Surrounds operator.
		 *
		 * @param surroundedOperator The oprator to be surrounded
		 *
		 * @return The final function
		 */
		public DataRowOperator surround(DataRowOperator surroundedOperator);
	}

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
	 * @param surroundOperator The operator to surrounding self
	 *
	 * @return new operator of data row
	 */
	default DataRowOperator surroundedBy(SurroundOperator surroundOperator)
	{
		return surroundOperator.surround(this);
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
