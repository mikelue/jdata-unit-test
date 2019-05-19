package guru.mikelue.jdut.testng;

import java.lang.annotation.*;

import org.testng.annotations.DataProvider;

import static java.lang.annotation.ElementType.*;

/**
 * Defines the behaviours working by {@link IInvokedMethodYamlFactoryListener}.<br>
 *
 * With {@code @TestNGConfig(oneTimeOnly=true)}, the data conductor performs actions only on
 * <strong>first time and last time</strong> of multiple tests on a testing method.
 *
 * <p>For example(by {@link DataProvider}):</p>
 * <pre><code class="java">
 * &#64;Test &#64;TestNGConfig(oneTimeOnly=true) &#64;JdutResource
 * public void mixStuff(int speed)
 * {
 *     // Executes tested code
 *     // Assertions ...
 * }
 * // Executes DuetConductor.build() before case of "0".
 * // Executes DuetConductor.clean() after case of "1275".
 * &#64;DataProvider
 * Object[][] mixStuff()
 * {
 *     return new Object[][] {
 *         { 0 }, { 30 }, { 2000 },
 *         { -1 }, { 50000 }, { 1275 },
 *     };
 * }
 * </code></pre>
 */
@Target({ANNOTATION_TYPE, CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE, TYPE_PARAMETER, TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented @Inherited
public @interface TestNGConfig {
	/**
	 * With using of {@link DataProvider}, set this field to true to build data only once
	 * before first invocation and after last invocation.
	 *
	 * @return true if conduction data one time only
	 */
	public boolean oneTimeOnly() default false;
}
