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
     * A test to validate the network graph method
     *
     * @throws Exception
     */
    @Test
    public void testST_Graph() throws Exception {

        //Input datasource                
        DataSource data = dsf.getDataSource(GRAPH2D);
        data.open();

        ST_Graph fn = new ST_Graph();
        DataSource[] tables = new DataSource[]{data};
        fn.evaluate(dsf,
                    tables,
                    new Value[]{ValueFactory.createValue(0),
                                ValueFactory.createValue(false),
                                ValueFactory.createValue("output")},
                    new NullProgressMonitor());

        DataSource nodes = dsf.getDataSource("output.nodes");

        // The graph returns 11 nodes
        nodes.open();
        assertTrue(nodes.getRowCount() == 6);
        nodes.close();

        DataSource edges = dsf.getDataSource("output.edges");
        DataSource expectedEdges = dsf.getDataSource(GRAPH2D_EDGES);

        // The graph returns 10 lines. The same as input.
        edges.open();
        expectedEdges.open();
        assertTrue(edges.getRowCount() == 6);
        // Make sure expectedEdges and edges match.
        for (int i = 0; i < expectedEdges.getRowCount(); i++) {
            assertTrue(checkIsPresent(expectedEdges.getRow(i), edges));
        }
        expectedEdges.close();
        edges.close();
        data.close();
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
            ValueFactory.createValue(wktReader.
            read("LINESTRING( 0 0 0, 5 5 10)")),
            ValueFactory.createValue(1)
        });
        driver_src.addValues(
                new Value[]{
            ValueFactory.createValue(wktReader.read(
            "LINESTRING( 0 10 5, 5 5 10)")),
            ValueFactory.createValue(2)
        });
        driver_src.addValues(
                new Value[]{
            ValueFactory.createValue(wktReader.read(
            "LINESTRING( 10 5 15, 5 5 10)")),
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
                assertTrue(
                        (values[3].getAsInt() == 1) && (values[4].getAsInt() == 2));
            } else if (values[gidField].getAsInt() == 2) {
                assertTrue(
                        (values[3].getAsInt() == 1) && (values[4].getAsInt() == 3));
            } else if (values[gidField].getAsInt() == 3) {
                assertTrue(
                        (values[3].getAsInt() == 4) && (values[4].getAsInt() == 1));
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
            ValueFactory.createValue(wktReader.
            read("LINESTRING( 0 0 0, 5 5 10)")),
            ValueFactory.createValue(1)
        });
        driver_src.addValues(
                new Value[]{
            ValueFactory.createValue(wktReader.read(
            "LINESTRING( 0 10 5, 5 5 10)")),
            ValueFactory.createValue(2)
        });
        driver_src.addValues(
                new Value[]{
            ValueFactory.createValue(wktReader.read(
            "LINESTRING( 10 5 5, 5 5 10)")),
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
                assertTrue(
                        (values[3].getAsInt() == 1) && (values[4].getAsInt() == 2));
            } else if (values[gidField].getAsInt() == 2) {
                assertTrue(
                        (values[3].getAsInt() == 1) && (values[4].getAsInt() == 3));
            } else if (values[gidField].getAsInt() == 3) {
                assertTrue(
                        (values[3].getAsInt() == 1) && (values[4].getAsInt() == 4));
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
            ValueFactory.createValue(wktReader.read(
            "LINESTRING( 200 300 0, 400 300 0)")),
            ValueFactory.createValue(1)
        });
        driver_src.addValues(
                new Value[]{
            ValueFactory.createValue(wktReader.read(
            "LINESTRING( 600 300 10, 400 300 0)")),
            ValueFactory.createValue(2)
        });
        driver_src.addValues(
                new Value[]{
            ValueFactory.createValue(wktReader.read(
            "LINESTRING( 600 300 10, 800 300 0)")),
            ValueFactory.createValue(3)
        });
        driver_src.addValues(
                new Value[]{
            ValueFactory.createValue(wktReader.read(
            "LINESTRING( 600 300 0, 400 300 0)")),
            ValueFactory.createValue(2)
        });


    }

    /**
     * Check if the line expected is present in the table out.
     *
     * @param expected
     * @param out
     *
     * @return
     *
     * @throws Exception
     */
    private boolean checkIsPresent(Value[] expected, DataSource out) throws
            Exception {
        for (long i = 0; i < out.getRowCount(); i++) {
            Value[] vals = out.getRow(i);
            if (expected[0].getAsGeometry().equals(vals[0].getAsGeometry())) {
                return true;
            }
        }
        return false;

    }
}
