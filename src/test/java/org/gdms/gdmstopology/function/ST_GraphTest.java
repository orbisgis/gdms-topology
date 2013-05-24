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
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
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
        // TODO: Check node ids and geometries.
        DataSource nodes = dsf.getDataSource("output.nodes");
        nodes.open();
        assertTrue(nodes.getRowCount() == 6);
        nodes.close();

        // Make sure the edge geometries are as expected.
        DataSource edges = dsf.getDataSource("output.edges");
        DataSource expectedEdges = dsf.getDataSource(GRAPH2D_EDGES);
        edges.open();
        expectedEdges.open();
        assertTrue(expectedEdges.getRowCount() == edges.getRowCount());
        for (long i = 0; i < edges.getRowCount(); i++) {
            Geometry expectedGeometry =
                    expectedEdges.getRow(i)[0].getAsGeometry();
            Geometry geometry = edges.getRow(i)[0].getAsGeometry();
            assertTrue(expectedGeometry.equals(geometry));
        }
        expectedEdges.close();
        edges.close();

        data.close();
    }

    /**
     * Tests orienting when node 1 has higher elevation than node 2 (1 --> 2).
     *
     * @throws Exception
     */
    @Test
    public void orientNodeOneToNodeTwo() throws Exception {
        orientByElevation(10, 5);
    }

    /**
     * Tests orienting when node 2 has higher elevation than node 1 (2 --> 1).
     *
     * @throws Exception
     */
    @Test
    public void orientNodeTwoToNodeOne() throws Exception {
        orientByElevation(5, 10);
    }

    /**
     * Tests orienting when nodes 1 and 2 have equal elevation (1 --> 2).
     *
     * @throws Exception
     */
    @Test
    public void orientSameElevation() throws Exception {
        orientByElevation(5, 5);
    }

    /**
     * Constructs a linestring consisting of distinct points nodeOne with
     * z-coordinate zOne and nodeTwo with z-coordinate zTwo; checks that the
     * resulting nodes and edges tables are created correctly and that the
     * resulting edge has the correct orientation.
     *
     * @param zOne z-coordinate of nodeOne
     * @param zTwo z-coordinate of nodeTwo
     *
     * @throws Exception
     */
    public void orientByElevation(int zOne, int zTwo) throws Exception {

        String nodeOne = "1 2 " + zOne;
        String nodeTwo = "3 4 " + zTwo;

        MemoryDataSetDriver data = initializeDriver();
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(" + nodeOne + ", " + nodeTwo + ")")),
            ValueFactory.createValue(1)});
        DataSet[] tables = new DataSet[]{data};

        // Evaluate ST_Graph.
        new ST_Graph().evaluate(dsf,
                                tables,
                                // Tolerance, orient by elevation, output
                                new Value[]{ValueFactory.createValue(0),
                                            ValueFactory.createValue(true),
                                            ValueFactory.createValue("output")},
                                new NullProgressMonitor());

        // Check the nodes table.
        DataSource nodes = dsf.getDataSource("output.nodes");
        nodes.open();
        assertTrue(nodes.getRowCount() == 2);
        Value[] row1 = nodes.getRow(0);
        assertTrue(row1[0].getAsGeometry().equals(
                wktReader.read("POINT(" + nodeOne + ")")));
        assertTrue(row1[1].getAsInt() == 1);
        Value[] row2 = nodes.getRow(1);
        assertTrue(row2[0].getAsGeometry().equals(
                wktReader.read("POINT(" + nodeTwo + ")")));
        assertTrue(row2[1].getAsInt() == 2);
        nodes.close();

        // Check the edges table.
        DataSource edges = dsf.getDataSource("output.edges");
        edges.open();
        System.out.println("");
        assertTrue(edges.getRowCount() == 1);
        Value[] row = edges.getRow(0);
        int source = row[3].getAsInt();
        int target = row[4].getAsInt();
        if (zTwo > zOne) {
            assertTrue(source == 2 && target == 1);
        } // zOne >= zTwo
        else {
            assertTrue(source == 1 && target == 2);
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

    /**
     * Prints the given table for debugging.
     *
     * @param table The table
     *
     * @throws DriverException
     */
    private void print(DataSet table) throws DriverException {
        for (String name : table.getMetadata().getFieldNames()) {
            System.out.print(name + "\t");
        }
        System.out.println("");
        for (int i = 0; i < table.getRowCount(); i++) {
            for (Value v : table.getRow(i)) {
                System.out.print(v + "\t");
            }
            System.out.println("");
        }
    }
}
