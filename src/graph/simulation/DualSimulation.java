
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


public class DualSimulation{

	/*****************************************************************
	 * Runs the sequential dual simulation.
	 * @param graph The Data Graph 
	 * @param query The Query Graph  
	 * @return The Dual sim set
	 */
	public static Map<Integer, Set<Integer>>  getDual(Graph graph, Graph query) {



		Map<Integer, Set<Integer>> sim = new HashMap<Integer, Set<Integer>> ();

		if (graph.labelIndex == null) {		
			graph.getLabelIndex();			
		}	

		for (int i = 0 ; i < query.getNumVertices() ; i++ ) {

			Set<Integer> temp = graph.labelIndex.get(query.getLabel(i));
			if ( temp != null)
				sim.put(i, new HashSet<Integer> (temp)); 
			else
				sim.put(i, new HashSet<Integer> ());
		}


		boolean flag = true;
		int var=-1;		

		while (flag) {
			flag = false;

			for ( int u = 0 ; u < query.getNumVertices() ; u++ ) {
				List<Integer> simArr = new ArrayList<Integer> (sim.get(u));
				for ( int w1 = 0 ; w1 < simArr.size(); w1++ ) {
					int w = simArr.get(w1);
					for ( int v:query.post(u))  {
						Set<Integer> gpost = null;						
						gpost = new HashSet<Integer> (graph.childIndex.get(w));						
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

			for ( int u = 0 ; u < query.getNumVertices() ; u++ ) {
				List<Integer> simArr = new ArrayList<Integer> (sim.get(u));
				for ( int w1 = 0 ; w1 < simArr.size() ; w1++ ) {
					int w = simArr.get(w1);
					for ( int v: query.pre(u)) {
						Set<Integer> gpre = null;
						gpre = new HashSet<Integer>(graph.parentIndex.get(w));						
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
		g.getParentIndex();
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
