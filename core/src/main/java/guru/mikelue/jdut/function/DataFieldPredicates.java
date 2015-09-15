package guru.mikelue.jdut.function;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import guru.mikelue.jdut.datagrain.DataField;

/**
 * Buildings for {@link Predicate} of {@link DataField}.
 */
public final class DataFieldPredicates {
	private DataFieldPredicates() {}

	/**
	 * Builds {@link Predicate} of null value for a column on table.
	 *
	 * @param <T> The type of data field
	 * @param tableName The name of table
	 * @param columnName the name of column
	 *
	 * @return The predicate of data field
	 */
	static <T> Predicate<DataField<T>> nullValue(String tableName, String columnName)
	{
		String safeTableName = StringUtils.trimToNull(tableName);
		String safeColumnName = StringUtils.trimToNull(columnName);

		Validate.notNull(safeTableName, "Need viable name of table");
		Validate.notNull(safeColumnName, "Need viable name of column");

		return dataField -> dataField.getData() == null &&
			safeTableName.equals(dataField.getTableName()) &&
			safeColumnName.equals(dataField.getColumnName());
	}

	/**
	 * The implementation of {@link Predicate Predicate}({@code DataField<?>}).
	 *
	 * @param dataField The tested data field
	 *
	 * @return true if the data of the field is not set by supplier
	 */
	static boolean nonSupplier(DataField<?> dataField)
	{
		return !dataField.getDataSupplier()
			.isPresent();
	}
}
