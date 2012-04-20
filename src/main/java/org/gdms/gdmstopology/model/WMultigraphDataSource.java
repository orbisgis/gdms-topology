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
import java.util.Set;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.jgrapht.graph.WeightedMultigraph;
import org.orbisgis.progress.ProgressMonitor;

/**
 *
 * @author ebocher
 */
public class WMultigraphDataSource extends WeightedMultigraph<Integer, GraphEdge> implements GDMSValueGraph<Integer, GraphEdge> {

        private final GDMSGraph GDMSGraph;

        /**
         * Create a WeigthedMultiGraph using a datasource.
         * 
         * @param dsf
         * @param dataSet
         * @param pm
         * @throws DriverException
         */
        public WMultigraphDataSource(DataSourceFactory dsf, DataSet dataSet, ProgressMonitor pm) throws DriverException {
                super(GraphEdge.class);
                GDMSGraph = new GDMSGraph(dsf, dataSet, pm);
        }

        public void setWeigthFieldIndex(String fieldName) throws DriverException {
                GDMSGraph.setWeigthFieldIndex(fieldName);
        }

        @Override
        public Set<GraphEdge> incomingEdgesOf(Integer vertex) {
                return GDMSGraph.incomingEdgesOf(vertex);
        }

        @Override
        public Set<GraphEdge> outgoingEdgesOf(Integer vertex) {
                return GDMSGraph.outgoingEdgesOf(vertex);
        }

        @Override
        public GraphEdge getEdge(Integer sourceVertex, Integer targetVertex) {
                return GDMSGraph.getEdge(sourceVertex, targetVertex);
        }

        @Override
        public boolean containsVertex(Integer v) {
                return GDMSGraph.containsVertex(v);
        }

        @Override
        public boolean containsEdge(GraphEdge e) {
                return GDMSGraph.containsEdge(e);
        }

        @Override
        public boolean containsEdge(Integer v, Integer v1) {
                return GDMSGraph.containsEdge(v, v1);
        }

        @Override
        public Set edgesOf(Integer vertex) {
                return GDMSGraph.edgesOf(vertex);
        }

        @Override
        public Integer getEdgeSource(GraphEdge e) {
                return GDMSGraph.getEdgeSource(e);
        }

        @Override
        public Integer getEdgeTarget(GraphEdge e) {
                return GDMSGraph.getEdgeTarget(e);
        }

        @Override
        public double getEdgeWeight(GraphEdge e) {
                return GDMSGraph.getEdgeWeight(e);
        }

        @Override
        public int inDegreeOf(Integer vertex) {
                return GDMSGraph.inDegreeOf(vertex);
        }

        @Override
        public int outDegreeOf(Integer vertex) {
                return GDMSGraph.outDegreeOf(vertex);
        }

        @Override
        public int degreeOf(Integer vertex) {
                return inDegreeOf(vertex) + outDegreeOf(vertex);
        }

        @Override
        public Set<Integer> vertexSet() {
                return GDMSGraph.vertexSet();
        }

        @Override
        public Geometry getGeometry(GraphEdge graphEdge) throws DriverException {
                return GDMSGraph.getGeometry(graphEdge);
        }

        public org.gdms.gdmstopology.model.GDMSGraph getGDMSGraph() {
                return GDMSGraph;
        }

        @Override
        public Geometry getGeometry(int rowid) throws DriverException {
                return GDMSGraph.getGeometry(rowid);
        }

        @Override
        public Value[] getValues(int rowid) throws DriverException {
                return GDMSGraph.getValues(rowid);
        }

        @Override
        public long getRowCount() throws DriverException {
                return GDMSGraph.getRowCount();
        }
}
