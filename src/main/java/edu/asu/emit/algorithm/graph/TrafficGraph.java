/*
 *
 * Copyright (c) 2004-2008 Arizona State University.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY ARIZONA STATE UNIVERSITY ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL ARIZONA STATE UNIVERSITY
 * NOR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package edu.asu.emit.algorithm.graph;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import edu.asu.emit.algorithm.graph.abstraction.BaseVertex;
import edu.asu.emit.algorithm.graph.shortestpaths.YenTopKShortestPathsAlg;
import edu.asu.emit.algorithm.utils.Pair;

/**
 * The class defines a graph which can be changed constantly.
 *  
 * @author yqi
 */
public class TrafficGraph extends Graph {
	private Set<Integer> remVertexIdSet = new HashSet<Integer>();
	private Set<Pair<Integer, Integer>> remEdgeSet = new HashSet<Pair<Integer, Integer>>();

	/**
	 * Default constructor
	 */
	public TrafficGraph() { }
	
	/**
	 * Constructor 1
	 * 
	 * @param dataFileName
	 */
	public TrafficGraph(String dataFileName)	{
		super(dataFileName);
	}
	
	/**
	 * Constructor 2
	 * 
	 * @param graph
	 */
	public TrafficGraph(Graph graph) {
		super(graph);
	}

	/**
	 * Set the set of vertices to be removed from the graph
	 * 
	 * @param remVertexList
	 */
	public void setDelVertexIdList(Collection<Integer> remVertexList) {
		this.remVertexIdSet.addAll(remVertexList);
	}

	/**
	 * Set the set of edges to be removed from the graph
	 * 
	 * @param _rem_edge_hashcode_set
	 */
	public void setDelEdgeHashcodeSet(Collection<Pair<Integer, Integer>> remEdgeCollection) {
		remEdgeSet.addAll(remEdgeCollection);
	}
	
	/**
	 * Add an edge to the set of removed edges
	 * 
	 * @param edge
	 */
	public void deleteEdge(Pair<Integer, Integer> edge) {
		remEdgeSet.add(edge);
	}
	
	/**
	 * Add a vertex to the set of removed vertices
	 * 
	 * @param vertexId
	 */
	public void deleteVertex(Integer vertexId) {
		remVertexIdSet.add(vertexId);
	}
	
	public void recoverDeletedEdges() {
		remEdgeSet.clear();
	}

	public void recoverDeletedEdge(Pair<Integer, Integer> edge)	{
		remEdgeSet.remove(edge);
	}
	
	public void recoverDeletedVertices() {
		remVertexIdSet.clear();
	}
	
	public void recoverDeletedVertex(Integer vertexId) {
		remVertexIdSet.remove(vertexId);
	}
	
	/**
	 * Return the weight associated with the input edge.
	 * 
	 * @param source
	 * @param sink
	 * @return
	 */
	public double getEdgeWeight(BaseVertex source, BaseVertex sink)	{
		int sourceId = source.getId();
		int sinkId = sink.getId();
		
		if (remVertexIdSet.contains(sourceId) || remVertexIdSet.contains(sinkId) ||
		   remEdgeSet.contains(new Pair<Integer, Integer>(sourceId, sinkId))) {
			return Graph.DISCONNECTED;
		}
		return super.getEdgeWeight(source, sink);
	}

	/**
	 * Return the weight associated with the input edge.
	 * 
	 * @param source
	 * @param sink
	 * @return
	 */
	public double getEdgeWeightOfGraph(BaseVertex source, BaseVertex sink) {
		return super.getEdgeWeight(source, sink);
	}
	
	/**
	 * Return the set of fan-outs of the input vertex.
	 * 
	 * @param vertex
	 * @return
	 */
	public Set<BaseVertex> getAdjacentVertices(BaseVertex vertex) {
		Set<BaseVertex> retSet = new HashSet<BaseVertex>();
		int startingVertexId = vertex.getId();
		if (!remVertexIdSet.contains(startingVertexId))	{
			Set<BaseVertex> adjVertexSet = super.getAdjacentVertices(vertex);
			for (BaseVertex curVertex : adjVertexSet) {
				int endingVertexId = curVertex.getId();
				if (remVertexIdSet.contains(endingVertexId) ||
					remEdgeSet.contains(new Pair<Integer,Integer>(startingVertexId, endingVertexId))) {
					continue;
				}
				// 
				retSet.add(curVertex);
			}
		}
		return retSet;
	}

	/**
	 * Get the set of vertices preceding the input vertex.
	 * 
	 * @param vertex
	 * @return
	 */
	public Set<BaseVertex> getPrecedentVertices(BaseVertex vertex) {
		Set<BaseVertex> retSet = new HashSet<BaseVertex>();
		if (!remVertexIdSet.contains(vertex.getId())) {
			int endingVertexId = vertex.getId();
			Set<BaseVertex> preVertexSet = super.getPrecedentVertices(vertex);
			for (BaseVertex curVertex : preVertexSet) {
				int startingVertexId = curVertex.getId();
				if (remVertexIdSet.contains(startingVertexId) ||
					remEdgeSet.contains(new Pair<Integer, Integer>(startingVertexId, endingVertexId))) {
					continue;
				}
				//
				retSet.add(curVertex);
			}
		}
		return retSet;
	}

	/**
	 * Get the list of vertices in the graph, except those removed.
	 * @return
	 */
	public List<BaseVertex> getVertexList() {
		List<BaseVertex> retList = new Vector<BaseVertex>();
		for (BaseVertex curVertex : super.getVertexList()) {
			if (remVertexIdSet.contains(curVertex.getId())) {
				continue;
			}
			retList.add(curVertex);
		}
		return retList;
	}

	/**
	 * Get the vertex corresponding to the input 'id', if exist. 
	 * 
	 * @param id
	 * @return
	 */
	public BaseVertex getVertex(int id)	{
		if (remVertexIdSet.contains(id)) {
			return null;
		} else {
			return super.getVertex(id);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		System.out.println("Welcome to the class TrafficGraph!");
		
		TrafficGraph graph = new TrafficGraph("traffic_simulator/data/output/graph_cost");
		YenTopKShortestPathsAlg alg = new YenTopKShortestPathsAlg(graph);
		int k = 6;
		
		try {
			// 1. read the file and put the content in the buffer
			FileReader input = new FileReader("traffic_simulator/data/normalized_od.csv");
			BufferedReader bufRead = new BufferedReader(input);
			String line; 	// String that holds current file line
			
			// 1.1 write allocation result to output file
			FileWriter output = new FileWriter("traffic_simulator/data/output/allocation_path.csv");
			BufferedWriter bufWrite = new BufferedWriter(output);
			bufWrite.write("nodes,car_num");
			bufWrite.newLine();

			// 2. Read Each line
			line = bufRead.readLine();
			while (line != null) {
				String[] strList = line.trim().split(",");
				
				int startVertexId = Integer.parseInt(strList[0]);
				int endVertexId = Integer.parseInt(strList[1]);
				int carNumber = Integer.parseInt(strList[2]);
			
				// Calculate Kth shortest path
//				System.out.println(alg.getShortestPaths(graph.getVertex(startVertexId), graph.getVertex(endVertexId), k));
				List<Path> cands = alg.getShortestPaths(graph.getVertex(startVertexId),
														graph.getVertex(endVertexId),
														k);

				// Calculate each cost
				double sum = 0;
				for (Path cand : cands) {
					double cost = calculateCost(cand.getWeight());
					System.out.println(cand.getVertexList().toString() + "," + cost);
					sum += cost;
				}
				
				// Calculate allocation
				for (Path cand: cands) {
					int allocation = (int) (calculateCost(cand.getWeight()) * carNumber / sum);

					if (allocation == 0) continue;

					// Save to output file
					List<BaseVertex> list = cand.getVertexList();
					for (int i = 0; i < list.size() - 1; i++) {
						bufWrite.write(list.get(i).toString() + "-");
					}
					bufWrite.write(list.get(list.size()-1).toString());
					bufWrite.write("," + allocation);
					bufWrite.newLine();
				}
				line = bufRead.readLine();
			}
			bufRead.close();
			bufWrite.close();

		} catch (IOException e) {
			// If another exception is generated, print a stack trace
			e.printStackTrace();
		}
	}

	private static double calculateCost(double cost) {
		double e = 2.718;
		double u = 0.00472;

		// 0.0003 = 0.000189394 mile/feet * 35mile/hour * 60hour/min
		return Math.pow(e, -cost * 0.0003);
	}
}
