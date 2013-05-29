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
import org.gdms.driver.DriverException;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.gdmstopology.TopologySetupTest;
import org.gdms.gdmstopology.graphcreator.GraphCreator;
import org.gdms.gdmstopology.model.GraphSchema;
import org.junit.Test;
import org.orbisgis.progress.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 * Tests the calculation of strongly connected components on a <b>directed</b>
 * graph.
 *
 * @author Adam Gouge
 */
public class ST_StronglyConnectedComponentsTest extends TopologySetupTest {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(ST_StronglyConnectedComponentsTest.class);

    @Test
    public void test() throws Exception {
        DataSet[] tables = new DataSet[]{prepareEdges()};
        DataSet result = new ST_StronglyConnectedComponents()
                .evaluate(dsf,
                          tables,
                          new Value[]{
            ValueFactory.createValue(ST_ShortestPathLength.DIRECTED
                                     + ST_ShortestPathLength.SEPARATOR
                                     + GraphSchema.EDGE_ORIENTATION)},
                          new NullProgressMonitor());
        print(result);

        for (int i = 0; i < result.getRowCount(); i++) {
            Value[] row = result.getRow(i);
            int node = row[0].getAsInt();
            int component = row[1].getAsInt();
            if (node == 1 || node == 2 || node == 5) {
                assertTrue(component == 1);
            } else if (node == 3 || node == 4 || node == 8) {
                assertTrue(component == 2);
            } else if (node == 6 || node == 7) {
                assertTrue(component == 3);
            } else {
                LOGGER.error("Unexpected vertex {}", node);
            }
        }
    }

    private DataSet prepareEdges() throws Exception {
        //
        //     1--->2--->3<-->4
        //     ^  / |    |    ^
        //     | /  |    |    |
        //     |<   v    v    v
        //     5--->6<-->7<---8
        //
        MemoryDataSetDriver data = initializeDriver();
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(1 2, 2 2)")),
            ValueFactory.createValue(GraphCreator.DIRECTED_EDGE)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(2 2, 3 2)")),
            ValueFactory.createValue(GraphCreator.DIRECTED_EDGE)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(3 2, 4 2)")),
            ValueFactory.createValue(GraphCreator.UNDIRECTED_EDGE)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(1 2, 1 1)")),
            ValueFactory.createValue(GraphCreator.REVERSED_EDGE)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(2 2, 2 1)")),
            ValueFactory.createValue(GraphCreator.DIRECTED_EDGE)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(3 2, 3 1)")),
            ValueFactory.createValue(GraphCreator.DIRECTED_EDGE)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(4 2, 4 1)")),
            ValueFactory.createValue(GraphCreator.UNDIRECTED_EDGE)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(1 1, 2 1)")),
            ValueFactory.createValue(GraphCreator.DIRECTED_EDGE)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(2 1, 3 1)")),
            ValueFactory.createValue(GraphCreator.UNDIRECTED_EDGE)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(3 1, 4 1)")),
            ValueFactory.createValue(GraphCreator.REVERSED_EDGE)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(2 2, 1 1)")),
            ValueFactory.createValue(GraphCreator.DIRECTED_EDGE)});

        DataSet[] tables = new DataSet[]{data};

        LOGGER.debug("\tDATA");
        print(data);

        // Evaluate ST_Graph.
        new ST_Graph().evaluate(dsf,
                                tables,
                                // Tolerance, orient by elevation, output
                                new Value[]{ValueFactory.createValue(0),
                                            ValueFactory.createValue(false),
                                            ValueFactory.createValue("output")},
                                new NullProgressMonitor());

        // Check the nodes table.
        DataSource nodes = dsf.getDataSource("output.nodes");
        nodes.open();
        LOGGER.debug("\tNODES");
        print(nodes);
        nodes.close();

        // Check the edges table.
        DataSource edges = dsf.getDataSource("output.edges");
        edges.open();
        LOGGER.debug("\tEDGES");
        print(edges);

        return edges;
    }

    /**
     * Prints the given table for debugging.
     *
     * @param table The table
     *
     * @throws DriverException
     */
    private void print(DataSet table) throws DriverException {
        String metadata = "";
        for (String name : table.getMetadata().getFieldNames()) {
            metadata += name + "\t";
        }
        LOGGER.debug(metadata);
        for (int i = 0; i < table.getRowCount(); i++) {
            String row = "";
            for (Value v : table.getRow(i)) {
                row += v + "\t";
            }
            LOGGER.debug(row);
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
                new String[]{"the_geom", GraphSchema.EDGE_ORIENTATION},
                new Type[]{TypeFactory.createType(Type.GEOMETRY),
                           TypeFactory.createType(Type.INT)});
        return data;
    }
}
