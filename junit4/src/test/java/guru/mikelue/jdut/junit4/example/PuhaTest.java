// tag::package[]
package guru.mikelue.jdut.junit4.example;
// end::package[]

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TestRule;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.junit4.JdutYamlFactory;

// tag::test_class[]
// file: classpath:guru/mikelue/jdut/junit4/example
// 		-> PuhaTest.yaml
@JdutResource // <1>
public class PuhaTest {
	@ClassRule // <2>
	public static TestRule rule = JdutYamlFactory.buildByDataSource(DataSourceGetter::get);

	public PuhaTest() {}

	@Test
	public void grow()
	{
		// Executes tested code
		// Assertions...
	}
}
// end::test_class[]
