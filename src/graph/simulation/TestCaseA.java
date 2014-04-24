package graph.simulation;

import graph.common.Ball;
import graph.common.Graph;
import graph.common.GraphMetrics;
import graph.common.Utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * -This test case involves in finding a subgraph from set of input vertices and finds balls against that graph.
 * -No dual Simulation is involved
 */

public class TestCaseA {


	public static  void testCaseA(Set<Integer> vertexSet, Graph g, Graph q){

		GraphMetrics qMet = new GraphMetrics(q.vertices);
		long qDiaStart = System.currentTimeMillis();
		int qDiameter = qMet.rad();
		long qDiaStop = System.currentTimeMillis();		

		//FINDING THE INDUCED SUBGRAPH
		long findSubGraphStart = System.currentTimeMillis();
		//System.out.println("vertexSet: "+vertexSet);
		Graph subGraph = g.inducedSubgraph(vertexSet);
		long findSubGraphStop = System.currentTimeMillis();		
		//subGraph.print();

		//FINDING THE DUAL SIMULATION USING THE INDUCED SUBGRAPH FROM THE ABOVE STEP

		long dualTimeStart = System.currentTimeMillis();
		Map<Integer,Set<Integer>> dualSimSet = DualSimulation.getDual(subGraph, q);
		System.out.println("the Dual Sime Set is"+dualSimSet);
		System.out.println();
		long dualTimeStop = System.currentTimeMillis();	
		//System.out.println("Time for finding DUAL Simulation: " + (dualTimeStop-dualTimeStart)/1000000.0+" ms");
		System.out.println("Time for finding DUAL Simulation: " + (dualTimeStop-dualTimeStart)+" ms");
		System.out.println();	

		if(dualSimSet.size() ==0){
			System.out.println("No Dual Match"); 
			System.exit(0);		
		}

		Set<Integer> nodesInDualSimSet = new HashSet<Integer>();
		for (Set<Integer> set : dualSimSet.values()){
			for(int id:set){
				nodesInDualSimSet.add(id);
			}
		}
		// FINDING THE MATCH GRAPH STEP

		Graph newGraph = subGraph;
		long pruningStart = System.currentTimeMillis();
		newGraph = filterGraph(subGraph,q,dualSimSet);
		//newGraph.print();	
		long pruningStop = System.currentTimeMillis();
		//		System.out.println("Graph Pruning Time: " + (pruningStop-pruningStart)+" ms");
		//		System.out.println();		


		long ballCreationTime = 0;
		long dualFilterTime = 0;
		Map<Integer, Ball> balls = new HashMap<Integer,Ball>();
		Set<Integer> matchCenters = new HashSet<Integer>();


		// THIS EXPERIMENT IS WITHOUT USING FILTER GRAPH AND DUAL FILTER

		for(int center:dualSimSet.get(q.selectivityCriteria(qMet.central()))){
			//	for(int center:dualSimSet.get(86)){

			long ballStartTime = System.currentTimeMillis();				
			Ball ball = new Ball(subGraph,center,qDiameter); // BALL CREATION			
			//System.out.println(ball.ballCenter+"--"+ball.getBallAsString());
			long ballStopTime = System.currentTimeMillis();	
			ballCreationTime += (ballStopTime-ballStartTime);
			long dualFilterTimeStart = System.currentTimeMillis();
			Map<Integer,Set<Integer>> clone = new HashMap<Integer,Set<Integer>>(dualSimSet);

			// DUAL FILTER STEP
			Map<Integer,Set<Integer>> mat = SequentialTight.getDualFilter(q, clone, ball);
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
		System.out.println("The FINAL candidate nodes are: " + SequentialTight.printRes(g, balls, matchCenters));
		System.out.println();			
		System.out.println("Time for finding DUAL Simulation: " + (dualTimeStop-dualTimeStart)+" ms");
		System.out.println();
		System.out.println("The nodes in Dual Sim Set are : "+nodesInDualSimSet);
		System.out.println();
		System.out.println("Time to find INDUCED SUBGRAPH: "+(findSubGraphStop-findSubGraphStart)+" ms");
		System.out.println();
		System.out.println("The size of Induced SubGraph is: "+subGraph.childIndex.size());
		System.out.println();
		System.out.println("Time for finding Query diameter: " + (qDiaStop-qDiaStart)+" ms");
		System.out.println();
		System.out.println("The Query diameter is: "+qDiameter);
		System.out.println();
		System.out.println("Graph Pruning Time: " + (pruningStop-pruningStart)+" ms");
		System.out.println();
		System.out.println("\nThe nodes form query selectivity are: "+q.selectivityCriteria(qMet.central()));
		System.out.println();
		System.out.println("\nThe nodes from DUAL SIM (which should be used) after query selectivity are: "+dualSimSet.get(q.selectivityCriteria(qMet.central()))); 
		//**
		System.out.println();
		System.out.println("Total no. of Balls: " + balls.keySet().size()+" balls");
		System.out.println();
		System.out.println("Ball Creation Time: " + ballCreationTime+" ms");
		System.out.println();			
		System.out.println("Number of Tight Simulation Matches: "+matchCenters.size());
		System.out.println();
	}

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


		Map<Integer, Set<Integer>> newChildIndex = new HashMap<Integer, Set<Integer>>();//g.childIndex.size()
		Map<Integer, Set<Integer>> newParentIndex =  new HashMap<Integer, Set<Integer>>(); ;//g.parentIndex.size()



		for(int i:g.childIndex.keySet()){


			g.childIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.childIndex.get(i), nodesInDualSimSet)));
			g.parentIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.parentIndex.get(i), nodesInDualSimSet)));				

			newChildIndex.put(i,new HashSet<Integer>());
			newParentIndex.put(i,new HashSet<Integer>());

		}	
		
		for(int u =0;u<q.allIds.length;u++){
			for(int w : simSet.get(u)){

				for(int v:q.post(u)){
					
					Set<Integer> temp = new HashSet<Integer>();
					temp = Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.childIndex.get(w),simSet.get(v)));
					newChildIndex.get(w).addAll(temp);
					
				}
				for(int v:q.pre(u)){
					
					Set<Integer> temp = new HashSet<Integer>();
					temp = Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.parentIndex.get(w),simSet.get(v)));
					newParentIndex.get(w).addAll(temp);
				
				}

			}
		}		
		Graph g1 = new Graph();
		g1.childIndex = newChildIndex;
		g1.parentIndex = g.parentIndex;		
		g1.labelIndex = g.labelIndex;
		return g1;	

	}

}
