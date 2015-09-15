/**
 * This package contains the primary objects of bean for data definitions.<br>
 *
 * <h3>{@link guru.mikelue.jdut.datagrain.DataGrain DataGrain}</h3>
 * <p>The main object to contains a list of {@link guru.mikelue.jdut.datagrain.DataRow DataRow}</p>
 *
 * <p>
 * This object could be {@link guru.mikelue.jdut.datagrain.DataGrain#decorate decorated} by {@link guru.mikelue.jdut.decorate.DataGrainDecorator DataGrainDecorator}, which you can:
 * </p>
 *
 * <ul>
 * 	<li>Assign value of field depends on another value of another field</li>
 * 	<li>Put {@link guru.mikelue.jdut.decorate.ReplaceFieldDataDecorator default value} to a field</li>
 * </ul>
 *
 * <h3>{@link guru.mikelue.jdut.datagrain.SchemaTable SchemaTable} and {@link guru.mikelue.jdut.datagrain.SchemaColumn SchemaColumn}</h3>
 * These two objects is a kind of containers for meta-data of a {@link guru.mikelue.jdut.datagrain.DataRow dataRow}.
 */
package guru.mikelue.jdut.datagrain;
