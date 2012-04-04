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
package org.gdms.gdmstopology.function;

import org.gdms.data.SQLDataSourceFactory;
import org.gdms.data.schema.Metadata;
import org.gdms.data.values.Value;
import org.gdms.driver.DriverException;
import org.gdms.driver.DataSet;
import org.gdms.gdmstopology.model.GraphMetadataFactory;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.process.GraphUtilities;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;
import org.orbisgis.progress.ProgressMonitor;

/**
 *
 * @author ebocher
 */
public class ST_MFindReachableEdges extends AbstractTableFunction {

        @Override
        public DataSet evaluate(SQLDataSourceFactory dsf, DataSet[] tables, Value[] values, ProgressMonitor pm) throws FunctionException {
                try {
                        DataSet sdsEdges = tables[0];
                        DataSet vertexes = tables[1];

                        if (values.length == 2) {
                                return GraphUtilities.getMReachableEdges(dsf, sdsEdges, vertexes, values[0].getAsString(), Double.POSITIVE_INFINITY, values[1].getAsInt(), pm);
                        } else {
                                return GraphUtilities.getMReachableEdges(dsf, sdsEdges, vertexes, values[0].getAsString(), Double.POSITIVE_INFINITY, GraphSchema.DIRECT, pm);
                        }

                } catch (Exception ex) {
                        throw new FunctionException("Cannot find reachable edges", ex);
                }


        }

        @Override
        public String getName() {
                return "ST_MFindReachableEdges";
        }

        @Override
        public String getDescription() {
                return "Find reachable edges from sereral vertexes to all other in a graph.\n"
                        + "Optional argument : \n"
                        + "1 if the graph is directed\n"
                        + "2 if the graph is directed and edges are reversed."
                        + "3 if the graph is undirected\n";
        }

        @Override
        public String getSqlOrder() {
                return "SELECT * from ST_MFindReachableEdges(graph, nodes, costField [,1]) );";
        }

        @Override
        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                return GraphMetadataFactory.createMReachablesEdgesMetadata();
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), new TableArgument(TableDefinition.ANY), ScalarArgument.STRING),
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), new TableArgument(TableDefinition.ANY), ScalarArgument.STRING, ScalarArgument.INT)
                        };
        }
}
