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

/**
 * <p>
 * This class defines several constants used to read the input fields of
 * the data source, as well as some constants to specify which type of 
 * graph we are dealing with.
 * </p>
 * 
 * <p>
 * The constructor is private since this class is only used to
 * access constants. 
 * </p>
 * 
 * <p>
 * Documentation added by Adam Gouge.
 * </p>
 * 
 * @author Erwan Bocher
 */
public final class GraphSchema {

    // Constants for reading input fields.
    /**
     * Specifies the start node field.
     */
    public static final String START_NODE = "start_node";
    /**
     * Specifies the end node field.
     */
    public static final String END_NODE = "end_node";
    /**
     * Specifies the id field.
     */
    public static final String ID = "id";
    /**
     * Specifies the weight field.
     */
    public static final String WEIGHT = "weight";
    /**
     * Specifies the field indicating which polygon is located to the 
     * left when the border of the current polygon is traversed 
     * clockwise.
     */
    public static final String LEFT_FACE = "left_polygon";
    /**
     * Specifies the field indicating which polygon is located to the 
     * right when the border of the current polygon is traversed 
     * clockwise.
     */
    public static final String RIGHT_FACE = "right_polygon";
    /**
     * Specifies the sum field.
     */
    public static final String SUM = "sum";
    /**
     * Specifies the path id field.
     */
    public static final String PATH_ID = "path_id";
    /**
     * Specifies the source node field.
     */
//    What is the difference between SOURCE_NODE and START_NODE ?
    public static final String SOURCE_NODE = "source";
    /**
     * Specifies the target node field.
     */
//    What is the difference between END_NODE and TARGET_NODE ?
    public static final String TARGET_NODE = "target";
    /**
     * Specifies the distance field.
     */
    public static final String DISTANCE = "distance";
    
    // Constants for specifying which type of graph we are dealing with.
    // Added the "final" keyword to make sure these constants 
    // are never changed. (Adam)
    /**
     * Specifies a directed graph.
     */
    public static final int DIRECT = 1;
    /**
     * Specifies a directed graph with all directions reversed.
     */
    public static final int DIRECT_REVERSED = 2;
    /**
     * Specifies an undirected graph.
     */
    public static final int UNDIRECT = 3;

    private GraphSchema() {
    }
}
