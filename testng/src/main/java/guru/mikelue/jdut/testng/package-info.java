/**
 * This package mainly contains various <a href="https://testng.org/doc/documentation-main.html#testng-listeners">listeners</a> of <a href="https://testng.org/">TestNG</a> for initializing {@link YamlConductorFactory}.<br>
 *
 * <h3>Listeners</h3>
 * <ul>
 * 	<li>{@link guru.mikelue.jdut.testng.IInvokedMethodYamlFactoryListener} - Listeners for method invocation, as {@link IInvokedMethodListener}</li>
 * 	<li>{@link guru.mikelue.jdut.testng.ISuiteYamlFactoryListener} - Listeners for suite events, as {@link ISuiteListener}</li>
 * 	<li>{@link guru.mikelue.jdut.testng.ITestContextYamlFactoryListener} - Listeners for test events, as {@link ITestListener}</li>
 * </ul>
 *
 * <h3>Write your own listener</h3>
 * The easy way to write your own listener is to inherit one of above listeners on your customization.<br>
 *
 * Otherwise, you may use <a href="https://testng.org/doc/documentation-main.html#annotations">@BeforeXXX</a> or <a href="https://testng.org/doc/documentation-main.html#annotations">@AfterXXX</a> of TestNG and <a href="https://github.com/mikelue/jdata-unit-test/wiki/API-Guideline-of-YAML" target="_blank">API of YAML loading</a>.
 *
 * @see guru.mikelue.jdut.testng.IInvokedMethodYamlFactoryListener
 * @see guru.mikelue.jdut.testng.ISuiteYamlFactoryListener
 * @see guru.mikelue.jdut.testng.ITestContextYamlFactoryListener
 */
package guru.mikelue.jdut.testng;

import org.testng.IInvokedMethodListener;
import org.testng.ISuiteListener;
import org.testng.ITestListener;

import guru.mikelue.jdut.yaml.YamlConductorFactory;
