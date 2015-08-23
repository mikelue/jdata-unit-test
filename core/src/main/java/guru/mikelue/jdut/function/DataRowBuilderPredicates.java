package guru.mikelue.jdut.function;

import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import guru.mikelue.jdut.datagrain.DataRow;

/**
 * Buildings for {@link Predicate} of {@link DataRow.Builder}.
 */
public interface DataRowBuilderPredicates{
	/**
	 * Builds {@link Predicate} of a not-existing column on table.
	 *
	 * @param tableName The name of table
	 * @param columnName the name of column
	 *
	 * @return The predicate for row builder
	 */
	static public Predicate<DataRow.Builder> notExistingColumn(String tableName, String columnName)
	{
		String safeTableName = StringUtils.trimToNull(tableName);
		String safeColumnName = StringUtils.trimToNull(columnName);

		Validate.notNull(safeTableName, "Need viable name of table");
		Validate.notNull(safeColumnName, "Need viable name of column");

		return rowBuilder -> safeTableName.equals(rowBuilder.getTableName()) &&
			!rowBuilder.getDataField(safeColumnName).isPresent();
	}
}
