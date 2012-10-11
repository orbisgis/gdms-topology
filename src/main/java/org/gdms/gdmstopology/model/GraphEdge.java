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

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * A default implementation for edges in a GDMS graph.
 *
 * <p> October 10, 2012: Documentation updated by Adam Gouge.
 *
 * @author Erwan Bocher
 */
public class GraphEdge extends DefaultWeightedEdge {

    private static final long serialVersionUID = -1862257432554700123L;
    /**
     * The source vertex.
     */
    private Integer source;
    /**
     * The target vertex.
     */
    private Integer target;
    /**
     * The default weight of the edge is 1, but a different weight may be
     * assigned in the constructor.
     */
    private double weight = 1;
    /**
     * The row id of the edge in the data source.
     */
    private long rowId = -1;

    /**
     * Constructs a new {@link GraphEdge} object, specifying the source and
     * target vertices as well as the weight of the edge and its row id in the
     * data source.
     *
     * @param sourceVertex The source vertex.
     * @param targetVertex The target vertex.
     * @param weight The weight.
     * @param rowId The row id.
     */
    public GraphEdge(Integer sourceVertex, Integer targetVertex, double weight, long rowId) {
        this.source = sourceVertex;
        this.target = targetVertex;
        this.weight = weight;
        this.rowId = rowId;
    }

    /**
     * Returns the weight of this edge.
     *
     * @see org.jgrapht.graph.DefaultWeightedEdge#getWeight()
     *
     * @return The weight of this edge.
     */
    @Override
    public double getWeight() {
        return weight;
    }

    /**
     * Returns the source vertex.
     *
     * @see org.jgrapht.graph.DefaultEdge#getSource()
     *
     * @return The source vertex.
     */
    @Override
    public Integer getSource() {
        return source;
    }

    /**
     * Returns the target vertex.
     *
     * @see org.jgrapht.graph.DefaultEdge#getTarget()
     *
     * @return The target vertex.
     */
    @Override
    public Integer getTarget() {
        return target;
    }

    /**
     * Returns the row id of the edge in the data source.
     *
     * @return The row id of the edge in the data source.
     */
    public long getRowId() {
        return rowId;
    }

    /**
     * Returns a {@link String} representation of the edge in the format
     * (source:target:weight).
     *
     * @return A {@link String} representation of the edge.
     */
    @Override
    public String toString() {
        return "(" + source + " : " + target + " : " + weight + ")";
    }
}
