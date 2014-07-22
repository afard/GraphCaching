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
