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

import java.io.FileNotFoundException;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.NoSuchTableException;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.TopologySetupTest;
import org.javanetworkanalyzer.data.VCent;
import org.javanetworkanalyzer.data.VUCent;
import org.javanetworkanalyzer.data.VWCent;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import static org.javanetworkanalyzer.graphcreators.GraphCreator.REVERSED;
import static org.javanetworkanalyzer.graphcreators.GraphCreator.UNDIRECTED;
import static org.javanetworkanalyzer.graphcreators.GraphCreator.DIRECTED;
import org.javanetworkanalyzer.model.UndirectedG;
import org.junit.Test;

/**
 * Tests {@link GraphCreator}.
 *
 * @author Adam Gouge
 */
public class Graph2DGraphCreatorTest extends TopologySetupTest {

    private static final String WEIGHT = "length";
    /**
     * A switch to control whether or not we print out all edges when running
     * tests.
     */
    private static final boolean VERBOSE = false;

    @Test
    public void unweightedDirected() {
        try {
            System.out.println("\n***** 2D Unweighted Directed *****");
            KeyedGraph<? extends VCent, Edge> graph =
                    load2DGraph(false, DIRECTED);
            printEdges(graph);
        } catch (Exception ex) {
        }
    }

    @Test
    public void unweightedReversed() {
        try {
            System.out.println("***** 2D Unweighted Reversed *****");
            KeyedGraph<? extends VCent, Edge> graph =
                    load2DGraph(false, REVERSED);
            printEdges(graph);
        } catch (Exception ex) {
        }
    }

    @Test
    public void unweightedUndirected() {
        try {
            System.out.println("***** 2D Unweighted Undirected *****");
            KeyedGraph<? extends VCent, Edge> graph =
                    load2DGraph(false, UNDIRECTED);
            printEdges(graph);
        } catch (Exception ex) {
        }
    }

    @Test
    public void weightedDirected() {
        try {
            System.out.println("***** 2D Weighted Directed *****");
            KeyedGraph<? extends VCent, Edge> graph =
                    load2DGraph(true, DIRECTED);
            printEdges(graph);
        } catch (Exception ex) {
        }
    }

    @Test
    public void weightedReversed() {
        try {
            System.out.println("***** 2D Weighted Reversed *****");
            KeyedGraph<? extends VCent, Edge> graph =
                    load2DGraph(true, REVERSED);
            printEdges(graph);
        } catch (Exception ex) {
        }
    }

    @Test
    public void weightedUndirected() {
        try {
            System.out.println("***** 2D Weighted Undirected *****");
            KeyedGraph<? extends VCent, Edge> graph =
                    load2DGraph(true, UNDIRECTED);
            printEdges(graph);
        } catch (Exception ex) {
        }
    }

    /**
     * Loads the 2D graph according to whether it is to be considered weighted
     * and according to the given globalOrientation.
     *
     * @param weighted    {@code true} iff the graph is to be considered
     *                    weighted.
     * @param globalOrientation The globalOrientation.
     *
     * @return The graph.
     *
     * @throws FileNotFoundException
     */
    private KeyedGraph<? extends VCent, Edge> load2DGraph(
            boolean weighted,
            int orientation) throws FileNotFoundException,
            NoSuchMethodException,
            NoSuchTableException,
            DataSourceCreationException,
            DriverException {
        DataSource ds = get2DGraphDataSource();
        if (weighted) {
            return new WeightedGraphCreator<VWCent, Edge>(
                    ds,
                    orientation,
                    VWCent.class,
                    Edge.class,
                    WEIGHT).prepareGraph();
        } else {
            return new GraphCreator<VUCent, Edge>(
                    ds,
                    orientation,
                    VUCent.class,
                    Edge.class).prepareGraph();
        }
    }

    /**
     * Prints all edges of the graph.
     *
     * @param graph The graph.
     */
    private void printEdges(
            KeyedGraph<? extends VCent, Edge> graph) {
        if (VERBOSE) {
            for (Edge edge : graph.edgeSet()) {
                String edgeString = graph.getEdgeSource(edge).getID() + " ";
                if (graph instanceof UndirectedG) {
                    edgeString += "<";
                }
                edgeString += "--> " + graph.getEdgeTarget(edge).getID()
                        + " (" + graph.getEdgeWeight(edge) + ")";
                System.out.println(edgeString);
            }
            System.out.println("");
        }
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
    public DataSource get2DGraphDataSource()
            throws NoSuchTableException,
            DataSourceCreationException,
            DriverException {
        DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
        ds.open();
        return ds;
    }
}
