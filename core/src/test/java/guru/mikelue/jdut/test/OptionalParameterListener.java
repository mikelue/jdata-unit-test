package guru.mikelue.jdut.test;

import java.util.Optional;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.internal.ConstructorOrMethod;

/**
 * Tries to convert the type of non-optional value to {@link Optional}.
 */
public class OptionalParameterListener implements IInvokedMethodListener {
	public OptionalParameterListener() {}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {}
	@Override
	public void beforeInvocation(IInvokedMethod testNgMethod, ITestResult testResult)
	{
		Class<?>[] typeOfParameters = null;

		ConstructorOrMethod calledTarget = testNgMethod.getTestMethod().getConstructorOrMethod();
		if (calledTarget.getMethod() != null) {
			typeOfParameters = calledTarget.getMethod().getParameterTypes();
		} else {
			typeOfParameters = calledTarget.getConstructor().getParameterTypes();
		}

		/**
		 * Checks every parameter
		 */
		Object[] parameters = testResult.getParameters();
		for (int i = 0; i < typeOfParameters.length; i++) {
			if (
				Optional.class.equals(typeOfParameters[i]) &&
				!Optional.class.isInstance(parameters[i])
			) {
				parameters[i] = parameters[i] != null ?
					Optional.of(parameters[i]) :
					Optional.empty();
			}
		}
		// :~)
	}
}
