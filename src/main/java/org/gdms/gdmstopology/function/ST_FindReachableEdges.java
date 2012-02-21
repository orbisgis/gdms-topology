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

import com.vividsolutions.jts.geom.Geometry;
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
import org.gdms.gdmstopology.process.GraphAnalysis;
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
 * @author ebocher IRSTV FR CNRS 2488
 *
 */
public class ST_FindReachableEdges extends AbstractTableFunction {

        private DiskBufferDriver diskBufferDriver;

        @Override
        public DataSet evaluate(SQLDataSourceFactory dsf, DataSet[] tables, Value[] values, ProgressMonitor pm) throws FunctionException {
                try {
                        int source = values[0].getAsInt();
                        String fieldCost = values[1].getAsString();
                        DataSet sdsEdges = tables[0];
                        diskBufferDriver = new DiskBufferDriver(dsf, getMetadata(null));

                        if (values.length == 3) {
                                int method = values[2].getAsInt();
                                if (method == GraphAnalysis.DIRECT) {
                                        return computeDWMPath(dsf, sdsEdges, source, fieldCost, false, pm);

                                } else if (method == GraphAnalysis.DIRECT_REVERSED) {
                                        return computeDWMPath(dsf, sdsEdges, source, fieldCost, true, pm);
                                } else if (method == GraphAnalysis.UNDIRECT) {
                                        return computeWMPath(dsf, sdsEdges, source, fieldCost, pm);
                                } else {
                                        throw new FunctionException("1, 2 or 3 input value constante is needed to execute the function");
                                }

                        } else {
                                return computeDWMPath(dsf, sdsEdges, source, fieldCost, false, pm);
                        }

                } catch (DriverException ex) {
                        throw new FunctionException("Cannot find reachable edges", ex);
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
                return "ST_FindReachableEdges";
        }

        @Override
        public String getDescription() {
                return "Find reachable edges from one vertex to all other in a graph."
                        + "Optional argument : \n"
                        + "1 if the graph is directed\n"
                        + "2 if the graph is directed and edges are reversed."
                        + "3 if the graph is undirected\n";
        }

        @Override
        public String getSqlOrder() {
                return "SELECT * from ST_FindReachableEdges(table, 12, costField [,1]) );";
        }

        @Override
        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.GEOMETRY), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.DOUBLE), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{"the_geom", GraphSchema.ID, GraphSchema.START_NODE, GraphSchema.END_NODE, GraphSchema.WEIGHT, GraphSchema.WEIGHT_SUM});
                return md;
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT, ScalarArgument.STRING),
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT, ScalarArgument.STRING, ScalarArgument.INT)
                        };
        }

        private DiskBufferDriver computeWMPath(DataSourceFactory dsf, DataSet sds, int source, String fieldCost, ProgressMonitor pm) throws DriverException {
                WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, sds, pm);
                wMultigraphDataSource.setWeigthFieldIndex(fieldCost);
                ClosestFirstIterator<Integer, GraphEdge> cl = new ClosestFirstIterator<Integer, GraphEdge>(
                        wMultigraphDataSource, source);

                int previous = source;
                while (cl.hasNext()) {
                        Integer node = cl.next();
                        if (node != source) {
                                double length = cl.getShortestPathLength(node);
                                GraphEdge edge = cl.getSpanningTreeEdge(node);
                                Geometry geom = wMultigraphDataSource.getGeometry(edge);
                                diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(geom), ValueFactory.createValue(source), ValueFactory.createValue(previous), ValueFactory.createValue(node), ValueFactory.createValue(edge.getWeight()), ValueFactory.createValue(length)});
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
                int previous = source;
                while (cl.hasNext()) {
                        Integer node = cl.next();
                        if (node != source) {
                                double length = cl.getShortestPathLength(node);
                                GraphEdge edge = cl.getSpanningTreeEdge(node);
                                Geometry geom = dwMultigraphDataSource.getGeometry(edge);
                                diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(geom), ValueFactory.createValue(source), ValueFactory.createValue(previous), ValueFactory.createValue(node), ValueFactory.createValue(edge.getWeight()), ValueFactory.createValue(length)});
                                previous = node;
                        }
                }
                diskBufferDriver.writingFinished();
                diskBufferDriver.start();
                return diskBufferDriver;
        }
}
