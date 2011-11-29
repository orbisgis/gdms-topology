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
package org.gdms.gdmstopology.process;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.gdms.driver.DriverException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.util.LinearComponentExtracter;
import org.gdms.data.schema.MetadataUtilities;
import org.gdms.driver.DataSet;

public class LineNoder {

        private DataSet dataSet;
        private GeometryFactory geometryFactory = new GeometryFactory();

        public LineNoder(final DataSet dataSet) {
                this.dataSet = dataSet;
        }

        public Collection getLines() throws DriverException {
                int geomFieldIndex = MetadataUtilities.getSpatialFieldIndex(dataSet.getMetadata());
                if (geomFieldIndex != -1) {
                        List linesList = new ArrayList();
                        LinearComponentExtracter lineFilter = new LinearComponentExtracter(
                                linesList);
                        for (int i = 0; i < dataSet.getRowCount(); i++) {
                                Geometry g = dataSet.getFieldValue(i, geomFieldIndex).getAsGeometry();
                                g.apply(lineFilter);
                        }
                        return linesList;
                } else {
                        throw new DriverException("The table must contains a geometry field");
                }
        }

        /**
         * Nodes a collection of linestrings. Noding is done via JTS union, which is
         * reasonably effective but may exhibit robustness failures.
         *
         * @param lines
         *            the linear geometries to node
         * @return a collection of linear geometries, noded together
         */
        public Geometry getNodeLines(Collection lines) {
                Geometry linesGeom = geometryFactory.createMultiLineString(geometryFactory.toLineStringArray(lines));

                Geometry unionInput = geometryFactory.createMultiLineString(null);
                // force the unionInput to be non-empty if possible, to ensure union is
                // not optimized away
                Geometry minLine = extractPoint(lines);
                if (minLine != null) {
                        unionInput = minLine;
                }
                Geometry noded = linesGeom.union(unionInput);
                return noded;
        }

        public static List toLines(Geometry geom) {
                List linesList = new ArrayList();
                LinearComponentExtracter lineFilter = new LinearComponentExtracter(
                        linesList);
                geom.apply(lineFilter);
                return linesList;
        }

        private Geometry extractPoint(Collection lines) {
                Geometry point = null;
                // extract first point from first non-empty geometry
                for (Iterator i = lines.iterator(); i.hasNext();) {
                        Geometry g = (Geometry) i.next();
                        if (!g.isEmpty()) {
                                Coordinate p = g.getCoordinate();
                                point = g.getFactory().createPoint(p);
                        }
                }
                return point;
        }
}
