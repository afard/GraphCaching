
/**************************************************************************
 * @author Satya
 *
 */
// *************Code available in Git*********//test
package graph.simulation;

import graph.common.Graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*****************************************************************
 * Runs the sequential dual simulation on the ball
 * @param graph The graph that we want to run dual simulation on 
 * @param graphLabels The labels for the graph
 * @param query The query graph  
 * @return The sim set
 * 
 */

public class DualSimulation{

	/*****************************************************************
	 * Runs the sequential dual simulation on the ball
	 * @param graph The Data Graph 
	 * @param query The Query Graph  
	 * @return The Dual sim set
	 */
	public static Map<Integer, Set<Integer>>  getDual(Graph graph, Graph query) {

		//System.out.println("entered inside.....");

		Map<Integer, Set<Integer>> sim = new HashMap<Integer, Set<Integer>> ();
		//long stop1  = System.currentTimeMillis();
		if (graph.labelIndex == null) {
//			System.out.println("caluculating the label Index...");
//			System.out.println();
			graph.getLabelIndex();
			
		}		
		//long start1  = System.currentTimeMillis();
//		System.out.println("Step1-- Label Index  :"+ (stop1-start1)+" ms");
//		System.out.println();
		

		//long start2  = System.currentTimeMillis();
		for (int i = 0 ; i < query.getNumVertices() ; i++ ) {
			//Set<Integer> temp = graph.getLabelIndex().get(query.getLabel(i));
			Set<Integer> temp = graph.labelIndex.get(query.getLabel(i));
			if ( temp != null)
				sim.put(i, new HashSet<Integer> (temp)); 
			else
				sim.put(i, new HashSet<Integer> ());
		}
		//long stop2  = System.currentTimeMillis();
		//System.out.println("Step2 :"+ (stop2-start2)+" ms");

		boolean flag = true;
		int var=-1;
		//System.out.println("Finished filling the initial sim");
		//System.out.println();
		// runs the dual simulation algorithm
		
		//long start3  = System.currentTimeMillis();
		
		while (flag) {
			flag = false;
			
			for ( int u = 0 ; u < query.getNumVertices() ; u++ ) {
				List<Integer> simArr = new ArrayList<Integer> (sim.get(u));
				for ( int w1 = 0 ; w1 < simArr.size(); w1++ ) {
					int w = simArr.get(w1);
					for ( int v:query.post(u))  {
						Set<Integer> gpost = null;
						//Seq
//						if(graph.childIndex==null){
//							gpost = new HashSet<Integer> (graph.post(w)); //flip back and forth between the 2 statements based on the experiments,
//						}else{
							gpost = new HashSet<Integer> (graph.childIndex.get(w));//TEST CASE A   //because the Graph from induced subgraph does not support the pre and post methods acc to the graph datastructure.
						//}
						//Testcase
						Set<Integer> sub = sim.get(v);
						if ( sub == null )
							sub = new HashSet<Integer> ();
						gpost.retainAll(sub);
						if (gpost.isEmpty()) {
							sim.get(u).remove(w);
							simArr.remove(w1);
							flag = true;
							w1--;
							break;
						}
					} // for v
				} // for w
			} // for u
			
			

			//if (graph.parentIndex == null) graph.getParentIndex();
			
			for ( int u = 0 ; u < query.getNumVertices() ; u++ ) {
				List<Integer> simArr = new ArrayList<Integer> (sim.get(u));
				for ( int w1 = 0 ; w1 < simArr.size() ; w1++ ) {
					int w = simArr.get(w1);
					for ( int v: query.pre(u)) {
						Set<Integer> gpre = null;
						//						if (!graph.parentIndex.containsKey(w))
						//							gpre = new HashSet<Integer> ();
						//						else
//						if(graph.parentIndex==null){
//							gpre = new HashSet<Integer> (graph.pre(w)); // Seq
//						}else{
							
							gpre = new HashSet<Integer>(graph.parentIndex.get(w)); //TestCaseA
						//}
						Set<Integer> sub = sim.get(v);
						if ( sub == null )
							sub = new HashSet<Integer> ();
						gpre.retainAll(sub);
						if (gpre.isEmpty()) {
							sim.get(u).remove(w);
							simArr.remove(w1);
							flag = true;
							w1--;
							break;
						}
					} // for v
				} // for w
			} // for u			
		}
		
		//long stop3  = System.currentTimeMillis();
		//System.out.println("Step3 :"+ (stop3-start3)+" ms");
		return sim;
	}	

	public static void main (String[] args) {

		System.out.println("Started.....");
		System.out.println();
		long readingStart = System.currentTimeMillis();

//		Graph g = new Graph("/Users/Satya/Desktop/g.txt");
//		Graph q = new Graph("/Users/Satya/Desktop/q.txt");

//		Graph g = new Graph("/Users/Satya/Desktop/thesis/satya-graphs/B-G-1M-100L-500D.txt");
//		Graph q = new Graph("/Users/Satya/Desktop/thesis/satya-graphs/B-Q-10N-5L.txt");
		
		Graph g = new Graph("/Users/Satya/Desktop/datagraph.txt");
		Graph q = new Graph("/Users/Satya/Desktop/query.txt");


		long readingStop = System.currentTimeMillis();
		System.out.println("Time to read the Graphs: "+(readingStop-readingStart)+" ms");
		System.out.println();

		long start = System.currentTimeMillis();
		g.getAllIds();
		System.out.println("g.getAllIds()");
		g.getParentIndex1();
		System.out.println("g.getParentIndex1()");
		g.getLabelIndex();
		//System.out.println("g.getLabelIndex()");
		g.getChildIndex();
		System.out.println("g.getChildIndex()");
		//q.getAllIds();
		long stop = System.currentTimeMillis();
		System.out.println("Time to calculate from the Graphs: "+(stop-start)+" ms");
		System.out.println();


		DualSimulation sim = new DualSimulation();		

		long dualStart = System.currentTimeMillis();
		System.out.println("The DUAL SIM set is "+sim.getDual(g,q));
		System.out.println();
		long dualStop = System.currentTimeMillis();
		System.out.println("Time to compute DUAL SIM is : "+(dualStop-dualStart)+" ms");
		System.out.println("DONE");
	}
}
