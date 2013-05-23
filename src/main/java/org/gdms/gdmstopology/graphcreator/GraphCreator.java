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

import org.gdms.data.indexes.IndexException;
import org.gdms.data.schema.Metadata;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.model.GraphSchema;
import org.javanetworkanalyzer.data.VId;
import static org.javanetworkanalyzer.graphcreators.GraphCreator.REVERSED;
import static org.javanetworkanalyzer.graphcreators.GraphCreator.UNDIRECTED;
import org.javanetworkanalyzer.model.DirectedPseudoG;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.javanetworkanalyzer.model.PseudoG;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Creates a graph with a specified orientation from the given {@link DataSet}.
 *
 * @author Adam Gouge
 */
public class GraphCreator<V extends VId, E extends Edge> {

    /**
     * The data set.
     */
    protected final DataSet dataSet;
    /**
     * Orientation.
     */
    protected final int orientation;
    /**
     * Vertex class used for initializing the graph.
     */
    protected final Class<? extends V> vertexClass;
    /**
     * Edge class used for initializing the graph.
     */
    protected final Class<? extends E> edgeClass;
    // Initialize all the indices to -1.
    protected int startNodeIndex = -1;
    protected int endNodeIndex = -1;
    /**
     * An error message given when a user inputs an erroneous graph orientation.
     */
    public final static String GRAPH_TYPE_ERROR =
            "Please enter an appropriate graph orientation (1, 2 or 3).";
    /**
     * An error message given when there is a problem accessing the edge
     * metadata or recovering the start node, end node or weight field indices.
     */
    public final static String METADATA_ERROR =
            "Cannot load the edge metadata OR Cannot recover node "
            + "or weight field indices.";
    /**
     * An error message given when the edges cannot be loaded.
     */
    public final static String EDGE_LOADING_ERROR =
            "Cannot load the edges.";
    /**
     * An error message given when a field is missing from the input table.
     */
    public final static String MISSING_FIELD_ERROR =
            "The input table must contain the field \'";
    /**
     * Used to allocate enough space for the GraphHopper graph.
     */
    // TODO: How big does this need to be?
    protected final static int ALLOCATE_GRAPH_SPACE = 10;
    /**
     * A logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(GraphCreator.class);

    /**
     * Constructs a new {@link GraphCreator}.
     *
     * @param dataSet The data set.
     *
     */
    public GraphCreator(DataSet dataSet,
                        int orientation,
                        Class<? extends V> vertexClass,
                        Class<? extends E> edgeClass) {
        this.dataSet = dataSet;
        this.orientation = orientation;
        this.vertexClass = vertexClass;
        this.edgeClass = edgeClass;
    }

    /**
     * Prepares a graph.
     *
     * @return The newly prepared graph.
     *
     * @throws IndexException
     */
    public KeyedGraph<V, E> prepareGraph() {
        // Initialize the indices.
        initializeIndices();
        // Create the graph.
        KeyedGraph<V, E> graph = initializeGraph();
        if (graph != null) {
            // Add the edges according to the given graph type.
            loadEdges(graph);
        } else {
            throw new IllegalStateException(
                    "Couldn't load edges since the graph was null.");
        }
        return graph;
    }

    /**
     * Initializes a graph.
     *
     * @return The newly initialized graph
     *
     * @throws NoSuchMethodException If the vertex class does not have a
     *                               constructor with just an Integer parameter.
     */
    protected KeyedGraph<V, E> initializeGraph() {
        KeyedGraph<V, E> graph;
        if (orientation != UNDIRECTED) {
            // Unweighted Directed or Reversed
            graph = new DirectedPseudoG<V, E>(vertexClass, edgeClass);
        } else {
            // Unweighted Undirected
            graph = new PseudoG<V, E>(vertexClass, edgeClass);
        }
        return graph;
    }

    /**
     * Loads the graph edges with the appropriate orientation.
     *
     * @param graph            The graph.
     * @param orientation      The orientation.
     * @param startNodeIndex   The start node index.
     * @param endNodeIndex     The end node index.
     * @param weightFieldIndex The weight field index.
     *
     * @throws GraphException
     */
    private KeyedGraph<V, E> loadEdges(KeyedGraph<V, E> graph) {
        // Should we reverse the edge orientation?
        boolean reverse = (orientation == REVERSED) ? true : false;
        for (Value[] row : dataSet) {
            loadEdge(row, graph, reverse);
        }
        return graph;
    }

    /**
     * Loads an edge into the graph.
     *
     * @param row     The row from which to load the edge.
     * @param graph   The graph to which the edges will be added.
     * @param reverse {@code true} iff the edge orientation should be reversed.
     *
     * @return The newly loaded edge.
     */
    protected E loadEdge(Value[] row,
                         KeyedGraph<V, E> graph,
                         boolean reverse) {
        // Add the edge to the graph.
        if (reverse) {
            return graph.addEdge(row[endNodeIndex].getAsInt(),
                                 row[startNodeIndex].getAsInt());
        } else {
            return graph.addEdge(row[startNodeIndex].getAsInt(),
                                 row[endNodeIndex].getAsInt());
        }
    }

    /**
     * Verifies that the given index is not equal to -1; if it is, then throws
     * an exception saying that the given field is missing.
     *
     * @param index        The index.
     * @param missingField The field.
     *
     * @throws FunctionException
     */
    protected void verifyIndex(int index, String missingField) throws
            IndexException {
        if (index == -1) {
            throw new IndexException(
                    MISSING_FIELD_ERROR + missingField + "\'.");
        }
    }

    /**
     * Recovers the indices from the metadata.
     *
     * @param weightColumnName
     */
    protected Metadata initializeIndices() {
        Metadata md = null;
        try {
            // Recover the edge Metadata.
            // TODO: Add a check to make sure the metadata was loaded correctly.
            md = dataSet.getMetadata();

            // Recover the indices of the start node and end node.
            startNodeIndex = md.getFieldIndex(GraphSchema.START_NODE);
            verifyIndex(startNodeIndex, GraphSchema.START_NODE);
            endNodeIndex = md.getFieldIndex(GraphSchema.END_NODE);
            verifyIndex(endNodeIndex, GraphSchema.END_NODE);
        } catch (DriverException ex) {
            LOGGER.error(METADATA_ERROR, ex);
        } catch (IndexException ex) {
            LOGGER.error("Problem with indices.", ex);
        }
        return md;
    }
}
