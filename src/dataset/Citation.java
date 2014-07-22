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
package dataset;

import graph.common.SmallGraph;
import graph.query.QueryGenerator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

/*
 * Reading Citation dataset and creating a compatible data graph
 * The dataset from http://arnetminer.org/citation
 */
public class Citation {

	public static void main(String[] args) throws Exception {
		String inputFile  = args[0];
		String outputFile = args[1];
		SmallGraph theRawGraph = new SmallGraph();
			
		FileInputStream fstream = new FileInputStream(inputFile);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String strLine;

		int currentLabel = 0;
		int currentIndex = 0;
		//First round - finding the labels
		while ((strLine = br.readLine()) != null) { 
			if(strLine.startsWith("#t")) { // a new paper
				currentLabel = Integer.parseInt((strLine.substring(2)).trim());				
			} else if (strLine.startsWith("#index")) {
				currentIndex = Integer.parseInt((strLine.substring(6)).trim());
				theRawGraph.vertices.put(currentIndex, new HashSet<Integer>());
				theRawGraph.labels.put(currentIndex, currentLabel);
			} else if (strLine.startsWith("#%") && strLine.trim().length() > 2) {
				int ref = Integer.parseInt((strLine.substring(2)).trim());
				theRawGraph.vertices.get(currentIndex).add(ref);
			}			
		}//while
		br.close();
		in.close();
		
		if(theRawGraph.vertices.containsKey(null))
			throw new Exception("null in the vertices of theRawGraph");
		for(Set<Integer> neighbors : theRawGraph.vertices.values()) {
			if(neighbors.contains(null))
				throw new Exception("null in the neighbors of theRawGraph");
		}
		SmallGraph g = QueryGenerator.arrangeID(theRawGraph);
		if(g.vertices.containsKey(null))
			throw new Exception("null in the vertices of g");
		for(Set<Integer> neighbors : g.vertices.values()) {
			if(neighbors.contains(null))
				throw new Exception("null in the neighbors of g");
		}
		g.print2File(outputFile);
	}//main
}//class
