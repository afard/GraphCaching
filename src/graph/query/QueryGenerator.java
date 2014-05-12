package graph.query;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Random;

import graph.common.*;

public class QueryGenerator {
	/*
	 * Randomly extract a few query graphs from a given data graph
	 * args[0] the data graph
	 * args[1] the path for storing the output queries
	 * args[2] the requested number of queries
	 * args[3] the number of vertices in each query
	 * args[4] the average degree of each vertex in the query (non-negative; 0 means no-limit)
	 * args[5] the reverse data graph if it is available
	 */
	public static void main(String[] args) throws Exception {
		Graph dataGraph = new Graph(args[0]);
		if(args.length == 6)	dataGraph.buildParentIndex(args[5]);
		
		File dir = new File(args[1]);
		if(!dir.isDirectory())
			throw new Exception("The specified path for storing the output queries is not a valid directory");

		Random rand = new Random();
		int nRequestedQueries = Integer.parseInt(args[2]);
		int nVertices = Integer.parseInt(args[3]);
		int degree = Integer.parseInt(args[4]);
		int nCreatedQueries = 0;
		
		while(nCreatedQueries < nRequestedQueries) {
			int center = rand.nextInt(dataGraph.getNumVertices());
			//System.out.println("center: " + center);
			SmallGraph sg = GraphUtils.subGraphBFS(dataGraph, center, degree, nVertices);
			if(sg.getNumVertices() == nVertices) {
				print2File(sg, args[1] + "/queryN" + nVertices + "D" + degree + "_" + nCreatedQueries + ".txt"); // print to file
				nCreatedQueries ++;
			} //if
		} //while

	} //main
	
	public static void print2File(SmallGraph q, String fileName) throws Exception {

			File file = new File(fileName);
			// if file does not exists, then create it
			if (!file.exists()) file.createNewFile();
			FileWriter fw = new FileWriter(file.getAbsoluteFile());			
			BufferedWriter bw = new BufferedWriter(fw);
			
			bw.write(q.toString());
			bw.close();
			System.out.println(fileName + " is written.");
	} //print2File
}
