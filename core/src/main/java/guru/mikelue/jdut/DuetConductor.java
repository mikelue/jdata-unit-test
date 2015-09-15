package guru.mikelue.jdut;

/**
 * Defines the method for data building/cleaning for testing.
 *
 * @see DuetFunctions
 */
public interface DuetConductor {
    /**
     * Builds data.
     */
    public void build();
    /**
     * Cleans data.
     */
    public void clean();
}
