package graph.common;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.Set;

import org.javatuples.Pair;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import static java.lang.System.out;

/** This class holds any graph in a 2D array. Named as Graph since this will be used for query graphs in GPS
 *  @author Usman Nisar, Arash Fard
 */
public class Graph {

	/*************************************************************
	 * The main data structure that holds all the graph information. The key is
	 * the id of the vertex, the first (zero-eth) column holds the label of
	 * vertex and the rest of entries in the column are outgoing edges to other
	 * vertices.
	 */
	public int[][] vertices = null;
	public int[] allIds = null;

	private int diameter = -1;
	private int radius = -1;
	public Map<Integer, Set<Integer>> labelIndex = null;
	public Map<Integer, Set<Integer>> parentIndex = null;
	public Map<Integer, Set<Integer>> childIndex = null;

	public static GraphMetrics grM = null;

	/*************************************************************
	 * Default constructor for the class
	 */
	public Graph() {
	}

	/*************************************************************
	 * Auxiliary constructor
	 * @param size The number of vertices in the graph. This value should be equal to the highest vertex id
	 */
	public Graph(int size) {
		vertices = new int[size][];
	}


	/*************************************************************
	 * Auxiliary constructor
	 * @param filePath The path to read the file from
	 */
	public Graph(String filePath) {

		try {
			// Open the file that is the first 
			// command line parameter
			FileInputStream fstream = new FileInputStream(filePath);

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			String strLine;

			// first pass: get the vertex with the maximum value
			int max = -1;
			while ((strLine = br.readLine()) != null) {
				int val = Integer.parseInt((strLine.split(" ")[0]));
				if (val > max) {
					max = val;
				}
			}

			// initialize the main array 
			vertices = new int[++max][];

			// Close the input stream
			in.close();

			fstream = new FileInputStream(filePath);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));

			//Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// System.out.println (strLine);
				String[] splits = strLine.split(" ");
				int index = Integer.parseInt(splits[0]);
				vertices[index] = new int[splits.length - 1];

				for (int i = 1; i < splits.length; i++) {
					vertices[index][i - 1] = Integer.parseInt(splits[i]);
				}
			}

			//Close the input stream
			in.close();



		} // try
		catch (Exception e) {//Catch exception if any

		} //catch
	}

	//	/*************************************************************
	//	 * Gets a hashmap storing the values from the labels to the ids of the vertices
	//	 * @return A hashmap where K is the label, and V is a set of all ids with K as their label 
	//	 */
	//	public Map<Integer, Set<Integer>> getLabelIndex() {
	//		if (labelIndex == null) {
	//			labelIndex = new HashMap<Integer, Set<Integer>>();
	//			//int[] allIds = getAllIds();
	//			getAllIds();
	//			for (int i = 0; i < allIds.length; i++) {
	//				if (labelIndex.get(getLabel(allIds[i])) == null) {
	//					Set<Integer> vSet = new HashSet<Integer>();
	//					vSet.add(allIds[i]);
	//					labelIndex.put(getLabel(allIds[i]), vSet);
	//				} else {
	//					labelIndex.get(getLabel(allIds[i])).add(allIds[i]);
	//				}
	//			}
	//		}
	//		return labelIndex;
	//	}

	//	/*************************************************************
	//	 * Gets an adjacency list representation of the parents of vertices
	//	 * @return A hashmap where K is the vertex id, and V is a set of all parent ids for K
	//	 */
	//	public Map<Integer, Set<Integer>> getParentIndex() {
	//
	//		if (parentIndex == null) {
	//			parentIndex = new HashMap<Integer, Set<Integer>>();
	//			for (int id = 0 ; id < allIds.length ; id++){
	//				Set<Integer> pSet = new HashSet<Integer>();
	//				for (int i = 0; i < vertices.length; i++) {
	//					if (vertices[i] != null) {
	//						for (int j = 1; j < vertices[i].length; j++) {
	//							//System.out.println("**"+vertices[i].length);
	//							if (vertices[i][j] == id) {
	//								pSet.add(i);
	//							}
	//						}
	//					}
	//				}
	//
	//				parentIndex.put(id, pSet);
	//			}
	//		}
	//		return parentIndex;
	//	}
	/*************************************************************
	 * Gets the Map of vertex and its outgoing edges for all vertices in the graph
	 * 
	 * @return Map<Integer, Set<Integer>> A  Map of outgoing edges from each vertex of the graph
	 */
	public Map<Integer, Set<Integer>> getChildIndex() {
		childIndex  = new HashMap<Integer, Set<Integer>> ();
		//System.out.println("The vertices length is: "+ vertices.length);
		for(int i =0;i<vertices.length;i++){
			childIndex.put(i, post(i));	
		}

		return childIndex;
	}



	/*************************************************************
	 * Calculates the centers of the graph using ecc
	 * @return Set<Integer> centers of the undirected graph
	 */
	public Set<Integer> getCenters() {
		if(grM == null)
			grM = new GraphMetrics(vertices);
		return grM.central();
	} // getCenters

	/*************************************************************
	 * Calculates the radius of the graph using ecc
	 * @return int The radius of the undirected graph
	 */
	public int getRadius() {
		if(grM == null)
			grM = new GraphMetrics(vertices);
		return grM.rad();        
	} // getRadius

	/*************************************************************
	 * Calculates the diameter of the graph using ecc
	 * @return int The diameter of the undirected graph
	 */
	public int getDiameter() {
		if(grM == null)
			grM = new GraphMetrics(vertices);
		return grM.diam();                
	} // getDiameter
	/*************************************************************
	 * Gets the label of the vertex
	 * @param id Id of the vertex
	 * @return int Label of the vertex, -1 if not present
	 */
	public int getLabel(int id) {
		try {
			return vertices[id][0];
		} catch (java.lang.NullPointerException ex) {

			return -1;
		}
	}
	/*************************************************************
	 * Gets a map of vertex id and its corresponding label
	 * @return A HashMap where K is the vertex id and V is the Label of the vertex
	 */
	public Map<Integer, Integer> getLabelMap() {

		Map<Integer,Integer> labelMap = new HashMap<Integer,Integer>();

		for(int i=0;i<vertices.length;i++){
			if (vertices[i] != null) {
				labelMap.put(i, vertices[i][0]);
			}
		}

		return labelMap;
	}

	/*************************************************************
	 * Sets the label of the vertex
	 * @param id Id of the vertex
	 * @param lab Label of the vertex
	 */
	public void setLabel(int id, int lab) {
		vertices[id][0] = lab;
	}

	/*************************************************************
	 * Adds/replaces a vertex to the graph with its associated label and its
	 * outgoing edges
	 * @param id Id of the vertex
	 * @param lab Label of the vertex
	 * @param outgoing An array that corresponds to the children vertices
	 */
	public void addEntry(int id, int lab, int[] outgoing) {
		vertices[id] = new int[outgoing.length + 1];
		setLabel(id, lab);
		setNeighbors(id, outgoing);
	}

	/*************************************************************
	 * Sets the outgoing edges of the given vertex id. Label stays the same
	 * @param id Id of the vertex
	 * @param outgoing An array that corresponds to the outgoing edges
	 */
	public void setNeighbors(int id, int[] outgoing) {
		int lab = vertices[id][0];
		vertices[id] = new int[outgoing.length + 1];
		vertices[id][0] = lab;
		for (int i = 0; i < outgoing.length; i++) {
			vertices[id][i + 1] = outgoing[i];
		}
	}

	/*************************************************************
	 * Gets the outgoing edges of the given vertex id
	 * @param id Id of the vertex
	 * @return int[] An array of outgoing edges from the given vertex
	 */
	public int[] getNeighbors(int id) {
		int[] arr = new int[vertices[id].length - 1];
		for (int i = 1; i < vertices[id].length; i++) {
			arr[i - 1] = vertices[id][i];
		}
		return arr;
	}

	/*************************************************************
	 * Gets the outgoing edges of the given vertex id
	 * @param id Id of the vertex
	 * @return Set<Integer> A set of outgoing edges from the given vertex
	 */
	public Set<Integer> post(int id) {
		int[] arr = getNeighbors(id);
		return Utils.convertArrayToHashSet(arr);
		//return getChildIndex().get(id);
	}	



	/*************************************************************
	 * Gets the number of vertices that can be in the graph
	 * @return int Size of the graph. This value would equal the highest vertex id in the graph
	 */
	public int getNumVertices() {
		if (vertices == null) {
			return -1;
		} else {
			return vertices.length;
		}
	}

	/*************************************************************
	 * Gets the ids of vertices that have incoming edges to the vertex id
	 * @param id The id of the vertex
	 * @return int[] An array of vertices that have incoming edges to the vertex id
	 */
	public int[] getParentIds(int id) {
		ArrayList<Integer> arr = new ArrayList<Integer>();
		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i] != null) {
				for (int j = 1; j < vertices[i].length; j++) {
					if (vertices[i][j] == id) {
						arr.add(i);
					}
				}
			}
		}
		return Utils.convertArrayListToInteger(arr);
	}

	/*************************************************************
	 * Gets the incoming edges of the given vertex id
	 * @param id Id of the vertex
	 * @return Set<Integer> A set of incoming edges from the given vertex
	 */
	public Set<Integer> pre(int id) {
		int[] arr = getParentIds(id);
		return Utils.convertArrayToHashSet(arr);
		//return getParentIndex().get(id);
	}	

	/*************************************************************
	 * Verifies that all children of the vertex id exist in the childrenMatch
	 * @param id The id of the vertex. We test its neighbors against childLabels
	 * @param childrenMatch A list of ids that should be superset of id neighbors
	 * @return boolean Returns true if its a complete match, false otherwise
	 */
	public boolean containsAllChildren(int id, int[] childrenMatch) {
		int[] childrenInQ = getNeighbors(id);
		boolean flag = arraySubSetOfAnArray(childrenInQ, childrenMatch);
		return flag;
	}

	/*************************************************************
	 * Verifies that all the labels of all neighbors of the vertex id exist in the childLabels
	 * @param id The id of the vertex. We test its neighbors against childLabels
	 * @param childLabels A list of labels that should be matched with id neighbors
	 * @return boolean Returns true if its a complete match, false otherwise
	 */
	public boolean containsAllLabelsinChildren(int id, int[] childLabels) {
		int[] childrenInQ = getNeighborLabels(id);
		boolean flag = arraySubSetOfAnArray(childrenInQ, childLabels);
		return flag;
	}

	/*************************************************************
	 * Returns the labels of all the parents of the vertex id
	 * @param id The id of the vertex. We test its neighbors against childLabels
	 * @return int[] Returns all the labels in an array of integers
	 */
	private int[] getParentLabels(int id) {
		ArrayList<Integer> labels = new ArrayList<Integer>();
		int[] pids = getParentIds(id);
		for (int i = 0; i < pids.length; i++) {
			labels.add(getLabel(pids[i]));
		}
		return Utils.convertArrayListToInteger(labels);
	}

	/*************************************************************
	 * Verifies that all the labels of all the parents of the vertex id exist in the parentLabels
	 * @param id The id of the vertex. We test its parent's labels against parentLabels
	 * @param parentLabels A list of labels that should be matched with id parentIds
	 * @return boolean Returns true if its a complete match, false otherwise
	 */
	public boolean containsAllLabelsInParents(int id, int[] parentLabels) {
		int[] parentsInQ = getParentLabels(id);
		boolean flag = arraySubSetOfAnArray(parentsInQ, parentLabels);
		return flag;
	}

	/*************************************************************
	 * A private method that makes sure the first array is a subset of second
	 * @param first The first array
	 * @param second The second array
	 * @return boolean Returns true if all the entries in first exist in second, false otherwise
	 */
	public boolean arraySubSetOfAnArray(int[] first, int[] second) {
		boolean flag = false;

		// iterate through all the vertices in Q
		for (int i = 0; i < first.length; i++) {
			flag = false;

			// look for the current vertex in the data graph
			for (int j = 0; j < second.length; j++) {
				if (second[j] == first[i]) {
					flag = true;
					break;
				}
			}
			if (!flag) {
				return false;
			}
		}
		return true;
	}

	/*************************************************************
	 * Returns the labels of all neighbors of vertex id
	 * @param id The id of the vertex
	 * @return int[] Returns an array of labels
	 */
	public int[] getNeighborLabels(int id) {
		int[] arr = new int[vertices[id].length - 1];
		for (int i = 1; i < vertices[id].length; i++) {
			arr[i - 1] = getLabel(vertices[id][i]);
		}
		return arr;
	}

	/*************************************************************
	 * Computes all the vertex Ids in the graph. Helper method for getAllIds
	 */
	private void computeAllIds() {
		int[] temp = new int[vertices.length];
		int j = 0;
		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i] != null) {
				temp[j++] = i;
			}
		}

		// shrink the array
		allIds = new int[j];
		System.arraycopy(temp, 0, allIds, 0, j);
	}

	/*************************************************************
	 * Returns all the vertex Ids in the graph
	 * @return int[] Returns an array of Ids
	 */
	public int[] getAllIds() {
		if (allIds == null) {
			computeAllIds();
		}
		return allIds;
	}

	/*************************************************************
	 * Returns the size of the graph in bytes
	 * @return int Size of the graph
	 */
	public int getSizeInBytes() {
		int sum = 0;
		sum += (vertices.length * 4);
		for (int i = 0; i < vertices.length; i++) {
			if (vertices[i] != null) {
				sum += (vertices[i].length * 4);
			}
		}
		return sum;
	}

	/*************************************************************
	 * Dumps the graph on console. Useful for debugging
	 */
	public void display() {
		System.out.println("***************");
		for (int i = 0; i < vertices.length; i++) {
			System.out.print(i + " ");
			if (vertices[i] != null) {
				for (int j = 0; j < vertices[i].length; j++) {
					System.out.print(vertices[i][j] + " ");
				}
			}
			System.out.println(" ");
		}
	}

	//	/*************************************************************
	//	 * Get the label of all the vertices in the ball
	//	 * @param the ball which is a Map of vertices and their outgoing edges 
	//	 * @param the parent graph
	//	 * @return A map which of vertices and their corresponding labels
	//	 */
	//	public Map<Integer,Integer> getBallLabels(Map<Integer,Set<Integer>> ball, Graph g){
	//		Map<Integer,Integer> ballLabels = new HashMap<Integer,Integer>();
	//		for(int id:ball.keySet()){
	//			ballLabels.put(id, g.getLabel(id)); 
	//		}
	//		return ballLabels;
	//	}

	//::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::
	/********************************************************************************
	 *  Return the vertex from an array of central vertices, those which have 
	 *  highest adjacent set size and lowest frequency of label in the query graph i.e
	 *  highest ratio.
	 *  @param the set of centers from the query Graph
	 *  @return a single vertex which satisfies the condition
	 */
	public int selectivityCriteria(Set<Integer> Centers){
		Double ratio = 0.0;
		int index = 0;
		Double max = 0.0;
		getChildIndex();
		getLabelIndex();	
		for(int cen: Centers){			
			ratio = (double) (childIndex.get(cen).size()) / (double)(labelIndex.get(getLabel(cen)).size());
			if(max<ratio){
				max = ratio;
				index = cen;
			}
		}
		return index;
	}
	/********************************************************************************
	 * Method to Print a Graph which prints the indices of the graph.
	 */

	public void print(){
		System.out.println("The Graph is:");
		System.out.println();
		System.out.println("The Adjacency List is:");
		if(childIndex ==null)getChildIndex();
		for(Entry<Integer, Set<Integer>> entry: childIndex.entrySet()){
			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		}
		System.out.println();
		System.out.println("The Parent List is:");
		if(parentIndex ==null)getParentIndex();
		for(Entry<Integer, Set<Integer>> entry: parentIndex.entrySet()){
			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		}
		System.out.println();
		System.out.println("The Label Map is:");
		if(labelIndex==null)getLabelIndex();
		for(Entry<Integer,Set<Integer>> entry: labelIndex.entrySet()){
			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		}

	}

	/********************************************************************************
	 * Auxiliary method to obtain an INDUCED SUBGRAPH from the dataGraph.
	 * @param Set of Vertices to be input as the candidate vertices.
	 * 
	 */
	public Graph inducedSubgraph1(Set<Integer> set){
		Map<Integer,Set<Integer>> adjSet = new HashMap<Integer,Set<Integer>>();
		Map<Integer,Set<Integer>> parList = new HashMap<Integer,Set<Integer>>();
		Map<Integer,Set<Integer>> newLabelIndex = new HashMap<Integer,Set<Integer>>();


		for(int node:set){
			Set<Integer> temp = new HashSet<Integer>();
			if(childIndex ==null) getChildIndex();
			for(int u : childIndex.get(node)){					
				if(set.contains(u)){
					temp.add(u);
				}					
			}
			adjSet.put(node, temp);
		}


		for(int node:set){
			Set<Integer> temp = new HashSet<Integer>();
			if(parentIndex ==null) getParentIndex();
			for(int v : parentIndex.get(node)){
				if(set.contains(v)){
					temp.add(v);
				}
			}
			parList.put(node, temp);
		}


		for (int w:set) {
			if (newLabelIndex.get(getLabel(w)) == null) {
				Set<Integer> vSet = new HashSet();
				vSet.add(w);
				newLabelIndex.put(getLabel(w), vSet);
			} else {
				newLabelIndex.get(getLabel(w)).add(w);
			}
		}

		//PRINTING THE GRAPH - WE CAN USE THE PRINT METHOD TOO.
		//		System.out.println("The Adjacency List is:");
		//		for(Entry<Integer,Set<Integer>> entry: adjSet.entrySet()){
		//			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		//		}
		//		System.out.println();
		//
		//		System.out.println("The Parent List is:");
		//		for(Entry<Integer,Set<Integer>> entry: parList.entrySet()){
		//			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		//			
		//		}
		//		
		//		System.out.println("The Label Index is:");
		//		for(Entry<Integer,Set<Integer>> entry: newLabelIndex.entrySet()){
		//			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		//			
		//		}



		Graph subGraph = new Graph();
		subGraph.childIndex = adjSet;
		subGraph.parentIndex = parList;
		subGraph.labelIndex = newLabelIndex;

		return subGraph;

	}



	/********************************************************************************
	 * Method to obtain a INDUCED SUBGRAPH from the dataGraph.
	 * @param Set of Vertices which are the candidate vertices.
	 * @return A Graph object which is the SugGraph induced by the input vertices.	 
	 */
	public Graph inducedSubgraph(Set<Integer> set){
		Map<Integer,Set<Integer>> adjSet = new HashMap<Integer,Set<Integer>>();
		Map<Integer,Set<Integer>> parList = new HashMap<Integer,Set<Integer>>();
		Map<Integer,Set<Integer>> newLabelIndex = new HashMap<Integer,Set<Integer>>();


		for(int node:set){
			Set<Integer> temp = new HashSet<Integer>();
			//if(childIndex ==null) getChildIndex();
			for(int u : post(node)){					
				if(set.contains(u)){
					temp.add(u);
				}					
			}
			adjSet.put(node, temp);
		}

		for(int node:set){
			Set<Integer> temp = new HashSet<Integer>();
			//if(parentIndex ==null) getParentIndex();
			for(int v : pre(node)){
				if(set.contains(v)){
					temp.add(v);
				}
			}
			parList.put(node, temp);
		}


		for (int w:set) {
			if (newLabelIndex.get(getLabel(w)) == null) {
				Set<Integer> vSet = new HashSet();
				vSet.add(w);
				newLabelIndex.put(getLabel(w), vSet);
			} else {
				newLabelIndex.get(getLabel(w)).add(w);
			}
		}

		//PRINTING THE GRAPH - WE CAN USE THE PRINT METHOD TOO.
		//		System.out.println("The Adjacency List is:");
		//		for(Entry<Integer,Set<Integer>> entry: adjSet.entrySet()){
		//			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		//		}
		//		System.out.println();
		//
		//		System.out.println("The Parent List is:");
		//		for(Entry<Integer,Set<Integer>> entry: parList.entrySet()){
		//			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		//			
		//		}
		//		
		//		System.out.println("The Label Index is:");
		//		for(Entry<Integer,Set<Integer>> entry: newLabelIndex.entrySet()){
		//			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		//			
		//		}



		Graph subGraph = new Graph();
		subGraph.childIndex = adjSet;
		subGraph.parentIndex = parList;
		subGraph.labelIndex = newLabelIndex;

		return subGraph;

	}


	/*************************************************************
	 * Gets an adjacency list representation of the parents of vertices
	 * @return A hashmap where K is the vertex id, and V is a set of all parent ids for K
	 */
	public Map<Integer, Set<Integer>> getParentIndex() {

		//getAllIds();
		parentIndex  = new HashMap<Integer, Set<Integer>> ();

		for(int j=0;j<childIndex.size();j++){
			//for(int j=0;j<allIds.length;j++){
			parentIndex.put(j, new HashSet<Integer>());
		}
		//System.out.println("the size of parent Index is: "+parentIndex.size());		

		//for(int i = 0;i<allIds.length;i++){		
		for(int i=0;i<childIndex.size();i++){
			//for(int j:post(i)){

			for(int j:childIndex.get(i)){
				parentIndex.get(j).add(i);
			}
		}


		return parentIndex;
	}

	/*************************************************************
	 * A Test ChildIndex method for tuning.
	 */
	public Map<Integer, Set<Integer>> getChildIndex1() {

		childIndex  = new HashMap<Integer, Set<Integer>> ();
		for(int i = 0;i < allIds.length; i++){
			Set<Integer> temp = new HashSet<Integer>();
			for(int j =1;j<vertices[i].length;j++){
				temp.add(vertices[i][j]);
			}

			//			for(int j : vertices[i]){				
			//				temp.add(j);				
			//			}
			childIndex.put(i, temp);
		}		
		return childIndex;

	}
	/*************************************************************
	 * Gets a hashmap storing the values from the labels to the ids of the vertices
	 * @return A hashmap where K is the label, and V is a set of all ids with K as their label 
	 */
	public Map<Integer, Set<Integer>> getLabelIndex() {
		if (labelIndex == null) {
			labelIndex = new HashMap<Integer, Set<Integer>>();
			//int[] allIds = getAllIds();
			//			getAllIds();
			//			for (int i = 0; i < allIds.length; i++) {
			for (int i = 0; i < childIndex.size(); i++) {
				if (labelIndex.get(getLabel(i)) == null) {
					Set<Integer> vSet = new HashSet<Integer>();
					vSet.add(i);
					labelIndex.put(getLabel(i), vSet);
				} else {
					labelIndex.get(getLabel(i)).add(i);
				}
			}
		}
		return labelIndex;
	}

	/********************************************************************************
	 * Get the Graph Signature, which is a list of pairs of vertex labels of the edges. 
	 * @return A List of Pairs of Integers where each pair constitute the vertex labels of an edge in the graph.
	 */

	public List<Pair<Integer,Integer>> getSignature(){

		List<Pair<Integer,Integer>> sig = new ArrayList<Pair<Integer,Integer>>();

		for(int i = 0;i<allIds.length;i++){


			for(int j : childIndex.get(i)){

				Pair p = new Pair<Integer,Integer>(getLabel(i),getLabel(j));
				sig.add(p);

			}

		}	
		return sig;		
	}

	/**********************************************************************
	 * Auxiliary method which gives the Undirected version of a Graph.
	 * 
	 * @return The undirected version of the Graph, which is an adjacency list representation of the graph, 
	 * 		   in this case a Map where K is a vertex and V is set of all edges both incoming and outgoing. 
	 */
	public Map<Integer,Set<Integer>> getUndirected(){
		Map<Integer,Set<Integer>> undirectedG =  new HashMap<Integer, Set<Integer>>();
		int n = allIds.length;
		for(int i = 0; i < n ; i++) {
			Set<Integer> aSet = new HashSet<Integer>();
			undirectedG.put(i, aSet);
		}

		for(int i = 0; i < n ; i++) {
			for(int j = 1; j < vertices[i].length; j++) { // the first int is the lable
				if(vertices[i][j] >= n) {
					System.out.println("Error: the input graph structure is not complete");
				}
				undirectedG.get(i).add(vertices[i][j]);
				undirectedG.get(vertices[i][j]).add(i);
			} // for
		}
		return undirectedG;

	}

	/**********************************************************************
	 * The Main method which gets the Undirected version of a Graph.
	 * 
	 * @return The undirected version of the Graph, essentially a new Graph object.
	 */
	public Graph getUndirectedGraph(){
		Map<Integer,Set<Integer>> adjList =  new HashMap<Integer, Set<Integer>>();
		int n = allIds.length;
		for(int i = 0; i < n ; i++) {
			Set<Integer> aSet = new HashSet<Integer>();
			adjList.put(i, aSet);
		}

		for(int i = 0; i < n ; i++) {
			for(int j = 1; j < vertices[i].length; j++) { // the first int is the lable
				if(vertices[i][j] >= n) {
					System.out.println("Error: the input graph structure is not complete");
				}
				adjList.get(i).add(vertices[i][j]);
				adjList.get(vertices[i][j]).add(i);
			} // for
		}
		//System.out.println(adjList);
		Graph undirGraph = new Graph();
		undirGraph.childIndex = adjList;
		undirGraph.parentIndex = undirGraph.getParentIndex();
		undirGraph.labelIndex = getLabelIndex();
		return undirGraph;

	}

	/********************************************************************************
	 * 
	 * @param The original graph from which the Polytree is to be found.
	 * @param center
	 * @return The Graph Object which is a Polytree extracted from the original graph.
	 */

	public Graph getPolytree(Graph g, int center){
		// ***** This is BFS traversal. ******** 
		Map<Integer,Set<Integer>> adjList = new HashMap<Integer, Set<Integer>>();
		int n = g.allIds.length;
		boolean[] visited = new boolean[n];

		for(int i = 0; i < n; i++){
			adjList.put(i, new HashSet<Integer>());
		}
		Graph ug = g.getUndirectedGraph();

		Queue<Integer> q = new LinkedList<Integer>();

		q.add(center);
		visited[center] = true;
		while(!q.isEmpty()){
			int node = q.poll();
			System.out.println("The visited node is "+node);
			for(int child : ug.childIndex.get(node)){
				if(!visited[child]){
					visited[child] = true;
					q.add(child);
					adjList.get(node).add(child);

				}

			}
		}

		Map<Integer,Set<Integer>> newAdj = new HashMap<Integer, Set<Integer>>();
		for(int i = 0; i < n; i++){
			newAdj.put(i, new HashSet<Integer>());
		}
		
		// ***** FIXING THE EDGE DIRECTIONS *****

		for(int i = 0 ; i<adjList.size(); i++){
			for(int j:adjList.get(i)){
				if(g.childIndex.get(i).contains(j)){
					newAdj.get(i).add(j);
					//newAdj.get(i).add(j);
				}
				else if (!g.childIndex.get(i).contains(j)) {
					newAdj.get(j).add(i);


				}
			}
		}


		System.out.println(adjList);
		System.out.println();
		System.out.println(newAdj);

		Graph polyTree  = new Graph();
		polyTree.childIndex = newAdj;
		polyTree.parentIndex = polyTree.getParentIndex();
		polyTree.labelIndex = g.labelIndex;

		return polyTree; 

	}




	public static void main (String[] args) {
		System.out.println("***** STARTED *****");
		System.out.println();
		//Graph g = new Graph("/Users/Satya/Desktop/datagraph.txt");
		Graph g = new Graph("/Users/Satya/Desktop/testGraph.txt");

		g.getAllIds();
		g.getChildIndex();
		//System.out.println(g.childIndex);
		//
		//		long start0 = System.currentTimeMillis();
		//
		//		//g.getLabelIndex();
		//		//g.getParentIndex();
		////				for(Entry<Integer, Set<Integer>> entry: g.parentIndex.entrySet()){
		////					//System.out.println(entry.getKey()+" ---> "+entry.getValue());
		////				}		
		//		long stop0 = System.currentTimeMillis();
		//		System.out.println("Time to find PARENT index 0 is: "+(stop0-start0)+" ms" );
		//		System.out.println();
		//		System.out.println();
		//		
		//		
		//		
		//		long start10 = System.currentTimeMillis();
		//		//g.getLabelIndex1();
		//		g.getParentIndex1();
		////				for(Entry<Integer, Set<Integer>> entry: g.parentIndex.entrySet()){
		////					//System.out.println(entry.getKey()+" ---> "+entry.getValue());
		////				}		
		//		long stop10 = System.currentTimeMillis();
		//		System.out.println("Time to find PARENT index 1 is: "+(stop10-start10)+" ms" );
		//
		//
		////		long start = System.currentTimeMillis();
		////		
		////		for(Entry<Integer, Set<Integer>> entry: g.parentIndex.entrySet()){
		////			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		////		}	
		////		long stop = System.currentTimeMillis();
		////		System.out.println("Time to find parent Index 2 is: "+(stop-start)+" ms" );
		////		System.out.println();
		//
		//		//		long start1 = System.nanoTime();			
		//		//		System.out.println("the children are: "+g.childIndex.get(49999));
		//		//		long stop1 = System.nanoTime();
		//		//		System.out.println("Time to find through indices is: "+(stop1-start1)+" nanosecs" );
		//
		////		g.getChildIndex();
		////		g.getLabelIndex();
		//				System.out.println("The size of Original Graph is: "+ g.allIds.length+" nodes");
		//				System.out.println();
		//				Integer[] aList = {3, 2, 1, 10, 0, 5, 4, 9, 13, 14, 11, 12};		
		//				Set<Integer> vertexSet = new HashSet<Integer>();
		//				vertexSet.addAll(Arrays.asList(aList));
		//				long findSubGraphStart = System.currentTimeMillis();
		//				System.out.println("vertexSet: "+vertexSet);
		//		
		//				Graph subGraph = g.inducedSubgraph1(vertexSet);
		//				
		//				long findSubGraphStop = System.currentTimeMillis();
		//				
		//				System.out.println("Time to find INDUCED SUBGRAPH: "+(findSubGraphStop-findSubGraphStart)+"ms");
		//				System.out.println();
		//				System.out.println("The size of Induced SubGraph is: "+subGraph.childIndex.size()+" nodes");
		//				System.out.println();
		//				System.out.println("***** DONE *****");
		//				subGraph.print();

		//		Ball b = new Ball(subGraph,0,1);			//(Ball( graph, center, radius)
		//		System.out.println();
		//		System.out.println(b.getBallAsString());
		//		
		//		Ball bigBall  = new Ball(g,0,1);
		//		System.out.println();
		//		System.out.println(bigBall.getBallAsString());




		//		GraphMetrics qMet = new GraphMetrics(q.vertices);
		//out.println("-------------------------------------------"+g.parentIndex.get(0));
		//		System.out.println("the centers are "+qMet.central());
		//		System.out.println("the vertices from selectivity criteria are"+q.selectivityCriteria(qMet.central()));
		//	g.getLabelIndex();
		//	g.getAllIds();

		//		out.println("BALL");
		//		out.println("-------------------------------------------");

		//g.getBall(g, 3,2 ); //graph, center , radius
		//System.out.println("THE BALL LABELS ARE:----"+g.getBallLabels(g.getBall(g, 3,2 ), g));

		//		g.display();
		//
		//		System.out.println(g.getLabelMap()); 
		//		System.out.println(g.getLabelIndex());
		//		//System.out.println(g.getAllIds());
		//		System.out.println(g.getParentIndex());
		//		System.out.println(g.getChildIndex());
		//		System.out.println("the diameter is:"+g.getDiameter());
		//		
		//		long ballStartTime = System.nanoTime();	
		//		Ball ball = new Ball(g,3,2);
		//		long ballStopTime = System.nanoTime();	
		//		System.out.println("Ball Creation Time: " + (ballStopTime-ballStartTime)/1000000.0+" ms");
		//		System.out.println();
		//		System.out.println("the BALL is "+ball.getBallAsString());
		////		System.out.println();
		////		System.out.println("The parent Index is "+g.parentIndex);

		//		for(Entry<Integer, Integer> entry: g.getSignature().entrySet()){
		//		System.out.println(entry.getKey()+" ---> "+entry.getValue());
		//	}
		//		Graph uG = g.getUndirectedGraph();
		//		uG.print();
		Graph g1 = new Graph("/Users/Satya/Desktop/testGraph1.txt");
		g1.getAllIds();
		g1.getChildIndex();
		
		(g.getPolytree(g1, 5)).print();


		System.out.println("DONE");
	}
}
