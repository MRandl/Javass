package ch.epfl.javass;

/**
 * @author Mathis Randl
 *
 */
public final class Preconditions {
    private Preconditions() {
    }

    public static void checkArgument(boolean b) {
        if (!b)
            throw new IllegalArgumentException();
    }

    public static int checkIndex(int index, int size) {
        if (index < 0 || index >= size)
            throw new IndexOutOfBoundsException();
        return index;
    }
}