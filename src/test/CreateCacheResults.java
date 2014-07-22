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

import graph.common.Graph;
import graph.common.GraphUtils;
import graph.common.SmallGraph;
import graph.query.QueryGenerator;
import graph.simulation.DualSimulation;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CreateCacheResults {

	/*
	 * Reads a set of queries and creates their corresponding induced subgraph in the cache
	 * args[0] is the dataGraph file
	 * args[1] the file containing the list of desired queries
	 * args[2] the folder containing the query Files
	 * args[3] is the output folder
	 * args[4] the reverse data graph if it is available 
	 */
	public static void main(String[] args) throws Exception {
		if(args.length < 4) {
			System.out.println("Not correct number of input arguments");
			System.exit(-1);
		}
		
		Graph dataGraph = new Graph(args[0]);
		if(args.length == 5)	dataGraph.buildParentIndex(args[4]);
		
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
		String outputFolder = args[3];
		
		for(String qName : queryNames) {
			System.out.println("_______________________________________________");
			System.out.println("Processing " + qName);
			System.out.println("_______________________________________________");
			
			// The queryGraph is read from file
			SmallGraph queryGraph = new SmallGraph(inputFolder+"/"+qName);

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
			System.out.println();

			// The polytree of the queryGraph is created
			int center = queryGraph.getSelectedCenter();
			SmallGraph polytree = GraphUtils.getPolytree(queryGraph, center);

			// The dualSimSet of the polytree is found
			Map<Integer, Set<Integer>> dualSim = DualSimulation.getNewDualSimSet(dataGraph, polytree);

			// The induced subgraph of the dualSimSet is found
			Set<Integer> dualSimSet = DualSimulation.nodesInSimSet(dualSim);
			SmallGraph inducedSubgraph = GraphUtils.inducedSubgraph(dataGraph, dualSimSet);
			inducedSubgraph = QueryGenerator.arrangeID(inducedSubgraph);

			inducedSubgraph.print2File(outputFolder + "/ans_" + qName);
		} //for(File query : queries)

	} //main

}//class
