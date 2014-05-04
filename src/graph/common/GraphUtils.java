package graph.common;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
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
    } // main
	
}
