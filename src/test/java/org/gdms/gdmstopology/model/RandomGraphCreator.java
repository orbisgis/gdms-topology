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
package org.gdms.gdmstopology.model;

import org.jgrapht.Graph;
import org.jgrapht.VertexFactory;
import org.jgrapht.generate.RandomGraphGenerator;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author Erwan Bocher
 */
public class RandomGraphCreator {

    private RandomGraphCreator() {
    }

    /**
     * A simple class to create a random directGraph
     *
     * @param numVertices
     * @param numEdges
     * @return
     */
    public static Graph<Integer, DefaultEdge> createRandomDirectGraph(int numVertices, int numEdges) {
        RandomGraphGenerator<Integer, DefaultEdge> randomGraphGenerator = new RandomGraphGenerator<Integer, DefaultEdge>(numVertices, numEdges);
        DefaultDirectedGraph<Integer, DefaultEdge> sourceGraph = new DefaultDirectedGraph<Integer, DefaultEdge>(DefaultEdge.class);
        randomGraphGenerator.generateGraph(sourceGraph, new IntVertexFactory(), null);
        return sourceGraph;
    }

    /**
     * Integer vertex factory
     */
    private static class IntVertexFactory implements VertexFactory<Integer> {

        private int nextVertex = 1;

        @Override
        public Integer createVertex() {
            return nextVertex++;
        }
    }
}
