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

public class TestHit2 {
	
	private static final int DEGREE_RATIO = 3;
	/*
	 * Randomly (but specific distribution) generate queries and store them in the cache
	 * The goal is to check hit rate which should increase after a while, and finding the type of queries with high hit rate
	 * args[0] the original data graph 
	 * args[1] the path to the candidate base query graphs
	 * args[2] the file for storing the statistical results
	 * args[3] the path to the modified queries
	 * args[4] the reverse of the original data graph if it is available
	*/
	public static void main(String[] args) throws Exception {
		long startTime, stopTime;
		double t_searchCache, t_storeCache;
//		Map<SmallGraph, SmallGraph> cache = new HashMap<SmallGraph, SmallGraph>(); // the cache
		// the index on the ploytrees stored in the cache
		Map<Set<Pair<Integer,Integer>>, Set<SmallGraph>> cacheIndex = new HashMap<Set<Pair<Integer,Integer>>, Set<SmallGraph>>();
		Map<SmallGraph, String> polytree2query = new HashMap<SmallGraph, String>();

		// reading the original data graph
		Graph originalDataGraph = new Graph(args[0]);
		if(args.length == 5)	originalDataGraph.buildParentIndex(args[4]);

		// reading all base query graphs and store them in the cache those which cannot be answered by previous ones
		File dirG = new File(args[1]);
		if(!dirG.isDirectory())
			throw new Exception("The specified path for the base query graphs is not a valid directory");
		File[] graphFiles = dirG.listFiles();
		
		// statistical result file
		File file = new File(args[2]);
		// if file does not exists, then create it
		if (!file.exists()) file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());			
		BufferedWriter bw = new BufferedWriter(fw);	

		bw.write("queryName\t querySize\t isPolytree\t nHitCandidates\t t_searchCache\t isHit\t hitQueryName\t t_storeCache\n");
		StringBuilder fileContents = new StringBuilder();
		
		System.out.println("Processing base queries");
		for(File qFile : graphFiles) { // reading the base query graphs
			System.out.print("Processing " + qFile.getName() + ":\t");
			SmallGraph q = new SmallGraph(qFile.getAbsolutePath());
			
			fileContents.append(qFile.getName() + "\t" + q.getNumVertices() + "\t");
			
			int queryStatus = q.isPolytree();
			switch (queryStatus) {
				case -1: System.out.println("The query Graph is disconnected");
					fileContents.append("-1\t 0\t 0\t 0\t -1\t 0\t"); // invalid query
					continue; // go to the next iteration
				case  0: System.out.print("! polytree, ");
					fileContents.append("0\t");
					break;
				case  1: System.out.print("a polytree, ");
					fileContents.append("1\t");
					break;
				default: System.out.println("Undefined status of the query graph");
					fileContents.append("2\t 0\t 0\t 0\t -1\t 0\t"); // invalid query
					continue;
			}

			// searching in the cache
			String hitQueryName = null;
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

					hitQueryName = polytree2query.get(candidate);
					// use the cache content to answer the query (not the goal of this test)
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
				fileContents.append(hitQueryName + "\t");
			}

			startTime = System.nanoTime();
			if(notInCache) { // Not found in the cache
				System.out.print("Not the cache!, ");
				fileContents.append("0\t");
				fileContents.append("-1\t");
				// Should be answered directly from the data graph (not the goal of this test)
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
				
				polytree2query.put(polytree, qFile.getName()); // save the queries filling the cache
			} //if
			stopTime = System.nanoTime();
			t_storeCache = (double)(stopTime - startTime) / 1000000;
			System.out.println("store: " + t_storeCache);
			fileContents.append(t_storeCache + "\n");			
			
			bw.write(fileContents.toString());
			fileContents.delete(0, fileContents.length());
		} //for
		
		System.out.println("Number of signatures stored (base only): " + cacheIndex.size());
		System.out.println("Number of polytrees stored (base only): " + polytree2query.size());

		//********************************************************************
		// reading all the modified query graphs and store in the cache those which cannot be answered by previous ones
		File dirGM = new File(args[3]);
		if(!dirGM.isDirectory()) {
			bw.close();
			throw new Exception("The specified path for the modified query graphs is not a valid directory");
		}
		File[] mgraphFiles = dirGM.listFiles();

		
		System.out.println("Processing the modified queries");
		for(File qFile : mgraphFiles) { // reading the modified query graphs
			System.out.print("Processing " + qFile.getName() + ":\t");
			SmallGraph q = new SmallGraph(qFile.getAbsolutePath());
			
			fileContents.append(qFile.getName() + "\t" + q.getNumVertices() + "\t");
			
			int queryStatus = q.isPolytree();
			switch (queryStatus) {
				case -1: System.out.println("The query Graph is disconnected");
					fileContents.append("-1\t 0\t 0\t 0\t -1\t 0\t"); // invalid query
					continue; // go to the next iteration
				case  0: System.out.print("! polytree, ");
					fileContents.append("0\t");
					break;
				case  1: System.out.print("a polytree, ");
					fileContents.append("1\t");
					break;
				default: System.out.println("Undefined status of the query graph");
					fileContents.append("2\t 0\t 0\t 0\t -1\t 0\t"); // invalid query
					continue;
			}

			// searching in the cache
			String hitQueryName = null;
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

					hitQueryName = polytree2query.get(candidate);
					// use the cache content to answer the query (not the goal of this test)
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
				fileContents.append(hitQueryName + "\t");
			}

			startTime = System.nanoTime();
			if(notInCache) { // Not found in the cache
				System.out.print("Not the cache!, ");
				fileContents.append("0\t");
				fileContents.append("-1\t");
				// Should be answered directly from the data graph (not the goal of this test)
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
				
				polytree2query.put(polytree, qFile.getName()); // save the queries filling the cache
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
