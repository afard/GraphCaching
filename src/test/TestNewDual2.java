/*
 * An MIT style license:
 * 
 * Written by Arash Fard and Satya Vikas under supervision of Dr. Lakshmish Ramaswamy and Dr. John A. Miller.
 * 
 * Copyright (c) 2014, The University of Georgia
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package test;

import graph.common.Ball;
import graph.common.Graph;
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

public class TestNewDual2 {
	/* 
	 * Running a set of queries against a data graph, and storing the performance results in file readable by Excel program
	 * args[0] is the dataGraph file
	 * args[1] the file containing the list of desired queries
	 * args[2] the folder containing the query Files
	 * args[3] is the output file
	 * args[4] the reverse data graph if it is available 
	 */
	public static void main(String[] args) throws Exception {
		if(args.length < 4) {
			System.out.println("Not correct number of input arguments");
			System.exit(-1);
		}
		long startTime, stopTime;
		
		startTime = System.nanoTime();
		Graph dataGraph = new Graph(args[0]);
		if(args.length == 5)	dataGraph.buildParentIndex(args[4]);
		stopTime = System.nanoTime();
		System.out.println("Spent time to load the data graph: " + (double)(stopTime - startTime)/1000000 + " ms");
		
		File file = new File(args[3]);
		// if file does not exists, then create it
		if (!file.exists()) file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());			
		BufferedWriter bw = new BufferedWriter(fw);	

		bw.write("queryFile\t querySize\t isPolytree\t t_dual\t nV_dual\t t_newDual\t nV_newDual\t t_tight\t nBall_tight\t nV_tight\t t_carTight\t nBall_carTight\t nV_carTight\n");
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

			// dualSim			
			startTime = System.nanoTime();
			Map<Integer, Set<Integer>> dualSim = DualSimulation.getDualSimSet(dataGraph, queryGraph);
			stopTime = System.nanoTime();
			long t_dualSim = stopTime - startTime;
			System.out.println("Spent time to find the dualSimSet: " + (double)t_dualSim/1000000 + " ms");
			fileContents.append((double)t_dualSim/1000000 + "\t");

			Set<Integer> dualSimSet = DualSimulation.nodesInSimSet(dualSim);
			int nV_dual = dualSimSet.size();
			System.out.println("The number of vertices in the dualSimSet: " + nV_dual);
			fileContents.append(nV_dual + "\t");
			System.out.println();

			// newDual			
			startTime = System.nanoTime();
			Map<Integer, Set<Integer>> newDualSim = DualSimulation.getNewDualSimSet(dataGraph, queryGraph);
			stopTime = System.nanoTime();
			long t_newDualSim = stopTime - startTime;
			System.out.println("Spent time to find the newDualSimSet: " + (double)t_newDualSim/1000000 + " ms");
			fileContents.append((double)t_newDualSim/1000000 + "\t");

			Set<Integer> newDualSimSet = DualSimulation.nodesInSimSet(newDualSim);
			int nV_newDual = newDualSimSet.size();
			System.out.println("The number of vertices in the newDualSimSet: " + nV_newDual);
			fileContents.append(nV_newDual + "\t");
			
			System.out.println("t_ratio: " + ((double)t_newDualSim/(double)t_dualSim));
//			fileContents.append(((double)t_newDualSim/(double)t_dualSim) + "\t");
			System.out.println("nV_ratio: " + ((double)nV_newDual/(double)nV_dual));
//			fileContents.append(((double)nV_newDual/(double)nV_dual) + "\t");
			System.out.println();
			
			// The tight simulation is performed and its time is measured			
			startTime = System.nanoTime();
			Set<Ball> tightResults = TightSimulation.getTightSimulation(dataGraph, queryGraph);
			TightSimulation.filterMatchGraphs(tightResults);
			stopTime = System.nanoTime();
			long t_tight = stopTime - startTime;
			System.out.println("The total time of tight simulation, 't_tight': " + (double)t_tight/1000000 + " ms");
			fileContents.append((double)t_tight/1000000 + "\t");

			System.out.println("The number of subgraph results: " + tightResults.size());
			fileContents.append(tightResults.size() + "\t");
			Set<Integer> vSet1 = new HashSet<Integer>();
			for(Ball b : tightResults)
				vSet1.addAll(b.nodesInBall);
			System.out.println("The total number of the vertices in all balls: " + vSet1.size());
			fileContents.append(vSet1.size() + "\t");
			System.out.println();
			
			// The car-tight simulation is performed and its time is measured			
			startTime = System.nanoTime();
			Set<Ball> carTightResults = TightSimulation.getNewTightSimulation(dataGraph, queryGraph, 0, new StringBuilder());
			TightSimulation.filterMatchGraphs(carTightResults);
			stopTime = System.nanoTime();
			long t_carTight = stopTime - startTime;
			System.out.println("The total time of tight simulation, 't_tight': " + (double)t_carTight/1000000 + " ms");
			fileContents.append((double)t_carTight/1000000 + "\t");

			System.out.println("The number of subgraph results: " + carTightResults.size());
			fileContents.append(carTightResults.size() + "\t");
			Set<Integer> vSet2 = new HashSet<Integer>();
			for(Ball b : carTightResults)
				vSet2.addAll(b.nodesInBall);
			System.out.println("The total number of the vertices in all balls: " + vSet2.size());
			fileContents.append(vSet2.size() + "\t");
			System.out.println();
			
		    fileContents.append("\n");
			bw.write(fileContents.toString());
			fileContents.delete(0, fileContents.length());
		}//for
		
		bw.close();
	}//main
}//class
