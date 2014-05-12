package graph.common;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.javatuples.Pair;

/**
 * It is a class for small graphs. The vertex id of vertices may not be sequential numbers.  
 * @author Arash Fard, Satya 
 *
 */
public class SmallGraph {
	public Map<Integer, Set<Integer>> vertices = null; 		// adjacency list of the graph (it must be populated in a 
															// 	valid graph) 
	public Map<Integer, Integer> labels = null; 			// label map of vertices (this map must contain the labels
															// 	for all vertices in the graph)
	public Map<Integer, Set<Integer>> parentIndex = null;  	// adjacency list of reversed graph (its population should be 
															// 	checked before usage)
	
	private Map<Integer, Set<Integer>> labelIndex = null;   // a map from given label to the set of vertices with this label
    public Map<Integer, Integer> eccentricity = null;    	// eccentricity of the vertices
    // Auxiliary variables
    Queue<Integer> qu = new LinkedList<Integer> (); // a queue supporting BFS
    Map<Integer, Boolean> visit  = null;               // vertex visitation flag
    Map<Integer, Integer> len = null;		        // path-length from vertex i to j
    private int len_max = 0;								// the maximum length from vertex i to any other vertex
    
    /**
     * Constructor
     */
    public SmallGraph() {
    	// It is used when the number of vertices is not known in advance
    }
    
    /**
     * Constructor
     * @param nVertices The number of vertices in the graph
     */
    public SmallGraph(int nVertices) {
    	vertices = new HashMap<Integer, Set<Integer>>(nVertices);
    	labels = new HashMap<Integer, Integer>(nVertices);    	
    }

	/*************************************************************
	 * Auxiliary constructor to read the small graph from a file
	 * @param filePath The path to read the file from
	 */
	public SmallGraph(String filePath) throws Exception {

		try {
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(filePath);

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;

			// first pass: get the vertex with the maximum value
			int max = -1;

			while ((strLine = br.readLine()) != null) {

				String[] splits = strLine.split("\\s+");
				int val = Integer.parseInt((splits[0]));
				if (val < 0) {
					throw new Exception("vertex id must be an integer bigger than 0");
				}
				if (val > max) {
					max = val;
				}
			} // while

			// Close the input stream
			br.close();
			in.close();

			// initialize the main array (the vertex id starts from 0)   
			this.vertices = new HashMap<Integer, Set<Integer>>(++max);
			this.labels = new HashMap<Integer, Integer>(max);

			fstream = new FileInputStream(filePath);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			//Read File Line By Line
			while ((strLine = br.readLine()) != null) { // each line belongs to one vertex

				String[] splits = strLine.split("\\s+");
				int index = Integer.parseInt(splits[0]); // the first integer is the id of the vertex
				this.vertices.put(index, new HashSet<Integer>(splits.length - 2));

				this.labels.put(index, Integer.parseInt(splits[1])); // the label of the vertex
				
				for (int i = 2; i < splits.length; i++) { // the id of the children of the vertex
					this.vertices.get(index).add(Integer.parseInt(splits[i]));
				}
			} //while

			//Close the input stream
			br.close();
			in.close();
		} // try
		catch (Exception e) {//Catch exception if any
			throw new Exception(e);
		} //catch
	}

	/**
	 * Builds Pattern Index for the SmallGraph
	 */
	public void buildParentIndex() {
		if (parentIndex == null) {
			parentIndex = new HashMap<Integer, Set<Integer>>(labels.size());

			for(int id : labels.keySet()) {
				if(vertices.get(id) != null) {
					for(int child : vertices.get(id)) {
						if(parentIndex.get(child) == null)
							parentIndex.put(child, new HashSet<Integer>());
						parentIndex.get(child).add(id);
					} // for
				}
			} // for
		} // if
	}

	/********************************************************************************
	 * Get the Graph Signature, which is a Set of pairs of vertex labels of the edges. 
	 * @return A Set of Pairs of Integers where each pair constitute the vertex labels of an edge in the graph.
	 */
	public Set<Pair<Integer,Integer>> getSignature(){
		Set<Pair<Integer,Integer>> sig = new HashSet<Pair<Integer,Integer>>();

		for(int id : labels.keySet()) {
			if(vertices.get(id) != null) {
				for(int child : vertices.get(id)) {
					Pair<Integer,Integer> p = new Pair<Integer,Integer>(labels.get(id), labels.get(child));
					sig.add(p);
				}
			} //if
		}	
		return sig;		
	}

	/*************************************************************
	 * Gets the number of vertices in the graph
	 * @return int The number of vertices in the graph. This value would equal the highest vertex id in the graph
	 */
	public int getNumVertices() {
		return this.labels.size();
	}
	
    /**
     * Calculates the eccentricity of all vertices
     * assuming the graph is connected
     */
    private void calcEcc() {    	
    	int nVertices = this.getNumVertices();
        eccentricity = new HashMap<Integer, Integer>(nVertices);
        visit = new HashMap<Integer, Boolean>(nVertices);
        len = new HashMap<Integer, Integer>(nVertices);
        
        this.buildParentIndex();
        
        for(int i : labels.keySet()) {	// finding ecc of vertex i
        	// initializing visit and len and Len_max
        	len_max = 0;
            for(int j : labels.keySet()) { 
                visit.put(j, false);
                len.put(j, 0);
            }

            qu.clear();	// clearing the queue
            qu.add(i); // putting vertex i in the queue
            visit.put(i, true); // mark as visited
            while (! qu.isEmpty()) visit(); // visit vertices in BFS order
            int max = len_max - 1;
            for(int l : len.keySet()) {
            	int length = len.get(l);
            	if( length > max)
            		max = length;
            }
            eccentricity.put(i, max);
        } // for
    } // calcEcc

    /****************************************************************************
     * Visit the next vertex (at the head of queue 'qu'), mark it, compute the
     *  path-length 'len' for each of its children and put them in the queue. 
     */
    private void visit() {    	
        int j = qu.poll(); 							// take next vertex from queue
        len_max = len.get(j) + 1;    			  	// path-length to child vertices
        // the underlying undirected graph should be used
        if(vertices.get(j) != null) {
        	for (int c : vertices.get(j)) {         // for each child of vertex j
        		if (! visit.get(c)) {
        			len.put(c, len_max);            // distance from vertex i to c                
        			qu.add(c);                      // put child c in queue
        			visit.put(c, true);				// mark as visited
        		} // if
        	} // for
        } //if
        if(parentIndex.get(j) != null) {
        	for (int p : parentIndex.get(j)) {      // for each parent of vertex j
        		if (! visit.get(p)) {
        			len.put(p, len_max);            // distance from vertex i to c                
        			qu.add(p);                      // put child c in queue
        			visit.put(p, true);				// mark as visited
        		} // if
        	} // for
        } //if
    } // visit

    /**
     * returns the eccentricity of a vertex
     * @param id the id of the vertex
     * @return the eccentricity of the vertex
     */
    public int eccentricity(int id) throws Exception {
    	if (eccentricity == null)
    		calcEcc();
    	int ecc = -1;
        try {
        	ecc = eccentricity.get(id);
        } catch (Exception ex) {
        	throw new Exception("the id is not valid");
        }
        return ecc;
    } // eccentricity

    /*********************************************************************************** 
     * Compute the diameter of the graph (longest shortest path and maximum eccentricity).
     * @return int the diameter of the graph
     */
    public int getDiameter() {
    	if (eccentricity == null)
    		calcEcc();
        int maxEcc = 0;
        for (int ecc : eccentricity.values()) {
            if (ecc > maxEcc) 
                maxEcc = ecc;
        } // for
        return maxEcc;
    } // diam

    /************************************************************************************ 
     * Compute the radius of the graph (minimum eccentricity).
     * @return int the radius of the graph
     */
    public int getRadius() {
    	if (eccentricity == null)
    		calcEcc();
        int minEcc = len_max; // It is eccentricity of a vertex
        for (int ecc : eccentricity.values()) {
            if (ecc < minEcc) 
            	minEcc = ecc;
        } // for
        return minEcc;
    } // rad
    
    /*************************************************************************************
     * Return the central vertices, those with eccentricities equal to the radius.
     * @return Set<Integer> the set of center vertices
     */
    public Set<Integer> getCenters() {
        Set<Integer> centers = new HashSet<Integer>();
        int radius = getRadius();
        
        for (int id : eccentricity.keySet()) {
        	if (eccentricity.get(id) == radius)
        		centers.add(id);
        }
        return centers;
    } // getCenters
    
    /*************************************************************************************
     * Returns a selected center
     * @return the selected center
     */
    public int getSelectedCenter() {
        Set<Integer> centers = getCenters();
        return selectivityCriteria(centers);
    } // getSelectedCenter

	/********************************************************************************
	 *  Return the vertex from a set of central vertices, those which have 
	 *  highest number of neighbors and lowest frequency of label in the query graph;
	 *  i.e, the highest ratio.
	 *  @param Set<Integer> the set of centers from the query Graph
	 *  @return int a single vertex which satisfies the condition
	 */
	public int selectivityCriteria(Set<Integer> Centers){
		Double ratio = 0.0;
		int index = 0;
		Double max = -1.0; // all the centers have ratio bigger than this
		buildParentIndex();
		buildLabelIndex();
		for(int cen: Centers){
			int neighbors = 0;
			if(vertices.get(cen) != null)	neighbors += vertices.get(cen).size();
			if(parentIndex.get(cen) != null)	neighbors += parentIndex.get(cen).size();
			ratio = (double) (neighbors) / (double)(labelIndex.get(labels.get(cen)).size());
			if(max < ratio) {
				max = ratio;
				index = cen;
			}
		}
		return index;
	}

	/*************************************************************
	 * Builds a HashMap storing the values from the labels to the ids of the vertices
	 * stores the resulted HashMap in labelIndex field variable
	 */
	public void buildLabelIndex() {
		if (labelIndex == null) {
			labelIndex = new HashMap<Integer, Set<Integer>>();
			for (int id : labels.keySet()) {
				int l = labels.get(id); // the label of vertex id
				if (labelIndex.get(l) == null) {
					Set<Integer> vSet = new HashSet<Integer>();
					vSet.add(id);
					labelIndex.put(l, vSet);
				} else {
					labelIndex.get(l).add(id);
				}
			}
		}
	}

	/****************************************************
	 * Returns the children of a particular vertex
	 * @param  The id of the vertex.
	 * @return A Set of Integers which are the children vertices of the given id. 
	 */	
	public Set<Integer> post(int id){
		return vertices.get(id);
	}

	/****************************************************
	 * Returns the parents of a particular vertex in the ball.
	 * @param  The id of the vertex.
	 * @return A Set of Integers which are the parent vertices of the given id. 
	 */	
	public Set<Integer> pre(int id){
		if(parentIndex == null)
			buildParentIndex();
		return parentIndex.get(id);		
	}	

	/*************************************************************
	 * Gets the label of the vertex
	 * @param id Id of the vertex
	 * @return int Label of the vertex, -1 if not present
	 */
	public int getLabel(int id) {
		return this.labels.get(id);
	}

	/*************************************************************
	 * Gets the set of vertices which have the same given label
	 * @return A HashMap where K is the vertex id and V is the Label of the vertex
	 */
	public Set<Integer> getVerticesLabeled(int label) {
		if (labelIndex == null)
			buildLabelIndex();

		return this.labelIndex.get(label);
	}

	/********************************************************************************
	 * Tests if the graph is connected and polytree
	 * @return -1 when it is not connected, 0 when it is connected but not polytree, 1 when it is a polytree
	 */
	public int isPolytree() {
		int nVertices = this.getNumVertices();
		buildParentIndex();
		boolean cyclic = false;

		// keeps track of visited vertices. 'w' not observed, 'g' observed, 'b' traversed
		Map<Integer, Character> visited = new HashMap<Integer, Character>(nVertices);
		int nTraversed = 0; // number of traversed vertices
		// initializing the visited map
		int center = 0;
		for(int id : labels.keySet()){
			visited.put(id, 'w');
			center = id;
		}
		
		// ***** This is BFS traversal on undirected ********
		qu.clear(); 

		qu.add(center);
		visited.put(center, 'g');
		
		while(!qu.isEmpty()){
			int node = qu.poll();
			if(vertices.get(node) != null) {
				for(int child : vertices.get(node)){
					char childColor = visited.get(child);
					if(childColor == 'w'){
						visited.put(child, 'g');
						qu.add(child);
					} else if(childColor == 'g') {
						cyclic = true;
					}
				} //for
			} //if
			if(parentIndex.get(node) != null) {
				for(int parent : parentIndex.get(node)){
					int parentColor = visited.get(parent);
					if(parentColor == 'w'){
						visited.put(parent, 'g');
						qu.add(parent);
					} else if (parentColor == 'g') {
						cyclic = true;
					}
				} //for
			} //if
			visited.put(node, 'b');
			nTraversed ++;
		} //while

		if(nTraversed < nVertices) return -1;
		else if (cyclic) return 0;
		else return 1;
	} // getPolytree

	/*************************************************************
	 * Dumps the graph on console. Useful for debugging
	 */
	public void display() {
		System.out.println("***************");
		for (int i : labels.keySet()) {
			System.out.print(i + " (");
			System.out.print(labels.get(i) + ") ");
			if (vertices.get(i) != null) {
				System.out.print(vertices.get(i));
			}
			System.out.println();
		}
	}

	/********************************************************************************
	 * Method to Print a Graph
	 */
	public String toString(){
		StringBuilder result = new StringBuilder();
		List<Integer> uList = new ArrayList<Integer>(labels.keySet());
		Collections.sort(uList);
		for(int u : uList) {
			result.append(u); // the vertex id
			result.append(" " + labels.get(u)); // the vertex label
			if(vertices.containsKey(u)) {	// the vertex neighbors
				List<Integer> vList = new ArrayList<Integer>(vertices.get(u));
				Collections.sort(vList);
				for(int v : vList) { 
					result.append(" " + v);
				} //for
			} //if
			result.append("\n");
		} //for
		
		return result.toString();
	}
	
	/**
	 * Test main method
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		SmallGraph q = new SmallGraph("exampleGraphs/40_1.2a_query.txt");
		System.out.println("The center: " + q.getSelectedCenter());
		int queryStatus = q.isPolytree();
		switch (queryStatus) {
			case -1: System.out.println("The query Graph is disconnected");
					 System.exit(-1);
					 break;
			case  0: System.out.println("The query Graph is connected but not a polytree");
					 break;
			case  1: System.out.println("The query Graph is a polytree");
					 break;
			default: System.out.println("Undefined status of the query graph");
			 		 System.exit(-1);
			 		 break;
		}
		
		System.out.println(q);

	} //main
	
} // class
