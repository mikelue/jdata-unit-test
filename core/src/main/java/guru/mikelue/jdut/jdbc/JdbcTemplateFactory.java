package guru.mikelue.jdut.jdbc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import guru.mikelue.jdut.jdbc.function.DbRelease;

/**
 * Template factory for building a bunch a JDBC working, with surrounding of {@link DbRelease#autoClose}.<br>
 *
 * See {@link guru.mikelue.jdut.jdbc JDBC Function} for usage of this factory.
 */
public final class JdbcTemplateFactory {
	private JdbcTemplateFactory() {}

	/**
	 * Surrounds surroundedJdbcFunction with surroundings(by sequence).
	 *
 	 * @param <T> The type of fed object
 	 * @param <R> The type of returned object
	 * @param surroundedJdbcFunction The function to be surrounded
	 * @param surroundings The surrounding starts with <b>outer block of syntax</b>
	 *
	 * @return The surrounded function
	 *
	 * @see #surround(JdbcFunction, SurroundingConfig)
	 */
	@SafeVarargs
	public static <T, R> JdbcFunction<T, R> surround(
		JdbcFunction<T, R> surroundedJdbcFunction,
		JdbcFunction.SurroundOperator<T, R>... surroundings
	) {
		return surround(
			surroundedJdbcFunction,
			surroundingList -> surroundingList.addAll(
				Arrays.asList(surroundings)
			)
		);
	}

	/**
	 * Surrounds surroundedJdbcFunction with surroundings(by sequence).
	 *
 	 * @param <T> The type of fed object
 	 * @param <R> The type of returned object
	 * @param surroundedJdbcFunction The function to be surrounded
	 * @param surroundingConfig The lambda expression for configuring surrounding list(starts with <b>outer block of syntax</b>)
	 *
	 * @return The surrounded function
	 *
	 * @see #surround(JdbcFunction, JdbcFunction.SurroundOperator...)
	 */
	public static <T, R> JdbcFunction<T, R> surround(
		JdbcFunction<T, R> surroundedJdbcFunction,
		SurroundingConfig<T, R> surroundingConfig
	) {
		List<JdbcFunction.SurroundOperator<T, R>> listingOfSurrounding = new ArrayList<>(3);

		surroundingConfig.accept(listingOfSurrounding);

		return surroundImpl(
			surroundedJdbcFunction, listingOfSurrounding
		);
	}

	private static <T, R> JdbcFunction<T, R> surroundImpl(
		JdbcFunction<T, R> surroundedJdbcFunction,
		List<JdbcFunction.SurroundOperator<T, R>> surroundings
	) {
		List<JdbcFunction.SurroundOperator<T, R>> reversedSurroundings = new ArrayList<>(surroundings);
		Collections.reverse(reversedSurroundings);

		for (JdbcFunction.SurroundOperator<T, R> surrounding: reversedSurroundings) {
			surroundedJdbcFunction = surroundedJdbcFunction.surroundedBy(surrounding);
		}

		return surroundedJdbcFunction;
	}

	/**
	 * Builds the runnable and work for JDBC operation.<p>
	 *
	 * @param <T> The type of fed object
	 * @param supplier The supplier of fed object
	 * @param worker The working code by fed object
	 *
	 * @return The instance of runnable function
	 */
	public static <T extends AutoCloseable> JdbcRunnable buildRunnable(
		JdbcSupplier<? extends T> supplier,
		JdbcVoidFunction<T> worker
	) {
		return () -> buildSupplier(
			supplier, worker.asJdbcFunction()
		).get();
	}

	/**
	 * Builds the runnable and work for JDBC operation.<p>
	 *
	 * @param <T> The type of fed object
	 * @param supplier The supplier of fed object
	 * @param worker The working code by fed object
	 * @param surroundingConfig The lambda expression for configuring surrounding list(starts with <b>outer block of syntax</b>)
	 *
	 *
	 * @return The instance of runnable function
	 */
	public static <T extends AutoCloseable> JdbcRunnable buildRunnable(
		JdbcSupplier<? extends T> supplier ,
		JdbcVoidFunction<T> worker,
		SurroundingConfig<T, Void> surroundingConfig
	) {
		return () -> buildSupplier(
			supplier, surround(worker.asJdbcFunction(), surroundingConfig)
		).get();
	}

	/**
	 * Builds the supplier and work for JDBC operation.<p>
	 *
	 * @param <T> The type of fed object
	 * @param <R> The type of returned object
	 * @param supplier The supplier of fed object
	 * @param worker The working code input as fed object
	 *
	 * @return The instance of result supplier function
	 */
	public static <T extends AutoCloseable, R> JdbcSupplier<R> buildSupplier(
		JdbcSupplier<? extends T> supplier,
		JdbcFunction<T, R> worker
	) {
		return () -> worker.surroundedBy(DbRelease::autoClose).apply(supplier.get());
	}

	/**
	 * Builds the supplier and work for JDBC operation.<p>
	 *
	 * @param <T> The type of fed object
	 * @param <R> The type of returned object
	 * @param supplier The supplier of fed object
	 * @param worker The working code input as fed object
	 * @param surroundingConfig The lambda expression for configuring surrounding list(starts with <b>outer block of syntax</b>)
	 *
	 * @return The instance of result supplier function
	 */
	public static <T extends AutoCloseable, R> JdbcSupplier<R> buildSupplier(
		JdbcSupplier<? extends T> supplier,
		JdbcFunction<T, R> worker,
		SurroundingConfig<T, R> surroundingConfig
	) {
		return buildSupplier(
			supplier, surround(worker, surroundingConfig)
		);
	}
}
