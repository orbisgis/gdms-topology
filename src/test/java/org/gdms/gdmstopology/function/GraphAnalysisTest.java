package org.gdms.gdmstopology.function;

import com.vividsolutions.jts.geom.Geometry;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.types.Type;
import org.junit.Test;
import org.gdms.data.DataSource;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.gdmstopology.TopologySetUpTest;
import org.orbisgis.progress.NullProgressMonitor;


import static org.junit.Assert.*;

/**
 *
 * @author ebocher
 */
public class GraphAnalysisTest extends TopologySetUpTest {

        @Test
        public void testST_ShortestPath() throws Exception {
                ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
                DataSource ds = dsf.getDataSource(GRAPH_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 3;
                int target = 4;
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target)}, null);
                assertTrue(result.getRowCount() == 1);
                assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING ( 191.77905737704918 272.73798360655735 10, 213.28322950819674 185.6593606557377 20 )")));
                ds.close();
        }

        @Test
        public void testST_ShortestPath2() throws Exception {
                ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
                DataSource ds = dsf.getDataSource(GRAPH_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 4;
                int target = 3;
                sT_ShortestPath = new ST_ShortestPath();
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target)}, null);
                assertTrue(result.getRowCount() == 0);
                ds.close();

        }

        @Test
        public void testST_ShortestPath3() throws Exception {
                ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
                DataSource ds = dsf.getDataSource(GRAPH_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 4;
                int target = 3;
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue(true)}, null);
                assertTrue(result.getRowCount() == 1);
                assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING ( 191.77905737704918 272.73798360655735 10, 213.28322950819674 185.6593606557377 20 )")));
                ds.close();
        }

        @Test
        public void testST_ShortestPath4() throws Exception {
                ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
                DataSource ds = dsf.getDataSource(GRAPH_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 3;
                int target = 8;
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue(true)}, null);
                assertTrue(result.getRowCount() == 3);
                ds.close();
        }

        @Test
        public void testST_ShortestPath5() throws Exception {
                ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();

                MemoryDataSetDriver mdsd = new MemoryDataSetDriver(new String[]{"geom", "start_node", "end_node", "weigth"},
                        new Type[]{
                                TypeFactory.createType(Type.GEOMETRY),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)});

                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 2 2)")), ValueFactory.createValue(1), ValueFactory.createValue(2), ValueFactory.createValue(0)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(2 2, 4 4 , 6 2)")), ValueFactory.createValue(2), ValueFactory.createValue(3), ValueFactory.createValue(0)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(2 2, 4 1 , 6 2)")), ValueFactory.createValue(2), ValueFactory.createValue(3), ValueFactory.createValue(10)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(6 2  , 10 2)")), ValueFactory.createValue(3), ValueFactory.createValue(4), ValueFactory.createValue(0)});

                DataSet[] tables = new DataSet[]{mdsd};
                int source = 1;
                int target = 4;
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target)}, null);
                assertTrue(result.getRowCount() == 3);
                assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING(0 0, 2 2)")));
                assertTrue(result.getFieldValue(1, 0).getAsGeometry().equals(wktReader.read("LINESTRING(2 2, 4 4 , 6 2)")));
                assertTrue(result.getFieldValue(2, 0).getAsGeometry().equals(wktReader.read("LINESTRING(6 2  , 10 2)")));

        }

        @Test
        public void testST_ShortestPath6() throws Exception {
                ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();

                MemoryDataSetDriver mdsd = new MemoryDataSetDriver(new String[]{"geom", "start_node", "end_node", "weigth"},
                        new Type[]{
                                TypeFactory.createType(Type.GEOMETRY),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)});

                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 2 2)")), ValueFactory.createValue(1), ValueFactory.createValue(2), ValueFactory.createValue(0)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(2 2, 4 4 , 6 2)")), ValueFactory.createValue(2), ValueFactory.createValue(3), ValueFactory.createValue(10)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(2 2, 4 1 , 6 2)")), ValueFactory.createValue(2), ValueFactory.createValue(3), ValueFactory.createValue(0)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(6 2  , 10 2)")), ValueFactory.createValue(3), ValueFactory.createValue(4), ValueFactory.createValue(0)});

                DataSet[] tables = new DataSet[]{mdsd};
                int source = 1;
                int target = 4;
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target)}, null);
                assertTrue(result.getRowCount() == 3);
                assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING(0 0, 2 2)")));
                assertTrue(result.getFieldValue(1, 0).getAsGeometry().equals(wktReader.read("LINESTRING(2 2, 4 1 , 6 2)")));
                assertTrue(result.getFieldValue(2, 0).getAsGeometry().equals(wktReader.read("LINESTRING(6 2  , 10 2)")));
        }

        @Test
        public void testFindReachableEdges() throws Exception {
                MemoryDataSetDriver mdsd = new MemoryDataSetDriver(new String[]{"geom", "start_node", "end_node", "weigth"},
                        new Type[]{
                                TypeFactory.createType(Type.GEOMETRY),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)});

                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 5 0)")),
                                ValueFactory.createValue(1), ValueFactory.createValue(2), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(5 0, 7 0)")),
                                ValueFactory.createValue(3), ValueFactory.createValue(2), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(7 0 , 10 0)")),
                                ValueFactory.createValue(4), ValueFactory.createValue(3), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(10 0  , 20 0)")),
                                ValueFactory.createValue(5), ValueFactory.createValue(4), ValueFactory.createValue(1)});

                DataSet[] tables = new DataSet[]{mdsd};
                int source = 1;
                ST_FindReachableEdges stspl = new ST_FindReachableEdges();
                DataSet result = stspl.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source)}, new NullProgressMonitor());
                assertTrue(result.getRowCount() == 1);
                assertTrue(result.getGeometry(0, 0).equalsExact(wktReader.read("LINESTRING(0 0, 5 0)")));

        }

        @Test
        public void testFindReachableEdges2() throws Exception {
                MemoryDataSetDriver mdsd = new MemoryDataSetDriver(new String[]{"geom", "start_node", "end_node", "weigth"},
                        new Type[]{
                                TypeFactory.createType(Type.GEOMETRY),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)});

                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 5 0)")),
                                ValueFactory.createValue(1), ValueFactory.createValue(2), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(5 0, 7 0)")),
                                ValueFactory.createValue(3), ValueFactory.createValue(2), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(7 0 , 10 0)")),
                                ValueFactory.createValue(4), ValueFactory.createValue(3), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(10 0  , 20 0)")),
                                ValueFactory.createValue(5), ValueFactory.createValue(4), ValueFactory.createValue(1)});

                DataSet[] tables = new DataSet[]{mdsd};
                int source = 2;
                ST_FindReachableEdges stspl = new ST_FindReachableEdges();
                DataSet result = stspl.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source)}, new NullProgressMonitor());
                assertTrue(result.getRowCount() == 0);

        }

        @Test
        public void testFindReachableEdgesr3() throws Exception {
                MemoryDataSetDriver mdsd = new MemoryDataSetDriver(new String[]{"geom", "start_node", "end_node", "weigth"},
                        new Type[]{
                                TypeFactory.createType(Type.GEOMETRY),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)});

                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 5 0)")),
                                ValueFactory.createValue(1), ValueFactory.createValue(2), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(5 0, 7 0)")),
                                ValueFactory.createValue(3), ValueFactory.createValue(2), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(7 0 , 10 0)")),
                                ValueFactory.createValue(4), ValueFactory.createValue(3), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(10 0  , 20 0)")),
                                ValueFactory.createValue(5), ValueFactory.createValue(4), ValueFactory.createValue(1)});

                DataSet[] tables = new DataSet[]{mdsd};
                int source = 5;
                ST_FindReachableEdges stspl = new ST_FindReachableEdges();
                DataSet result = stspl.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source)}, new NullProgressMonitor());
                assertTrue(result.getRowCount() == 3);
                assertTrue(result.getGeometry(0, 0).equalsExact(wktReader.read("LINESTRING(10 0 , 20 0)")));
                assertTrue(result.getGeometry(1, 0).equalsExact(wktReader.read("LINESTRING(7 0 , 10 0)")));
                assertTrue(result.getGeometry(2, 0).equalsExact(wktReader.read("LINESTRING(5 0, 7 0)")));
        }

        @Test
        public void testFindReachableEdges4() throws Exception {
                MemoryDataSetDriver mdsd = new MemoryDataSetDriver(new String[]{"geom", "start_node", "end_node", "weigth"},
                        new Type[]{
                                TypeFactory.createType(Type.GEOMETRY),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)});

                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 5 0)")),
                                ValueFactory.createValue(1), ValueFactory.createValue(2), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(5 0, 7 0)")),
                                ValueFactory.createValue(3), ValueFactory.createValue(2), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(7 0 , 10 0)")),
                                ValueFactory.createValue(4), ValueFactory.createValue(3), ValueFactory.createValue(1)});
                mdsd.addValues(new Value[]{ValueFactory.createValue(wktReader.read("LINESTRING(10 0  , 20 0)")),
                                ValueFactory.createValue(5), ValueFactory.createValue(4), ValueFactory.createValue(1)});

                DataSet[] tables = new DataSet[]{mdsd};
                int source = 2;
                ST_FindReachableEdges stspl = new ST_FindReachableEdges();
                DataSet result = stspl.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(true), ValueFactory.createValue(false)}, new NullProgressMonitor());
                assertTrue(result.getRowCount() == 4);

                int count = 0;
                for (int i = 0; i < result.getRowCount(); i++) {
                        Geometry geom = result.getGeometry(0, 0);
                        for (int j = 0; j < mdsd.getRowCount(); j++) {
                                if (geom.equalsExact(mdsd.getGeometry(j, 0))) {
                                        count++;
                                }
                        }
                }
                assertTrue(count == 4);
        }
}
