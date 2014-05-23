package graph.common;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** 
 *  It is a class for large graphs. The vertex id of vertices must be sequential numbers. 
 *  @author Arash Fard, Satya.
 */
public class Graph {

	/*************************************************************
	 * The main data structures that holds all the graph information.
	 */
	int[][] adj = null; // the adjacency list of the graph
	int[] label = null; // the array of labels for vertices

	private Map<Integer, Set<Integer>> labelIndex = null;

	private int[][] parent = null; // it remains null by default
	private Set<Integer>[] parentIndex = null; // it remains null by default

//	private Set<Integer>[] childIndex = null; // it will be filled gradually

	/*************************************************************
	 * Auxiliary constructor
	 * @param size The number of vertices in the graph. This value should be equal to the highest vertex id
	 */
	public Graph(int size) {
		this.adj = new int[size][];
		this.label = new int[size];
	}

	/*************************************************************
	 * Auxiliary constructor
	 * @param filePath The path to read the file from
	 */
	public Graph(String filePath) throws Exception {

		try {
			FileInputStream fstream = new FileInputStream(filePath);

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;

			// first pass: get the vertex with the maximum value
			int max = -1;

			while ((strLine = br.readLine()) != null) {

				String[] splits = strLine.split("\\s+");
				int val = Integer.parseInt((splits[0]));
				if (val < 0) {
					throw new Exception("vertex id must be an integer bigger than 0");
				}
				if (val > max) {
					max = val;
				}
			}

			// Close the input stream
			br.close();
			in.close();

			// initialize the main array (the vertex id starts from 0)   
			this.adj = new int[++max][];
//			this.childIndex = (Set<Integer>[]) new Set<?>[max];
			this.label = new int[max];
			System.out.println("Number of vertices in " + filePath + ": " + max);

			fstream = new FileInputStream(filePath);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			//Read File Line By Line
			while ((strLine = br.readLine()) != null) { // each line belongs to one vertex

				String[] splits = strLine.split("\\s+");
				int index = Integer.parseInt(splits[0]); // the first integer is the id of the vertex
				this.adj[index] = new int[splits.length - 2];

				this.label[index] =  Integer.parseInt(splits[1]); // the label of the vertex

				for (int i = 2; i < splits.length; i++) { // the id of the children of the vertex
					this.adj[index][i - 2] = Integer.parseInt(splits[i]);
				}
			} //while

			//Close the input stream
			br.close();
			in.close();
		} // try
		catch (Exception e) {//Catch exception if any
			throw new Exception(e);
		} //catch
	}

	/*************************************************************
	 * Builds a HashMap storing the values from the labels to the ids of the vertices
	 * stores the resulted HashMap in labelIndex field variable
	 */
	public void buildLabelIndex() {
		if (labelIndex == null) {
			labelIndex = new HashMap<Integer, Set<Integer>>();
			for (int i = 0; i < label.length; i++) {
				if (labelIndex.get(label[i]) == null) {
					Set<Integer> vSet = new HashSet<Integer>();
					vSet.add(i);
					labelIndex.put(label[i], vSet);
				} else {
					labelIndex.get(label[i]).add(i);
				}
			}
		}
	}

	/*************************************************************
	 * Builds an adjacency list for reverse graph (to retrieve parent of vertices)
	 * the result is stored in parent field
	 */
	@SuppressWarnings("unchecked")
	public void buildParentIndex() {

		if (parent == null && parentIndex == null) {
			parentIndex = (Set<Integer>[]) new Set<?>[adj.length];;
			for (int id = 0 ; id < adj.length ; id++)
				parentIndex[id] = new HashSet<Integer>();
//			System.out.println("The sets are created!");
			for (int id = 0; id < adj.length; id++) {
				if (adj[id] != null) {
					for (int child : adj[id])
						parentIndex[child].add(id);
				}
			}
		}
	}

	/*************************************************************
	 * Builds an adjacency list for reverse graph (to retrieve parent of vertices) from a file
	 * the result is stored in parent field
	 * @param fileName	the name of file containing reverse graph
	 * @throws Exception
	 */
	public void buildParentIndex(String fileName) throws Exception {

		if (parent == null) {
			parent = new int[adj.length][];
			try {
				FileInputStream fstream = new FileInputStream(fileName);

				// Get the object of DataInputStream
				DataInputStream in = new DataInputStream(fstream);
				BufferedReader br = new BufferedReader(new InputStreamReader(in));

				String strLine;

				//Read File Line By Line
				while ((strLine = br.readLine()) != null) { // each line belongs to one vertex

					String[] splits = strLine.split("\\s+");
					int index = Integer.parseInt(splits[0]); // the first integer is the id of the vertex
					this.parent[index] = new int[splits.length - 2];

					for (int i = 2; i < splits.length; i++) { // the id of the children of the vertex
						this.parent[index][i-2] = Integer.parseInt(splits[i]);
					}
				} //while

				//Close the input stream
				br.close();
				in.close();
			} // try
			catch (Exception e) {//Catch exception if any
				throw new Exception(e);
			} //catch
		} //if
	}

	/*************************************************************
	 * Gets the label of the vertex
	 * @param id Id of the vertex
	 * @return int Label of the vertex, -1 if not present
	 */
	public int getLabel(int id) {
		try {
			return this.label[id];
		} catch (java.lang.NullPointerException ex) {
			System.err.println(ex.getMessage());
			return -1;
		}
	}

	/*************************************************************
	 * Sets the label of the vertex
	 * @param id Id of the vertex
	 * @param lab Label of the vertex
	 */
	public void setLabel(int id, int lab) {
		try {
			this.label[id] = lab;
		} catch (java.lang.NullPointerException ex) {
			System.err.println(ex.getMessage());
			System.exit(1);
		}
	}

	/*************************************************************
	 * Gets the set of vertices which have the same given label
	 * @return A HashMap where K is the vertex id and V is the Label of the vertex
	 */
	public Set<Integer> getVerticesLabeled(int label) {
		if (labelIndex == null)
			buildLabelIndex();

		return this.labelIndex.get(label);
	}

	/*************************************************************
	 * Sets the outgoing edges of the given vertex id. Label stays the same
	 * @param id Id of the vertex
	 * @param outgoing An array that corresponds to the outgoing edges
	 */
	public void setNeighbors(int id, int[] outgoing) throws Exception {
		if (id > adj.length -1) {
			throw new Exception("id: " + id + "is out of range");
		}
		adj[id] = outgoing;
	}

	/*************************************************************
	 * Gets the outgoing edges of the given vertex id
	 * @param id Id of the vertex
	 * @return Set<Inetegr> The Set of outgoing edges from the given vertex
	 */
//	@SuppressWarnings("unchecked")
	public Set<Integer> post(int id) {
		if (id > adj.length -1) // the first id is 0
			return null;
		if (adj[id] == null)
			return new HashSet<Integer>();

//		if (this.childIndex == null)
//			childIndex = (Set<Integer>[]) new Set<?>[adj.length];
//		
//		if (this.childIndex[id] == null) {
			Set<Integer> children = new HashSet<Integer>(adj[id].length);
			for (int i = 0; i < adj[id].length; i++)
				children.add(adj[id][i]);
//			this.childIndex[id] = children;
//		}
//
//		return this.childIndex[id];
			return children;
	}

	/*************************************************************
	 * Gets the number of vertices in the graph
	 * @return int The number of vertices in the graph. This value would equal the highest vertex id in the graph
	 */
	public int getNumVertices() {
		return this.adj.length;
	}

	/*************************************************************
	 * Gets the ids of parents of the given vertex id
	 * @param id The id of the vertex
	 * @return Set<Inetegr> The Set of incoming edges to the given vertex
	 */
	public Set<Integer> pre(int id) {
		if (id > adj.length -1) // the first id is 0
			return null;
		if (parent == null) {
			buildParentIndex();
			return parentIndex[id];
		}
		
		Set<Integer> par;
		if(parent[id] != null) {
			par = new HashSet<Integer>(parent[id].length);
			for(int i : parent[id])
				par.add(i);
		} else {
			par = new HashSet<Integer>();
		}
		
		return par;
	}

	/*************************************************************
	 * Dumps the graph on console. Useful for debugging
	 */
	public void display() {
		System.out.println("***************");
		for (int i = 0; i < adj.length; i++) {
			System.out.print(i + " (");
			System.out.print(label[i] + ") ");
			if (adj[i] != null) {
				System.out.print("[");
				int j = 0;
				for (; j < adj[i].length - 1; j++)
					System.out.print(adj[i][j] + ", ");
				if (adj[i].length != 0) System.out.print(adj[i][j]);
				System.out.println("]");
			}
		}
	}

	/********************************************************************************
	 * Method to Print a Graph
	 */
	public String toString(){
		return ("The Graph has " + adj.length + " vertices.");
	}

	/**
	 * Test main method
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		Graph g = new Graph("exampleGraphs/G_tight1.txt");
		System.out.println(g);
		g.display();

		System.out.println();
		Set<Integer> subset = new HashSet<Integer>();
		subset.add(8); subset.add(9); subset.add(10); subset.add(11); subset.add(12); subset.add(13); subset.add(14); subset.add(15); 
		SmallGraph sg = GraphUtils.inducedSubgraph(g, subset);
		System.out.println(sg);
		sg.display();
	}

} // class
