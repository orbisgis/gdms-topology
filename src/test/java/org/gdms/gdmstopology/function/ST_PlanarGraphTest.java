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
package org.gdms.gdmstopology.function;

import org.gdms.data.DataSource;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.gdmstopology.TopologySetupTest;
import org.orbisgis.progress.NullProgressMonitor;
import static org.junit.Assert.assertTrue;

/**
 * Tests {@link ST_PlanarGraph}.
 *
 * @author Erwan Bocher, Adam Gouge
 */
public class ST_PlanarGraphTest extends TopologySetupTest {

    /**
     * A test to validate the planar graph method with two polygons.
     *
     * @throws Exception
     */
    public void testST_PlanarGraph() throws Exception {
        final MemoryDataSetDriver sourceDriver =
                new MemoryDataSetDriver(new String[]{"the_geom", "id"},
                                        new Type[]{
            TypeFactory.createType(Type.GEOMETRY),
            TypeFactory.createType(Type.INT)});
        sourceDriver.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read(
            "POLYGON( ( 69 152, 69 293, 221 293, 221 152, 69 152 ))")),
            ValueFactory.createValue(1)});
        sourceDriver.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read(
            "POLYGON( ( 221 152, 221 293, 390 293, 390 152, 221 152 ))")),
            ValueFactory.createValue(2)});
        ST_PlanarGraph st_PlanarGraph = new ST_PlanarGraph();
        DataSet[] tables = new DataSet[]{sourceDriver};
        st_PlanarGraph.evaluate(dsf, tables,
                                new Value[]{ValueFactory.createValue("output")},
                                new NullProgressMonitor());
        DataSource dsResult_polygons =
                dsf.getDataSource("output.polygons");
        dsResult_polygons.open();
        assertTrue(dsResult_polygons.getRowCount() == 2);
        dsResult_polygons.close();
        DataSource dsResult_edges = dsf.getDataSource("output.edges");
        dsResult_edges.open();
        assertTrue(dsResult_edges.getRowCount() == 3);
        Value[] row0 = dsResult_edges.getRow(0);
        Value[] row1 = dsResult_edges.getRow(1);
        Value[] row2 = dsResult_edges.getRow(2);
        int gidRight = row1[4].getAsInt();
        int gidLeft = row1[5].getAsInt();
        if (gidLeft == 1 && gidRight == 2) {
            assertTrue(row0[5].getAsInt() == -1);
            assertTrue(row2[5].getAsInt() == -1);
            assertTrue(row0[4].getAsInt() == 1);
            assertTrue(row2[4].getAsInt() == 2);
        } else if (gidLeft == 2 && gidRight == 1) {
            assertTrue(row0[5].getAsInt() == -1);
            assertTrue(row2[5].getAsInt() == -1);
            assertTrue(row0[4].getAsInt() == 2);
            assertTrue(row2[4].getAsInt() == 1);
        } else {
            assertTrue("The topology is not valid", false);
        }
        assertTrue(row0[2].getAsInt() == 1);
        assertTrue(row0[3].getAsInt() == 2);
        assertTrue(row1[2].getAsInt() == 1);
        assertTrue(row1[3].getAsInt() == 2);
        assertTrue(row2[2].getAsInt() == 2);
        assertTrue(row2[3].getAsInt() == 1);
        dsResult_edges.close();
    }
}
