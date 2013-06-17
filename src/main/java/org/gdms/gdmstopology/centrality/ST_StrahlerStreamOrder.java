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

import org.gdms.data.DataSourceFactory;
import org.gdms.data.schema.Metadata;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.parse.GraphFunctionParser;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;
import org.orbisgis.progress.ProgressMonitor;

/**
 * SQL function to calculate the Strahler numbers of the nodes of a given tree.
 *
 * <p> We do not check that the given input graph is in fact a tree. The user
 * must specify the root node.
 *
 * @author Adam Gouge
 */
public class ST_StrahlerStreamOrder extends AbstractTableFunction {

    /**
     * The name of this function.
     */
    private static final String NAME = "ST_StrahlerStreamOrder";
    /**
     * The SQL order of this function.
     */
    private static final String SQL_ORDER =
            "SELECT * FROM ST_StrahlerStreamOrder("
            + "input_table, "
            + "root_node;";
    /**
     * Short description of this function.
     */
    private static final String SHORT_DESCRIPTION =
            "Calculates the Strahler numbers for the given tree. ";
    /**
     * Long description of this function.
     */
    private static final String LONG_DESCRIPTION =
            "<p> "
            + "Creates a new table "
            + "which lists all the vertices of a tree and their respective "
            + "Strahler numbers. "
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
            + "<code>root_node</code> - an integer specifying the id of the "
            + "root node of the tree. </ul>";
    /**
     * Description of this function.
     */
    private static final String DESCRIPTION =
            SHORT_DESCRIPTION + LONG_DESCRIPTION;
    // REQUIRED ARGUMENT
    /**
     * The root node.
     */
    private int rootNode;
    /**
     * An error message to be displayed when {@link #evaluate(
     * org.gdms.data.DataSourceFactory,
     * org.gdms.driver.DataSet[],
     * org.gdms.data.values.Value[],
     * org.orbisgis.progress.ProgressMonitor) evaluate} fails.
     */
    private static final String EVALUATE_ERROR =
            "Cannot compute the Strahler stream order.";

    @Override
    public DataSet evaluate(DataSourceFactory dsf, DataSet[] tables,
                            Value[] values, ProgressMonitor pm) {
        // Recover the DataSet.
        final DataSet dataSet = tables[0];
        // Get the root node.
        rootNode = new GraphFunctionParser().parseSource(values[0]);
        // Return a new table listing all the vertices and to which
        // connected component they belong.
        return new StrahlerAnalyzer(dsf, pm, dataSet, rootNode).prepareDataSet();
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
            // input_table, root_node
            new TableFunctionSignature(
            TableDefinition.ANY,
            TableArgument.GEOMETRY,
            ScalarArgument.INT)
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Metadata getMetadata(Metadata[] tables) throws DriverException {
        return StrahlerAnalyzer.MD;
    }
}
