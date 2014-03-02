package com.satya.simulation;
/******************************************************************
 * @author Satya
 * This is the program for Sequential Strong Simulation.
 */
// Code available in GIT
import com.satya.graph.common.Ball;
import com.satya.graph.common.Graph;
import com.satya.graph.common.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import static java.lang.System.out;

public class SequentialStrong {

	//contains total number of matches after post processing
	static List<Set<String>> listOfDistinctReducedSet = new ArrayList<Set<String>>(); 
	//contains balls left after postprocessing with diameter.
	static Map<Integer,Long>  mapOfBallWithSize = new HashMap<Integer,Long>();
	//contains list of center vertices which are a match and left after post processing.
	static List<Integer> listOfMatchedBallVertices = new ArrayList<Integer>();


	/*****************************************************************
	 * Runs the sequential STRONG simulation on the ball
	 * @param graph The graph that we want to run dual simulation on 
	 * @param graphLabels The labels for the graph
	 * @param query The query graph  
	 * @return The sim set
	 */
	public static void  getStrongSimMap(Graph dGraph, Graph query, String sim) {

		int dataGraphSize = dGraph.getAllIds().length;
		int queryGraphSize = query.getAllIds().length;

		//Finding the Dual Sim Set

		Map<Integer,Set<Integer>> dualSimSet = SequentialDual.getDual(dGraph.getChildIndex(), dGraph.getLabelMap(), query);
		out.println("The Dual Sim Set is :"+dualSimSet);
		System.out.println();
		if(dualSimSet.size() ==0){
			System.out.println("No Dual Match");
			System.exit(0);		
		}

		Graph newGraph = dGraph;

		/*
		 * 	The kind of simulation is mentioned through the args. For eg: If you want to perform Strict simulation, give the first argument as "strict"
		 *	If we are doing strict simulation we also DO the step of finding the match graph.
		 *	NOTE: after finding the match graph... note that we are only changing the adjSet and parList of the graph.
		 *	
		 */


		if(sim.equals("strict")){ 			
			System.out.println("Doing STRICT Simulation.....");
			System.out.println();
			long pruningStart = System.nanoTime();
			newGraph = SequentialStrong.filterGraph(dGraph,query,dualSimSet);
			//			System.out.println("the new childIndex is "+ newGraph.childIndex);
			//			System.out.println();
			//			System.out.println("the new Parentindex is "+ newGraph.parentIndex );
			//			System.out.println();			
			long pruningStop = System.nanoTime();	
			System.out.println("Graph Pruning Time: " + (pruningStop-pruningStart)/1000000.0+" ms");
			System.out.println();
		}


		else{
			System.out.println("Doing STRONG Simulation.....");
			System.out.println();
		}
		//flatten 
		Set<Integer> nodesInDualSimSet = new HashSet<Integer>();
		for (Set<Integer> set : dualSimSet.values()){
			for(int id:set){
				nodesInDualSimSet.add(id);
			}
		}	

		int prunedSize = nodesInDualSimSet.size();

		int queryDiameter = query.getDiameter();
		System.out.println("The query diameter is "+ queryDiameter);
		System.out.println();

		int ballSum = 0;

		Map<Integer, Ball> balls = new HashMap<Integer,Ball>();
		Set<Integer> matchCenters = new HashSet<Integer>();

		long ballTime = 0;

		//Process of Ball creation starts from here.
		for(int center:nodesInDualSimSet){

			long ballStartTime = System.nanoTime();	
			Ball ball = new Ball(newGraph,center,queryDiameter);
			long ballStopTime = System.nanoTime();	
			ballTime+=(ballStopTime-ballStartTime);

			ballSum += ball.nodesInBall.size(); 
			//			Map<Integer,Set<Integer>> ballAdjSet = ball.getBall();
			//			Map<Integer,Integer> ballLabels = ball.ballLabelIndex;
			int ballCenter = ball.ballCenter;
			Map<Integer,Set<Integer>> clone = new HashMap<Integer,Set<Integer>>(dualSimSet);			
			
			Map<Integer,Set<Integer>> mat = SequentialStrong.dualFilter(query, clone, ball);

			balls.put(ballCenter, ball);
			if(mat.size()!=0)
			{
				matchCenters.add(center);				
			}else{
				System.out.println("No match for ball centered at "+center+".");
			}

		}
		System.out.println("the ball centers are"+matchCenters);
		System.out.println();		
		System.out.println("Total no. of Balls: " + balls.keySet().size()+" balls");
		System.out.println();
		System.out.println("Ball Creation Time: " + ballTime/1000000.0+" ms");
		SequentialStrong.printRes(dGraph, balls, matchCenters);
		System.out.println("The number of "+(String) sim+" matches are: "+ matchCenters.size());
		System.out.println();

	}
	/******************************************************************
	 * perform DUAL FILTER STEP
	 * 
	 */																			
	
	public static Map<Integer,Set<Integer>> dualFilter(Graph query, Map<Integer,Set<Integer>> clone, Ball b){

		for(int key:clone.keySet()){
			Set<Integer> temp = new HashSet<Integer> ();
			temp =Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(clone.get(key), b.nodesInBall));
			clone.put(key, temp);
		}


		TreeMap<Integer, Integer> filterSet = new TreeMap<Integer, Integer>(Collections.reverseOrder());
		boolean filtered = false;
		for(Map.Entry<Integer, Set<Integer>> entry : clone.entrySet()){
			int u = entry.getKey();
			for(int v : entry.getValue()){
				if(b.borderNodes.contains(v)){
					filtered = false;
					for(int u1:query.post(u)){ 
						if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v),clone.get(u1))).isEmpty()){
							filterSet.put(u, v);
							filtered = true;
							break;
						}
					}
					if(!filtered){
						for(int u2:query.pre(u)){
							if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v),clone.get(u2))).isEmpty()){
								filterSet.put(u, v);
							}

						}
					}
				}
			}
		}

		while(!filterSet.isEmpty()){
			//the below steps are equivalent to popping an element from a stack. I used TreeMap instead of a stack.
			int u = filterSet.firstKey();
			int v = filterSet.get(u);
			filterSet.remove(u);


			clone.get(u).remove(v);			
			for(int u2:query.pre(u)){
				for(int v2:Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v),clone.get(u2)))){
					if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v2),clone.get(u))).isEmpty()){
						filterSet.put(u2, v2);
					}

				}
			}
			for(int u1:query.post(u)){
				for(int v1:Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v),clone.get(u1)))){
					if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v1),clone.get(u))).isEmpty()){
						filterSet.put(u1, v1);
					}
				}
			}		
		}

		Map<Integer,Set<Integer>> adjSet = new HashMap<Integer,Set<Integer>>();
		Map<Integer,Set<Integer>> parList = new HashMap<Integer,Set<Integer>>();

		for(Map.Entry<Integer, Set<Integer>> entry : clone.entrySet()){
			int u = entry.getKey();
			for(int v:entry.getValue()){ // CRUMB: here if any errors pop up.. change entry.getValue() TO qbIntersection.get(u) cos its (u,uSim)
				for(int uc:query.post(u)){
					for(int vc:Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v),clone.get(uc)))){
						if(!adjSet.containsKey(v)){
							Set<Integer> post = new HashSet<Integer>();
							post.add(vc);
							adjSet.put(v, post);
						}else
							if(adjSet.containsKey(v)){
								(adjSet.get(v)).add(vc);

							}
						if(!parList.containsKey(vc)){
							Set<Integer> pre = new HashSet<Integer>();
							pre.add(v);
							parList.put(vc, pre);
						}else
							if(parList.containsKey(vc)){
								(parList.get(vc)).add(v);					

							}
					}
				}

			}

		}
		//Finding Max perfect subgraph
		Stack<Integer> stack = new Stack<Integer> ();
		Set<Integer> visited = new HashSet<Integer>();
		visited.add(b.ballCenter);
		stack.push(b.ballCenter);
		while(!stack.isEmpty()){
			int v = stack.pop();
			if ( adjSet.containsKey(v)) {
				for ( int child: adjSet.get(v)) {
					if (!visited.contains(child)) {
						stack.push(child);
						visited.add(child);
					}
				}
			}

			if ( parList.containsKey(v)) {
				for ( int parent: parList.get(v)) {
					if (!visited.contains(parent)) {
						stack.push(parent);
						visited.add(parent);
					}
				}
			}
		}

		/*
		 * for ((v, simV) <- sim.iterator) {
      		sim(v) = simV & visited
    }
		 */
		for ( int k: clone.keySet()) {
			int[] res = Utils.intersectionOfTwoArrays(clone.get(k), visited);
			clone.put(k, Utils.convertArrayToHashSet(res));
		} 



		Set<Integer> matchNodes = new HashSet<Integer>();
		for (Set<Integer> set : clone.values()){
			for(int id:set){
				matchNodes.add(id);
			}
		}
		//The process of trimming the size of the ball starts here.

		b.adjSet = new HashMap<Integer,Set<Integer>>();
		for(int n :adjSet.keySet()){
			if(adjSet.get(n)!=null){				
				for(int nc : adjSet.get(n)){
					if(matchNodes.contains(n) && matchNodes.contains(nc)){
						if(!b.adjSet.containsKey(n)){
							Set<Integer> temp = new HashSet<Integer>();
							temp.add(nc);
							b.adjSet.put(n, temp);
						}else
							if(b.adjSet.containsKey(n)){
								(b.adjSet.get(n)).add(nc);

							}
					}
				}
			}
		}

		for(int v:clone.keySet()){
			if((clone.get(v)).isEmpty()){
				Set <Integer> temp = new HashSet<Integer>();
				clone.put(v, temp);
			}
		}
		return clone;
	}


	/********************************************************************************
	 * This is the method used for printing the result in the distributed format. This is basically the post processing phase.
	 */

	public static Set<Integer> printRes(Graph g, Map<Integer,Ball> balls, Set<Integer> matchCenters){
		int i = 0;
		long postProcessingTime =0;
		long t0 = 0;
		long t1 = 0;
		Set<Integer> finalCandidates = new HashSet<Integer>();		
		for(int nodeId =0;nodeId<g.allIds.length;nodeId++){
			String ballString = "";
			int isMatch = 0;
			int nEdges = 0;

			if(balls.keySet().contains(nodeId)){				
				ballString = balls.get(nodeId).getBallAsString();
				t0 = System.nanoTime();				
				Set str = new HashSet<String>(Arrays.asList(ballString.replaceAll("[\\[\\]\\-\\>,]*", " ").replaceAll("  ", ",").replaceAll(" ","").split(",")));			
				finalCandidates.addAll(str);
				if(checkInsertOfMatch(str)){					
					if(matchCenters.contains(nodeId)){
						isMatch = 1;
						if(!listOfMatchedBallVertices.contains(nodeId)){
							listOfMatchedBallVertices.add(nodeId);
						}
					}
				}	
				else{
					ballString = "";
					matchCenters.remove(nodeId);
				}
				t1 = System.nanoTime();
				postProcessingTime+=(t1-t0);

			}
			System.out.println(nodeId + " " + g.getLabel(nodeId)+ " " + ballString + " " + isMatch);
		}		
		System.out.println("Post Processing Time is: "+postProcessingTime/1000000.0+" ms");
		return finalCandidates;
	}

	// we use filterGraph() method if we are doing strict

	/********************************************************
	 * This returns the Match graph Generated from the DualSim Set
	 * FILTER GRAPH
	 * 
	 */

	public static Graph filterGraph(Graph g, Graph q, Map<Integer,Set<Integer>> simSet ){
		Set<Integer> nodesInDualSimSet = new HashSet<Integer>();

		for (Set<Integer> set : simSet.values()){
			for(int id:set){
				nodesInDualSimSet.add(id);
			}
		}
		g.getParentIndex();
		g.getChildIndex();

		for(int i = 0 ; i< g.getAllIds().length;i++){		

			g.childIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.childIndex.get(i), nodesInDualSimSet)));
			g.parentIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.parentIndex.get(i), nodesInDualSimSet)));		
		}	

		Map<Integer, Set<Integer>> newChildIndex = new HashMap<Integer, Set<Integer>>();//g.childIndex.size()
		Map<Integer, Set<Integer>> newParentIndex =  new HashMap<Integer, Set<Integer>>(); ;//g.parentIndex.size()


		for (int i = 0;i<g.allIds.length;i++){
			newChildIndex.put(i,new HashSet<Integer>());
			newParentIndex.put(i,new HashSet<Integer>());		}


		for(int u =0;u<q.allIds.length;u++){
			for(int w : simSet.get(u)){

				for(int v:q.post(u)){
					if(newChildIndex.keySet().contains(w) && g.childIndex.keySet().contains(w) && simSet.containsKey(v)){
						Set<Integer> temp = new HashSet<Integer>();
						temp = Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.post(w),simSet.get(v)));
						newChildIndex.get(w).addAll(temp);
					}
				}
				for(int v:q.pre(u)){
					if(newParentIndex.keySet().contains(w) && g.parentIndex.keySet().contains(w) && simSet.containsKey(v)){
						Set<Integer> temp = new HashSet<Integer>();
						temp = Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.pre(w),simSet.get(v)));
						newParentIndex.get(w).addAll(temp);
					}
				}

			}
		}
		g.parentIndex.clear();
		g.parentIndex = newParentIndex;
		g.childIndex.clear();
		g.childIndex = newChildIndex;
		return g;

	}

	/*
	 * Checks whether a ball is subset or superset of any other ball and add its entry accordingly.
	 */

	public static boolean checkInsertOfMatch(Set<String> str){
		boolean isInsert = true;		
		if(listOfDistinctReducedSet.isEmpty()){
			listOfDistinctReducedSet.add(str);
		}else{
			outerloop:
				for(int i = 0;i<listOfDistinctReducedSet.size();i++){
					if(listOfDistinctReducedSet.get(i).containsAll(str)){
						listOfDistinctReducedSet.set(i, str);
						isInsert = false;
					}else if(str.containsAll(listOfDistinctReducedSet.get(i))){
						isInsert = false;
						break outerloop;
					}
				}
		if(isInsert){
			listOfDistinctReducedSet.add(str);
		}
		}
		return isInsert;
	}
	/*
	 * Just for debugging purposes
	 */
	public static void printMatch(int center, Map<Integer,Set<Integer>> mat){
		System.out.println("\n\nMatch for ball centered at "+ center+": ");
		for(Entry<Integer,Set<Integer>> entry : mat.entrySet()){
			System.out.println(entry.getKey()+"  "+entry.getValue());
		}
		System.out.println("---------------------");

	}
	
	
	public static void main (String[] args) {
		Graph g = new Graph("/Users/Satya/Desktop/datagraph.txt");
		Graph q = new Graph("/Users/Satya/Desktop/query.txt");
		System.out.println(args[0]);
		SequentialStrong.getStrongSimMap(g, q,args[0]);
	}
}
