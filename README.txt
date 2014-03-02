A. SEQUENTIAL DUAL: Runs fine. No Errors.

How to run: 

1)	In the Java Program, I have a main method to test the Simulation.
	So to run the program, You just have to pass the parameters to the method.

2)	The data graph and the query graph are read form the filesystem.

3) 	The parameters are : getDual( childIndex of G, LabelMap of G, Query Graph).

----------------------------------------------------------------------------------------------------------------

B.  SEQUENTIAL STRONG and SEQUENTIAL STRICT are in a single program. Runs fine. No Errors.

How to Run: 
1) 	In the Java Program, I have a main method to test the Simulation.
	So to run the program, You just have to pass the parameters to the method.

2) The parameters in this case change according to the simulation we want to run.

	For eg: The method getStrong() has the parameters:
		a) The data graph.
		b) The query graph.
		c) The simulation which we want to run. And the simulation string is passed through arguments to the program.
		
		sample: getStrong(g, q, args[0]);  // args[0] = "strict"


3) The data graph and the query graph are read form the filesystem.

----------------------------------------------------------------------------------------------------------------

C. SEQUENTIAL TIGHT: Runs Fine. No errors.

How to run:

1)  In the Java Program, I have a main method to test the Simulation.
	So to run the program, You just have to pass the parameters to the method.
	
2)	The parameters in this case ARE:

		SAMPLE: getTight( g ,q )
		 a) the data graph.
		 b) the query graph
		 
3)	The data graph and the query graph are read form the filesystem.

4) 	The methods used in selecting the candidate vertex for Tight Simulation are: 

	i) selectivityCriteria(); 		It is present in Graph.java
	ii) central(); 					It is present in GraphMetrics.java

----------------------------------------------------------------------------------------------------------------
D. TIGHT SIMULATION EXPERIMENT 

How to run:

1) 	To run this , we have to pass the dataGraph and the query graph as parameters to the method 
	tightExperiment() in the main method.
	
2)	I am currently passing the centers from the information I am fetching from the Sequential tight simulation .

3) 	Should verify  with Arash. Needs some refinement.


----------------------------------------------------------------------------------------------------------------


E. The graphs used:

1) 	For my experiments and all the testing I have generated the graphs using the graph generator method from the 
	Scala Package.

