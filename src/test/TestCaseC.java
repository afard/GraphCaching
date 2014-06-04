package test;

import graph.common.Ball;
import graph.common.Graph;
import graph.common.GraphUtils;
import graph.common.SmallGraph;
import graph.simulation.DualSimulation;
import graph.simulation.TightSimulation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cache.CacheUtils;

public class TestCaseC {

	/*
	 * Running a set of queries against a data graph, and storing the performance results in file readable by Excel program
	 * args[0] is the dataGraph file
	 * args[1] is the path to the folder of queries
	 * args[2] is the output file
	 * args[3] a number to assign limit for the number of balls    
	 * args[4] the reverse data graph if it is available 
	 */
	public static void main(String[] args) throws Exception {
		if(args.length < 4) {
			System.out.println("Not correct number of input arguments");
			System.exit(-1);
		}
		long startTime, stopTime;
		int limit = Integer.parseInt(args[3]);
		
		startTime = System.nanoTime();
		Graph dataGraph = new Graph(args[0]);
		if(args.length == 5)	dataGraph.buildParentIndex(args[4]);
		stopTime = System.nanoTime();
		System.out.println("Spent time to load the data graph: " + (double)(stopTime - startTime)/1000000 + " ms");
		
		File file = new File(args[2]);
		// if file does not exists, then create it
		if (!file.exists()) file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());			
		BufferedWriter bw = new BufferedWriter(fw);	

		bw.write("queryFile\t querySize\t isPolytree\t t_dualSim\t nV\t nMC\t t_MG\t t_B\t t_noCache\t nSubgraphs\t nVertices\t t_polytree\t t_dualSim\t dualSimSet.size()\t "
				+ "t_subgraph\t t_withCache\t nVertices\t ratio1\t t_balls\t t_wcacheBalls\t nV\t ratio2\n");
		StringBuilder fileContents = new StringBuilder();

		File dirQ = new File(args[1]);
		if(!dirQ.isDirectory()) {
			bw.close();
			throw new Exception("The specified path of the queries is not a valid directory");
		}

		File[] queries = dirQ.listFiles(); // the list of query files	
		if(queries == null) {
			bw.close();
			throw new Exception("No query files found in the directory");
		}

		for(File query : queries) {
			System.out.println("Processing " + query.getAbsolutePath());
			System.out.println("_______________________________________________");
			
			// The queryGraph is read from file
			startTime = System.nanoTime();
			SmallGraph queryGraph = new SmallGraph(query.getAbsolutePath());
			stopTime = System.nanoTime();
			System.out.println("Spent time to load the query graph: " + (double)(stopTime - startTime)/1000000 + " ms");
			int querySize = queryGraph.getNumVertices();
			fileContents.append(query.getName() + "\t");
			fileContents.append(querySize + "\t");

			int queryStatus = queryGraph.isPolytree();
			switch (queryStatus) {
				case -1: System.out.println("The query Graph is disconnected");
					System.exit(-1);
					break;
				case  0: System.out.println("The query Graph is connected but not a polytree");
					fileContents.append("0\t");
					break;
				case  1: System.out.println("The query Graph is a polytree");
					fileContents.append("1\t");
					break;
				default: System.out.println("Undefined status of the query graph");
					System.exit(-1);
					break;
			}
			System.out.println();

			// The tight simulation is performed and its time, t_noCache, is measured			
			startTime = System.nanoTime();
			Set<Ball> tightResults = TightSimulation.getNewTightSimulationModified(dataGraph, queryGraph, limit, fileContents);
			tightResults = TightSimulation.filterMatchGraphs(tightResults);
			stopTime = System.nanoTime();
			long t_noCache = stopTime - startTime;
			System.out.println("The total time of tight simulation without cache, 't_noCache': " + (double)t_noCache/1000000 + " ms");
			fileContents.append((double)t_noCache/1000000 + "\t");

			System.out.println("The number of subgraph results: " + tightResults.size());
			fileContents.append(tightResults.size() + "\t");
			Set<Integer> vSet = new HashSet<Integer>();
			for(Ball b : tightResults)
				vSet.addAll(b.nodesInBall);
			System.out.println("The total number of the vertices in all balls: " + vSet.size());
			fileContents.append(vSet.size() + "\t");
			System.out.println();


			// The polytree of the queryGraph is created
			startTime = System.nanoTime();
			int center = queryGraph.getSelectedCenter();
			SmallGraph polytree = GraphUtils.getPolytree(queryGraph, center);
			stopTime = System.nanoTime();
			long t_polytree = stopTime - startTime;
			System.out.println("Spent time to create the polytree: " + (double)t_polytree/1000000 + " ms");
			fileContents.append((double)t_polytree/1000000 + "\t");
			System.out.println();

			// The dualSimSet of the polytree is found
			startTime = System.nanoTime();
			Map<Integer, Set<Integer>> dualSim = DualSimulation.getNewDualSimSet(dataGraph, polytree);
			stopTime = System.nanoTime();
			long t_dualSim = stopTime - startTime;
			System.out.println("Spent time to find the dualSimSet of the polytree: " + (double)t_dualSim/1000000 + " ms");
			fileContents.append((double)t_dualSim/1000000 + "\t");
			System.out.println();

			// The induced subgraph of the dualSimSet is found
			startTime = System.nanoTime();
			Set<Integer> dualSimSet = DualSimulation.nodesInSimSet(dualSim);
			SmallGraph inducedSubgraph = GraphUtils.inducedSubgraph(dataGraph, dualSimSet);
			stopTime = System.nanoTime();
			long t_subgraph = stopTime - startTime;
			System.out.println("The number of vertices in the dualSimSet of polytree: " + dualSimSet.size());
			fileContents.append(dualSimSet.size() + "\t");
			System.out.println("Spent time to find the induced subgraph of the dualSimSet: " + (double)t_subgraph/1000000 + " ms");
			fileContents.append((double)t_subgraph/1000000 + "\t");
			System.out.println();


			// (1) ******************************************************************************************
			// The result of tight simulation for queryGraph is retrieved from the cache and its time, t_withCache, is measured
//			if(tightResults.size() < limit) { // when the number of balls is less than the limit
				startTime = System.nanoTime();
				Set<Ball> tightResults_cache = TightSimulation.getNewTightSimulation(inducedSubgraph, queryGraph, limit);
				tightResults_cache = TightSimulation.filterMatchGraphs(tightResults_cache);
				stopTime = System.nanoTime();
				long t_withCache = stopTime - startTime;
				System.out.println("(1) The total time of tight simulation with cache, 't_withCache': " + (double)t_withCache/1000000 + " ms");
				fileContents.append((double)t_withCache/1000000 + "\t");

				System.out.println("(1) The number of subgraph results: " + tightResults_cache.size());
				vSet.clear();
				for(Ball b : tightResults_cache)
					vSet.addAll(b.nodesInBall);
				System.out.println("(1) The total number of the vertices in all balls: " + vSet.size());
				fileContents.append(vSet.size() + "\t");
				System.out.println();

				System.out.println("************ (1) The ratio ***************");
				double ratio1 = (double)t_noCache / (double)t_withCache;
				System.out.println("(1) t_noCache / t_withCache (using induced subgraph)= " + ratio1);
				System.out.println("******************************************");
				fileContents.append(ratio1 + "\t");
//			} else
//				fileContents.append("-1\t -1\t -1\t");
			
			// (2) ******************************************************************************************
			// Creating balls of polytree and assuming that they are stored in the catch
			startTime = System.nanoTime();
			Set<Ball> cacheBalls = CacheUtils.ballExtractor(inducedSubgraph, polytree, dualSim, limit);
			stopTime = System.nanoTime();
			System.out.println("(2) Time for creating cacheBalls: " + (double)(stopTime - startTime)/1000000 + " ms");
			System.out.println("(2) Number of balls: " + cacheBalls.size());
			fileContents.append((double)(stopTime - startTime)/1000000 + "\t");
			
			startTime = System.nanoTime();
			Set<Ball> resultFromBalls = CacheUtils.tightSimBalls(cacheBalls, queryGraph, limit);
			resultFromBalls = TightSimulation.filterMatchGraphs(resultFromBalls);
			stopTime = System.nanoTime();
			long t_wcacheBalls = stopTime - startTime;
			System.out.println("(2) The total time of tight simulation with cache, 't_wcacheBalls': " + (double)t_wcacheBalls/1000000 + " ms");
			fileContents.append((double)t_wcacheBalls/1000000 + "\t");

			System.out.println("(2) The number of subgraph results: " + resultFromBalls.size());
			vSet.clear();
			for(Ball b : resultFromBalls)
				vSet.addAll(b.nodesInBall);
			System.out.println("(2) The total number of the vertices in all balls: " + vSet.size());
			fileContents.append(vSet.size() + "\t");
			System.out.println();

			System.out.println("************ (2) The ratio ***************");
			double ratio2 = (double)t_noCache / (double)t_wcacheBalls;
			System.out.println("(2) t_noCache / t_wcacheBalls (using extracted balls)= " + ratio2);
			fileContents.append(ratio2 + "\n");
			
			// ******************************************************************************************
			bw.write(fileContents.toString());
			fileContents.delete(0, fileContents.length());
			System.out.println("_______________________________________________");
		} //for(File query : queries)

		bw.close();
	} //main

}
