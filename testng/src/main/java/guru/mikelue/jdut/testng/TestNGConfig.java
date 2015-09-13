package guru.mikelue.jdut.testng;

import java.lang.annotation.*;

import org.testng.annotations.DataProvider;

import static java.lang.annotation.ElementType.*;

/**
 * Defines the behaviours working by {@link IInvokedMethodYamlFactoryListener}.
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
