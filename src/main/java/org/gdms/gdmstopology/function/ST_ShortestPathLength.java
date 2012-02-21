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

import org.gdms.data.DataSourceFactory;
import org.gdms.data.SQLDataSourceFactory;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.driver.DataSet;
import org.gdms.gdmstopology.model.DWMultigraphDataSource;
import org.gdms.gdmstopology.model.GraphEdge;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.model.WMultigraphDataSource;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;
import org.orbisgis.progress.ProgressMonitor;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.jgrapht.graph.EdgeReversedGraph;

/**
 *
 * @author ebocher
 */
public class ST_ShortestPathLength extends AbstractTableFunction {

        private DiskBufferDriver diskBufferDriver;

        @Override
        public DataSet evaluate(SQLDataSourceFactory dsf, DataSet[] tables, Value[] values, ProgressMonitor pm) throws FunctionException {
                int source = values[0].getAsInt();
                String fieldCost = values[1].getAsString();
                try {
                        DataSet sdsEdges = tables[0];
                        diskBufferDriver = new DiskBufferDriver(dsf, getMetadata(null));
                        if (values.length == 4) {
                                if (values[2].getAsBoolean()) {
                                        return computeWMPath(dsf, sdsEdges, source, fieldCost, pm);
                                } else {
                                        return computeDWMPath(dsf, sdsEdges, source, fieldCost, values[3].getAsBoolean(), pm);
                                }

                        } else {
                                return computeDWMPath(dsf, sdsEdges, source, fieldCost, false, pm);
                        }

                } catch (DriverException ex) {
                        throw new FunctionException("Cannot compute the shortest path length", ex);
                }


        }

        @Override
        public void workFinished() throws DriverException {
                if (diskBufferDriver != null) {
                        diskBufferDriver.stop();
                }
        }

        @Override
        public String getName() {
                return "ST_ShortestPathLength";
        }

        @Override
        public String getDescription() {
                return "Return the shortest path length beetwen one vertex to all other based on a directed graph. True if the path is computed using an undirected graph."
                        + "The last boolean argument is used to reverse or not the edges.";
        }

        @Override
        public String getSqlOrder() {
                return "SELECT * from ST_ShortestPathLength(table, 12, costField,[,true, false]) );";
        }

        @Override
        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{GraphSchema.ID, GraphSchema.START_NODE, GraphSchema.END_NODE, GraphSchema.WEIGHT});
                return md;
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT, ScalarArgument.STRING),
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT, ScalarArgument.STRING, ScalarArgument.BOOLEAN, ScalarArgument.BOOLEAN)
                        };
        }

        
        private DiskBufferDriver computeWMPath(DataSourceFactory dsf, DataSet sds, int source, String fieldCost, ProgressMonitor pm) throws DriverException {
                WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, sds, pm);
                wMultigraphDataSource.setWeigthFieldIndex(fieldCost);
                ClosestFirstIterator<Integer, GraphEdge> cl = new ClosestFirstIterator<Integer, GraphEdge>(
                        wMultigraphDataSource, source);
                //First point added
                diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(source), ValueFactory.createValue(source), ValueFactory.createValue(0)});

                int previous = source;
                while (cl.hasNext()) {
                        Integer node = cl.next();
                        if (node != source) {
                                double length = cl.getShortestPathLength(node);
                                diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(previous), ValueFactory.createValue(node), ValueFactory.createValue(length)});
                                previous = node;
                        }
                }
                diskBufferDriver.writingFinished();
                diskBufferDriver.start();
                return diskBufferDriver;


        }

        private DiskBufferDriver computeDWMPath(DataSourceFactory dsf, DataSet sds, int source, String fieldCost, boolean reverseGraph, ProgressMonitor pm) throws DriverException {
                DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, sds, pm);
                dwMultigraphDataSource.setWeigthFieldIndex(fieldCost);
                ClosestFirstIterator<Integer, GraphEdge> cl;
                if (reverseGraph) {
                        EdgeReversedGraph edgeReversedGraph = new EdgeReversedGraph(dwMultigraphDataSource);
                        cl = new ClosestFirstIterator<Integer, GraphEdge>(
                                edgeReversedGraph, source);
                } else {
                        cl = new ClosestFirstIterator<Integer, GraphEdge>(
                                dwMultigraphDataSource, source);
                }
                //First point added
                diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(source), ValueFactory.createValue(source), ValueFactory.createValue(0)});

                int previous = source;
                while (cl.hasNext()) {
                        Integer node = cl.next();
                        if (node != source) {
                                double length = cl.getShortestPathLength(node);
                                diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(previous), ValueFactory.createValue(node), ValueFactory.createValue(length)});
                                previous = node;
                        }
                }
                diskBufferDriver.writingFinished();
                diskBufferDriver.start();
                return diskBufferDriver;
        }
}
