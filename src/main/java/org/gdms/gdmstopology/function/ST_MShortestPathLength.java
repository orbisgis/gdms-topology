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
public class ST_MShortestPathLength extends AbstractTableFunction {

        private DiskBufferDriver diskBufferDriver;
        private int ID_FIELD_INDEX = -1;

        @Override
        public DataSet evaluate(SQLDataSourceFactory dsf, DataSet[] tables, Value[] values, ProgressMonitor pm) throws FunctionException {
                try {
                        DataSet sdsEdges = tables[0];
                        DataSet vertexes = tables[1];
                        checkMetadata(vertexes);

                        diskBufferDriver = new DiskBufferDriver(dsf, getMetadata(null));

                        if (values.length == 2) {
                                if (values[0].getAsBoolean()) {
                                        return computeWMPath(dsf, sdsEdges, vertexes, pm);
                                } else {
                                        return computeDWMPath(dsf, sdsEdges, vertexes, values[1].getAsBoolean(), pm);
                                }

                        } else {
                                return computeDWMPath(dsf, sdsEdges, vertexes, false, pm);
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
                return "ST_MShortestPathLength";
        }

        @Override
        public String getDescription() {
                return "Return the shortest path length beetwen several vertexes to all other based on a directed or undirected graph.\n"
                        + "Optional arguments : \n"
                        + "true is the graph is undirected\n"
                        + "true is the graph is reversed.";
        }

        @Override
        public String getSqlOrder() {
                return "SELECT * from ST_MShortestPathLength(table, vertexes[,true, true]) );";
        }

        @Override
        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{GraphSchema.ID, GraphSchema.START_NODE, GraphSchema.END_NODE, GraphSchema.WEIGTH});
                return md;
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), new TableArgument(TableDefinition.ANY)),
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), new TableArgument(TableDefinition.ANY), ScalarArgument.BOOLEAN, ScalarArgument.BOOLEAN)
                        };
        }

        private DiskBufferDriver computeWMPath(DataSourceFactory dsf, DataSet sds, DataSet vertexes, ProgressMonitor pm) throws DriverException {
                WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, sds, pm);
                long rowCount = vertexes.getRowCount();
                ClosestFirstIterator<Integer, GraphEdge> cl;
                for (int i = 0; i < rowCount; i++) {
                        int source = vertexes.getInt(i, ID_FIELD_INDEX);
                        cl = new ClosestFirstIterator<Integer, GraphEdge>(
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
                }
                diskBufferDriver.writingFinished();
                diskBufferDriver.start();
                return diskBufferDriver;

        }

        private DiskBufferDriver computeDWMPath(DataSourceFactory dsf, DataSet sds, DataSet vertexes, boolean reverseGraph, ProgressMonitor pm) throws DriverException {
                DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, sds, pm);
                ClosestFirstIterator<Integer, GraphEdge> cl;
                if (reverseGraph) {
                        long rowCount = vertexes.getRowCount();
                        for (int i = 0; i < rowCount; i++) {
                                int source = vertexes.getInt(i, ID_FIELD_INDEX);
                                cl = new ClosestFirstIterator<Integer, GraphEdge>(new EdgeReversedGraph(dwMultigraphDataSource), source);
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
                        }
                } else {
                        long rowCount = vertexes.getRowCount();
                        for (int i = 0; i < rowCount; i++) {
                                int source = vertexes.getInt(i, ID_FIELD_INDEX);
                                cl = new ClosestFirstIterator<Integer, GraphEdge>(dwMultigraphDataSource, source);
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
                        }
                }


                diskBufferDriver.writingFinished();
                diskBufferDriver.start();
                return diskBufferDriver;
        }

        /**
         * A method to check if the schema are well populated to use this function.
         *
         * @throws DriverException
         */
        private void checkMetadata(DataSet dataSet) throws DriverException {
                Metadata metadata = dataSet.getMetadata();
                ID_FIELD_INDEX = metadata.getFieldIndex(GraphSchema.ID);
                if (ID_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table must contains a field named id");
                }
        }
}
