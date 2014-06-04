package test;

import graph.common.Graph;

public class GraphAnalysis {

	public static void main(String[] args) throws Exception {
		Graph dataGraph = new Graph(args[0]);
		dataGraph.buildLabelIndex();
		dataGraph.stats();
	}//main
}//class
