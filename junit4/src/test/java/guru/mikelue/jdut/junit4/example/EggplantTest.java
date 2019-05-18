// tag::package[]
package guru.mikelue.jdut.junit4.example;
// end::package[]

import org.junit.Rule;
import org.junit.Test;

import guru.mikelue.jdut.annotation.JdutResource;
import guru.mikelue.jdut.junit4.JdutYamlFactory;

// tag::test_class[]
public class EggplantTest {
	public EggplantTest() {}

	@Rule // <1>
	public JdutYamlFactory jdutYamlFactoryForMethodLevel = new JdutYamlFactory(DataSourceGetter.get());

	// file: classpath:guru/mikelue/jdut/junit4/example
	// 		-> EggplantTest-grow.yaml
	@Test @JdutResource // <2>
	public void grow()
	{
		// Executes tested code
		// Assertions...
	}
}
// end::test_class[]
