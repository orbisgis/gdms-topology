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

import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import org.gdms.gdmstopology.centrality.GraphAnalyzer;
import org.gdms.gdmstopology.graphcreator.WeightedGraphCreator;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.parse.GraphFunctionParser;
import org.gdms.gdmstopology.utils.ArrayConcatenator;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.executor.ExecutorFunctionSignature;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;
import org.javanetworkanalyzer.alg.Dijkstra;
import org.javanetworkanalyzer.data.VWBetw;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.WeightedKeyedGraph;
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
    /**
     * Specifies the weight column (or 1 in the case of an unweighted graph).
     */
    private String weightsColumn;
    private static final org.slf4j.Logger LOGGER =
            LoggerFactory.getLogger(ST_Distance.class);

    @Override
    public DataSet evaluate(DataSourceFactory dsf, DataSet[] tables,
                            Value[] values, ProgressMonitor pm) throws
            FunctionException {
        // REQUIRED PARAMETERS

        // FIRST PARAMETER: Recover the DataSet. There is only one table.
        final DataSet edges = tables[0];

        // SECOND PARAMETER: Source
        int source = GraphFunctionParser.parseSource(values[0]);

        // THIRD PARAMETER: Destination
        int target = GraphFunctionParser.parseTarget(values[1]);

        // FOUR PARAMETER: Either 1 or weight column name.
        weightsColumn = GraphFunctionParser.parseWeight(values[2]);

        WeightedKeyedGraph<VWBetw, Edge> graph =
                new WeightedGraphCreator<VWBetw, Edge>(
                edges,
                GraphSchema.DIRECT,
                VWBetw.class,
                Edge.class,
                weightsColumn).prepareGraph();

        Dijkstra<VWBetw, Edge> dijkstra = new Dijkstra<VWBetw, Edge>(graph);

        double distance = dijkstra.oneToOne(graph.getVertex(source),
                                            graph.getVertex(target));

        DiskBufferDriver output = null;
        try {
            output = new DiskBufferDriver(dsf, getMetadata(null));
            output.addValues(ValueFactory.createValue(source),
                             ValueFactory.createValue(target),
                             ValueFactory.createValue(distance));
            output.writingFinished();
            output.open();
        } catch (DriverException ex) {
            LOGGER.error(ex.toString());
        }

        return output;
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
            //            
            //            + "output.edges, "
            //            + "source_dest_table OR source[, destination], "
            //            + "'weights_column' OR 1"
            //            + "[, orientation]);
            // For now, just take care of the source, destination case, 
            // no orientation.
            // (output.edges, source, destination, weight)
            new TableFunctionSignature(TableDefinition.GEOMETRY,
                                       ScalarArgument.INT,
                                       ScalarArgument.INT,
                                       weight)};
    }

    @Override
    public Metadata getMetadata(Metadata[] tables) throws DriverException {
        return new DefaultMetadata(
                new Type[]{TypeFactory.createType(Type.INT),
                           TypeFactory.createType(Type.INT),
                           TypeFactory.createType(Type.DOUBLE)},
                new String[]{"source",
                             "destination",
                             "distance"});
    }
}
