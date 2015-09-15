/**
 * jdata-unit-test(JDUT) is a framework to ease the data building/cleaning
 * for automatic testing, which the test is repeatable without interfere each other.<br>
 *
 * <h3>{@link guru.mikelue.jdut.DataConductor DataConductor}</h3>
 *
 * The conductor is the core executor for applying changes according {@link DataGrainOperator}.
 *
 * <h3>{@link guru.mikelue.jdut.DuetConductor DuetConductor}</h3>
 * Depending on implementation, this interface only defines two methods to represent the actions for testing:
 * <ol>
 * 	<li>{@link guru.mikelue.jdut.DuetConductor#build() build()} - Get called before testing</li>
 * 	<li>{@link guru.mikelue.jdut.DuetConductor#clean() clean()} - Get called after testing</li>
 * </ol>
 *
 * <p>{@link guru.mikelue.jdut.DuetFunctions DuetFunctions} is an interface used to provide the functions
 * for building/cleaning actions of testing.</p>
 */
package guru.mikelue.jdut;

import guru.mikelue.jdut.operation.DataGrainOperator;
