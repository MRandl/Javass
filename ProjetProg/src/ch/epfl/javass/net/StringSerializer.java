package ch.epfl.javass.net;

import java.util.Base64;

public final class StringSerializer {
    private StringSerializer(){}

    /**
     * @param l a long
     * @return the serialized version of it
     */
    //NO PUBLIC/PRIVATE IDENTIFIERS ON THESE ON PURPOSE
    static String serializeLong(long l){
        return Long.toHexString(l);
    }

    /**
     * @param i an int
     * @return the serialized version of it
     */
    static String serializeInt(int i){
        return Integer.toHexString(i);
    }

    /**
     * @param s a string
     * @return the long contained in the string
     * @throws NumberFormatException if no long can possibly be extracted
     */
    static long deserializeLong(String s){
        return Long.parseUnsignedLong(s, 16);
    }

    /**
     * @param s a string
     * @return the int contained in the string
     * @throws NumberFormatException if no int can possibly be extracted
     */
    static int deserializeInt(String s){
        return Integer.parseUnsignedInt(s, 16);
    }

    /**
     * @param s a string
     * @return the serialized version of it
     */
    static String serializeString(String s){
        return Base64.getEncoder().encodeToString(s.getBytes());
    }

    /**
     * @param s a string
     * @return the text originally contained before serialization
     */
    static String deserializeString(String s){
        return new String(Base64.getDecoder().decode(s));
    }

    /**
     * @param c a splitting character
     * @param values an ellipse of string
     * @return a string with all the values of values joined by c
     */
    static String theJoiner(char c, String ... values){
        return  String.join(Character.toString(c), values);
    }

    /**
     * @param c a splitting character
     * @param joinedValues a string containing multiple words separated by c
     * @return the array 
     */
    static String[] theSplitter(char c, String joinedValues){ return joinedValues.split(Character.toString(c)); }
}

