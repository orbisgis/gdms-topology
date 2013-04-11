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

import com.graphhopper.sna.analyzers.GeneralizedGraphAnalyzer;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.indexes.IndexException;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.TopologySetupTest;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.sql.function.FunctionException;
import org.junit.Test;

/**
 * Tests {@link GraphCreator}.
 *
 * @author Adam Gouge
 */
public abstract class GraphCreatorTest extends TopologySetupTest {

    /**
     * A switch to control whether or not we print out all edges when running
     * tests.
     */
    private static final boolean VERBOSE = false;

    /**
     * Instantiates an appropriate {@link GraphCreator} and prepares the graph.
     *
     * @param ds               The data source.
     * @param orientation      The orientation.
     * @param weightColumnName The weight column name.
     *
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     * @throws DriverException
     * @throws FunctionException
     */
    public void testGraphCreator(DataSource ds, int orientation,
                                 String weightColumnName)
            throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException,
            FunctionException,
            IndexException {

        GraphCreator creator = (weightColumnName == null)
                ? new UnweightedGraphCreator(ds, orientation)
                : new WeightedGraphCreator(ds, orientation, weightColumnName);
        Graph graph = creator.prepareGraph();

        if (VERBOSE) {
            System.out.println("\n Orientation: " + orientation);
            printAllEdges(graph);
        }
    }

    /**
     * Prints all edges of the given graph.
     *
     * @param graph
     */
    private void printAllEdges(Graph graph) {
        for (int i = 0; i < graph.nodes(); i++) {
            for (EdgeIterator incoming =
                    GeneralizedGraphAnalyzer.outgoingEdges(graph, i);
                    incoming.next();) {
                System.out.print(i + " <- " + incoming.adjNode()
                        + " (" + incoming.distance() + "); ");
            }
            for (EdgeIterator outgoing =
                    GeneralizedGraphAnalyzer.outgoingEdges(graph, i);
                    outgoing.next();) {
                System.out.print(i + " -> " + outgoing.adjNode()
                        + " (" + outgoing.distance() + "); ");
            }
            System.out.println("");
        }
    }

    /**
     * Tests creating a directed graph.
     *
     * @param ds               The data source.
     * @param weightColumnName The weight column name.
     *
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     * @throws DriverException
     * @throws FunctionException
     */
    public void testGraphCreatorDirected(DataSource ds,
                                         String weightColumnName) throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException,
            FunctionException,
            IndexException {
        testGraphCreator(ds, GraphSchema.DIRECT, weightColumnName);
    }

    /**
     * Tests creating a directed graph with edges reversed.
     *
     * @param ds               The data source.
     * @param weightColumnName The weight column name.
     *
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     * @throws DriverException
     * @throws FunctionException
     */
    public void testGraphCreatorReversed(DataSource ds,
                                         String weightColumnName) throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException,
            FunctionException,
            IndexException {
        testGraphCreator(ds, GraphSchema.DIRECT_REVERSED, weightColumnName);
    }

    /**
     * Tests creating an undirected graph.
     *
     * @param ds               The data source.
     * @param weightColumnName The weight column name.
     *
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     * @throws DriverException
     * @throws FunctionException
     */
    public void testGraphCreatorUndirected(DataSource ds,
                                           String weightColumnName) throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException,
            FunctionException,
            IndexException {
        testGraphCreator(ds, GraphSchema.UNDIRECT, weightColumnName);
    }

    /**
     * Gets the data source for the 2D graph.
     *
     * @return The data source for the 2D graph.
     *
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     * @throws DriverException
     */
    public DataSource get2DGraphDataSource() throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException {
        DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
        ds.open();
        return ds;
    }

    /**
     * Tests creating the 2D directed graph.
     *
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     * @throws DriverException
     * @throws FunctionException
     */
    @Test
    public abstract void test2DGraphDirected() throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException,
            FunctionException,
            IndexException;

    /**
     * Tests creating the 2D directed graph with edges reversed.
     *
     *
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     * @throws DriverException
     * @throws FunctionException
     */
    @Test
    public abstract void test2DGraphReversed() throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException,
            FunctionException,
            IndexException;

    /**
     * Tests creating the 2D undirected graph.
     *
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     * @throws DriverException
     * @throws FunctionException
     */
    @Test
    public abstract void test2DGraphUndirected() throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException,
            FunctionException,
            IndexException;
}
