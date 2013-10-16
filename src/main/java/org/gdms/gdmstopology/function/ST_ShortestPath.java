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
package org.gdms.gdmstopology.function;

import com.vividsolutions.jts.geom.Geometry;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.indexes.DefaultAlphaQuery;
import org.gdms.data.indexes.IndexException;
import org.gdms.data.indexes.IndexManager;
import org.gdms.data.schema.Metadata;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.graphcreator.WeightedGraphCreator;
import org.gdms.gdmstopology.model.GraphMetadataFactory;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.parse.GraphFunctionParser;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;
import org.javanetworkanalyzer.alg.Dijkstra;
import org.javanetworkanalyzer.data.VWCent;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.orbisgis.progress.ProgressMonitor;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static org.gdms.data.values.ValueFactory.createValue;

/**
 * Calculates the shortest path between two vertices of a graph using Dijkstra's
 * algorithm.
 * <p/>
 * <p> Example usage: <center>
 * {@code SELECT * from  ST_ShortestPath(input_table, source_vertex, target_vertex, 'weights_column'[,orientation]);}
 * </center>
 * <p/>
 * <p> Required parameters: <ul> <li> {@code input_table} - the input table.
 * Specifically, this is the {@code output_table_prefix.edges} table produced by
 * {@link ST_Graph}, except that an additional column specifying the weight of
 * each edge must be added (this is the 'weights_column'). <li>
 * {@code source_vertex} - an integer specifying the source vertex. <li>
 * {@code target_vertex} - an integer specifying the target vertex. <li>
 * {@code 'weights_column'} - a string specifying the name of the column of the
 * input table that gives the weight of each edge. </ul>
 * <p/>
 * <p> Optional parameter: <ul> <li> {@code orientation} - an integer specifying
 * the orientation of the graph: <ul> <li> 1 if the graph is directed, <li> 2 if
 * it is directed and we wish to reverse the orientation of the edges, <li> 3 if
 * the graph is undirected. </ul> If no orientation is specified, we assume the
 * graph is directed. </ul>
 * <p/>
 *
 * @author Erwan Bocher
 * @author Adam Gouge
 */
public class ST_ShortestPath extends AbstractTableFunction {

    private int source = -1;
    private int destination = -1;
    private String weightsColumn = null;
    private String globalOrientation = null;
    private String edgeOrientationColumnName = null;
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(ST_ShortestPath.class);
    private static final Metadata MD = GraphMetadataFactory.createEdgeMetadataShortestPath();

    /**
     * Evaluates the function to calculate the shortest path using Dijkstra'
     * algorithm.
     *
     * @param dsf    The {@link DataSourceFactory} used to parse the data set.
     * @param tables The input table. (This {@link DataSet} array will contain
     *               only one element since there is only one input table.)
     * @param values Array containing the optional arguments.
     * @param pm     The progress monitor used to track the progress of the shortest
     *               path calculation.
     * @return The {@link DataSet} containing the shortest path.
     * @throws FunctionException
     */
    @Override
    public DataSet evaluate(
            DataSourceFactory dsf,
            DataSet[] tables,
            Value[] values,
            ProgressMonitor pm)
            throws FunctionException {

        // Recover the edges.
        final DataSet edges = tables[0];

        // Recover all other parameters.
        parseArguments(edges, values);

        // Prepare the graph.
        KeyedGraph<VWCent, Edge> graph = prepareGraph(edges);

        // Compute and return results.
        DiskBufferDriver results = null;
        try {
            results = compute(dsf, edges, graph, pm);
        } catch (DriverException ex) {
            LOGGER.error(ex.toString());
        }
        return results;
    }

    /**
     * Parse all possible arguments for {@link ST_ShortestPath}.
     *
     * @param edges  Edges input table
     * @param values Arguments
     */
    private void parseArguments(DataSet edges, Value[] values) {
        GraphFunctionParser parser = new GraphFunctionParser();
        source = parser.parseSource(values[0]);
        destination = parser.parseTarget(values[1]);
        parser.parseOptionalArguments(edges, values, 2);
        globalOrientation = parser.getGlobalOrientation();
        edgeOrientationColumnName = parser.getEdgeOrientationColumnName();
        weightsColumn = parser.getWeightsColumn();
    }

    /**
     * Prepare the JGraphT graph from the given edges table.
     *
     * @param edges Edges table
     * @return JGraphT graph
     */
    private KeyedGraph<VWCent, Edge> prepareGraph(final DataSet edges) {
        KeyedGraph<VWCent, Edge> graph;

        // Get the graph orientation.
        int graphType = -1;
        if (globalOrientation != null) {
            graphType = globalOrientation.equalsIgnoreCase(ST_ShortestPathLength.DIRECTED)
                    ? GraphSchema.DIRECT
                    : globalOrientation.equalsIgnoreCase(ST_ShortestPathLength.REVERSED)
                    ? GraphSchema.DIRECT_REVERSED
                    : globalOrientation.equalsIgnoreCase(ST_ShortestPathLength.UNDIRECTED)
                    ? GraphSchema.UNDIRECT
                    : -1;
        } else if (graphType == -1) {
            LOGGER.warn("Assuming a directed graph.");
            graphType = GraphSchema.DIRECT;
        }

        // Create the graph.
        if (weightsColumn != null) {
            graph = new WeightedGraphCreator<VWCent, Edge>(
                    edges,
                    graphType,
                    edgeOrientationColumnName,
                    VWCent.class,
                    Edge.class,
                    weightsColumn).prepareGraph();
        } else {
            throw new UnsupportedOperationException(
                    "ST_ShortestPath has not yet been implemented for "
                            + "unweighted graphs.");
        }
        return graph;
    }

    /**
     * Compute the distances and write them to a table.
     *
     * @param dsf   Data source factory
     * @param graph JGraphT graph
     * @return The requested distances
     * @throws DriverException
     */
    private DiskBufferDriver compute(DataSourceFactory dsf,
                                     DataSet dataSet,
                                     KeyedGraph<VWCent, Edge> graph,
                                     ProgressMonitor pm)
            throws DriverException {

        // A DiskBufferDriver to store the shortest path.
        DiskBufferDriver output =
                new DiskBufferDriver(dsf, MD);

        if (graph != null) {

            // (source, destination, ...) (One-to-one)
            if (source != -1 && destination != -1) {

                // Get a Dijkstra algo for the distance calculation.
                Dijkstra<VWCent, Edge> dijkstra = new Dijkstra<VWCent, Edge>(graph);
                dijkstra.oneToOne(graph.getVertex(source), graph.getVertex(destination));

                // Get the index of the_geom
                final int geomIndex = dataSet.getSpatialFieldIndex();
                if (geomIndex == -1) {
                    throw new IndexOutOfBoundsException("Geometry field not found.");
                }
                // Build an index on id for looking up the row index later.
                final IndexManager indexManager = dsf.getIndexManager();
                if (!indexManager.isIndexed(dataSet, GraphSchema.ID)) {
                    try {
                        indexManager.buildIndex(dataSet, GraphSchema.ID, pm);
                    } catch (NoSuchTableException e) {
                        LOGGER.error("Table not found when building index.");
                    } catch (IndexException e) {
                        LOGGER.error("Problem building indices.");
                    }
                }

                // Rebuild the shortest path(s). (Yes, there could be more than
                // one if they have the same distance!)
                int newID = 1;

                Set<Edge> predecessorEdges = graph.getVertex(destination).getPredecessorEdges();
                VWCent previousDestination = graph.getVertex(destination);
                while (!predecessorEdges.isEmpty()) {
                    Set<Edge> nextPredecessorEdges = new HashSet<Edge>();

                    for (Edge e : predecessorEdges) {

                        // With undirected graphs, the source and target could
                        // be switched. This is JGraphT's fault. Here we make
                        // sure they are in the right order.
                        final VWCent edgeSource = graph.getEdgeSource(e);
                        final VWCent edgeTarget = graph.getEdgeTarget(e);
                        int sourceID = -1;
                        int targetID = -1;
                        if (previousDestination.equals(edgeTarget)) {
                            sourceID = edgeSource.getID();
                            targetID = edgeTarget.getID();
                            nextPredecessorEdges.addAll(edgeSource.getPredecessorEdges());
                            previousDestination = edgeSource;
                        } else if (previousDestination.equals(edgeSource)) {
                            sourceID = edgeTarget.getID();
                            targetID = edgeSource.getID();
                            nextPredecessorEdges.addAll(edgeTarget.getPredecessorEdges());
                            previousDestination = edgeTarget;
                        } else {
                            throw new IllegalStateException("A vertex has a predecessor " +
                                    "edge not ending on itself.");
                        }

                        output.addValues(
                                createValue(getEdgeGeometry(dsf, dataSet, geomIndex, e)),
                                createValue(e.getID()),
                                createValue(newID++),
                                createValue(sourceID),
                                createValue(targetID),
                                createValue(graph.getEdgeWeight(e)));
                    }
                    predecessorEdges = nextPredecessorEdges;
                }
            } else {
                LOGGER.error("Source or destination note configured correctly. " +
                        "Source: " + source + ", Destination: " + destination);
            }
            // Clean-up
            output.writingFinished();
            output.open();
        } else {
            LOGGER.error("Null graph.");
        }
        return output;
    }

    /**
     *
     * @param dsf
     * @param dataSet
     * @param geomIndex
     * @param e
     * @return
     * @throws DriverException
     */
    private Geometry getEdgeGeometry(DataSourceFactory dsf, DataSet dataSet, int geomIndex, Edge e) throws DriverException {
        // We have to use Math.abs on the id because in directed
        // graphs, an undirected edge could have a negative id.
        // This is used in JNA for the edge betweenness calculation.
        // But the geometry remains the same.
        Iterator<Integer> it = dataSet.queryIndex(dsf,
                new DefaultAlphaQuery(GraphSchema.ID,
                        createValue(Math.abs(e.getID()))));
        // Since the id is unique, we expect the row id to be unique.
        int edgeRowIndex = -1;
        if (it.hasNext()) {
            edgeRowIndex = it.next().intValue();
            if (it.hasNext()) {
                throw new IllegalStateException("Multiple edge ids!");
            }
        } else {
            throw new IllegalStateException("No row index found for edge " + e.getID()
                    + " (Edge not found).");
        }
        return dataSet.getGeometry(edgeRowIndex, geomIndex);
    }

    /**
     * Returns the name of this function. This name will be used in SQL
     * statements.
     *
     * @return The name of this function.
     */
    @Override
    public String getName() {
        return "ST_ShortestPath";
    }

    /**
     * Returns an example query using this function.
     *
     * @return An example query using this function.
     */
    @Override
    public String getSqlOrder() {
        return "SELECT * from  ST_ShortestPath(input_table, source_vertex, " +
                "target_vertex, 'weights_column'[, "
                + ST_ShortestPathLength.POSSIBLE_ORIENTATIONS + "]);";
    }

    /**
     * Returns a description of this function.
     *
     * @return A description of this function.
     */
    @Override
    public String getDescription() {
        return "Calculates the shortest path between two vertices of a "
                + "graph using Dijkstra's algorithm. The input_table is the "
                + "output_table_prefix.edges table produced by the ST_Graph "
                + "function, except that an extra column must be added to "
                + "specify the weight of each edge ('weights_column'). An additional "
                + "column may be added to specify individual edge orientations "
                + "by integers: 1 for directed, -1 for reversed, and 0 for bidirectional. "
                + "The source_vertex and the "
                + "target_vertex are specified by an integer. The "
                + "'weights_column' is a string specifying the name of the "
                + "column of the input table that gives the weight of each "
                + "edge. The optional parameter orientation is a string specifying the "
                + "orientation of the graph: "
                + "<ul> "
                + "<li> '" + ST_ShortestPathLength.DIRECTED + " - " + ST_ShortestPathLength.EDGE_ORIENTATION_COLUMN + "' "
                + "<li> '" + ST_ShortestPathLength.REVERSED + " - " + ST_ShortestPathLength.EDGE_ORIENTATION_COLUMN + "' "
                + "<li> '" + ST_ShortestPathLength.UNDIRECTED + "'."
                + "</ul> The default orientation is " + ST_ShortestPathLength.DIRECTED + " with edge "
                + "orientations given by the geometries, though edge orientations "
                + "should most definitely be provided by the user. ";
    }

    /**
     * Returns the {@link Metadata} of the result of this function without
     * executing the query.
     *
     * @param tables {@link Metadata} objects of the input tables.
     * @return The {@link Metadata} of the result.
     * @throws DriverException
     */
    // TODO: The input 'Metadata[] tables' is never used!
    @Override
    public Metadata getMetadata(Metadata[] tables) throws DriverException {
        return MD;
    }

    /**
     * Returns an array of all possible signatures of this function. Multiple
     * signatures arise from some arguments being optional.
     * <p/>
     * <p> Possible signatures: <OL> <li> {@code (TABLE, INT, INT, STRING)} <li>
     * {@code (TABLE, INT, INT, STRING, INT)} </OL>
     *
     * @return An array of all possible signatures of this function.
     */
    @Override
    public FunctionSignature[] getFunctionSignatures() {
        return new FunctionSignature[]{
                new TableFunctionSignature(
                        TableDefinition.GEOMETRY,
                        new TableArgument(TableDefinition.GEOMETRY),
                        ScalarArgument.INT,
                        ScalarArgument.INT,
                        ScalarArgument.STRING),
                new TableFunctionSignature(
                        TableDefinition.GEOMETRY,
                        new TableArgument(TableDefinition.GEOMETRY),
                        ScalarArgument.INT,
                        ScalarArgument.INT,
                        ScalarArgument.STRING,
                        ScalarArgument.STRING)
        };
    }
}
