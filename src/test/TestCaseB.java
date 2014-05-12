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
import java.util.Map;
import java.util.Set;

public class TestCaseB {

	/**
	 * Running a set of queries against a data graph, and storing the performance results in file readable by Excel program
	 * @param args	args[0] is the dataGraph file, args[1] is the path to the folder of queries, args[2] is the output file
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if(args.length != 3) {
			System.out.println("Not correct number of input arguments");
			System.exit(-1);
		}
		long startTime, stopTime;
		
		startTime = System.currentTimeMillis();
		Graph dataGraph = new Graph(args[0]);
		stopTime = System.currentTimeMillis();
		System.out.println("Spent time to load the data graph: " + (stopTime - startTime) + " ms");
		
		File file = new File(args[2]);
		// if file does not exists, then create it
		if (!file.exists()) file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());			
		BufferedWriter bw = new BufferedWriter(fw);	

		bw.write("querySize\t isPolytree\t t_noCache\t nSubgraphs\t nVertices\t t_polytree\t t_dualSim\t dualSimSet.size()\t "
				+ "t_subgraph\t nVertices\t ratio\n");
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
			System.out.println("Processin " + query.getAbsolutePath());
			System.out.println("_______________________________________________");
			
			// The queryGraph is read from file
			startTime = System.currentTimeMillis();
			SmallGraph queryGraph = new SmallGraph(query.getAbsolutePath());
			stopTime = System.currentTimeMillis();
			System.out.println("Spent time to load the query graph: " + (stopTime - startTime) + " ms");
			fileContents.append(queryGraph.getNumVertices());

			int queryStatus = queryGraph.isPolytree();
			switch (queryStatus) {
				case -1: System.out.println("The query Graph is disconnected");
					System.exit(-1);
					break;
				case  0: System.out.println("The query Graph is connected but not a polytree");
					fileContents.append("\t 0");
					break;
				case  1: System.out.println("The query Graph is a polytree");
					fileContents.append("\t 1");
					break;
				default: System.out.println("Undefined status of the query graph");
					System.exit(-1);
					break;
			}
			System.out.println();

			// The tight simulation is performed and its time, t_noCache, is measured
			startTime = System.currentTimeMillis();
			Set<Ball> tightResults = TightSimulation.getTightSimulation(dataGraph, queryGraph);
			tightResults = TightSimulation.filterMatchGraphs(tightResults);
			stopTime = System.currentTimeMillis();
			long t_noCache = stopTime - startTime;
			System.out.println("The total time of tight simulation without cache, 't_noCache': " + t_noCache + " ms");
			fileContents.append("\t " + t_noCache);

			System.out.println("The number of subgraph results: " + tightResults.size());
			fileContents.append("\t " + tightResults.size());
			int nVertices = 0;
			for(Ball b : tightResults)
				nVertices += b.nodesInBall.size();
			System.out.println("The total number of the vertices in all balls: " + nVertices);
			fileContents.append("\t " + nVertices);
			System.out.println();

			// The polytree of the queryGraph is created
			startTime = System.currentTimeMillis();
			int center = queryGraph.getSelectedCenter();
			SmallGraph polytree = GraphUtils.getPolytree(queryGraph, center);
			stopTime = System.currentTimeMillis();
			long t_polytree = stopTime - startTime;
			System.out.println("Spent time to create the polytree: " + t_polytree + " ms");
			fileContents.append("\t " + t_polytree);
			System.out.println();

			// The dualSimSet of the polytree is found
			startTime = System.currentTimeMillis();
			Map<Integer, Set<Integer>> dualSim = DualSimulation.getDualSimSet(dataGraph, polytree);
			stopTime = System.currentTimeMillis();
			long t_dualSim = stopTime - startTime;
			System.out.println("Spent time to find the dualSimSet of the polytree: " + t_dualSim + " ms");
			fileContents.append("\t " + t_dualSim);
			System.out.println();

			// The induced subgraph of the dualSimSet is found
			startTime = System.currentTimeMillis();
			Set<Integer> dualSimSet = DualSimulation.nodesInSimSet(dualSim);
			SmallGraph inducedSubgraph = GraphUtils.inducedSubgraph(dataGraph, dualSimSet);
			stopTime = System.currentTimeMillis();
			long t_subgraph = stopTime - startTime;
			System.out.println("The number of vertices in the dualSimSet of polytree: " + dualSimSet.size());
			fileContents.append("\t " + dualSimSet.size());
			System.out.println("Spent time to find the induced subgraph of the dualSimSet: " + t_subgraph + " ms");
			fileContents.append("\t " + t_subgraph);
			System.out.println();


			// The result of tight simulation for queryGraph is retrieved from the cache and its time, t_withCache, is measured
			startTime = System.currentTimeMillis();
			Set<Ball> tightResults_cache = TightSimulation.getTightSimulation(inducedSubgraph, queryGraph);
			tightResults_cache = TightSimulation.filterMatchGraphs(tightResults_cache);
			stopTime = System.currentTimeMillis();
			long t_withCache = stopTime - startTime;
			System.out.println("The total time of tight simulation with cache, 't_withCache': " + t_withCache + " ms");
			fileContents.append("\t " + t_withCache);

			System.out.println("The number of subgraph results: " + tightResults_cache.size());
			nVertices = 0;
			for(Ball b : tightResults_cache)
				nVertices += b.nodesInBall.size();
			System.out.println("The total number of the vertices in all balls: " + nVertices);
			fileContents.append("\t " + nVertices);
			System.out.println();

			System.out.println("************ The ratio ***************");
			double ratio = (double)t_noCache / (double)t_withCache;
			System.out.println("t_noCache / t_withCache = " + ratio);
			fileContents.append("\t " + ratio + "\n");

			bw.write(fileContents.toString());
			fileContents.delete(0, fileContents.length());
			System.out.println("_______________________________________________");
		} //for(File query : queries)
		
		bw.close();
	} //main

}
