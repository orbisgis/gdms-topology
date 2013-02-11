/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV institut as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministery
 * of Ecology and Sustainable Development.
 *
 * GDMS-Topology is distributed under GPL 3 license. It is produced by the
 * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
 * 2488.
 *
 * Copyright (C) 2009-2012 IRSTV (FR CNRS 2488)
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://wwwc.orbisgis.org/> or contact
 * directly: info_at_ orbisgis.org
 */
package org.gdms.gdmstopology.demo;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/**
 * A class to test the jgrapht-sna implementations of betweenness centrality,
 * closeness centrality, and the Floyd-Warshall algorithm.
 *
 * @author Adam Gouge
 */
public class DemoCentralityIndices {

//    /**
//     * The main method.
//     * 
//     * @param args The command line arguments
//     */
//    public static void main(String[] args) {
//
//        int nodesExponent = 1;
//        int edgesExponent = 1;
//
//        // Generate a CONNECTED random graph.
//        Graph<Integer, DefaultEdge> graph = RandomGraphCreator.createRandomConnectedWeightedMultigraphByExponents(nodesExponent, edgesExponent);
////        printGraph(graph);
//
//        testBetweennessCentrality(graph);
//        testClosenessCentrality(graph);
//
//        compareFloydWarshallAlgorithms(graph);
//    }
    /**
     * Prints the number of nodes and edges of a graph as well as a list of all
     * edges with their weights.
     *
     * @param graph The graph to be printed.
     */
    public static void printGraph(Graph<Integer, DefaultEdge> graph) {
        // Print the number of nodes and edges.
        System.out.
                println("Nodes: " + graph.vertexSet().size() + ", Edges: " + graph.
                edgeSet().size());
        // Print the edges and their weights.
        for (DefaultEdge edge : graph.edgeSet()) {
            System.out.print(edge + " ");
            System.out.println(graph.getEdgeWeight(edge));
        }
    }
//    /**
//     * Prints the Freeman closeness centrality metric implemented in the
//     * jgrapht-sna library applied to the given graph.
//     *
//     * @param graph The graph on which to calculate the closeness centrality
//     * indices.
//     */
//    public static void testClosenessCentrality(Graph<Integer, DefaultEdge> graph) {
//        // Start timing.
//        long start = System.currentTimeMillis();
//
//        // Do the calculation.
//        FreemanClosenessCentrality<Integer, DefaultEdge> alg = new FreemanClosenessCentrality<Integer, DefaultEdge>(graph);
//        CentralityResult<Integer> result = alg.calculate();
//
//        // End timing.
//        long end = System.currentTimeMillis();
//        long time = end - start;
//
//        // Print out the results.
//        System.out.println("Closeness centrality: " + result.toString());
//        System.out.println("Nodes: " + graph.vertexSet().size() + ", Edges: " + graph.edgeSet().size());
//        System.out.println("Time: " + time + " ms.");
//        System.out.println();
//    }
//    /**
//     * Prints the betweenness centrality metric implemented in the jgrapht-sna
//     * library applied to the given graph.
//     *
//     * @param graph The graph on which to calculate the betweenness centrality
//     * indices.
//     */
//    public static void testBetweennessCentrality(Graph<Integer, DefaultEdge> graph) {
//
//        // Start timing.
//        long start = System.currentTimeMillis();
//
//        // Do the calculation.
//        BrandesBetweennessCentrality<Integer, DefaultEdge> alg = new BrandesBetweennessCentrality<Integer, DefaultEdge>(graph);
//        CentralityResult<Integer> result = alg.calculate();
//
//        // End timing.
//        long end = System.currentTimeMillis();
//        long time = end - start;
//
//        // Print out the results.
//        System.out.println("Betweenness centrality: " + result.toString());
//        System.out.println("Nodes: " + graph.vertexSet().size() + ", Edges: " + graph.edgeSet().size());
//        System.out.println("Time: " + time + " ms.");
//        System.out.println();
//    }
//    /**
//     * Compares the JGraphT implementation of
//     *
//     * @param graph
//     */
//    public static void compareFloydWarshallAlgorithms(Graph<Integer, DefaultEdge> graph) {
//
//        System.out.println("Comparison of Floyd-Warshall algorithm implementations:");
//        
//        // Floyd-Warshall JGraphT -- seems to be much shorter.
//        long start = System.currentTimeMillis();
//        FloydWarshallShortestPaths<Integer, DefaultEdge> fWAlg = new FloydWarshallShortestPaths<Integer, DefaultEdge>(graph);
//        int numberOfPaths = fWAlg.getShortestPathsCount();
//        long end = System.currentTimeMillis();
//        long time = end - start;
//        System.out.println("JGraphT time: " + time + " ms, " + numberOfPaths + " paths.");
//
//        // Floyd-Warshall SNA -- seems to be much longer.
//        start = System.currentTimeMillis();
//        FloydWarshallAllShortestPaths<Integer, DefaultEdge> fWAlgSNA = new FloydWarshallAllShortestPaths<Integer, DefaultEdge>(graph);
//        numberOfPaths = fWAlgSNA.lazyCalculatePaths();
//        end = System.currentTimeMillis();
//        time = end - start;
//        System.out.println("SNA time: " + time + " ms, " + numberOfPaths + " paths.");
//
//    }
}
