/**
 * Defines annotations could be used to integrate testing framework.<br>
 *
 * <h3>{@link guru.mikelue.jdut.annotation.JdutResource JdutResource}</h3>
 * <p>
 * This annotation defins the {@link String} or {@link Class} type properties for one to multiple resources.<br>
 * How to load the resources is defined by implementing controller/container.
 * </p>
 *
 * <p>The {@link guru.mikelue.jdut.annotation.JdutResourceConfig} is just a tagging interface to {@link guru.mikelue.jdut.annotation.JdutResource#resourcesConfig Jdut.Resource.resourcesConfig()}.</p>
 *
 * <h3>{@link guru.mikelue.jdut.annotation.IfDatabaseVendor IfDatabaseVendor}</h3>
 * <p>If you are developing application with support of various vendor of databases, this annotation let you declare the semantics on method/class., etc.</p>
 *
 * <p>
 * As same as {@link guru.mikelue.jdut.annotation.JdutResource JdutResource}, the implementation of the semantics for {@link guru.mikelue.jdut.vendor.DatabaseVendor DatabaseVendor} is defined by implementation.<br>
 * You may want to check out the {@link AnnotationUtil#matchDatabaseVendor} for default matching logic.<br>
 * </p>
 *
 * <h3>{@link guru.mikelue.jdut.annotation.JdutResourceNaming JdutResourceNaming}</h3>
 * <p>There are some supplementary methods provided by this object to generates name of file by certain context(e.g. {@link java.lang.Class Class} or {@link java.lang.reflect.Method Method}).</p>
 *
 * @see <a href="https://testng.org/">TestNG</a>
 * @see <a href="https://junit.org/junit4/">JUnit</a>
 * @see DatabaseVendor
 */
package guru.mikelue.jdut.annotation;

import guru.mikelue.jdut.vendor.DatabaseVendor;
