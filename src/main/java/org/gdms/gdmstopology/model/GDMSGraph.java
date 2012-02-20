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
package org.gdms.gdmstopology.model;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.indexes.DefaultAlphaQuery;
import org.gdms.data.indexes.IndexException;
import org.gdms.data.schema.Metadata;
import org.gdms.data.schema.MetadataUtilities;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.jgrapht.EdgeFactory;
import org.jgrapht.graph.AbstractGraph;
import org.jgrapht.graph.ClassBasedEdgeFactory;
import org.orbisgis.progress.ProgressMonitor;

/**
 *
 * @author ebocher
 */
public class GDMSGraph extends AbstractGraph<Integer, GraphEdge> {

        private final DataSet dataSet;
        private final DataSourceFactory dsf;
        public int WEIGTH_FIELD_INDEX = -1;
        public int START_NODE_FIELD_INDEX = -1;
        public int END_NODE_FIELD_INDEX = -1;
        public int GEOMETRY_FIELD_INDEX = -1;
        public final ProgressMonitor pm;

        /*
         * Be carefull the schema of the input datasource must match the fields below:
         * start_node (int)
         * end_node (int)
         * weigth (double)
         */
        public GDMSGraph(DataSourceFactory dsf, DataSet dataSet, ProgressMonitor pm) throws DriverException {
                this.dataSet = dataSet;
                this.dsf = dsf;
                this.pm = pm;
                initIndex();
        }

        /**
         * Create indexes for start and end node.
         * @param pm
         * @throws ExecutionException
         */
        public void initIndex() throws DriverException {
                try {
                        if (checkMetadata()) {
                                if (!dsf.getIndexManager().isIndexed(dataSet, GraphSchema.START_NODE)) {
                                        dsf.getIndexManager().buildIndex(dataSet, GraphSchema.START_NODE, pm);
                                }
                                if (!dsf.getIndexManager().isIndexed(dataSet, GraphSchema.END_NODE)) {
                                        dsf.getIndexManager().buildIndex(dataSet, GraphSchema.END_NODE, pm);
                                }
                        }
                } catch (DriverException ex) {
                        throw new DriverException("Unable to get metadata.", ex);
                } catch (IndexException ex) {
                        throw new DriverException("Unable to create index.", ex);
                } catch (NoSuchTableException ex) {
                        throw new DriverException("Unable to find the table.", ex);
                }
        }

        /**
         * A method to check if the schemas are well populated to use graph analysis
         * tools.
         *
         * @throws DriverException
         */
        private boolean checkMetadata() throws DriverException {
                Metadata edgesMetadata = dataSet.getMetadata();
                GEOMETRY_FIELD_INDEX = MetadataUtilities.getSpatialFieldIndex(edgesMetadata);
                START_NODE_FIELD_INDEX = edgesMetadata.getFieldIndex(GraphSchema.START_NODE);
                END_NODE_FIELD_INDEX = edgesMetadata.getFieldIndex(GraphSchema.END_NODE);
                if (START_NODE_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table must contains a field named start_node");
                }
                if (END_NODE_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table must contains a field named end_node");
                }
                return true;
        }

        /**
         * This method is used to specify the field that contains the weigth value
         * to process the graph.
         * @param fieldIndex 
         */
        public void setWeigthFieldIndex(String fieldName) throws DriverException {
                Metadata edgesMetadata = dataSet.getMetadata();
                int fieldIndex = edgesMetadata.getFieldIndex(fieldName);
                if (fieldIndex == -1) {
                        throw new IllegalArgumentException("The table must contains a field named " + fieldName);
                }
                this.WEIGTH_FIELD_INDEX = fieldIndex;
        }

        /**
         * Return the weight field index
         * @return 
         */
        public int getWeightFieldIindex() {
                return WEIGTH_FIELD_INDEX;
        }

        /**
         * Return the geometry of the edge
         * @param graphEdge
         * @return
         * @throws DriverException 
         */
        public Geometry getGeometry(GraphEdge graphEdge) throws DriverException {
                return dataSet.getGeometry(graphEdge.getRowId(), GEOMETRY_FIELD_INDEX);
        }

        @Override
        public Set<GraphEdge> getAllEdges(Integer v, Integer v1) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, v);
                        if (queryResult.hasNext()) {
                                HashSet<GraphEdge> edges = new HashSet<GraphEdge>();
                                while (queryResult.hasNext()) {
                                        Integer rowId = queryResult.next();
                                        edges.add(new GraphEdge(v, v1, getWeigthVertex(rowId), rowId));
                                }
                        }
                } catch (DriverException ex) {
                }
                return Collections.EMPTY_SET;

        }

        @Override
        public GraphEdge getEdge(Integer v, Integer v1) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, v);
                        if (queryResult.hasNext()) {
                                while (queryResult.hasNext()) {
                                        Integer rowId = queryResult.next();
                                        if (getTargetVertex(rowId) == v1) {
                                                return new GraphEdge(v, v1,
                                                        getWeigthVertex(rowId), rowId);
                                        }
                                }
                        }
                } catch (DriverException ex) {
                }
                return null;
        }

        @Override
        public EdgeFactory<Integer, GraphEdge> getEdgeFactory() {
                return new ClassBasedEdgeFactory<Integer, GraphEdge>(GraphEdge.class);
        }

        @Override
        public GraphEdge addEdge(Integer v, Integer v1) {
                throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addEdge(Integer v, Integer v1, GraphEdge e) {
                throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addVertex(Integer v) {
                throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean containsEdge(Integer v, Integer v1) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, v);
                        if (queryResult.hasNext()) {
                                while (queryResult.hasNext()) {
                                        Integer rowId = queryResult.next();
                                        if (getTargetVertex(rowId) == v1) {
                                                return true;
                                        }
                                }
                        }
                } catch (DriverException ex) {
                }
                return false;
        }

        @Override
        public boolean containsEdge(GraphEdge e) {
                return containsEdge(e.getSource(), e.getTarget());
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
        public Set<GraphEdge> edgeSet() {
                throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<GraphEdge> edgesOf(Integer v) {
                HashSet<GraphEdge> edgesOf = new HashSet<GraphEdge>();
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, v);
                        while (queryResult.hasNext()) {
                                Integer rowId = queryResult.next();
                                Integer dest = getTargetVertex(rowId);
                                edgesOf.add(new GraphEdge(v, dest, getWeigthVertex(rowId), rowId));
                        }
                        queryResult = getIndexIterator(GraphSchema.END_NODE, v);

                        while (queryResult.hasNext()) {
                                Integer rowId = queryResult.next();
                                Integer source = getSourceVertex(rowId);
                                edgesOf.add(new GraphEdge(source, v, getWeigthVertex(rowId), rowId));
                        }

                        return edgesOf;
                } catch (DriverException ex) {
                }
                return edgesOf;
        }

        @Override
        public GraphEdge removeEdge(Integer v, Integer v1) {
                throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeEdge(GraphEdge e) {
                throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeVertex(Integer v) {
                throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Set<Integer> vertexSet() {
                HashSet<Integer> vertexSet = new HashSet<Integer>();
                try {
                        long rowCount = dataSet.getRowCount();
                        for (int i = 0; i < rowCount; i++) {
                                Integer source = getSourceVertex(i);
                                Integer dest = getTargetVertex(i);
                                vertexSet.add(source);
                                vertexSet.add(dest);
                        }
                        return vertexSet;
                } catch (DriverException ex) {
                }
                return vertexSet;
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

        /**
         * 
         * @param vertex
         * @return 
         */
        public Set<GraphEdge> incomingEdgesOf(Integer vertex) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.END_NODE, vertex);
                        if (queryResult.hasNext()) {
                                HashSet<GraphEdge> preds = new HashSet<GraphEdge>();
                                while (queryResult.hasNext()) {
                                        Integer rowId = queryResult.next();
                                        Integer pred = getSourceVertex(rowId);
                                        preds.add(new GraphEdge(pred, vertex, getWeigthVertex(rowId), rowId));
                                }
                                return preds;

                        }
                } catch (DriverException ex) {
                }
                return Collections.EMPTY_SET;
        }

        public Set<GraphEdge> outgoingEdgesOf(Integer vertex) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, vertex);
                        if (queryResult.hasNext()) {
                                HashSet<GraphEdge> preds = new HashSet<GraphEdge>();
                                while (queryResult.hasNext()) {
                                        Integer rowId = queryResult.next();
                                        Integer dest = getTargetVertex(rowId);
                                        preds.add(new GraphEdge(vertex, dest, getWeigthVertex(rowId), rowId));
                                }
                                return preds;

                        }
                } catch (DriverException ex) {
                }
                return Collections.emptySet();
        }

        /**
         * Query the dataset using an alphanumeric index
         * @param fieldToQuery
         * @param valueToQuery
         * @return
         * @throws DriverException 
         */
        public Iterator<Integer> getIndexIterator(String fieldToQuery, Integer valueToQuery) throws DriverException {
                DefaultAlphaQuery defaultAlphaQuery = new DefaultAlphaQuery(
                        fieldToQuery, ValueFactory.createValue(valueToQuery));
                return dataSet.queryIndex(dsf, defaultAlphaQuery);
        }

        /**
         * Return the source vertex from a given edge defined by its row id in the dataset
         * @param dataSet
         * @param rowId
         * @return
         * @throws DriverException 
         */
        private int getSourceVertex(long rowId) throws DriverException {
                return dataSet.getInt(rowId, START_NODE_FIELD_INDEX);
        }

        /**
         * Return the target vertex from a given edge defined by its row id in the dataset
         * @param dataSet
         * @param rowId
         * @return
         * @throws DriverException 
         */
        private int getTargetVertex(long rowId) throws DriverException {
                return dataSet.getInt(rowId, END_NODE_FIELD_INDEX);
        }

        /**
         * Return the weigth of an edge defined by its row id in the dataset
         * @param dataSet
         * @param rowId
         * @return
         * @throws DriverException 
         */
        private double getWeigthVertex(long rowId) throws DriverException {
                return dataSet.getDouble(rowId, WEIGTH_FIELD_INDEX);
        }

        /**
         * Return  the number of head endpoints adjacent to the vertex
         * @param vertex
         * @return 
         */
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

        /**
         * Return the number of tail endpoints to the vertex
         * @param vertex
         * @return 
         */
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
}
