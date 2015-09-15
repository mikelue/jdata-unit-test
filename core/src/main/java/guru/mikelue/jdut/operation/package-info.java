/**
 * The most important objects to effect data to database.<br>
 *
 * <h3>{@link guru.mikelue.jdut.operation.DataGrainOperator DataGrainOperator}</h3>
 *
 * <p>This interface defines the {@link guru.mikelue.jdut.datagrain.DataGrain DataGrain} to be process over a {@link java.sql.Connection Connection}.</p>
 *
 * <ul>
 * 	<li>{@link guru.mikelue.jdut.operation.DataRowsOperator DataRowsOperator} - The operator to process a {@link java.util.List list} of {@link guru.mikelue.jdut.datagrain.DataRow DataRows}</li>
 * 	<li>{@link guru.mikelue.jdut.operation.DataRowOperator DataRowOperator} - The operator to process every {@link guru.mikelue.jdut.datagrain.DataRow DataRow} in a {@link guru.mikelue.jdut.datagrain.DataGrain DataGrain}</li>
 * </ul>
 *
 * <h3>{@link guru.mikelue.jdut.operation.OperatorFactory OperatorFactory}</h3>
 *
 * <p>This interface defines the factory used to retrieving a {@link guru.mikelue.jdut.operation.DataGrainOperator DataGrainOperator} by a {@link java.lang.String String}.</p>
 *
 * <h4>{@link guru.mikelue.jdut.operation.DefaultOperatorFactory DefaultOperatorFactory}</h4>
 *
 * <p>
 * This implementation of <em>OperatorFactory</em> is an extensible, automatic vendor-detection factory you should use.<br>
 * Depending on provided {@link guru.mikelue.jdut.vendor vendor-specific implementation}, some pre-defined operation would be replaced with certain requirements for certain vendor.<br>
 * </p>
 *
 * <p>For example, to insert new data to a table on <a href="https://en.wikipedia.org/wiki/Microsoft_SQL_Server">MS SQL Server</a>, which contains <a href="https://technet.microsoft.com/en-us/library/aa933196%28v=sql.80%29.aspx">identity</a>,
 * the column should allow you to assign value directly.<br>
 * See {@link guru.mikelue.jdut.vendor.mssql.IdentityInsertOperator IdentityInsertOperator}</p>
 *
 * <h3>{@link guru.mikelue.jdut.operation.DefaultOperators DefaultOperators}</h3>
 * <ol>
 * 	<li>Defines the name of operator pre-defined by this framework, e.g. {@link guru.mikelue.jdut.operation.DefaultOperators#INSERT DefaultOperators.INSERT}. </li>
 * 	<li>Defines the lambda of operator(for {@link guru.mikelue.jdut.datagrain.DataGrain DataGrain}) pre-defined by this framework, e.g. {@link guru.mikelue.jdut.operation.DefaultOperators#insert DefaultOperators::insert}. </li>
 * 	<li>Defines the lambda of operator(for {@link guru.mikelue.jdut.datagrain.DataRow DataRow}) pre-defined by this framework, e.g. {@link guru.mikelue.jdut.operation.DefaultOperators#doInsert DefaultOperators::doInsert}. </li>
 * </ol>
 *
 * @see guru.mikelue.jdut.operation.DefaultOperators
 * @see guru.mikelue.jdut.operation.DefaultOperatorFactory
 * @see guru.mikelue.jdut.operation.DataGrainOperator
 */
package guru.mikelue.jdut.operation;
