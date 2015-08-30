package guru.mikelue.jdut.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import guru.mikelue.jdut.datagrain.DataRow;

/**
 * This operator operates every table only once.
 */
public class TableBasedOperator implements DataRowOperator {
	private Set<String> processedTable = new HashSet<>(8);
	private final DataRowOperator dataRowOperator;

	/**
	 * Constructs with the implementation of operator.
	 *
	 * @param newDataRowOperator The operator to be called
	 */
	public TableBasedOperator(DataRowOperator newDataRowOperator)
	{
		dataRowOperator = newDataRowOperator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRow operate(Connection conn, DataRow dataRow) throws SQLException
	{
		String tableName = dataRow.getTable().getName();

		if (processedTable.contains(tableName)) {
			return dataRow;
		}

		processedTable.add(tableName);
		return dataRowOperator.operate(conn, dataRow);
	}
}
