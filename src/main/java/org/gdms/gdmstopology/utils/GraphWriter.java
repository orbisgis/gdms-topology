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

import java.io.File;
import java.util.Set;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.types.Type;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.model.GraphSchema;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author ebocher
 */
public class GraphWriter {

        /**
         * A simple method to store a graph in two GDMS files : nodes and edges.
         *
         * @param graphPath
         * @param nbOfEdges
         * @param nbOfVertexes
         */
        public static boolean saveAsGDMSFile(Graph<Integer, DefaultEdge> graph, String nodesFile, String edgesFile) throws DriverException {

                boolean isDone = false;
                if (nodesFile != null) {
                        File file = new File(nodesFile);
                        if (file.exists()) {
                                throw new RuntimeException("the file " + nodesFile + " exists");
                        }
                        DefaultMetadata nodesMetadata = new DefaultMetadata();
                        nodesMetadata.addField(GraphSchema.ID, Type.LONG);

                        DiskBufferDriver nodes = new DiskBufferDriver(file, nodesMetadata);
                        Set<Integer> vs = graph.vertexSet();

                        for (Integer v : vs) {
                                nodes.addValues(ValueFactory.createValue(v));
                        }
                        nodes.writingFinished();
                        isDone = true;
                }

                if (edgesFile != null) {
                        File file = new File(edgesFile);
                        if (file.exists()) {
                                throw new RuntimeException("the file " + edgesFile + " exists");
                        }
                        DefaultMetadata edgesMetadata = new DefaultMetadata();
                        edgesMetadata.addField(GraphSchema.ID, Type.LONG);
                        edgesMetadata.addField(GraphSchema.START_NODE, Type.LONG);
                        edgesMetadata.addField(GraphSchema.END_NODE, Type.LONG);
                        edgesMetadata.addField(GraphSchema.WEIGHT, Type.DOUBLE);

                        DiskBufferDriver edges = new DiskBufferDriver(file, edgesMetadata);
                        Set<DefaultEdge> es = graph.edgeSet();
                        int i = 1;
                        for (DefaultEdge e : es) {
                                edges.addValues(ValueFactory.createValue(i++),
                                        ValueFactory.createValue(graph.getEdgeSource(e)),
                                        ValueFactory.createValue(graph.getEdgeTarget(e)),
                                        ValueFactory.createValue(graph.getEdgeWeight(e)));
                        }
                        edges.writingFinished();
                        isDone = true;
                }
                return isDone;
        }

        /**
         * Store the edges of the graph into a supported gdms file format.
         *
         * @param graph
         * @param edgesFile
         * @return
         * @throws DriverException
         */
        public static boolean saveAsGDMSFile(Graph<Integer, DefaultEdge> graph, String edgesFile) throws DriverException {
                return saveAsGDMSFile(graph, null, edgesFile);
        }
}
