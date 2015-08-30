package guru.mikelue.jdut.test;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface DoLiquibase {
	public boolean update() default true;
	public boolean rollback() default true;
}
