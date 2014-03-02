package com.satya.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.satya.graph.common.Ball;
import com.satya.graph.common.Graph;
import com.satya.graph.common.GraphMetrics;



public class SequentialTight {

	static List<Set<String>> listOfDistinctReducedSet = new ArrayList<Set<String>>(); 
	//contains balls left after postprocessing with diameter.
	static Map<Integer,Long>  mapOfBallWithSize = new HashMap<Integer,Long>();
	//conatins list of center vertices which are a match and left after post processing.
	static List<Integer> listOfMatchedBallVertices = new ArrayList<Integer>();
	 
	

	Random rand = new Random();

	public static void getTight(Graph dGraph, Graph query ){
		
		int dataGraphSize = dGraph.getAllIds().length;
		int queryGraphSize = query.getAllIds().length;
		System.out.println();
		System.out.println();
		System.out.println("The size of Data Graph is: "+dataGraphSize+" nodes");
		System.out.println("The size of Query Graph is: "+queryGraphSize+" nodes");
		System.out.println();

		GraphMetrics qMet = new GraphMetrics(query.vertices);
		//long qDiaTime = 0;
		long qDiaStart = System.nanoTime();
		int qDiameter = qMet.rad();
		long qDiaStop = System.nanoTime();

		System.out.println("Finding Query diamter: " + (qDiaStop-qDiaStart)/1000000.0+" ms");
		System.out.println();
		System.out.println("The Query diameter is: "+qDiameter);
		System.out.println();



		
		long dualTimeStart = System.nanoTime();	
		
		Map<Integer,Set<Integer>> dualSimSet = SequentialDual.getDual(dGraph.getChildIndex(), dGraph.getLabelMap(), query);
		
		System.out.println("the Dual Sime Set is"+dualSimSet);
		System.out.println();
		long dualTimeStop = System.nanoTime();	
		System.out.println("Time for finding DUAL Simulation: " + (dualTimeStop-dualTimeStart)/1000000.0+" ms");
		System.out.println();	

		if(dualSimSet.size() ==0){
			System.out.println("No Dual Match"); 
			System.exit(0);		
		}
		
		Graph newGraph = new Graph();
		long pruningStart = System.nanoTime();	
		newGraph = SequentialStrong.filterGraph(dGraph,query,dualSimSet);

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
		System.out.println("Graph Size after Pruning is: "+newGraph.allIds.length+" nodes");

		
		int ballSum = 0;

		Map<Integer, Ball> balls = new HashMap<Integer,Ball>();
		Set<Integer> matchCenters = new HashSet<Integer>();
		

		Map<Integer,Set<Integer>> clone = new HashMap<Integer,Set<Integer>>(dualSimSet);
		long ballCreationTime = 0;
		long dualFilterTime = 0;
		
		// The below things are output to the console to be used for the tight simulation experiment. 
		
		System.out.println("\nThe nodes form query selectivity are: "+query.selectivityCriteria(qMet.central()));
		System.out.println("\nThe nodes from DUAL SIM (which should be used) after query selectivity are: "+dualSimSet.get(query.selectivityCriteria(qMet.central()))); 
		
		
		for(int center:dualSimSet.get(query.selectivityCriteria(qMet.central()))){
		//for(int center:nodesInDualSimSet){	
			
			long ballStartTime = System.nanoTime();	
			Ball ball = new Ball(newGraph,center,qDiameter);
			long ballStopTime = System.nanoTime();	
			ballCreationTime += (ballStopTime-ballStartTime);
			
			long dualFilterTimeStart = System.nanoTime();
			Map<Integer,Set<Integer>> mat = SequentialStrong.dualFilter(query, clone, ball);
			long dualFilterTimeStop = System.nanoTime();
			dualFilterTime+=(dualFilterTimeStop-dualFilterTimeStart);
			balls.put(center, ball);			
			if(mat.size()!=0)
			{
				matchCenters.add(center);
			}

		}		
		
		System.out.println();
		System.out.println("Total no. of Balls: " + balls.keySet().size()+" balls");
		System.out.println();
		System.out.println("Ball Creation Time: " + ballCreationTime/1000000.0+" ms");
		System.out.println();
		System.out.println("Creating and filtering balls " + (ballCreationTime+dualFilterTime)/1000000.0+" ms");
		System.out.println();		
		System.out.println("-------------------------------------------------------");
		System.out.println();
		System.out.println("The Distributed Format result is:");
		
		System.out.println("The candidate nodes are: " + SequentialStrong.printRes(dGraph, balls, matchCenters));
		
		System.out.println("Number of Tight Simulation Matches: "+matchCenters.size());
	}





	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Graph g = new Graph("/Users/Satya/Desktop/G.txt");
		Graph q = new Graph("/Users/Satya/Desktop/Q.txt");
		System.out.println("Started SEQUENTIAL TIGHT:.....");
		
		long tightStart = System.nanoTime();
		SequentialTight.getTight(g, q); // pass the data graph and the query graph as the arguments.
		long tightStop = System.nanoTime();
		System.out.println("******************************************************");		
		System.out.println("The total Time for the entireProcessing is: "+(tightStop-tightStart)/1000000.0+" ms");
		System.out.println();

	}

}
