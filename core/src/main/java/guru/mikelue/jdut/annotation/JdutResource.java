package guru.mikelue.jdut.annotation;

import java.lang.annotation.*;
import static java.lang.annotation.ElementType.*;

/**
 * Only defines the annotation of resources, the detail of usage is defined by controller/contrainer which
 * loads the instance of this annotation.
 */
@Target({ANNOTATION_TYPE, CONSTRUCTOR, FIELD, LOCAL_VARIABLE, METHOD, PACKAGE, PARAMETER, TYPE, TYPE_PARAMETER, TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
@Documented @Inherited
public @interface JdutResource {
	/**
	 * The resources of Strings.
	 *
	 * @return The array of resources as text
	 */
	public String[] resources() default {};
	/**
	 * The resources defined by type of {@link JdutResourceConfig} which is just a tagging interface.
	 *
	 * @return The array of resources as classes
	 */
	public Class<JdutResourceConfig>[] resourcesConfig() default {};
}
