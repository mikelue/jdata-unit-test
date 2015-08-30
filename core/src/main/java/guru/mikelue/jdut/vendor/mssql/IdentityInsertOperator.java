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
	 * Used as flag to indicate enable/disable of identity for current row.
	 */
	private final static String ENABLE_IDENTITY_DATA = "_mssql_enable_identity_";
	/**
	 * Used as flag to indicate the current data row has data of identity column.
	 */
	private final static String HAS_IDENTITY_DATA = "_mssql_has_identity_data_";

	/**
	 * Checks whether or not a data row has identity data.
	 *
	 * @param dataRow The data row to be checked
	 *
	 * @return true if it does
	 */
	public static boolean hasIdentityData(DataRow dataRow)
	{
		return dataRow.getAttribute(HAS_IDENTITY_DATA);
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
	public static boolean identityEnabled(DataRow dataRow)
	{
		if (!dataRow.hasAttribute(ENABLE_IDENTITY_DATA)) {
			return true;
		}

		return dataRow.getAttribute(ENABLE_IDENTITY_DATA);
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
		 * Initialize falgs for identity data
		 */
		if (!dataRow.hasAttribute(HAS_IDENTITY_DATA)) {
			if (
				/**
				 * Checks whether or not this row has identity data
				 */
				dataRow.getColumns().stream()
					.anyMatch(columnName -> {
						Optional<Boolean> autoIncremental = table.getColumn(columnName).getAutoIncremental();
						return autoIncremental.isPresent() && autoIncremental.get();
					})
				// :~)
			) {
				dataRow.putAttribute(HAS_IDENTITY_DATA, true);
				dataRow.putAttribute(ENABLE_IDENTITY_DATA, true);
			}
		}
		// :~)

		/**
		 * Executes the disable command to the table
		 */
		if (hasIdentityData(dataRow) && identityEnabled(dataRow)) {
			logger.debug("[MS SQL Server] Disable identity: \"{}\"", table.getName());
			JdbcTemplateFactory.buildRunnable(
				() -> connection.createStatement(),
				stat -> stat.executeUpdate(String.format(
					"SET IDENTITY_INSERT %s ON",
					table.quoteIdentifier(table.getName())
				))
			).run();
		}
		// :~)

		DataRow result = op.operate(connection, dataRow);

		/**
		 * Re-enable the table
		 */
		if (!identityEnabled(dataRow)) {
			logger.debug("[MS SQL Server] Enable identity: \"{}\"", table.getName());
			JdbcTemplateFactory.buildRunnable(
				() -> connection.createStatement(),
				stat -> stat.executeUpdate(String.format(
					"SET IDENTITY_INSERT %s OFF",
					table.quoteIdentifier(table.getName())
				))
			).run();
		}
		// :~)

		return result;
	}
}
