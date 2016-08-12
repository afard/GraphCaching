/*
 * An MIT style license:
 * 
 * Written by Arash Fard and Satya Vikas under supervision of Dr. Lakshmish Ramaswamy and Dr. John A. Miller.
 * 
 * Copyright (c) 2014, The University of Georgia
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
 * THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package cache;

import graph.common.SmallGraph;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/*
 * A comparator to sort the map for frequency-usage or time-usage of polytrees 
 */
public class FrequencyUsage {
	Map<SmallGraph, Long> fuMap = null;
	TreeMap<SmallGraph, Long> fuMap_sorted = null;
	
	/**
	 * Constructor
	 * @param maxSize the maximum size of cache
	 * @throws Exception
	 */
	public FrequencyUsage(int maxSize) throws Exception {
		if(maxSize <= 0)
			throw new Exception("maxSize must be a positive integer");
		fuMap = new HashMap<SmallGraph, Long>(maxSize);
	}
	
	/**
	 * private comparator class for sorting the fuMap by values
	 * @author arash
	 *
	 */
	private class ValueComparator implements Comparator<SmallGraph>{
		Map<SmallGraph,Long> base;

		public ValueComparator(Map<SmallGraph,Long> base) {
			this.base = base;
		}

		@Override
		public int compare(SmallGraph g1, SmallGraph g2) {
			if (base.get(g1) <= base.get(g2)) {
				return -1;
			} else {
				return 1;
			}			
		}
	} //ValueComparator
	
	/**
	 * Adds a polytree to the frequency map
	 * @param polytree
	 */
	public void addEntry(SmallGraph polytree) {
		if(fuMap.get(polytree) == null) {
			fuMap.put(polytree, 1L);
		} else {
			Long fr = fuMap.get(polytree);
			fuMap.put(polytree, fr+1);
		}
	} //addEntry
	
	/**
	 * Returns the least frequently used polytree, and removes it from its fuMap
	 * @return Returns the least frequently used polytree, and removes it
	 */
	public SmallGraph pollLeast() {
		fuMap_sorted = new TreeMap<SmallGraph, Long>(new ValueComparator(fuMap));
		fuMap_sorted.putAll(fuMap);
		Map.Entry<SmallGraph, Long> smallest = fuMap_sorted.pollFirstEntry();
		SmallGraph removingPolytree = smallest.getKey();
		fuMap.remove(removingPolytree);
		
		return removingPolytree;
	}
	
	/*
	 * Test method
	 */
	public static void main(String[] args) throws Exception {
		SmallGraph g1 = new SmallGraph("exampleGraphs/G1.txt");
		SmallGraph g2 = new SmallGraph("exampleGraphs/G2.txt");
		SmallGraph g3 = new SmallGraph("exampleGraphs/G_tight1.txt");
		FrequencyUsage fuReplacement = new FrequencyUsage(4);
		
		fuReplacement.addEntry(g1);
		fuReplacement.addEntry(g2);
		fuReplacement.addEntry(g3);
		fuReplacement.addEntry(g1);
		fuReplacement.addEntry(g3);
		
		SmallGraph polledG = fuReplacement.pollLeast();	
		
		System.out.println(polledG);
	} //main
} //class
