package test;

import graph.common.Ball;
import graph.common.Graph;
import graph.common.GraphUtils;
import graph.common.SmallGraph;
import graph.simulation.DualSimulation;
import graph.simulation.TightSimulation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import cache.CacheUtils;

public class TestRunTime2 {

	/*
	 * Running a set of queries against a data graph, and storing the performance results in file readable by Excel program
	 * args[0] is the dataGraph file
	 * args[1] the file containing the list of desired queries
	 * args[2] the folder containing the query Files
	 * args[3] is the output file
	 * args[4] a number to assign limit for the number of balls (0 means no limit)
	 * args[5] the reverse data graph if it is available 
	 */
	public static void main(String[] args) throws Exception {
		if(args.length < 5) {
			System.out.println("Not correct number of input arguments");
			System.exit(-1);
		}
		long startTime, stopTime;
		int limit = Integer.parseInt(args[4]);
		
		startTime = System.nanoTime();
		Graph dataGraph = new Graph(args[0]);
		if(args.length == 6)	dataGraph.buildParentIndex(args[5]);
		stopTime = System.nanoTime();
		System.out.println("Spent time to load the data graph: " + (double)(stopTime - startTime)/1000000 + " ms");
		
		File file = new File(args[3]);
		// if file does not exists, then create it
		if (!file.exists()) file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());			
		BufferedWriter bw = new BufferedWriter(fw);	

		bw.write("queryFile\t querySize\t isPolytree\t t_dualSim\t nV\t nMC\t t_MG\t t_B\t t_noCache\t nSubgraphs\t nVertices\t t_polytree\t t_dualSim\t dualSimSet.size()\t "
				+ "t_subgraph\t t_withCache\t nVertices\t ratio1\n");
		StringBuilder fileContents = new StringBuilder();

		Set<String> queryNames = new HashSet<String>();
		FileInputStream fstream = new FileInputStream(args[1]);
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String strLine;

		while ((strLine = br.readLine()) != null) {
			String qName = strLine.trim();
			if(!qName.equals(" "))
				queryNames.add(strLine.trim());			
		} // while

		// Close the input stream
		br.close();
		in.close();
		fstream.close();
		
		String inputFolder  = args[2];

		for(String qName : queryNames) {
			System.out.println("_______________________________________________");
			System.out.println("Processing " + qName);
			System.out.println("_______________________________________________");
			
			// The queryGraph is read from file
			startTime = System.nanoTime();
			SmallGraph queryGraph = new SmallGraph(inputFolder+"/"+qName);
			stopTime = System.nanoTime();
			System.out.println("Spent time to load the query graph: " + (double)(stopTime - startTime)/1000000 + " ms");
			int querySize = queryGraph.getNumVertices();
			fileContents.append(qName + "\t");
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
			Set<Ball> tightResults = TightSimulation.getNewTightSimulation(dataGraph, queryGraph, limit, fileContents);
			TightSimulation.filterMatchGraphs(tightResults);
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


			//  ******************************************************************************************
			// The result of tight simulation for queryGraph is retrieved from the cache and its time, t_withCache, is measured
			startTime = System.nanoTime();
			Set<Ball> tightResults_cache = TightSimulation.getNewTightSimulation(inducedSubgraph, queryGraph, limit);
			TightSimulation.filterMatchGraphs(tightResults_cache);
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

			// ******************************************************************************************
		    fileContents.append("\n");
			bw.write(fileContents.toString());
			bw.flush();
			fileContents.delete(0, fileContents.length());
			System.out.println("_______________________________________________");
		} //for(File query : queries)

		bw.close();
	} //main

}
