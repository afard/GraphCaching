package graph.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
/*************************************************************
 * @author Satya
 *This is the class that holds the ball. 
 */
public class Ball {
	public Set<Integer> nodesInBall = null;
	public Set<Integer> borderNodes = null;
	public Map<Integer, Set<Integer>> adjSet = null;
	Map<Integer, Set<Integer>> parList = null;	
	public Map<Integer,Integer> ballLabelIndex = null;
	Map<Integer, Integer> distance = null;	// This is  LinkedHashMap similar to the queue used in BFS
	public int ballCenter = 0;


	/*************************************************************
	 * Default constructor for the class
	 */
	public Ball() {
	}
	/*
	 * Constructor for creating the ball when the graph , center of the ball, and radius are passed
	 *  
	 */
	public Ball(Graph graph, int center, int radius){
		if(nodesInBall == null && borderNodes == null && adjSet == null && parList == null && distance == null && ballLabelIndex ==null){
			adjSet = new HashMap<Integer, Set<Integer>> ();
			parList = new HashMap<Integer, Set<Integer>> ();
			borderNodes = new HashSet<Integer>();
			nodesInBall = new HashSet<Integer>();
			
			//linked Hash Map in this case functions as a QUEUE.
			//Used a structure to hold the vertex-depth pair.			
			distance = new LinkedHashMap<Integer, Integer> (); 															
			ballLabelIndex = new HashMap<Integer,Integer>();
		}
		int depth = 0;
		ballCenter = center;	
		distance.put(center, 0);
		nodesInBall.add(center);
//		adjSet.put(center, graph.post(center));		
//		parList.put(center, graph.pre(center));//Uncomment all these and comment the ones below if the graph is not populated
		if(graph.childIndex==null)graph.getChildIndex();
		adjSet.put(center, graph.childIndex.get(center));
		
		if(graph.parentIndex==null)graph.getParentIndex();
		parList.put(center, graph.parentIndex.get(center));
		
		while (!distance.isEmpty()){
			Entry<Integer, Integer> vdPair = distance.entrySet().iterator().next();			
			distance.remove(vdPair.getKey());
			int nextV = vdPair.getKey();
			depth = vdPair.getValue();
			
			if(vdPair.getValue() == radius){				
				borderNodes.add(vdPair.getKey());
			}			
			else{
				//Set<Integer> children = graph.post(nextV);
				if(graph.childIndex==null)graph.getChildIndex();
				Set<Integer> children = graph.childIndex.get(nextV);
				//Set<Integer> parents = graph.pre(nextV);
				//if(graph.parentIndex==null)graph.getParentIndex();
				Set<Integer> parents = graph.parentIndex.get(nextV);
				

				for(int child:children){
					if(!nodesInBall.contains(child)){
					
						nodesInBall.add(child);
						distance.put(child, depth+1);						
					}
				}

				for(int parent:parents){
					if(!nodesInBall.contains(parent)){						
						nodesInBall.add(parent);
						distance.put(parent, depth+1);						
					}
				}
			}
		}		
		for (int node:nodesInBall){
			
			ballLabelIndex.put(node, graph.getLabel(node));
			Set<Integer> children = graph.childIndex.get(node);
			Set<Integer> parents = graph.parentIndex.get(node);
			if(!adjSet.containsKey(node)){
				Set<Integer> tempAdj = new HashSet<Integer>();
				adjSet.put(node,tempAdj );				
			}
			if(!parList.containsKey(node)){
				Set<Integer> tempPar = new HashSet<Integer>();
				parList.put(node, tempPar);
			}
			for(int child:children){
				if(nodesInBall.contains(child)){
					if(!parList.containsKey(child)){
						Set<Integer> pre = new HashSet<Integer>();
						pre.add(node);
						parList.put(child, pre);
					}else 
						if (parList.containsKey(child)){
							(parList.get(child)).add(node);
						}
				}			
			}
			for(int parent :parents){
				if(nodesInBall.contains(parent)){
					if(!adjSet.containsKey(parent)){
						Set<Integer> post = new HashSet<Integer>();
						post.add(node);
						adjSet.put(parent,post);
					}else			
						if(adjSet.containsKey(parent)){
							(adjSet.get(parent)).add(node);
						}
				}
			}
		}		
	}
	
	public Map<Integer, Set<Integer>> getBall(){
		return adjSet;

	}
	public Set<Integer> post(int id){
		return adjSet.get(id);

	}
	public Set<Integer> pre(int id){
		return parList.get(id);		
	}	
	
	/****************************************************
	 * A method to return the ball as a string
	 * @paramthe adjSet of the ball
	 * @return the ball in a string format
	 */
	
	public String getBallAsString() {
        StringBuilder s = new StringBuilder();
        if (adjSet != null) {
            List<Integer> keys = new ArrayList<Integer> (adjSet.keySet());
            Collections.sort (keys);
            for (Integer i : keys) {
                s.append(i + "->[");
                List<Integer> values = new ArrayList<Integer> (adjSet.get(i));
                Collections.sort (values);
                for (Integer j: values) 
                    s.append(j+",");
                s.append("],");
            }
        }
        return s.toString();
    }
	
	/****************************************************
	 * A method to return the diameter of the ball
	 * @return The diameter of the ball.
	 */
	
	public int getBallDiameter(){

		int n = nodesInBall.size()+1;
		int[][] path = new int[n][n];
		for(int u:nodesInBall){
			for(int v:nodesInBall){
				if(u!=v){
					if(post(u).contains(v)){
						path[v][u] = 1;
						path[u][v] = 1;
					}
				}
			}
		}
		int diameter = 0;
		for(int k:nodesInBall){
			for(int i:nodesInBall){
				for(int j:nodesInBall){
					if(i!=j){
						if(path[i][k] * path[k][j]!=0){
							if((path[i][k]+path[k][j]<path[i][j]) ||path[i][j]==0 ){
								path[i][j] = (path[i][k]+path[k][j]);
							}
							diameter = Math.max(path[i][j], diameter);
						}
					}
				}
			}
		}

		return diameter;

	}
	
	public static void main (String[] args) {
		Graph g = new Graph("/Users/Satya/Desktop/datagraph.txt");
		g.getChildIndex();
		g.getAllIds();
		g.getParentIndex();
		//Ball ball = new Ball(g,4,3);
		int[] centers = {5,6,8};
		for(int i:centers){
			System.out.println(i+"---"+(new Ball(g,i,1)).nodesInBall);
			System.out.println();
		}
		
//		Graph g1 = new Graph("/Users/Satya/Desktop/testGraph.txt");
//		g1.getChildIndex();
//		g1.getAllIds();
//		g1.getParentIndex();
//		Ball b = new Ball(g1,0,1);
//		System.out.println(b.getBallAsString());
		
//		System.out.println("BALL DIAMETER = "+ball.getBallDiameter());
//		System.out.println();
//		System.out.println(ball.getBall());
//		System.out.println();
//		System.out.println("THE BORDER NODES ARE: "+ ball.borderNodes);
//		System.out.println();
//		System.out.println("THE LABELS OF THE NODES ARE: "+ ball.ballLabelIndex);
//		System.out.println();
//		System.out.println(ball.getBallAsString());
		
		
		
	}
}