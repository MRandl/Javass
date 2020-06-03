package ch.epfl.javass.bits;

import static ch.epfl.javass.Preconditions.checkArgument;

/**
 * @author Mathis Randl
 * 
 */
public final class Bits32 {
    
    private Bits32() {
    } // prevents unwanted instantiation, constructor cannot be invoked

    /**
     * @param start
     *            the bit index where the 1 bit part begins
     * @param size
     *            the size of the 1 bit part of the integer
     * @return the integer whose bits are 1 between start and start+size and 0
     *         everywhere else
     */
    public static int mask(int start, int size) {
        checkArgument(size + start <= Integer.SIZE && start >= 0 && size >= 0);
        if (size == 32) 
            return -1; 
        return ((1 << size) -1) << start; 
    }

    /**
     * @param bits
     *            the integer that data will be extracted from
     * @param start
     *            the index at which the extraction begins
     * @param size
     *            the size of the extraction
     * @return the integer that is created through extraction from bits, with
     *         respect to start and size
     */
    public static int extract(int bits, int start, int size) {
        checkArgument(size + start <= Integer.SIZE && start >= 0 && size >= 0);

        return (mask(start, size) & bits) >>> start;
    }

    /**
     * @param v1
     *            the first integer that extraction will be performed upon
     * @param s1
     *            the length of the extraction of v1
     * @param v2
     *            the second integer that extraction will be performed upon
     * @param s2
     *            the length of the extraction of v2
     * @return the concatenation of both extractions
     * @throws IllegalArgumentException
     *             if one or more of the conditions listed in checkArguments
     *             (begins 3 lines below) is/are false
     */
    
    public static int pack(int v1, int s1, int v2, int s2) {
        checkArgument(0 < s1 && s1 < Integer.SIZE 
                   && 0 < s2 && s2 < Integer.SIZE
                   && v1 >>> s1 == 0 && v2 >>> s2 == 0 
                   && s1 + s2 <= Integer.SIZE);

        return extract(v2, 0, s2) << s1 |
               extract(v1, 0, s1); 
        // extracts both chunks of data and concatenates them: the second one (v2)
        // is shifted to the left and the first one (v1) is inserted in the
        //created space
    }

    /**
     * @param v1
     *            the first integer that extraction will be performed upon
     * @param s1
     *            the length of the extraction of v1
     * @param v2
     *            the second integer that extraction will be performed upon
     * @param s2
     *            the length of the extraction of v2
     * @param v3
     *            the third integer that extraction will be performed upon
     * @param s3
     *            the length of the extraction of v3
     * @return the concatenation of the three extractions
     * @throws IllegalArgumentException
     *             if one or more of the conditions listed in checkArguments
     *             (begins 3 lines below) is/are false
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3) { 
        checkArgument((0 < s1) && (s1 < Integer.SIZE) 
                   && (0 < s2) && (s2 < Integer.SIZE) 
                   && (0 < s3) && (s3 < Integer.SIZE)
           && (v1 >>> s1 == 0) && (v2 >>> s2 == 0) && (v3 >>> s3 == 0)
           && (s1 + s2 + s3 <= Integer.SIZE));

        return extract(v3, 0, s3) << s1 + s2 | 
               extract(v2, 0, s2) << s1|
               extract(v1, 0, s1); 
        //extracts the three chunks of data and concatenates them: similar to the pack
        //method for 2 arguments, but extended for 3
    }

    /**
     * @param v1
     *            first integer to be extracted
     * @param s1
     *            size of the first extraction
     * @param v2
     *            second integer to be extracted
     * @param s2
     *            size of the second extraction
     * @param v3
     *            third integer to be extracted
     * @param s3
     *            size of the third extraction
     * @param v4
     *            fourth integer to be extracted
     * @param s4
     *            size of the fourth extraction
     * @param v5
     *            fifth integer to be extracted
     * @param s5
     *            size of the fifth extraction
     * @param v6
     *            sixth integer to be extracted
     * @param s6
     *            size of the sixth extraction
     * @param v7
     *            seventh integer to be extracted
     * @param s7
     *            size of the seventh extraction
     * @return the concatenation of all of these integers, with respect to their
     *         size
     */
    public static int pack(int v1, int s1, int v2, int s2, int v3, int s3,
            int v4, int s4, int v5, int s5, int v6, int s6, int v7, int s7) {

        int[] values = new int[] { v1, v2, v3, v4, v5, v6, v7 }; 
        // Let's group them immediately
        int[] sizes = new int[] { 0, s1, s2, s3, s4, s5, s6, s7 }; 
        // 0 will be useful later
        // Arrays may take a bit more memory than required but are easier to use
        
        boolean assertion = true; 
        // assertion will be our iterator over the conditions we want to verify
        
        int sumOfSizes = 0; 
        // must not go over 32

        // it's check time !

        for (int i = 0; i <= 6; ++i) {
            assertion = assertion && 0 < sizes[i+1] 
                    // checks if sizes are positive
                    && values[i] >>> sizes[i+1] == 0; 
                    // checks if integers are 0 when bitshifted to the right
            sumOfSizes += sizes[i+1];
        }
        assertion = assertion && sumOfSizes <= Integer.SIZE;
        checkArgument(assertion);

        // good to go !

        int bitShifter = 0;
        int finalValue = 0;
        for (int i = 0; i < values.length; ++i) { 
            // it's basically 7 times what we did 2 and 3 times earlier, 
            // but written in a loop to compact it a bit
            
            bitShifter += sizes[i]; 
            //the fact that 0 is in sizes[] is useful here:
            //the first bitshift never occurs that way
            
            finalValue |= (extract(values[i], 0, sizes[i+1]) << bitShifter); 
            // finalValue becomes the concatenation of the extracted values
        }
        return finalValue;
    }
}
