package org.gdms.gdmstopology.model;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.ExecutionException;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.SpatialDataSourceDecorator;
import org.gdms.data.indexes.DefaultAlphaQuery;
import org.gdms.data.indexes.IndexException;
import org.gdms.data.metadata.Metadata;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DriverException;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.orbisgis.progress.IProgressMonitor;

/**
 *
 * @author ebocher
 */
public class DWMultigraphDataSource extends DirectedWeightedMultigraph<Integer, GraphEdge> {

        private boolean init = false;
        private final SpatialDataSourceDecorator sds;
        private final DataSourceFactory dsf;
        private int WEIGTH_FIELD = -1;
        private int START_NODE_FIELD = -1;
        private int END_NODE_FIELD = -1;
        private int GEOMETRY_FIELD = -1;
        private final IProgressMonitor pm;

        public DWMultigraphDataSource(SpatialDataSourceDecorator sdsEdges, IProgressMonitor pm) {
                super(GraphEdge.class);
                this.sds = sdsEdges;
                dsf = sdsEdges.getDataSourceFactory();
                this.pm = pm;
        }

        @Override
        public Set<GraphEdge> incomingEdgesOf(Integer vertex) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.END_NODE, vertex);
                        if (queryResult.hasNext()) {
                                HashSet<GraphEdge> preds = new HashSet<GraphEdge>();
                                while (queryResult.hasNext()) {
                                        Integer rowId = queryResult.next();
                                        Value[] values = sds.getRow(rowId);
                                        Integer pred = values[START_NODE_FIELD].getAsInt();
                                        preds.add(new GraphEdge(pred, vertex,
                                                values[WEIGTH_FIELD].getAsDouble()));
                                }
                                return preds;

                        }
                } catch (DriverException ex) {
                }
                return Collections.EMPTY_SET;
        }

        @Override
        public Set<GraphEdge> outgoingEdgesOf(Integer vertex) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, vertex);
                        if (queryResult.hasNext()) {
                                HashSet<GraphEdge> preds = new HashSet<GraphEdge>();
                                while (queryResult.hasNext()) {
                                        Integer rowId = queryResult.next();
                                        Value[] values = sds.getRow(rowId);
                                        Integer dest = values[END_NODE_FIELD].getAsInt();
                                        preds.add(new GraphEdge(vertex, dest,
                                                values[WEIGTH_FIELD].getAsDouble()));
                                }
                                return preds;

                        }
                } catch (DriverException ex) {
                }
                return Collections.emptySet();
        }

        @Override
        public GraphEdge getEdge(Integer sourceVertex, Integer targetVertex) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, sourceVertex);
                        if (queryResult.hasNext()) {
                                Integer rowId = queryResult.next();
                                Value[] values = sds.getRow(rowId);
                                if (values[END_NODE_FIELD].getAsInt() == targetVertex) {
                                        return new GraphEdge(sourceVertex, targetVertex,
                                                values[WEIGTH_FIELD].getAsDouble());
                                }
                        }
                } catch (DriverException ex) {
                }
                return null;
        }

        @Override
        public boolean containsVertex(Integer v) {
                try {

                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, v);
                        if (queryResult.hasNext()) {
                                return true;
                        } else {
                                queryResult = getIndexIterator(GraphSchema.END_NODE, v);
                                if (queryResult.hasNext()) {
                                        return true;
                                } else {
                                        return false;
                                }
                        }

                } catch (DriverException ex) {
                }
                return false;
        }

        @Override
        public Set edgesOf(Integer vertex) {
                HashSet<GraphEdge> edgesOf = new HashSet<GraphEdge>();
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, vertex);
                        while (queryResult.hasNext()) {
                                Integer rowId = queryResult.next();
                                Value[] values = sds.getRow(rowId);
                                Integer dest = values[END_NODE_FIELD].getAsInt();
                                edgesOf.add(new GraphEdge(vertex, dest, values[WEIGTH_FIELD].getAsDouble()));
                        }
                        queryResult = getIndexIterator(GraphSchema.END_NODE, vertex);

                        while (queryResult.hasNext()) {
                                Integer rowId = queryResult.next();
                                Value[] values = sds.getRow(rowId);
                                Integer source = values[START_NODE_FIELD].getAsInt();
                                edgesOf.add(new GraphEdge(source, vertex,
                                        values[WEIGTH_FIELD].getAsDouble()));
                        }

                        return edgesOf;
                } catch (DriverException ex) {
                }
                return edgesOf;
        }

        @Override
        public Integer getEdgeSource(GraphEdge e) {
                return e.getSource();
        }

        @Override
        public Integer getEdgeTarget(GraphEdge e) {
                return e.getTarget();
        }

        @Override
        public double getEdgeWeight(GraphEdge e) {
                return e.getWeight();
        }

        @Override
        public int inDegreeOf(Integer vertex) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.END_NODE, vertex);
                        int counter = 0;
                        while (queryResult.hasNext()) {
                                queryResult.next();
                                counter++;
                        }
                        return counter;

                } catch (DriverException ex) {
                }
                return 0;
        }

        @Override
        public int outDegreeOf(Integer vertex) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, vertex);
                        int counter = 0;
                        while (queryResult.hasNext()) {
                                queryResult.next();
                                counter++;
                        }                      
                        return counter;
                } catch (DriverException ex) {
                }
                return 0;
        }

        @Override
        public int degreeOf(Integer vertex) {
                return inDegreeOf(vertex) + outDegreeOf(vertex);
        }

        @Override
        public Set<Integer> vertexSet() {
                HashSet<Integer> vertexSet = new HashSet<Integer>();
                try {
                        long rowCount = sds.getRowCount();
                        for (int i = 0; i < rowCount; i++) {
                                Value[] values = sds.getRow(i);
                                Integer source = values[START_NODE_FIELD].getAsInt();
                                Integer dest = values[END_NODE_FIELD].getAsInt();
                                vertexSet.add(source);
                                vertexSet.add(dest);
                        }
                        return vertexSet;
                } catch (DriverException ex) {
                }
                return vertexSet;
        }

        public Geometry getGeometry(GraphEdge graphEdge) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, graphEdge.getSource());
                        while (queryResult.hasNext()) {
                                Integer rowId = queryResult.next();
                                Value[] values = sds.getRow(rowId);
                                if (values[END_NODE_FIELD].getAsInt() == graphEdge.getTarget()) {
                                        return values[GEOMETRY_FIELD].getAsGeometry();
                                }
                        }
                } catch (DriverException ex) {
                }
                return null;
        }

        public DataSourceFactory getDataSourceFactory() {
                return sds.getDataSourceFactory();
        }

        public void open() throws DriverException {
                sds.open();
                if (!init) {
                        try {
                                initIndex();
                        } catch (ExecutionException ex) {
                                throw new DriverException("Cannot create indexes", ex);
                        }
                }
        }

        public void close() throws DriverException {
                sds.close();
        }

        /**
         * Create indexes for start and end node.
         * @param pm
         * @throws ExecutionException
         */
        public void initIndex() throws ExecutionException {
                try {
                        if (checkMetadata()) {
                                if (!dsf.getIndexManager().isIndexed(sds.getName(), GraphSchema.START_NODE)) {
                                        dsf.getIndexManager().buildIndex(sds.getName(), GraphSchema.START_NODE, pm);
                                }
                                if (!dsf.getIndexManager().isIndexed(sds.getName(), GraphSchema.END_NODE)) {
                                        dsf.getIndexManager().buildIndex(sds.getName(), GraphSchema.END_NODE, pm);
                                }
                                init = true;
                        }
                } catch (DriverException ex) {
                        throw new ExecutionException("Unable to get metadata.", ex);
                } catch (IndexException ex) {
                        throw new ExecutionException("Unable to create index.", ex);
                } catch (NoSuchTableException ex) {
                        throw new ExecutionException("Unable to find the table.", ex);
                }
        }

        /**
         * A method to check if the schemas are well populated to use graph analysis
         * tools.
         *
         * @throws DriverException
         */
        private boolean checkMetadata() throws DriverException {
                Metadata edgesMetadata = sds.getMetadata();
                GEOMETRY_FIELD = sds.getSpatialFieldIndex();
                WEIGTH_FIELD = edgesMetadata.getFieldIndex(GraphSchema.WEIGTH);
                START_NODE_FIELD = edgesMetadata.getFieldIndex(GraphSchema.START_NODE);
                END_NODE_FIELD = edgesMetadata.getFieldIndex(GraphSchema.END_NODE);
                if (WEIGTH_FIELD == -1) {
                        throw new IllegalArgumentException("The table "
                                + sds.getName() + " must contains a field named weight");
                }
                if (START_NODE_FIELD == -1) {
                        throw new IllegalArgumentException("The table " + sds.getName()
                                + " must contains a field named start_node");
                }
                if (END_NODE_FIELD == -1) {
                        throw new IllegalArgumentException("The table " + sds.getName()
                                + " must contains a field named end_node");
                }
                return true;
        }

        public Iterator<Integer> getIndexIterator(String fieldToQuery, Integer valueToQuery) throws DriverException {
                DefaultAlphaQuery defaultAlphaQuery = new DefaultAlphaQuery(
                        fieldToQuery, ValueFactory.createValue(valueToQuery));
                return sds.queryIndex(defaultAlphaQuery);
        }

        public SpatialDataSourceDecorator getSpatialDataSourceDecorator() {
                return sds;
        }
}
