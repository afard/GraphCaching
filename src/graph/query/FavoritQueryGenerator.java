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
package graph.query;

import graph.common.Graph;
import graph.common.GraphUtils;
import graph.common.SmallGraph;

import java.io.File;
import java.util.Random;
/**
 * 
 * @author Arash Fard
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
