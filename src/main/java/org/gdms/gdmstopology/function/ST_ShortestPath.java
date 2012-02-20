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
import java.util.List;
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

/**
 *
 * @author ebocher
 */
public class ST_ShortestPath extends AbstractTableFunction {
        
        private DiskBufferDriver diskBufferDriver;
        
        @Override
        public DataSet evaluate(SQLDataSourceFactory dsf, DataSet[] tables, Value[] values, ProgressMonitor pm) throws FunctionException {
                int source = values[0].getAsInt();
                int target = values[1].getAsInt();
                String costField = values[2].getAsString();
                try {
                        diskBufferDriver = new DiskBufferDriver(dsf, getMetadata(null));
                        if (values.length == 4) {
                                if (values[3].getAsBoolean()) {
                                        diskBufferDriver = computeWMPath(dsf, tables[0], source, target, costField, pm);
                                        diskBufferDriver.start();
                                        return diskBufferDriver;
                                        
                                } else {
                                        diskBufferDriver = computeDWMPath(dsf, tables[0], source, target, costField, pm);
                                        diskBufferDriver.start();
                                        return diskBufferDriver;
                                }
                                
                        } else {
                                diskBufferDriver = computeDWMPath(dsf, tables[0], source, target, costField, pm);
                                diskBufferDriver.start();
                                return diskBufferDriver;
                        }
                } catch (DriverException ex) {
                        throw new FunctionException("Cannot compute the shortest path", ex);
                }
                
                
        }
        
        @Override
        public String getName() {
                return "ST_ShortestPath";
        }
        
        @Override
        public String getDescription() {
                return "Return the shortest path beetwen two vertexes using the Dijkstra algorithm.\n"
                        + "Optional arguments : \n"
                        + "true is the graph is undirected\n"
                        + "true is the graph is reversed.";
        }
        
        @Override
        public String getSqlOrder() {
                return "SELECT * from  ST_ShortestPath(table,12, 10, costField [,true, false]);";
        }
        
        @Override
        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.GEOMETRY), TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{"the_geom", GraphSchema.ID, GraphSchema.START_NODE, GraphSchema.END_NODE, GraphSchema.WEIGHT});
                return md;
        }
        
        @Override
        public void workFinished() throws DriverException {
                if (diskBufferDriver != null) {
                        diskBufferDriver.stop();
                }
        }
        
        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT, ScalarArgument.INT),
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT,
                                ScalarArgument.INT, ScalarArgument.BOOLEAN)
                        };
        }
        
        private DiskBufferDriver computeDWMPath(DataSourceFactory dsf, DataSet dataSet, Integer source, Integer target, String costField, ProgressMonitor pm) throws DriverException {
                DWMultigraphDataSource dWMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                dWMultigraphDataSource.setWeigthFieldIndex(costField);
                List<GraphEdge> result = GraphAnalysis.getShortestPath(dWMultigraphDataSource, source, target);
                if (result != null) {
                        int k = 0;
                        for (GraphEdge graphEdge : result) {
                                Geometry geometry = dWMultigraphDataSource.getGeometry(graphEdge);
                                diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(geometry),
                                                ValueFactory.createValue(k++),
                                                ValueFactory.createValue(graphEdge.getSource()),
                                                ValueFactory.createValue(graphEdge.getTarget()),
                                                ValueFactory.createValue(graphEdge.getWeight())});
                        }
                }
                diskBufferDriver.writingFinished();
                return diskBufferDriver;
        }
        
        private DiskBufferDriver computeWMPath(DataSourceFactory dsf, DataSet dataSet, int source, int target, String costField, ProgressMonitor pm) throws DriverException {
                WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, dataSet, pm);
                wMultigraphDataSource.setWeigthFieldIndex(costField);
                List<GraphEdge> result = GraphAnalysis.getShortestPath(wMultigraphDataSource, source, target);
                if (result != null) {
                        int k = 0;
                        for (GraphEdge graphEdge : result) {
                                Geometry geometry = wMultigraphDataSource.getGeometry(graphEdge);
                                diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(geometry),
                                                ValueFactory.createValue(k++),
                                                ValueFactory.createValue(graphEdge.getSource()),
                                                ValueFactory.createValue(graphEdge.getTarget()),
                                                ValueFactory.createValue(graphEdge.getWeight())});
                                
                        }
                }
                diskBufferDriver.writingFinished();
                return diskBufferDriver;
        }
}
