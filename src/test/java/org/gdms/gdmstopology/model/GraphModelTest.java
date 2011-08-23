package org.gdms.gdmstopology.model;

import org.gdms.data.SpatialDataSourceDecorator;
import org.gdms.gdmstopology.TopologySetUpTest;
import org.orbisgis.progress.NullProgressMonitor;

/**
 *
 * @author ebocher
 */
public class GraphModelTest extends TopologySetUpTest {

        /**
         * A test to wrap a DirectedWeightedMultigraph with JGrapht
         * @throws Exception
         */
        public void testCreateDWMGraph() throws Exception {
                SpatialDataSourceDecorator sdsEdges = new SpatialDataSourceDecorator(dsf.getDataSource(GRAPH_EDGES));
                DWMultigraphDataSource dWMultigraphDataSource = new DWMultigraphDataSource(sdsEdges, new NullProgressMonitor());
                dWMultigraphDataSource.open();

                //Tests to validate the graph model and some properties
                GraphEdge ge = (GraphEdge) dWMultigraphDataSource.getEdge(3, 4);
                assertTrue(ge != null);
                assertTrue((Integer) ge.getSource() == 3);
                assertTrue((Integer) ge.getTarget() == 4);
                assertTrue((ge.getWeight() - sdsEdges.getGeometry(1).getLength()) == 0);
                assertTrue(dWMultigraphDataSource.inDegreeOf(4) == 3);
                assertTrue(dWMultigraphDataSource.outDegreeOf(4) == 1);
                assertTrue(dWMultigraphDataSource.inDegreeOf(3) == 0);
                assertTrue(dWMultigraphDataSource.outDegreeOf(3) == 1);
                assertTrue(dWMultigraphDataSource.outDegreeOf(100) == 0);
                assertTrue(dWMultigraphDataSource.incomingEdgesOf(4).size() == 3);
                assertTrue(dWMultigraphDataSource.outgoingEdgesOf(4).size() == 1);
                dWMultigraphDataSource.close();
        }


        /**
         * A test to wrap a WeightedMultigraph with JGrapht
         * @throws Exception
         */
        public void testCreateWMGraph() throws Exception {
                SpatialDataSourceDecorator sdsEdges = new SpatialDataSourceDecorator(dsf.getDataSource(GRAPH_EDGES));
                WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(sdsEdges, new NullProgressMonitor());
                wMultigraphDataSource.open();

                //Tests to validate the graph model and some properties
                GraphEdge ge = (GraphEdge) wMultigraphDataSource.getEdge(3, 4);
                assertTrue(ge != null);
                assertTrue((Integer) ge.getSource() == 3);
                assertTrue((Integer) ge.getTarget() == 4);
                assertTrue((ge.getWeight() - sdsEdges.getGeometry(1).getLength()) == 0);
                assertTrue(wMultigraphDataSource.inDegreeOf(4) == 3);
                assertTrue(wMultigraphDataSource.outDegreeOf(4) == 1);
                assertTrue(wMultigraphDataSource.inDegreeOf(3) == 0);
                assertTrue(wMultigraphDataSource.outDegreeOf(3) == 1);
                assertTrue(wMultigraphDataSource.outDegreeOf(100) == 0);
                assertTrue(wMultigraphDataSource.incomingEdgesOf(4).size() == 3);
                assertTrue(wMultigraphDataSource.outgoingEdgesOf(4).size() == 1);
                wMultigraphDataSource.close();
        }
}
