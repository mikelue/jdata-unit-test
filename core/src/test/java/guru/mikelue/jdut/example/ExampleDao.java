package guru.mikelue.jdut.example;

import java.sql.SQLException;
import javax.sql.DataSource;

import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;

public class ExampleDao {
	private final DataSource dataSource;

	public ExampleDao(DataSource newDataSource)
	{
		dataSource = newDataSource;
	}

	public int countAlbumsByType(int type) throws SQLException
	{
		return JdbcTemplateFactory.buildSupplier(
			() -> dataSource.getConnection(),
			conn -> JdbcTemplateFactory.buildSupplier(
				() -> conn.prepareStatement(
					" SELECT COUNT(ab_id)" +
					" FROM ex_album" +
					" WHERE ab_type = ?"
				),
				stat -> JdbcTemplateFactory.buildSupplier(
					() -> {
						stat.setInt(1, type);
						return stat.executeQuery();
					},
					rs -> {
						rs.next();
						return rs.getInt(1);
					}
				).getJdbc()
			).getJdbc()
		).getJdbc();
	}

	public void addArtist(String name) throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> dataSource.getConnection(),
			conn -> JdbcTemplateFactory.buildRunnable(
				() -> conn.prepareStatement(
					"INSERT INTO ex_artist(at_name)" +
					"VALUES(?)"
				),
				stat -> {
					stat.setString(1, name);
					stat.executeUpdate();
				}
			).runJdbc()
		).runJdbc();
	}

	public void updateArtistName(int id, String newName) throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> dataSource.getConnection(),
			conn -> JdbcTemplateFactory.buildRunnable(
				() -> conn.prepareStatement(
					" UPDATE ex_artist" +
					" SET at_name = ?" +
					" WHERE at_id = ?"
				),
				stat -> {
					stat.setString(1, newName);
					stat.setInt(2, id);
					stat.executeUpdate();
				}
			).runJdbc()
		).runJdbc();
	}

	public void removeArtistByName(String name) throws SQLException
	{
		JdbcTemplateFactory.buildRunnable(
			() -> dataSource.getConnection(),
			conn -> JdbcTemplateFactory.buildRunnable(
				() -> conn.prepareStatement(
					" DELETE ex_artist" +
					" WHERE at_name = ?"
				),
				stat -> {
					stat.setString(1, name);
					stat.executeUpdate();
				}
			).runJdbc()
		).runJdbc();
	}
}
