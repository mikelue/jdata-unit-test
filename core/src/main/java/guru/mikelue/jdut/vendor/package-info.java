/**
 * This package defines the {@link guru.mikelue.jdut.vendor.DatabaseVendor Vendors},
 * which is used to discriminate vendor-specific funcdtions.<br>
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
 * 	<li>oracle - <a href="https://www.oracle.com/database/" target="_blank">Oracle DBMS</a></li>
 * 	<li>massql - <a href="https://www.microsoft.com/en-us/sql-server/sql-server-2017" target="_blank">MS SQL Server DBMS</a></li>
 * 	<li>mysql - <a href="https://www.mysql.com/" target="_blank">MySql DBMS</a></li>
 * 	<li>postgresql - <a href="https://www.postgresql.org/" target="_blank">PostgreSql DBMS</a></li>
 * 	<li>derby - <a href="https://db.apache.org/derby/" target="_blank">Derby Database</a></li>
 * 	<li>h2 - <a href="https://www.h2database.com/" target="_blank">H2 Database</a></li>
 * 	<li>hsqldb - <a href="https://hsqldb.org/" target="_blank">HSQLDB Database</a></li>
 * 	<li>sqlite - <a href="https://www.sqlite.org/" target="_blank">Sqlite Database</a></li>
 * </ul>
 */

package guru.mikelue.jdut.vendor;
