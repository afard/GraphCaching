package com.satya.simulation;
/*
 * For experiments with any java stuff
 */

import com.satya.graph.common.Ball;
import com.satya.graph.common.Graph;
import com.satya.graph.common.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
		
		
		
		//System.out.println(filterSet.lowerEntry(key));
	}

}
