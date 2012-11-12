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

import org.gdms.driver.DriverException;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.data.DataSource;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.gdmstopology.TopologySetUpTest;
import org.junit.Test;
import org.orbisgis.progress.NullProgressMonitor;

import static org.junit.Assert.*;

/**
 *
 * @author Erwan Bocher
 */
public class GraphBuilderTest extends TopologySetUpTest {

    /**
     * A test to validate the planar graph method with two polygons.
     *
     * @throws Exception
     */
    @Test
    public void testST_PlanarGraph() throws Exception {
        //Input datasource
        final MemoryDataSetDriver driver_src = new MemoryDataSetDriver(
                new String[]{
                    "the_geom",
                    "id"
                },
                new Type[]{
                    TypeFactory.createType(Type.GEOMETRY),
                    TypeFactory.createType(Type.INT)
                });

        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("POLYGON( ( 69 152, 69 293, 221 293, 221 152, 69 152 ))")),
                    ValueFactory.createValue(1)
                });
        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("POLYGON( ( 221 152, 221 293, 390 293, 390 152, 221 152 ))")),
                    ValueFactory.createValue(2)
                });


        ST_PlanarGraph st_PlanarGraph = new ST_PlanarGraph();
        DataSet[] tables = new DataSet[]{driver_src};
        st_PlanarGraph.evaluate(
                dsf,
                tables,
                new Value[]{
                    ValueFactory.createValue("output")
                },
                new NullProgressMonitor());


        DataSource dsResult_polygons = dsf.getDataSource("output.polygons");


        dsResult_polygons.open();
        //The planar graph returns 2 polygons
        assertTrue(dsResult_polygons.getRowCount() == 2);

        dsResult_polygons.close();
        DataSource dsResult_edges = dsf.getDataSource("output.edges");

        dsResult_edges.open();
        //The planar graph returns 3 lines
        assertTrue(dsResult_edges.getRowCount() == 3);

        //Test the gid values for the 3 lines                
        Value[] row0 = dsResult_edges.getRow(0);
        //This row corresponds to the shared segment between geometry from row0 and row1
        Value[] row1 = dsResult_edges.getRow(1);
        Value[] row2 = dsResult_edges.getRow(2);

        int gidRight = row1[4].getAsInt();
        int gidLeft = row1[5].getAsInt();

        //Check the left and right gids
        //if the gids are not equal do a flip test, otherwise the topology is not valid.
        if (gidLeft == 1 && gidRight == 2) {
            //Left gid
            assertTrue(row0[5].getAsInt() == -1);
            assertTrue(row2[5].getAsInt() == -1);
            //Right gid
            assertTrue(row0[4].getAsInt() == 1);
            assertTrue(row2[4].getAsInt() == 2);
        } else if (gidLeft == 2 && gidRight == 1) {
            //Left gid
            assertTrue(row0[5].getAsInt() == -1);
            assertTrue(row2[5].getAsInt() == -1);
            //Right gid
            assertTrue(row0[4].getAsInt() == 2);
            assertTrue(row2[4].getAsInt() == 1);
        } else {
            assertTrue("The topology is not valid", false);
        }

        //Check the start and end gids
        assertTrue(row0[2].getAsInt() == 1);
        assertTrue(row0[3].getAsInt() == 2);
        assertTrue(row1[2].getAsInt() == 1);
        assertTrue(row1[3].getAsInt() == 2);
        assertTrue(row2[2].getAsInt() == 2);
        assertTrue(row2[3].getAsInt() == 1);

        dsResult_edges.close();
    }

    /**
     * A test to validate the network graph method
     *
     * @throws Exception
     */
    @Test
    public void testST_Graph() throws Exception {

        //Input datasource                
        DataSource srcDS = dsf.getDataSource(GRAPH2D);
        srcDS.open();

        ST_Graph st_Graph = new ST_Graph();
        DataSource[] tables = new DataSource[]{srcDS};
        st_Graph.evaluate(
                dsf,
                tables,
                new Value[]{
                    ValueFactory.createValue(0),
                    ValueFactory.createValue(false),
                    ValueFactory.createValue("output")
                },
                new NullProgressMonitor());

        DataSource dsResult_nodes = dsf.getDataSource("output.nodes");


        dsResult_nodes.open();
        //The graph returns 11 nodes
        assertTrue(dsResult_nodes.getRowCount() == 6);
        dsResult_nodes.close();

        DataSource dsResult_edges = dsf.getDataSource("output.edges");
        DataSource expectedDS = dsf.getDataSource(GRAPH2D_EDGES);

        dsResult_edges.open();
        //The graph returns 10 lines. The same as input.
        assertTrue(dsResult_edges.getRowCount() == 6);

        expectedDS.open();
        //Check if all values are equals between input datasource and excepted datasource
        for (int i = 0; i < expectedDS.getRowCount(); i++) {
            assertTrue(checkIsPresent(expectedDS.getRow(i), dsResult_edges));

        }
        expectedDS.close();
        dsResult_edges.close();
        srcDS.close();

    }

    @Test
    public void testZGraph() throws Exception {

        //Input datasource
        final MemoryDataSetDriver driver_src = new MemoryDataSetDriver(
                new String[]{
                    "the_geom",
                    "gid"
                },
                new Type[]{
                    TypeFactory.createType(Type.GEOMETRY),
                    TypeFactory.createType(Type.INT)
                });

        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING( 0 0 0, 5 5 10)")),
                    ValueFactory.createValue(1)
                });
        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING( 0 10 5, 5 5 10)")),
                    ValueFactory.createValue(2)
                });
        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING( 10 5 15, 5 5 10)")),
                    ValueFactory.createValue(3)
                });


        ST_Graph st_Graph = new ST_Graph();
        DataSet[] tables = new DataSet[]{driver_src};
        st_Graph.evaluate(
                dsf,
                tables,
                new Value[]{
                    ValueFactory.createValue(0),
                    ValueFactory.createValue(true),
                    ValueFactory.createValue("output")
                },
                new NullProgressMonitor());

        DataSource dsResult_nodes = dsf.getDataSource("output.edges");
        dsResult_nodes.open();
        int gidField = dsResult_nodes.getMetadata().getFieldIndex("gid");

        for (int i = 0; i < dsResult_nodes.getRowCount(); i++) {
            Value[] values = dsResult_nodes.getRow(i);

            if (values[gidField].getAsInt() == 1) {
                assertTrue((values[3].getAsInt() == 1) && (values[4].getAsInt() == 2));
            } else if (values[gidField].getAsInt() == 2) {
                assertTrue((values[3].getAsInt() == 1) && (values[4].getAsInt() == 3));
            } else if (values[gidField].getAsInt() == 3) {
                assertTrue((values[3].getAsInt() == 4) && (values[4].getAsInt() == 1));
            }
        }
        dsResult_nodes.close();
    }

    @Test
    public void testZGraph2() throws Exception {

        //Input datasource
        final MemoryDataSetDriver driver_src = new MemoryDataSetDriver(
                new String[]{
                    "the_geom",
                    "gid"},
                new Type[]{
                    TypeFactory.createType(Type.GEOMETRY),
                    TypeFactory.createType(Type.INT)
                });

        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING( 0 0 0, 5 5 10)")),
                    ValueFactory.createValue(1)
                });
        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING( 0 10 5, 5 5 10)")),
                    ValueFactory.createValue(2)
                });
        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING( 10 5 5, 5 5 10)")),
                    ValueFactory.createValue(3)
                });


        ST_Graph st_Graph = new ST_Graph();
        DataSet[] tables = new DataSet[]{driver_src};
        st_Graph.evaluate(
                dsf,
                tables,
                new Value[]{
                    ValueFactory.createValue(0),
                    ValueFactory.createValue(true),
                    ValueFactory.createValue("output")
                },
                new NullProgressMonitor());

        DataSource dsResult_nodes = dsf.getDataSource("output.edges");
        dsResult_nodes.open();
        int gidField = dsResult_nodes.getMetadata().getFieldIndex("gid");

        for (int i = 0; i < dsResult_nodes.getRowCount(); i++) {
            Value[] values = dsResult_nodes.getRow(i);

            if (values[gidField].getAsInt() == 1) {
                assertTrue((values[3].getAsInt() == 1) && (values[4].getAsInt() == 2));
            } else if (values[gidField].getAsInt() == 2) {
                assertTrue((values[3].getAsInt() == 1) && (values[4].getAsInt() == 3));
            } else if (values[gidField].getAsInt() == 3) {
                assertTrue((values[3].getAsInt() == 1) && (values[4].getAsInt() == 4));
            }
        }
        dsResult_nodes.close();
    }

    /**
     * We test if the 3D is well managed during the graph construction
     *
     * @throws Exception
     */
    @Test
    public void testDim3() throws Exception {
        //Input datasource
        final MemoryDataSetDriver driver_src = new MemoryDataSetDriver(
                new String[]{
                    "the_geom",
                    "gid"},
                new Type[]{
                    TypeFactory.createType(Type.GEOMETRY),
                    TypeFactory.createType(Type.INT)
                });

        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING( 200 300 0, 400 300 0)")),
                    ValueFactory.createValue(1)
                });
        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING( 600 300 10, 400 300 0)")),
                    ValueFactory.createValue(2)
                });
        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING( 600 300 10, 800 300 0)")),
                    ValueFactory.createValue(3)
                });
        driver_src.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING( 600 300 0, 400 300 0)")),
                    ValueFactory.createValue(2)
                });


    }

    /**
     * Check if the line expected is present in the table out.
     *
     * @param expected
     * @param out
     * @return
     * @throws Exception
     */
    private boolean checkIsPresent(Value[] expected, DataSource out) throws Exception {
        for (long i = 0; i < out.getRowCount(); i++) {
            Value[] vals = out.getRow(i);
            if (expected[0].getAsGeometry().equals(vals[0].getAsGeometry())) {
                return true;
            }
        }
        return false;

    }
}
