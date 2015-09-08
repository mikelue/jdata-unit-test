package guru.mikelue.jdut.annotation;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;

import guru.mikelue.jdut.vendor.DatabaseVendor;

/**
 * Defines the annotation used to check if the current vendor is matched the environment(implemented by container, framework., etc.).<br>
 *
 * The default value is {@link DatabaseVendor#Unknown}, which means nothing to checked(everything is passed always).
 */
@Target({ANNOTATION_TYPE, CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE, TYPE_PARAMETER, TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented @Inherited
public @interface IfDatabaseVendor {
	/**
	 * Any one of the vendors matches environment.
	 */
	public DatabaseVendor[] match() default DatabaseVendor.Unknown;
	/**
	 * Any one of the vendors must not match environment.
	 */
	public DatabaseVendor[] notMatch() default DatabaseVendor.Unknown;
}
