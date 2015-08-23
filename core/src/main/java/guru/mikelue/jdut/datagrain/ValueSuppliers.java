package guru.mikelue.jdut.datagrain;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * This utility builds special version of {@link Supplier}.
 */
public final class ValueSuppliers {
	private ValueSuppliers() {}

	/**
	 * Builds a supplier which caches the value of source supplier.
	 *
	 * @param <T> The type of result
	 * @param sourceSupplier The source of supplier to be cached
	 *
	 * @return The supplier supports caching value
	 */
	public static <T> Supplier<T> cachedValue(Supplier<? extends T> sourceSupplier)
	{
		return new Supplier<T>() {
			Optional<T> cachedValue = null;

			@Override
			public T get()
			{
				if (cachedValue == null) {
					cachedValue = Optional.ofNullable(sourceSupplier.get());
				}

				return cachedValue.orElse(null);
			}
		};
	}
}
