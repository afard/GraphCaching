package test;

import graph.common.*;
import graph.simulation.*;

import java.util.Map;
import java.util.Set;

/*
 * In this test case, There are two input files: dataGraph and queryGraph.
 * 1) The graphs are read from the files
 * 2) The tight simulation is performed and its time, t_noCache, is measured
 * 3) The polytree of the queryGraph is created
 * 4) The dualSimSet of the polytree is found
 * 5) The induced subgraph of the dualSimSet is found
 * 6) The <polytree, inducedSubgraph> is stored in the cache
 * 7) The result of tight simulation for queryGraph is retrived from the cache and its time, t_withCache, is measured 
 */

public class TestCaseA {

	public static void main(String[] args) throws Exception {
		if(args.length != 2) {
			System.out.println("Not correct number of input files");
			System.exit(-1);
		}
		long startTime, stopTime;
		
		// 1) The graphs are read from the files
		startTime = System.currentTimeMillis();
		Graph dataGraph = new Graph(args[0]);
		stopTime = System.currentTimeMillis();
		System.out.println("Spent time to load the data graph: " + (stopTime - startTime) + " ms");
		startTime = System.currentTimeMillis();
		SmallGraph queryGraph = new SmallGraph(args[1]);
		stopTime = System.currentTimeMillis();
		System.out.println("Spent time to load the query graph: " + (stopTime - startTime) + " ms");

		System.out.println("The number of vertices in the data graph: " + dataGraph.getNumVertices());
		System.out.println("The number of vertices in the query graph: " + queryGraph.getNumVertices());
		System.out.println();
		
		// 2) The tight simulation is performed and its time, t_noCache, is measured
		startTime = System.currentTimeMillis();
		Set<Ball> tightResults = TightSimulation.getTightSimulation(dataGraph, queryGraph);
		stopTime = System.currentTimeMillis();
		long pre_t_noCache = stopTime - startTime;
		System.out.println("Spent time for tight simulation without post processing: " + pre_t_noCache + " ms");
		
		startTime = System.currentTimeMillis();
		tightResults = TightSimulation.filterMatchGraphs(tightResults);
		stopTime = System.currentTimeMillis();
		long post_t_noCache = stopTime - startTime;
		System.out.println("Spent time for post processing: " + post_t_noCache + " ms");
		long t_noCache = pre_t_noCache + post_t_noCache;
		System.out.println("The total time of tight simulation without cache, 't_noCache': " + t_noCache + " ms");
		
//		System.out.println("\nThe subgraph results of tight simulation:");
//		TightSimulation.printMatch(tightResults);
		System.out.println("The number of subgraph results: " + tightResults.size());
		int nVertices = 0;
		for(Ball b : tightResults)
			nVertices += b.nodesInBall.size();
		System.out.println("The total number of the vertices in all balls: " + nVertices);
		System.out.println();
		
		// 3) The polytree of the queryGraph is created
		startTime = System.currentTimeMillis();
		int center = queryGraph.getSelectedCenter();
		SmallGraph polytree = GraphUtils.getPolytree(queryGraph, center);
		stopTime = System.currentTimeMillis();
		long t_polytree = stopTime - startTime;
		System.out.println("Spent time to create the polytree: " + t_polytree + " ms");
		System.out.println();
		
		// 4) The dualSimSet of the polytree is found
		startTime = System.currentTimeMillis();
		Map<Integer, Set<Integer>> dualSim = DualSimulation.getDualSimSet(dataGraph, polytree);
		stopTime = System.currentTimeMillis();
		long t_dualSim = stopTime - startTime;
		System.out.println("Spent time to find the dualSimSet of the polytree: " + t_dualSim + " ms");
		System.out.println();
		
		// 5) The induced subgraph of the dualSimSet is found
		startTime = System.currentTimeMillis();
		Set<Integer> dualSimSet = DualSimulation.nodesInSimSet(dualSim);
		SmallGraph inducedSubgraph = GraphUtils.inducedSubgraph(dataGraph, dualSimSet);
		stopTime = System.currentTimeMillis();
		long t_subgraph = stopTime - startTime;
		System.out.println("The number of vertices in the dualSimSet of polytree: " + dualSimSet.size());
		System.out.println("Spent time to find the induced subgraph of the dualSimSet: " + t_subgraph + " ms");
		System.out.println();
		
		// 6) The <polytree, inducedSubgraph> is stored in the cache
		startTime = System.currentTimeMillis();
		System.out.println("-*** Skipping this step for now ***-");
		stopTime = System.currentTimeMillis();
		long t_store = stopTime - startTime;
		System.out.println("Spent time to store the <polytree, inducedSubgraph> in the cache: " + t_store + " ms");
		
		System.out.println();
		long t_cache = t_polytree + t_dualSim + t_subgraph + t_store;
		System.out.println("Total time for warming up the cache: " + t_cache + " ms");
		System.out.println();
		
		// 7) The result of tight simulation for queryGraph is retrieved from the cache and its time, t_withCache, is measured
		startTime = System.currentTimeMillis();
		Set<Ball> tightResults_cache = TightSimulation.getTightSimulation(inducedSubgraph, queryGraph);
		stopTime = System.currentTimeMillis();
		long pre_t_withCache = stopTime - startTime;
		System.out.println("Spent time for tight simulation from cache without post processing: " + pre_t_withCache + " ms");
		
		startTime = System.currentTimeMillis();
		tightResults_cache = TightSimulation.filterMatchGraphs(tightResults_cache);
		stopTime = System.currentTimeMillis();
		long post_t_withCache = stopTime - startTime;
		System.out.println("Spent time for post processing: " + post_t_withCache + " ms");
		long t_withCache = pre_t_withCache + post_t_withCache;
		System.out.println("The total time of tight simulation with cache, 't_withCache': " + t_withCache + " ms");
		
//		System.out.println("\nThe subgraph results of tight simulation retrieved from cache:");
//		TightSimulation.printMatch(tightResults_cache);
		System.out.println("The number of subgraph results: " + tightResults_cache.size());
		nVertices = 0;
		for(Ball b : tightResults_cache)
			nVertices += b.nodesInBall.size();
		System.out.println("The total number of the vertices in all balls: " + nVertices);
		System.out.println();
		
		System.out.println("************ The ratio ********************");
		System.out.println("t_noCache / t_withCache = " + t_noCache / t_withCache);

	} //testCaseA
	
} //class
