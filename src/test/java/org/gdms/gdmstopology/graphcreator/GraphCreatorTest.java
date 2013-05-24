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
package org.gdms.gdmstopology.graphcreator;

import com.vividsolutions.jts.io.ParseException;
import java.util.HashSet;
import java.util.Set;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.gdmstopology.TopologySetupTest;
import org.gdms.gdmstopology.function.ST_Graph;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.sql.function.FunctionException;
import org.javanetworkanalyzer.data.VBetw;
import org.javanetworkanalyzer.data.VUBetw;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.jgrapht.Graphs;
import org.junit.Test;
import org.orbisgis.progress.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 * Tests the graph creators.
 *
 * @author Adam Gouge
 */
public class GraphCreatorTest extends TopologySetupTest {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GraphCreatorTest.class);

    @Test
    public void unweightedUndirected() throws Exception {

        DataSource edges = prepareEdges();

        // Unweighted undirected graph
        KeyedGraph<VUBetw, Edge> graph =
                new GraphCreator<VUBetw, Edge>(edges,
                                               GraphSchema.UNDIRECT,
                                               VUBetw.class,
                                               Edge.class).prepareGraph();

        assertTrue(graph.vertexSet().size() == 4);

        assertTrue(graph.edgeSet().size() == 3);
        assertTrue(graph.containsEdge(graph.getVertex(1), graph.getVertex(2)));
        assertTrue(graph.containsEdge(graph.getVertex(2), graph.getVertex(1)));
        assertTrue(graph.containsEdge(graph.getVertex(2), graph.getVertex(3)));
        assertTrue(graph.containsEdge(graph.getVertex(3), graph.getVertex(2)));
        assertTrue(graph.containsEdge(graph.getVertex(3), graph.getVertex(4)));
        assertTrue(graph.containsEdge(graph.getVertex(4), graph.getVertex(3)));

        print(graph);
        edges.close();
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
        String metadata = "";
        for (String name : table.getMetadata().getFieldNames()) {
            metadata += name + "\t";
        }
        LOGGER.info(metadata);
        for (int i = 0; i < table.getRowCount(); i++) {
            String row = "";
            for (Value v : table.getRow(i)) {
                row += v + "\t";
            }
            LOGGER.info(row);
        }
    }

    /**
     * Prints all edges of the graph.
     *
     * @param graph The graph.
     */
    private void print(KeyedGraph<? extends VBetw, Edge> graph) {
        LOGGER.info("\tGRAPH");
        for (Edge edge : graph.edgeSet()) {
            LOGGER.info("{} --> {} ({})",
                        graph.getEdgeSource(edge).getID(),
                        graph.getEdgeTarget(edge).getID(),
                        graph.getEdgeWeight(edge));
        }
    }

    private DataSource prepareEdges() throws FunctionException, DriverException,
            DataSourceCreationException, NoSuchTableException, ParseException {
        MemoryDataSetDriver data = initializeDriver();
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(1 1, 2 2)")),
            ValueFactory.createValue(1)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(2 2, 2 3)")),
            ValueFactory.createValue(2)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(2 3, 3 3)")),
            ValueFactory.createValue(3)});

        DataSet[] tables = new DataSet[]{data};

        LOGGER.info("\tDATA");
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
        LOGGER.info("\tNODES");
        print(nodes);
        nodes.close();

        // Check the edges table.
        DataSource edges = dsf.getDataSource("output.edges");
        edges.open();
        LOGGER.info("\tEDGES");
        print(edges);

        return edges;
    }
}
