package guru.mikelue.jdut.vendor.mssql;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import guru.mikelue.jdut.datagrain.DataRow;
import guru.mikelue.jdut.datagrain.SchemaTable;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.operation.DataRowOperator;

/**
 * Used to surrounding {@link DataRowOperator} for supporting insertion of identity for table.
 */
public class IdentityInsertOperator implements DataRowOperator {
	private Logger logger = LoggerFactory.getLogger(IdentityInsertOperator.class);

	/**
	 * Used as flag to indicate the current data row has data of identity column.
	 */
	private final static String HAS_IDENTITY_COLUMN = "_mssql_has_identity_column_";

	/**
	 * Checks whether or not a data row has identity data.
	 *
	 * @param dataRow The data row to be checked
	 *
	 * @return true if it does
	 */
	public static boolean hasIdentityColumn(DataRow dataRow)
	{
		return dataRow.getAttribute(HAS_IDENTITY_COLUMN);
	}
	/**
	 * Checks whether or not the table of data row has enabled identity.<br>
	 *
	 * If it is unknown, this method gives <em>true value</em>.
	 *
	 * @param dataRow The data row to be checked
	 *
	 * @return true if it does
	 */
	public static boolean identityChecked(DataRow dataRow)
	{
		return dataRow.hasAttribute(HAS_IDENTITY_COLUMN);
	}

	private final DataRowOperator op;

	/**
	 * As function interface of {@link DataRowOperator.SurroundOperator}.
	 *
	 * @param newOperator The operator to be surrounded
	 *
	 * @see DataRowOperator
	 */
	public IdentityInsertOperator(DataRowOperator newOperator)
	{
		op = newOperator;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataRow operate(Connection connection, DataRow dataRow) throws SQLException
	{
		SchemaTable table = dataRow.getTable();

		/**
		 * Initialize flags for identity data
		 */
		if (!identityChecked(dataRow)) {
			dataRow.putAttribute(
				HAS_IDENTITY_COLUMN,
				dataRow.getColumns().stream()
					.anyMatch(columnName -> {
						Optional<Boolean> autoIncremental = table.getColumn(columnName).getAutoIncremental();
						return autoIncremental.isPresent() && autoIncremental.get();
					})
			);
		}
		// :~)

		/**
		 * Executes the enabled identity insert command to the table
		 */
		if (hasIdentityColumn(dataRow)) {
			JdbcTemplateFactory.buildRunnable(
				() -> connection.createStatement(),
				stat -> stat.executeUpdate(String.format(
					"SET IDENTITY_INSERT %s ON",
					table.getQuotedFullName()
				))
			).runJdbc();
		}
		// :~)

		DataRow result = op.operate(connection, dataRow);

		/**
		 * Disable identity insert
		 */
		if (hasIdentityColumn(dataRow)) {
			logger.debug("[MS SQL Server] Enable identity: \"{}\"", table.getName());
			JdbcTemplateFactory.buildRunnable(
				() -> connection.createStatement(),
				stat -> stat.executeUpdate(String.format(
					"SET IDENTITY_INSERT %s OFF",
					table.getQuotedFullName()
				))
			).runJdbc();
		}
		// :~)

		return result;
	}
}
