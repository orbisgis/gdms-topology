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
package org.gdms.gdmstopology.centrality;

import org.gdms.data.DataSourceFactory;
import org.gdms.data.schema.Metadata;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.function.ST_ShortestPathLength;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.DIRECTED;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.POSSIBLE_ORIENTATIONS;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.REVERSED;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.UNDIRECTED;
import org.gdms.gdmstopology.graphcreator.GraphCreator;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.parse.GraphFunctionParser;
import org.gdms.gdmstopology.utils.ArrayConcatenator;
import org.gdms.source.SourceManager;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.executor.AbstractExecutorFunction;
import org.gdms.sql.function.executor.ExecutorFunctionSignature;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.orbisgis.progress.ProgressMonitor;
import org.slf4j.LoggerFactory;

/**
 * SQL function to perform graph analysis on all nodes of a given graph.
 *
 * <p><i>Note</i>: This function use Dijkstra's algorithm to calculate, for each
 * node, all possible shortest paths to all the other nodes (we assume the graph
 * is connected). These calculations are intense and can take a long time to
 * complete.
 *
 * @author Adam Gouge
 */
public class ST_GraphAnalysis extends AbstractExecutorFunction {

    /**
     * The name of this function.
     */
    private static final String NAME = "ST_GraphAnalysis";
    /**
     * The SQL order of this function.
     */
    private static final String SQL_ORDER =
            "EXECUTE " + NAME + "("
            + "output.edges"
            + "[, 'weights_column']"
            + "[, " + POSSIBLE_ORIENTATIONS + "]);";
    /**
     * Short description of this function.
     */
    private static final String SHORT_DESCRIPTION =
            "Performs graph analysis on all nodes of the given graph.";
    /**
     * Long description of this function.
     */
    private static final String LONG_DESCRIPTION =
            "<p><i>Note</i>: This function use Dijkstra's algorithm to "
            + "calculate, for each node, all possible shortest paths to all "
            + "the other nodes (we assume the graph is connected). These "
            + "calculations are intense and can take a long time to complete."
            + "<p> Example usage: "
            + "<center> "
            + "<code>" + SQL_ORDER + "</code> </center> "
            + "<p> Required parameter: "
            + "<ul> "
            + "<li> <code>output.edges</code> - the input table. The "
            + "<code>output_table_prefix.edges</code> table "
            + "produced by <code>ST_Graph</code>, with an optional additional "
            + "column specifying the weight of each edge and an optional "
            + "additional column specifying the orientation of each edge:"
            + "<ul><li> " + GraphCreator.DIRECTED_EDGE + " directed"
            + "<li> " + GraphCreator.REVERSED_EDGE + " reversed"
            + "<li> " + GraphCreator.UNDIRECTED_EDGE + " undirected. </ul></ul>"
            + "<p> Optional parameters: "
            + "<ul> "
            + "<li> <code>'weights_column'</code> - a string specifying "
            + "the name of the column of the input table that gives the weight "
            + "of each edge. If omitted, the graph is considered to be unweighted. "
            + "<li> <code>orientation</code> - a string specifying the "
            + "orientation of the graph: "
            + "<ul> "
            + "<li> '" + DIRECTED + " - " + ST_ShortestPathLength.EDGE_ORIENTATION_COLUMN + "' "
            + "<li> '" + REVERSED + " - " + ST_ShortestPathLength.EDGE_ORIENTATION_COLUMN + "' "
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
            LoggerFactory.getLogger(ST_GraphAnalysis.class);

    /**
     * Evaluates the function to calculate the centrality indices.
     *
     * @param dsf    The {@link DataSourceFactory} used to parse the data set.
     * @param tables The input table. (This {@link DataSet} array will contain
     *               only one element since there is only one input table.)
     * @param values Array containing the other arguments.
     * @param pm     The progress monitor used to track the progress of the
     *               calculation.
     *
     * @return The {@link DataSet} containing the shortest path.
     *
     * @throws FunctionException
     */
    @Override
    public void evaluate(
            DataSourceFactory dsf,
            DataSet[] tables,
            Value[] values,
            ProgressMonitor pm) {
        final DataSet edges = tables[0];
        parseArguments(edges, tables, values);
        doAnalysisAccordingToUserInput(dsf, edges, pm);
    }

    /**
     * Registers closeness centrality according to the SQL arguments provided by
     * the user.
     *
     * @param dsf   The {@link DataSourceFactory} used to parse the data set.
     * @param edges The edges table.
     * @param pm    The progress monitor used to track the progress of the
     *              calculation.
     *
     * @throws GraphException
     * @throws DriverException
     */
    private void doAnalysisAccordingToUserInput(
            DataSourceFactory dsf,
            DataSet edges,
            ProgressMonitor pm) {
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

        GraphAnalyzer analyzer = (weightsColumn == null)
                ? // Unweighted graph
                new UnweightedGraphAnalyzer(
                dsf, edges, pm, graphType, edgeOrientationColumnName)
                : // Weighted graph
                new WeightedGraphAnalyzer(
                dsf, edges, pm, graphType, edgeOrientationColumnName,
                weightsColumn);

        final SourceManager sourceManager = dsf.getSourceManager();
        // Nodes table
        final DiskBufferDriver nodesDriver = analyzer.prepareDataSet();
        sourceManager.register(sourceManager.getUniqueName("node_centrality"),
                nodesDriver.getFile());
        // Edges table
        final DiskBufferDriver edgesDriver = analyzer.getEdgesDriver();
        sourceManager.register(sourceManager.getUniqueName("edge_centrality"),
                edgesDriver.getFile());
    }

    /**
     * Returns the name of this function. This name will be used in SQL
     * statements.
     *
     * @return The name of this function.
     */
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
    public FunctionSignature[] getFunctionSignatures() {
        return ArrayConcatenator.
                concatenate(unweightedFunctionSignatures(),
                            weightedFunctionSignatures());
    }

    /**
     * Returns all possible function signatures for unweighted graph analyzers.
     *
     * @return Unweighted function signatures.
     */
    private FunctionSignature[] unweightedFunctionSignatures() {
        return possibleFunctionSignatures(ScalarArgument.INT);
    }

    /**
     * Returns all possible function signatures for weighted graph analyzers.
     *
     * @return Weighted function signatures.
     */
    private FunctionSignature[] weightedFunctionSignatures() {
        return possibleFunctionSignatures(ScalarArgument.STRING);
    }

    /**
     * Returns all possible function signatures according to whether the graph
     * analyzer is weighted or unweighted.
     *
     * @return Possible function signatures according to weight.
     */
    private FunctionSignature[] possibleFunctionSignatures(
            ScalarArgument weight) {
        return new FunctionSignature[]{
            // (input_table, weight)
            new ExecutorFunctionSignature(
            TableArgument.GEOMETRY,
            weight),
            // (input_table, weight,
            //     'output_table_prefix')
            new ExecutorFunctionSignature(
            TableArgument.GEOMETRY,
            weight,
            ScalarArgument.STRING),
            // (input_table, weight,
            //     orientation)
            new ExecutorFunctionSignature(
            TableArgument.GEOMETRY,
            weight,
            ScalarArgument.INT),
            // (input_table, weight,
            //     'output_table_prefix', orientation)
            new ExecutorFunctionSignature(
            TableArgument.GEOMETRY,
            weight,
            ScalarArgument.STRING,
            ScalarArgument.INT),
            // (input_table, weight,
            //     orientation, 'output_table_prefix')
            new ExecutorFunctionSignature(
            TableArgument.GEOMETRY,
            weight,
            ScalarArgument.INT,
            ScalarArgument.STRING)};
    }

    /**
     * Parse all possible arguments for {@link ST_ShortestPathLength}.
     *
     * @param tables Input table(s)
     * @param values Arguments
     */
    private void parseArguments(DataSet edges, DataSet[] tables, Value[] values) {
        GraphFunctionParser parser = new GraphFunctionParser();
        parser.parseOptionalArguments(edges, values, 0);
        globalOrientation = parser.getGlobalOrientation();
        edgeOrientationColumnName = parser.getEdgeOrientationColumnName();
        weightsColumn = parser.getWeightsColumn();
    }
}
