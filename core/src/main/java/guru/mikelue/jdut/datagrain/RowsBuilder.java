package guru.mikelue.jdut.datagrain;

import java.util.function.Supplier;

/**
 * Declares the interface to build a bunch of rows.
 */
public interface RowsBuilder {
	/**
	 * Sets the sequence of columns for implicit data.
	 *
	 * @param nameOfColumns The name of columns by sequence of varargs.
	 *
     * @return cascading self
	 *
	 * @see #addValues(Object...)
	 */
	public RowsBuilder implicitColumns(String... nameOfColumns);

    /**
     * Adds a row with values of fields.
     *
     * @param valuesOfField The values of field
     *
     * @return cascading self
     */
    public RowsBuilder addValues(Object... valuesOfField);
    /**
     * Adds a row with implicit data, the sequence of columns is defined by {@link #implicitColumns}.
     *
     * @param dataFields The object of {@link DataField}
     *
     * @return cascading self
     *
     * @see #implicitColumns
     */
    public RowsBuilder addFields(DataField<?>... dataFields);
    /**
     * Builds a data field.
     *
     * @param <T> the type of data for the field
     * @param columnName The name of column
     * @param fieldValue The value of field
     *
     * @return data field
     *
     * @see #addFields(DataField...)
     */
    public <T> DataField<T> newField(String columnName, T fieldValue);
    /**
     * Builds a data field by {@link Supplier}.
     *
     * @param <T> the type of data for the field
     * @param columnName The name of column
     * @param fieldSupplier The value of field
     *
     * @return data field
     *
     * @see #addFields(DataField...)
     */
    public <T> DataField<T> newField(String columnName, Supplier<T> fieldSupplier);
}
