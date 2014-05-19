package test;
/*
 * For experiments with any java stuff
 */

import graph.common.GraphUtils;

public class ArrangeID {

//	public static void main(String[] args) throws Exception {
//		Graph dataGraph = new Graph(args[0]);
//		GraphUtils.storeInverseGraph(dataGraph, args[1]);
//	}

	public static void main(String[] args) throws Exception {
		GraphUtils.arrangeVertexID(args[0], args[1]);
	}
}

