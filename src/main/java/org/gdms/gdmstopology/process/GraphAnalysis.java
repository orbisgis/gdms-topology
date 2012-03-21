/**
 * GDMS-Topology  is a library dedicated to graph analysis. It's based on the JGraphT available at
 * http://www.jgrapht.org/. It ables to compute and process large graphs using spatial
 * and alphanumeric indexes.
 * This version is developed at French IRSTV institut as part of the
 * EvalPDU project, funded by the French Agence Nationale de la Recherche
 * (ANR) under contract ANR-08-VILL-0005-01 and GEBD project
 * funded by the French Ministery of Ecology and Sustainable Development .
 *
 * GDMS-Topology  is distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://trac.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.gdms.gdmstopology.process;

import java.util.Iterator;
import java.util.List;
import org.gdms.data.SQLDataSourceFactory;
import org.gdms.data.schema.Metadata;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.model.DWMultigraphDataSource;
import org.gdms.gdmstopology.model.EdgeReversedGraphDataSource;
import org.gdms.gdmstopology.model.GDMSValueGraph;
import org.gdms.gdmstopology.model.GraphEdge;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.model.GraphMetadataFactory;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.model.WMultigraphDataSource;
import org.jgrapht.Graphs;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.orbisgis.progress.ProgressMonitor;

/**
 *
 * @author ebocher
 */
public class GraphAnalysis {

        //Constants used in the functions
        public static int DIRECT = 1;
        public static int DIRECT_REVERSED = 2;
        public static int UNDIRECT = 3;
        private static int ID_FIELD_INDEX = -1;
        private static int SOURCE_FIELD_INDEX = -1;
        private static int TARGET_FIELD_INDEX = -1;
        private static int pass =0;

        /**
         * Some usefull methods to analyse a graph network.
         */
        private GraphAnalysis() {
        }

        public static List<GraphEdge> findPathBetween(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                Integer startNode, Integer endNode) {
                return DijkstraShortestPath.findPathBetween(graph, startNode, endNode);
        }

        public static DiskBufferDriver findPathBetween2Nodes(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                Integer sourceVertex, Integer targetVertex) throws GraphException, DriverException {
                return findPathBetween2Nodes(dsf, graph, sourceVertex, targetVertex, Double.POSITIVE_INFINITY);
        }

        public static DiskBufferDriver findPathBetween2Nodes(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                Integer sourceVertex, Integer targetVertex, double radius) throws GraphException, DriverException {

                if (!graph.containsVertex(targetVertex)) {
                        throw new GraphException(
                                "The graph must contain the target vertex");
                }

                ClosestFirstIterator<Integer, GraphEdge> cl = new ClosestFirstIterator<Integer, GraphEdge>(graph, sourceVertex, radius);
                DiskBufferDriver diskBufferDriver = new DiskBufferDriver(dsf, GraphMetadataFactory.createEdgeMetadataGraph());
                while (cl.hasNext()) {
                        int vertex = cl.next();
                        if (vertex == targetVertex) {
                                int v = targetVertex;
                                int k = 0;
                                while (true) {
                                        GraphEdge edge = cl.getSpanningTreeEdge(v);
                                        if (edge == null) {
                                                break;
                                        }
                                        diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(graph.getGeometry(edge)),
                                                        ValueFactory.createValue(edge.getRowId()),
                                                        ValueFactory.createValue(k),
                                                        ValueFactory.createValue(edge.getSource()),
                                                        ValueFactory.createValue(edge.getTarget()),
                                                        ValueFactory.createValue(edge.getWeight())});
                                        k++;
                                        v = Graphs.getOppositeVertex(graph, edge, v);
                                }
                                break;
                        }
                }
                diskBufferDriver.writingFinished();
                diskBufferDriver.stop();

                return diskBufferDriver;

        }

        public static DiskBufferDriver findPathBetweenSeveralNodes(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                DataSet nodes) throws GraphException, DriverException {
                return findPathBetweenSeveralNodes(dsf, graph, nodes, Double.POSITIVE_INFINITY);
        }

        public static DiskBufferDriver findPathBetweenSeveralNodes(SQLDataSourceFactory dsf, GDMSValueGraph<Integer, GraphEdge> graph,
                DataSet nodes, double radius) throws GraphException, DriverException {
               
                DiskBufferDriver diskBufferDriver = new DiskBufferDriver(dsf, GraphMetadataFactory.createEdgeMetadataGraph());

                Iterator<Value[]> it = nodes.iterator();
                int currentSource = -1;
                ClosestFirstIterator<Integer, GraphEdge> cl = null;

                while (it.hasNext()) {
                        Value[] values = it.next();
                        int id_path_nodes = values[ID_FIELD_INDEX].getAsInt();
                        int source = values[SOURCE_FIELD_INDEX].getAsInt();
                        if (currentSource == -1) {
                                currentSource = source;
                                cl = new ClosestFirstIterator<Integer, GraphEdge>(graph, source);
                        }
                        //Check if the target node is in the graph
                        int targetVertex = values[TARGET_FIELD_INDEX].getAsInt();

                        if (!graph.containsVertex(targetVertex)) {
                                throw new GraphException(
                                        "The graph must contain the target vertex");
                        }
                        if (source != currentSource) {
                                cl = new ClosestFirstIterator<Integer, GraphEdge>(graph, source);
                        }

                        while (cl.hasNext()) {
                                int vertex = cl.next();
                                if (vertex == targetVertex) {
                                        int v = targetVertex;
                                        int k = 0;
                                        while (true) {
                                                GraphEdge edge = cl.getSpanningTreeEdge(v);
                                                if (edge == null) {
                                                        break;
                                                }
                                                diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(graph.getGeometry(edge)),
                                                                ValueFactory.createValue(id_path_nodes),
                                                                ValueFactory.createValue(k),
                                                                ValueFactory.createValue(edge.getSource()),
                                                                ValueFactory.createValue(edge.getTarget()),
                                                                ValueFactory.createValue(edge.getWeight())});
                                                k++;
                                                v = Graphs.getOppositeVertex(graph, edge, v);
                                        }
                                        break;
                                }
                        }

                }
                diskBufferDriver.writingFinished();
                diskBufferDriver.stop();
                return diskBufferDriver;
        }

        /**
         * Return as set of geometries the shortest path between two nodes. 
         * @param dsf
         * @param dataSet
         * @param source
         * @param target
         * @param costField
         * @param graphType
         * @param pm
         * @return
         * @throws GraphException
         * @throws DriverException 
         */
        public static DiskBufferDriver getShortestPath(SQLDataSourceFactory dsf, DataSet dataSet, int source, int target, String costField, int graphType, ProgressMonitor pm) throws GraphException, DriverException {
                if (graphType == DIRECT) {
                        DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                        dwMultigraphDataSource.setWeigthFieldIndex(costField);
                        return findPathBetween2Nodes(dsf, dwMultigraphDataSource, source, target);
                } else if (graphType == DIRECT_REVERSED) {
                        DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                        dwMultigraphDataSource.setWeigthFieldIndex(costField);
                        EdgeReversedGraphDataSource edgeReversedGraph = new EdgeReversedGraphDataSource(dwMultigraphDataSource);
                        return findPathBetween2Nodes(dsf, edgeReversedGraph, source, target);
                } else if (graphType == UNDIRECT) {
                        WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, dataSet, pm);
                        wMultigraphDataSource.setWeigthFieldIndex(costField);
                        return findPathBetween2Nodes(dsf, wMultigraphDataSource, source, target);
                } else {
                        throw new GraphException("Only 3 type of graphs are allowed."
                                + "1 if the path is computing using a directed graph.\n"
                                + "2 if the path is computing using a directed graph and edges are reversed\n"
                                + "3 if the path is computing using a undirected.");
                }
        }

        /**
         * Return all shortest paths from a set of start and target nodes.
         * The dataset that contains all nodes must following the schema : 
         * id (int or long), source (int or long) ,target(int or long)
         * @param dsf
         * @param dataSet
         * @param nodes
         * @param costField
         * @param graphType
         * @param pm
         * @return
         * @throws GraphException
         * @throws DriverException 
         */
        public static DiskBufferDriver getMShortestPath(SQLDataSourceFactory dsf, DataSet dataSet, DataSet nodes, String costField, int graphType, ProgressMonitor pm) throws GraphException, DriverException {
                if (checkMetadata(nodes)) {
                        if (graphType == DIRECT) {
                                DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                                dwMultigraphDataSource.setWeigthFieldIndex(costField);
                                return findPathBetweenSeveralNodes(dsf, dwMultigraphDataSource, nodes);
                        } else if (graphType == DIRECT_REVERSED) {
                                DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                                dwMultigraphDataSource.setWeigthFieldIndex(costField);
                                EdgeReversedGraphDataSource edgeReversedGraph = new EdgeReversedGraphDataSource(dwMultigraphDataSource);
                                return findPathBetweenSeveralNodes(dsf, edgeReversedGraph, nodes);
                        } else if (graphType == UNDIRECT) {
                                WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, dataSet, pm);
                                wMultigraphDataSource.setWeigthFieldIndex(costField);
                                return findPathBetweenSeveralNodes(dsf, wMultigraphDataSource, nodes);
                        } else {
                                throw new GraphException("Only 3 type of graphs are allowed."
                                        + "1 if the path is computing using a directed graph.\n"
                                        + "2 if the path is computing using a directed graph and edges are reversed\n"
                                        + "3 if the path is computing using a undirected.");
                        }
                } else {
                        throw new GraphException("The table nodes must contains the field id, source and target");
                }
        }

        /**
         * A method to check if the schemas are well populated to use graph analysis
         * tools.
         *
         * @throws DriverException
         */
        private static boolean checkMetadata(DataSet nodes) throws DriverException {
                Metadata nodesMD = nodes.getMetadata();
                ID_FIELD_INDEX = nodesMD.getFieldIndex(GraphSchema.ID);
                SOURCE_FIELD_INDEX = nodesMD.getFieldIndex("source");
                TARGET_FIELD_INDEX = nodesMD.getFieldIndex("target");
                if (ID_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table must contains a field named id");
                }

                if (SOURCE_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table must contains a field named source");
                }
                if (TARGET_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table must contains a field named target");
                }
                return true;
        }
}
