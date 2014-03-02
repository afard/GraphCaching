package com.satya.simulation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.satya.graph.common.Ball;
import com.satya.graph.common.Graph;
import com.satya.graph.common.GraphMetrics;

public class TightSimExperiment {
	
	
	
	public static void tightExperiment(Graph dGraph, Graph query){
		long initStart = System.nanoTime();
		
		
		long initStop = System.nanoTime();		
		GraphMetrics qMet = new GraphMetrics(query.vertices);
		//long qDiaTime = 0;
		long qDiaStart = System.nanoTime();
		int qDiameter = qMet.rad();
		long qDiaStop = System.nanoTime();
		
		
		long pruningStart = System.nanoTime();	
		// Right now I am not using the match graph generating step because it needs the dual simulation set.
		//Should verify with Arash how we shall pass the dual sim set.
		//newGraph = SequentialStrong.filterGraph(dGraph, query, dualSimSet);
		
		Map<Integer, Ball> balls = new HashMap<Integer,Ball>();
		Set<Integer> matchCenters = new HashSet<Integer>();
		
		long ballCreationTime = 0;
		long dualFilterTime = 0;
//		System.out.println("\nthe nodes form query selectivity are: "+query.selectivityCriteria(qMet.central()));
//		System.out.println("\n***the nodes from dual SIm after query selectivity are: "+dualSimSet.get(query.selectivityCriteria(qMet.central())));
		
//		for(int center:dualSimSet.get(query.selectivityCriteria(qMet.central()))){
//			System.out.println("the nodes from the selec critieria are: "+dualSimSet.get(query.selectivityCriteria(qMet.central())));
//			System.out.println();
			int center = 4983;
			long ballStartTime = System.nanoTime();	
			Ball ball = new Ball(dGraph,center,qDiameter);
			long ballStopTime = System.nanoTime();	
			ballCreationTime += (ballStopTime-ballStartTime);
			
			long dualFilterTimeStart = System.nanoTime(); // makes changes so that dual filter is used i.e., find a way to pass the dual sim set
			//Map<Integer,Set<Integer>> mat = SequentialStrong.dualFilter(query, dualSimSet, ball);
			long dualFilterTimeStop = System.nanoTime();
			dualFilterTime+=(dualFilterTimeStop-dualFilterTimeStart);
			balls.put(center, ball);
			//System.out.println("the balls are "+balls.values().iterator().next().getBallAsString(ball.adjSet)+"center "+center);
//			if(mat.size()!=0)
//			{
//				matchCenters.add(center);
//			}
//
//		}
		
		System.out.println("Total no. of Balls: " + balls.keySet().size()+" balls");
		System.out.println();
		System.out.println("Ball Creation Time: " + ballCreationTime/1000000.0+" ms");
		System.out.println();
//		System.out.println("Creating and filtering balls " + (ballCreationTime+dualFilterTime)/1000000.0+" ms");
//		System.out.println();
//		System.out.println("Number of Tight Simulation Matches: "+matchCenters.size());
//		System.out.println("-------------------------------------------------------");
		System.out.println();
		//System.out.println("The Distributed Format result is:");
		
		//SequentialStrong.printRes(dGraph, balls, matchCenters);
		}//
	
	
	


	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Graph g = new Graph("/Users/Satya/Desktop/datagraph.txt");
		Graph q = new Graph("/Users/Satya/Desktop/query.txt");
		System.out.println("Started SEQUENTIAL TIGHT EXPERIMENT:.....");
		long tightStart = System.nanoTime();
		tightExperiment(g, q);
		long tightStop = System.nanoTime();
		System.out.println("******************************************************");		
		System.out.println("The total Time for the entireProcessing is: "+(tightStop-tightStart)/1000000.0+" ms");
		System.out.println();
	}

}
