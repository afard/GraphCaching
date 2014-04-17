package graph.simulation;

import static java.lang.System.out;
import graph.common.Ball;
import graph.common.Graph;
import graph.common.GraphMetrics;
import graph.common.Utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.javatuples.Pair;


public class SequentialTight {

	static List<Set<String>> listOfDistinctReducedSet = new ArrayList<Set<String>>(); 
	//contains balls left after postprocessing with diameter.
	static Map<Integer,Long>  mapOfBallWithSize = new HashMap<Integer,Long>();
	//conatins list of center vertices which are a match and left after post processing.
	static List<Integer> listOfMatchedBallVertices = new ArrayList<Integer>();

	static int dataGraphSize = 0;
	static int queryGraphSize = 0;

	/******************************************************************************************************
	 * 
	 * @param dGraph - The Data Graph.
	 * @param query - The Query graph.
	 * @return All the subgraphs i.e., the balls, in this case a Map where K is the center and V is the Ball.
	 */
	
	public static Map<Integer,Ball> getTight(Graph dGraph, Graph query ){

		dataGraphSize = dGraph.allIds.length;
		queryGraphSize = query.allIds.length;		

		System.out.println();

		System.out.println("The size of Data Graph is: "+dataGraphSize+" nodes");
		System.out.println("The size of Query Graph is: "+queryGraphSize+" nodes");
		System.out.println();
		
		GraphMetrics qMet = new GraphMetrics(query.vertices);

		//********** FINDING THE "DUAL SIMULATION" STEP **********

		long dualTimeStart = System.currentTimeMillis();
		Map<Integer,Set<Integer>> dualSimSet = DualSimulation.getDual(dGraph, query);
		System.out.println("The Dual Sim Set is"+dualSimSet);
		System.out.println();
		long dualTimeStop = System.currentTimeMillis();
		


		if(dualSimSet.isEmpty()){
			System.out.println("No Dual Match"); 
			System.exit(0);		
		}

		long flattenStart = System.currentTimeMillis();
		Set<Integer> nodesInDualSimSet = new HashSet<Integer>();
		for (Set<Integer> set : dualSimSet.values()){
			for(int id:set){
				nodesInDualSimSet.add(id);
			}
		}
		long flattenStop = System.currentTimeMillis();

		// ********** FINDING THE MATCH GRAPH STEP **************//

		Graph newGraph = dGraph;
		long pruningStart = System.currentTimeMillis();
		newGraph = filterGraph(dGraph,query,dualSimSet);
		//newGraph.print();	
		long pruningStop = System.currentTimeMillis();
		
		
		// FINDING QUERY DIAMETER
		long qDiaStart = System.currentTimeMillis();		
		int qDiameter = query.getRadius(); // changed from qMet to normal getRadius method in Graph.java
		long qDiaStop =System.currentTimeMillis();



		int ballSum = 0;
		Map<Integer, Ball> balls = new HashMap<Integer,Ball>();
		Set<Integer> matchCenters = new HashSet<Integer>();
		long ballCreationTime = 0;
		long dualFilterTime = 0;




		// ****** BALL CREATION STEP ********* // // error in selectivity and central:


		for(int center:dualSimSet.get(query.selectivityCriteria(qMet.central()))){
		//for(int center:dualSimSet.get(86)){
			long ballStartTime = System.currentTimeMillis();	
			//
			Ball ball = new Ball(newGraph,center,qDiameter); // BALL CREATION			
			//System.out.println(ball.ballCenter+"--"+ball.getBallAsString());
			//out.println(center+"  --->   "+ball.nodesInBall);

			long ballStopTime = System.currentTimeMillis();	
			ballCreationTime += (ballStopTime-ballStartTime);

			long dualFilterTimeStart = System.currentTimeMillis();
			Map<Integer,Set<Integer>> clone = new HashMap<Integer,Set<Integer>>(dualSimSet);

			// ******** DUAL FILTER STEP  **********
			
			Map<Integer,Set<Integer>> mat = getDualFilter(query, clone, ball);
			long dualFilterTimeStop = System.currentTimeMillis();
			dualFilterTime+=(dualFilterTimeStop-dualFilterTimeStart);
			balls.put(center, ball);
			if(mat.size()!=0)
			{
				matchCenters.add(center);
				//printMatch(center, mat);
			}

		}
		System.out.println("******************************************************************");		
		System.out.println("The DISTRIBUTED FORMAT RESULT IS:");
		System.out.println();
		System.out.println("The FINAL candidate nodes are: " + printRes(dGraph, balls, matchCenters));
		System.out.println();
		System.out.println("Time for finding DUAL Simulation: " + (dualTimeStop-dualTimeStart)+" ms");
		System.out.println();
		System.out.println("Time for FLATTEN Dual Sim Set: " + (flattenStop-flattenStart)+" ms");
		System.out.println();		
		System.out.println("Graph Pruning Time: " + (pruningStop-pruningStart)+" ms");
		System.out.println();
		System.out.println("The nodes in Dual Sim Set are : "+nodesInDualSimSet);
		System.out.println();
		System.out.println("Graph Size after Pruning is: "+nodesInDualSimSet.size()+" nodes");
		System.out.println();
		System.out.println("Time for finding Query diameter: " + (qDiaStop-qDiaStart)+" ms");
		System.out.println();
		System.out.println("The Query diameter is: "+qDiameter);
		System.out.println();
		//** The below things are output to the console to be used for the tight simulation experiment.
		System.out.println("\nThe nodes form query selectivity are: "+query.selectivityCriteria(qMet.central()));
		System.out.println();
		System.out.println("\nThe nodes from DUAL SIM (which should be used) after query selectivity are: "+dualSimSet.get(query.selectivityCriteria(qMet.central()))); 
		//**
		System.out.println();
		System.out.println("Total no. of Balls: " + balls.keySet().size()+" balls");
		System.out.println();
		System.out.println("Ball Creation Time: " + ballCreationTime+" ms");
		System.out.println();
		System.out.println("Dual Filter Time: " + dualFilterTime+" ms");
		System.out.println();		
		System.out.println("Creating and filtering balls " + (ballCreationTime+dualFilterTime)+" ms");
		System.out.println();
		System.out.println("Number of Tight Simulation Matches: "+matchCenters.size());
		System.out.println();
		
		return balls;
	}


//	/******************************************************************
//	 * perform DUAL FILTER STEP
//	 * 
//	 */																			
//
//	public static Map<Integer,Set<Integer>> dualFilter(Graph query, Map<Integer,Set<Integer>> clone, Ball b){
//
//		//Imposing the Ball on the Dual Simulation result i.e., keeping only the nodes common in the Ball and Dual Sim Set. 
//
//		for(int key:clone.keySet()){
//			Set<Integer> temp = new HashSet<Integer> ();
//			temp =Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(clone.get(key), b.nodesInBall));
//			clone.put(key, temp);
//		}
//
//		//System.out.println(clone);
//		TreeMap<Integer, Integer> filterSet = new TreeMap<Integer, Integer>(Collections.reverseOrder());
//		boolean filtered = false;
//
//		for(Map.Entry<Integer, Set<Integer>> entry : clone.entrySet()){
//			int u = entry.getKey();
//			for(int v : entry.getValue()){
//				if(b.borderNodes.contains(v)){
//					filtered = false;
//					for(int u1:query.post(u)){ 
//						if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v),clone.get(u1))).isEmpty()){
//							filterSet.put(u, v);							
//							filtered = true;
//							break;
//						}
//					}
//					if(!filtered){
//						for(int u2:query.pre(u)){
//							if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v),clone.get(u2))).isEmpty()){
//								filterSet.put(u, v);
//							}
//						}
//					}
//				}
//			}
//		}
//
//
//		while(!filterSet.isEmpty()){
//			out.println("entered the while loop");
//			//the below steps are equivalent to popping an element from a stack. I used TreeMap instead of a stack.
//			int u = filterSet.firstKey();
//			int v = filterSet.get(u);
//
//			//out.println("U --> "+u+"  V --> "+v);
//			filterSet.remove(u);
//
//
//			clone.get(u).remove(v);	
//
//			for(int u2:query.pre(u)){
//				for(int v2:Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v),clone.get(u2)))){
//					if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v2),clone.get(u))).isEmpty()){						
//						filterSet.put(u2, v2);
//					}
//				}
//			}
//			for(int u1:query.post(u)){
//				for(int v1:Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v),clone.get(u1)))){
//					if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v1),clone.get(u))).isEmpty()){
//						filterSet.put(u1, v1);
//					}
//				}
//			}		
//		}
//
//		Map<Integer,Set<Integer>> adjSet = new HashMap<Integer,Set<Integer>>();
//		Map<Integer,Set<Integer>> parList = new HashMap<Integer,Set<Integer>>();
//
//		for(Map.Entry<Integer, Set<Integer>> entry : clone.entrySet()){
//			int u = entry.getKey();
//			for(int v:clone.get(u)){ // BREADCRUMB: here if any errors pop up.. change entry.getValue() TO qbIntersection.get(u) cos its (u,uSim)
//				for(int uc:query.post(u)){
//					for(int vc:Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v),clone.get(uc)))){
//						if(!adjSet.containsKey(v)){
//							Set<Integer> post = new HashSet<Integer>();
//							post.add(vc);
//							adjSet.put(v, post);
//						}else
//							if(adjSet.containsKey(v)){
//								(adjSet.get(v)).add(vc);
//
//							}
//						if(!parList.containsKey(vc)){
//							Set<Integer> pre = new HashSet<Integer>();
//							pre.add(v);
//							parList.put(vc, pre);
//						}else
//							if(parList.containsKey(vc)){
//								(parList.get(vc)).add(v);					
//
//							}
//					}
//				}
//
//			}
//
//		}
//
//		//Finding Max perfect subgraph		
//		Stack<Integer> stack = new Stack<Integer> ();
//		Set<Integer> visited = new HashSet<Integer>();
//		visited.add(b.ballCenter);
//		stack.push(b.ballCenter);
//		while(!stack.isEmpty()){
//			int v = stack.pop();
//			if ( adjSet.containsKey(v)) {
//				for ( int child: adjSet.get(v)) {
//					if (!visited.contains(child)) {
//						stack.push(child);
//						visited.add(child);
//					}
//				}
//			}
//
//			if ( parList.containsKey(v)) {
//				for ( int parent: parList.get(v)) {
//					if (!visited.contains(parent)) {
//						stack.push(parent);
//						visited.add(parent);
//					}
//				}
//			}
//		}
//
//		for ( int k: clone.keySet()) {
//			int[] res = Utils.intersectionOfTwoArrays(clone.get(k), visited);
//			clone.put(k, Utils.convertArrayToHashSet(res));
//		}
//		Set<Integer> matchNodes = new HashSet<Integer>();
//		for (Set<Integer> set : clone.values()){
//			for(int id:set){
//				matchNodes.add(id);
//			}
//		}
//		//out.println("The match nodes are "+ matchNodes);
//		//out.println("the adjSet form the graph is "+adjSet);
//		//The process of trimming the size of the ball starts here.
//		//System.out.println("the ball adjSet befre trimming is"+b.adjSet);
//		//		b.adjSet.clear();
//		//		//b.adjSet = new HashMap<Integer,Set<Integer>>();
//		//		for(int n :adjSet.keySet()){
//		//			if(adjSet.get(n)!=null){				
//		//				for(int nc : adjSet.get(n)){
//		//					if(matchNodes.contains(n) && matchNodes.contains(nc)){
//		//						if(!b.adjSet.containsKey(n)){
//		//							Set<Integer> temp = new HashSet<Integer>();
//		//							temp.add(nc);
//		//							b.adjSet.put(n, temp);
//		//						}else
//		//							if(b.adjSet.containsKey(n)){
//		//								(b.adjSet.get(n)).add(nc);
//		//
//		//							}
//		//					}
//		//				}
//		//			}
//		//		}
//		//out.println("the adjSet after trimming is "+ b.adjSet);
//
//		b.adjSet = new HashMap<Integer,Set<Integer>>();
//		for(Map.Entry<Integer, Set<Integer>> entry : adjSet.entrySet()){
//			for(int nc : entry.getValue()){
//				if(matchNodes.contains(entry.getKey()) && matchNodes.contains(nc)){
//					if(!b.adjSet.containsKey(entry.getKey())){
//						Set<Integer> temp = new HashSet<Integer>();
//						temp.add(nc);
//						b.adjSet.put(entry.getKey(), temp);
//					}else
//						if(b.adjSet.containsKey(entry.getKey())){
//							(b.adjSet.get(entry.getKey())).add(nc);
//						}
//				}
//			}
//
//		}
//		for(int v:clone.keySet()){
//			if((clone.get(v)).isEmpty()){
//				Set <Integer> temp = new HashSet<Integer>();
//				clone.put(v, temp);
//			}
//		}		
//		return clone;
//
//	}


	/********************************************************************************
	 * This is the method used for printing the result in the distributed format. This is basically the post processing phase.
	 * @param The data graph.
	 * @param All the balls, in this case a Map where K is the center and V is the ball.
	 * @param The match Centers.
	 * @return A set of Final set of candidate vertices which are passed as an argument for Induced SubGraph method. 
	 */

	public static Set<Integer> printRes(Graph g, Map<Integer,Ball> balls, Set<Integer> matchCenters){

		long postProcessingTime =0;
		long t0 = 0;
		long t1 = 0;
		Set<Integer> finalCandidates = new HashSet<Integer>();	
		//for(int nodeId =0;nodeId<g.childIndex.size();nodeId++){
		for(int nodeId =0;nodeId<g.allIds.length;nodeId++){
			String ballString = "";
			int isMatch = 0;
			int nEdges = 0;

			if(balls.keySet().contains(nodeId)){
				ballString = balls.get(nodeId).getBallAsString();
				t0 = System.currentTimeMillis();			
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
				t1 = System.currentTimeMillis();
				postProcessingTime+=(t1-t0);
			}
			System.out.println(nodeId + " " + g.getLabel(nodeId)+ " " + ballString + " " + isMatch);
		}		
		System.out.println();
		System.out.println("******************************************************");
		System.out.println();
		System.out.println("Post Processing Time is: "+postProcessingTime+" ms");
		System.out.println();

		return finalCandidates;
	}

	// we use filterGraph() method if we are doing strict

	/********************************************************************
	 This returns the Match graph Generated from the DualSim Set
	 * FILTER GRAPH METHOD
	 * @param DataGraph
	 * @param Query Graph
	 * @param Dual Simulation Set
	 * @return A New Graph object which is the match graph.
	 */

	public static Graph filterGraph(Graph g, Graph q, Map<Integer,Set<Integer>> simSet ){
		Set<Integer> nodesInDualSimSet = new HashSet<Integer>();

		//long start1  = System.currentTimeMillis();
		for (Set<Integer> set : simSet.values()){
			for(int id:set){
				nodesInDualSimSet.add(id);
			}
		}
		//long stop1  = System.currentTimeMillis();
		//System.out.println("Step1 :"+ (stop1-start1)+" ms");

		//				long start = System.currentTimeMillis();
		//				g.getParentIndex1();
		//				g.getChildIndex();
		//				long stop = System.currentTimeMillis();		
		//				System.out.println("Time to find Index is: "+(stop-start)+" ms" );

		//		if(g.childIndex==null) g.childIndex=new HashMap<Integer, Set<Integer>>();
		//		if(g.parentIndex==null) g.parentIndex=new HashMap<Integer, Set<Integer>>();

		Map<Integer, Set<Integer>> newChildIndex = new HashMap<Integer, Set<Integer>>();//g.childIndex.size()
		Map<Integer, Set<Integer>> newParentIndex =  new HashMap<Integer, Set<Integer>>(); ;//g.parentIndex.size()


		//long start2  = System.currentTimeMillis();
		for(int i = 0 ; i< g.allIds.length;i++){
		//	for(int i = 0 ; i< g.childIndex.size();i++){
			//for(int i = 0 ; i< g.childIndex.size();i++){

			//						g.childIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.post(i), nodesInDualSimSet)));			//			
			//						g.parentIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.pre(i), nodesInDualSimSet)));


			// test Case - "fast"
						g.childIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.childIndex.get(i), nodesInDualSimSet)));
						g.parentIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.parentIndex.get(i), nodesInDualSimSet)));

//			Set<Integer> temp1 = g.childIndex.get(i);
//			temp1.retainAll(nodesInDualSimSet);
//
//			Set<Integer> temp2 = g.parentIndex.get(i);
//			temp2.retainAll(nodesInDualSimSet);
//
//			g.childIndex.put(i, temp1);
//			g.parentIndex.put(i, temp2);

			newChildIndex.put(i,new HashSet<Integer>());
			newParentIndex.put(i,new HashSet<Integer>());

		}	
		//long stop2  = System.currentTimeMillis();
		//System.out.println("Step2 :"+ (stop2-start2)+" ms");



		//		Map<Integer, Set<Integer>> newChildIndex = new HashMap<Integer, Set<Integer>>();//g.childIndex.size()
		//		Map<Integer, Set<Integer>> newParentIndex =  new HashMap<Integer, Set<Integer>>(); ;//g.parentIndex.size()


		//long start3  = System.currentTimeMillis();
		//for (int i = 0;i<g.allIds.length;i++){
			//			newChildIndex.put(i,new HashSet<Integer>());
			//			newParentIndex.put(i,new HashSet<Integer>());		
		//}		
		//long stop3  = System.currentTimeMillis();
		//System.out.println("Step3 :"+ (stop3-start3)+" ms");



		//long start4  = System.currentTimeMillis();
		for(int u =0;u<q.allIds.length;u++){
			for(int w : simSet.get(u)){

				for(int v:q.post(u)){
					//if(newChildIndex.keySet().contains(w) && g.childIndex.keySet().contains(w) && simSet.containsKey(v)){
					Set<Integer> temp = new HashSet<Integer>();
					temp = Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.childIndex.get(w),simSet.get(v)));
					newChildIndex.get(w).addAll(temp);
					//}
				}
				for(int v:q.pre(u)){
					//if(newParentIndex.keySet().contains(w) && g.parentIndex.keySet().contains(w) && simSet.containsKey(v)){
					Set<Integer> temp = new HashSet<Integer>();
					temp = Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.parentIndex.get(w),simSet.get(v)));
					newParentIndex.get(w).addAll(temp);
					//}
				}

			}
		}

		//long stop4  = System.currentTimeMillis();
		//System.out.println("Step4 :"+ (stop4-start4)+" ms");
		Graph g1 = new Graph();
		g1.childIndex = newChildIndex;
		g1.parentIndex = g.parentIndex;
		//g1.parentIndex = newParentIndex;
		g1.labelIndex = g.labelIndex;
		return g1;	

	}

	/****************************************************************************************
	 * Checks whether a ball is subset or superset of any other ball and add its entry accordingly.
	 * @param The ball as a string.
	 * @return A boolean variable based on the condition.
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

	/*************************************************************************************************
	 * A method to print the match centers. Just for debugging purposes. 
	 */
	public static void printMatch(int center, Map<Integer,Set<Integer>> mat){
		System.out.println("\n\nMatch for ball centered at "+ center+": ");
		for(Entry<Integer,Set<Integer>> entry : mat.entrySet()){
			System.out.println(entry.getKey()+"  "+entry.getValue());
		}
		System.out.println("---------------------");

	}
	/*********************************************************************
	 * Method Similar to "DUAL FILTER" but with some changes in the Data structures. CAN USE ANY OF THE 2 METHODS.
	 * @param query
	 * @param clone - (Dual Sim Set)
	 * @param ball
	 * @return 
	 */
	public static Map<Integer,Set<Integer>> getDualFilter(Graph query, Map<Integer,Set<Integer>> clone, Ball b){

		//System.out.println("1");
		for(int key:clone.keySet()){
			Set<Integer> temp = new HashSet<Integer> ();
			temp =Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(clone.get(key), b.nodesInBall));
			clone.put(key, temp);
		}
		//System.out.println("after"+clone);

		Stack<Pair<Integer,Integer>> filterSet = new Stack<Pair<Integer,Integer>>();
		boolean filtered = false;

		for(Map.Entry<Integer, Set<Integer>> entry : clone.entrySet()){
			int u = entry.getKey();
			for(int v : entry.getValue()){
				if(b.borderNodes.contains(v)){
					filtered = false;
					for(int u1:query.post(u)){ 
						if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v),clone.get(u1))).isEmpty()){
							filterSet.push(new Pair<Integer, Integer>(u, v));
							filtered = true;
							break;
						}
					}

					if(!filtered){
						for(int u2:query.pre(u)){
							if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v),clone.get(u2))).isEmpty()){
								filterSet.push(new Pair<Integer, Integer>(u, v));
								break;
							}
						}
					}
				}
			}
		}
		while(!filterSet.isEmpty()){
			//System.out.println("2");

			Pair<Integer,Integer> p = filterSet.pop();
			int u = p.getValue0();
			int v = p.getValue1();
			//filterSet.pop();
			//out.println("U --> "+u+"  V --> "+v);
			//out.println("entered the while loop");
			
			clone.get(u).remove(v);

			for(int u2:query.pre(u)){
				for(int v2:Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v),clone.get(u2)))){
					if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v2),clone.get(u))).isEmpty()){

						filterSet.push(new Pair<Integer, Integer>(u2, v2));
					}

				}
			}
			for(int u1:query.post(u)){
				for(int v1:Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v),clone.get(u1)))){
					if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v1),clone.get(u))).isEmpty()){
						filterSet.push(new Pair<Integer, Integer>(u1, v1));
					}
				}
			}		
		}
		Map<Integer,Set<Integer>> adjSet = new HashMap<Integer,Set<Integer>>();
		Map<Integer,Set<Integer>> parList = new HashMap<Integer,Set<Integer>>();


		for(Map.Entry<Integer, Set<Integer>> entry : clone.entrySet()){
			int u = entry.getKey();
			for(int v:clone.get(u)){ // CRUMB: here if any errors pop up.. change entry.getValue() TO qbIntersection.get(u) cos its (u,uSim)
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

		//TRIMMING THE BALL EDGES - (ONLY for Printing Purposes.)

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
	
	
	public static void main(String[] args) {
		System.out.println("READING THE GRAPHS.....");
		System.out.println();
		long readingStart = System.currentTimeMillis();
//		Graph g = new Graph("/Users/Satya/Desktop/graphs/syn-1M/syn-g-200l.txt");
//		Graph q = new Graph("/Users/Satya/Desktop/graphs/syn-1M/80_1.2a_query.txt");		
		
		Graph g = new Graph("/Users/Satya/Desktop/g.txt");
		Graph q = new Graph("/Users/Satya/Desktop/q.txt");
		
//		Graph g = new Graph("/Users/Satya/Desktop/thesis/satya-graphs/A-G-10k.txt");
//		Graph q = new Graph("/Users/Satya/Desktop/thesis/satya-graphs/A-Q-10.txt");
		
		long readingStop = System.currentTimeMillis();
		System.out.println("Time to read the Graphs from the file system: "+(readingStop-readingStart)+" ms");
		System.out.println();
		
		q.getAllIds();

		// ****** FINDING ALL THE INDICES
		
		System.out.println("=====================================================");
		long indexStart = System.currentTimeMillis();
		
		long idStart = System.currentTimeMillis();
		g.getAllIds();
		long idStop = System.currentTimeMillis();
		System.out.println("Time to find IDS is: "+(idStop-idStart)+" ms" );
		System.out.println();
		
		long childIdStart = System.currentTimeMillis();
		g.getChildIndex();
		long childIdStop = System.currentTimeMillis();
		System.out.println("Time to find CHILD INDEX is: "+(childIdStop-childIdStart)+" ms" );
		System.out.println();
		
		long parentIdStart = System.currentTimeMillis();
		g.getParentIndex();
		long parentIdStop = System.currentTimeMillis();
		System.out.println("Time to find PARENT INDEX is: "+(parentIdStop-parentIdStart)+" ms" );
		System.out.println();
		
		long labelIndexStart = System.currentTimeMillis();
		g.getLabelIndex();
		long labelIndexStop = System.currentTimeMillis();
		System.out.println("Time to find LABEL INDEX is: "+(labelIndexStop-labelIndexStart)+" ms" );
		System.out.println();
		
		
		
		long indexStop = System.currentTimeMillis();
		System.out.println("Time to find ALL INDICES is: "+(indexStop-indexStart)+" ms" );
		System.out.println("=====================================================");
		//  FINDING INDICES COMPLETED ******* //
		
		
		System.out.println("Started SEQUENTIAL TIGHT:.....");
		long tightStart = System.currentTimeMillis();
		SequentialTight.getTight(g, q); // pass the data graph and the query graph as the arguments.
		long tightStop = System.currentTimeMillis();				
		System.out.println("The total Time for TIGHT SIMULATION is: "+(tightStop-tightStart)+" ms");
		System.out.println();

	}

}
