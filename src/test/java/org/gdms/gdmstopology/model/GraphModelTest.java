/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV institut as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministery
 * of Ecology and Sustainable Development.
 *
 * GDMS-Topology is distributed under GPL 3 license. It is produced by the
 * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
 * 2488.
 *
 * Copyright (C) 2009-2012 IRSTV (FR CNRS 2488)
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://wwwc.orbisgis.org/> or contact
 * directly: info_at_ orbisgis.org
 */
package org.gdms.gdmstopology.model;

import org.junit.Test;
import org.gdms.data.DataSource;
import org.gdms.gdmstopology.TopologySetUpTest;
import org.orbisgis.progress.NullProgressMonitor;

import static org.junit.Assert.*;

/**
 *
 * @author Erwan Bocher
 */
public class GraphModelTest extends TopologySetUpTest {

    /**
     * A test to wrap a DirectedWeightedMultigraph with JGrapht.
     *
     * @throws Exception
     */
    @Test
    public void testCreateDWMGraph() throws Exception {
        // INITIAL SETUP
        // Get and open the DataSource.
        DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
        ds.open();
        // 
        DWMultigraphDataSource dWMultigraphDataSource = new DWMultigraphDataSource(dsf, ds, new NullProgressMonitor());
        dWMultigraphDataSource.setWeightFieldIndex("length");

        // Tests to validate the graph model and some properties
        GraphEdge ge = (GraphEdge) dWMultigraphDataSource.getEdge(3, 5);
        assertTrue(ge != null);
        assertTrue((Integer) ge.getSource() == 3);
        assertTrue((Integer) ge.getTarget() == 5);
        assertTrue((ge.getWeight() - ds.getGeometry(1).getLength()) == 0);
        assertTrue(dWMultigraphDataSource.inDegreeOf(3) == 1);
        assertTrue(dWMultigraphDataSource.outDegreeOf(6) == 2);
        assertTrue(dWMultigraphDataSource.inDegreeOf(5) == 1);
        assertTrue(dWMultigraphDataSource.outDegreeOf(5) == 0);
        assertTrue(dWMultigraphDataSource.outDegreeOf(100) == 0);
        assertTrue(dWMultigraphDataSource.incomingEdgesOf(3).size() == 1);
        assertTrue(dWMultigraphDataSource.outgoingEdgesOf(5).isEmpty());
        ds.close();
    }

    /**
     * A test to wrap a WeightedMultigraph with JGrapht
     *
     * @throws Exception
     */
    @Test
    public void testCreateWMGraph() throws Exception {
        DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
        ds.open();
        WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, ds, new NullProgressMonitor());
        wMultigraphDataSource.setWeightFieldIndex("length");

        //Tests to validate the graph model and some properties
        GraphEdge ge = (GraphEdge) wMultigraphDataSource.getEdge(3, 5);
        assertTrue(ge != null);
        assertTrue((Integer) ge.getSource() == 3);
        assertTrue((Integer) ge.getTarget() == 5);
        assertTrue((ge.getWeight() - ds.getGeometry(1).getLength()) == 0);
        assertTrue(wMultigraphDataSource.inDegreeOf(3) == 1);
        assertTrue(wMultigraphDataSource.outDegreeOf(6) == 2);
        assertTrue(wMultigraphDataSource.inDegreeOf(5) == 1);
        assertTrue(wMultigraphDataSource.outDegreeOf(5) == 0);
        assertTrue(wMultigraphDataSource.outDegreeOf(100) == 0);
        assertTrue(wMultigraphDataSource.incomingEdgesOf(3).size() == 1);
        assertTrue(wMultigraphDataSource.outgoingEdgesOf(5).isEmpty());
        ds.close();
    }
}
