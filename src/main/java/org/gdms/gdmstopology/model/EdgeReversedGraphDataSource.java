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
import org.gdms.data.values.Value;
import org.gdms.driver.DriverException;
import org.jgrapht.graph.EdgeReversedGraph;

/**
 * A class to create an edge-reversed (JGraphT) directed weighted 
 * multigraph from a {@link DWMultigraphDataSource}. The vertex sets 
 * for the two graphs are the same, but g' contains an edge (v2, v1) iff 
 * g contains an edge (v1, v2).
 *
 * <p> October 10, 2012: Documentation updated by Adam Gouge.
 *
 * @author Erwan Bocher
 */
public class EdgeReversedGraphDataSource
        extends EdgeReversedGraph<Integer, GraphEdge>
        implements GDMSValueGraph<Integer, GraphEdge> {

    /**
     * The {@link DWMultigraphDataSource} 
     * whose edges are to be reversed.
     */
    private DWMultigraphDataSource dWMultigraphDataSource;

    /**
     * Creates a new {@link EdgeReversedGraphDataSource} by reversing 
     * the edges of a given {@link DWMultigraphDataSource}.
     *
     * @param dWMultigraphDataSource The {@link DWMultigraphDataSource} to whose
     * edges are to be reversed.
     */
    public EdgeReversedGraphDataSource(DWMultigraphDataSource dWMultigraphDataSource) {
        super(dWMultigraphDataSource);
        this.dWMultigraphDataSource = dWMultigraphDataSource;
    }

    /**
     * @see org.gdms.gdmstopology.model.DWMultigraphDataSource#getGeometry(int) 
     */
    // Javadoc will be copied from the GDMSValueGraph interface.
    @Override
    public Geometry getGeometry(int rowid) throws DriverException {
        return dWMultigraphDataSource.getGeometry(rowid);
    }

    /**
     * @see org.gdms.gdmstopology.model.DWMultigraphDataSource#getGeometry(GraphEdge) 
     */
    // Javadoc will be copied from the GDMSValueGraph interface.
    @Override
    public Geometry getGeometry(GraphEdge graphEdge) throws DriverException {
        return dWMultigraphDataSource.getGeometry(graphEdge);
    }

    /**
     * @see org.gdms.gdmstopology.model.DWMultigraphDataSource#getValues(int) 
     */
    // Javadoc will be copied from the GDMSValueGraph interface.
    @Override
    public Value[] getValues(int rowid) throws DriverException {
        return dWMultigraphDataSource.getValues(rowid);
    }

    /**
     * @see org.gdms.gdmstopology.model.DWMultigraphDataSource#getRowCount() 
     */
    // Javadoc will be copied from the GDMSValueGraph interface.
    @Override
    public long getRowCount() throws DriverException {
        return dWMultigraphDataSource.getRowCount();
    }
}
