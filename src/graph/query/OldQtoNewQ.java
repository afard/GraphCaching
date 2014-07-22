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
package graph.query;

import graph.common.SmallGraph;

import java.io.File;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.javatuples.Pair;
/**
 * 
 * @author Arash Fard
 *
 */

public class OldQtoNewQ {
	
	/*
	 * Randomly extract a few query graphs from a given data graph
	 * args[0] the path to the old queries
	 * args[1] the requested number of output queries from each size
	 * args[2] the requested number of increments in each input query
	 * args[3] the path for storing the output queries
	 */
	public static void main(String[] args) throws Exception {
		File dirIn = new File(args[0]);
		if(!dirIn.isDirectory())
			throw new Exception("The specified path for the input graphs is not a valid directory");
		File[] graphFiles = dirIn.listFiles();
		
		int qPerSize = Integer.parseInt(args[1]);
		int qPerInc =  Integer.parseInt(args[2]);

		Random randLabel = new Random();
		Random randVertex = new Random();
		Random randEdgeDirection = new Random();
		
		for(File inG : graphFiles) {
			SmallGraph oldQ = new SmallGraph(inG.getAbsolutePath());
			
			int version = 0;

			for(int count1 = 0; count1 < qPerSize; count1++) {
				SmallGraph newQ = oldQ.clone();

				for(int count2 = 0; count2 < qPerInc; count2++) {
					int newQ_N = newQ.getNumVertices();
					int newLabel = newQ.getLabel(randLabel.nextInt(newQ_N));
					int nConnections = 0;
					while(nConnections == 0)
						nConnections = randVertex.nextInt(newQ_N);
					Set<Pair<Integer, Integer>> connectVertices = new HashSet<Pair<Integer, Integer>>(nConnections);
					for(int i=0; i < nConnections; i++)
						connectVertices.add(new Pair<Integer, Integer>(randVertex.nextInt(newQ_N), randEdgeDirection.nextInt(2)));

					newQ.connectNewVertex(newQ_N , newLabel, connectVertices);
					newQ.print2File(args[3] + "/" + inG.getName() + "-V" + version);
					version ++;
				} //for
			} //for
			
		} //for

	}//main
	
}//class
