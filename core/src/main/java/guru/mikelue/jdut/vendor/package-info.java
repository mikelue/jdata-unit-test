/**
 * This package defined
 *
 * <h2>Type enumration of vendor</h2>
 * {@link guru.mikelue.jdut.vendor.DatabaseVendor} defines the value of enumeration for pre-defined databases,
 * which implements {@link guru.mikelue.jdut.vendor.VendorType}.
 *
 * <h2>Choose vendor by {@link java.sql.DatabaseMetaData}</h2>
 * You could check {@link guru.mikelue.jdut.vendor.DatabaseVendor#getVendor(java.sql.DatabaseMetaData)} for
 * build-in selection engine for vendor.
 *
 * <h2>Vendor specific implementation</h2>
 * The specific vendor implementations are put into "<em>guru.mikelue.jdut.vendor.&lt;DatabaseName&gt;</em>" <br>
 *
 * Naming for databases:
 * <ul>
 * 	<li>oracle - <a href="https://www.oracle.com/database/">Oracle DBMS</a></li>
 * 	<li>massql - <a href="http://www.microsoft.com/en-us/server-cloud/products/sql-server/">MS SQL Server DBMS</a></li>
 * 	<li>mysql - <a href="http://www.mysql.com/">MySql DBMS</a></li>
 * 	<li>postgresql - <a href="http://www.postgresql.org/">PostgreSql DBMS</a></li>
 * 	<li>derby - <a href="https://db.apache.org/derby/">Derby Database</a></li>
 * 	<li>h2 - <a href="http://www.h2database.com/html/main.html">H2 Database</a></li>
 * 	<li>hsqldb - <a href="http://hsqldb.org/">HSQLDB Database</a></li>
 * 	<li>sqlite - <a href="https://www.sqlite.org/">Sqlite Database</a></li>
 * </ul>
 */

package guru.mikelue.jdut.vendor;
