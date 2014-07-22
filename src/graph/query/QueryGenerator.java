package graph.query;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import graph.common.*;
/**
 * 
 * @author Arash Fard
 *
 */

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
		Map<Integer, Integer> nLabels2Frequency = new HashMap<Integer, Integer>();
		
		while(nCreatedQueries < nRequestedQueries) {
			int center = rand.nextInt(dataGraph.getNumVertices());
			//System.out.println("center: " + center);
			SmallGraph sg = GraphUtils.subGraphBFS(dataGraph, center, degree, nVertices);
			if(sg.getNumVertices() == nVertices) {
				if(degree == 0)
					sg.print2File(args[1] + "/subGN" + nVertices + "D" + degree + "_" + nCreatedQueries + ".txt"); // print to file
				else {
					SmallGraph q = arrangeID(sg);
					q.print2File(args[1] + "/queryN" + nVertices + "D" + degree + "_" + nCreatedQueries + ".txt"); // print to file
					q.buildLabelIndex();
					int nLabels = q.labelIndex.size();
					if(nLabels2Frequency.get(nLabels) == null)
						nLabels2Frequency.put(nLabels, 1);
					else
						nLabels2Frequency.put(nLabels, nLabels2Frequency.get(nLabels) + 1);
				}
				nCreatedQueries ++;
			} //if
		} //while
		
		System.out.println("Frequency of label-range size in " + args[2] + " queries of size " + args[3] + ":");
		for(int ls : nLabels2Frequency.keySet())
			System.out.println(ls + " labels: " + nLabels2Frequency.get(ls));

	} //main
	
	public static SmallGraph arrangeID(SmallGraph g) {
		int nVertices = g.getNumVertices();
		SmallGraph q = new SmallGraph(nVertices);
		Map<Integer, Integer> vMap = new HashMap<Integer, Integer>(nVertices);
		int counter = 0;
		
		for(int indexG : g.labels.keySet()) {
			vMap.put(indexG, counter);			
			q.labels.put(counter, g.labels.get(indexG));
			counter ++;
		} //for
		
		for(int indexG : g.labels.keySet()) {
			int indexQ = vMap.get(indexG);
			q.vertices.put(indexQ, new HashSet<Integer>());
			if(g.post(indexG) != null) {
				for(int child : g.post(indexG)) {
					if(vMap.containsKey(child)) // Any child without a label in labels map will be discarded 
						q.vertices.get(indexQ).add(vMap.get(child));
				} // for
			} //if
		} //for
		
		return q;
	} // arrangeID
}
