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

import org.gdms.data.DataSourceFactory;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.model.GraphEdge;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.parse.GraphFunctionParser;
import org.gdms.gdmstopology.process.GraphConnectivityUtilities;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;
import org.jgrapht.alg.ConnectivityInspector;
import org.orbisgis.progress.ProgressMonitor;

/**
 * Calculates the connected components of a given graph.
 *
 * <p> Creates a new table called
 * <code>connected_components</code> which lists all the vertices and to which
 * connected component they belong.
 *
 * <p> Example usage: <center>
 * <code>
 * SELECT * FROM ST_ConnectedComponents(input_table, 'weights_column'
 * [, orientation]);
 * </code> </center>
 *
 * <p> Required parameters: <ul> <li>
 * <code>input_table</code> - the input table. Specifically, this is the
 * <code>output_table_prefix.edges</code> table produced by {@link ST_Graph},
 * except that an additional column specifying the weight of each edge must be
 * added.
 * <li>
 * <code>'weights_column'</code> - the weights column.</ul>
 * <p> Optional parameter: <ul> <li>
 * <code>orientation</code> - an integer specifying the orientation of the
 * graph: <ul> <li> 1 if the graph is directed, <li> 2 if it is directed and we
 * wish to reverse the orientation of the edges, <li> 3 if the graph is
 * undirected. </ul> If no orientation is specified, we assume the graph is
 * directed. </ul>
 *
 * @author Adam Gouge
 */
public class ST_ConnectedComponents extends AbstractTableFunction {

    /**
     * The name of this function.
     */
    private static final String NAME = "ST_ConnectedComponents";
    /**
     * The SQL order of this function.
     */
    private static final String SQL_ORDER =
            "SELECT * FROM ST_ConnectedComponents("
            + "input_table, "
            + "'weights_column'"
            + "[, orientation]);";
    /**
     * Short description of this function.
     */
    private static final String SHORT_DESCRIPTION =
            "Calculates the connected components of a given graph. ";
    /**
     * Long description of this function.
     */
    private static final String LONG_DESCRIPTION =
            "<p> "
            + "Creates a new table called "
            + "<code>connected_components</code> "
            + "which lists all the vertices and to which "
            + "connected component they belong. "
            + "<p> "
            + "Required parameters: "
            + "<ul> "
            + "<li> "
            + "<code>input_table</code> - the input table. "
            + "Specifically, this is the "
            + "<code>output_table_prefix.edges</code> "
            + "table produced by "
            + "<code>ST_Graph</code>. "
            + "<li> "
            + "<code>'weights_column'</code> - the name of the weights "
            + "column. </ul>"
            + "<p> "
            + "Optional parameter: "
            + "<ul> "
            + "<li> "
            + "<code>orientation</code> - "
            + "an integer specifying the orientation of the graph: "
            + "<ul> "
            + "<li> 1 if the graph is directed, "
            + "<li> 2 if it is directed and we wish to reverse the "
            + "orientation of the edges, "
            + "<li> 3 if the graph is undirected. "
            + "</ul> " // end orientation list
            + "If no orientation is specified, we assume the graph is"
            + "directed. "
            + "</ul> "; // end required parameters list
    /**
     * Description of this function.
     */
    private static final String DESCRIPTION =
            SHORT_DESCRIPTION + LONG_DESCRIPTION;
    // OPTIONAL ARGUMENT
    /**
     * Specifies the orientation of the graph (default: directed).
     */
    private int orientation = GraphSchema.DIRECT;
    /**
     * An error message to be displayed when {@link #evaluate(
     * org.gdms.data.DataSourceFactory,
     * org.gdms.driver.DataSet[],
     * org.gdms.data.values.Value[],
     * org.orbisgis.progress.ProgressMonitor) evaluate} fails.
     */
    private static final String EVALUATE_ERROR =
            "Cannot compute the connected components.";

    /**
     * Evaluates the function to calculate the connected components of a graph.
     *
     * @param dsf    The {@link DataSourceFactory} used to parse the data set.
     * @param tables The input table. (This {@link DataSet} array will contain
     *               only one element since there is only one input table.)
     * @param values Array containing the other arguments.
     * @param pm     The progress monitor used to track the progress of the
     *               calculation.
     */
    @Override
    public DataSet evaluate(
            DataSourceFactory dsf,
            DataSet[] tables,
            Value[] values,
            ProgressMonitor pm)
            throws FunctionException {
        try {
            // Recover the DataSet.
            final DataSet dataSet = tables[0];
            // Set the weights column name.
            final String weightsColumn = values[0].getAsString();
            // Get the orientation
            parseOptionalArgument(values[1]);
            // Create the ConnectivityInspector.
            ConnectivityInspector<Integer, GraphEdge> inspector =
                    GraphConnectivityUtilities.
                    getConnectivityInspector(
                    dsf,
                    dataSet,
                    weightsColumn,
                    orientation,
                    pm);
            // Record a new table listing all the vertices and to which
            // connected component they belong.
            return GraphConnectivityUtilities.
                    registerConnectedComponents(dsf, inspector);
        } catch (Exception ex) {
            System.out.println(ex);
            throw new FunctionException(EVALUATE_ERROR, ex);
        }
    }

    /**
     * Parse the optional function argument at the given index.
     *
     * @param values Array containing the other arguments.
     * @param index  The index.
     *
     * @throws FunctionException
     */
    private void parseOptionalArgument(Value value) throws
            FunctionException {
        final int slotType = value.getType();
        if (slotType == Type.INT) {
            orientation = GraphFunctionParser.parseOrientation(value);
            if (!GraphFunctionParser.validOrientation(orientation)) {
                throw new FunctionException(
                        "Please enter a valid orientation: 1, 2 or 3.");
            }
        } else {
            throw new FunctionException(
                    "Please enter an integer orientation.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return NAME;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSqlOrder() {
        return SQL_ORDER;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return DESCRIPTION;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FunctionSignature[] getFunctionSignatures() {
        return new FunctionSignature[]{
            // No orientation specified.
            new TableFunctionSignature(
            TableDefinition.ANY,
            TableArgument.GEOMETRY,
            ScalarArgument.STRING),
            // Specify orientation.
            new TableFunctionSignature(
            TableDefinition.ANY,
            TableArgument.GEOMETRY,
            ScalarArgument.STRING,
            ScalarArgument.INT)
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Metadata getMetadata(Metadata[] tables) throws DriverException {
        return GraphConnectivityUtilities.createMetadata();
    }
}
