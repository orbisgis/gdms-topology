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
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.driver.DataSet;
import org.gdms.gdmstopology.model.GraphMetadataFactory;
import org.gdms.gdmstopology.process.GraphAnalysis;
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
public class ST_ShortestPathLength extends AbstractTableFunction {

        @Override
        public DataSet evaluate(SQLDataSourceFactory dsf, DataSet[] tables, Value[] values, ProgressMonitor pm) throws FunctionException {
                try {
                        int source = values[0].getAsInt();
                        String costField = values[1].getAsString();
                        if (values.length == 3) {
                                DiskBufferDriver diskBufferDriver = GraphAnalysis.getShortestPathLength(dsf, tables[0], source, costField, values[2].getAsInt(), pm);
                                diskBufferDriver.start();
                                return diskBufferDriver;

                        } else {
                                DiskBufferDriver diskBufferDriver = GraphAnalysis.getShortestPathLength(dsf, tables[0], source, costField, GraphAnalysis.DIRECT, pm);
                                diskBufferDriver.start();
                                return diskBufferDriver;
                        }

                } catch (Exception ex) {
                        throw new FunctionException("Cannot compute the shortest path length", ex);
                }


        }

        @Override
        public String getName() {
                return "ST_ShortestPathLength";
        }

        @Override
        public String getDescription() {
                return "Return the shortest path length beetwen one vertex to all other based on a directed graph.\n "
                        + "1 if the path is computing using a directed graph.\n"
                        + "2 if the path is computing using a directed graph and edges are reversed\n"
                        + "3 if the path is computing using a undirected.";
        }

        @Override
        public String getSqlOrder() {
                return "SELECT * from ST_ShortestPathLength(table, 12, costField[,2]) );";
        }

        @Override
        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                return GraphMetadataFactory.createDistancesMetadataGraph();
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT, ScalarArgument.STRING),
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT, ScalarArgument.STRING, ScalarArgument.INT)
                        };
        }
}
