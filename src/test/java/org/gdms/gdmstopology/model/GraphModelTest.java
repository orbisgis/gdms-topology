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
