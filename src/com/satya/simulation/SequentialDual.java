/**
 * 
 */
/**
 * @author Satya
 *
 */
package com.satya.simulation;

//import gps.examples.gm.gpsPageRank.gpsPageRank.gpsPageRankVertex;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.satya.graph.common.Graph;

/*****************************************************************
 * Runs the sequential dual simulation on the ball
 * @param graph The graph that we want to run dual simulation on 
 * @param graphLabels The labels for the graph
 * @param query The query graph  
 * @return The sim set
 * 
 */

public class SequentialDual{


	/******************************************************
	 * Returns the graph where we change the direction of all the edges
	 * @param graph The graph that we want to change the edge directions of
	 * @return Map<Integer, Set<Integer>> The new graph with opposite directions
	 */
	private static Map<Integer, Set<Integer>> getPreGraph(Map<Integer, Set<Integer>> graph) {
		Map<Integer, Set<Integer>> pre = new HashMap<Integer, Set<Integer>> ();
		for (int key: graph.keySet()) {
			for (int out: graph.get(key)) {
				Set<Integer> val = null;
				if (pre.containsKey(out))
					val = pre.get(out);
				else
					val = new HashSet<Integer> ();
				val.add(key);
				pre.put(out, val);
			}
		}
		return pre;
	}
	/*****************************************************************
	 * Runs the sequential dual simulation on the ball
	 * @param graph The childIndex of the graph that we want to run dual simulation on 
	 * @param graphLabels The labels for the graph
	 * @param query The query graph  
	 * @return The sim set
	 */
	public static Map<Integer, Set<Integer>>  getDual(Map<Integer, Set<Integer>> graph, Map<Integer, Integer> graphLabels, Graph query) {

		Map<Integer, Set<Integer>> sim = new HashMap<Integer, Set<Integer>> ();

		// holds an index from the labels to the vertex id's in the data graph
		Map<Integer, Set<Integer>> labelMap = new HashMap<Integer, Set<Integer>> ();

		for (int key : graph.keySet() ) {
			Set<Integer> map = null;
			if (labelMap.containsKey(graphLabels.get(key))) 
				map = labelMap.get(graphLabels.get(key));
			else
				map = new HashSet<Integer> ();
			map.add(key);
			labelMap.put(graphLabels.get(key), map);
		}

		// compute the pre for the graph. i.e., a map from vertex id to a set of it's parent ids
		Map<Integer, Set<Integer>> preGraph = getPreGraph(graph);

		for (int i = 0 ; i < query.getNumVertices() ; i++ ) {
			Set<Integer> temp = labelMap.get(query.getLabel(i));
			if ( temp != null)
				sim.put(i, new HashSet<Integer> (temp)); 
			else
				sim.put(i, new HashSet<Integer> ());
		}


		boolean flag = true;
		int var=-1;

		// run the dual simulation algorithm
		while (flag) {
			flag = false;
			for ( int u = 0 ; u < query.getNumVertices() ; u++ ) {
				List<Integer> simArr = new ArrayList<Integer> (sim.get(u));
				for ( int w1 = 0 ; w1 < simArr.size(); w1++ ) {
					int w = simArr.get(w1);
					for ( int v: query.getNeighbors(u)) {
						Set<Integer> gpost = new HashSet<Integer> (graph.get(w));
						Set<Integer> sub = sim.get(v);
						if ( sub == null )
							sub = new HashSet<Integer> ();
						gpost.retainAll(sub);
						if (gpost.isEmpty()) {
							sim.get(u).remove(w);
							simArr.remove(w1);
							flag = true;
							w1--;
							break;
						}
					} // for v
				} // for w
			} // for u
			for ( int u = 0 ; u < query.getNumVertices() ; u++ ) {
				List<Integer> simArr = new ArrayList<Integer> (sim.get(u));
				for ( int w1 = 0 ; w1 < simArr.size() ; w1++ ) {
					int w = simArr.get(w1);
					for ( int v: query.getParentIndex().get(u)) {
						Set<Integer> gpre = null;
						if (!preGraph.containsKey(w))
							gpre = new HashSet<Integer> ();
						else
							gpre = new HashSet<Integer> (preGraph.get(w));

						Set<Integer> sub = sim.get(v);
						if ( sub == null )
							sub = new HashSet<Integer> ();
						gpre.retainAll(sub);
						if (gpre.isEmpty()) {
							sim.get(u).remove(w);
							simArr.remove(w1);
							flag = true;
							w1--;
							break;
						}
					} // for v
				} // for w
			} // for u
		}
		return sim;
	}	
		
		public static void main (String[] args) {
			
			
			Graph g = new Graph("/Users/Satya/Desktop/sample-data-graph-final.txt");
			Graph q = new Graph("/Users/Satya/Desktop/sample-query-graph-final.txt");
			g.getAllIds();
			q.getAllIds();
			SequentialDual sim = new SequentialDual();		
			/*
			 *  The parameters to the "getDual() method are  
			 *  1) the child index of the dataGraph
			 *  2) the label Map of the dataGraph
			 *  3) The Query graph. 
			 *   
			 */
			
			System.out.println("The DUAL SIM set is "+sim.getDual(g.getChildIndex(), g.getLabelMap(), q));
			
			
		}
	}
