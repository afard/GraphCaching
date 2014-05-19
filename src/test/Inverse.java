package test;

import graph.common.Graph;
import graph.common.GraphUtils;

public class Inverse {
	
	public static void main(String[] args) throws Exception {
		Graph dataGraph = new Graph(args[0]);
		GraphUtils.storeInverseGraph(dataGraph, args[1]);
	}

}
