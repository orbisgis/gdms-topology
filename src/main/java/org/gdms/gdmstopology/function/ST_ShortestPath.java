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

        private DiskBufferDriver dataSet;

        @Override
        public DataSet evaluate(SQLDataSourceFactory dsf, DataSet[] tables, Value[] values, ProgressMonitor pm) throws FunctionException {
                int source = values[0].getAsInt();
                int target = values[1].getAsInt();
                try {
                        if (values.length == 3) {
                                if (values[2].getAsBoolean()) {
                                        dataSet = computeWMPath(dsf, tables[0], source, target, pm);
                                        dataSet.start();
                                        return dataSet;

                                } else {
                                        dataSet = computeDWMPath(dsf, tables[0], source, target, pm);
                                        dataSet.start();
                                        return dataSet;
                                }

                        } else {
                                dataSet = computeDWMPath(dsf, tables[0], source, target, pm);
                                dataSet.start();
                                return dataSet;
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
                return "Return the shortest path beetwen two vertexes using the Dijkstra algorithm.";
        }

        @Override
        public String getSqlOrder() {
                return "SELECT * from  ST_ShortestPath(table,12, 10 [,true]);";
        }

        @Override
        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.GEOMETRY), TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{"the_geom", GraphSchema.ID, GraphSchema.START_NODE, GraphSchema.END_NODE, GraphSchema.WEIGTH});
                return md;
        }

        @Override
        public void workFinished() throws DriverException {
                dataSet.stop();
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new TableFunctionSignature(TableDefinition.ANY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT, ScalarArgument.INT),
                                new TableFunctionSignature(TableDefinition.ANY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT,
                                ScalarArgument.INT, ScalarArgument.BOOLEAN)
                        };
        }

        private DiskBufferDriver computeDWMPath(DataSourceFactory dsf, DataSet dataSet, Integer source, Integer target, ProgressMonitor pm) throws DriverException {
                DWMultigraphDataSource dWMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
                List<GraphEdge> result = GraphAnalysis.getShortestPath(dWMultigraphDataSource, source, target);
                DiskBufferDriver diskBufferDriver = new DiskBufferDriver(dsf, getMetadata(null));
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

        private DiskBufferDriver computeWMPath(DataSourceFactory dsf, DataSet dataSet, int source, int target, ProgressMonitor pm) throws DriverException {
                WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, dataSet, pm);
                List<GraphEdge> result = GraphAnalysis.getShortestPath(wMultigraphDataSource, source, target);
                DiskBufferDriver diskBufferDriver = new DiskBufferDriver(dsf, getMetadata(null));
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
