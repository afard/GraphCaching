/** This class provides methods for determining graph metrics
 *  @author Arash Fard
 *  @date 11/19/2013
 *  @see LICENSE (MIT style license file).
 *  @see http://cobweb.cs.uga.edu/~jam/scalation_1.1/src/main/scala/scalation/graphalytics/GraphMetrics.scala
 */
package graph.common;




import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

public class GraphMetrics {

    // adjacency list of the graph, the first integer in each sub-array is the lable
    public int[][] vertices  = null;
    private int n             = 0;              // number of vertices
    private Queue<Integer> qu = new LinkedList<Integer> ();  // a queue supporting BFS
    private boolean[] go      = null;                        // vertex visitation flag
    private int[] len         = null;       // path-length from vertex i to j
    public int[][] eccentricity = null;    // eccentricity of the vertices
    private List<Set<Integer>> undirectedG = null; // corresponding undirected graph

    /*************************************************************
     * Default constructor for the class
     * @param 
     */
    public GraphMetrics(int[][] theGraph) {
        if(theGraph == null)
        System.out.println("Error: GraphMetrics object can not be created for a null graph");
        vertices = theGraph;
        n = vertices.length;
        go = new boolean[n];
        len = new int[n];
        // calculate eccentricity for all vertices
        calcEcc();
    }

    /**
     * Calculates the eccentricity of all vertices
     * assuming the graph is connected
     */
    private void calcEcc() {
        eccentricity = new int[n][1];
        for(int i = 0; i < n; i++)
            eccentricity[i][0] = -1;
        // making the undirected copy
        undirectedG = new ArrayList<Set<Integer>>();
        for(int i = 0; i < n ; i++) {
            Set<Integer> aSet = new HashSet<Integer>();
            undirectedG.add(aSet);
        } // for
        for(int i = 0; i < n ; i++) {
            for(int j = 1; j < vertices[i].length; j++) { // the first int is the lable
                if(vertices[i][j] >= n) {
                    System.out.println("Error: the input graph structure is not complete");
                }
                undirectedG.get(i).add(vertices[i][j]);
                undirectedG.get(vertices[i][j]).add(i);
            } // for
        } // for

        for(int i = 0; i < n; i++) {
            // finding ecc of vertex i
            for(int j = 0; j < n; j++) {
                go[j] = true;
                len[j] = 0;
            }

            qu.clear();
            qu.offer(i);
            while (! qu.isEmpty()) visit(); // visit vertices in BFS order
            // finding the max len
            eccentricity[i][0] = len[0];
            for(int j = 1; j < n; j++)
                if(len[j] >  eccentricity[i][0])
                    eccentricity[i][0] = len[j];                    
        } // for
    } // calcEcc

    /** Visit the next vertex (at the head of queue 'qu'), mark it, compute the
     *  path-length 'len' for each of its children and put them in the queue. 
     */
    private void visit() {
    	int r = 1;
    	
        int j = qu.poll(); 
       // System.out.println("the next vertex is :"+j);// take next vertex from queue
        go[j] = false;                                // mark as visited
        int len_c = len[j] + 1;                       // path-length to child vertices
        //System.out.println("the childern of the vertice "+j+"is"+undirectedG.get(j));
        for (int c : undirectedG.get(j)) {                   // for each child of vertex j
            if (go[c] && len[c] == 0) {
                len[c] = len_c;                       // distance from vertex i to c
                
                qu.offer(c);                          // put child c in queue
                
            } // if
        } // for
    } // visit

    public int ecc(int id) {
        if(id > n-1 || id < 0) {
            System.out.println("Error in GraphMetrics: id out of bound");
            return -1;
        }
        return eccentricity[id][0];
    } // ecc

    /*********************************************************************************** 
     * Compute the diameter of the graph (longest shortest path and maximum
     *  eccentricity).
     */
    public int diam() {
        int max = eccentricity[0][0];
        for (int i = 1; i < n; i++) {
            if (eccentricity[i][0] != -1 && eccentricity[i][0] > max) 
                max = eccentricity[i][0];
        } // for
        return max;
    } // diam

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /************************************************************************************ 
     * Compute the radius of the graph (minimum eccentricity).
     */
    public int rad() {
        int min = eccentricity[0][0]; // assuming it is not -1
        for (int i = 1; i < n; i++) {
            if (eccentricity[i][0] != -1 && eccentricity[i][0] < min) 
                min = eccentricity[i][0];
        } // for
        return min;
    } // rad

    //::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
    /*************************************************************************************
     * Return the central vertices, those with eccentricities equal to the radius.
     */
    public Set<Integer> central() {
        Set<Integer> centers = new HashSet<Integer>();
        int[] ecc_v = new int[n];
        for(int i = 0;i<n;i++){
        	ecc_v[i] = ecc(i);
        }
        int radius = ecc(0);
        for(int i = 1; i < n; i++) 
            if(ecc_v[i]< radius){
            	radius = ecc_v[i];
            }
        for(int i = 0; i < n; i++){
        	if(ecc_v[i]==radius);
        	centers.add(i);
        		
        }
        return centers;
    } // central
    

    public static void main(String[] args) { // test code
        int[][] graph = {
         {1, 1},       // 0
         {2, 2},           // 1
         {3},           // 2
//         {4, 4},              // 3
//         {5},        // 4
//         {104, 4,6,7},       // 5
//         {100, 7},           // 6
//         {102, 8},           // 7
//         {103, 9},           // 8
//         {100, 1},           // 9
//         {102, 9}            // 10
        };
        // diameter = 4, radius = 3, centers = {0,1,4,5,6,9,10}
        System.out.println("***************");
        for (int i = 0; i < graph.length; i++) {
            System.out.print(i + " ");
            if (graph[i] != null) {
                for (int j = 0; j < graph[i].length; j++) {
                    System.out.print(graph[i][j] + " ");
                }
            }
            System.out.println(" ");
        }

        GraphMetrics grM = new GraphMetrics(graph);
        System.out.println("radius = " + grM.rad());
        System.out.println("diameter = " + grM.diam());
        Set<Integer> centers = grM.central();
        System.out.print("centers = ");
        for(int c : centers)
            System.out.print(c + " ");
        System.out.println();
    } // main

} //class

