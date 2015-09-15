/**
 * For {@link guru.mikelue.jdut.operation.DataGrainOperator DataGrainOperator}, {@link guru.mikelue.jdut.decorate.ReplaceFieldDataDecorator ReplaceFieldDataDecorator}, this package contains pre-implemented functions of utility.<br>
 *
 * <h3>{@link guru.mikelue.jdut.function.DatabaseTransactional DatabaseTransactional}</h3>
 *
 * <p>Provides <a href="https://en.wikipedia.org/wiki/Database_transaction">database transaction</a> over <em>DataGrainOperator</em>
 * by {@link guru.mikelue.jdut.operation.DataGrainOperator#surroundedBy surrounding mechanism}.</p>
 *
 * <h3>{@link guru.mikelue.jdut.function.ValueSuppliers#cachedValue Supplier with caching value}</h3>
 * If you like to caching the value once the supplier has been called, you should use {@link guru.mikelue.jdut.function.ValueSuppliers#cachedValue ValueSuppliers.cachedValue(&lt;your_supplier&gt;)} to build a supplier which caches value.
 */
package guru.mikelue.jdut.function;
