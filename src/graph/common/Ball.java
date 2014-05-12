package graph.common;

import graph.simulation.DualSimulation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*************************************************************
 * @author Satya , Arash
 *This is the class that holds the ball. 
 */
public class Ball extends SmallGraph {
	public Set<Integer> nodesInBall = null;
	public Set<Integer> borderNodes = null;
	public int ballCenter = 0;
	public int ballRadius = 0;


	/*************************************************************
	 * Default constructor for the class.
	 */
	public Ball() {
	}
	
	/**************************************************************
	 * Constructor for creating the ball when the data graph , center of the ball, and radius are passed.
	 *  
	 */
	public Ball(SmallGraph graph, int center, int radius){
		ballCenter = center;
		ballRadius = radius;
		graph.buildParentIndex();
		borderNodes = new HashSet<Integer>();
		nodesInBall = new HashSet<Integer>();			

        this.len = new HashMap<Integer, Integer>();
		int distance = 0;
		this.qu.clear();
		qu.add(center);
		len.put(center, 0);
		nodesInBall.add(center);

		// finding nodesInBall and borderNodes
		while(! qu.isEmpty()) {			
			int nextVertex = qu.poll();
			distance = len.get(nextVertex);
			if(distance == radius) { 		// nextVertex is a border node
				nodesInBall.add(nextVertex);
				borderNodes.add(nextVertex);
			} else {
				distance ++;
				// the underlying undirected graph should be used
				if(graph.vertices.get(nextVertex) != null) {
					for (int c : graph.vertices.get(nextVertex)) { // for each child of nextVertex
						if(!this.nodesInBall.contains(c)) {  // it means that this vertex is not visited yet
							nodesInBall.add(c);
							len.put(c, distance);            // distance from center to c                
							qu.add(c);                       // put child c in the queue	        			
						} // if
					} // for
				} //if

				if(graph.parentIndex.get(nextVertex) != null) {
					for (int p : graph.parentIndex.get(nextVertex)) { // for each parent of vertex id
						if(!this.nodesInBall.contains(p)) {  // it means that this vertex is not visited yet
							nodesInBall.add(p);
							len.put(p, distance);            // distance from center to c                
							qu.add(p);                       // put child c in the queue	        			
						} // if
					} // for
				} //if
			} // if-else
		} // while
		
		SmallGraph subgraph = GraphUtils.inducedSubgraph(graph, nodesInBall);
		this.vertices = subgraph.vertices;
		this.labels = subgraph.labels;
		
	} // Ball
	
	public void clear() {
		this.vertices.clear();
		this.labels.clear();
		this.parentIndex = null;
		
		this.borderNodes.clear();
		this.nodesInBall.clear();
	}
	
    /** Perform dual simulation onto the ball and refine the vertices of the ball
     *  @param query  the query graph Q(U, D, k)
     *  @param dualSim    mappings from a query vertex u_q to { graph vertices v_g }
     *  @return			returns false when the ball becomes empty; true otherwise
     */ 
    public boolean dualFilter (SmallGraph query, Map<Integer, Set<Integer>> dualsim) {
    	return dualFilter(query, dualsim, false);
    }
    
    /** Perform dual simulation on this ball.
     *  @param query  	the query graph Q(U, D, k)
     *  @param dualSim  mappings from a query vertex u_q to { graph vertices v_g }
     *  @param strong	when it is false, we assume that ball is created based on the dual match result graph
     *  @return			returns false when the ball becomes empty; true otherwise
     */ 
    public boolean dualFilter (SmallGraph query, Map<Integer, Set<Integer>> dualsim, boolean strong) {
    	if(strong) { // projecting dualsim on the ball
    		this.nodesInBall.retainAll(DualSimulation.nodesInSimSet(dualsim));
    		if(!nodesInBall.contains(ballCenter)) {
    			this.clear();
    			return false;
    		}
    		Iterator<Integer> it = this.vertices.keySet().iterator();
    		while(it.hasNext()) {
    			int v = it.next();
    			if(! nodesInBall.contains(v)) {
    				it.remove();
    				vertices.remove(v);
    			} else {
    				if(vertices.get(v) != null)
    					vertices.get(v).retainAll(nodesInBall);
    			}
    		}
    	} //if
    	
    	// making a copy of dualsim and keeping only the vertices of the ball
    	Map<Integer, Set<Integer>> localDualSim = new HashMap<Integer, Set<Integer>>(dualsim.size());
    	for(int u : dualsim.keySet()) {
    		Set<Integer> localMatch = new HashSet<Integer>(dualsim.get(u));
    		localMatch.retainAll(nodesInBall);
    		localDualSim.put(u, localMatch);
    	}
    	
    	// filtered dualsim on the ball
    	localDualSim = DualSimulation.dualSimSetHelper(this, query, localDualSim);
    	if(localDualSim.isEmpty()) {
    		this.clear();
    		return false;
    	} //if
    	// it is valid only if it still contains the center
    	this.nodesInBall.clear();
    	this.borderNodes.clear();
    	nodesInBall.addAll(DualSimulation.nodesInSimSet(localDualSim));
    	if(! nodesInBall.contains(ballCenter) ) {
    		this.clear();
    		return false;
    	} //if

        // Finding max perfect subgraph
    	SmallGraph maxPG = DualSimulation.getResultMatchGraph(this, query, localDualSim);
    	this.vertices = maxPG.vertices;
    	this.labels = maxPG.labels;
    	
    	return true;
    } //dualFilter
	
    /**
     * Checks if this ball contains all the vertices of another ball
     * @param anotherBall
     * @return true when it contains another ball; false otherwise
     */
    public boolean contains(Ball anotherBall) {
    	if(this.nodesInBall.containsAll(anotherBall.nodesInBall)) {
    		for(int vertex : anotherBall.nodesInBall) {
    			if(! this.post(vertex).containsAll(anotherBall.post(vertex)))
    				return false;
    		}
    		return true;
    	} else
    		return false;
    }
    
	/****************************************************
	 * A method to return the ball as a string.
	 * @return The ball in a string format.
	 */
	public String getBallAsString() {
        StringBuilder s = new StringBuilder();
        if (vertices != null) {
            List<Integer> keys = new ArrayList<Integer> (vertices.keySet());
            Collections.sort (keys);
            for (int i : keys) {
                s.append(i + "->[");
                List<Integer> values = new ArrayList<Integer> (vertices.get(i));
                Collections.sort (values);
                for (int j: values) 
                    s.append(j+",");
                s.append("],");
            }
        }
        return s.toString();
    } //getBallAsString
	
	public String toString() {
		StringBuilder s = new StringBuilder("-----------\n");
		s.append("Center: " + ballCenter + "\n");
		s.append("Nodes in ball: " +nodesInBall + "\n");
		for (int u : this.labels.keySet()) {
			s.append(u + " (" + labels.get(u) + ") ");
			if(this.vertices.get(u) != null)
				s.append(vertices.get(u) + "\n");
		} // for
		//s.append("Border nodes: " + borderNodes + "\n");
		s.append("-----------\n");
		return(s.toString());
	}
	
	/**
	 * Test main method
	 * @param args
	 */
	public static void main (String[] args) throws Exception {
		SmallGraph sg = new SmallGraph("exampleGraphs/G_tight1.txt");
		Ball b = new Ball(sg, 8, 2);
		
		System.out.println(b.getBallAsString());
		System.out.println();
		System.out.println(b);
		
		System.out.println("Diameter of the ball:" + b.getDiameter());
		System.out.println("Radius of the ball:" + b.getRadius());
	}
}