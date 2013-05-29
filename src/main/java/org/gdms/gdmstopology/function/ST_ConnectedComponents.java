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
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.parse.GraphFunctionParser;
import org.gdms.gdmstopology.process.GraphConnectivityInspector;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;
import org.orbisgis.progress.ProgressMonitor;

/**
 * Calculates the connected components of the given <b>undirected</b> graph.
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
            "SELECT * FROM " + NAME + "(edges);";
    /**
     * Short description of this function.
     */
    private static final String SHORT_DESCRIPTION =
            "Calculates the connected components of the given "
            + "<b>undirected</b> graph. ";
    /**
     * Long description of this function.
     */
    private static final String LONG_DESCRIPTION =
            "<p> Lists every vertex and to which connected component it belongs."
            + "<p> Required parameter: "
            + "<ul> <li> "
            + "<code>output.edges</code> - the input table. Specifically, "
            + "this is the <code>output.edges</code> table "
            + "produced by <code>ST_Graph</code>. </ul>";
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
            ProgressMonitor pm) {
        // Return a new table listing all the vertices and to which
        // connected component they belong.
        return new GraphConnectivityInspector(
                dsf,
                pm,
                tables[0]).prepareDataSet();
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
            new TableFunctionSignature(
            TableDefinition.ANY,
            TableArgument.GEOMETRY)};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Metadata getMetadata(Metadata[] tables) throws DriverException {
        return GraphConnectivityInspector.MD;
    }
}
