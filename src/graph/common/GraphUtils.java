package graph.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

/** This class holds together a bunch of static helper methods 
* @author Arash Fard, Satya
*/

public class GraphUtils {

	/********************************************************************************
	 * Method to obtain a INDUCED SUBGRAPH from the dataGraph.
	 * @param Set of Vertices which are the candidate vertices.
	 * @return A Graph object which is the SugGraph induced by the input vertices.	 
	 */
	public static SmallGraph inducedSubgraph(Graph mainGraph, Set<Integer> setOfVertices){
		SmallGraph subGraph = new SmallGraph(setOfVertices.size());

		for(int id : setOfVertices) {
			Set<Integer> neighbors = new HashSet<Integer>(mainGraph.post(id));
			neighbors.retainAll(setOfVertices);
			subGraph.vertices.put(id, neighbors);
			subGraph.labels.put(id, mainGraph.getLabel(id));
		}
		
		return subGraph;
	}

	/********************************************************************************
	 * Method to obtain a INDUCED SUBGRAPH from the a small Graph.
	 * @param Set of Vertices which are the candidate vertices.
	 * @return A Graph object which is the SugGraph induced by the input vertices.	 
	 */
	public static SmallGraph inducedSubgraph(SmallGraph mainGraph, Set<Integer> setOfVertices){
		SmallGraph subGraph = new SmallGraph(setOfVertices.size());

		for(int id : setOfVertices) {
			Set<Integer> neighbors = new HashSet<Integer>(mainGraph.post(id));
			neighbors.retainAll(setOfVertices);
			subGraph.vertices.put(id, neighbors);
			subGraph.labels.put(id, mainGraph.getLabel(id));
		}
		
		return subGraph;
	}

	/********************************************************************************
	 * Generates a polytree for a SmallGraph
	 * @param The original graph from which the Polytree is to be found.
	 * @param center - The center from where the BFS traversal starts.
	 * @return The Graph Object which is a Polytree extracted from the original graph.
	 */
	public static SmallGraph getPolytree(SmallGraph g, int center) {
		int nVertices = g.getNumVertices();
		g.buildParentIndex();

		// initializing the polyTree 
		SmallGraph polyTree = new SmallGraph();
		polyTree.vertices = new HashMap<Integer, Set<Integer>>(nVertices);
		polyTree.labels = new HashMap<Integer, Integer>(g.labels);
		
		// keeps track of visited vertices
		Map<Integer, Boolean> visited = new HashMap<Integer, Boolean>(nVertices);
		// initializing the visited map
		for(int id : g.labels.keySet()){
			visited.put(id, false);
			polyTree.vertices.put(id, new HashSet<Integer>());
		}
		
		// ***** This is BFS traversal on undirected ********
		Queue<Integer> q = new LinkedList<Integer>();

		q.add(center);
		visited.put(center, true);
		while(!q.isEmpty()){
			int node = q.poll();
			if(g.vertices.get(node) != null) {
				for(int child : g.vertices.get(node)){
					if(!visited.get(child)){
						visited.put(child, true);
						q.add(child);
						polyTree.vertices.get(node).add(child);
					} //if
				} //for
			} //if
			if(g.parentIndex.get(node) != null) {
				for(int parent : g.parentIndex.get(node)){
					if(!visited.get(parent)){
						visited.put(parent, true);
						q.add(parent);
						polyTree.vertices.get(parent).add(node);
					} //if
				} //for
			} //if
		} //while

		return polyTree; 	
	} // getPolytree

	/**
	 * Finding a subset of 'n' vertices of graph in the neighborhood of a given 'center' vertex.
	 * The graph is traversed from the center in a BFS fashion. However, the number of children of each vertex
	 * will be a random number smaller than the specified degree. When degree=0, it would be a normal BFS.
	 * @param g			the data graph 
	 * @param center 	the specified center
	 * @param degree 	the specified degree
	 * @param n			the number vertices in the return subset 	
	 * @return			the found subgraph
	 */
	public static SmallGraph subGraphBFS(Graph g, int center, int degree, int n) throws Exception {
		if(degree < 0) {
			throw new Exception("degree cannot be negative");
		}
		g.buildParentIndex();
		SmallGraph subgraph = new SmallGraph(n);
		subgraph.parentIndex = new HashMap<Integer, Set<Integer>>();
		Queue<Integer> qu = new LinkedList<Integer> (); // a queue supporting BFS
		Random rand = new Random();
		
		// ***** This is BFS traversal on undirected ********
		qu.add(center);
		
		while(!qu.isEmpty() && (subgraph.labels.size() < n)){
			// adding the new node to subgraph
			int node = qu.poll();
			if(! subgraph.vertices.containsKey(node)) {
				subgraph.vertices.put(node, new HashSet<Integer>());
				subgraph.labels.put(node, g.getLabel(node));
			}
			if(subgraph.labels.size() >= n) break;
			// observing children 
			if(g.post(node) != null) {
				int rn = rand.nextInt(degree + 1); // number of children will be limited to a random number 
				int d = 0;
				for(int child : g.post(node)){
					if(++d > rn && degree != 0) break;
					subgraph.vertices.get(node).add(child); // adding the edge (it might be repeated)
					if(! subgraph.vertices.containsKey(child)){
						subgraph.labels.put(child, g.getLabel(child));
						qu.add(child);
					}
					if(subgraph.labels.size() >= n) break;
				} //for
			} //if
			if(subgraph.labels.size() >= n) break;
			// observing parents
			if(g.pre(node) != null) {
				int rn = rand.nextInt(degree + 1); // number of children will be limited to a random number 
				int d = 0;
				for(int parent : g.pre(node)){
					if(++d > rn && degree != 0) break;
					if(! subgraph.vertices.containsKey(parent)) {
						subgraph.vertices.put(parent, new HashSet<Integer>());
						subgraph.labels.put(parent, g.getLabel(parent));
						qu.add(parent);
					}
					subgraph.vertices.get(parent).add(node);
					
					if(subgraph.labels.size() >= n) break;
				} //for
			} //if
		} //while
		
		return subgraph;
	} //subSetBFS
	
	/**
	 * Creates the reverse graph of the given graph and stores it in the file
	 * @param g			the input graph
	 * @param fileName	the name of output file for storing the reverse graph
	 * @throws Exception
	 */
	public static void storeInverseGraph(Graph g, String fileName) throws Exception {
		System.out.println("Making the reverse graph");
		g.label = null; // release memory
		g.buildParentIndex();
		
		System.out.println("Writing the reverse graph");		
		File file = new File(fileName);
		// if file does not exists, then create it
		if (!file.exists()) file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());			
		BufferedWriter bw = new BufferedWriter(fw);
		
		StringBuilder reverseGraph = new StringBuilder();
		int nVertices = g.getNumVertices();
		g.adj = null; // release memory
		for(int u=0; u < nVertices; u++) {
			reverseGraph.append(u);		// the id of the vertex
//			reverseGraph.append(" " + g.getLabel(u)); // the label of the vertex
			reverseGraph.append(" 0");
			for(int v : g.pre(u)) {		// the parents of the vertex now becomes its children
				reverseGraph.append(" " + v);
			} //for
			reverseGraph.append("\n");
			bw.write(reverseGraph.toString());				// write this line to buffer writer
			reverseGraph.delete(0, reverseGraph.length());	// delete the contents of the StringBuilder
		} //for
		
		bw.close();		
	}
	
	/**
	 * Test main method
	 * @param args
	 */
	public static void main(String[] args) { // test code
        SmallGraph graph = new SmallGraph(6);
        graph.vertices.put(1, new HashSet<Integer>());
        graph.vertices.get(1).add(2);
        graph.vertices.get(1).add(5);
        graph.vertices.get(1).add(10);
        graph.vertices.put(2, new HashSet<Integer>());
        graph.vertices.get(2).add(21);
        graph.vertices.put(21, new HashSet<Integer>());
        graph.vertices.get(21).add(1);
        graph.vertices.get(21).add(30);
        graph.vertices.put(5, new HashSet<Integer>());
        graph.vertices.get(5).add(1);
        graph.vertices.put(10, new HashSet<Integer>());
        graph.vertices.get(10).add(5);
        //graph.vertices.put(30, new HashSet<Integer>());

        graph.labels.put(1, 0);
        graph.labels.put(2, 1);
        graph.labels.put(21, 2);
        graph.labels.put(10, 3);
        graph.labels.put(30, 3);
        graph.labels.put(5, 2);

        // diameter = 3, radius = 2, center = {1}
        System.out.println("***************");
        System.out.println("Radius: " + graph.getRadius());
        System.out.println("Diameter: " + graph.getDiameter());
        System.out.println(graph);
        System.out.println("The adjacency List:");
        graph.display();
        
        // polyTree
        System.out.println();
        SmallGraph pt1 = getPolytree(graph, 1);
        System.out.println("The polytree from center 1:");
        pt1.display();
        System.out.println();
        SmallGraph pt2 = getPolytree(graph, 5);
        System.out.println("The polytree from center 5:");
        pt2.display();
        
		int queryStatus = pt2.isPolytree();
		switch (queryStatus) {
			case -1: System.out.println("It is disconnected");
					 System.exit(-1);
					 break;
			case  0: System.out.println("It is connected but not a polytree");
					 break;
			case  1: System.out.println("It is a polytree");
					 break;
			default: System.out.println("Undefined status of the graph");
			 		 System.exit(-1);
			 		 break;
		}

    } // main
	
}
