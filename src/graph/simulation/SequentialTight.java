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


	public static void getTight(Graph dGraph, Graph query ){

		dataGraphSize = dGraph.getAllIds().length;
		queryGraphSize = query.getAllIds().length;
		System.out.println();
		System.out.println();
		System.out.println("The size of Data Graph is: "+dataGraphSize+" nodes");
		System.out.println("The size of Query Graph is: "+queryGraphSize+" nodes");
		System.out.println();


		GraphMetrics qMet = new GraphMetrics(query.vertices);		
		long qDiaStart = System.nanoTime();
		int qDiameter = qMet.rad();
		long qDiaStop = System.nanoTime();

		System.out.println("Finding Query diamter: " + (qDiaStop-qDiaStart)/1000000.0+" ms");
		System.out.println();
		System.out.println("The Query diameter is: "+qDiameter);
		System.out.println();

		long dualTimeStart = System.nanoTime();
		Map<Integer,Set<Integer>> dualSimSet = DualSimulation.getDual(dGraph, query);
		System.out.println("the Dual Sime Set is"+dualSimSet);
		System.out.println();
		long dualTimeStop = System.nanoTime();	
		System.out.println("Time for finding DUAL Simulation: " + (dualTimeStop-dualTimeStart)/1000000.0+" ms");
		System.out.println();	

		if(dualSimSet.size() ==0){
			System.out.println("No Dual Match"); 
			System.exit(0);		
		}


		// GENERATING THE MATCH GRAPH		

		Graph newGraph = dGraph;
		long pruningStart = System.nanoTime();	
		newGraph = filterGraph(dGraph,query,dualSimSet);

		//newGraph.print(); // print() method is useful for printing the graph after modifying it - Helpful for debugging


		long pruningStop = System.nanoTime();	
		System.out.println("Graph Pruning Time: " + (pruningStop-pruningStart)/1000000.0+" ms");
		System.out.println();


		Set<Integer> nodesInDualSimSet = new HashSet<Integer>();
		for (Set<Integer> set : dualSimSet.values()){
			for(int id:set){
				nodesInDualSimSet.add(id);
			}
		}

		System.out.println("The nodes in Dual Sim Set are : "+nodesInDualSimSet);
		System.out.println();
		System.out.println("Graph Size after Pruning is: "+nodesInDualSimSet.size()+" nodes");


		int ballSum = 0;

		Map<Integer, Ball> balls = new HashMap<Integer,Ball>();
		Set<Integer> matchCenters = new HashSet<Integer>();



		long ballCreationTime = 0;
		long dualFilterTime = 0;

		// The below things are output to the console to be used for the tight simulation experiment. 

		System.out.println("\nThe nodes form query selectivity are: "+query.selectivityCriteria(qMet.central()));
		System.out.println();
		System.out.println("\nThe nodes from DUAL SIM (which should be used) after query selectivity are: "+dualSimSet.get(query.selectivityCriteria(qMet.central()))); 


		// ****** BALL CREATION STEP ********* //



		for(int center:dualSimSet.get(query.selectivityCriteria(qMet.central()))){

			long ballStartTime = System.nanoTime();				
			Ball ball = new Ball(newGraph,center,qDiameter); // BALL CREATION			
			//System.out.println(ball.ballCenter+"--"+ball.getBallAsString());
			long ballStopTime = System.nanoTime();	
			ballCreationTime += (ballStopTime-ballStartTime);

			long dualFilterTimeStart = System.nanoTime();

			Map<Integer,Set<Integer>> clone = new HashMap<Integer,Set<Integer>>(dualSimSet);

			// DUAL FILTER STEP
			Map<Integer,Set<Integer>> mat = getDualFilter(query, clone, ball);
			long dualFilterTimeStop = System.nanoTime();
			dualFilterTime+=(dualFilterTimeStop-dualFilterTimeStart);
			balls.put(center, ball);
			if(mat.size()!=0)
			{
				matchCenters.add(center);
				//printMatch(center, mat);
			}

		}	
		System.out.println("----------------------------------------------------------------------");

		System.out.println();
		System.out.println("Total no. of Balls: " + balls.keySet().size()+" balls");
		System.out.println();
		System.out.println("Ball Creation Time: " + ballCreationTime/1000000.0+" ms");
		System.out.println();
		System.out.println("Creating and filtering balls " + (ballCreationTime+dualFilterTime)/1000000.0+" ms");
		System.out.println();		
		System.out.println("******************************************************************");
		System.out.println();
		System.out.println("The Distributed Format result is:");

		System.out.println("The candidate nodes are: " + printRes(dGraph, balls, matchCenters));
		System.out.println();
		System.out.println("Number of Tight Simulation Matches: "+matchCenters.size());
	}






	/******************************************************************
	 * perform DUAL FILTER STEP
	 * 
	 */																			

	public static Map<Integer,Set<Integer>> dualFilter(Graph query, Map<Integer,Set<Integer>> clone, Ball b){

		//Imposing the Ball on the Dual Simulation result i.e., keeping only the nodes common in the Ball and Dual Sim Set. 

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

			out.println("U --> "+u+"  V --> "+v);


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
			for(int v:clone.get(u)){ // BREADCRUMB: here if any errors pop up.. change entry.getValue() TO qbIntersection.get(u) cos its (u,uSim)
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
		//out.println("The match nodes are "+ matchNodes);
		//out.println("the adjSet form the graph is "+adjSet);
		//The process of trimming the size of the ball starts here.
		//System.out.println("the ball adjSet befre trimming is"+b.adjSet);
		//		b.adjSet.clear();
		//		//b.adjSet = new HashMap<Integer,Set<Integer>>();
		//		for(int n :adjSet.keySet()){
		//			if(adjSet.get(n)!=null){				
		//				for(int nc : adjSet.get(n)){
		//					if(matchNodes.contains(n) && matchNodes.contains(nc)){
		//						if(!b.adjSet.containsKey(n)){
		//							Set<Integer> temp = new HashSet<Integer>();
		//							temp.add(nc);
		//							b.adjSet.put(n, temp);
		//						}else
		//							if(b.adjSet.containsKey(n)){
		//								(b.adjSet.get(n)).add(nc);
		//
		//							}
		//					}
		//				}
		//			}
		//		}
		//out.println("the adjSet after trimming is "+ b.adjSet);

		b.adjSet = new HashMap<Integer,Set<Integer>>();
		for(Map.Entry<Integer, Set<Integer>> entry : adjSet.entrySet()){
			for(int nc : entry.getValue()){
				if(matchNodes.contains(entry.getKey()) && matchNodes.contains(nc)){
					if(!b.adjSet.containsKey(entry.getKey())){
						Set<Integer> temp = new HashSet<Integer>();
						temp.add(nc);
						b.adjSet.put(entry.getKey(), temp);
					}else
						if(b.adjSet.containsKey(entry.getKey())){
							(b.adjSet.get(entry.getKey())).add(nc);
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

	/********************************************************************
	 This returns the Match graph Generated from the DualSim Set
	 * FILTER GRAPH METHOD
	 * @param DataGraph
	 * @param Query Graph
	 * @param Dual Simulation Set
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
			newParentIndex.put(i,new HashSet<Integer>());		
		}

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
		Graph g1 = new Graph();
		g1.childIndex = newChildIndex;
		g1.parentIndex = newParentIndex;
		g1.labelIndex = g.labelIndex;
		return g1;

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
	/*********************************************************************
	 * Method Similar to "DUAL FILTER" but with some changes in the Data structures. CAN USE ANY OF THE 2 METHODS.
	 * @param query
	 * @param clone - (Dual Sim Set)
	 * @param ball
	 * @return
	 */


	public static Map<Integer,Set<Integer>> getDualFilter(Graph query, Map<Integer,Set<Integer>> clone, Ball b){


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

							filtered = true;
							break;
						}
					}
					if(!filtered){
						for(int u2:query.pre(u)){
							if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v),clone.get(u2))).isEmpty()){


							}

						}
					}
				}
			}
		}
		while(!filterSet.isEmpty()){

			Pair<Integer,Integer> p = filterSet.peek();
			int u = p.getValue0();
			int v = p.getValue1();
			filterSet.pop();
			out.println("entered the while loop");



			clone.get(u).remove(v);	


			for(int u2:query.pre(u)){
				for(int v2:Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v),clone.get(u2)))){
					if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v2),clone.get(u))).isEmpty()){

						filterSet.push(new Pair(u2, v2));
					}

				}
			}
			for(int u1:query.post(u)){
				for(int v1:Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.post(v),clone.get(u1)))){
					if(Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(b.pre(v1),clone.get(u))).isEmpty()){

						filterSet.push(new Pair(u1, v1));
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

		Graph g = new Graph("/Users/Satya/Desktop/datagraph.txt");
		Graph q = new Graph("/Users/Satya/Desktop/query.txt");
		System.out.println("Started SEQUENTIAL TIGHT:.....");

		long tightStart = System.nanoTime();
		SequentialTight.getTight(g, q); // pass the data graph and the query graph as the arguments.
		long tightStop = System.nanoTime();
		System.out.println("******************************************************");		
		System.out.println("The total Time for the entireProcessing is: "+(tightStop-tightStart)/1000000.0+" ms");
		System.out.println();

	}

}
