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
package org.gdms.gdmstopology.utils;

import org.jgrapht.Graph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.WeightedMultigraph;

/**
 * Generates several different kinds of random graphs.
 *
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class RandomGraphCreator {

    private RandomGraphCreator() {
    }

    /**
     * Creates a random {@link DefaultDirectedGraph}.
     *
     * @param numVertices The number of vertices in the graph.
     * @param numEdges    The number of edges in the graph.
     *
     * @return The newly-generated random graph.
     */
    public static Graph<Integer, DefaultEdge> createRandomDirectGraph(
            int numVertices, int numEdges) {
        RandomGraphGenerator<Integer, DefaultEdge> randomGraphGenerator = new RandomGraphGenerator<Integer, DefaultEdge>(
                numVertices, numEdges);
        DefaultDirectedGraph<Integer, DefaultEdge> sourceGraph = new DefaultDirectedGraph<Integer, DefaultEdge>(
                DefaultEdge.class);
        randomGraphGenerator.generateGraph(sourceGraph, new IntVertexFactory(),
                                           null);
        return sourceGraph;
    }

    /**
     * Creates a random {@link WeightedMultigraph}.
     *
     * @param numVertices The number of vertices in the graph.
     * @param numEdges    The number of edges in the graph.
     *
     * @return The newly-generated random graph.
     */
    public static Graph<Integer, DefaultEdge> createRandomWeightedMultigraph(
            int numVertices, int numEdges) {
        RandomGraphGenerator<Integer, DefaultEdge> randomGraphGenerator = new RandomGraphGenerator<Integer, DefaultEdge>(
                numVertices, numEdges);
        WeightedMultigraph<Integer, DefaultEdge> sourceGraph = new WeightedMultigraph<Integer, DefaultEdge>(
                DefaultEdge.class);
        randomGraphGenerator.generateGraph(sourceGraph, new IntVertexFactory(),
                                           null);
        return sourceGraph;
    }

    /**
     * Creates a random <i>connected</i> {@link WeightedMultigraph} with the
     * specified number of nodes and edges.
     *
     * <i>Note</i>: There must be at least |V|-1 edges for the graph to be
     * connected. In order to not get stuck in an infinite loop, this method
     * should be called with the number of edges much larger than the number of
     * nodes.
     *
     * @param numNodes The number of nodes.
     * @param numEdges The number of edges.
     *
     * @return A random connected graph with the specified number of nodes and
     *         edges.
     */
    public static Graph<Integer, DefaultEdge> createRandomConnectedWeightedMultigraph(
            int numNodes, int numEdges) {
        int count = 1;
        // TODO: Infinite loop.
        while (true) {
            // Generate a random graph
            Graph<Integer, DefaultEdge> graph = createRandomWeightedMultigraph(
                    numNodes, numEdges);
            // Make sure the graph is connected.
            // We have to type-cast the graph in order to be able to use the constructor.
            ConnectivityInspector inspector = new ConnectivityInspector(
                    (UndirectedGraph<Integer, DefaultEdge>) graph);
            System.out.println(count + ". Graph is connected <-- " + inspector.
                    isGraphConnected());
            if (inspector.isGraphConnected()) {
                return graph;
            }
            count++;
        }
    }

    /**
     * Creates a random <i>connected</i> {@link WeightedMultigraph} with
     * <code>10^nodesExponent</code> nodes and
     * <code>10^edgesExponent</code> edges.
     *
     * @param nodesExponent    The exponent for the number of nodes.
     * @param edgesExponentThe exponent for the number of edges.
     *
     * @return A random connected graph with the specified number of nodes and
     *         edges.
     */
    public static Graph<Integer, DefaultEdge> createRandomConnectedWeightedMultigraphByExponents(
            int nodesExponent, int edgesExponent) {
        return createRandomConnectedWeightedMultigraph(
                (int) Math.pow(10, nodesExponent), (int) Math.pow(10,
                                                                  edgesExponent));
    }

    /**
     * A vertex factory where the vertices are {@link Integer}s.
     */
    private static class IntVertexFactory implements VertexFactory<Integer> {

        private int nextVertex = 1;

        @Override
        public Integer createVertex() {
            return nextVertex++;
        }
    }
}
