package guru.mikelue.jdut.datagrain;

/**
 * Represents the exception which defined data field cannot be found
 * in database schema.
 */
public class MissedColumnException extends DataRowException {
	private final SchemaColumn missedColumn;

	private final static long serialVersionUID = 1L;

	public MissedColumnException(SchemaTable tableSchema, SchemaColumn newMissedColumn)
	{
		super(String.format("Miss column: Table:\"%s\" Column:\"%s\"", tableSchema.getName(), newMissedColumn.getName()));
		missedColumn = newMissedColumn;
	}

	public SchemaColumn getMissedColumn()
	{
		return missedColumn;
	}
}
