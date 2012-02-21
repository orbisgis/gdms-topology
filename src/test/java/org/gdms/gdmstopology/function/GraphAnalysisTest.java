/**
 * GDMS-Topology  is a library dedicated to graph analysis. It's based on the JGraphT available at
 * http://www.jgrapht.org/. It ables to compute and process large graphs using spatial
 * and alphanumeric indexes.
 * This version is developed at French IRSTV institut as part of the
 * EvalPDU project, funded by the French Agence Nationale de la Recherche
 * (ANR) under contract ANR-08-VILL-0005-01 and GEBD project
 * funded by the French Ministery of Ecology and Sustainable Development .
 *
 * GDMS-Topology  is distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://trac.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.gdms.gdmstopology.function;

import com.vividsolutions.jts.geom.Geometry;
import java.util.HashMap;
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
                DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 3;
                int target = 5;
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue("length")}, null);
                assertTrue(result.getRowCount() == 1);
                assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING ( 222 242, 335 313 )")));
                ds.close();
        }

        @Test
        public void testST_ShortestPath2() throws Exception {
                ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
                DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 5;
                int target = 3;
                sT_ShortestPath = new ST_ShortestPath();
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue("length")}, null);
                assertTrue(result.getRowCount() == 0);
                ds.close();

        }

        @Test
        public void testST_ShortestPath3() throws Exception {
                ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
                DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 5;
                int target = 3;
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue("length"), ValueFactory.createValue(true)}, null);
                assertTrue(result.getRowCount() == 1);
                assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING ( 222 242, 335 313 )")));
                ds.close();
        }

        @Test
        public void testST_ShortestPath4() throws Exception {
                ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
                DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 3;
                int target = 4;
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue("length"), ValueFactory.createValue(true)}, null);
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
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue("weigth")}, null);
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
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue("weigth")}, null);
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

        @Test
        public void testST_ShortestPathLength() throws Exception {
                ST_ShortestPathLength sT_ShortestPathLength = new ST_ShortestPathLength();
                DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 3;
                DataSet result = sT_ShortestPathLength.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue("length")}, null);
                assertTrue(result.getRowCount() == 5);
                HashMap<Integer, Double> results = new HashMap<Integer, Double>();
                results.put(3, 0d);
                results.put(5, 133.4541119636259);
                results.put(6, 51.35172830587107);
                results.put(1, (211.6687715105811 + 51.35172830587107));
                results.put(4, (211.6687715105811 + 51.35172830587107) + 56.32051136131489);
                for (int i = 0; i < result.getRowCount(); i++) {
                        int key = result.getInt(i, 2);
                        double cost = result.getDouble(i, 3);
                        assertTrue((results.get(key) - cost) == 0);

                }
                ds.close();
        }

        @Test
        public void testST_ShortestPathLengthReverse() throws Exception {
                ST_ShortestPathLength sT_ShortestPathLength = new ST_ShortestPathLength();
                DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 3;
                DataSet result = sT_ShortestPathLength.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue("length"), ValueFactory.createValue(false), ValueFactory.createValue(true)}, null);
                assertTrue(result.getRowCount() == 2);
                HashMap<Integer, Double> results = new HashMap<Integer, Double>();
                results.put(3, 0d);
                results.put(2, 129.63024338479042);
                for (int i = 0; i < result.getRowCount(); i++) {
                        int key = result.getInt(i, 2);
                        double cost = result.getDouble(i, 3);
                        assertTrue((results.get(key) - cost) == 0);
                }
                ds.close();
        }

        @Test
        public void testST_ShortestPathLengthMultiGraph() throws Exception {
                ST_ShortestPathLength sT_ShortestPathLength = new ST_ShortestPathLength();
                DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 3;
                DataSet result = sT_ShortestPathLength.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue("length"), ValueFactory.createValue(true), ValueFactory.createValue(false)}, null);
                assertTrue(result.getRowCount() == 6);
                HashMap<Integer, Double> results = new HashMap<Integer, Double>();
                results.put(3, 0d);
                results.put(2, 129.63024338479042);
                results.put(5, 133.4541119636259);
                results.put(6, 51.35172830587107);
                results.put(1, (211.6687715105811 + 51.35172830587107));
                results.put(4, (211.6687715105811 + 51.35172830587107) + 56.32051136131489);
                for (int i = 0; i < result.getRowCount(); i++) {
                        int key = result.getInt(i, 2);
                        double cost = result.getDouble(i, 3);
                        assertTrue((results.get(key) - cost) == 0);
                }
                ds.close();
        }
}
