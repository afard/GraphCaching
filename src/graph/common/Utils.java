package graph.common;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;

/** This class holds together a bunch of static helper methods 
* @author Usman Nisar
*/

public class Utils {
    /***********************************************
    * This methods converts a list of integer to an integer array
    * @param list The list of integers
    * @return the integer array
    */    
    public static int[] convertArrayListToInteger(List<Integer> list) {
        int[] result = new int[list.size()];
        for ( int i = 0 ; i < result.length ; i++ )
            result[i] = list.get(i);
        return result;
    }
    /***********************************************
    * This methods converts a set of integers to an integer array
    * @param set The set of integers
    * @return the integer array
    */
    public static int[] convertHashSetToArray(Set<Integer> set) {
        int[] result = new int[set.size()];
        Iterator<Integer> it = set.iterator();
        int i = 0;
        while (it.hasNext())
            result[i++] = it.next();
        return result;
    }
    /***********************************************
    * This methods converts a set of integers to an integer list
    * @param set The set of integers
    * @return the integer list
    */
    public static List<Integer> convertSetToList(Set<Integer> set) {
        List<Integer> result = new ArrayList<Integer> ();
        for ( int s: set )
            result.add(s);
        return result;
    }
    /***********************************************
    * This methods converts an integer array to a set of integers
    * @param arr The integer array
    * @return Set<Integer> The set of integers
    */
    public static Set<Integer> convertArrayToHashSet(int[] arr) {
        Set<Integer> out = new HashSet<Integer> ();
        for ( int i = 0 ; i < arr.length ; i++ )
            out.add(arr[i]);

        return out;
    }
    /***********************************************
    * This methods does an intersection between two sets of integers
    * @param first The first set
    * @param second The other set
    * @return int[] Returns an array of integers that are an intersection of the two sets
    */
    public static int[] intersectionOfTwoArrays(Set<Integer> first, Set<Integer> second) {
        Set<Integer> result = new HashSet<Integer> ();
        for (int k: first) {
            if (second.contains(k))
                result.add(k);
        }
        return convertHashSetToArray(result);
    }
    /***********************************************
    * This method does an intersection check between two arrays of integers
    * @param first The first array
    * @param second The other array
    * @return boolean Returns a boolean check between two arrays
    */
    public static boolean arrayIntersectWithArray(int[] first, int[] second) {
        if ( first == null || second == null )
            return false;
        
        for ( int i = 0 ; i < first.length ; i++ ) {
            for ( int j = 0 ; j < second.length ; j++ ) {
                if (first[i] == second[j])
                    return true;
            }
        }
        return false;
    }
    /*************************************************************
     * A private method that makes sure the first array is a subset of second
     * @param first The first array
     * @param second The second array
     * @return boolean Returns true if all the entries in first exist in second, false otherwise
     */
    public static boolean arraySubSetOfAnArray(int[] first, int[] second) {
        boolean flag = false;

        if ( first == null || second == null )
            return flag;

        // iterate through all the vertices in Q
        for (int i = 0; i < first.length; i++) {
            flag = false;

            // look for the current vertex in the data graph
            for (int j = 0; j < second.length; j++) {
                if (second[j] == first[i]) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return false;
            }
        }
        return true;
    }
    /***********************************************
    * This methods converts an integer array to a list of integers
    * @param arr The array of integers
    * @return the integer list
    */
    public static List<Integer> convertArrayToArrayList(int[] arr) {
        List<Integer> out = new ArrayList<Integer> ();
        for ( int i = 0 ; i < arr.length ; i++ )
            out.add(arr[i]);

        return out;
    }
    /***********************************************
    * Makes a copy of an integer array
    * @param source The source array
    * @return the new copy of the integer array
    */        
    public static int[] copyIntArray(int[] source){
        int[] copy = new int[source.length];
        for( int i = 0 ; i < source.length; i++) {
            copy[i] = source[i];
        }
        return copy;
    }

    /***********************************************
    * Makes a copy of an integer array with some detailed parametesr
    * @param source The source array
    * @param destination The destination array
    * @param startIndex The index where to start copying from
    * @param length The total number of integer values to copy to destination array
    * @return Boolean If the operation was successful, false otherwise
    */   
    public static boolean copyIntArray(int[] source, int[] destination, int startIndex, int length){
        if (length > source.length || (startIndex + length) > destination.length)
            return false;
        for( int i = 0 ; i < length; i++) {
            destination[i + startIndex] = source[i];
        }
        return true;
    }
}
