package guru.mikelue.jdut.assertion;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.function.Supplier;

/**
 * A stateful object to assert data of result set by cascading way.
 *
 * <pre>{@code
 * JdbcTemplateFactory.buildRunnable(
 *     () -> getDataSource().getConnection(),
 *     conn -> DbResultSet.buildRunnable(
 *         conn,
 *         "SELECT * FROM DO_INSERT",
 *         rs -> new ResultSetAssert(rs)
 *             .assertNextTrue()
 *             .assertInt("dm_v1", 1)
 *             .assertInt("dm_v3", 20)
 *             .assertNextTrue()
 *     ).run()
 * ).run();
 * }</pre>
 */
public class ResultSetAssert {
	private final ResultSet testedResultSet;

	/**
	 * Constructs this object with result set to be tested.
	 *
	 * @param newResultSet The result rest
	 */
	public ResultSetAssert(ResultSet newResultSet)
	{
		testedResultSet = newResultSet;
	}

	/**
	 * Calls {@link ResultSet#absolute} and asserts the result as true value.
	 *
	 * @param row the number of the row to which the cursor should move. A value of zero indicates that the cursor will be positioned
	 *
	 * @return cascading self
	 *
	 * @see #assertAbsoluteFalse
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertAbsoluteTrue(int row)
		throws SQLException
	{
		return assertAbsoluteTrue(row, () -> "");
	}

	/**
	 * Calls {@link ResultSet#absolute} and asserts the result as true value.
	 *
	 *
	 * @param row the number of the row to which the cursor should move. A value of zero indicates that the cursor will be positioned
	 * @param message The message shown if the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @see #assertAbsoluteFalse
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertAbsoluteTrue(int row, String message)
		throws SQLException
	{
		return assertAbsoluteTrue(row, () -> message);
	}

	/**
	 * Calls {@link ResultSet#absolute} and asserts the result as true value.
	 *
	 * @param row the number of the row to which the cursor should move. A value of zero indicates that the cursor will be positioned
	 * @param messageSupplier The message supplier which is used while the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @see #assertAbsoluteFalse
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertAbsoluteTrue(int row, Supplier<String> messageSupplier)
		throws SQLException
	{
		return assertEqualValue(
			testedResultSet.absolute(row), true,
			() -> String.format("absolute([{}]) need to be true. %s", row, messageSupplier.get())
		);
	}

	/**
	 * Calls {@link ResultSet#absolute} and asserts the result as false value.
	 *
	 * @param row the number of the row to which the cursor should move. A value of zero indicates that the cursor will be positioned
	 *
	 * @return cascading self
	 *
	 * @see #assertAbsoluteFalse
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertAbsoluteFalse(int row)
		throws SQLException
	{
		return assertAbsoluteFalse(row, () -> "");
	}

	/**
	 * Calls {@link ResultSet#absolute} and asserts the result as false value.
	 *
	 *
	 * @param row the number of the row to which the cursor should move. A value of zero indicates that the cursor will be positioned
	 * @param message The message shown if the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @see #assertAbsoluteFalse
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertAbsoluteFalse(int row, String message)
		throws SQLException
	{
		return assertAbsoluteFalse(row, () -> message);
	}

	/**
	 * Calls {@link ResultSet#absolute} and asserts the result as false value.
	 *
	 * @param row the number of the row to which the cursor should move. A value of zero indicates that the cursor will be positioned
	 * @param messageSupplier The message supplier which is used while the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @see #assertAbsoluteFalse
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertAbsoluteFalse(int row, Supplier<String> messageSupplier)
		throws SQLException
	{
		return assertEqualValue(
			testedResultSet.absolute(row), false,
			() -> String.format("absolute([{}]) need to be false. %s", row, messageSupplier.get())
		);
	}

	/**
	 * Calls {@link ResultSet#next} and asserts the result as true value.
	 *
	 * @return cascading self
	 *
	 * @see #assertNextFalse
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertNextTrue()
		throws SQLException
	{
		return assertNextTrue(() -> "");
	}

	/**
	 * Calls {@link ResultSet#next} and asserts the result as true value.
	 *
	 * @param message The message shown if the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @see #assertNextFalse
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertNextTrue(String message)
		throws SQLException
	{
		return assertNextTrue(() -> message);
	}

	/**
	 * Calls {@link ResultSet#next} and asserts the result as true value.
	 *
	 * @param messageSupplier The message supplier which is used while the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @see #assertNextFalse
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertNextTrue(Supplier<String> messageSupplier)
		throws SQLException
	{
		return assertEqualValue(
			testedResultSet.next(), true,
			() -> String.format("next() need to be true. %s", messageSupplier.get())
		);
	}

	/**
	 * Calls {@link ResultSet#next} and asserts the result as false value.
	 *
	 * @return cascading self
	 *
	 * @see #assertNextTrue
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertNextFalse()
		throws SQLException
	{
		return assertNextFalse(() -> "");
	}

	/**
	 * Calls {@link ResultSet#next} and asserts the result as false value.
	 *
	 * @param message The message shown if the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @see #assertNextFalse
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertNextFalse(String message)
		throws SQLException
	{
		return assertNextFalse(() -> message);
	}

	/**
	 * Calls {@link ResultSet#next} and asserts the result as false value.
	 *
	 * @param messageSupplier The message supplier which is used while the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @see #assertNextFalse
	 *
	 * @throws SQLException If the calling method of result set has error
	 */
	public ResultSetAssert assertNextFalse(Supplier<String> messageSupplier)
		throws SQLException
	{
		return assertEqualValue(testedResultSet.next(), false,
			() -> String.format("next() need to be false. %s", messageSupplier.get())
		);
	}

	/**
	 * Calls {@link ResultSet#wasNull} and asserts the result as true value.
	 *
	 * @return cascading self
	 *
	 * @throws SQLException If the calling method of result set has error
	 *
	 * @see #assertWasNotNull
	 */
	public ResultSetAssert assertWasNull()
		throws SQLException
	{
		return assertWasNull(() -> "");
	}

	/**
	 * Calls {@link ResultSet#wasNull} and asserts the result as true value.
	 *
	 * @param message The message shown if the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @throws SQLException If the calling method of result set has error
	 *
	 * @see #assertWasNotNull
	 */
	public ResultSetAssert assertWasNull(String message)
		throws SQLException
	{
		return assertWasNull(() -> message);
	}

	/**
	 * Calls {@link ResultSet#wasNull} and asserts the result as true value.
	 *
	 * @param messageSupplier The message supplier which is used while the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @throws SQLException If the calling method of result set has error
	 *
	 * @see #assertWasNotNull
	 */
	public ResultSetAssert assertWasNull(Supplier<String> messageSupplier)
		throws SQLException
	{
		return assertEqualValue(testedResultSet.wasNull(), true,
			() -> String.format("Need wasNull() to be true. %s", messageSupplier.get())
		);
	}

	/**
	 * Calls {@link ResultSet#wasNull} and asserts the result as false value.
	 *
	 * @return cascading self
	 *
	 * @throws SQLException If the calling method of result set has error
	 *
	 * @see #assertWasNull
	 */
	public ResultSetAssert assertWasNotNull()
		throws SQLException
	{
		return assertWasNotNull(() -> "");
	}

	/**
	 * Calls {@link ResultSet#wasNull} and asserts the result as false value.
	 *
	 * @param message The message shown if the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @throws SQLException If the calling method of result set has error
	 *
	 * @see #assertWasNotNull
	 */
	public ResultSetAssert assertWasNotNull(String message)
		throws SQLException
	{
		return assertWasNotNull(() -> message);
	}

	/**
	 * Calls {@link ResultSet#wasNull} and asserts the result as false value.
	 *
	 * @param messageSupplier The message supplier which is used while the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @throws SQLException If the calling method of result set has error
	 *
	 * @see #assertWasNotNull
	 */
	public ResultSetAssert assertWasNotNull(Supplier<String> messageSupplier)
		throws SQLException
	{
		return assertEqualValue(testedResultSet.wasNull(), false,
			() -> String.format("Need wasNull() to be false. %s", messageSupplier.get())
		);
	}

	/**
	 * Calls {@link ResultSet#getFetchSize} and asserts the result as false value.
	 *
	 * @param size The expected size of feched data
	 *
	 * @return cascading self
	 *
	 * @throws SQLException If the calling method of result set has error
	 *
	 * @see #assertWasNull
	 */
	public ResultSetAssert assertFetchSize(int size)
		throws SQLException
	{
		return assertFetchSize(size, () -> "");
	}

	/**
	 * Calls {@link ResultSet#getFetchSize} and asserts the result as false value.
	 *
	 * @param size The expected size of feched data
	 * @param message The message shown if the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @throws SQLException If the calling method of result set has error
	 *
	 * @see #assertFetchSize
	 */
	public ResultSetAssert assertFetchSize(int size, String message)
		throws SQLException
	{
		return assertFetchSize(size, () -> message);
	}

	/**
	 * Calls {@link ResultSet#getFetchSize} and asserts the result as false value.
	 *
	 * @param size The expected size of feched data
	 * @param messageSupplier The message supplier which is used while the assertion is failed
	 *
	 * @return cascading self
	 *
	 * @throws SQLException If the calling method of result set has error
	 *
	 * @see #assertFetchSize
	 */
	public ResultSetAssert assertFetchSize(int size, Supplier<String> messageSupplier)
		throws SQLException
	{
		int checkedSize = testedResultSet.getFetchSize();

		return assertEqualValue(size, checkedSize,
			() -> String.format("Need getFetchSize() Value:[%d]. Got:[%d], %s", size, checkedSize, messageSupplier.get())
		);
	}

	public ResultSetAssert assertString(String columnName, String expectedString)
		throws SQLException
	{
		return assertString(
			columnName, expectedString, () -> ""
		);
	}
	public ResultSetAssert assertString(String columnName, String expectedString, String message)
		throws SQLException
	{
		return assertString(
			columnName, expectedString, () -> message
		);
	}
	public ResultSetAssert assertString(String columnName, String expectedString, Supplier<String> messageBuilder)
		throws SQLException
	{
		String testedValue = testedResultSet.getString(columnName);
		return assertEqualValue(
			testedValue, expectedString,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedString,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertString(int columnIndex, String expectedString)
		throws SQLException
	{
		return assertString(
			columnIndex, expectedString, () -> ""
		);
	}
	public ResultSetAssert assertString(int columnIndex, String expectedString, String message)
		throws SQLException
	{
		return assertString(
			columnIndex, expectedString, () -> message
		);
	}
	public ResultSetAssert assertString(int columnIndex, String expectedString, Supplier<String> messageBuilder)
		throws SQLException
	{
		String testedValue = testedResultSet.getString(columnIndex);
		return assertEqualValue(
			testedValue, expectedString,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedString,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertNString(String columnName, String expectedNString)
		throws SQLException
	{
		return assertNString(
			columnName, expectedNString, () -> ""
		);
	}
	public ResultSetAssert assertNString(String columnName, String expectedNString, String message)
		throws SQLException
	{
		return assertNString(
			columnName, expectedNString, () -> message
		);
	}
	public ResultSetAssert assertNString(String columnName, String expectedNString, Supplier<String> messageBuilder)
		throws SQLException
	{
		String testedValue = testedResultSet.getNString(columnName);
		return assertEqualValue(
			testedValue, expectedNString,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedNString,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertNString(int columnIndex, String expectedNString)
		throws SQLException
	{
		return assertNString(
			columnIndex, expectedNString, () -> ""
		);
	}
	public ResultSetAssert assertNString(int columnIndex, String expectedNString, String message)
		throws SQLException
	{
		return assertNString(
			columnIndex, expectedNString, () -> message
		);
	}
	public ResultSetAssert assertNString(int columnIndex, String expectedNString, Supplier<String> messageBuilder)
		throws SQLException
	{
		String testedValue = testedResultSet.getNString(columnIndex);
		return assertEqualValue(
			testedValue, expectedNString,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedNString,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertObject(String columnName, Object expectedObject)
		throws SQLException
	{
		return assertObject(
			columnName, expectedObject, () -> ""
		);
	}
	public ResultSetAssert assertObject(String columnName, Object expectedObject, String message)
		throws SQLException
	{
		return assertObject(
			columnName, expectedObject, () -> message
		);
	}
	public ResultSetAssert assertObject(String columnName, Object expectedObject, Supplier<String> messageBuilder)
		throws SQLException
	{
		Object testedValue = testedResultSet.getObject(columnName);
		return assertEqualValue(
			testedValue, expectedObject,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedObject,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertObject(int columnIndex, Object expectedObject)
		throws SQLException
	{
		return assertObject(
			columnIndex, expectedObject, () -> ""
		);
	}
	public ResultSetAssert assertObject(int columnIndex, Object expectedObject, String message)
		throws SQLException
	{
		return assertObject(
			columnIndex, expectedObject, () -> message
		);
	}
	public ResultSetAssert assertObject(int columnIndex, Object expectedObject, Supplier<String> messageBuilder)
		throws SQLException
	{
		Object testedValue = testedResultSet.getObject(columnIndex);
		return assertEqualValue(
			testedValue, expectedObject,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedObject,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertArray(String columnName, Array expectedArray)
		throws SQLException
	{
		return assertArray(
			columnName, expectedArray, () -> ""
		);
	}
	public ResultSetAssert assertArray(String columnName, Array expectedArray, String message)
		throws SQLException
	{
		return assertArray(
			columnName, expectedArray, () -> message
		);
	}
	public ResultSetAssert assertArray(String columnName, Array expectedArray, Supplier<String> messageBuilder)
		throws SQLException
	{
		Array testedValue = testedResultSet.getArray(columnName);
		return assertEqualValue(
			testedValue, expectedArray,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedArray,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertArray(int columnIndex, Array expectedArray)
		throws SQLException
	{
		return assertArray(
			columnIndex, expectedArray, () -> ""
		);
	}
	public ResultSetAssert assertArray(int columnIndex, Array expectedArray, String message)
		throws SQLException
	{
		return assertArray(
			columnIndex, expectedArray, () -> message
		);
	}
	public ResultSetAssert assertArray(int columnIndex, Array expectedArray, Supplier<String> messageBuilder)
		throws SQLException
	{
		Array testedValue = testedResultSet.getArray(columnIndex);
		return assertEqualValue(
			testedValue, expectedArray,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedArray,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertByte(String columnName, byte expectedByte)
		throws SQLException
	{
		return assertByte(
			columnName, expectedByte, () -> ""
		);
	}
	public ResultSetAssert assertByte(String columnName, byte expectedByte, String message)
		throws SQLException
	{
		return assertByte(
			columnName, expectedByte, () -> message
		);
	}
	public ResultSetAssert assertByte(String columnName, byte expectedByte, Supplier<String> messageBuilder)
		throws SQLException
	{
		int testedValue = testedResultSet.getByte(columnName);
		return assertEqualValue(
			testedValue, expectedByte,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedByte,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertByte(int columnIndex, byte expectedByte)
		throws SQLException
	{
		return assertByte(
			columnIndex, expectedByte, () -> ""
		);
	}
	public ResultSetAssert assertByte(int columnIndex, byte expectedByte, String message)
		throws SQLException
	{
		return assertByte(
			columnIndex, expectedByte, () -> message
		);
	}
	public ResultSetAssert assertByte(int columnIndex, byte expectedByte, Supplier<String> messageBuilder)
		throws SQLException
	{
		int testedValue = testedResultSet.getByte(columnIndex);
		return assertEqualValue(
			testedValue, expectedByte,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedByte,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertShort(String columnName, short expectedShort)
		throws SQLException
	{
		return assertShort(
			columnName, expectedShort, () -> ""
		);
	}
	public ResultSetAssert assertShort(String columnName, short expectedShort, String message)
		throws SQLException
	{
		return assertShort(
			columnName, expectedShort, () -> message
		);
	}
	public ResultSetAssert assertShort(String columnName, short expectedShort, Supplier<String> messageBuilder)
		throws SQLException
	{
		int testedValue = testedResultSet.getShort(columnName);
		return assertEqualValue(
			testedValue, expectedShort,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedShort,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertShort(int columnIndex, short expectedShort)
		throws SQLException
	{
		return assertShort(
			columnIndex, expectedShort, () -> ""
		);
	}
	public ResultSetAssert assertShort(int columnIndex, short expectedShort, String message)
		throws SQLException
	{
		return assertShort(
			columnIndex, expectedShort, () -> message
		);
	}
	public ResultSetAssert assertShort(int columnIndex, short expectedShort, Supplier<String> messageBuilder)
		throws SQLException
	{
		int testedValue = testedResultSet.getShort(columnIndex);
		return assertEqualValue(
			testedValue, expectedShort,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedShort,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertInt(String columnName, int expectedInt)
		throws SQLException
	{
		return assertInt(
			columnName, expectedInt, () -> ""
		);
	}
	public ResultSetAssert assertInt(String columnName, int expectedInt, String message)
		throws SQLException
	{
		return assertInt(
			columnName, expectedInt, () -> message
		);
	}
	public ResultSetAssert assertInt(String columnName, int expectedInt, Supplier<String> messageBuilder)
		throws SQLException
	{
		int testedValue = testedResultSet.getInt(columnName);
		return assertEqualValue(
			testedValue, expectedInt,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedInt,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertInt(int columnIndex, int expectedInt)
		throws SQLException
	{
		return assertInt(
			columnIndex, expectedInt, () -> ""
		);
	}
	public ResultSetAssert assertInt(int columnIndex, int expectedInt, String message)
		throws SQLException
	{
		return assertInt(
			columnIndex, expectedInt, () -> message
		);
	}
	public ResultSetAssert assertInt(int columnIndex, int expectedInt, Supplier<String> messageBuilder)
		throws SQLException
	{
		int testedValue = testedResultSet.getInt(columnIndex);
		return assertEqualValue(
			testedValue, expectedInt,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedInt,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertLong(String columnName, long expectedLong)
		throws SQLException
	{
		return assertLong(
			columnName, expectedLong, () -> ""
		);
	}
	public ResultSetAssert assertLong(String columnName, long expectedLong, String message)
		throws SQLException
	{
		return assertLong(
			columnName, expectedLong, () -> message
		);
	}
	public ResultSetAssert assertLong(String columnName, long expectedLong, Supplier<String> messageBuilder)
		throws SQLException
	{
		long testedValue = testedResultSet.getLong(columnName);
		return assertEqualValue(
			testedValue, expectedLong,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedLong,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertLong(int columnIndex, long expectedLong)
		throws SQLException
	{
		return assertLong(
			columnIndex, expectedLong, () -> ""
		);
	}
	public ResultSetAssert assertLong(int columnIndex, long expectedLong, String message)
		throws SQLException
	{
		return assertLong(
			columnIndex, expectedLong, () -> message
		);
	}
	public ResultSetAssert assertLong(int columnIndex, long expectedLong, Supplier<String> messageBuilder)
		throws SQLException
	{
		long testedValue = testedResultSet.getLong(columnIndex);
		return assertEqualValue(
			testedValue, expectedLong,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedLong,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertFloat(String columnName, float expectedFloat)
		throws SQLException
	{
		return assertFloat(
			columnName, expectedFloat, () -> ""
		);
	}
	public ResultSetAssert assertFloat(String columnName, float expectedFloat, String message)
		throws SQLException
	{
		return assertFloat(
			columnName, expectedFloat, () -> message
		);
	}
	public ResultSetAssert assertFloat(String columnName, float expectedFloat, Supplier<String> messageBuilder)
		throws SQLException
	{
		float testedValue = testedResultSet.getFloat(columnName);
		return assertEqualValue(
			testedValue, expectedFloat,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedFloat,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertFloat(int columnIndex, float expectedFloat)
		throws SQLException
	{
		return assertFloat(
			columnIndex, expectedFloat, () -> ""
		);
	}
	public ResultSetAssert assertFloat(int columnIndex, float expectedFloat, String message)
		throws SQLException
	{
		return assertFloat(
			columnIndex, expectedFloat, () -> message
		);
	}
	public ResultSetAssert assertFloat(int columnIndex, float expectedFloat, Supplier<String> messageBuilder)
		throws SQLException
	{
		float testedValue = testedResultSet.getFloat(columnIndex);
		return assertEqualValue(
			testedValue, expectedFloat,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedFloat,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertDouble(String columnName, double expectedDouble)
		throws SQLException
	{
		return assertDouble(
			columnName, expectedDouble, () -> ""
		);
	}
	public ResultSetAssert assertDouble(String columnName, double expectedDouble, String message)
		throws SQLException
	{
		return assertDouble(
			columnName, expectedDouble, () -> message
		);
	}
	public ResultSetAssert assertDouble(String columnName, double expectedDouble, Supplier<String> messageBuilder)
		throws SQLException
	{
		double testedValue = testedResultSet.getDouble(columnName);
		return assertEqualValue(
			testedValue, expectedDouble,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedDouble,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertDouble(int columnIndex, double expectedDouble)
		throws SQLException
	{
		return assertDouble(
			columnIndex, expectedDouble, () -> ""
		);
	}
	public ResultSetAssert assertDouble(int columnIndex, double expectedDouble, String message)
		throws SQLException
	{
		return assertDouble(
			columnIndex, expectedDouble, () -> message
		);
	}
	public ResultSetAssert assertDouble(int columnIndex, double expectedDouble, Supplier<String> messageBuilder)
		throws SQLException
	{
		double testedValue = testedResultSet.getDouble(columnIndex);
		return assertEqualValue(
			testedValue, expectedDouble,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedDouble,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertBigDecimal(String columnName, BigDecimal expectedBigDecimal)
		throws SQLException
	{
		return assertBigDecimal(
			columnName, expectedBigDecimal, () -> ""
		);
	}
	public ResultSetAssert assertBigDecimal(String columnName, BigDecimal expectedBigDecimal, String message)
		throws SQLException
	{
		return assertBigDecimal(
			columnName, expectedBigDecimal, () -> message
		);
	}
	public ResultSetAssert assertBigDecimal(String columnName, BigDecimal expectedBigDecimal, Supplier<String> messageBuilder)
		throws SQLException
	{
		BigDecimal testedValue = testedResultSet.getBigDecimal(columnName);
		return assertEqualValue(
			testedValue, expectedBigDecimal,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedBigDecimal,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertBigDecimal(int columnIndex, BigDecimal expectedBigDecimal)
		throws SQLException
	{
		return assertBigDecimal(
			columnIndex, expectedBigDecimal, () -> ""
		);
	}
	public ResultSetAssert assertBigDecimal(int columnIndex, BigDecimal expectedBigDecimal, String message)
		throws SQLException
	{
		return assertBigDecimal(
			columnIndex, expectedBigDecimal, () -> message
		);
	}
	public ResultSetAssert assertBigDecimal(int columnIndex, BigDecimal expectedBigDecimal, Supplier<String> messageBuilder)
		throws SQLException
	{
		BigDecimal testedValue = testedResultSet.getBigDecimal(columnIndex);
		return assertEqualValue(
			testedValue, expectedBigDecimal,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedBigDecimal,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertBlob(String columnName, Blob expectedBlob)
		throws SQLException
	{
		return assertBlob(
			columnName, expectedBlob, () -> ""
		);
	}
	public ResultSetAssert assertBlob(String columnName, Blob expectedBlob, String message)
		throws SQLException
	{
		return assertBlob(
			columnName, expectedBlob, () -> message
		);
	}
	public ResultSetAssert assertBlob(String columnName, Blob expectedBlob, Supplier<String> messageBuilder)
		throws SQLException
	{
		Blob testedValue = testedResultSet.getBlob(columnName);
		return assertEqualValue(
			testedValue, expectedBlob,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedBlob,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertBlob(int columnIndex, Blob expectedBlob)
		throws SQLException
	{
		return assertBlob(
			columnIndex, expectedBlob, () -> ""
		);
	}
	public ResultSetAssert assertBlob(int columnIndex, Blob expectedBlob, String message)
		throws SQLException
	{
		return assertBlob(
			columnIndex, expectedBlob, () -> message
		);
	}
	public ResultSetAssert assertBlob(int columnIndex, Blob expectedBlob, Supplier<String> messageBuilder)
		throws SQLException
	{
		Blob testedValue = testedResultSet.getBlob(columnIndex);
		return assertEqualValue(
			testedValue, expectedBlob,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedBlob,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertClob(String columnName, Clob expectedClob)
		throws SQLException
	{
		return assertClob(
			columnName, expectedClob, () -> ""
		);
	}
	public ResultSetAssert assertClob(String columnName, Clob expectedClob, String message)
		throws SQLException
	{
		return assertClob(
			columnName, expectedClob, () -> message
		);
	}
	public ResultSetAssert assertClob(String columnName, Clob expectedClob, Supplier<String> messageBuilder)
		throws SQLException
	{
		Clob testedValue = testedResultSet.getClob(columnName);
		return assertEqualValue(
			testedValue, expectedClob,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedClob,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertClob(int columnIndex, Clob expectedClob)
		throws SQLException
	{
		return assertClob(
			columnIndex, expectedClob, () -> ""
		);
	}
	public ResultSetAssert assertClob(int columnIndex, Clob expectedClob, String message)
		throws SQLException
	{
		return assertClob(
			columnIndex, expectedClob, () -> message
		);
	}
	public ResultSetAssert assertClob(int columnIndex, Clob expectedClob, Supplier<String> messageBuilder)
		throws SQLException
	{
		Clob testedValue = testedResultSet.getClob(columnIndex);
		return assertEqualValue(
			testedValue, expectedClob,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedClob,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertNClob(String columnName, NClob expectedNClob)
		throws SQLException
	{
		return assertNClob(
			columnName, expectedNClob, () -> ""
		);
	}
	public ResultSetAssert assertNClob(String columnName, NClob expectedNClob, String message)
		throws SQLException
	{
		return assertNClob(
			columnName, expectedNClob, () -> message
		);
	}
	public ResultSetAssert assertNClob(String columnName, NClob expectedNClob, Supplier<String> messageBuilder)
		throws SQLException
	{
		NClob testedValue = testedResultSet.getNClob(columnName);
		return assertEqualValue(
			testedValue, expectedNClob,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedNClob,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertNClob(int columnIndex, NClob expectedNClob)
		throws SQLException
	{
		return assertNClob(
			columnIndex, expectedNClob, () -> ""
		);
	}
	public ResultSetAssert assertNClob(int columnIndex, NClob expectedNClob, String message)
		throws SQLException
	{
		return assertNClob(
			columnIndex, expectedNClob, () -> message
		);
	}
	public ResultSetAssert assertNClob(int columnIndex, NClob expectedNClob, Supplier<String> messageBuilder)
		throws SQLException
	{
		NClob testedValue = testedResultSet.getNClob(columnIndex);
		return assertEqualValue(
			testedValue, expectedNClob,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedNClob,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertDate(String columnName, Date expectedDate)
		throws SQLException
	{
		return assertDate(
			columnName, expectedDate, () -> ""
		);
	}
	public ResultSetAssert assertDate(String columnName, Date expectedDate, String message)
		throws SQLException
	{
		return assertDate(
			columnName, expectedDate, () -> message
		);
	}
	public ResultSetAssert assertDate(String columnName, Date expectedDate, Supplier<String> messageBuilder)
		throws SQLException
	{
		Date testedValue = testedResultSet.getDate(columnName);
		return assertEqualValue(
			testedValue, expectedDate,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedDate,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertDate(int columnIndex, Date expectedDate)
		throws SQLException
	{
		return assertDate(
			columnIndex, expectedDate, () -> ""
		);
	}
	public ResultSetAssert assertDate(int columnIndex, Date expectedDate, String message)
		throws SQLException
	{
		return assertDate(
			columnIndex, expectedDate, () -> message
		);
	}
	public ResultSetAssert assertDate(int columnIndex, Date expectedDate, Supplier<String> messageBuilder)
		throws SQLException
	{
		Date testedValue = testedResultSet.getDate(columnIndex);
		return assertEqualValue(
			testedValue, expectedDate,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedDate,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertTime(String columnName, Time expectedTime)
		throws SQLException
	{
		return assertTime(
			columnName, expectedTime, () -> ""
		);
	}
	public ResultSetAssert assertTime(String columnName, Time expectedTime, String message)
		throws SQLException
	{
		return assertTime(
			columnName, expectedTime, () -> message
		);
	}
	public ResultSetAssert assertTime(String columnName, Time expectedTime, Supplier<String> messageBuilder)
		throws SQLException
	{
		Time testedValue = testedResultSet.getTime(columnName);
		return assertEqualValue(
			testedValue, expectedTime,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedTime,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertTime(int columnIndex, Time expectedTime)
		throws SQLException
	{
		return assertTime(
			columnIndex, expectedTime, () -> ""
		);
	}
	public ResultSetAssert assertTime(int columnIndex, Time expectedTime, String message)
		throws SQLException
	{
		return assertTime(
			columnIndex, expectedTime, () -> message
		);
	}
	public ResultSetAssert assertTime(int columnIndex, Time expectedTime, Supplier<String> messageBuilder)
		throws SQLException
	{
		Time testedValue = testedResultSet.getTime(columnIndex);
		return assertEqualValue(
			testedValue, expectedTime,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedTime,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertTimestamp(String columnName, Timestamp expectedTimestamp)
		throws SQLException
	{
		return assertTimestamp(
			columnName, expectedTimestamp, () -> ""
		);
	}
	public ResultSetAssert assertTimestamp(String columnName, Timestamp expectedTimestamp, String message)
		throws SQLException
	{
		return assertTimestamp(
			columnName, expectedTimestamp, () -> message
		);
	}
	public ResultSetAssert assertTimestamp(String columnName, Timestamp expectedTimestamp, Supplier<String> messageBuilder)
		throws SQLException
	{
		Timestamp testedValue = testedResultSet.getTimestamp(columnName);
		return assertEqualValue(
			testedValue, expectedTimestamp,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedTimestamp,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertTimestamp(int columnIndex, Timestamp expectedTimestamp)
		throws SQLException
	{
		return assertTimestamp(
			columnIndex, expectedTimestamp, () -> ""
		);
	}
	public ResultSetAssert assertTimestamp(int columnIndex, Timestamp expectedTimestamp, String message)
		throws SQLException
	{
		return assertTimestamp(
			columnIndex, expectedTimestamp, () -> message
		);
	}
	public ResultSetAssert assertTimestamp(int columnIndex, Timestamp expectedTimestamp, Supplier<String> messageBuilder)
		throws SQLException
	{
		Timestamp testedValue = testedResultSet.getTimestamp(columnIndex);
		return assertEqualValue(
			testedValue, expectedTimestamp,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedTimestamp,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertURL(String columnName, URL expectedURL)
		throws SQLException
	{
		return assertURL(
			columnName, expectedURL, () -> ""
		);
	}
	public ResultSetAssert assertURL(String columnName, URL expectedURL, String message)
		throws SQLException
	{
		return assertURL(
			columnName, expectedURL, () -> message
		);
	}
	public ResultSetAssert assertURL(String columnName, URL expectedURL, Supplier<String> messageBuilder)
		throws SQLException
	{
		URL testedValue = testedResultSet.getURL(columnName);
		return assertEqualValue(
			testedValue, expectedURL,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedURL,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertURL(int columnIndex, URL expectedURL)
		throws SQLException
	{
		return assertURL(
			columnIndex, expectedURL, () -> ""
		);
	}
	public ResultSetAssert assertURL(int columnIndex, URL expectedURL, String message)
		throws SQLException
	{
		return assertURL(
			columnIndex, expectedURL, () -> message
		);
	}
	public ResultSetAssert assertURL(int columnIndex, URL expectedURL, Supplier<String> messageBuilder)
		throws SQLException
	{
		URL testedValue = testedResultSet.getURL(columnIndex);
		return assertEqualValue(
			testedValue, expectedURL,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedURL,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertRef(String columnName, Ref expectedRef)
		throws SQLException
	{
		return assertRef(
			columnName, expectedRef, () -> ""
		);
	}
	public ResultSetAssert assertRef(String columnName, Ref expectedRef, String message)
		throws SQLException
	{
		return assertRef(
			columnName, expectedRef, () -> message
		);
	}
	public ResultSetAssert assertRef(String columnName, Ref expectedRef, Supplier<String> messageBuilder)
		throws SQLException
	{
		Ref testedValue = testedResultSet.getRef(columnName);
		return assertEqualValue(
			testedValue, expectedRef,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedRef,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertRef(int columnIndex, Ref expectedRef)
		throws SQLException
	{
		return assertRef(
			columnIndex, expectedRef, () -> ""
		);
	}
	public ResultSetAssert assertRef(int columnIndex, Ref expectedRef, String message)
		throws SQLException
	{
		return assertRef(
			columnIndex, expectedRef, () -> message
		);
	}
	public ResultSetAssert assertRef(int columnIndex, Ref expectedRef, Supplier<String> messageBuilder)
		throws SQLException
	{
		Ref testedValue = testedResultSet.getRef(columnIndex);
		return assertEqualValue(
			testedValue, expectedRef,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedRef,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertRowId(String columnName, RowId expectedRowId)
		throws SQLException
	{
		return assertRowId(
			columnName, expectedRowId, () -> ""
		);
	}
	public ResultSetAssert assertRowId(String columnName, RowId expectedRowId, String message)
		throws SQLException
	{
		return assertRowId(
			columnName, expectedRowId, () -> message
		);
	}
	public ResultSetAssert assertRowId(String columnName, RowId expectedRowId, Supplier<String> messageBuilder)
		throws SQLException
	{
		RowId testedValue = testedResultSet.getRowId(columnName);
		return assertEqualValue(
			testedValue, expectedRowId,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedRowId,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertRowId(int columnIndex, RowId expectedRowId)
		throws SQLException
	{
		return assertRowId(
			columnIndex, expectedRowId, () -> ""
		);
	}
	public ResultSetAssert assertRowId(int columnIndex, RowId expectedRowId, String message)
		throws SQLException
	{
		return assertRowId(
			columnIndex, expectedRowId, () -> message
		);
	}
	public ResultSetAssert assertRowId(int columnIndex, RowId expectedRowId, Supplier<String> messageBuilder)
		throws SQLException
	{
		RowId testedValue = testedResultSet.getRowId(columnIndex);
		return assertEqualValue(
			testedValue, expectedRowId,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedRowId,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertSQLXML(String columnName, SQLXML expectedSQLXML)
		throws SQLException
	{
		return assertSQLXML(
			columnName, expectedSQLXML, () -> ""
		);
	}
	public ResultSetAssert assertSQLXML(String columnName, SQLXML expectedSQLXML, String message)
		throws SQLException
	{
		return assertSQLXML(
			columnName, expectedSQLXML, () -> message
		);
	}
	public ResultSetAssert assertSQLXML(String columnName, SQLXML expectedSQLXML, Supplier<String> messageBuilder)
		throws SQLException
	{
		SQLXML testedValue = testedResultSet.getSQLXML(columnName);
		return assertEqualValue(
			testedValue, expectedSQLXML,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedSQLXML,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertSQLXML(int columnIndex, SQLXML expectedSQLXML)
		throws SQLException
	{
		return assertSQLXML(
			columnIndex, expectedSQLXML, () -> ""
		);
	}
	public ResultSetAssert assertSQLXML(int columnIndex, SQLXML expectedSQLXML, String message)
		throws SQLException
	{
		return assertSQLXML(
			columnIndex, expectedSQLXML, () -> message
		);
	}
	public ResultSetAssert assertSQLXML(int columnIndex, SQLXML expectedSQLXML, Supplier<String> messageBuilder)
		throws SQLException
	{
		SQLXML testedValue = testedResultSet.getSQLXML(columnIndex);
		return assertEqualValue(
			testedValue, expectedSQLXML,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedSQLXML,
				messageBuilder.get()
			)
		);
	}

	public ResultSetAssert assertBytes(String columnName, byte[] expectedBytes)
		throws SQLException
	{
		return assertBytes(
			columnName, expectedBytes, () -> ""
		);
	}
	public ResultSetAssert assertBytes(String columnName, byte[] expectedBytes, String message)
		throws SQLException
	{
		return assertBytes(
			columnName, expectedBytes, () -> message
		);
	}
	public ResultSetAssert assertBytes(String columnName, byte[] expectedBytes, Supplier<String> messageBuilder)
		throws SQLException
	{
		byte[] testedValue = testedResultSet.getBytes(columnName);
		return assertEqualValue(
			testedValue, expectedBytes,
			() -> String.format(
				"The value of field[\"%s\"] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnName, testedValue, expectedBytes,
				messageBuilder.get()
			)
		);
	}
	public ResultSetAssert assertBytes(int columnIndex, byte[] expectedBytes)
		throws SQLException
	{
		return assertBytes(
			columnIndex, expectedBytes, () -> ""
		);
	}
	public ResultSetAssert assertBytes(int columnIndex, byte[] expectedBytes, String message)
		throws SQLException
	{
		return assertBytes(
			columnIndex, expectedBytes, () -> message
		);
	}
	public ResultSetAssert assertBytes(int columnIndex, byte[] expectedBytes, Supplier<String> messageBuilder)
		throws SQLException
	{
		byte[] testedValue = testedResultSet.getBytes(columnIndex);
		return assertEqualValue(
			testedValue, expectedBytes,
			() -> String.format(
				"The value of field[%d] is not matched. Value: [%s]. Expected: [%s]. %s",
				columnIndex, testedValue, expectedBytes,
				messageBuilder.get()
			)
		);
	}

	private ResultSetAssert assertEqualValue(Object testedObject, Object expectedObject, Supplier<String> messageBuilder)
	{
		if (testedObject == null && expectedObject == null) {
			return this;
		}

		boolean passed = false;
		if (testedObject != null) {
			passed = testedObject.equals(expectedObject);
		} else {
			passed = expectedObject.equals(testedObject);
		}

		if (!passed) {
			throw new AssertException(
				messageBuilder.get()
			);
		}

		return this;
	}
}
