package guru.mikelue.jdut.junit4.example;

import javax.sql.DataSource;

import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbStatement;

/**
 * Setup of schema for example.
 */
public final class SchemaSetup {
	private final static String TABLE_ARTIST =
		"CREATE TABLE IF NOT EXISTS ex_artist(" +
		"	at_id IDENTITY PRIMARY KEY," +
		"	at_name VARCHAR(512) NOT NULL," +
		"	at_gender TINYINT NOT NULL DEFAULT 3," +
		"	at_birthday DATE" +
		")";
	private final static String TABLE_ALBUM =
		"CREATE TABLE IF NOT EXISTS ex_album(" +
		"	ab_id IDENTITY PRIMARY KEY," +
		"	ab_name VARCHAR(512) NOT NULL," +
		"	ab_release_date DATE NOT NULL," +
		"	ab_duration_seconds SMALLINT NOT NULL," +
		"	ab_type TINYINT NOT NULL DEFAULT 1," +
		"	ab_at_id INTEGER NOT NULL" +
		")";

	private SchemaSetup() {}

	/**
	 * Builds schema for database of example.
	 */
	public static void buildSchema(DataSource ds)
	{
		JdbcTemplateFactory.buildRunnable(
			() -> ds.getConnection(),
			conn -> DbStatement.buildRunnableForStatement(
				conn,
				stat -> {
					stat.executeUpdate(TABLE_ALBUM);
					stat.executeUpdate(TABLE_ARTIST);
				}
			).runJdbc()
		).asRunnable().run();
	}
}
