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
import com.vividsolutions.jts.geom.Geometry;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.gdmstopology.TopologySetupTest;
import org.orbisgis.progress.NullProgressMonitor;
import org.junit.Test;


import static org.junit.Assert.*;

/**
 *
 * @author Erwan Bocher
 */
public class GraphUtilitiesTest extends TopologySetupTest {

    @Test
    public void testFindReachableEdges() throws Exception {
        MemoryDataSetDriver mdsd = new MemoryDataSetDriver(
                new String[]{
                    "geom",
                    "start_node",
                    "end_node", "weigth"},
                new Type[]{
                    TypeFactory.createType(Type.GEOMETRY),
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.DOUBLE)
                });

        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 5 0)")),
                    ValueFactory.createValue(1),
                    ValueFactory.createValue(2),
                    ValueFactory.createValue(1)
                });
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(5 0, 7 0)")),
                    ValueFactory.createValue(3),
                    ValueFactory.createValue(2),
                    ValueFactory.createValue(1)
                });
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(7 0 , 10 0)")),
                    ValueFactory.createValue(4),
                    ValueFactory.createValue(3),
                    ValueFactory.createValue(1)
                });
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(10 0  , 20 0)")),
                    ValueFactory.createValue(5),
                    ValueFactory.createValue(4),
                    ValueFactory.createValue(1)
                });

        DataSet[] tables = new DataSet[]{mdsd};
        int source = 1;
        ST_FindReachableEdges stspl = new ST_FindReachableEdges();
        DataSet result = stspl.evaluate(
                dsf,
                tables,
                new Value[]{
                    ValueFactory.createValue(source),
                    ValueFactory.createValue("weigth")
                },
                new NullProgressMonitor());
        assertTrue(result.getRowCount() == 1);
        assertTrue(result.getGeometry(0, 0).equalsExact(wktReader.read("LINESTRING(0 0, 5 0)")));

    }

    @Test
    public void testFindReachableEdges2() throws Exception {
        MemoryDataSetDriver mdsd = new MemoryDataSetDriver(
                new String[]{
                    "geom",
                    "start_node",
                    "end_node",
                    "weigth"},
                new Type[]{
                    TypeFactory.createType(Type.GEOMETRY),
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.DOUBLE)
                });

        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 5 0)")),
                    ValueFactory.createValue(1),
                    ValueFactory.createValue(2),
                    ValueFactory.createValue(1)
                });
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(5 0, 7 0)")),
                    ValueFactory.createValue(3),
                    ValueFactory.createValue(2),
                    ValueFactory.createValue(1)});
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(7 0 , 10 0)")),
                    ValueFactory.createValue(4),
                    ValueFactory.createValue(3),
                    ValueFactory.createValue(1)});
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(10 0  , 20 0)")),
                    ValueFactory.createValue(5),
                    ValueFactory.createValue(4),
                    ValueFactory.createValue(1)});

        DataSet[] tables = new DataSet[]{mdsd};
        int source = 2;
        ST_FindReachableEdges stspl = new ST_FindReachableEdges();
        DataSet result = stspl.evaluate(
                dsf,
                tables,
                new Value[]{
                    ValueFactory.createValue(source),
                    ValueFactory.createValue("weigth")
                },
                new NullProgressMonitor());
        assertTrue(result.getRowCount() == 0);

    }

    @Test
    public void testFindReachableEdges3() throws Exception {
        MemoryDataSetDriver mdsd = new MemoryDataSetDriver(
                new String[]{
                    "geom",
                    "start_node",
                    "end_node",
                    "weigth"},
                new Type[]{
                    TypeFactory.createType(Type.GEOMETRY),
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.DOUBLE)
                });

        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 5 0)")),
                    ValueFactory.createValue(1),
                    ValueFactory.createValue(2),
                    ValueFactory.createValue(1)
                });
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(5 0, 7 0)")),
                    ValueFactory.createValue(3),
                    ValueFactory.createValue(2),
                    ValueFactory.createValue(1)
                });
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(7 0 , 10 0)")),
                    ValueFactory.createValue(4),
                    ValueFactory.createValue(3),
                    ValueFactory.createValue(1)
                });
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(10 0  , 20 0)")),
                    ValueFactory.createValue(5),
                    ValueFactory.createValue(4),
                    ValueFactory.createValue(1)
                });

        DataSet[] tables = new DataSet[]{mdsd};
        int source = 5;
        ST_FindReachableEdges stspl = new ST_FindReachableEdges();
        DataSet result = stspl.evaluate(
                dsf,
                tables,
                new Value[]{
                    ValueFactory.createValue(source),
                    ValueFactory.createValue("weigth")
                },
                new NullProgressMonitor());
        assertTrue(result.getRowCount() == 3);
        assertTrue(result.getGeometry(0, 0).equalsExact(wktReader.read("LINESTRING(10 0 , 20 0)")));
        assertTrue(result.getGeometry(1, 0).equalsExact(wktReader.read("LINESTRING(7 0 , 10 0)")));
        assertTrue(result.getGeometry(2, 0).equalsExact(wktReader.read("LINESTRING(5 0, 7 0)")));
    }

    @Test
    public void testFindReachableEdges4() throws Exception {
        MemoryDataSetDriver mdsd = new MemoryDataSetDriver(
                new String[]{
                    "geom",
                    "start_node",
                    "end_node",
                    "weigth"},
                new Type[]{
                    TypeFactory.createType(Type.GEOMETRY),
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.DOUBLE)
                });

        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(0 0, 5 0)")),
                    ValueFactory.createValue(1),
                    ValueFactory.createValue(2),
                    ValueFactory.createValue(1)
                });
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(5 0, 7 0)")),
                    ValueFactory.createValue(3),
                    ValueFactory.createValue(2),
                    ValueFactory.createValue(1)
                });
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(7 0 , 10 0)")),
                    ValueFactory.createValue(4),
                    ValueFactory.createValue(3),
                    ValueFactory.createValue(1)
                });
        mdsd.addValues(
                new Value[]{
                    ValueFactory.createValue(wktReader.read("LINESTRING(10 0  , 20 0)")),
                    ValueFactory.createValue(5),
                    ValueFactory.createValue(4),
                    ValueFactory.createValue(1)
                });

        DataSet[] tables = new DataSet[]{mdsd};
        int source = 2;
        ST_FindReachableEdges stspl = new ST_FindReachableEdges();
        DataSet result = stspl.evaluate(
                dsf,
                tables,
                new Value[]{
                    ValueFactory.createValue(source),
                    ValueFactory.createValue("weigth"),
                    ValueFactory.createValue(3)
                },
                new NullProgressMonitor());
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
    public void testST_SubGraphStatistics() throws Exception {
        ST_SubGraphStatistics sT_SubGraphStatistics = new ST_SubGraphStatistics();
        DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
        ds.open();
        DataSet[] tables = new DataSet[]{ds};
        DataSet result = sT_SubGraphStatistics.evaluate(
                dsf,
                tables,
                new Value[]{
                    ValueFactory.createValue("length")
                },
                new NullProgressMonitor());
        ds.close();
        System.out.println(result.getRowCount());

    }

    @Test
    public void testMySubGraph() {
    }
}
