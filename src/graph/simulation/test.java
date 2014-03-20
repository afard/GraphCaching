package graph.simulation;
/*
 * For experiments with any java stuff
 */

import graph.common.Ball;
import graph.common.Graph;
import graph.common.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;


// THIS IS THE TEST COMMENT
public class test {
	public static void main(String[] args) {
		Set<Integer> nodesInBall1 = new HashSet<Integer>();
		nodesInBall1.add(0);
		nodesInBall1.add(1);
		nodesInBall1.add(2);
		nodesInBall1.add(3);
		//nodesInBall1.add(5);
		
		 Map<Integer,Set<Integer>> dualSimSet = new HashMap<Integer,Set<Integer>>();
		//  dualSimSet.put(0,new Set(440));
		
		Set<Integer> nodesInBall2 = new HashSet<Integer>();
		nodesInBall2.add(0);
		nodesInBall2.add(1);
		nodesInBall2.add(2);
		nodesInBall2.add(3);
		//nodesInBall2.add(4);
		
		//Set<Integer> newSet = (nodesInBall2) | (nodesInBall1);
		
		if((nodesInBall2).containsAll(nodesInBall1)){
			System.out.println("node2 is subset of node1");
		}else{
			System.out.println("node2 is  NOT subset of node1");
		}
	
		Map<Integer, Integer> myMap = new HashMap<Integer, Integer>();
		myMap.put(1, 2);
		myMap.put(1, 3);
		System.out.println("*******"+myMap.get(1));
		
		
		TreeMap<Integer, Integer> filterSet = new TreeMap<Integer, Integer>(Collections.reverseOrder());
		
		filterSet.put(1,1);
		filterSet.put(2,2);
		filterSet.put(3,3);
		filterSet.put(4,4);
		filterSet.put(5,5);
		String ballstr = "0->[1,2,],1->[],2->[],4->[2,],";
		String[] str = ballstr.replaceAll("[\\[\\]\\-\\>,]*", " ").replaceAll("  ", ",").replaceAll(" ","").split(",");
		Set<String> mySet = new HashSet<String>(Arrays.asList(str));
		System.out.println(mySet);
		
		Set<Integer> centerList = new HashSet<Integer>(); // Doing this to find the vertices from query Selectivity 
		Integer[] cList = {1, 684, 817, 816, 556, 130, 153, 154, 18, 696, 561, 145, 264, 265, 516, 33, 649, 310, 525, 916, 520, 186, 290, 909, 905, 667, 540, 297, 181, 896, 343, 751, 475, 336, 618, 197, 760, 761, 868, 639, 375, 714, 717, 580, 719, 110, 862, 116, 732, 114, 974, 490, 367, 604};
		centerList.addAll(Arrays.asList(cList));
		System.out.println("******"+centerList.size());
		
		
		//System.out.println(filterSet.lowerEntry(key));
	}

}
