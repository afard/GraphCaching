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
import cache.FrequencyUsage;

public class TestHit3 {
	
	private static final int DEGREE_RATIO = 3;
	/*
	 * Randomly (but specific distribution) generate queries and store them in the cache
	 * The goal is to check hit rate which should increase after a while, and finding the type of queries with high hit rate
	 * args[0] the path to the bag of query graphs
	 * args[1] the file for storing the statistical results
	 * args[2] the number of queries which can be stored in the cache (cacheSize)
	 * args[3] the ratio of asked queries to available queries
	*/
	public static void main(String[] args) throws Exception {
		long startTime, stopTime;
		double t_searchCache, t_storeCache;
		Map<SmallGraph, SmallGraph> cache = new HashMap<SmallGraph, SmallGraph>(); // the cache
		// the index on the ploytrees stored in the cache
		Map<Set<Pair<Integer,Integer>>, Set<SmallGraph>> cacheIndex = new HashMap<Set<Pair<Integer,Integer>>, Set<SmallGraph>>();
		Map<SmallGraph, String> polytree2query = new HashMap<SmallGraph, String>(); // to keep the relation of polytrees in cache to entered queries
		
		// fuReplacement is an object for LFU replacement
		int cacheSize = Integer.parseInt(args[2]);
		FrequencyUsage fuReplacement = new FrequencyUsage(cacheSize);
		
		// statistical result file
		File file = new File(args[1]);
		// if file does not exists, then create it
		if (!file.exists()) file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());			
		BufferedWriter bw = new BufferedWriter(fw);	
		
		// writing the name of removed queries into file
		File file_r = new File(args[1] + "_removed");
		// if file does not exists, then create it
		if (!file_r.exists()) file_r.createNewFile();
		FileWriter fw_r = new FileWriter(file_r.getAbsoluteFile());			
		BufferedWriter bw_r = new BufferedWriter(fw_r);	

//		int queryOrder = 1;
		bw.write("queryOrder\t queryName\t querySize\t isPolytree\t nHitCandidates\t t_searchCache\t isHit\t hitQueryName\t t_storeCache\n");
		StringBuilder fileContents = new StringBuilder();
		
		//********************************************************************
		// reading all the query graphs and store in the cache those which cannot be answered by previous ones
		File dirGM = new File(args[0]);
		if(!dirGM.isDirectory()) {
			bw.close();
			bw_r.close();
			throw new Exception("The specified path for the modified query graphs is not a valid directory");
		}
		File[] mgraphFiles = dirGM.listFiles();		

		mgraphFiles = CacheUtils.RandomizeArray(mgraphFiles);
		
		int bagSize = mgraphFiles.length;
		int nQueries = Integer.parseInt(args[3]) * bagSize;
		Random randFile = new Random(); 
		
		System.out.println("Processing the bag of queries");
		for(int queryOrder=1; queryOrder <= nQueries; queryOrder ++) { // reading the modified query graphs
			File qFile =  mgraphFiles[randFile.nextInt(bagSize)];
			System.out.print("Processing " + qFile.getName() + ":\t");
			SmallGraph q = new SmallGraph(qFile.getAbsolutePath());
			
			fileContents.append(queryOrder + "\t" + qFile.getName() + "\t" + q.getNumVertices() + "\t");
//			queryOrder ++;
			
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
					// increasing the frequency counter of the polytree
					fuReplacement.addEntry(candidate);

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
				if(cache.size() >= cacheSize) {
					SmallGraph removedP = CacheUtils.removeLFU(fuReplacement, cache, cacheIndex);					
					bw_r.write(polytree2query.get(removedP) + "\t");
					polytree2query.remove(removedP);
				}//if
				cache.put(polytree, new SmallGraph()); // a fake cache just to measure hit-rate
				// adding the polytree to replacement object
				fuReplacement.addEntry(polytree);
				// adding the polytree to cacheIndex
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
		
		System.out.println("Number of signatures in the cache: " + cacheIndex.size());
		System.out.println("Number of polytrees in the cache: " + polytree2query.size());
		bw_r.write("\nNumber of signatures in the cache: " + cacheIndex.size() + "\nNumber of polytrees in the cache: " + polytree2query.size());
		int maxSet = 0;
		for(Set<SmallGraph> pt : cacheIndex.values()) {
			int theSize = pt.size();
			if(theSize > maxSet)
				maxSet = theSize;
		} //for
		System.out.println("The maximum number of stored polytrees with the same signature: " + maxSet);
		bw_r.write("\nThe maximum number of stored polytrees with the same signature: " + maxSet);
		bw_r.close();
	} // main
	
} //class
