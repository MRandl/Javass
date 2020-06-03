package ch.epfl.javass.bits;

import static ch.epfl.javass.Preconditions.checkArgument;

public final class Bits64 {
    private Bits64() {} 

    /**
     * @param start
     *            the bit index where the 1 bit part begins
     * @param size
     *            the size of the 1 bit part of the Long
     * @return the Long whose bits are 1 between start and start+size and 0
     *         everywhere else
     */
    public static long mask(int start, int size) {
        checkArgument(size + start <= Long.SIZE && start >= 0 && size >= 0);
        
        if (size == Long.SIZE) 
            return -1;
        return ((1L << size) -1) << start;
    }

    /**
     * @param bits
     *            the Long that data will be extracted from
     * @param start
     *            the index at which the extraction begins
     * @param size
     *            the size of the extraction
     * @return the Long that is created through extraction from bits, with
     *         respect to start and size
     */
    public static long extract(long bits, int start, int size) {
        checkArgument(size + start <= Long.SIZE && start >= 0 && size >= 0);

        return (mask(start, size) & bits) >>> start;
    }

    /**
     * @param v1
     *            the first Long that extraction will be performed upon
     * @param s1
     *            the length of the extraction of v1
     * @param v2
     *            the second Long that extraction will be performed upon
     * @param s2
     *            the length of the extraction of v2
     * @return the concatenation of both extractions
     * @throws IllegalArgumentException
     *             if one or more of the conditions listed in checkArguments
     *             (begins 3 lines below) is/are false
     */
    public static long pack(long v1, int s1, long v2, int s2) {
        checkArgument(0 < s1 && s1 < Long.SIZE 
                   && 0 < s2 && s2 < Long.SIZE
                   && v1 >>> s1 == 0 && v2 >>> s2 == 0 
                   && s1 + s2 <= Long.SIZE);

        return v2 << s1 | v1;
    }

}
