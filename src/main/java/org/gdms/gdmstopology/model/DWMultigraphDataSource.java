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
package org.gdms.gdmstopology.model;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Set;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.jgrapht.graph.DirectedWeightedMultigraph;
import org.orbisgis.progress.ProgressMonitor;

/**
 * A class to create a (JGraphT) directed weighted multigraph 
 * from a data source. A directed weighted multigraph is a
 * non-simple directed graph in which loops and multiple edges between any two
 * vertices are permitted, and edges have weights.
 * 
 * <p> Most operations are performed on a private {@link GDMSGraph}.
 *
 * <p> October 10, 2012: Documentation updated by Adam Gouge.
 *
 * @author Erwan Bocher
 */
public class DWMultigraphDataSource
        extends DirectedWeightedMultigraph<Integer, GraphEdge>
        implements GDMSValueGraph<Integer, GraphEdge> {

    /**
     * A {@link GDMSGraph} to be created from the data source.
     */
    private final GDMSGraph GDMSGraph;

    /**
     * Creates a {@link DirectedWeightedMultigraph} based on the 
     * {@link GraphEdge} class. Also creates a new {@link GDMSGraph} 
     * using the given data source. Most operations are performed on 
     * this private {@link GDMSGraph}.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param dataSet The data set.
     * @param pm The progress monitor used to track the progress of the index
     * initialization.
     * @throws DriverException
     */
    public DWMultigraphDataSource(DataSourceFactory dsf, DataSet dataSet, ProgressMonitor pm) throws DriverException {
        super(GraphEdge.class);
        GDMSGraph = new GDMSGraph(dsf, dataSet, pm);
    }

    /**
     * Sets the name of the field that contains the weight value 
     * in order to process the graph.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#setWeightFieldIndex(String) 
     * 
     * @param fieldName The name of the field that contains the weight value.
     * @throws DriverException 
     */
    public void setWeightFieldIndex(String fieldName) throws DriverException {
        GDMSGraph.setWeightFieldIndex(fieldName);
    }

    /**
     * Returns the {@link Set} of edges that end at a given
     * vertex.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#incomingEdgesOf(Integer)
     * 
     * @param vertex The vertex.
     * @return The set of incoming edges.
     */
    @Override
    public Set<GraphEdge> incomingEdgesOf(Integer vertex) {
        return GDMSGraph.incomingEdgesOf(vertex);
    }

    /**
     * Returns the {@link Set} of edges that start at a given
     * vertex.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#outgoingEdgesOf(Integer) 
     * 
     * @param vertex The vertex.
     * @return The set of outgoing edges.
     */
    @Override
    public Set<GraphEdge> outgoingEdgesOf(Integer vertex) {
        return GDMSGraph.outgoingEdgesOf(vertex);
    }

    /**
     * Returns a (new) {@link GraphEdge} object if an edge from
     * a given start vertex to a given end vertex exists
     * in the data set.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#getEdge(Integer, Integer) 
     * 
     * @param startVertex The start vertex.
     * @param endVertex The end vertex.
     * @return A (new) {@link GraphEdge} object from the given start vertex
     * to the given end vertex if such an edge exists in the data set; 
     * {@code null} otherwise.
     */
    @Override
    public GraphEdge getEdge(Integer startVertex, Integer endVertex) {
        return GDMSGraph.getEdge(startVertex, endVertex);
    }

    /**
     * Returns {@code true} if the data set contains a given vertex.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#containsVertex(Integer) 
     * 
     * @param vertex The vertex.
     * @return {@code true} if and only if the vertex is contained in 
     * the data set.
     */
    @Override
    public boolean containsVertex(Integer vertex) {
        return GDMSGraph.containsVertex(vertex);
    }

    /**
     * Returns {@code true} if the data set contains a given 
     * {@link GraphEdge}.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#containsEdge(org.gdms.gdmstopology.model.GraphEdge) 
     * 
     * @param graphEdge The {@link GraphEdge}.
     * @return {@code true} if and only if the {@link GraphEdge} is contained 
     * in the data set.
     */
    @Override
    public boolean containsEdge(GraphEdge graphEdge) {
        return GDMSGraph.containsEdge(graphEdge);
    }

    /**
     * Returns {@code true} if the data set contains an edge from a 
     * given start vertex to a given end vertex.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#containsEdge(Integer, Integer) 
     * 
     * @param startVertex The start vertex.
     * @param endVertex The end vertex.
     * @return {@code true} if and only if there exists an edge from the start 
     * vertex to the end vertex.
     */
    @Override
    public boolean containsEdge(Integer startVertex, Integer endVertex) {
        return GDMSGraph.containsEdge(startVertex, endVertex);
    }

    /**
     * Returns the {@link Set} of all edges touching the specified 
     * vertex, or the empty set if no edges touch the vertex.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#edgesOf(Integer) 
     * 
     * @param vertex The vertex to be examined.
     * @return The edges touching this vertex, or the empty set if no edges
     * touch this vertex.
     */
    @Override
    public Set edgesOf(Integer vertex) {
        return GDMSGraph.edgesOf(vertex);
    }

    /**
     * Returns the source vertex of a given {@link GraphEdge}.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#getEdgeSource(org.gdms.gdmstopology.model.GraphEdge) 
     * 
     * @param graphEdge The {@link GraphEdge}.
     * @return The source vertex.
     */
    @Override
    public Integer getEdgeSource(GraphEdge graphEdge) {
        return GDMSGraph.getEdgeSource(graphEdge);
    }

    /**
     * Returns the target vertex of a given {@link GraphEdge}.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#getEdgeTarget(org.gdms.gdmstopology.model.GraphEdge) 
     * 
     * @param graphEdge The {@link GraphEdge}.
     * @return The target vertex.
     */
    @Override
    public Integer getEdgeTarget(GraphEdge graphEdge) {
        return GDMSGraph.getEdgeTarget(graphEdge);
    }

    /**
     * Returns the weight of a given {@link GraphEdge}.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#getEdgeWeight(org.gdms.gdmstopology.model.GraphEdge) 
     * 
     * @param graphEdge The {@link GraphEdge}.
     * @return The weight.
     */
    @Override
    public double getEdgeWeight(GraphEdge graphEdge) {
        return GDMSGraph.getEdgeWeight(graphEdge);
    }

    /**
     * Returns the indegree of a vertex (the number of head endpoints 
     * adjacent to the vertex).
     *
     * @see org.gdms.gdmstopology.model.GDMSGraph#inDegreeOf(Integer) 
     * 
     * @param vertex The vertex.
     * @return The indegree of the vertex.
     */
    @Override
    public int inDegreeOf(Integer vertex) {
        return GDMSGraph.inDegreeOf(vertex);
    }

     /**
     * Returns the outdegree of a vertex (the number of tail endpoints 
     * adjacent to the vertex).
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#outDegreeOf(Integer) 
     * 
     * @param vertex The vertex.
     * @return The outdegree of the vertex.
     */
    @Override
    public int outDegreeOf(Integer vertex) {
        return GDMSGraph.outDegreeOf(vertex);
    }

    /**
     * Returns the degree of a vertex (the sum of the indegree
     * and the outdegree of the vertex).
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#degreeOf(Integer) 
     * 
     * @param vertex The vertex.
     * @return The degree of the vertex.
     */
    @Override
    public int degreeOf(Integer vertex) {
        return GDMSGraph.degreeOf(vertex);
    }

    /**
     * Checks to see if the vertex set of this graph is {@code null}:
     * if so, recovers all source and target vertices into the vertex 
     * set and returns the vertex set; if not, returns the vertex set 
     * directly.
     * 
     * @see org.gdms.gdmstopology.model.GDMSGraph#vertexSet() 
     *
     * @return The {@link Set} of vertices located in the data source.
     */
    // Why does this method return Set<Integer> and not HashSet<Integer>?
    @Override
    public Set<Integer> vertexSet() {
        return GDMSGraph.vertexSet();
    }

    /**
     * Returns the private {@link GDMSGraph} on which most
     * operations are performed.
     * 
     * @return The private {@link GDMSGraph}.
     */
    public org.gdms.gdmstopology.model.GDMSGraph getGDMSGraph() {
        return GDMSGraph;
    }

    /**
     * @see org.gdms.gdmstopology.model.GDMSGraph#getGeometry(GraphEdge) 
     */
    // Javadoc will be copied from the GDMSValueGraph interface.
    @Override
    public Geometry getGeometry(GraphEdge graphEdge) throws DriverException {
        return GDMSGraph.getGeometry(graphEdge);
    }

    /**
     * @see org.gdms.gdmstopology.model.GDMSGraph#getGeometry(int) 
     */
    // Javadoc will be copied from the GDMSValueGraph interface.
    @Override
    public Geometry getGeometry(int rowid) throws DriverException {
        return GDMSGraph.getGeometry(rowid);
    }

    /**
     * @see org.gdms.gdmstopology.model.GDMSGraph#getValues(int) 
     */
    // Javadoc will be copied from the GDMSValueGraph interface.
    @Override
    public Value[] getValues(int rowid) throws DriverException {
        return GDMSGraph.getValues(rowid);
    }

    /**
     * @see org.gdms.gdmstopology.model.GDMSGraph#getRowCount() 
     */
    // Javadoc will be copied from the GDMSValueGraph interface.
    @Override
    public long getRowCount() throws DriverException {
        return GDMSGraph.getRowCount();
    }
}
