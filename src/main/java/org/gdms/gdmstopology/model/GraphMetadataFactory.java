/**
 * GDMS-Topology  is a library dedicated to graph analysis. It is based on the JGraphT
 * library available at <http://www.jgrapht.org/>. It enables computing and processing
 * large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV institut as part of the
 * EvalPDU project, funded by the French Agence Nationale de la Recherche
 * (ANR) under contract ANR-08-VILL-0005-01 and GEBD project
 * funded by the French Ministery of Ecology and Sustainable Development.
 *
 * GDMS-Topology  is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 *
 * Copyright (C) 2009-2012 IRSTV (FR CNRS 2488)
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://wwwc.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.gdms.gdmstopology.model;

import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Constraint;
import org.gdms.data.types.GeometryDimensionConstraint;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;

/**
 *
 * @author Erwan Bocher
 */
public final class GraphMetadataFactory {

        private GraphMetadataFactory() {
        }

        /**
         * Create the metadata needs for the graph nodes datasource.
         * @return 
         */
        public static Metadata createNodesMetadataGraph() {
                return new DefaultMetadata(new Type[]{
                                TypeFactory.createType(Type.POINT),
                                TypeFactory.createType(Type.INT)}, new String[]{"the_geom",
                                GraphSchema.ID});
        }

        /**
         * 
         * @return 
         */
        public static Metadata createEdgeMetadataShortestPath() {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.GEOMETRY, new Constraint[]{new GeometryDimensionConstraint(GeometryDimensionConstraint.DIMENSION_CURVE)}),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{"the_geom", GraphSchema.ID, GraphSchema.PATH_ID, GraphSchema.START_NODE, GraphSchema.END_NODE, GraphSchema.WEIGHT});
                return md;
        }

        /**
         * Create the metadata model used by the distance functions.
         * @return 
         */
        public static Metadata createDistancesMetadataGraph() {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.DOUBLE)},
                        new String[]{GraphSchema.ID, GraphSchema.DISTANCE});
                return md;
        }

        /**
         * Create the metadata model used by the reachable edge function.
         * @return 
         */
        public static Metadata createReachablesEdgesMetadata() {
                return new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.GEOMETRY), TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.DOUBLE), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{"the_geom", GraphSchema.ID, GraphSchema.WEIGHT, GraphSchema.DISTANCE});
        }

        /**
         * Create the metadata model used by the multi-reachable edges function.
         * @return 
         */
        public static Metadata createMReachablesEdgesMetadata() {
                return new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.GEOMETRY), TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.DOUBLE), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{"the_geom", GraphSchema.ID, GraphSchema.SOURCE_NODE, GraphSchema.WEIGHT, GraphSchema.DISTANCE});
        }

        /**
         * Metadata for the subgraph statistics utility.
         * @return 
         */
        public static Metadata createSubGraphStatsMetadata() {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.DOUBLE)},
                        new String[]{GraphSchema.ID, "count", GraphSchema.SUM});
                return md;
        }
}
