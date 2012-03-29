/**
 * GDMS-Topology  is a library dedicated to graph analysis. It's based on the JGraphT available at
 * http://www.jgrapht.org/. It ables to compute and process large graphs using spatial
 * and alphanumeric indexes.
 * This version is developed at French IRSTV institut as part of the
 * EvalPDU project, funded by the French Agence Nationale de la Recherche
 * (ANR) under contract ANR-08-VILL-0005-01 and GEBD project
 * funded by the French Ministery of Ecology and Sustainable Development .
 *
 * GDMS-Topology  is distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
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
 * For more information, please consult: <http://trac.orbisgis.org/>
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
 * @author ebocher
 */
public final class GraphMetadataFactory {

        private GraphMetadataFactory() {
        }

        /**
         * 
         * @return 
         */
        public static Metadata createEdgeMetadataGraph() {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.GEOMETRY, new Constraint[]{new GeometryDimensionConstraint(GeometryDimensionConstraint.DIMENSION_CURVE)}),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{"the_geom", GraphSchema.ID, GraphSchema.PATH_ID, GraphSchema.START_NODE, GraphSchema.END_NODE, GraphSchema.WEIGHT});
                return md;
        }

        /**
         * 
         * @return 
         */
        public static Metadata createEdgeMetadataGraphWithoutGeometry() {
                Metadata md = new DefaultMetadata(
                        new Type[]{
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.INT), TypeFactory.createType(Type.INT), TypeFactory.createType(Type.DOUBLE)},
                        new String[]{GraphSchema.ID, GraphSchema.PATH_ID, GraphSchema.START_NODE, GraphSchema.END_NODE, GraphSchema.WEIGHT});
                return md;
        }

        /**
         * 
         * @return 
         */
        public static Metadata createDistancesMetadataGraph() {
                Metadata md = new DefaultMetadata(
                        new Type[]{TypeFactory.createType(Type.INT), 
                                TypeFactory.createType(Type.DOUBLE)},
                        new String[]{GraphSchema.ID, GraphSchema.DISTANCE});
                return md;
        }
}
