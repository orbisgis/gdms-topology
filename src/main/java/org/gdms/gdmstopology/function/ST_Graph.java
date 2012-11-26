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

import org.gdms.gdmstopology.process.NetworkGraphBuilder;
import java.io.IOException;
import org.gdms.data.NonEditableDataSourceException;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.values.Value;
import org.gdms.driver.DriverException;
import org.gdms.driver.driverManager.DriverLoadException;
import org.gdms.sql.function.FunctionSignature;
import org.orbisgis.progress.ProgressMonitor;
import org.gdms.driver.DataSet;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.executor.AbstractExecutorFunction;
import org.gdms.sql.function.executor.ExecutorFunctionSignature;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;

/**
 * Constructs a mathematical graph based on the data contained in the input
 * table.
 *
 * <p> Example usage: <center>
 * {@code EXECUTE ST_Graph(input_table[, tolerance, orient_by_slope, 'output_table_prefix']);}
 * </center>
 *
 * <p> Concretely, this function produces two tables, containing the indicated
 * columns: <ol> <li> {@code output_table_prefix.nodes} - A new table with
 * columns: <ul> <li> {@code the_geom} - a representation of each node as a
 * {@code POINT}. <li> {@code id} - a unique integer id assigned to each node.
 * </ul> <li> {@code output_table_prefix.edges} - A new table containing all the
 * information contained in the input table (though the rows are not in the same
 * order), as well as three new columns: <ul> <li> {@code id} - a unique integer
 * id assigned to each edge. <li> {@code start_node} - the {@code id} of the
 * start node defined in the nodes table.<li> {@code end_node} - the {@code id}
 * of the end node defined in the nodes table.</ul> </ol>
 *
 * <p> Required parameter: <ul> <li> {@code input_table} - the input table. This
 * table should contain one-dimensional geometries. <li> </ul>
 *
 * <p> Optional parameters: <ul> <li> {@code tolerance} - a double used to
 * define how easily closely neighboring nodes "snap together" to become a
 * single node. More precisely, a tolerance value of <i>r</i> defines a disk of
 * radius <i>r</i> around each node. If two disks intersect, then the
 * corresponding nodes are assigned the same {@code id} by preserving the
 * {@code id} of the first node found in the index. The
 * {@code output_table_prefix.edges} table is updated to reflect these labeling
 * changes. If more than two disks intersect, a similar procedure is applied. A
 * standard value for the tolerance is {@code 0.01}. <li>
 * {@code orient_by_slope} - a boolean to indicate whether the edges should be
 * oriented according to slope. That is, a value of {@code true} will orient
 * each edge from the node with larger <i>z</i>-coordinate to the node with
 * smaller <i>z</i>-coordinate. A value of {@code false} will orient the graph
 * according to the input geometry. That is, the orientation will be from the
 * first point of a segment to the last point of the segment. <li>
 * {@code 'output_table_prefix'} - a string used to prefix the names of the two
 * output tables. </ul>
 *
 * <p> October 12, 2012: Documentation added by Adam Gouge.
 *
 * @author Erwan Bocher
 */
// TODO: Describe the geometry data type in the input table.
// 2D: Multipolygons, the geometry is preserved. "Edges" are faces, with
// nodes lieing on the boundary.
// 1D: Works as expected.
// 0D: Keeps all the same nodes (as points), forgets all other info, adds ids.
// As for edges, they are exactly the same as the nodes, with the start and end 
// nodes identical to the node(=edge) id.
// TODO: Check how orientation works.
// TODO: First node found in the index?
// TODO: How is the order determined in output_table_prefix.edges?
public class ST_Graph extends AbstractExecutorFunction {

    /**
     * Returns the name of this function. This name will be used in SQL
     * statements.
     *
     * @return The name of this function.
     */
    @Override
    public String getName() {
        return "ST_Graph";
    }

    /**
     * Returns an example query using this function.
     *
     * @return An example query using this function.
     */
    @Override
    public String getSqlOrder() {
        return "EXECUTE ST_Graph(input_table[, tolerance, orient_by_slope, 'output_table_prefix']);";
    }

    /**
     * Returns a description of this function.
     *
     * @return A description of this function.
     */
    @Override
    public String getDescription() {
        return "Constructs a mathematical graph based on the data contained "
                + "in the <code>input_table</code>. This table should contain "
                + "one-dimensional geometries."
                
                + "<p>The <code>tolerance</code> is a double used to define how easily "
                + "closely neighboring nodes 'snap together' to become a single "
                + "node. More precisely, a tolerance value of <i>r</i> defines a disk "
                + "of radius <i>r</i> around each node. If two disks intersect, then "
                + "the corresponding nodes are assigned the same id by "
                + "preserving the id of the first node found in the index. "
                + "The <code>output_table_prefix.edges</code> table is updated to "
                + "reflect these labeling changes. If more than two disks intersect,"
                + " a similar procedure is applied. A standard value for the "
                + "tolerance is <code>0.01</code>."
                
                + "<p>The boolean <code>orient_by_slope</code> indicates whether the edges "
                + "should be oriented according to slope. That is, a value of "
                + "<code>true</code> will orient each edge from the node with larger "
                + "<i>z</i>-coordinate to the node with smaller <i>z</i>-coordinate. A value "
                + "of <code>false</code> will orient the graph according to the input "
                + "geometry. That is, the orientation will be from the first "
                + "point of a segment to the last point of the segment. "
                
                + "<p>Finally, <code>output_table_prefix</code> prefixes the names of the two "
                + "output tables (<code>.nodes</code> and <code>.edges</code>).";
    }

    /**
     * Evaluates the function in order to build the graph.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param tables The input table. (This {@link DataSet} array will contain
     * only one element since there is only one input table.)
     * @param values Array containing the optional arguments.
     * @param pm The progress monitor used to track the progress of the graph
     * construction.
     * @throws FunctionException
     */
    @Override
    public void evaluate(
            DataSourceFactory dsf,
            DataSet[] tables,
            Value[] values,
            ProgressMonitor pm)
            throws FunctionException {
        try {
            // Recover the DataSet.
            final DataSet dataSet = tables[0];
            // Initialize a NetworkGraphBuilder.
            NetworkGraphBuilder graphNetwork = new NetworkGraphBuilder(dsf, pm);
            // If no output table name is given, set the output table name 
            // by a unique identifier.
            graphNetwork.setOutput_name(dsf.getUID());
            // Take the optional values into account.
            if (values.length == 1) {
                graphNetwork.setTolerance(values[0].getAsDouble());
            } else if (values.length == 2) {
                graphNetwork.setTolerance(values[0].getAsDouble());
                graphNetwork.setZDirection(values[1].getAsBoolean());
            } else if (values.length == 3) {
                graphNetwork.setTolerance(values[0].getAsDouble());
                graphNetwork.setZDirection(values[1].getAsBoolean());
                graphNetwork.setOutput_name(values[2].getAsString());
            }
            // Build the actual graph.
            graphNetwork.buildGraph(dataSet);
        } catch (IOException e) {
            throw new FunctionException(e);
        } catch (DriverLoadException e) {
            throw new FunctionException(e);
        } catch (DriverException e) {
            throw new FunctionException(e);
        } catch (NonEditableDataSourceException e) {
            throw new FunctionException(e);
        }
    }

    /**
     * Returns an array of all possible signatures of this function. Multiple
     * signatures arise from some arguments being optional.
     *
     * <p> Possible signatures: <OL> <li> {@code (TABLE)} <li>
     * {@code (TABLE,DOUBLE)} <li> {@code (TABLE,DOUBLE,BOOLEAN)} <li>
     * {@code (TABLE,DOUBLE,BOOLEAN,STRING)} </OL>
     *
     * @return An array of all possible signatures of this function.
     */
    @Override
    public FunctionSignature[] getFunctionSignatures() {
        return new FunctionSignature[]{
                    // First possible signature.
                    new ExecutorFunctionSignature(
                    new TableArgument(TableDefinition.GEOMETRY)),
                    // Second possible signature.
                    new ExecutorFunctionSignature(
                    new TableArgument(TableDefinition.GEOMETRY),
                    ScalarArgument.DOUBLE),
                    // Third possible signature.
                    new ExecutorFunctionSignature(
                    new TableArgument(TableDefinition.GEOMETRY),
                    ScalarArgument.DOUBLE,
                    ScalarArgument.BOOLEAN),
                    // Fourth possible signature.
                    new ExecutorFunctionSignature(
                    new TableArgument(TableDefinition.GEOMETRY),
                    ScalarArgument.DOUBLE,
                    ScalarArgument.BOOLEAN,
                    ScalarArgument.STRING)
                };
    }
}
