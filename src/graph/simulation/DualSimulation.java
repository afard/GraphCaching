/**
 * 
 */
/**
 * @author Satya
 *
 */
// *************Code available in Git*********//test
package graph.simulation;

//import gps.examples.gm.gpsPageRank.gpsPageRank.gpsPageRankVertex;
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

		Map<Integer, Set<Integer>> sim = new HashMap<Integer, Set<Integer>> ();

		for (int i = 0 ; i < query.getNumVertices() ; i++ ) {
			Set<Integer> temp = graph.getLabelIndex().get(query.getLabel(i));
			if ( temp != null)
				sim.put(i, new HashSet<Integer> (temp)); 
			else
				sim.put(i, new HashSet<Integer> ());
		}
		
		boolean flag = true;
		int var=-1;

		// runs the dual simulation algorithm
		while (flag) {
			flag = false;
			for ( int u = 0 ; u < query.getNumVertices() ; u++ ) {
				List<Integer> simArr = new ArrayList<Integer> (sim.get(u));
				for ( int w1 = 0 ; w1 < simArr.size(); w1++ ) {
					int w = simArr.get(w1);
					for ( int v: query.getNeighbors(u)) {
						Set<Integer> gpost = new HashSet<Integer> (graph.post(w));
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
					for ( int v: query.getParentIndex().get(u)) {
						Set<Integer> gpre = null;
						if (!graph.getParentIndex().containsKey(w))
							gpre = new HashSet<Integer> ();
						else
							gpre = new HashSet<Integer> (graph.parentIndex.get(w));

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


		Graph g = new Graph("/Users/Satya/Desktop/datagraph.txt");
		Graph q = new Graph("/Users/Satya/Desktop/query.txt");
		//			g.getAllIds();
		q.getAllIds();
		DualSimulation sim = new DualSimulation();		


		System.out.println("The DUAL SIM set is "+sim.getDual(g,q));


	}
}
