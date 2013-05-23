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

import com.vividsolutions.jts.geom.Geometry;
import org.gdms.data.DataSource;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.gdmstopology.TopologySetupTest;
import static org.junit.Assert.*;
import org.junit.Test;
import org.orbisgis.progress.NullProgressMonitor;

/**
 * Tests {@link ST_Graph}.
 *
 * @author Erwan Bocher, Adam Gouge
 */
public class ST_GraphTest extends TopologySetupTest {

    /**
     * Tests ST_Graph on a 2D graph.
     *
     * @throws Exception
     */
    @Test
    public void graph2DTest() throws Exception {

        DataSource data = dsf.getDataSource(GRAPH2D);
        data.open();
        DataSource[] tables = new DataSource[]{data};

        new ST_Graph().evaluate(dsf,
                                tables,
                                new Value[]{ValueFactory.createValue(0),
                                            ValueFactory.createValue(false),
                                            ValueFactory.createValue("output")},
                                new NullProgressMonitor());

        // Make sure six nodes were created.
        DataSource nodes = dsf.getDataSource("output.nodes");
        nodes.open();
        assertTrue(nodes.getRowCount() == 6);
        nodes.close();

        // Make sure the edge geometries are as expected.
        DataSource edges = dsf.getDataSource("output.edges");
        DataSource expectedEdges = dsf.getDataSource(GRAPH2D_EDGES);
        edges.open();
        expectedEdges.open();
        checkGeometries(expectedEdges, edges);
        expectedEdges.close();
        edges.close();

        data.close();
    }

    /**
     * Makes sure ST_Graph correctly orients edges by their elevation when
     * requested to do so.
     *
     * @throws Exception
     */
    @Test
    public void orientByElevationTest1() throws Exception {

        // Prepare data.
        MemoryDataSetDriver data = initializeDriver();
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING( 0 0 0, 1 1 2)")),
            ValueFactory.createValue(1)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING( 0 2 1, 1 1 2)")),
            ValueFactory.createValue(2)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING( 2 1 3, 1 1 2)")),
            ValueFactory.createValue(3)});
        DataSet[] tables = new DataSet[]{data};

        // Evaluate ST_Graph.
        new ST_Graph().evaluate(dsf,
                                tables,
                                // Tolerance, orient by elevation, output
                                new Value[]{ValueFactory.createValue(0),
                                            ValueFactory.createValue(true),
                                            ValueFactory.createValue("output")},
                                new NullProgressMonitor());

        // Check edge orientations.
        DataSource edges = dsf.getDataSource("output.edges");
        edges.open();
        int gidIndex = edges.getMetadata().getFieldIndex("gid");
        for (int i = 0; i < edges.getRowCount(); i++) {
            Value[] row = edges.getRow(i);
            int id = row[gidIndex].getAsInt();
            int source = row[3].getAsInt();
            int target = row[4].getAsInt();

            if (id == 1) {
                assertTrue(source == 1 && target == 2);
            } else if (id == 2) {
                assertTrue(source == 1 && target == 3);
            } else if (id == 3) {
                assertTrue(source == 4 && target == 1);
            }
        }
        edges.close();
    }

    /**
     * Makes sure ST_Graph correctly orients edges by their elevation when
     * requested to do so.
     *
     * @throws Exception
     */
    @Test
    public void orientByElevationTest2() throws Exception {

        // Prepare data.
        MemoryDataSetDriver data = initializeDriver();
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING( 0 0 0, 1 1 2)")),
            ValueFactory.createValue(1)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING( 0 2 1, 1 1 2)")),
            ValueFactory.createValue(2)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING( 2 1 1, 1 1 2)")),
            ValueFactory.createValue(3)});
        DataSet[] tables = new DataSet[]{data};

        // Evaluate ST_Graph.
        new ST_Graph().evaluate(dsf,
                                tables,
                                // Tolerance, orient by elevation, output
                                new Value[]{ValueFactory.createValue(0),
                                            ValueFactory.createValue(true),
                                            ValueFactory.createValue("output")},
                                new NullProgressMonitor());

        // Check edge orientations.
        DataSource edges = dsf.getDataSource("output.edges");
        edges.open();
        int gidIndex = edges.getMetadata().getFieldIndex("gid");
        for (int i = 0; i < edges.getRowCount(); i++) {
            Value[] row = edges.getRow(i);
            int id = row[gidIndex].getAsInt();
            int source = row[3].getAsInt();
            int target = row[4].getAsInt();

            if (id == 1) {
                assertTrue(source == 1 && target == 2);
            } else if (id == 2) {
                assertTrue(source == 1 && target == 3);
            } else if (id == 3) {
                assertTrue(source == 1 && target == 4);
            }
        }
        edges.close();
    }

    /**
     * We test if the 3D is well managed during the graph construction
     *
     * @throws Exception
     */
    @Test
    public void testDim3() throws Exception {
        MemoryDataSetDriver data = initializeDriver();
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING( 200 300 0, 400 300 0)")),
            ValueFactory.createValue(1)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING( 600 300 10, 400 300 0)")),
            ValueFactory.createValue(2)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING( 600 300 10, 800 300 0)")),
            ValueFactory.createValue(3)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING( 600 300 0, 400 300 0)")),
            ValueFactory.createValue(2)});
    }

    /**
     * Checks the geometries (in the first column) in a given table against
     * expected geometries.
     *
     * @param expected Table containing expected geometries
     * @param table    Table to check
     *
     * @throws Exception
     */
    private void checkGeometries(DataSource expected, DataSource table)
            throws Exception {
        assertTrue(expected.getRowCount() == table.getRowCount());
        for (long i = 0; i < table.getRowCount(); i++) {
            Geometry expectedGeometry = expected.getRow(i)[0].getAsGeometry();
            Geometry geometry = table.getRow(i)[0].getAsGeometry();
            assertTrue(expectedGeometry.equals(geometry));
        }
    }

    /**
     * Sets up a driver with geometry and gid columns ready to receive input
     * data.
     *
     * @return A newly created driver
     */
    private MemoryDataSetDriver initializeDriver() {
        final MemoryDataSetDriver data =
                new MemoryDataSetDriver(
                new String[]{"the_geom", "gid"},
                new Type[]{TypeFactory.createType(Type.GEOMETRY),
                           TypeFactory.createType(Type.INT)});
        return data;
    }
}
