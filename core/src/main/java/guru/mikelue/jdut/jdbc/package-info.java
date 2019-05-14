/**
 * This package provides coding library of JDBC by usage of lambda expression.<br>
 *
 * When we are coding database operations with JDBC, there are two things important, and, annoying:
 * <ol>
 * 	<li>The handling of {@link java.sql.SQLException}</li>
 * 	<li>The release of resource </li>
 * </ol>
 *
 * This package is intent on "surrounding" your JDBC code by <a href="https://docs.oracle.com/javase/tutorial/java/javaOO/lambdaexpressions.html">lambda expressions.</a><br>
 *
 * <h2>Quick example</h2>
 * Following example shows the basic usage of JDBC coding.
 *
 * <pre><code class="java">
 * // dataSource - The initialized object of javax.sql.DataSource
 *
 * JdbcVoidFunction&lt;Connection&gt; jdbcFunc = JdbcTemplateFactory.buildRunnable(
 *     () -&gt; dataSource.getConnection(), // The supplier provides connection object
 *     connection -&gt; {
 *         JdbcExecuteFactory.buildRunnable(
 *             () -&gt; connection.createStatement(), // The supplier provides statement object
 *             JdbcTemplateFactory.buildRunnable(
 *                 statement -&gt; {
 *                     statement.executeUpdate("INSERT INTO tab_car VALUES('CC-01', 20)");
 *                 };
 *             )
 *         ).runJdbc();
 *     }
 * );
 *
 * try {
 *     jdbcFunc.runJdbc();
 * } catch (SQLException e) {
 *     throws new RuntimeException(e);
 * }
 * </code></pre>
 *
 * <h2>{@link guru.mikelue.jdut.jdbc.JdbcFunction} and {@link guru.mikelue.jdut.jdbc.JdbcVoidFunction}</h2>
 *
 * {@link guru.mikelue.jdut.jdbc.JdbcFunction} is the core function to implement your JDBC operations, which returns value.<br>
 * {@link guru.mikelue.jdut.jdbc.JdbcVoidFunction} is the convenient function without return value.<br>
 *
 * <h2>Surrounding</h2>
 *
 * You may implement {@link guru.mikelue.jdut.jdbc.JdbcFunction.SurroundOperator SurroundOperator} to surround functions.<br>
 *
 * Using either {@link guru.mikelue.jdut.jdbc.JdbcFunction#surroundedBy JdbcFunction.surroundedBy()} or {@link guru.mikelue.jdut.jdbc.JdbcTemplateFactory#surround JdbcExecuteFactory.surround()},
 * you can surround your code by lambda expression.
 *
 * <pre><code class="java">
 * JdbcFunction&lt;Connection, Integer&gt; funcGetCount = conn -&gt; { 20 };
 * funcGetCount.surroundedBy(
 *     func -&gt; conn -&gt; {
 *         logger.info("Before calling");
 *         func.applyJdbc(conn);
 *         logger.info("After calling");
 *     }
 * );
 * </code></pre>
 *
 * <h2>Transaction</h2>
 *
 * With benefit of {@link guru.mikelue.jdut.jdbc.JdbcFunction.SurroundOperator}, there are some build-in functions to provide
 * surrounding of {@link java.sql.Connection} which is inside transaction-ready code.<br>
 *
 * Reference {@link guru.mikelue.jdut.jdbc.function.Transactional}.
 *
 * <pre><code class="java">
 * JdbcVoidFunction&lt;Connection&gt; jdbcFunc = JdbcTemplateFactory.buildRunnable(
 *     () -&gt; dataSource.getConnection(), // The supplier provides connection object
 *     connection -&gt; {
 *         // Using connection....
 *     },
 *     surroundingList -&gt; {
 *         surroundingList.add(Transactional::new);
 *     };
 * )
 * </code></pre>
 *
 * @see guru.mikelue.jdut.jdbc.JdbcFunction
 * @see guru.mikelue.jdut.jdbc.JdbcTemplateFactory
 */
package guru.mikelue.jdut.jdbc;
