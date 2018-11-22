package guru.mikelue.jdut.example;

import java.lang.reflect.Method;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import guru.mikelue.jdut.DuetConductor;
import guru.mikelue.jdut.annotation.IfDatabaseVendor;
import guru.mikelue.jdut.assertion.ResultSetAssert;
import guru.mikelue.jdut.datagrain.DataGrain;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.jdbc.function.DbResultSet;
import guru.mikelue.jdut.operation.DefaultOperators;
import guru.mikelue.jdut.test.AbstractDataSourceTestBase;
import guru.mikelue.jdut.vendor.DatabaseVendor;
import guru.mikelue.jdut.yaml.YamlConductorFactory;

@IfDatabaseVendor(match=DatabaseVendor.H2)
public class YamlExampleTest extends AbstractDataSourceTestBase {
	private static ExampleDao testedDao;

    private static YamlConductorFactory yamlConductor;

	private final String INSERT_ARTIST_NAME = "Miles Davis";
	private final String UPDATE_ARTIST_NAME = "John Coltrane";
	private final static String REMOVE_ARTIST_NAME = "Blue Mountain";

    private static Map<String, DuetConductor> duetConductors = new HashMap<>(6);

    public YamlExampleTest() {}

    @BeforeMethod
    private void buildData(Method method)
    {
        duetConductors.put(
            method.getName(),
            yamlConductor.conductResource(
                "guru/mikelue/jdut/example/YamlExampleTest-" + method.getName() + ".yaml"
            )
        );

        duetConductors.get(method.getName()).build();
    }
    @AfterMethod
    private void cleanData(Method method)
    {
		duetConductors.get(method.getName()).clean();
    }

	/**
	 * Tests the insertion of data for artist.
	 */
	@Test
	public void addArtist() throws SQLException
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

	/**
	 * Tests the updating of data for artist.
	 */
	@Test
	public void updateArtistName() throws SQLException
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

	/**
	 * Tests the removal of data for artist.
	 */
	@Test
	public void removeArtistByName() throws SQLException
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

	@Test
	public void countAlbumsByType() throws SQLException
	{
		Assert.assertEquals(
			testedDao.countAlbumsByType(1),
			2
		);
	}

	@BeforeClass
	private static void setupDatabaseSchema()
	{
		SchemaSetup.buildSchema(getDataSource());

        final Logger logger = LoggerFactory.getLogger("guru.mikelue.jdut.example.INSERT_AND_LOG");

		testedDao = new ExampleDao(getDataSource());

        yamlConductor = YamlConductorFactory.build(
            getDataSource(),
            builder -> builder
                .namedSupplier(
                    "random_date", YamlExampleTest::randomDate
                )
                .namedSupplier(
                    "random_duration", YamlExampleTest::randomDuration
                )
                .namedOperator(
                    "insert_and_log",
                    (connection, dataGrain) -> {
                        logger.info("@@@ BEFORE BUILDING DATA @@@");

                        DataGrain result = DefaultOperators.insert(connection, dataGrain);

                        logger.info("@@@ AFTER BUILDING DATA @@@");

                        return result;
                    }
                )
                .namedDecorator(
                    "decorator_album",
                    (dataRowBuilder) -> {
                        dataRowBuilder.fieldOfValue(
                            "ab_name",
                            dataRowBuilder.getData("ab_name").get() + "(BlueNote)"
                        );
                    }
                )
        );
	}
    @AfterClass
    private static void releaseResources()
    {
        duetConductors = null;
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
