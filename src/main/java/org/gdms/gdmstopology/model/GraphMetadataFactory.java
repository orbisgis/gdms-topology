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

import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Constraint;
import org.gdms.data.types.GeometryDimensionConstraint;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.driver.DriverException;

/**
 * Contains methods used to create the metadata models used by various GDMS
 * Topology functions.
 *
 * <p> The constructor is private since every method in this class is static.
 *
 * <p> October 11, 2012: Documentation updated by Adam Gouge.
 *
 * @author Erwan Bocher
 */
public final class GraphMetadataFactory {

    /**
     * Empty constructor.
     */
    private GraphMetadataFactory() {
    }

    /**
     * Creates the metadata model for the datasource containing the nodes of the
     * graph.
     *
     * <p> Format: {@code [the_geom (POINT), ID (INT)]}.
     *
     * @return The metadata model for the graph nodes datasource.
     */
    public static Metadata createNodesMetadata() {
        return new DefaultMetadata(
                new Type[]{
            TypeFactory.createType(Type.POINT),
            TypeFactory.createType(Type.INT)},
                new String[]{
            "the_geom",
            GraphSchema.ID});
    }

    /**
     * Creates the edge metadata by appending id, start node and end node
     * columns to the original metadata.
     *
     * @param originalMD Original metadata
     *
     * @return Metadata for edges
     *
     * @throws DriverException
     */
    public static DefaultMetadata createEdgeMetadata(Metadata originalMD)
            throws DriverException {
        DefaultMetadata edgeMedata = new DefaultMetadata(originalMD);
        edgeMedata.addField(GraphSchema.ID, TypeFactory.createType(Type.INT));
        edgeMedata.addField(GraphSchema.START_NODE,
                            TypeFactory.createType(Type.INT));
        edgeMedata.addField(GraphSchema.END_NODE,
                            TypeFactory.createType(Type.INT));
        return edgeMedata;
    }

    /**
     * Creates the metadata model used by the shortest path functions
     * {@link org.gdms.gdmstopology.function.ST_ShortestPath} and
     * {@link org.gdms.gdmstopology.function.ST_MShortestPath}.
     *
     * <p> Format: {@code [the_geom (CONSTRAINED GEOMETRY), ID (INT), PATH_ID
     * (INT), START_NODE (INT), END_NODE (INT), WEIGHT (DOUBLE)]}.
     *
     * @return The metadata model used by the shortest path functions.
     */
    public static Metadata createEdgeMetadataShortestPath() {
        Metadata md = new DefaultMetadata(
                new Type[]{
            TypeFactory.createType(
            Type.GEOMETRY,
            new Constraint[]{
                new GeometryDimensionConstraint(
                GeometryDimensionConstraint.DIMENSION_CURVE)
            }),
            TypeFactory.createType(Type.INT),
            TypeFactory.createType(Type.INT),
            TypeFactory.createType(Type.INT),
            TypeFactory.createType(Type.INT),
            TypeFactory.createType(Type.DOUBLE)},
                new String[]{
            "the_geom",
            GraphSchema.ID,
            GraphSchema.PATH_ID,
            GraphSchema.START_NODE,
            GraphSchema.END_NODE,
            GraphSchema.WEIGHT});
        return md;
    }

    /**
     * Creates the metadata model used by the distance functions
     * {@link org.gdms.gdmstopology.function.ST_ShortestPathLength} and
     * {@link org.gdms.gdmstopology.function.ST_MShortestPathLength}.
     *
     * <p> Format: {@code [ID (INT), DISTANCE (DOUBLE)]}.
     *
     * @return The metadata model used by the distance functions.
     */
    public static Metadata createDistancesMetadata() {
        Metadata md = new DefaultMetadata(
                new Type[]{
            TypeFactory.createType(Type.INT),
            TypeFactory.createType(Type.DOUBLE)},
                new String[]{
            GraphSchema.ID,
            GraphSchema.DISTANCE});
        return md;
    }

    /**
     * Creates the metadata model used by the
     * {@link org.gdms.gdmstopology.function.ST_FindReachableEdges} function.
     *
     * <p> Format: {@code [the_geom (GEOMETRY), ID (INT), WEIGHT (DOUBLE), DISTANCE
     * (DOUBLE)]}.
     *
     * @return The metadata model used by the {@code ST_FindReachableEdges}
     *         function.
     */
    public static Metadata createReachableEdgesMetadata() {
        return new DefaultMetadata(
                new Type[]{
            TypeFactory.createType(Type.GEOMETRY),
            TypeFactory.createType(Type.INT),
            TypeFactory.createType(Type.DOUBLE),
            TypeFactory.createType(Type.DOUBLE)},
                new String[]{
            "the_geom",
            GraphSchema.ID,
            GraphSchema.WEIGHT,
            GraphSchema.DISTANCE});
    }

    /**
     * Creates the metadata model used by the
     * {@link org.gdms.gdmstopology.function.ST_MFindReachableEdges} function.
     *
     * <p> Format: {@code [the_geom (GEOMETRY), ID (INT), SOURCE_NODE (INT), WEIGHT
     * (DOUBLE), DISTANCE (DOUBLE)]}.
     *
     * @return The metadata model used by the {@code ST_MFindReachableEdges}
     *         function.
     */
    public static Metadata createMReachableEdgesMetadata() {
        return new DefaultMetadata(
                new Type[]{
            TypeFactory.createType(Type.GEOMETRY),
            TypeFactory.createType(Type.INT),
            TypeFactory.createType(Type.INT),
            TypeFactory.createType(Type.DOUBLE),
            TypeFactory.createType(Type.DOUBLE)},
                new String[]{
            "the_geom",
            GraphSchema.ID,
            GraphSchema.SOURCE_NODE,
            GraphSchema.WEIGHT,
            GraphSchema.DISTANCE});
    }

    /**
     * Creates the metadata model used by the
     * {@link org.gdms.gdmstopology.function.ST_SubGraphStatistics} function.
     *
     * <p> Format: {@code [ID (INT), count (INT), SUM (DOUBLE)]}.
     *
     * @return The metadata model used by the {@code ST_SubGraphStatistics}
     *         function.
     */
    public static Metadata createSubGraphStatsMetadata() {
        Metadata md = new DefaultMetadata(
                new Type[]{
            TypeFactory.createType(Type.INT),
            TypeFactory.createType(Type.INT),
            TypeFactory.createType(Type.DOUBLE)},
                new String[]{
            GraphSchema.ID,
            "count",
            GraphSchema.SUM});
        return md;
    }
}