/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV Institute as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministry
 * of Ecology and Sustainable Development.
 *
 * GDMS-Topology is distributed under GPL 3 license. It is produced by the
 * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
 * 2488.
 *
 * Copyright (C) 2009-2013 IRSTV (FR CNRS 2488)
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
 * directly: info_at_orbisgis.org
 */
package org.gdms.gdmstopology.centrality;

import java.io.File;
import org.gdms.data.DataSource;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.gdmstopology.TopologySetupTest;
import org.junit.Test;
import org.orbisgis.progress.NullProgressMonitor;
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link ST_StrahlerStreamOrder} in a given river network
 * (test_hydro.gdms) and makes sure that the number of vertices with Strahler
 * numbers 1, 2, 3 and 4 is as expected.
 *
 * @author Adam Gouge
 */
public class ST_StrahlerStreamOrderTest extends TopologySetupTest {

    /**
     * Test file: part of a river network extracted from the
     * <a href="http://professionnels.ign.fr/bdcarthage">
     * BD Carthage</a> database.
     */
    protected String TEST_HYDRO = "test_hydro";
    /**
     * The edges file extension.
     */
    private static final String DOT_EDGES = ".edges";
    /**
     * The shape file extension.
     */
    private static final String DOT_GDMS = ".gdms";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        dsf.getSourceManager()
                .register(TEST_HYDRO,
                          new File(resourcesFolder + TEST_HYDRO + DOT_GDMS));
        dsf.getSourceManager()
                .register(TEST_HYDRO + DOT_EDGES,
                          new File(
                resourcesFolder + TEST_HYDRO + DOT_EDGES + DOT_GDMS));
    }

    @Test
    public void testST_StrahlerStreamOrder() throws Exception {
        ST_StrahlerStreamOrder fn = new ST_StrahlerStreamOrder();
        DataSource ds = dsf.getDataSource(TEST_HYDRO + DOT_EDGES);
        ds.open();
        DataSet[] tables = new DataSet[]{ds};
        int rootNode = 84;
        DataSet result = fn.evaluate(
                dsf,
                tables,
                new Value[]{ValueFactory.createValue(rootNode)},
                new NullProgressMonitor());
        // Count the number of nodes that have Strahler number 1, 2, 3 or 4.
        int[] count = {0, 0, 0, 0};
        for (int i = 0; i < result.getRowCount(); i++) {
            Value[] row = result.getRow(i);
            for (int k = 0; k < 4; k++) {
                if (row[1].getAsInt() == k + 1) {
                    count[k]++;
                }
            }
        }
        // Check that they are equal to the expected values.
        assertEquals(count[0], 63);
        assertEquals(count[1], 22);
        assertEquals(count[2], 8);
        assertEquals(count[3], 18);
    }
}
