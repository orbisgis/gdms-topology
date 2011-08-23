package org.gdms.gdmstopology.function;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.ExecutionException;
import org.gdms.data.SpatialDataSourceDecorator;
import org.gdms.data.metadata.DefaultMetadata;
import org.gdms.data.metadata.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.driver.ObjectDriver;
import org.gdms.gdmstopology.model.DWMultigraphDataSource;
import org.gdms.gdmstopology.model.GraphEdge;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.model.WMultigraphDataSource;
import org.gdms.sql.customQuery.CustomQuery;
import org.gdms.sql.customQuery.TableDefinition;
import org.gdms.sql.function.Argument;
import org.gdms.sql.function.Arguments;
import org.orbisgis.progress.IProgressMonitor;
import org.jgrapht.traverse.ClosestFirstIterator;

/**
 *
 * @author ebocher
 */
public class ST_ShortestPathLength implements CustomQuery {

        @Override
        public ObjectDriver evaluate(DataSourceFactory dsf, DataSource[] tables, Value[] values, IProgressMonitor pm) throws ExecutionException {
                int source = values[0].getAsInt();

                SpatialDataSourceDecorator sdsEdges = new SpatialDataSourceDecorator(tables[0]);

                if (values.length == 2) {
                        if (values[1].getAsBoolean()) {
                                return computeWMPath(dsf, sdsEdges, source, pm);
                        } else {
                                return computeDWMPath(dsf, sdsEdges, source,pm);
                        }

                } else {
                        return computeDWMPath(dsf, sdsEdges, source,pm);
                }


        }

        @Override
        public String getName() {
                return "ST_ShortestPathLength";
        }

        @Override
        public String getDescription() {
                return "Return the shortest path length beetwen one vertex to all other.";
        }

        @Override
        public String getSqlOrder() {
                return "SELECT ST_ShortestPathLength(12[,true]) ) from data;";
        }

        @Override
        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{GraphSchema.ID, GraphSchema.END_NODE, GraphSchema.WEIGTH});
                return md;
        }

        @Override
        public TableDefinition[] getTablesDefinitions() {
                return new TableDefinition[]{TableDefinition.ANY};
        }

        @Override
        public Arguments[] getFunctionArguments() {
                return new Arguments[]{new Arguments(Argument.INT), new Arguments(Argument.INT, Argument.BOOLEAN)};
        }

        private ObjectDriver computeWMPath(DataSourceFactory dsf, SpatialDataSourceDecorator sds, int source, IProgressMonitor pm) {
                WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(sds, pm);
                try {
                        wMultigraphDataSource.open();

                        ClosestFirstIterator<Integer, GraphEdge> cl = new ClosestFirstIterator<Integer, GraphEdge>(
                                wMultigraphDataSource, source);

                        DiskBufferDriver diskBufferDriver = new DiskBufferDriver(dsf, getMetadata(null));

                        while (cl.hasNext()) {
                                Integer node = cl.next();
                                if (node != source) {
                                        double length = cl.getShortestPathLength(node);
                                        diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(node), ValueFactory.createValue(length)});

                                }
                        }
                        diskBufferDriver.writingFinished();
                        wMultigraphDataSource.close();
                        return diskBufferDriver;

                } catch (DriverException ex) {
                        Logger.getLogger(ST_ShortestPathLength.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
        }

        private ObjectDriver computeDWMPath(DataSourceFactory dsf, SpatialDataSourceDecorator sds, int source, IProgressMonitor pm) {
                DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(sds, pm);
                try {
                        dwMultigraphDataSource.open();

                        ClosestFirstIterator<Integer, GraphEdge> cl = new ClosestFirstIterator<Integer, GraphEdge>(
                                dwMultigraphDataSource, source);

                        DiskBufferDriver diskBufferDriver = new DiskBufferDriver(dsf, getMetadata(null));

                        while (cl.hasNext()) {
                                Integer node = cl.next();
                                if (node != source) {
                                        double length = cl.getShortestPathLength(node);
                                        diskBufferDriver.addValues(new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(node), ValueFactory.createValue(length)});

                                }
                        }
                        diskBufferDriver.writingFinished();
                        dwMultigraphDataSource.close();
                        return diskBufferDriver;

                } catch (DriverException ex) {
                        Logger.getLogger(ST_ShortestPathLength.class.getName()).log(Level.SEVERE, null, ex);
                }
                return null;
        }
}
