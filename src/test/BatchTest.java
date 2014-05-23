/**
 * 
 * @author arash
 *
 */

package test;

import graph.common.*;
import graph.simulation.*;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.javatuples.Pair;

import cache.CacheUtils;

/**
 * I) Receives the dataGraph as its first argument and the path to warm-up queries as its second argument.
 * II) After warming up the cache waits for a path to the new queries in a while loop.
 * III) In each iteration answers the queries and when needed updates the cache.
 * IV) Exits when instead of path receives 'exit' string.
 */
public class BatchTest {
	public static void main(String[] args) throws Exception {
		if(args.length != 2) {
			System.out.println("Not correct number of inputs");
			System.exit(-1);
		}
		long startTime, stopTime, totalWarmupTime = 0, totalAnsweTime = 0;
		Map<SmallGraph, SmallGraph> cache = new HashMap<SmallGraph, SmallGraph>(); // the cache
		// the index on the ploytrees stored in the cache
		Map<Set<Pair<Integer,Integer>>, Set<SmallGraph>> cacheIndex = new HashMap<Set<Pair<Integer,Integer>>, Set<SmallGraph>>();
		
		// I)
		// The data graph is loaded from its file
		startTime = System.currentTimeMillis();
		Graph dataGraph = new Graph(args[0]);
		stopTime = System.currentTimeMillis();
		System.out.println("Spent time to load the data graph: " + (stopTime - startTime) + " ms");
		System.out.println("The number of vertices in the data graph: " + dataGraph.getNumVertices());
		
		// II)
		File dir = new File(args[1]);
		if (! dir.isDirectory()) {
			System.out.println("The second argument must be directory of queries");
			System.exit(-1);
		}
		File[] directoryListing = dir.listFiles(); // the list of query files
		if (directoryListing != null) {
			for (File queryFile : directoryListing) {
				System.out.println("Processing " + queryFile);
				SmallGraph queryGraph = new SmallGraph(queryFile.getAbsolutePath());

				int queryStatus = queryGraph.isPolytree();
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

				// The tight simulation is performed and its time, t_noCache, is measured
				startTime = System.currentTimeMillis();
				Set<Ball> tightResults = TightSimulation.getTightSimulation(dataGraph, queryGraph);
				tightResults = TightSimulation.filterMatchGraphs(tightResults);
				stopTime = System.currentTimeMillis();
				long t_noCache = stopTime - startTime;
				System.out.println("The total time of tight simulation without cache, 't_noCache': " + t_noCache + " ms");

				// The polytree of the queryGraph is created
				startTime = System.currentTimeMillis();
				int center = queryGraph.getSelectedCenter();
				SmallGraph polytree = GraphUtils.getPolytree(queryGraph, center);
				stopTime = System.currentTimeMillis();
				long t_polytree = stopTime - startTime;
//				System.out.println("Spent time to create the polytree: " + t_polytree + " ms");
//				System.out.println();

				// The dualSimSet of the polytree is found
				startTime = System.currentTimeMillis();
				Map<Integer, Set<Integer>> dualSim = DualSimulation.getDualSimSet(dataGraph, polytree);
				stopTime = System.currentTimeMillis();
				long t_dualSim = stopTime - startTime;
//				System.out.println("Spent time to find the dualSimSet of the polytree: " + t_dualSim + " ms");
//				System.out.println();

				// The induced subgraph of the dualSimSet is found
				startTime = System.currentTimeMillis();
				Set<Integer> dualSimSet = DualSimulation.nodesInSimSet(dualSim);
				SmallGraph inducedSubgraph = GraphUtils.inducedSubgraph(dataGraph, dualSimSet);
				stopTime = System.currentTimeMillis();
				long t_subgraph = stopTime - startTime;
				System.out.println("\nThe number of vertices in the dualSimSet of polytree: " + dualSimSet.size());
//				System.out.println("Spent time to find the induced subgraph of the dualSimSet: " + t_subgraph + " ms");
//				System.out.println();

				// The <polytree, inducedSubgraph> is stored in the cache
				startTime = System.currentTimeMillis();
				cache.put(polytree, inducedSubgraph);
				Set<Pair<Integer, Integer>> sig = polytree.getSignature(); 
				if (cacheIndex.get(sig) == null) {
					Set<SmallGraph> pltSet = new HashSet<SmallGraph>();
					pltSet.add(polytree);
					cacheIndex.put(sig, pltSet);
				} else
					cacheIndex.get(sig).add(polytree);
				
				stopTime = System.currentTimeMillis();
				long t_store = stopTime - startTime;
//				System.out.println("Spent time to store the <polytree, inducedSubgraph> in the cache: " + t_store + " ms");

				System.out.println();
				long t_cache = t_polytree + t_dualSim + t_subgraph + t_store;
				System.out.println("Time for warming up the cache with this query: " + t_cache + " ms");
				totalWarmupTime += t_cache;
				System.out.println("------------");
			} //for
			System.out.println("The total time of warm up: " + totalWarmupTime + " ms");
		} else {
			System.out.println("Could not find any query file in " + args[1]);
			System.exit(-1);
		} // if-else
		System.out.println("**********************************************************");
		
		// III)
		Scanner keyboard = new Scanner(System.in);
		String queryPath = "";
		
		while (true) {
			System.out.println("Enter the folder path of new queries (or 'exit'):");			
			queryPath = keyboard.nextLine();			
			dir = new File(queryPath);
			if(queryPath.equals("exit")) break;
			if (! dir.isDirectory()) {
				System.out.println("The entered path is not valid");
				continue;
			}
			directoryListing = dir.listFiles(); // the list of query files
			if (directoryListing != null) {
				for (File queryFile : directoryListing) {
					System.out.println("\nAnswering " + queryFile);
					SmallGraph queryGraph = new SmallGraph(queryFile.getAbsolutePath());
					// searching in the cache
					startTime = System.currentTimeMillis();
					boolean notInCache = true;
					Set<SmallGraph> candidateMatchSet = CacheUtils.getCandidateMatchSet(queryGraph, cacheIndex);

					if(candidateMatchSet != null) {
						for(SmallGraph candidate : candidateMatchSet) {
							if(CacheUtils.isDualCoverMatch(queryGraph, candidate)) {
								notInCache = false;
								System.out.println("Hit the cache!");
								long bTime = System.currentTimeMillis();
								SmallGraph inducedSubgraph = cache.get(candidate);
								Set<Ball> tightResults_cache = TightSimulation.getTightSimulation(inducedSubgraph, queryGraph);
								tightResults_cache = TightSimulation.filterMatchGraphs(tightResults_cache);
								long fTime = System.currentTimeMillis();
								System.out.println("The time for tight simulation from cache: " + (fTime - bTime) + " ms");
							} //if
						} //for
					} //if
					if(notInCache) {
						System.out.println("Not in the cache!");
						long bTime = System.currentTimeMillis();
						Set<Ball> tightResults_cache = TightSimulation.getTightSimulation(dataGraph, queryGraph);
						tightResults_cache = TightSimulation.filterMatchGraphs(tightResults_cache);
						long fTime = System.currentTimeMillis();
						System.out.println("The time for tight simulation without cache: " + (fTime - bTime) + " ms");
					} //if
					stopTime = System.currentTimeMillis();
					long tt_q = stopTime - startTime;
					totalAnsweTime += tt_q;
					System.out.println("Total time for answering this query: " + tt_q + " ms");
					System.out.println("--------------------------------------------");
				} //for
			} //if
			System.out.println("\nTotal time for answering all the queries: " + totalAnsweTime + " ms");
			System.out.println("**********************************************************");
			
		} //while
		
		keyboard.close();
	} // main
	
	
} // class
