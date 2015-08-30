package guru.mikelue.jdut.vendor;

import java.sql.DatabaseMetaData;
import javax.sql.DataSource;

import guru.mikelue.jdut.function.OperatorPredicate;
import guru.mikelue.jdut.jdbc.JdbcSupplier;
import guru.mikelue.jdut.jdbc.JdbcTemplateFactory;
import guru.mikelue.jdut.operation.DefaultOperatorFactory;

/**
 * The defined vendors of database.
 */
public enum DatabaseVendor implements VendorType {
	Oracle("oracle"),
	MsSql("mssql"),
	PostgreSql("postgresql"),
	MySql("mysql"),
	H2("h2"),
	HsqlDb("hsqldb"),
	Sqlite("sqlite"),
	Unknown("unknown");

	private final String name;
	DatabaseVendor(String newName)
	{
		name = newName;
	}

	@Override
	public String getVendorName()
	{
		return name;
	}

	/**
	 * Gets vendor from data source.
	 *
	 * @param dataSource The data source to be checked
	 *
	 * @return The matched vendor
	 */
	public static DatabaseVendor getVendor(DataSource dataSource)
	{
		return JdbcTemplateFactory.buildSupplier(
			() -> dataSource.getConnection(),
			conn -> getVendor(conn.getMetaData())
		).asSupplier().get();
	}

	/**
	 * Gets vendor from {@link DatabaseMetaData}.
	 *
	 * @param databaseMetaData The meta data to be checked
	 *
	 * @return The matched vendor
	 */
	public static DatabaseVendor getVendor(DatabaseMetaData databaseMetaData)
	{
		String driverName = ((JdbcSupplier<String>)
			() -> databaseMetaData.getDriverName().toLowerCase()
		).asSupplier().get();

		if (driverName.contains("h2")) {
			return H2;
		}

		if (driverName.contains("hsql")) {
			return HsqlDb;
		}

		if (driverName.contains("mysql")) {
			return MySql;
		}

		if (driverName.contains("postgresql")) {
			return PostgreSql;
		}

		if (driverName.contains("sql server")) {
			return MsSql;
		}

		if (driverName.contains("oracle")) {
			return Oracle;
		}

		if (driverName.contains("sqlite")) {
			return Sqlite;
		}

		return Unknown;
	}

	/**
	 * Builds {@link OperatorPredicate} for check of {@link DatabaseVendor}.
	 *
	 * @param needVendor The vendor to be checked
	 *
	 * @return The predicate can be used in {@link DefaultOperatorFactory}
	 *
	 * @see DefaultOperatorFactory
	 */
	public static OperatorPredicate buildOperatorPredicate(DatabaseVendor needVendor)
	{
		return metaData -> needVendor.equals(getVendor(metaData));
	}
}
