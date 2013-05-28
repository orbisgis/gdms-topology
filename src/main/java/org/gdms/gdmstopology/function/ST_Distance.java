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
package org.gdms.gdmstopology.function;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
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
import org.gdms.gdmstopology.graphcreator.WeightedGraphCreator;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.parse.GraphFunctionParser;
import org.gdms.gdmstopology.utils.ArrayConcatenator;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;
import org.javanetworkanalyzer.alg.Dijkstra;
import org.javanetworkanalyzer.data.VWBetw;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.orbisgis.progress.ProgressMonitor;
import org.slf4j.LoggerFactory;

/**
 * Function for calculating distances (shortest path lengths).
 *
 * @author Adam Gouge
 */
public class ST_Distance extends AbstractTableFunction {

    /**
     * The name of this function.
     */
    private static final String NAME = "ST_Distance";
    /**
     * The SQL order of this function.
     */
    private static final String SQL_ORDER = "SELECT * FROM ST_Distance("
                                            + "output.edges, "
                                            + "source_dest_table OR source[, destination], "
                                            + "'weights_column' OR 1"
                                            + "[, orientation]);";
    /**
     * Short description of this function.
     */
    private static final String SHORT_DESCRIPTION =
            "Calculates the distance from a set of sources to a set of targets.";
    /**
     * Long description of this function.
     */
    private static final String LONG_DESCRIPTION =
            "<p><i>Note</i>: This function use Dijkstra's algorithm to "
            + "calculate the shortest path lengths from a set of sources "
            + "to a set of target. We assume the graph is connected. "
            + "<p> Example usage: "
            + "<center> "
            + "<code>SELECT * FROM ST_Distance("
            + "edges, "
            + "source_dest_table OR source[, destination], "
            + "'weights_column' OR 1"
            + "[, orientation]);</code> </center> "
            + "<p> Required parameters: "
            + "<ul> "
            + "<li> <code>output.edges</code> - the input table. Specifically, "
            + "this is the <code>output.edges</code> table "
            + "produced by <code>ST_Graph</code>, with an additional "
            + "column specifying the weight of each edge. "
            + "<li> <code>'weights_column'</code> - either a string specifying "
            + "the name of the column of the input table that gives the weight "
            + "of each edge, or 1 if the graph is to be considered unweighted. "
            + "</ul>"
            + "<p> Optional parameter: "
            + "<ul> "
            + "<li> <code>orientation</code> - an integer specifying the "
            + "orientation of the graph: "
            + "<ul> "
            + "<li> 1 if the graph is directed, "
            + "<li> 2 if it is directed and we wish to reverse the orientation "
            + "of the edges. "
            + "<li> 3 if the graph is undirected, "
            + "</ul> The default orientation is directed. </ul>";
    /**
     * Description of this function.
     */
    private static final String DESCRIPTION =
            SHORT_DESCRIPTION + LONG_DESCRIPTION;
    private int source = -1;
    private int destination = -1;
    private DataSet sourceDestinationTable = null;
    private String weightsColumn = null;
    private String orientation = null;
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(ST_Distance.class);
    public static final String SOURCE = "source";
    public static final String DESTINATION = "destination";
    public static final String DISTANCE = "distance";
    private static final Metadata md = new DefaultMetadata(
            new Type[]{TypeFactory.createType(Type.INT),
                       TypeFactory.createType(Type.INT),
                       TypeFactory.createType(Type.DOUBLE)},
            new String[]{SOURCE,
                         DESTINATION,
                         DISTANCE});

    @Override
    public DataSet evaluate(DataSourceFactory dsf, DataSet[] tables,
                            Value[] values, ProgressMonitor pm) throws
            FunctionException {

        // Recover the edges.
        final DataSet edges = tables[0];

        // Recover all other parameters.
        parseArguments(tables, values);

        // Prepare the graph.
        KeyedGraph<VWBetw, Edge> graph = prepareGraph(edges);

        // Compute and return results.
        DiskBufferDriver results = null;
        try {
            results = compute(graph, dsf);
        } catch (DriverException ex) {
            LOGGER.error(ex.toString());
        }
        return results;
    }

    @Override
    public String getName() {
        return NAME;
    }

    /**
     * Returns an example query using this function.
     *
     * @return An example query using this function.
     */
    @Override
    public String getSqlOrder() {
        return SQL_ORDER;
    }

    /**
     * Returns a description of this function.
     *
     * @return A description of this function.
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * Returns an array of all possible signatures of this function. Multiple
     * signatures arise from some arguments being optional.
     *
     * @return An array of all possible signatures of this function.
     */
    @Override
    public TableFunctionSignature[] getFunctionSignatures() {
        return ArrayConcatenator.
                concatenate(sourceDestinationSignatures(),
                            sourceSignatures(),
                            sourceDestinationTableSignatures());
    }

    /**
     * Returns all possible function signatures for finding all distances from a
     * given source.
     *
     * @return Source signatures
     */
    private TableFunctionSignature[] sourceSignatures() {
        return new TableFunctionSignature[]{
            // (s)
            new TableFunctionSignature(TableDefinition.GEOMETRY,
                                       ScalarArgument.INT),
            // (s,w) OR (s,o)
            new TableFunctionSignature(TableDefinition.GEOMETRY,
                                       ScalarArgument.INT,
                                       ScalarArgument.STRING),
            // (s,w,o) OR (s,o,w)
            new TableFunctionSignature(TableDefinition.GEOMETRY,
                                       ScalarArgument.INT,
                                       ScalarArgument.STRING,
                                       ScalarArgument.STRING)
        };
    }

    /**
     * Returns all possible function signatures for finding the distance from a
     * given source to a given destination.
     *
     * @return Source-destination signatures
     */
    private TableFunctionSignature[] sourceDestinationSignatures() {
        return new TableFunctionSignature[]{
            // (s,d)
            new TableFunctionSignature(TableDefinition.GEOMETRY,
                                       ScalarArgument.INT,
                                       ScalarArgument.INT),
            // (s,d,w) OR (s,d,o)
            new TableFunctionSignature(TableDefinition.GEOMETRY,
                                       ScalarArgument.INT,
                                       ScalarArgument.INT,
                                       ScalarArgument.STRING),
            // (s,d,w,o) OR (s,d,o,w)
            new TableFunctionSignature(TableDefinition.GEOMETRY,
                                       ScalarArgument.INT,
                                       ScalarArgument.INT,
                                       ScalarArgument.STRING,
                                       ScalarArgument.STRING)
        };
    }

    /**
     * Returns all possible function signatures for finding the distances from
     * the given sources to the given destinations.
     *
     * @return Source-destination signatures
     */
    private TableFunctionSignature[] sourceDestinationTableSignatures() {
        return new TableFunctionSignature[]{
            // (s_d_t)
            new TableFunctionSignature(TableDefinition.GEOMETRY,
                                       TableArgument.ANY),
            // (s_d_t,w) OR (s_d_t,o)
            new TableFunctionSignature(TableDefinition.GEOMETRY,
                                       TableArgument.ANY,
                                       ScalarArgument.STRING),
            // (s_d_t,w,o) OR (s_d_t,o,w)
            new TableFunctionSignature(TableDefinition.GEOMETRY,
                                       TableArgument.ANY,
                                       ScalarArgument.STRING,
                                       ScalarArgument.STRING)
        };
    }

    @Override
    public Metadata getMetadata(Metadata[] tables) throws DriverException {
        return md;
    }

    private void parseArguments(DataSet[] tables, Value[] values) {
        // (source_dest_table, ...)
        if (tables.length == 2) {
            sourceDestinationTable = tables[1];
            parseOptionalArguments(values, 0);
        } else {
            source = GraphFunctionParser.parseSource(values[0]);
            if (values.length > 1) {
                // (source, destination, ...)
                if (values[1].getType() == Type.INT) {
                    destination = GraphFunctionParser.parseTarget(values[1]);
                    parseOptionalArguments(values, 2);
                } // (source, ...)
                else {
                    parseOptionalArguments(values, 1);
                }
            }
        }
    }

    private void parseOptionalArguments(Value[] values, int argIndex) {
        if (values.length > argIndex) {
            while (values.length > argIndex) {
                parseStringArguments(values[argIndex++]);
            }
        } else if (orientation == null) {
            LOGGER.warn("Assuming an unweighted graph.");
        }
    }

    private void parseStringArguments(Value value) {
        if (value.getType() == Type.STRING) {
            String v = value.getAsString();
            if (v.equals("directed")
                || v.equals("reversed")
                || v.equals("undirected")) {
                orientation = v;
            } else {
                LOGGER.info("Setting weights column name to {}.", v);
                weightsColumn = v;
            }
        } else {
            throw new IllegalArgumentException("Weights and orientation "
                                               + "must be specified as strings.");
        }
    }

    private KeyedGraph<VWBetw, Edge> prepareGraph(final DataSet edges) {
        KeyedGraph<VWBetw, Edge> graph;

        int graphType = -1;
        if (orientation != null) {
            graphType = orientation.equals("directed")
                    ? GraphSchema.DIRECT
                    : orientation.equals("reversed")
                    ? GraphSchema.DIRECT_REVERSED
                    : orientation.equals("undirected")
                    ? GraphSchema.UNDIRECT
                    : -1;
        } else if (graphType == -1) {
            LOGGER.warn("Assuming a directed graph.");
            graphType = GraphSchema.DIRECT;
        }

        if (weightsColumn != null) {
            graph = new WeightedGraphCreator<VWBetw, Edge>(
                    edges,
                    graphType,
                    VWBetw.class,
                    Edge.class,
                    weightsColumn).prepareGraph();
        } else {
//            graph = new GraphCreator<VUBetw, Edge>(edges,
//                                                   graphType,
//                                                   VUBetw.class,
//                                                   Edge.class).prepareGraph();
            throw new UnsupportedOperationException(
                    "ST_Distance has not yet been implemented for "
                    + "unweighted graphs.");
        }
        return graph;
    }

    private DiskBufferDriver compute(
            KeyedGraph<VWBetw, Edge> graph,
            DataSourceFactory dsf) throws DriverException {

        DiskBufferDriver output = new DiskBufferDriver(dsf, getMetadata(null));
        if (graph == null) {
            LOGGER.error("Null graph.");
        } else {
            Dijkstra<VWBetw, Edge> dijkstra = new Dijkstra<VWBetw, Edge>(graph);

            // (source, destination, ...)
            if (source != -1 && destination != -1) {
                double distance =
                        dijkstra.oneToOne(graph.getVertex(source),
                                          graph.getVertex(destination));
                storeValue(source, destination, distance, output);
            } // (source, ...)
            else if (source != -1 && destination == -1) {
                // TODO: Replace this by calculate().
                Map<VWBetw, Double> distances =
                        dijkstra.oneToMany(graph.getVertex(source),
                                           graph.vertexSet());
                storeValues(source, distances, output);
            } // (source_dest_table, ...)
            else if (sourceDestinationTable != null) {
                Metadata metadata = sourceDestinationTable.getMetadata();
                int sourceIndex = metadata.getFieldIndex(SOURCE);
                int targetIndex = metadata.getFieldIndex(DESTINATION);
                if (sourceIndex == -1) {
                    throw new IllegalArgumentException(
                            "The source-destination table must contain "
                            + "a column named \'" + SOURCE + "\'.");
                } else if (targetIndex == -1) {
                    throw new IllegalArgumentException(
                            "The source-destination table must contain "
                            + "a column named \'" + DESTINATION + "\'.");
                } else {
                    Map<VWBetw, Set<VWBetw>> sourceDestinationMap =
                            prepareSourceDestinationMap(graph,
                                                        sourceIndex,
                                                        targetIndex);
                    if (sourceDestinationMap.isEmpty()) {
                        LOGGER.error(
                                "No sources/destinations requested.");
                    }

                    for (Entry<VWBetw, Set<VWBetw>> e
                         : sourceDestinationMap.entrySet()) {
                        Map<VWBetw, Double> distances =
                                dijkstra.oneToMany(e.getKey(), e.getValue());
                        storeValues(e.getKey().getID(), distances, output);
                    }
                }
            }

            output.writingFinished();
            output.open();
        }
        return output;
    }

    private Map<VWBetw, Set<VWBetw>> prepareSourceDestinationMap(
            KeyedGraph<VWBetw, Edge> graph,
            int sourceIndex,
            int targetIndex) throws DriverException {
        // Prepare the source-destination map.
        Map<VWBetw, Set<VWBetw>> map =
                new HashMap<VWBetw, Set<VWBetw>>();
        for (int i = 0;
             i < sourceDestinationTable.getRowCount();
             i++) {
            Value[] row = sourceDestinationTable.getRow(i);

            source = row[sourceIndex].getAsInt();
            VWBetw sourceVertex = graph.getVertex(source);
            destination = row[targetIndex].getAsInt();
            VWBetw destinationVertex = graph.getVertex(destination);

            Set<VWBetw> targets = map.get(sourceVertex);
            if (targets == null) {
                targets = new HashSet<VWBetw>();
                map.put(sourceVertex, targets);
            }
            targets.add(destinationVertex);
        }
        return map;
    }

    private void storeValues(int source,
                             Map<VWBetw, Double> distances,
                             DiskBufferDriver output) throws DriverException {
        for (Entry<VWBetw, Double> e : distances.entrySet()) {
            storeValue(source, e.getKey().getID(), e.getValue(), output);
        }
    }

    private void storeValue(int source, int target, double distance,
                            DiskBufferDriver output) throws DriverException {
        output.addValues(ValueFactory.createValue(source),
                         ValueFactory.createValue(target),
                         ValueFactory.createValue(distance));
    }
}
