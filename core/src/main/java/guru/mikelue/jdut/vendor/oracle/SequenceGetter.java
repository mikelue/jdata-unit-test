package guru.mikelue.jdut.vendor.oracle;

import javax.sql.DataSource;

import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;

/**
 * Utilities for sequence accessor of oracle database.
 */
public class SequenceGetter {
	private final DataSource dataSource;

	public SequenceGetter(DataSource newDataSource)
	{
		dataSource = newDataSource;
	}

	/**
	 * Gets current value of sequence(as int).
	 *
	 * @param sequenceName The name of sequence
	 *
	 * @return The current value of sequence
	 */
	public int currentValAsInt(String sequenceName)
	{
		return (int)getSequenceValue(sequenceName, "CURRVAL");
	}

	/**
	 * Gets next value of sequence(as int).
	 *
	 * @param sequenceName The name of sequence
	 *
	 * @return The next value of sequence
	 */
	public int nextValAsInt(String sequenceName)
	{
		return (int)getSequenceValue(sequenceName, "NEXTVAL");
	}

	/**
	 * Gets current value of sequence(as long).
	 *
	 * @param sequenceName The name of sequence
	 *
	 * @return The current value of sequence
	 */
	public long currentValAsLong(String sequenceName)
	{
		return getSequenceValue(sequenceName, "CURRVAL");
	}

	/**
	 * Gets next value of sequence(as long).
	 *
	 * @param sequenceName The name of sequence
	 *
	 * @return The next value of sequence
	 */
	public long nextValAsLong(String sequenceName)
	{
		return getSequenceValue(sequenceName, "NEXTVAL");
	}

	private long getSequenceValue(String sequenceName, String nameOfPseudoColumn)
	{
		return JdbcTemplateFactory.buildSupplier(
			() -> dataSource.getConnection(),
			conn -> JdbcTemplateFactory.buildSupplier(
				() -> conn.createStatement(),
				stat -> JdbcTemplateFactory.buildSupplier(
					() -> stat.executeQuery(String.format(
						"SELECT %s.%s FROM DUAL",
						sequenceName, nameOfPseudoColumn
					)),
					rs -> {
						rs.next();
						return rs.getLong(1);
					}
				).get()
			).get()
		).asSupplier().get();
	}
}
