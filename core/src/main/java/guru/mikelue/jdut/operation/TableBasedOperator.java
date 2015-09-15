package guru.mikelue.jdut.operation;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import guru.mikelue.jdut.datagrain.DataRow;

/**
 * This operator operates every table only once.<br>
 *
 * <p>This object is not thread-safe. For a thread-safe version, please use {@link #buildThreadSafe TableBasedOperator.buildThreadSafe} to instantiate this class.</p>
 */
public class TableBasedOperator implements DataRowOperator {
	/**
	 * Builds this operator with thread-safe property.
	 *
	 * @param newDataRowOperator The operator to be wrapped
	 *
	 * @return The new instance of thread-safe
	 */
	public static TableBasedOperator buildThreadSafe(DataRowOperator newDataRowOperator)
	{
		return new TableBasedOperator(
			newDataRowOperator,
			Collections.synchronizedSet(new HashSet<>(8))
		) {
			@Override
			public synchronized DataRow operate(Connection conn, DataRow dataRow) throws SQLException
			{
				return super.operate(conn, dataRow);
			}
		};
	}

	private Set<String> processedTable;
	private final DataRowOperator dataRowOperator;

	/**
	 * Constructs with the implementation of operator.
	 *
	 * @param newDataRowOperator The operator to be called
	 */
	public TableBasedOperator(DataRowOperator newDataRowOperator)
	{
		this(newDataRowOperator, new HashSet<>(8));
	}

	private TableBasedOperator(DataRowOperator newDataRowOperator, Set<String> processTableImpl)
	{
		dataRowOperator = newDataRowOperator;
		processedTable = processTableImpl;
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
