package guru.mikelue.jdut.example;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import guru.mikelue.jdut.DataConductor;
import guru.mikelue.jdut.annotation.IfDatabaseVendor;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.operation.DefaultOperatorFactory;
import guru.mikelue.jdut.operation.DefaultOperators;
import guru.mikelue.jdut.operation.OperatorFactory;
import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.vendor.DatabaseVendor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This example demonstrates the usage of JDUT by pure JDBC.
 */
@IfDatabaseVendor(match=DatabaseVendor.H2)
public class JdbcExampleTest extends AbstractDataSourceTestBase {
	private static ExampleDao testedDao;
	private static DataConductor dataConductor;
	private static OperatorFactory operatorFactory;

	private final String INSERT_ARTIST_NAME = "Miles Davis";
	private final String UPDATE_ARTIST_NAME = "John Coltrane";
	private final String REMOVE_ARTIST_NAME = "Blue Mountain";

	private DataGrain dataGrainForUpdate;

	private DataGrain dataGrain_1ForListing;
	private DataGrain dataGrain_2ForListing;

	private Map<String, Runnable> setupFunctions = new HashMap<>();
	private Map<String, Runnable> tearDownFunctions = new HashMap<>();

	public JdbcExampleTest()
	{
		setupFunctions.put("updateArtistName", this::setupUpdateArtistName);
		tearDownFunctions.put("updateArtistName", this::tearDownUpdateArtistName);

		setupFunctions.put("countAlbumsByType", this::setupCountAlbumsByType);
		tearDownFunctions.put("countAlbumsByType", this::tearDownCountAlbumsByType);

		setupFunctions.put("removeArtistByName", this::setupRemoveArtistByName);

		tearDownFunctions.put("addArtist", this::tearDownAddArtist);
	}

	@BeforeAll
	void setupDatabaseSchema()
	{
		SchemaSetup.buildSchema(getDataSource());

		testedDao = new ExampleDao(getDataSource());

		dataConductor = new DataConductor(getDataSource());
		operatorFactory = DefaultOperatorFactory.build(getDataSource());
	}

	@BeforeEach
	void setupTest(TestInfo testInfo)
	{
		runByMethodName(testInfo, setupFunctions);
	}
	@AfterEach
	void tearDownTest(TestInfo testInfo)
	{
		runByMethodName(testInfo, tearDownFunctions);
	}

	private void runByMethodName(TestInfo testInfo, Map<String, Runnable> funcs)
	{
		Runnable r = funcs.get(
			testInfo.getTestMethod().get().getName()
		);

		if (r != null) {
			r.run();
		}
	}

	@Test
	void addArtist() throws SQLException
	{
		testedDao.addArtist(INSERT_ARTIST_NAME);

		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn,
				"SELECT COUNT(*) FROM ex_artist WHERE at_name = '" + INSERT_ARTIST_NAME + "'",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 1)
			).runJdbc()
		).runJdbc();
	}
	private void tearDownAddArtist()
	{
		dataConductor.conduct(
			DataGrain.build(
				builder ->
					builder.name("ex_artist").keys("at_name"),
				rowsBuilder -> rowsBuilder
					.addFields(rowsBuilder.newField("at_name", INSERT_ARTIST_NAME))
			),
			operatorFactory.get(DefaultOperators.DELETE)
		);
	}

	/**
	 * Tests the updating of data for artist.
	 */
	@Test
	void updateArtistName() throws SQLException
	{
		testedDao.updateArtistName(1001, UPDATE_ARTIST_NAME);

		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn,
				"SELECT COUNT(*) FROM ex_artist WHERE at_name = '" + UPDATE_ARTIST_NAME + "'",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 1)
			).runJdbc()
		).runJdbc();
	}
	private void setupUpdateArtistName()
	{
		dataGrainForUpdate = DataGrain.build(
			builder ->
				builder.name("ex_artist"),
			rowsBuilder -> rowsBuilder
				.implicitColumns("at_id", "at_name")
				.addValues(
					1001, "Dizzy Gillespie"
				)
		);

		dataConductor.conduct(
			dataGrainForUpdate,
			operatorFactory.get(DefaultOperators.REFRESH)
		);
	}
	private void tearDownUpdateArtistName()
	{
		dataConductor.conduct(
			dataGrainForUpdate,
			operatorFactory.get(DefaultOperators.DELETE)
		);
	}

	@Test
	void removeArtistByName() throws SQLException
	{
		testedDao.removeArtistByName(REMOVE_ARTIST_NAME);

		JdbcTemplateFactory.buildRunnable(
			() -> getDataSource().getConnection(),
			conn -> DbResultSet.buildRunnable(
				conn,
				"SELECT COUNT(*) FROM ex_artist WHERE at_name = '" + REMOVE_ARTIST_NAME + "'",
				rs -> new ResultSetAssert(rs)
					.assertNextTrue()
					.assertInt(1, 0)
			).runJdbc()
		).runJdbc();
	}
	private void setupRemoveArtistByName()
	{
		dataConductor.conduct(
			DataGrain.build(
				builder -> builder.name("ex_artist"),
				rowsBuilder -> rowsBuilder
					.addFields(
						rowsBuilder.newField("at_name", REMOVE_ARTIST_NAME)
					)
			),
			operatorFactory.get(DefaultOperators.INSERT)
		);
	}

	@Test
	void countAlbumsByType() throws SQLException
	{
		assertEquals(
			2,
			testedDao.countAlbumsByType(1)
		);
	}
	private void setupCountAlbumsByType()
	{
		int idOfArtistForListing = 9081;

		/**
		 * Random data supplier
		 */
		Supplier<Date> randomDate = JdbcExampleTest::randomDate;
		Supplier<Integer> randomDuration = JdbcExampleTest::randomDuration;
		// :~)

		/**
		 * Insertion of data for table "ex_artist"
		 */
		dataConductor.conduct(
			dataGrain_1ForListing = DataGrain.build(
				builder -> builder.name("ex_artist"),
				rowsBuilder -> rowsBuilder
					.implicitColumns("at_id", "at_name")
					.addValues(idOfArtistForListing, "Sonny Rollins")
			),
			operatorFactory.get(DefaultOperators.INSERT)
		);
		// :~)
		/**
		 * Insertion of data for table "ex_album"
		 */
		dataConductor.conduct(
			dataGrain_2ForListing = DataGrain.build(
				builder -> builder.name("ex_album"),
				rowsBuilder -> rowsBuilder
					.implicitColumns(
						"ab_id", "ab_name", "ab_release_date", "ab_duration_seconds", "ab_type", "ab_at_id"
					)
					.addValues(
						4051, "No. 1", randomDate, randomDuration,
						1,
						idOfArtistForListing
					)
					.addValues(
						4052, "No. 2", randomDate, randomDuration,
						2,
						idOfArtistForListing
					)
					.addValues(
						4053, "No. 3", randomDate, randomDuration,
						3,
						idOfArtistForListing
					)
					.addValues(
						4054, "No. 4", randomDate, randomDuration,
						1,
						idOfArtistForListing
					)
			),
			operatorFactory.get(DefaultOperators.INSERT)
		);
		// :~)
	}
	private void tearDownCountAlbumsByType()
	{
		dataConductor.conduct(
			dataGrain_2ForListing,
			operatorFactory.get(DefaultOperators.DELETE)
		);
		dataConductor.conduct(
			dataGrain_1ForListing,
			operatorFactory.get(DefaultOperators.DELETE)
		);
	}

	private static Date randomDate()
	{
		return Date.valueOf(
			LocalDate.of(
				RandomUtils.nextInt(1950, 1961),
				RandomUtils.nextInt(1, 13),
				RandomUtils.nextInt(1, 26)
			)
		);
	}

	private static int randomDuration()
	{
		return RandomUtils.nextInt(1800, 10801);
	}
}
