package guru.mikelue.jdut.jdbc;

import java.util.List;
import java.util.function.Consumer;

/**
 * This interface defines the lambda expression of configuration of {@link List} of {@link JdbcFunction.SurroundOperator}.
 *
 * @param <T> The type of fed object of the surrounded function
 * @param <R> The type of returned object the surrounded function
 */
@FunctionalInterface
public interface SurroundingConfig<T, R> extends Consumer<List<JdbcFunction.SurroundOperator<T, R>>> {}
