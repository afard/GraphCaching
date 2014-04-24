package graph.simulation;
/*
 * For experiments with any java stuff
 */

import graph.common.Ball;
import graph.common.Graph;
import graph.common.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;


// THIS IS THE TEST COMMENT
public class test {

	public Map<Integer,Set<Integer>> getDfs(Graph g, int center){
		System.out.println("***** this is DFS  *****");
		
		Map<Integer,Set<Integer>> adjList = new HashMap<Integer, Set<Integer>>();
		int n = g.childIndex.size();
		for(int i = 0; i < n; i++){
			adjList.put(i, new HashSet<Integer>());
		}

		Graph ug = g.getUndirectedGraph();

		boolean[] visited = new boolean[n];
		Stack<Integer> s = new Stack<Integer>();
		s.add(center);
		visited[center] = true;
		while(!s.isEmpty()){
			int node = s.pop();
			System.out.println("The node visited is    "+node);
			for(int child : ug.childIndex.get(node)){
				if(!visited[child]){
					visited[child] = true;
					s.push(child);
					adjList.get(node).add(child);					
					//System.out.println("The pushed child is "+child);

				}

			}
		}
		Map<Integer,Set<Integer>> newAdj = new HashMap<Integer, Set<Integer>>();
		for(int i = 0; i < n; i++){
			newAdj.put(i, new HashSet<Integer>());
		}
		
		for(int i = 0 ; i<adjList.size(); i++){
			for(int j:adjList.get(i)){
				if(g.childIndex.get(i).contains(j)){
					newAdj.get(i).add(j);
					//newAdj.get(i).add(j);
				}
				else if (!g.childIndex.get(i).contains(j)) {
					newAdj.get(j).add(i);
					
					
				}
			}
		}
		

		System.out.println(adjList);
		System.out.println();
		System.out.println(newAdj);


		return adjList;

	}

	public Map<Integer,Set<Integer>> fixPoly(Graph g, Map<Integer,Set<Integer>> adjList){
		
		Map<Integer,Set<Integer>> clone = new HashMap<Integer, Set<Integer>>(adjList);
		for(int i = 0; i<clone.size();i++){

			for(int j:clone.get(i) ){
				if(!g.childIndex.get(i).contains(j)){
					clone.get(i).remove(j);
					clone.get(j).add(i);
				}

			}
		}
		System.out.println(clone);
		return clone;

	}


	public boolean getDfs1(Map<Integer, Set<Integer>> undirGraph, int center){
		Map<Integer,Set<Integer>> adjList = new HashMap<Integer, Set<Integer>>();
		int n = undirGraph.size();
		for(int i = 0; i < n; i++){
			adjList.put(i, new HashSet<Integer>());
		}
		boolean[] visited = new boolean[n];
		Stack<Integer> s = new Stack<Integer>();
		s.add(center);
		visited[center] = true;
		while(!s.isEmpty()){
			int node = s.pop();
			System.out.println("The node visited is    "+node);
			for(int child : undirGraph.get(node)){
				if(!visited[child]){
					visited[child] = true;
					s.push(child);
					adjList.get(node).add(child);					
					//System.out.println("The pushed child is "+child);

				}

			}
		}


		System.out.println(adjList);


		return false;

	}

	public Map<Integer,Set<Integer>> getBfs(Graph g, int center){
		System.out.println("***** this is BFS traversal *****");
		Map<Integer,Set<Integer>> adjList = new HashMap<Integer, Set<Integer>>();
		int n = g.allIds.length;
		boolean[] visited = new boolean[n];

		for(int i = 0; i < n; i++){
			adjList.put(i, new HashSet<Integer>());
		}
		Graph ug = g.getUndirectedGraph();
		
		Queue<Integer> q = new LinkedList<Integer>();

		q.add(center);
		visited[center] = true;
		while(!q.isEmpty()){
			int node = q.poll();
			System.out.println("The visited node is "+node);
			for(int child : ug.childIndex.get(node)){
				if(!visited[child]){
					visited[child] = true;
					q.add(child);
					adjList.get(node).add(child);

				}

			}
		}
		


		Map<Integer,Set<Integer>> newAdj = new HashMap<Integer, Set<Integer>>();
		for(int i = 0; i < n; i++){
			newAdj.put(i, new HashSet<Integer>());
		}
		
		for(int i = 0 ; i<adjList.size(); i++){
			for(int j:adjList.get(i)){
				if(g.childIndex.get(i).contains(j)){
					newAdj.get(i).add(j);
					//newAdj.get(i).add(j);
				}
				else if (!g.childIndex.get(i).contains(j)) {
					newAdj.get(j).add(i);
					
					
				}
			}
		}
		

		System.out.println(adjList);
		System.out.println();
		System.out.println(newAdj);
		
		return newAdj;

	}

	public static void main(String[] args) {
		//		Set<Integer> nodesInBall1 = new HashSet<Integer>();
		//		nodesInBall1.add(0);
		//		nodesInBall1.add(1);
		//		nodesInBall1.add(2);
		//		nodesInBall1.add(3);
		//		//nodesInBall1.add(5);
		//		
		//		 Map<Integer,Set<Integer>> dualSimSet = new HashMap<Integer,Set<Integer>>();
		//		//  dualSimSet.put(0,new Set(440));
		//		
		//		Set<Integer> nodesInBall2 = new HashSet<Integer>();
		//		nodesInBall2.add(0);
		//		nodesInBall2.add(1);
		//		nodesInBall2.add(2);
		//		nodesInBall2.add(3);
		//		//nodesInBall2.add(4);
		//		
		//		//Set<Integer> newSet = (nodesInBall2) | (nodesInBall1);
		//		
		//		if((nodesInBall2).containsAll(nodesInBall1)){
		//			System.out.println("node2 is subset of node1");
		//		}else{
		//			System.out.println("node2 is  NOT subset of node1");
		//		}
		//	
		//		Map<Integer, Integer> myMap = new HashMap<Integer, Integer>();
		//		myMap.put(1, 2);
		//		myMap.put(1, 3);
		//		System.out.println("*******"+myMap.get(1));
		//		
		//		
		//		TreeMap<Integer, Integer> filterSet = new TreeMap<Integer, Integer>(Collections.reverseOrder());
		//		
		//		filterSet.put(1,1);
		//		filterSet.put(2,2);
		//		filterSet.put(3,3);
		//		filterSet.put(4,4);
		//		filterSet.put(5,5);
		//		String ballstr = "0->[1,2,],1->[],2->[],4->[2,],";
		//		String[] str = ballstr.replaceAll("[\\[\\]\\-\\>,]*", " ").replaceAll("  ", ",").replaceAll(" ","").split(",");
		//		Set<String> mySet = new HashSet<String>(Arrays.asList(str));
		//		System.out.println(mySet);
		//		
		//		Set<Integer> centerList = new HashSet<Integer>(); // Doing this to find the vertices from query Selectivity 
		//		Integer[] cList = {1, 684, 817, 816, 556, 130, 153, 154, 18, 696, 561, 145, 264, 265, 516, 33, 649, 310, 525, 916, 520, 186, 290, 909, 905, 667, 540, 297, 181, 896, 343, 751, 475, 336, 618, 197, 760, 761, 868, 639, 375, 714, 717, 580, 719, 110, 862, 116, 732, 114, 974, 490, 367, 604};
		//		centerList.addAll(Arrays.asList(cList));
		//		System.out.println("******"+centerList.size());
		//		

		//System.out.println(filterSet.lowerEntry(key));

		Map m = new HashMap();
		m.put(12, 13);
		m.put(18, 19);
		m.put(16, 14);
		m.put(13, 16);
		m.put(11, 10);



		Map m1 = new HashMap();
		m1.put(18, 19);
		m1.put(12, 13);
		m1.put(11, 10);
		m1.put(16, 14);
		m1.put(13, 16);
		//		
		System.out.println(m.hashCode());
		System.out.println(m1.hashCode());
		System.out.println(m1.equals(m));


		Graph q1 = new Graph("/Users/Satya/Desktop/testGraph1.txt");
		q1.getAllIds();
		q1.getChildIndex();
		
		test t = new test();
		//t.getDfs1(uG, 1);
		//t.fixPoly(g, t.getDfs(g, 0));
		t.getBfs(q1, 5);
		//t.getDfs(g, 2);
	}
}

