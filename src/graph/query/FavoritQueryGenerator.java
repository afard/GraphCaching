package graph.query;

import graph.common.Graph;
import graph.common.GraphUtils;
import graph.common.SmallGraph;

import java.io.File;
import java.util.Random;
/**
 * 
 * @author arash
 *
 */
public class FavoritQueryGenerator {
	/*
	 * Randomly extract a few query graphs from a given data graph
	 * args[0] the path to the candidate data graphs
	 * args[1] the path for storing the output queries
	 * args[2] the requested number of queries
	 * args[3] the number of vertices in each query
	 * args[4] the average degree of each vertex in the query (non-negative; 0 means no-limit)
	 */
	public static void main(String[] args) throws Exception {
		// reading all data graphs
		File dirG = new File(args[0]);
		if(!dirG.isDirectory())
			throw new Exception("The specified path for the candidate data graphs is not a valid directory");
		File[] graphFiles = dirG.listFiles();
		Graph[] graphs = new Graph[graphFiles.length];
		for(int i=0; i < graphFiles.length; i++)
			graphs[i] = new Graph(graphFiles[i].getAbsolutePath());
		
		File dirQ = new File(args[1]);
		if(!dirQ.isDirectory())
			throw new Exception("The specified path for storing the output queries is not a valid directory");

		Random rand1 = new Random();
		Random rand2 = new Random();
		int nRequestedQueries = Integer.parseInt(args[2]);
		int nVertices = Integer.parseInt(args[3]);
		int degree = Integer.parseInt(args[4]);
		int nCreatedQueries = 0;
		
		while(nCreatedQueries < nRequestedQueries) {
			Graph dataGraph = graphs[rand1.nextInt(graphs.length)];
			if(dataGraph.getNumVertices() < nVertices) {
				System.out.println("Encountered a small data graph");
				continue;
			}
			int center = rand2.nextInt(dataGraph.getNumVertices());
			//System.out.println("center: " + center);
			SmallGraph sg = GraphUtils.subGraphBFS(dataGraph, center, degree, nVertices);
			if(sg.getNumVertices() == nVertices) {
//				QueryGenerator.print2File(sg, args[1] + "/subGN" + nVertices + "D" + degree + "_" + nCreatedQueries + ".txt"); // print to file
				SmallGraph q = QueryGenerator.arrangeID(sg);
				q.print2File(args[1] + "/queryN" + nVertices + "D" + degree + "_" + nCreatedQueries + ".txt"); // print to file
				nCreatedQueries ++;
			} //if
		} //while

	} //main

}
