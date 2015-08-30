package guru.mikelue.jdut.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.UnaryOperator;

import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.datagrain.DataRow;

/**
 * Operator to execute code by fed {@link List} of {@link DataRow}.
 */
@FunctionalInterface
public interface DataRowsOperator {
	/**
	 * The operator for surrounding of {@link DataRowsOperator}.
	 */
	public interface SurroundOperator extends UnaryOperator<DataRowsOperator> {}

	/**
	 * Does nothing.
	 *
	 * @param conn The connection object
	 * @param dataRows The data of rows
	 *
	 * @return The same data rows
	 */
	static List<DataRow> none(Connection conn, List<DataRow> dataRows) { return dataRows; }

	/**
	 * Converts this instance to {@link DataGrainOperator}.
	 *
	 * @return The operator for {@link DataGrain}
	 */
	default DataGrainOperator toDataGrainOperator()
	{
		return (connection, dataGrain) -> new DataGrain(operate(connection, dataGrain.getRows()));
	}

	/**
	 * Surrounds this instance by {@link DataGrainOperator.SurroundOperator}.
	 *
	 * @param surroundOperator The operator to surrounding staff
	 *
	 * @return new operator of data grain
	 */
	default DataGrainOperator surroundedBy(DataGrainOperator.SurroundOperator surroundOperator)
	{
		return this.toDataGrainOperator().surroundedBy(surroundOperator);
	}

	/**
	 * Surrounds this instance by {@link SurroundOperator}.
	 *
	 * @param surroundOperator The operator to surrounding staff
	 *
	 * @return new operator of data rows
	 */
	default DataRowsOperator surroundedBy(SurroundOperator surroundOperator)
	{
		return surroundOperator.apply(this);
	}

	/**
	 * Operates {@link List} of {@link DataRow} by {@link Connection}.
	 *
	 * @param connection The connection of database
	 * @param dataRows The data grain to be operated
	 *
	 * @return The processed data rows
	 *
	 * @throws SQLException The SQL exception defined by JDBC
	 */
	public List<DataRow> operate(Connection connection, List<DataRow> dataRows) throws SQLException;
}
