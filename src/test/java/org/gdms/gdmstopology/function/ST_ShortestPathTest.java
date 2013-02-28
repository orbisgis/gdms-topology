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
package org.gdms.gdmstopology.function;

import org.gdms.data.DataSource;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.gdmstopology.TopologySetupTest;
import static org.junit.Assert.*;
import org.junit.Test;
import org.orbisgis.progress.NullProgressMonitor;

/**
 * Tests the function {@link ST_ShortestPath}.
 *
 * @author Adam Gouge
 */
public class ST_ShortestPathTest extends TopologySetupTest {

    /**
     * Tests finding the shortest path between neighboring vertices of a
     * directed graph (GRAPH2D).
     *
     * @throws Exception
     */
    @Test
    public void testST_ShortestPath() throws Exception {
        ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
        DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
        ds.open();
        DataSet[] tables = new DataSet[]{ds};
        int source = 3;
        int target = 4;
        DataSet result = sT_ShortestPath.evaluate(
                dsf,
                tables,
                new Value[]{
                    ValueFactory.createValue(source),
                    ValueFactory.createValue(target),
                    ValueFactory.createValue("length")
                },
                new NullProgressMonitor());
        assertTrue(result.getRowCount() == 3);
        ds.close();
    }
}
