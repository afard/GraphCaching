
/**************************************************************************
 * @author Satya,Arash
 */
package graph.simulation;

import graph.common.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class TightSimulation {

	/******************************************************************************************************
	 * Performs tight simulation when data graph is of type Graph
	 * @param dGraph - The Data Graph.
	 * @param query - The Query graph.
	 * @return All the subgraphs; i.e., the set of balls in this case
	 */
	public static Set<Ball> getTightSimulation(Graph dataGraph, SmallGraph query) {
		long startTime, stopTime;
		Set<Ball> resultBalls = new HashSet<Ball>();
		
		//********** FINDING THE "DUAL SIMULATION" STEP ********** //
		startTime = System.currentTimeMillis();
		Map<Integer,Set<Integer>> dualSimSet = DualSimulation.getDualSimSet(dataGraph, query);
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- time for dualSimSet: " + (stopTime - startTime) + " ms");
		if(dualSimSet.isEmpty()){
			System.out.println("No Dual Match"); 
			return resultBalls;		
		}
		
		// ********** finding the number of vertices in the dualSimSet **************//
		// *** This step is not a part of the algorithm, and is done only for extracting information
		startTime = System.currentTimeMillis();
		Set<Integer> nVdualSimSet = DualSimulation.nodesInSimSet(dualSimSet);
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- the number of vertices in the dualSimSet: " + nVdualSimSet.size());
		System.out.println("- INSIDE getTightSimulation()- the time for this extra step: " + (stopTime - startTime) + " ms");

		// ********** FINDING THE MATCH GRAPH STEP **************//
		startTime = System.currentTimeMillis();
		SmallGraph newGraph = DualSimulation.getResultMatchGraph(dataGraph, query, dualSimSet);	
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- time for dualMatchGraph: " + (stopTime - startTime) + " ms");

		// ********** FINDING QUERY Radius and selected center ********** //
		startTime = System.currentTimeMillis();
		int qRadius = query.getRadius();
		int qCenter = query.getSelectedCenter();

		// ****** BALL CREATION STEP ********* //
		Set<Integer> matchCenters = dualSimSet.get(qCenter);
		System.out.println("- INSIDE getTightSimulation()- the number of match vertices: " + matchCenters.size());
		
		for(int center : matchCenters){
			Ball ball = new Ball(newGraph, center, qRadius); // BALL CREATION
			// ******** DUAL FILTER STEP  **********
			boolean found = ball.dualFilter(query, dualSimSet);
			if(found)
				resultBalls.add(ball);
		} //for
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- time for ball creation and filtering: " + (stopTime - startTime) + " ms");

		return resultBalls;
	} //getTightSimulation

	/******************************************************************************************************
	 * Performs tight simulation when data graph is of type Graph
	 * It is modified for my tests
	 * @param dGraph - The Data Graph.
	 * @param query - The Query graph.
	 * @return All the subgraphs; i.e., the set of balls in this case
	 */
	public static Set<Ball> getTightSimulationModified(Graph dataGraph, SmallGraph query, int limit) {
		long startTime, stopTime;
		Set<Ball> resultBalls = new HashSet<Ball>();
		
		//********** FINDING THE "DUAL SIMULATION" STEP ********** //
		startTime = System.currentTimeMillis();
		Map<Integer,Set<Integer>> dualSimSet = DualSimulation.getDualSimSet(dataGraph, query);
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- time for dualSimSet: " + (stopTime - startTime) + " ms");
		if(dualSimSet.isEmpty()){
			System.out.println("No Dual Match"); 
			return resultBalls;		
		}
		
		// ********** finding the number of vertices in the dualSimSet **************//
		// *** This step is not a part of the algorithm, and is done only for extracting information
		startTime = System.currentTimeMillis();
		Set<Integer> nVdualSimSet = DualSimulation.nodesInSimSet(dualSimSet);
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- the number of vertices in the dualSimSet: " + nVdualSimSet.size());
		System.out.println("- INSIDE getTightSimulation()- the time for this extra step: " + (stopTime - startTime) + " ms");

		// ********** FINDING QUERY Radius and selected center ********** //
		int qRadius = query.getRadius();
		int qCenter = query.getSelectedCenter();
		Set<Integer> matchCenters = dualSimSet.get(qCenter);
		System.out.println("- INSIDE getTightSimulation()- the number of match vertices: " + matchCenters.size());
		if(matchCenters.size() > limit) { // not processing when it is likely very time consuming
			System.out.println("- INSIDE getTightSimulation()- skip over " + limit);
			return resultBalls;
		}
		
		// ********** FINDING THE MATCH GRAPH STEP **************//
		startTime = System.currentTimeMillis();
		SmallGraph newGraph = DualSimulation.getResultMatchGraph(dataGraph, query, dualSimSet);	
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- time for dualMatchGraph: " + (stopTime - startTime) + " ms");
		
		// ****** BALL CREATION STEP ********* //
		startTime = System.currentTimeMillis();
		for(int center : matchCenters){
			Ball ball = new Ball(newGraph, center, qRadius); // BALL CREATION
			// ******** DUAL FILTER STEP  **********
			boolean found = ball.dualFilter(query, dualSimSet);
			if(found)
				resultBalls.add(ball);
		} //for
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- time for ball creation and filtering: " + (stopTime - startTime) + " ms");

		return resultBalls;
	} //getTightSimulationModified

	/******************************************************************************************************
	 * Performs new-tight simulation when data graph is of type Graph
	 * It is modified for my tests
	 * @param dGraph - The Data Graph.
	 * @param query - The Query graph.
	 * @return All the subgraphs; i.e., the set of balls in this case
	 */
	public static Set<Ball> getNewTightSimulationModified(Graph dataGraph, SmallGraph query, int limit, StringBuilder notes) {
		long startTime, stopTime;
		Set<Ball> resultBalls = new HashSet<Ball>();
		
		//********** FINDING THE "DUAL SIMULATION" STEP ********** //
		startTime = System.currentTimeMillis();
		Map<Integer,Set<Integer>> dualSimSet = DualSimulation.getNewDualSimSet(dataGraph, query);
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- time for dualSimSet: " + (stopTime - startTime) + " ms");
		notes.append((stopTime - startTime) + "\t");
		if(dualSimSet.isEmpty()){
			System.out.println("No Dual Match");
			notes.append("0\t 0\t 0\t 0\t");
			return resultBalls;
		}
		
		// ********** finding the number of vertices in the dualSimSet **************//
		// *** This step is not a part of the algorithm, and is done only for extracting information
		startTime = System.currentTimeMillis();
		Set<Integer> nVdualSimSet = DualSimulation.nodesInSimSet(dualSimSet);
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- the number of vertices in the dualSimSet: " + nVdualSimSet.size());
		System.out.println("- INSIDE getTightSimulation()- the time for this extra step: " + (stopTime - startTime) + " ms");
		notes.append(nVdualSimSet.size() + "\t");

		// ********** FINDING QUERY Radius and selected center ********** //
		int qRadius = query.getRadius();
		int qCenter = query.getSelectedCenter();
		Set<Integer> matchCenters = dualSimSet.get(qCenter);
		System.out.println("- INSIDE getTightSimulation()- the number of match vertices: " + matchCenters.size());
		notes.append(matchCenters.size() + "\t");
//		if(matchCenters.size() > limit) { // not processing when it is likely very time consuming
//			System.out.println("- INSIDE getTightSimulation()- skip over " + limit);
//			return resultBalls;
//		}
		
		// ********** FINDING THE MATCH GRAPH STEP **************//
		startTime = System.currentTimeMillis();
		SmallGraph newGraph = DualSimulation.getResultMatchGraph(dataGraph, query, dualSimSet);	
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- time for dualMatchGraph: " + (stopTime - startTime) + " ms");
		notes.append((stopTime - startTime) + "\t");
		
		// ****** BALL CREATION STEP ********* //
		startTime = System.currentTimeMillis();
		for(int center : matchCenters){
			Ball ball = new Ball(newGraph, center, qRadius); // BALL CREATION
			// ******** DUAL FILTER STEP  **********
			boolean found = ball.dualFilter(query, dualSimSet);
			if(found) {
				resultBalls.add(ball);
				if(resultBalls.size() == limit) break; // it will never happen when limit=0, so finds all the results
			} //if
		} //for
		stopTime = System.currentTimeMillis();
		System.out.println("- INSIDE getTightSimulation()- time for ball creation and filtering: " + (stopTime - startTime) + " ms");
		notes.append((stopTime - startTime) + "\t");

		return resultBalls;
	} //getNewTightSimulationModified

	/******************************************************************************************************
	 * Performs new-tight simulation when data graph is of type Graph
	 * It is modified for my tests
	 * @param dGraph - The Data Graph.
	 * @param query - The Query graph.
	 * @param limit the upper bound for the number of found subgraph results, no limit when it is 0
	 * @return All the subgraphs; i.e., the set of balls in this case
	 */
	public static Set<Ball> getNewTightSimulation(SmallGraph dataGraph, SmallGraph query, int limit) {
//		long startTime, stopTime;
		Set<Ball> resultBalls = new HashSet<Ball>();
		
		//********** FINDING THE "DUAL SIMULATION" STEP ********** //
//		startTime = System.currentTimeMillis();
		Map<Integer,Set<Integer>> dualSimSet = DualSimulation.getNewDualSimSet(dataGraph, query);
//		stopTime = System.currentTimeMillis();
//		System.out.println("- INSIDE getTightSimulation()- time for dualSimSet: " + (stopTime - startTime) + " ms");
		if(dualSimSet.isEmpty()){
//			System.out.println("No Dual Match"); 
			return resultBalls;
		}
		
		// ********** finding the number of vertices in the dualSimSet **************//
		// *** This step is not a part of the algorithm, and is done only for extracting information
//		startTime = System.currentTimeMillis();
//		Set<Integer> nVdualSimSet = DualSimulation.nodesInSimSet(dualSimSet);
//		stopTime = System.currentTimeMillis();
//		System.out.println("- INSIDE getTightSimulation()- the number of vertices in the dualSimSet: " + nVdualSimSet.size());
//		System.out.println("- INSIDE getTightSimulation()- the time for this extra step: " + (stopTime - startTime) + " ms");

		// ********** FINDING QUERY Radius and selected center ********** //
		int qRadius = query.getRadius();
		int qCenter = query.getSelectedCenter();
		Set<Integer> matchCenters = dualSimSet.get(qCenter);
//		System.out.println("- INSIDE getTightSimulation()- the number of match vertices: " + matchCenters.size());
		
		// ********** FINDING THE MATCH GRAPH STEP **************//
//		startTime = System.currentTimeMillis();
		SmallGraph newGraph = DualSimulation.getResultMatchGraph(dataGraph, query, dualSimSet);	
//		stopTime = System.currentTimeMillis();
//		System.out.println("- INSIDE getTightSimulation()- time for dualMatchGraph: " + (stopTime - startTime) + " ms");
		
		// ****** BALL CREATION STEP ********* //
//		startTime = System.currentTimeMillis();
		for(int center : matchCenters){
			Ball ball = new Ball(newGraph, center, qRadius); // BALL CREATION
			// ******** DUAL FILTER STEP  **********
			boolean found = ball.dualFilter(query, dualSimSet);
			if(found) {
				resultBalls.add(ball);
				if(resultBalls.size() == limit) break;
			}
		} //for
//		stopTime = System.currentTimeMillis();
//		System.out.println("- INSIDE getTightSimulation()- time for ball creation and filtering: " + (stopTime - startTime) + " ms");

		return resultBalls;
	} //getNewTightSimulation
	
	/******************************************************************************************************
	 * Performs tight simulation when data graph is of type SmallGraph
	 * @param dGraph - The Data Graph.
	 * @param query - The Query graph.
	 * @return All the subgraphs i.e., the balls, in this case a Map where K is the center and V is the Ball.
	 */
	public static Set<Ball> getTightSimulation(SmallGraph dataGraph, SmallGraph query) {
		Set<Ball> resultBalls = new HashSet<Ball>();
		
		//********** FINDING THE "DUAL SIMULATION" STEP ********** //
		Map<Integer,Set<Integer>> dualSimSet = DualSimulation.getDualSimSet(dataGraph, query);
		if(dualSimSet.isEmpty()){
			System.out.println("No Dual Match"); 
			return resultBalls;		
		}

		// ********** FINDING THE MATCH GRAPH STEP **************//
		SmallGraph newGraph = DualSimulation.getResultMatchGraph(dataGraph, query, dualSimSet);	

		// ********** FINDING QUERY Radius and selected center ********** //
		int qRadius = query.getRadius();
		int qCenter = query.getSelectedCenter();

		// ****** BALL CREATION STEP ********* //
		Set<Integer> matchCenters = dualSimSet.get(qCenter);

		for(int center : matchCenters){
			Ball ball = new Ball(newGraph, center, qRadius); // BALL CREATION
			// ******** DUAL FILTER STEP  **********
			boolean found = ball.dualFilter(query, dualSimSet);
			if(found)
				resultBalls.add(ball);
		} //for

		return resultBalls;
	}

	/********************************************************************************
	 * This method filters any ball which is superset of any other ball
	 * @param matchGraphs the result of tight simulation in the center->ball format
	 * @return filtered set of results
	 */
	public static Set<Ball> filterMatchGraphs(Set<Ball> matchGraphs) {
		Set<Ball> filteredGraphs = new HashSet<Ball>();
		
		Iterator<Ball> it = matchGraphs.iterator();
		while(it.hasNext()) {
			Ball theBall = it.next();
			it.remove();
			matchGraphs.remove(it);
			boolean good = true;
			for(Ball aBall : matchGraphs) {
				if(theBall.contains(aBall)) {
					good = false;
					break;
				}
			} //for
			if(good)
				filteredGraphs.add(theBall);
		} //while
		return filteredGraphs;
	}

	/*************************************************************************************************
	 * A method to print the match centers. Just for debugging purposes. 
	 */
	public static void printMatch(Set<Ball> matchGraphs){
		System.out.println("Number of Match Graphs: " + matchGraphs.size());
		for(Ball b : matchGraphs){
			System.out.println(b);
		}
		System.out.println("---------------------");
	}
	
	/**
	 * Test main method
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Graph g = new Graph("exampleGraphs/G_tight1.txt");
		SmallGraph q = new SmallGraph("exampleGraphs/Q_tight1.txt");
		
		System.out.println("Result of tight simulation without post-processing:");
		long startTime = System.currentTimeMillis();
		Set<Ball> tightSim = getTightSimulation(g, q);
		long stopTime = System.currentTimeMillis();
		printMatch(tightSim);
		System.out.println("Spent time: " + (stopTime - startTime) + "ms");
		
		System.out.println("Result of tight simulation after post-processing:");
		startTime = System.currentTimeMillis();
		Set<Ball> filteredTightSim = filterMatchGraphs(tightSim);
		stopTime = System.currentTimeMillis();
		printMatch(filteredTightSim);
		System.out.println("Spent time: " + (stopTime - startTime) + "ms");
	} //main

} //class
