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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.javatuples.Pair;

import cache.CacheUtils;

public class TestHit {
	
	private static final int DEGREE_RATIO = 3;
	/*
	 * Randomly (but specific distribution) generate queries and store them in the cache
	 * The goal is to check hit rate which should increase after a while, and finding the type of queries with high hit rate
	 * args[0] the original data graph 
	 * args[1] the path to the candidate popular data graphs
	 * args[2] the file for storing the statistical results
	 * args[3] the path to store hot queries (hit in the cache)
	 * args[4] the number of queries
	 * args[5] the reverse of the original data graph if it is available
	*/
	public static void main(String[] args) throws Exception {
		long startTime, stopTime;
		double t_searchCache, t_storeCache;
//		Map<SmallGraph, SmallGraph> cache = new HashMap<SmallGraph, SmallGraph>(); // the cache
		// the index on the ploytrees stored in the cache
		Map<Set<Pair<Integer,Integer>>, Set<SmallGraph>> cacheIndex = new HashMap<Set<Pair<Integer,Integer>>, Set<SmallGraph>>();
		Map<SmallGraph, Pair<Integer, SmallGraph>> polytree2query = new HashMap<SmallGraph, Pair<Integer, SmallGraph>>();

		// reading the original data graph
		Graph originalDataGraph = new Graph(args[0]);
		if(args.length == 6)	originalDataGraph.buildParentIndex(args[5]);

		// reading all popular data graphs
		File dirG = new File(args[1]);
		if(!dirG.isDirectory())
			throw new Exception("The specified path for the candidate data graphs is not a valid directory");
		File[] graphFiles = dirG.listFiles();
		int nPopularGraphs = graphFiles.length;
		Graph[] graphs = new Graph[nPopularGraphs];
		for(int i=0; i < nPopularGraphs; i++)
			graphs[i] = new Graph(graphFiles[i].getAbsolutePath());
		
		// statistical result file
		File file = new File(args[2]);
		// if file does not exists, then create it
		if (!file.exists()) file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());			
		BufferedWriter bw = new BufferedWriter(fw);	

		bw.write("queryNo\t querySize\t degree\t sourceNo\t isPolytree\t nHitCandidates\t t_searchCache\t isHit\t hitQueryNo\t hitQuerySize\t t_storeCache\n");
		StringBuilder fileContents = new StringBuilder();
		
		int nSourceOptions = nPopularGraphs + 1; // number of sources for creating query
		Random randSource = new Random();
		int[] querySizes = {20,22,24,26,28,30,32,34,36,38}; // different available sizes for queries
		Random randQuerySize = new Random();
//		Random randDegree = new Random();
		Random randCenter = new Random();
				
		int requestedQueries = Integer.parseInt(args[4]);
		for(int queryNo=0; queryNo < requestedQueries; queryNo++) { // main loop
			System.out.print("Processing Q" + queryNo + ":\t");
			SmallGraph q;
			int querySize = 25;
			int degree = 5;
			int sourceNo = randSource.nextInt(nSourceOptions);
			// q is created
			if(sourceNo == nSourceOptions - 1) { // from the original data graph
				boolean notFound = true;
				SmallGraph sg = null;
				while(notFound) {
					querySize = querySizes[randQuerySize.nextInt(querySizes.length)];
					//degree = randDegree.nextInt(querySize/DEGREE_RATIO);
					degree = (int)Math.sqrt(querySize);
					int center = randCenter.nextInt(originalDataGraph.getNumVertices());
					sg = GraphUtils.subGraphBFS(originalDataGraph, center, degree, querySize);
					if(sg.getNumVertices() == querySize)
						notFound = false;
				}
				q = QueryGenerator.arrangeID(sg);
			} else {	// from popular data graphs
				Graph dataGraph = graphs[sourceNo];
				boolean notFound = true;
				SmallGraph sg = null;
				while(notFound) {
					querySize = querySizes[randQuerySize.nextInt(querySizes.length)];
					degree = (int)Math.sqrt(querySize);
					if(degree == 0) continue;
					int center = randCenter.nextInt(dataGraph.getNumVertices());
					sg = GraphUtils.subGraphBFS(dataGraph, center, degree, querySize);
					if(sg.getNumVertices() == querySize)
						notFound = false;
				}
				q = QueryGenerator.arrangeID(sg);
			} //if-else
			
			fileContents.append(queryNo + "\t" + q.getNumVertices() + "\t" + degree + "\t" + sourceNo + "\t");
			System.out.print("N" + q.getNumVertices() + "D" + degree + "S" + sourceNo + ",\t");
			
			int queryStatus = q.isPolytree();
			switch (queryStatus) {
				case -1: System.out.println("The query Graph is disconnected");
					fileContents.append("-1\t 0\t 0\t 0\t -1\t 0\t");
					continue;
				case  0: System.out.print("! polytree, ");
					fileContents.append("0\t");
					break;
				case  1: System.out.print("a polytree, ");
					fileContents.append("1\t");
					break;
				default: System.out.println("Undefined status of the query graph");
					fileContents.append("2\t 0\t 0\t 0\t -1\t 0\t");
					continue;
			}

			// searching in the cache
			Pair<Integer, SmallGraph> hitPair = null;
			startTime = System.nanoTime();
			boolean notInCache = true;
			Set<SmallGraph> candidateMatchSet = CacheUtils.getCandidateMatchSet(q, cacheIndex);
			int nHitCandidates = candidateMatchSet.size();
			System.out.print("nHitCandidates=" + nHitCandidates + ", ");
			fileContents.append(nHitCandidates + "\t");
			
			for(SmallGraph candidate : candidateMatchSet) {
				if(CacheUtils.isDualCoverMatch(q, candidate)) {
					notInCache = false;
					System.out.print("Hit the cache!, ");

					hitPair = polytree2query.get(candidate);
					// use the cache content to answer the query
//					long bTime = System.currentTimeMillis();
//					SmallGraph inducedSubgraph = cache.get(candidate);
//					Set<Ball> tightResults_cache = TightSimulation.getTightSimulation(inducedSubgraph, queryGraph);
//					tightResults_cache = TightSimulation.filterMatchGraphs(tightResults_cache);
//					long fTime = System.currentTimeMillis();
//					System.out.println("The time for tight simulation from cache: " + (fTime - bTime) + " ms");
					break; // the first match would be enough 
				} //if
			} //for
			stopTime = System.nanoTime();
			t_searchCache = (double)(stopTime - startTime) / 1000000;
			System.out.print("search: " + t_searchCache + ", ");
			fileContents.append(t_searchCache + "\t");
			
			if(! notInCache) { // found in the cache
				// hit query
				fileContents.append("1\t");
				int hitQueryNo = hitPair.getValue0();
				SmallGraph hitQuery = hitPair.getValue1();
				hitQuery.print2File(args[3] + "/Q" + hitQueryNo + "_N" + hitQuery.getNumVertices() + ".txt");
				fileContents.append(hitQueryNo + "\t" + hitQuery.getNumVertices() + "\t");
			}

			startTime = System.nanoTime();
			if(notInCache) { // Not found in the cache
				System.out.print("Not the cache!, ");
				fileContents.append("0\t");
				fileContents.append("-1\t-1\t");
//				long bTime = System.currentTimeMillis();
//				Set<Ball> tightResults_cache = TightSimulation.getTightSimulation(dataGraph, queryGraph);
//				tightResults_cache = TightSimulation.filterMatchGraphs(tightResults_cache);
//				long fTime = System.currentTimeMillis();
//				System.out.println("The time for tight simulation without cache: " + (fTime - bTime) + " ms");
				// store in the cache
				// The polytree of the queryGraph is created
				int center = q.getSelectedCenter();
				SmallGraph polytree = GraphUtils.getPolytree(q, center);
				// The dualSimSet of the polytree is found
				// The induced subgraph of the dualSimSet is found
				// The <polytree, inducedSubgraph> is stored in the cache
//				cache.put(polytree, inducedSubgraph);
				Set<Pair<Integer, Integer>> sig = polytree.getSignature(); 
				if (cacheIndex.get(sig) == null) {
					Set<SmallGraph> pltSet = new HashSet<SmallGraph>();
					pltSet.add(polytree);
					cacheIndex.put(sig, pltSet);
				} else
					cacheIndex.get(sig).add(polytree);
				
				polytree2query.put(polytree, new Pair<Integer, SmallGraph>(queryNo, q)); // save the queries filling the cache
			} //if
			stopTime = System.nanoTime();
			t_storeCache = (double)(stopTime - startTime) / 1000000;
			System.out.println("store: " + t_storeCache);
			fileContents.append(t_storeCache + "\n");			
			
			bw.write(fileContents.toString());
			fileContents.delete(0, fileContents.length());
		} //for
		
		bw.close();
		
		System.out.println("Number of signatures stored: " + cacheIndex.size());
		System.out.println("Number of polytrees stored: " + polytree2query.size());
		int maxSet = 0;
		for(Set<SmallGraph> pt : cacheIndex.values()) {
			int theSize = pt.size();
			if(theSize > maxSet)
				maxSet = theSize;
		} //for
		System.out.println("The maximum number of stored polytrees with the same signature: " + maxSet);
	} // main
	
} //class
