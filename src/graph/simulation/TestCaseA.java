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
		Graph subGraph = g.inducedSubgraph1(vertexSet);
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
		
		//for(int center:dualSimSet.get(q.selectivityCriteria(qMet.central()))){
			for(int center:dualSimSet.get(86)){
			
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

		//long start1  = System.currentTimeMillis();
		for (Set<Integer> set : simSet.values()){
			for(int id:set){
				nodesInDualSimSet.add(id);
			}
		}
		//System.out.println(nodesInDualSimSet);
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
		//for(int i = 0 ; i< g.allIds.length;i++){
			for(int i:g.childIndex.keySet()){
			//for(int i = 0 ; i< g.childIndex.size();i++){

			//						g.childIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.post(i), nodesInDualSimSet)));			//			
			//						g.parentIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.pre(i), nodesInDualSimSet)));


			// test Case - "fast"
						
						g.childIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.childIndex.get(i), nodesInDualSimSet)));
						g.parentIndex.put(i,Utils.convertArrayToHashSet(Utils.intersectionOfTwoArrays(g.parentIndex.get(i), nodesInDualSimSet)));
//				System.out.println(nodesInDualSimSet.size());
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
		
	

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("TESTCASE A:");
		System.out.println("========================================================================");
		
		Graph g = new Graph("/Users/Satya/Desktop/g.txt");
		Graph q = new Graph("/Users/Satya/Desktop/q.txt");
		
//		Graph g = new Graph("/Users/Satya/Desktop/amz-g.txt");
//		Graph q = new Graph("/Users/Satya/Desktop/amz-q-20.txt");
		
//		Graph g = new Graph("/Users/Satya/Desktop/graphs/syn-1M/syn-g-200l.txt");
//		Graph q = new Graph("/Users/Satya/Desktop/graphs/syn-1M/100_1.2a_query.txt");
		
//		Graph g = new Graph("/Users/Satya/Desktop/thesis/satya-graphs/A-G-10k.txt");
//		Graph q = new Graph("/Users/Satya/Desktop/thesis/satya-graphs/A-Q-10.txt");
		g.getAllIds();
		q.getAllIds();
		q.getParentIndex();
		//g.getChildIndex();
		long tightStart = System.currentTimeMillis();
		
		//THIS IS WHERE WE PASS THE VERTEX SET THAT WE GET FROM THE CACHE (in our experiments we get it from SEQUENTIAL TIGHT)	
		
		Integer[] aList = {259126, 373867, 664664, 865141, 158714, 661000, 804941, 2295, 553553, 70554, 314187, 781718, 184663, 818361, 40722, 962925, 94483, 383886, 785061, 585114, 853178, 927990, 70789, 443178, 58407, 353347, 409269, 745559, 815353, 48276, 564440, 313461, 542426, 831819, 898727, 380635, 449230, 58485, 669446, 870205, 238185, 604335, 866214, 956279, 481967, 801568, 78702, 347729, 810313, 444170, 446564, 548820, 948119, 509258, 604839, 599204, 374677, 192479, 482390, 949556, 375667, 203773, 840539, 278607, 234776, 711183, 236525, 74319, 396806, 296502, 332734, 217324, 784263, 270246, 792074, 75948, 48574, 211233, 706841, 953073, 699341, 504621, 622191, 764357, 921822, 131172, 12261, 34657, 953126, 390700, 726186, 565900, 56591, 974639, 73297, 138090, 149995, 112541, 252573, 172378, 269107, 863667, 144595, 866448, 567595, 535274, 142046, 270094, 540352, 462483};		
		Set<Integer> vertexSet = new HashSet<Integer>();
		vertexSet.addAll(Arrays.asList(aList));
		
		TestCaseA.testCaseA(vertexSet, g, q);; // pass the data graph and the query graph as the arguments.
		
		
		long tightStop = System.currentTimeMillis();
		System.out.println("******************************************************");		
		System.out.println("The total Time for the entireProcessing is: "+(tightStop-tightStart)+" ms");
		System.out.println();
	}

}
