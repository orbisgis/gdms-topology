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

import java.util.Iterator;
import org.gdms.driver.DriverException;

/**
 * An {@link Iterator} on the {@link GraphEdge}s of a GDMS graph.
 *
 * <p> October 11, 2012: Documentation added by Adam Gouge.
 *
 * @author Erwan Bocher
 */
public class GraphEdgeIterator implements Iterator<GraphEdge> {

    /**
     * Records the number of rows in the data source.
     */
    private final long count;
    /**
     * Keeps track of the current {@link GraphEdge} in the data source.
     */
    private long index = 0;
    /**
     * A {@link GDMSGraph} to be recovered via the constructor.
     */
    private final GDMSGraph gdmsGraph;

    /**
     * Constructs a {@link GraphEdgeIterator} by assigning the given
     * {@link GDMSGraph} to a private final variable and initializing a private
     * final variable to record the number of rows.
     *
     * @param gdmsGraph The {@link GDMSGraph}.
     * @throws DriverException
     */
    public GraphEdgeIterator(GDMSGraph gdmsGraph) throws DriverException {
        if (gdmsGraph == null) {
            throw new NullPointerException("The graph cannot be null!");
        }
        this.gdmsGraph = gdmsGraph;
        count = gdmsGraph.getRowCount();
    }

    /**
     * Returns {@code true} if the iteration has more elements. (In other words,
     * returns {@code true} if {@link GraphEdgeIterator#next()} would return an
     * element rather than throwing an exception.)
     *
     * @return {@code true} if the iteration has more elements.
     */
    @Override
    public boolean hasNext() {
        return index < count;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return The next element in the iteration.
     * @throws {@link IllegalStateException} if the iteration has no more
     * elements.
     */
    @Override
    public GraphEdge next() {
        GraphEdge graphEdge;
        try {
            graphEdge = gdmsGraph.getGraphEdge(index);
        } catch (DriverException ex) {
            throw new IllegalStateException(ex);
        }
        index++;
        return graphEdge;
    }

    /**
     * NOT SUPPORTED.
     *
     * @throws {@link UnsupportedOperationException}.
     */
    // TODO: Implement?
    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }
}
