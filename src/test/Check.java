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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import graph.common.Ball;
import graph.common.Graph;
import graph.common.GraphUtils;
import graph.common.SmallGraph;
import graph.simulation.DualSimulation;
import graph.simulation.TightSimulation;

public class Check {
	/*
	 * Running a set of queries against a data graph, and storing the performance results in file readable by Excel program
	 * args[0] is the dataGraph file
	 * args[1] is the query file
	 * args[2] the reverse data graph if it is available 
	 */
	public static void main(String[] args) throws Exception {

		Graph dataGraph = new Graph(args[0]);
		if(args.length == 3)	dataGraph.buildParentIndex(args[2]);

		// The queryGraph is read from file
		SmallGraph queryGraph = new SmallGraph(args[1]);

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
		
		// car-tight
		Set<Ball> tightResults = TightSimulation.getNewTightSimulation(dataGraph, queryGraph, 0, new StringBuilder());
		TightSimulation.filterMatchGraphs(tightResults);

		System.out.println("The number of subgraph results: " + tightResults.size());
		Set<Integer> vSet = new HashSet<Integer>();
		for(Ball b : tightResults)
			vSet.addAll(b.nodesInBall);
		System.out.println("The total number of the vertices in all balls: " + vSet.size());

		// polytree
		int center = queryGraph.getSelectedCenter();
		SmallGraph polytree = GraphUtils.getPolytree(queryGraph, center);
		Map<Integer, Set<Integer>> dualSim = DualSimulation.getNewDualSimSet(dataGraph, polytree);

		// The induced subgraph of the dualSimSet is found
		Set<Integer> dualSimSet = DualSimulation.nodesInSimSet(dualSim);
		SmallGraph inducedSubgraph = GraphUtils.inducedSubgraph(dataGraph, dualSimSet);

		// car-tight on the induced subgraph 
		Set<Ball> tightResults_cache = TightSimulation.getNewTightSimulation(inducedSubgraph, queryGraph, 0);
		TightSimulation.filterMatchGraphs(tightResults_cache);
		
		System.out.println("(1) The number of subgraph results: " + tightResults_cache.size());
		Set<Integer> vSet2 = new HashSet<Integer>();
		for(Ball b : tightResults_cache)
			vSet2.addAll(b.nodesInBall);
		System.out.println("(1) The total number of the vertices in all balls: " + vSet2.size());

		if(vSet.equals(vSet2))
			System.out.println("The results have the same vertices.");
		else
			System.out.println("Different vertices in the results!");
	}//main
}//class
