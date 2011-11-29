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

import org.junit.Test;
import org.gdms.data.DataSource;
import org.gdms.gdmstopology.TopologySetUpTest;
import org.orbisgis.progress.NullProgressMonitor;

import static org.junit.Assert.*;
/**
 *
 * @author ebocher
 */
public class GraphModelTest extends TopologySetUpTest {

        /**
         * A test to wrap a DirectedWeightedMultigraph with JGrapht
         * @throws Exception
         */
        @Test
        public void testCreateDWMGraph() throws Exception {
                DataSource ds = dsf.getDataSource(GRAPH_EDGES);
                ds.open();

                DWMultigraphDataSource dWMultigraphDataSource = new DWMultigraphDataSource(dsf, ds, new NullProgressMonitor());

                //Tests to validate the graph model and some properties
                GraphEdge ge = (GraphEdge) dWMultigraphDataSource.getEdge(3, 4);
                assertTrue(ge != null);
                assertTrue((Integer) ge.getSource() == 3);
                assertTrue((Integer) ge.getTarget() == 4);
                assertTrue((ge.getWeight() - ds.getGeometry(1).getLength()) == 0);
                assertTrue(dWMultigraphDataSource.inDegreeOf(4) == 3);
                assertTrue(dWMultigraphDataSource.outDegreeOf(4) == 1);
                assertTrue(dWMultigraphDataSource.inDegreeOf(3) == 0);
                assertTrue(dWMultigraphDataSource.outDegreeOf(3) == 1);
                assertTrue(dWMultigraphDataSource.outDegreeOf(100) == 0);
                assertTrue(dWMultigraphDataSource.incomingEdgesOf(4).size() == 3);
                assertTrue(dWMultigraphDataSource.outgoingEdgesOf(4).size() == 1);
                ds.close();
        }

        /**
         * A test to wrap a WeightedMultigraph with JGrapht
         * @throws Exception
         */
          @Test
        public void testCreateWMGraph() throws Exception {
                DataSource ds = dsf.getDataSource(GRAPH_EDGES);
                ds.open();
                WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, ds, new NullProgressMonitor());
                //Tests to validate the graph model and some properties
                GraphEdge ge = (GraphEdge) wMultigraphDataSource.getEdge(3, 4);
                assertTrue(ge != null);
                assertTrue((Integer) ge.getSource() == 3);
                assertTrue((Integer) ge.getTarget() == 4);
                assertTrue((ge.getWeight() - ds.getGeometry(1).getLength()) == 0);
                assertTrue(wMultigraphDataSource.inDegreeOf(4) == 3);
                assertTrue(wMultigraphDataSource.outDegreeOf(4) == 1);
                assertTrue(wMultigraphDataSource.inDegreeOf(3) == 0);
                assertTrue(wMultigraphDataSource.outDegreeOf(3) == 1);
                assertTrue(wMultigraphDataSource.outDegreeOf(100) == 0);
                assertTrue(wMultigraphDataSource.incomingEdgesOf(4).size() == 3);
                assertTrue(wMultigraphDataSource.outgoingEdgesOf(4).size() == 1);
                ds.close();
        }
}
