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
public class ST_FindReachableEdges extends AbstractTableFunction {

        private DiskBufferDriver diskBufferDriver;

        @Override
        public DataSet evaluate(SQLDataSourceFactory dsf, DataSet[] tables, Value[] values, ProgressMonitor pm) throws FunctionException {
                int source = values[0].getAsInt();
                try {
                        DataSet sdsEdges = tables[0];
                        diskBufferDriver = new DiskBufferDriver(dsf, getMetadata(null));

                        if (values.length == 3) {
                                if (values[1].getAsBoolean()) {
                                        return computeWMPath(dsf, sdsEdges, source, pm);
                                } else {
                                        return computeDWMPath(dsf, sdsEdges, source, values[2].getAsBoolean(), pm);
                                }

                        } else {
                                return computeDWMPath(dsf, sdsEdges, source, false, pm);
                        }

                } catch (DriverException ex) {
                        throw new FunctionException("Cannot find reachable edges", ex);
                }

        }

        @Override
        public void workFinished() throws DriverException {
                if(diskBufferDriver!=null) {
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
                        + "Optional arguments : \n"
                        + "true is the graph is undirected\n"
                        + "true is the graph is reversed.";
        }

        @Override
        public String getSqlOrder() {
                return "SELECT * from ST_FindReachableEdges(table, 12[,true, false]) );";
        }

        @Override
        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.GEOMETRY), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.DOUBLE), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{"the_geom", GraphSchema.ID, GraphSchema.START_NODE, GraphSchema.END_NODE, GraphSchema.WEIGTH, GraphSchema.WEIGTH_SUM});
                return md;
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT),
                                new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.INT, ScalarArgument.BOOLEAN, ScalarArgument.BOOLEAN)
                        };
        }

        private DiskBufferDriver computeWMPath(DataSourceFactory dsf, DataSet sds, int source, ProgressMonitor pm) throws DriverException {
                WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, sds, pm);
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

        private DiskBufferDriver computeDWMPath(DataSourceFactory dsf, DataSet sds, int source, Boolean reverseGraph, ProgressMonitor pm) throws DriverException {
                DWMultigraphDataSource dwMultigraphDataSource = new DWMultigraphDataSource(dsf, sds, pm);
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
