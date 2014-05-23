package cache;

import graph.common.SmallGraph;
import graph.simulation.DualSimulation;

import java.util.HashSet;
import java.util.Map;
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
	 * @param sig
	 * @return
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
	 * @param candidate
	 * @param polytree
	 * @return true if it is dual-cover-match; false otherwise
	 */
	public static boolean isDualCoverMatch(SmallGraph newQuery, SmallGraph polytree) {
		Map<Integer, Set<Integer>> dualSim = DualSimulation.getDualSimSet(newQuery, polytree);
		Set<Integer> dualSimSet = DualSimulation.nodesInSimSet(dualSim);
		int nVerticesInQ = newQuery.getNumVertices();
		if(dualSimSet.size() == nVerticesInQ)
			return true;
		else
			return false;
	} //isDualCoverMatch

} //class
