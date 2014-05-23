
/**************************************************************************
 * @author Satya,Arash
 *
 */
package graph.simulation;

import graph.common.Graph;
import graph.common.SmallGraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class DualSimulation{

	/*****************************************************************
	 * Runs the sequential dual simulation when the data graph is of type Graph
	 * @param graph The Data Graph 
	 * @param query The Query Graph  
	 * @return The Dual simulation map
	 */
	public static Map<Integer, Set<Integer>>  getDualSimSet(Graph dataGraph, SmallGraph query) {
		dataGraph.buildLabelIndex();
		// matching map from query to dataGraph
		Map<Integer, Set<Integer>> sim = new HashMap<Integer, Set<Integer>>(query.getNumVertices());
		
		// relating the vertices of dataGraph to the vertices of query based on label match
		for(int u : query.labels.keySet()) {
			int label = query.labels.get(u);
			Set<Integer> phi = new HashSet<Integer>(dataGraph.getVerticesLabeled(label));
			sim.put(u, phi);			
		} //for
		
		// sim will be refined based on the dualSim condition
		boolean alter = true;
		while (alter) {
            alter = false;

            // loop over query vertices u and u's children u_c
            for(int u : query.labels.keySet()) {
            	if(query.post(u) != null) {
            		for(int u_c : query.post(u)) {
            			Set<Integer> newPhi = new HashSet<Integer>();	// subset of phi(u_c) having a parent in phi(u)
                		Iterator<Integer> it = sim.get(u).iterator();
            			while(it.hasNext()) {
            				int v = it.next();							// data vertex v is in phi(u)
            				Set<Integer> phiTemp = new HashSet<Integer>(dataGraph.post(v));
            				phiTemp.retainAll(sim.get(u_c));			// children of v contained in phi(u_c)
            				if(phiTemp.isEmpty()) {
            					it.remove();
            					sim.get(u).remove(v);					// remove vertex v from phi(u) 
            					if(sim.get(u).isEmpty())				// no match for vertex u => no overall match
            						return new HashMap<Integer, Set<Integer>>();
            					alter = true;
            				} //if
            				// build newPhi to contain only those vertices in phi(u_c) which also have a parent in phi(u)
            				newPhi.addAll(phiTemp);
            			} //while
            			
            			if (newPhi.size() < sim.get(u_c).size()) alter = true;        // since newPhi is smaller than phi(u_c)           			
            			sim.put(u_c, newPhi); // newPhi is the refined set of the previous phi(u_c)
            		} //for
            	} //if
            } //for
            
        } // while
		
		return sim;
	} // getDualSimSet

	/*****************************************************************
	 * Runs the sequential dual simulation when the data graph is of type SmallGraph
	 * @param graph The Data Graph 
	 * @param query The Query Graph  
	 * @return The Dual simulation map
	 */
	public static Map<Integer, Set<Integer>>  getDualSimSet(SmallGraph dataGraph, SmallGraph query) {
		dataGraph.buildLabelIndex();
		// matching map from query to dataGraph
		Map<Integer, Set<Integer>> sim = new HashMap<Integer, Set<Integer>>(query.getNumVertices());
		
		// relating the vertices of dataGraph to the vertices of query based on label match
		for(int u : query.labels.keySet()) {
			int label = query.labels.get(u);
			// a copy of the vertices with the same label
			Set<Integer> phi = new HashSet<Integer>(dataGraph.getVerticesLabeled(label));
			if(phi.isEmpty())
				return new HashMap<Integer, Set<Integer>>(); // a vertex without any candidate match
			sim.put(u, phi);			
		} //for
		
        sim = dualSimSetHelper(dataGraph, query, sim);
		
		return sim;
	} // getDualSimSet

	/*****************************************************************
	 * Runs the sequential dual simulation when the data graph is of type SmallGraph and an initial relation match is known 
	 * @param graph The Data Graph 
	 * @param query The Query Graph
	 * @param relation an initial relation match (will be altered and returned)
	 * @return The refined relation match based on dual simulation
	 */
	public static Map<Integer, Set<Integer>>  dualSimSetHelper(SmallGraph dataGraph, SmallGraph query, Map<Integer, Set<Integer>> relation) {
		// relation will be refined based on the dualSim condition
		boolean alter = true;
		while (alter) {
			alter = false;

			// loop over query vertices u and u's children u_c
			for(int u : query.labels.keySet()) {
				if(query.post(u) != null) {
					for(int u_c : query.post(u)) {
						Set<Integer> newPhi = new HashSet<Integer>();	// subset of phi(u_c) having a parent in phi(u)
						Iterator<Integer> it = relation.get(u).iterator();
						while(it.hasNext()) {
							int v = it.next();							// data vertex v is in phi(u)
							Set<Integer> phiTemp = new HashSet<Integer>(dataGraph.post(v));
							phiTemp.retainAll(relation.get(u_c));			// children of v contained in phi(u_c)
							if(phiTemp.isEmpty()) {
								it.remove();
								relation.get(u).remove(v);					// remove vertex v from phi(u) 
								if(relation.get(u).isEmpty())				// no match for vertex u => no overall match
									return new HashMap<Integer, Set<Integer>>();
								alter = true;
							} //if
							// build newPhi to contain only those vertices in phi(u_c) which also have a parent in phi(u)
							newPhi.addAll(phiTemp);
						} //while

						if (newPhi.size() < relation.get(u_c).size()) alter = true;        // since newPhi is smaller than phi(u_c)           			
						relation.put(u_c, newPhi); // newPhi is the refined set of the previous phi(u_c)
					} //for
				} //if
			} //for
		} // while
		return relation;
	} //dualSimSetHelper
	
	/**
	 * Returns the vertices of a data graph available in a Simulation Set 
	 * @param simSet the input Simulation Set
	 * @return the set of vertices in the data graph side of simulation 
	 */
	public static Set<Integer> nodesInSimSet(Map<Integer, Set<Integer>> simSet) {
		Set<Integer> theNodes = new HashSet<Integer>();
		for(int u : simSet.keySet()) {
			theNodes.addAll(simSet.get(u));
		}
		return theNodes;
	} // nodesInSimSet
	
	/**
	 * Finds the result match graph of dual simulation when the data graph is of type Graph
	 * @param dataGraph
	 * @param query
	 * @param dualSimSet
	 * @return result match graph
	 */
	public static SmallGraph getResultMatchGraph(Graph dataGraph, SmallGraph query, Map<Integer, Set<Integer>> dualSimSet) {

		SmallGraph resultMatch = new SmallGraph();
		resultMatch.vertices = new HashMap<Integer, Set<Integer>>();
		resultMatch.labels = new HashMap<Integer, Integer>();    	
		if(dualSimSet.isEmpty()) return resultMatch;

		for(int u : query.labels.keySet()) {
			for(int v : dualSimSet.get(u)) {
				if(resultMatch.post(v) == null)
					resultMatch.vertices.put(v, new HashSet<Integer>());
				if(resultMatch.labels.get(v) == null)
					resultMatch.labels.put(v, query.labels.get(u));
			} // for
		} // for
		
		// only those edges of the data graph which are involved in the sim set should be added to the result match graph
		for(int u : query.labels.keySet()) {
			if(query.post(u) != null) {
				for(int uc : query.post(u)) {
					for(int v : dualSimSet.get(u)) {
						Set<Integer> newAdjSet = new HashSet<Integer>(dualSimSet.get(uc));
						newAdjSet.retainAll(dataGraph.post(v));
						resultMatch.post(v).addAll(newAdjSet);
					} // for
				} // if
			} // for
		} // for
		
		return resultMatch;
	} // getDualSimMatchGraph
	
	/**
	 * Finds the result match graph of dual simulation when the data graph is of type SmallGraph
	 * @param dataGraph
	 * @param query
	 * @param dualSimSet
	 * @return result match graph
	 */
	public static SmallGraph getResultMatchGraph(SmallGraph dataGraph, SmallGraph query, Map<Integer, Set<Integer>> dualSimSet) {

		SmallGraph resultMatch = new SmallGraph();
		resultMatch.vertices = new HashMap<Integer, Set<Integer>>();
		resultMatch.labels = new HashMap<Integer, Integer>();    	
		if(dualSimSet.isEmpty()) return resultMatch;

		for(int u : query.labels.keySet()) {
			for(int v : dualSimSet.get(u)) {
				if(resultMatch.post(v) == null)
					resultMatch.vertices.put(v, new HashSet<Integer>());
				if(resultMatch.labels.get(v) == null)
					resultMatch.labels.put(v, query.labels.get(u));
			} // for
		} // for
		
		// only those edges of the data graph which are involved in the sim set should be added to the result match graph
		for(int u : query.labels.keySet()) {
			if(query.post(u) != null) {
				for(int uc : query.post(u)) {
					for(int v : dualSimSet.get(u)) {
						Set<Integer> newAdjSet = new HashSet<Integer>(dualSimSet.get(uc));
						newAdjSet.retainAll(dataGraph.post(v));
						resultMatch.post(v).addAll(newAdjSet);
					} // for
				} // if
			} // for
		} // for
		
		return resultMatch;
	} // getDualSimMatchGraph
	
	public static void main(String[] args) throws Exception {
		Graph g = new Graph("exampleGraphs/G_tight1.txt");
		SmallGraph q = new SmallGraph("exampleGraphs/Q_tight1.txt");
		
		Map<Integer, Set<Integer>> dualSim = getDualSimSet(g, q);
		
		System.out.println("Dual Sim Relation:");
		System.out.println(dualSim);
		System.out.println("Vertices in Dual:");
		System.out.println(nodesInSimSet(dualSim));
		System.out.println("Result Match Graph:");
		SmallGraph resultGraph = getResultMatchGraph(g, q, dualSim);
		resultGraph.display();
	} //main
	
} // class
