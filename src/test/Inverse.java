package test;

import graph.common.Graph;
import graph.common.GraphUtils;

public class Inverse {
	
	/*
	 * args[0]: the original data graph (input)
	 * args[1]: the inverse graph without considering labels (output)
	 */
	public static void main(String[] args) throws Exception {
		Graph dataGraph = new Graph(args[0]);
		GraphUtils.storeInverseGraph(dataGraph, args[1]);
	}

}
