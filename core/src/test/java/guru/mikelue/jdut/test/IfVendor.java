package guru.mikelue.jdut.test;

import java.lang.annotation.*;

import guru.mikelue.jdut.vendor.DatabaseVendor;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented @Inherited
public @interface IfVendor {
	public DatabaseVendor[] match() default DatabaseVendor.Unknown;
	public DatabaseVendor[] notMatch() default DatabaseVendor.Unknown;
}
