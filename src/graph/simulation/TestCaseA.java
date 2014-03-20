package graph.simulation;

import graph.common.Ball;
import graph.common.Graph;
import graph.common.GraphMetrics;

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
		long qDiaStart = System.nanoTime();
		int qDiameter = qMet.rad();
		long qDiaStop = System.nanoTime();
		System.out.println("Finding Query diamter: " + (qDiaStop-qDiaStart)/1000000.0+" ms");
		System.out.println();
		System.out.println("The Query diameter is: "+qDiameter);
		System.out.println();
		
		//Finding the subGraph
		long findSubGraphStart = System.nanoTime();
		Graph subGraph = g.inducedSubgraph(vertexSet);
		long findSubGraphStop = System.nanoTime();
		
		System.out.println("Time to find INDUCED SUBGRAPH: "+(findSubGraphStop-findSubGraphStart)/1000000.0+" ms");
		
		long ballCreationTime = 0;
		Map<Integer, Ball> balls = new HashMap<Integer,Ball>();
		Set<Integer> matchCenters = new HashSet<Integer>();
		
		Set<Integer> centerList = new HashSet<Integer>(); // Doing this to find the vertices from query Selectivity 
		Integer[] cList = {2, 1, 6, 5, 4};
		centerList.addAll(Arrays.asList(cList));
		
		//for(int center:dualSimSet.get(query.selectivityCriteria(qMet.central()))){
			for(int center : centerList){
				long ballStartTime = System.nanoTime();				
				Ball ball = new Ball(subGraph,center,qDiameter); // BALL CREATION			
				//System.out.println(ball.ballCenter+"--"+ball.getBallAsString());
				long ballStopTime = System.nanoTime();	
				ballCreationTime += (ballStopTime-ballStartTime);
				balls.put(center, ball);
				matchCenters.add(center);
		}		
			

			System.out.println();
			System.out.println("Total no. of Balls: " + balls.keySet().size()+" balls");
			System.out.println();
			System.out.println("Ball Creation Time: " + ballCreationTime/1000000.0+" ms");
			System.out.println();				
			System.out.println("******************************************************************");
			System.out.println();
			System.out.println("The Distributed Format result is:");

			System.out.println("The candidate nodes are: " + SequentialTight.printRes(g, balls, matchCenters));
			System.out.println();
			System.out.println("Number of Tight Simulation Matches: "+matchCenters.size());
		}
		
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("TESTCASE A:");
		System.out.println("========================================================================");
		Graph g = new Graph("/Users/Satya/Desktop/datagraph.txt");
		Graph q = new Graph("/Users/Satya/Desktop/query.txt");
		g.getAllIds();
		//g.getChildIndex();
		long tightStart = System.nanoTime();
		
		Integer[] aList = {2, 1, 6, 5, 4};		
		Set<Integer> vertexSet = new HashSet<Integer>();
		vertexSet.addAll(Arrays.asList(aList));
		TestCaseA.testCaseA(vertexSet, g, q);; // pass the data graph and the query graph as the arguments.
		long tightStop = System.nanoTime();
		System.out.println("******************************************************");		
		System.out.println("The total Time for the entireProcessing is: "+(tightStop-tightStart)/1000000.0+" ms");
		System.out.println();
	}

}
