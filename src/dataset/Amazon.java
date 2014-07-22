package dataset;

import graph.common.SmallGraph;
import graph.query.QueryGenerator;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Reading Amazon dataset and creating a compatible data graph
 * The dataset from http://snap.stanford.edu/data/amazon-meta.html
 */
public class Amazon {
	private final static int CATEGORY_DEPTH = 3;

	public static void main(String[] args) throws Exception {
		String inputFile  = args[0];
		String outputFile = args[1];
		SmallGraph theRawGraph = new SmallGraph();
			
		FileInputStream fstream = new FileInputStream(inputFile);
		// Get the object of DataInputStream
		DataInputStream in = new DataInputStream(fstream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));

		String strLine;

		int labelCounter = 0;
		int currentIndex = 0;
		Map<String,Integer> ASIN2Id = new HashMap<String,Integer>();
		Map<Integer,Set<String>> children = new HashMap<Integer,Set<String>>();
		Map<String,Integer> labelMap = new HashMap<String,Integer>();
		labelMap.put("discontinued product", labelCounter); // label 0 is dedicated to "discontinued product"
		labelCounter ++;
		labelMap.put("uncategorized product", labelCounter); // label 1 is dedicated to "uncategorized product"
		labelCounter ++;
		Map<Integer,String> Id2Label = new HashMap<Integer,String>();
		
		//First round - finding the information
		System.out.println("Reading the file");
		while ((strLine = br.readLine()) != null) { 
			strLine = strLine.trim();
			if(strLine.startsWith("Id:")) { // a new product
//				System.out.print("\nId:");
				currentIndex = Integer.parseInt((strLine.substring(3)).trim());				
			} else if (strLine.startsWith("ASIN:")) {
				String currentASIN = (strLine.substring(5)).trim();
				ASIN2Id.put(currentASIN,currentIndex);
			} else if (strLine.equals("discontinued product")) {
				Id2Label.put(currentIndex, "discontinued product");
			} else if (strLine.startsWith("similar:")) {
				String[] tokens = (strLine.substring(8).trim()).split("\\s+");
//				System.out.print("\tsimilar:");
				int nChildren = Integer.parseInt(tokens[0]);
				Set<String> childSet = new HashSet<String>(nChildren);
				for(int i=1; i <= nChildren; i++) {
					childSet.add(tokens[i]);
				}//for
				children.put(currentIndex, childSet);
			} else if (strLine.startsWith("categories:")) {
//				System.out.print("\tcategories:");
				int nCategories = Integer.parseInt((strLine.substring(11)).trim());
				if(nCategories == 0) {
					Id2Label.put(currentIndex, "uncategorized product");
				} else {
					strLine = (br.readLine()).trim();
					String[] tokens = strLine.split("\\|");
					int depth = CATEGORY_DEPTH;
					if (depth > (tokens.length -1)) depth = (tokens.length -1);
					StringBuilder label = new StringBuilder();
					for(int cat=1; cat <= depth; cat++) { // Apparently cat=0 is always empty
						label.append(tokens[cat]);
					}
					if(!labelMap.containsKey(label.toString())) {
						labelMap.put(label.toString(), labelCounter);
						labelCounter ++;
					}
					Id2Label.put(currentIndex, label.toString());
				}
			} //if-else-ladder
		}//while
		br.close();
		in.close();

		System.out.println("Constructing the graph");
		int nInvalidEdges = 0;
		int nValidEdges = 0;
		for(int u : Id2Label.keySet()) {
			if(! Id2Label.containsKey(u))
				throw new Exception("No entry for" + u + "in Id2Label");

			int label = labelMap.get(Id2Label.get(u));
			theRawGraph.labels.put(u, label);
			
			Set<Integer> c_u;
			if(children.containsKey(u)) {
				c_u = new HashSet<Integer>(children.get(u).size());
				for(String child : children.get(u)) {
					if(!ASIN2Id.containsKey(child)) {
//						System.out.println("There is no entry in ASIN2Id for " + child);
						nInvalidEdges ++;
					} else {
						c_u.add(ASIN2Id.get(child));
						nValidEdges ++;
					}
				} //for
			} else {
				c_u = new HashSet<Integer>();
			}
			theRawGraph.vertices.put(u, c_u);
		}//for
		System.out.println("Number of removed invalid edges: " + nInvalidEdges);
		System.out.println("Number of remained edges:" + nValidEdges);
		System.out.println("Number of vertices: " + theRawGraph.getNumVertices());
		System.out.println("Number of labels " + labelMap.size());
		
		// Checking any inserted null 
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
