package cache;

import graph.common.Ball;
import graph.common.SmallGraph;
import graph.simulation.DualSimulation;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.javatuples.Pair;

public class CacheUtils {
	
	/**
	 * Returns candidate set of polytrees in the cache if any; otherwise, returns null
	 * @param inGraph	the input graph
	 * @param cacheIndex	the cacheIndex
	 * @return the candidate match set of the input graph
	 */
	public static Set<SmallGraph> getCandidateMatchSet(SmallGraph inGraph, Map<Set<Pair<Integer,Integer>>, Set<SmallGraph>> cacheIndex) {
		Set<SmallGraph> matchSet = new HashSet<SmallGraph>();		
		Set<Pair<Integer,Integer>> inSig = inGraph.getSignature();
		Set<Integer> inLabelSig = getSigLabels(inSig);
		
		for(Set<Pair<Integer,Integer>> cSig : cacheIndex.keySet()) {
			Set<Integer> cLabelSig = getSigLabels(cSig);
			if(! inLabelSig.equals(cLabelSig) ) continue;
			if(inSig.containsAll(cSig)) {
				// every signature match should be considered
				matchSet.addAll(cacheIndex.get(cSig));
			} //if
		} //for
		return matchSet;
	} //isCandidateMatch
	
	/**
	 * Returns the set of labels in a graph signature
	 * @param sig a graph signature
	 * @return the set of the labels in the given signature
	 */
	public static Set<Integer> getSigLabels(Set<Pair<Integer,Integer>> sig) {
		Set<Integer> labels = new HashSet<Integer>();
		for(Pair<Integer,Integer> p : sig) {
			labels.add(p.getValue0());
			labels.add(p.getValue1());
		} //for
		return labels;
	} //getSigLabels
	
	/**
	 * Finds if the new query is a dual-cover-match to the polytree 
	 * @param newQuery the new query graph from smallGraph class
	 * @param polytree the polytree
	 * @return true if it is dual-cover-match; false otherwise
	 */
	public static boolean isDualCoverMatch(SmallGraph newQuery, SmallGraph polytree) {
		Map<Integer, Set<Integer>> dualSim = DualSimulation.getNewDualSimSet(newQuery, polytree);
		Set<Integer> dualSimSet = DualSimulation.nodesInSimSet(dualSim);
		int nVerticesInQ = newQuery.getNumVertices();
		if(dualSimSet.size() == nVerticesInQ)
			return true;
		else
			return false;
	} //isDualCoverMatch

	/**
	 * Given a graph (induced subgraph) and its corresponding polytree,
	 * creates and returns all the balls corresponding to its tight balls but without filtering
	 * @param graph the input graph
	 * @param polytree the input polytree
	 * @param the dualSim of the polytree which is already calculated. it is used for finding the center of the balls
	 * @param limit the upper bound for the number of extracted balls
	 * @return the set of balls
	 */
	public static Set<Ball> ballExtractor(SmallGraph graph, SmallGraph polytree, Map<Integer, Set<Integer>> dualSim, int limit) {
		Set<Ball> resultBalls = new HashSet<Ball>();
		if(dualSim.isEmpty()) return resultBalls;
		
		int bRadius = polytree.getDiameter(); // using diameter of the polytree to make sure that the ball will contain any isomorphic match
		int qCenter = polytree.getSelectedCenter();
		Set<Integer> matchCenters = dualSim.get(qCenter);
		
		for(int center : matchCenters){
			Ball ball = new Ball(graph, center, bRadius); // BALL CREATION
			resultBalls.add(ball);
			if(resultBalls.size() == limit) break; // it will never happen when limit=0, so finds all the results
		} //for		
		
		return resultBalls;
	}//ballExtractor
	
	/**
	 * Given a set of balls and a query, it is looking for tight match inside each ball
	 * @param balls the set of balls
	 * @param query the query
	 * @param limit the maximum number of balls which will be considered
	 * @return the set of balls which are are the result of tight match on the input balls
	 */
	public static Set<Ball> tightSimBalls(Set<Ball> balls, SmallGraph query, int limit) {
		Set<Ball> resultBalls = new HashSet<Ball>();
		
		for(Ball b : balls) {
			
//			Set<Ball> resultB = TightSimulation.getNewTightSimulation(b, query, limit);
			Map<Integer,Set<Integer>> dualSimSet = DualSimulation.getNewDualSimSet(b, query);
			if(! dualSimSet.isEmpty()) {
				Ball resultB = b.clone();
				if (resultB.dualFilter(query, dualSimSet))
					resultBalls.add(resultB);
			}
			if(resultBalls.size() >= limit) break;
		} //for
		
		return resultBalls;
	}//tightSimBalls
	
	/**
	 * Shuffles the elements of an array
	 * @param array the input array
	 * @return the shuffled array
	 */
	public static <E> E[] RandomizeArray(E[] array){
		Random rgen = new Random();  // Random number generator			
 
		for (int i=0; i<array.length; i++) {
		    int randomPosition = rgen.nextInt(array.length);
		    E temp = array[i];
		    array[i] = array[randomPosition];
		    array[randomPosition] = temp;
		}
 
		return array;
	} //RandomizeArray
	
	/**
	 * Removes Least Frequently Used element from the cache
	 * @param fu FrequencyUsage object
	 * @param cache map object of cache
	 * @param cacheIndex cache index object
	 * @return the removed polytree
	 */
	public static SmallGraph removeLFU(FrequencyUsage fu, Map<SmallGraph, SmallGraph> cache, 
			Map<Set<Pair<Integer,Integer>>, Set<SmallGraph>> cacheIndex) {
		SmallGraph lfuPolytree = fu.pollLeast();
		cache.remove(lfuPolytree);
		for(Set<SmallGraph> aSet : cacheIndex.values()) {
			aSet.remove(lfuPolytree);
		}
		return lfuPolytree;
	} //removeLFU
	
} //class
