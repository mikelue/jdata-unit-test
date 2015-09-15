/**
 * Defines the interface of decoration for {@link guru.mikelue.jdut.datagrain.DataGrain DataGrain}<br>
 *
 * <h3>The decoration</h3>
 *
 * <p>The {@link guru.mikelue.jdut.decorate.DataGrainDecorator DataGrainDecorator}
 * defines the lambda expression would be used to change data(or schema) of a {@link guru.mikelue.jdut.datagrain.DataRow DataRow}.</p>
 *
 * <p>{@link guru.mikelue.jdut.decorate.DataFieldDecorator DataFieldDecorator} is a fine-grained extension of decorator for every field.</p>
 *
 * <h3>{@link guru.mikelue.jdut.decorate.TableSchemaLoadingDecorator TableSchemaLoadingDecorator}</h3>
 * The dependency of {@link guru.mikelue.jdut.DataConductor DataConductor}, this decorator would load schema of database before a {@link guru.mikelue.jdut.datagrain.DataRow DataRow} is going to be {@link guru.mikelue.jdut.operation.DataGrainOperator operated}.
 *
 * <h3>{@link guru.mikelue.jdut.decorate.ReplaceFieldDataDecorator ReplaceFieldDataDecorator}</h3>
 *
 * <p>You may replace a value of field with another value by certain condition(the {@link java.util.function.Predicate Predicate} over {@link guru.mikelue.jdut.datagrain.DataField DataField}).</p>
 *
 * <h4>Default value for null</h4>
 * <p>The predicate of {@link guru.mikelue.jdut.function.DataFieldPredicates#nullValue DataFieldPredicates::nullValue} could be used as condition for replacing null value.</p>
 *
 * <h4>Side effects to {@link java.util.function.Supplier Supplier} of field data</h4>
 * <p>If you don't want to evaluate the lambda expression, which provides value of a field, you could uses {@link guru.mikelue.jdut.function.DataFieldPredicates#nonSupplier} to prevent the evaluation until the real needing for that field.</p>
 */

package guru.mikelue.jdut.decorate;
