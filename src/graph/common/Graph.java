package graph.common;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

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

	/*************************************************************
	 * Gets a hashmap storing the values from the labels to the ids of the vertices
	 * @return A hashmap where K is the label, and V is a set of all ids with K as their label 
	 */
	public Map<Integer, Set<Integer>> getLabelIndex() {
		if (labelIndex == null) {
			labelIndex = new HashMap<Integer, Set<Integer>>();
			int[] allIds = getAllIds();
			for (int i = 0; i < allIds.length; i++) {
				if (labelIndex.get(getLabel(allIds[i])) == null) {
					Set<Integer> vSet = new HashSet<Integer>();
					vSet.add(allIds[i]);
					labelIndex.put(getLabel(allIds[i]), vSet);
				} else {
					labelIndex.get(getLabel(allIds[i])).add(allIds[i]);
				}
			}
		}
		return labelIndex;
	}

	/*************************************************************
	 * Gets an adjacency list representation of the parents of vertices
	 * @return A hashmap where K is the vertex id, and V is a set of all parent ids for K
	 */
	public Map<Integer, Set<Integer>> getParentIndex() {

		if (parentIndex == null) {
			parentIndex = new HashMap<Integer, Set<Integer>>();
			for (int id = 0 ; id < allIds.length ; id++){
				Set<Integer> pSet = new HashSet<Integer>();
				for (int i = 0; i < vertices.length; i++) {
					if (vertices[i] != null) {
						for (int j = 1; j < vertices[i].length; j++) {
							//System.out.println("**"+vertices[i].length);
							if (vertices[i][j] == id) {
								pSet.add(i);
							}
						}
					}
				}

				parentIndex.put(id, pSet);
			}
		}
		return parentIndex;
	}
	/*************************************************************
	 * Gets the Map of vertex and its outgoing edges for all vertices in the graph
	 * 
	 * @return Map<Integer, Set<Integer>> A  Map of outgoing edges from each vertex of the graph
	 */
	public Map<Integer, Set<Integer>> getChildIndex() {
		childIndex  = new HashMap<Integer, Set<Integer>> ();
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

	//	/******************************************************************
	//	 * Creates a ball on a given vertex taking the radius and the graph as radius
	//	 * @param graph the parent graph on which the balls are created
	//	 * @param radius the radius of the ball
	//	 * @param id the vertex id which is the center of the ball
	//	 * @return A map of vertex ids and their corresponding adjacency lists
	//	 */
	//
	//	public Map<Integer, Set<Integer>> getBall(Graph graph, int center, int radius) {
	//		//Map<Integer, Set<Integer>> ball = new HashMap<Integer, Set<Integer>> ();
	//
	//		Map<Integer, Set<Integer>> adjSet = new HashMap<Integer, Set<Integer>> ();
	//		Map<Integer, Set<Integer>> parList = new HashMap<Integer, Set<Integer>> ();
	//		Set<Integer> borderNodes = new HashSet<Integer>();
	//		Set<Integer> nodesInBall = new HashSet<Integer>();
	//		Queue<Integer> qu = new LinkedList<Integer> ();
	//		Map<Integer, Integer> distance = new LinkedHashMap<Integer, Integer> ();
	//		int n = vertices.length;
	//		//boolean[] go = new boolean[n];
	//		//int[] len = new int[n];
	//		int depth = 0;
	//		distance.clear();
	//		distance.put(center, 0);
	//		nodesInBall.add(center);
	//
	//		adjSet.put(center, graph.post(center));
	//		parList.put(center, graph.pre(center));
	//		//System.out.println(parList);
	//		//		qu.clear();
	//		//		qu.offer(center);
	//		while (!distance.isEmpty()){
	//			Entry<Integer, Integer> vdPair = distance.entrySet().iterator().next();			
	//			distance.remove(vdPair.getKey());
	//			int nextV = vdPair.getKey();
	//			depth = vdPair.getValue();
	//			System.out.println("the next vertex "+nextV);
	//			out.println("the depth "+vdPair.getValue());	
	//			System.out.println("---------");				//System.out.println();
	//			if(vdPair.getValue()==radius){
	//				System.out.println("entered bordernodesloop");
	//				borderNodes.add(vdPair.getKey());
	//			}else{
	//				Set<Integer> children = graph.post(nextV);
	//				System.out.println("the children of"+nextV+"are"+children);
	//				Set<Integer> parents = graph.pre(nextV);
	//				System.out.println("the parents of"+nextV+"are"+parents);
	//
	//				for(int child:children){
	//					if(!nodesInBall.contains(child)){
	//						System.out.println("entered children loop");
	//						nodesInBall.add(child);
	//						distance.put(child, depth+1);
	//						//System.out.println("THE DEPTH IS "+depth);
	//						System.out.println("---------DISTANCE"+distance);	
	//					}
	//
	//				}
	//
	//				for(int parent:parents){
	//					if(!nodesInBall.contains(parent)){
	//						System.out.println("entered parents loop");
	//						nodesInBall.add(parent);
	//						distance.put(parent, depth+1);
	//						System.out.println("---------DISTANCE"+distance);
	//					}
	//
	//				}
	//
	//			}
	//		}
	//		System.out.println(nodesInBall);
	//		System.out.println("***********************");
	//		for (int node:nodesInBall){
	//			System.out.println("^^^"+node);
	//			Set<Integer> children = graph.post(node);
	//			Set<Integer> parents = graph.pre(node);
	//			if(!adjSet.containsKey(node)){
	//				Set<Integer> tempAdj = new HashSet<Integer>();
	//				adjSet.put(node,tempAdj );				
	//			}
	//			if(!parList.containsKey(node)){
	//				Set<Integer> tempPar = new HashSet<Integer>();
	//				parList.put(node, tempPar);
	//			}
	//			for(int child:children){
	//				if(nodesInBall.contains(child)){
	//					if(!parList.containsKey(child)){
	//						Set<Integer> pre = new HashSet<Integer>();
	//						pre.add(node);
	//						parList.put(child, pre);
	//					}else 
	//						if (parList.containsKey(child)){
	//							(parList.get(child)).add(node);
	//						}
	//				}				
	//
	//			}
	//			for(int parent :parents){
	//				if(nodesInBall.contains(parent)){
	//					if(!adjSet.containsKey(parent)){
	//						Set<Integer> post = new HashSet<Integer>();
	//						post.add(node);
	//						adjSet.put(parent,post);
	//					}else			
	//						if(adjSet.containsKey(parent)){
	//							(adjSet.get(parent)).add(node);
	//						}
	//				}
	//			}
	//		return adjSet;
	//	}
	/*************************************************************
	 * Get the label of all the vertices in the ball
	 * @param the ball which is a Map of vertices and their outgoing edges 
	 * @param the parent graph
	 * @return A map which of vertices and their corresponding labels
	 */
	public Map<Integer,Integer> getBallLabels(Map<Integer,Set<Integer>> ball, Graph g){
		Map<Integer,Integer> ballLabels = new HashMap<Integer,Integer>();
		for(int id:ball.keySet()){
			ballLabels.put(id, g.getLabel(id)); 
		}
		return ballLabels;
	}

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
			ratio = (double) (childIndex.get(cen).size() / labelIndex.get(getLabel(cen)).size());
			if(max<ratio){
				max = ratio;
				index = cen;
			}
		}
		return index;
	}
	/********************************************************************************
	 * Method to Print a Graph
	 */

	public void print(){
		System.out.println("The Graph is:");
		System.out.println();
		System.out.println("The Adjacency List is:");
		for(Entry<Integer, Set<Integer>> entry: childIndex.entrySet()){
			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		}
		System.out.println();
		System.out.println("The Label Map is:");
		for(Entry<Integer,Set<Integer>> entry: labelIndex.entrySet()){
			System.out.println(entry.getKey()+" ---> "+entry.getValue());
		}

	}

	/********************************************************************************
	 * Method to obtain a INDUCED SUBGRAPH from the dataGraph.
	 * @param Set of Vertices to be input as the candidate vertices.
	 * 
	 */
	public Graph inducedSubgraph(Set<Integer> set){
		Map<Integer,Set<Integer>> adjSet = new HashMap<Integer,Set<Integer>>();
		Map<Integer,Set<Integer>> parList = new HashMap<Integer,Set<Integer>>();
		Map<Integer,Set<Integer>> newLabelIndex = new HashMap<Integer,Set<Integer>>();


		for(int node:set){
			Set<Integer> temp = new HashSet<Integer>();

			for(int u : childIndex.get(node)){					
				if(set.contains(u)){
					temp.add(u);
				}					
			}
			adjSet.put(node, temp);
		}

		for(int node:set){
			Set<Integer> temp = new HashSet<Integer>();
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





	public static void main (String[] args) {
		Graph g = new Graph("/Users/Satya/Desktop/testGraph.txt");
		g.getAllIds();
		g.getParentIndex();
		g.getChildIndex();
		g.getLabelIndex();
		Set<Integer> set = new HashSet<Integer>();
		set.add(0); 
		set.add(1);
		//set.add(2);
		Graph subGraph = g.inducedSubgraph(set);
		subGraph.print();
		Ball b = new Ball(subGraph,0,1);			//(Ball( graph, center, radius)
		System.out.println();
		System.out.println(b.getBallAsString());
		
		Ball bigBall  = new Ball(g,0,1);
		System.out.println();
		System.out.println(bigBall.getBallAsString());
		



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
	}
}
