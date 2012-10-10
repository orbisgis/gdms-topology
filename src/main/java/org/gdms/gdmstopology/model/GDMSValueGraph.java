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
import org.jgrapht.Graph;

/**
 * The root interface in the GDMS graph hierarchy.
 * 
 * <p> October 10, 2012: Documentation updated by Adam Gouge.
 * 
 * @author Erwan Bocher
 */
public interface GDMSValueGraph<V extends Integer, E extends GraphEdge> extends Graph<V, E> {

    /**
     * Returns the {@link Geometry} located at a given row.
     * 
     * @param rowid The row id.
     * @return The {@link Geometry} located at this row.
     * @throws DriverException 
     */
    Geometry getGeometry(int rowid) throws DriverException;

    /**
     * Returns the {@link Geometry} of a given {@link GraphEdge}.
     *
     * @param graphEdge The given {@link GraphEdge}.
     * @return The {@link Geometry} of this {@link GraphEdge}.
     * @throws DriverException
     */
    Geometry getGeometry(GraphEdge graphEdge) throws DriverException;

    /**
     * Returns the values of all fields at a given row.
     * 
     * @param rowid The row id.
     * @return The values of all fields at the specified row, in order.
     * @throws DriverException if the access fails.
     */
    Value[] getValues(int rowid) throws DriverException;

    /**
     * Returns the number of rows in the data set.
     * 
     * @return The number of rows in the data set.
     * @throws DriverException 
     */
    long getRowCount() throws DriverException;
}
