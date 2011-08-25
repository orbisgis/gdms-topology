/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gdms.gdmstopology.function;

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
 * @author ebocher
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
                        new String[]{"the_geom", "id"},
                        new Type[]{TypeFactory.createType(Type.GEOMETRY),
                                TypeFactory.createType(Type.INT)
                        });

                driver_src.addValues(new Value[]{ValueFactory.createValue(wktReader.read("POLYGON( ( 69 152, 69 293, 221 293, 221 152, 69 152 ))")),
                                ValueFactory.createValue(1)});
                driver_src.addValues(new Value[]{ValueFactory.createValue(wktReader.read("POLYGON( ( 221 152, 221 293, 390 293, 390 152, 221 152 ))")),
                                ValueFactory.createValue(2)});


                ST_PlanarGraph st_PlanarGraph = new ST_PlanarGraph();
                DataSet[] tables = new DataSet[]{driver_src};
                st_PlanarGraph.evaluate(dsf, tables, new Value[]{ValueFactory.createValue("output")}, new NullProgressMonitor());


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
         * @throws Exception
         */
        @Test
        public void testST_Graph() throws Exception {

                //Input datasource                
                DataSource srcDS = dsf.getDataSource(GRAPH);
                srcDS.open();

                ST_Graph st_Graph = new ST_Graph();
                DataSource[] tables = new DataSource[]{srcDS};
                st_Graph.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(0), ValueFactory.createValue(false), ValueFactory.createValue("output")}, new NullProgressMonitor());

                DataSource dsResult_nodes = dsf.getDataSource("output.nodes");


                dsResult_nodes.open();
                //The graph returns 11 nodes
                assertTrue(dsResult_nodes.getRowCount() == 11);
                dsResult_nodes.close();

                DataSource dsResult_edges = dsf.getDataSource("output.edges");
                DataSource expectedDS = dsf.getDataSource(GRAPH_EDGES);

                dsResult_edges.open();
                //The graph returns 10 lines. The same as input.
                assertTrue(dsResult_edges.getRowCount() == 10);

                expectedDS.open();
                //Check if all values are equals between input datasource and excepted datasource
                for (int i = 0; i < expectedDS.getRowCount(); i++) {
                        assertTrue(checkIsPresent(expectedDS.getRow(i), dsResult_edges));

                }
                expectedDS.close();
                dsResult_edges.close();
                srcDS.close();



        }

        /**
         * Check if the line expected is present in the table out.
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
