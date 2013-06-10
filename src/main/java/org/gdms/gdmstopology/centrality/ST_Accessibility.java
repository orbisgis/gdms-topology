/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV Institute as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministry
 * of Ecology and Sustainable Development.
 *
 * GDMS-Topology is distributed under GPL 3 license. It is produced by the
 * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
 * 2488.
 *
 * Copyright (C) 2009-2013 IRSTV (FR CNRS 2488)
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
 * directly: info_at_orbisgis.org
 */
package org.gdms.gdmstopology.centrality;

import java.util.HashSet;
import java.util.Set;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.function.ST_ShortestPathLength;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.DESTINATION;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.DIRECTED;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.EDGE_ORIENTATION_COLUMN;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.POSSIBLE_ORIENTATIONS;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.REVERSED;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.UNDIRECTED;
import org.gdms.gdmstopology.graphcreator.GraphCreator;
import org.gdms.gdmstopology.graphcreator.WeightedGraphCreator;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.parse.GraphFunctionParser;
import org.gdms.gdmstopology.utils.ArrayConcatenator;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;
import org.javanetworkanalyzer.alg.DijkstraForAccessibility;
import org.javanetworkanalyzer.analyzers.AccessibilityAnalyzer;
import org.javanetworkanalyzer.data.VAccess;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.orbisgis.progress.ProgressMonitor;
import org.slf4j.LoggerFactory;

/**
 * Calculates, for each vertex, the (distance to the) closest destination among
 * several possible destinations.
 *
 * @author Adam Gouge
 */
public class ST_Accessibility extends AbstractTableFunction {

    /**
     * The name of this function.
     */
    private static final String NAME = "ST_Accessibility";
    /**
     * The SQL order of this function.
     */
    private static final String SQL_ORDER =
            "SELECT * FROM " + NAME + "("
            + "output.edges, "
            + "destination_table | 'dest1, dest2, ...'"
            + "[, 'weights_column']"
            + "[, " + POSSIBLE_ORIENTATIONS + "]);";
    /**
     * Short description of this function.
     */
    private static final String SHORT_DESCRIPTION =
            "Calculates, for each vertex, the (distance to the) closest "
            + "destination among the given possible destinations.";
    /**
     * Long description of this function.
     */
    private static final String LONG_DESCRIPTION =
            "<p> Example usage: "
            + "<center> "
            + "<code>" + SQL_ORDER + "</code> </center> "
            + "<p> Required parameters: "
            + "<ul> "
            + "<li> <code>output.edges</code> - The <code>output.edges</code> "
            + "table produced by <code>ST_Graph</code>, with an additional "
            + "column specifying the weight of each edge. "
            + "<li> <ul> "
            + "<li> <code>destination_table</code> - a table containing a "
            + "single column 'destination' consisting of the ids of the "
            + "desired destinations in the <code>output.nodes</code> table. "
            + "<li> <code>'dest1, dest2, ...'</code> - a string consisting "
            + "of a comma-separated list of destination ids. </ul></ul>"
            + "<p> Optional parameters: "
            + "<ul> "
            + "<li> <code>'weights_column'</code> - a string specifying "
            + "the name of the column of the input table that gives the weight "
            + "of each edge. If omitted, the graph is considered to be unweighted. "
            + "<li> <code>orientation</code> - a string specifying the "
            + "orientation of the graph: "
            + "<ul> "
            + "<li> '" + DIRECTED + " - " + EDGE_ORIENTATION_COLUMN + "' "
            + "<li> '" + REVERSED + " - " + EDGE_ORIENTATION_COLUMN + "' "
            + "<li> '" + UNDIRECTED + "'."
            + "</ul> The default orientation is " + DIRECTED + " with edge "
            + "orientations given by the geometries, though edge orientations "
            + "should most definitely be provided by the user. </ul>";
    /**
     * Description of this function.
     */
    private static final String DESCRIPTION =
            SHORT_DESCRIPTION + LONG_DESCRIPTION;
    /**
     * Result metadata.
     */
    public static final Metadata MD = new DefaultMetadata(
            new Type[]{
        TypeFactory.createType(Type.INT),
        TypeFactory.createType(Type.INT),
        TypeFactory.createType(Type.DOUBLE)},
            new String[]{
        GraphSchema.ID,
        GraphSchema.CLOSEST_DESTINATION,
        GraphSchema.DIST_TO_CLOSEST_DESTINATION});
    /**
     * Table of destinations.
     */
    private DataSet destinationTable = null;
    /**
     * Destinations array if a table is not used.
     */
    private int[] destinations = null;
    /**
     * Weight column name.
     */
    private String weightsColumn = null;
    /**
     * Global orientation string.
     */
    private String globalOrientation = null;
    /**
     * Edge orientation string.
     */
    private String edgeOrientationColumnName = null;
    /**
     * Logger.
     */
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(ST_Accessibility.class);

    @Override
    public DataSet evaluate(DataSourceFactory dsf, DataSet[] tables,
                            Value[] values, ProgressMonitor pm) throws
            FunctionException {

        // Recover the edges.
        final DataSet edges = tables[0];

        // Recover all other parameters.
        parseArguments(edges, tables, values);

        // Prepare the graph.
        KeyedGraph<VAccess, Edge> graph = prepareGraph(edges);

        // Compute and return results.
        DiskBufferDriver results = null;
        try {
            results = compute(dsf, graph);
        } catch (DriverException ex) {
            LOGGER.error(ex.toString());
        }
        return results;
    }

    /**
     * Parse all possible arguments for {@link ST_ShortestPathLength}.
     *
     * @param tables Input table(s)
     * @param values Arguments
     */
    private void parseArguments(DataSet edges, DataSet[] tables, Value[] values) {
        GraphFunctionParser parser = new GraphFunctionParser();
        int valuesIndex = 0;
        // (dest_table, ...)
        if (tables.length == 2) {
            destinationTable = tables[1];
        } // ('dest1, dest2, ...', ...)
        else {
            destinations = parser.parseDestinationsString(values[valuesIndex++]);
        }
        parser.parseOptionalArguments(edges, values, valuesIndex);
        globalOrientation = parser.getGlobalOrientation();
        edgeOrientationColumnName = parser.getEdgeOrientationColumnName();
        weightsColumn = parser.getWeightsColumn();
    }

    /**
     * Prepare the JGraphT graph from the given edges table.
     *
     * @param edges Edges table
     *
     * @return JGraphT graph
     */
    private KeyedGraph<VAccess, Edge> prepareGraph(final DataSet edges) {
        KeyedGraph<VAccess, Edge> graph;

        // Get the graph orientation.
        int graphType = -1;
        if (globalOrientation != null) {
            graphType = globalOrientation.equalsIgnoreCase(DIRECTED)
                    ? GraphSchema.DIRECT
                    : globalOrientation.equalsIgnoreCase(REVERSED)
                    ? GraphSchema.DIRECT_REVERSED
                    : globalOrientation.equalsIgnoreCase(UNDIRECTED)
                    ? GraphSchema.UNDIRECT
                    : -1;
        } else if (graphType == -1) {
            LOGGER.warn("Assuming a directed graph.");
            graphType = GraphSchema.DIRECT;
        }

        // Create the graph.
        if (weightsColumn != null) {
            graph = new WeightedGraphCreator<VAccess, Edge>(
                    edges,
                    graphType,
                    edgeOrientationColumnName,
                    VAccess.class,
                    Edge.class,
                    weightsColumn).prepareGraph();
        } else {
            graph = new GraphCreator<VAccess, Edge>(
                    edges,
                    graphType,
                    edgeOrientationColumnName,
                    VAccess.class,
                    Edge.class).prepareGraph();
        }
        return graph;
    }

    /**
     * Compute the distances and write them to a table.
     *
     * @param dsf   Data source factory
     * @param graph JGraphT graph
     *
     * @return The requested distances
     *
     * @throws DriverException
     */
    private DiskBufferDriver compute(DataSourceFactory dsf,
                                     KeyedGraph<VAccess, Edge> graph)
            throws DriverException {

        // Initialize the output.
        DiskBufferDriver output = new DiskBufferDriver(dsf, getMetadata(null));

        if (graph == null) {
            LOGGER.error("Null graph.");
        } else {
            // Get a DijkstraForAccessibility algo for the accessibility
            // calculation.
            DijkstraForAccessibility<Edge> dijsktra =
                    new DijkstraForAccessibility<Edge>(graph);
            // Prepare the destination set.
            Set<VAccess> destSet = new HashSet<VAccess>();

            // Destination table
            if (destinationTable != null) {
                // Make sure the destination table has a column named
                // DESTINATION.
                Metadata metadata = destinationTable.getMetadata();
                int destIndex = metadata.getFieldIndex(DESTINATION);
                if (destIndex == -1) {
                    throw new IllegalArgumentException(
                            "The destination table must contain "
                            + "a column named \'" + DESTINATION + "\'.");
                } else {
                    // Add the destinations.
                    for (int i = 0; i < destinationTable.getRowCount(); i++) {
                        int target = destinationTable.getRow(i)[destIndex]
                                .getAsInt();
                        destSet.add(graph.getVertex(target));
                    }
                }
            } // Destination string
            else if (destinations != null) {
                for (int i = 0; i < destinations.length; i++) {
                    destSet.add(graph.getVertex(destinations[i]));
                }
            } else {
                throw new IllegalArgumentException("No destinations specified.");
            }

            // Do the actual analysis.
            new AccessibilityAnalyzer<Edge>(graph, destSet).compute();
            // Store the result.
            for (VAccess node : graph.vertexSet()) {
                output.addValues(
                        ValueFactory.createValue(node.getID()),
                        ValueFactory.createValue(node.getClosestDestinationId()),
                        ValueFactory.createValue(
                        node.getDistanceToClosestDestination()));
            }
            // Clean-up
            output.writingFinished();
            output.open();
        }
        return output;
    }

    @Override
    public Metadata getMetadata(Metadata[] tables) throws DriverException {
        return MD;
    }

    /**
     * Returns all possible function signatures for specifying destinations in a
     * table.
     *
     * @return Destination table signatures
     */
    private TableFunctionSignature[] destinationTableSignatures() {
        return new TableFunctionSignature[]{
            // (d_t)
            new TableFunctionSignature(TableDefinition.ANY,
                                       TableArgument.GEOMETRY,
                                       TableArgument.ANY),
            // (d_t,w) | (d_t,o)
            new TableFunctionSignature(TableDefinition.ANY,
                                       TableArgument.GEOMETRY,
                                       TableArgument.ANY,
                                       ScalarArgument.STRING),
            // (d_t,w,o) | (d_t,o,w)
            new TableFunctionSignature(TableDefinition.ANY,
                                       TableArgument.GEOMETRY,
                                       TableArgument.ANY,
                                       ScalarArgument.STRING,
                                       ScalarArgument.STRING)
        };
    }

    /**
     * Returns all possible function signatures for specifying destinations in a
     * string.
     *
     * @return Destination string signatures
     */
    private TableFunctionSignature[] destinationStringSignatures() {
        return new TableFunctionSignature[]{
            // ('d')
            new TableFunctionSignature(TableDefinition.ANY,
                                       TableArgument.GEOMETRY,
                                       ScalarArgument.STRING),
            // ('d',w) | ('d',o)
            new TableFunctionSignature(TableDefinition.ANY,
                                       TableArgument.GEOMETRY,
                                       ScalarArgument.STRING,
                                       ScalarArgument.STRING),
            // ('d',w,o) | ('d',o,w)
            new TableFunctionSignature(TableDefinition.ANY,
                                       TableArgument.GEOMETRY,
                                       ScalarArgument.STRING,
                                       ScalarArgument.STRING,
                                       ScalarArgument.STRING)
        };
    }

    @Override
    public FunctionSignature[] getFunctionSignatures() {
        return ArrayConcatenator.
                concatenate(destinationTableSignatures(),
                            destinationStringSignatures());
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String getSqlOrder() {
        return SQL_ORDER;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }
}
