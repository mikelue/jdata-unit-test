package guru.mikelue.jdut.jdbc.util;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.JDBCType;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Date;

import org.slf4j.Logger;
import static org.slf4j.LoggerFactory.getLogger;

import guru.mikelue.jdut.datagrain.DataRow;

/**
 * Utility for {@link PreparedStatement}.
 */
public final class PreparedStatements {
	private final static Logger logger = getLogger(PreparedStatements.class);

	private PreparedStatements() {}

	/**
	 * Sets a parameter over <em>statement</em> by {@link DataRow}.
	 *
	 * <ol>
	 * 	<li><b>This method chooses the corresponding <em>setXXX</em> method of {@link PreparedStatement} by the type of data.</b></li>
	 * 	<li>If the type of data cannot be determined, the method of {@link PreparedStatement#setObject(int, Object)} would be used.</li>
	 * </ol>
	 *
	 * <h3>java.util.Date</h3>
	 * The object of {@link java.util.Date} would use {@link PreparedStatement#setTimestamp} to set parameter.
	 *
	 * @param statement The statement to be set
	 * @param dataRow The data row to be used over statement
	 * @param columnName The name of column to be set
	 * @param paramIndex The index of parameter
	 *
	 * @throws SQLException The exception of SQL
	 */
	public static void setParameter(
		PreparedStatement statement,
		DataRow dataRow, String columnName,
		int paramIndex
	) throws SQLException {
		Object data = dataRow.<Object>getData(columnName);
		JDBCType jdbcType = dataRow.getTable().getColumn(columnName).getJdbcType().get();

		logger.debug(
			"Sets parameter[\"{}\"({})] data: [{}]. Type: \"{}\"",
			columnName, paramIndex, data, jdbcType
		);

		if (data == null) {
			statement.setNull(paramIndex, jdbcType.getVendorTypeNumber());
			return;
		}

		if (String.class.isInstance(data)) {
			String finalData = (String)data;
			switch (jdbcType) {
				case CHAR:
				case LONGVARCHAR:
				case VARCHAR:
				case CLOB:
					statement.setString(paramIndex, finalData);
					return;
				case LONGNVARCHAR:
				case NCHAR:
				case NVARCHAR:
				case NCLOB:
					statement.setNString(paramIndex, finalData);
					return;
				default:
					break;
			}
		}

		if (java.util.Date.class.isInstance(data)) {
			if (java.sql.Timestamp.class.isInstance(data)) {
				statement.setTimestamp(paramIndex, (java.sql.Timestamp)data);
				return;
			}

			if (java.sql.Date.class.isInstance(data)) {
				statement.setDate(paramIndex, (java.sql.Date)data);
				return;
			}

			if (java.sql.Time.class.isInstance(data)) {
				statement.setTime(paramIndex, (java.sql.Time)data);
				return;
			}

			Date value = (Date)data;
			statement.setTimestamp(paramIndex, new java.sql.Timestamp(value.getTime()));
			return;
		}

		if (Number.class.isInstance(data)) {
			if (Integer.class.isInstance(data)) {
				statement.setInt(paramIndex, (Integer)data);
				return;
			}

			if (Long.class.isInstance(data)) {
				statement.setLong(paramIndex, (Long)data);
				return;
			}

			if (Short.class.isInstance(data)) {
				statement.setShort(paramIndex, (Short)data);
				return;
			}

			if (Byte.class.isInstance(data)) {
				statement.setByte(paramIndex, (Byte)data);
				return;
			}

			if (Double.class.isInstance(data)) {
				statement.setDouble(paramIndex, (Double)data);
				return;
			}

			if (BigDecimal.class.isInstance(data)) {
				statement.setBigDecimal(paramIndex, (BigDecimal)data);
				return;
			}

			if (Float.class.isInstance(data)) {
				statement.setFloat(paramIndex, (Float)data);
				return;
			}
		}

		if (Boolean.class.isInstance(data)) {
			statement.setBoolean(paramIndex, (Boolean)data);
			return;
		}

		if (Clob.class.isInstance(data)) {
			if (NClob.class.isInstance(data)) {
				statement.setNClob(paramIndex, (NClob)data);
				return;
			}

			statement.setClob(paramIndex, (Clob)data);
			return;
		}

		if (Blob.class.isInstance(data)) {
			statement.setBlob(paramIndex, (Blob)data);
			return;
		}

		if (new byte[0].getClass().getComponentType().isInstance(data)) {
			statement.setBytes(paramIndex, (byte[])data);
			return;
		}

		if (Ref.class.isInstance(data)) {
			statement.setRef(paramIndex, (Ref)data);
			return;
		}

		if (RowId.class.isInstance(data)) {
			statement.setRowId(paramIndex, (RowId)data);
			return;
		}

		if (Array.class.isInstance(data)) {
			statement.setArray(paramIndex, (Array)data);
			return;
		}

		if (SQLXML.class.isInstance(data)) {
			statement.setSQLXML(paramIndex, (SQLXML)data);
			return;
		}

		if (URL.class.isInstance(data)) {
			statement.setURL(paramIndex, (URL)data);
			return;
		}

		statement.setObject(paramIndex, data);
	}
}
