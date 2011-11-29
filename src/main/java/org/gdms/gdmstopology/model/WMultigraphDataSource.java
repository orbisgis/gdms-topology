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
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.jgrapht.graph.WeightedMultigraph;
import org.orbisgis.progress.ProgressMonitor;

/**
 *
 * @author ebocher
 */
public class WMultigraphDataSource extends WeightedMultigraph<Integer, GraphEdge> {

        private final DataSet dataSet;
        private final DataSourceFactory dsf;
        public int WEIGTH_FIELD_INDEX = -1;
        public int START_NODE_FIELD_INDEX = -1;
        public int END_NODE_FIELD_INDEX = -1;
        public int GEOMETRY_FIELD_INDEX = -1;
        private final ProgressMonitor pm;

        /**
         * Create a WeigthedMultiGraph using a datasource.
         * Be carefull the schema of the input datasource must match the fields below:
         * start_node (int)
         * end_node (int)
         * weigth (double)
         * 
         * @param dsf
         * @param dataSet
         * @param pm
         * @throws DriverException
         */
        public WMultigraphDataSource(DataSourceFactory dsf, DataSet dataSet, ProgressMonitor pm) throws DriverException {
                super(GraphEdge.class);
                this.dataSet = dataSet;
                this.pm = pm;
                this.dsf = dsf;
                initIndex();
        }

        @Override
        public Set<GraphEdge> incomingEdgesOf(Integer vertex) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.END_NODE, vertex);
                        if (queryResult.hasNext()) {
                                HashSet<GraphEdge> preds = new HashSet<GraphEdge>();
                                while (queryResult.hasNext()) {
                                        Integer rowId = queryResult.next();
                                        Integer pred = getSourceVertex(dataSet, rowId);
                                        preds.add(new GraphEdge(pred, vertex, getWeigthVertex(dataSet, rowId), rowId));
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
                                        Integer dest = getTargetVertex(dataSet, rowId);
                                        preds.add(new GraphEdge(vertex, dest, getWeigthVertex(dataSet, rowId), rowId));
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
                                if (getTargetVertex(dataSet, rowId) == targetVertex) {
                                        return new GraphEdge(sourceVertex, targetVertex,
                                                getWeigthVertex(dataSet, rowId), rowId);
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
        public boolean containsEdge(GraphEdge e) {
                return containsEdge(e.getSource(), e.getTarget());
        }

        @Override
        public boolean containsEdge(Integer v, Integer v1) {
                try {
                        Iterator<Integer> queryResult = getIndexIterator(GraphSchema.START_NODE, v);
                        if (queryResult.hasNext()) {
                                Integer rowId = queryResult.next();
                                if (getTargetVertex(dataSet, rowId) == v1) {
                                        return true;
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
                                Integer dest = getTargetVertex(dataSet, rowId);
                                edgesOf.add(new GraphEdge(vertex, dest, getWeigthVertex(dataSet, rowId), rowId));
                        }
                        queryResult = getIndexIterator(GraphSchema.END_NODE, vertex);

                        while (queryResult.hasNext()) {
                                Integer rowId = queryResult.next();
                                Integer source = getSourceVertex(dataSet, rowId);
                                edgesOf.add(new GraphEdge(source, vertex,
                                        getWeigthVertex(dataSet, rowId), rowId));
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
                        long rowCount = dataSet.getRowCount();
                        for (int i = 0; i < rowCount; i++) {
                                Value[] values = dataSet.getRow(i);
                                Integer source = getSourceVertex(dataSet, i);
                                Integer dest = getTargetVertex(dataSet, i);
                                vertexSet.add(source);
                                vertexSet.add(dest);
                        }
                        return vertexSet;
                } catch (DriverException ex) {
                }
                return vertexSet;
        }

        public Geometry getGeometry(GraphEdge graphEdge) throws DriverException {
                return dataSet.getGeometry(graphEdge.getRowId(), GEOMETRY_FIELD_INDEX);
        }
        
        private int getSourceVertex(DataSet dataSet, long rowId) throws DriverException {
                return dataSet.getInt(rowId, START_NODE_FIELD_INDEX);
        }

        private int getTargetVertex(DataSet dataSet, long rowId) throws DriverException {
                return dataSet.getInt(rowId, END_NODE_FIELD_INDEX);
        }

        private double getWeigthVertex(DataSet dataSet, long rowId) throws DriverException {
                return dataSet.getDouble(rowId, WEIGTH_FIELD_INDEX);
        }

        private Geometry getEdgeGeometry(DataSet dataSet, long rowId) throws DriverException {
                return dataSet.getGeometry(rowId, GEOMETRY_FIELD_INDEX);
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
                WEIGTH_FIELD_INDEX = edgesMetadata.getFieldIndex(GraphSchema.WEIGTH);
                START_NODE_FIELD_INDEX = edgesMetadata.getFieldIndex(GraphSchema.START_NODE);
                END_NODE_FIELD_INDEX = edgesMetadata.getFieldIndex(GraphSchema.END_NODE);
                if (WEIGTH_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table  must contains a field named weigth");
                }
                if (START_NODE_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table must contains a field named start_node");
                }
                if (END_NODE_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table  must contains a field named end_node");
                }
                return true;
        }

        public Iterator<Integer> getIndexIterator(String fieldToQuery, Integer valueToQuery) throws DriverException {
                DefaultAlphaQuery defaultAlphaQuery = new DefaultAlphaQuery(
                        fieldToQuery, ValueFactory.createValue(valueToQuery));
                return dataSet.queryIndex(dsf, defaultAlphaQuery);
        }
}
