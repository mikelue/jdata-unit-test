package guru.mikelue.jdut.test;

import java.lang.reflect.Method;
import java.util.Optional;

import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class OptionalParameterListener implements IInvokedMethodListener {
	public OptionalParameterListener() {}

	@Override
	public void afterInvocation(IInvokedMethod method, ITestResult testResult) {}
	@Override
	public void beforeInvocation(IInvokedMethod testNgMethod, ITestResult testResult)
	{
		Object[] parameters = testResult.getParameters();

		Method method = testNgMethod.getTestMethod().getConstructorOrMethod()
			.getMethod();
		if (method == null) {
			return;
		}

		Class<?>[] typeOfParameters = method.getParameterTypes();
		for (int i = 0; i < typeOfParameters.length; i++) {
			if (
				typeOfParameters[i].isAssignableFrom(Optional.class) &&
				!Optional.class.isInstance(parameters[i])
			) {
				parameters[i] = parameters[i] != null ?
					Optional.of(parameters[i]) :
					Optional.empty();
			}
		}
	}
}
