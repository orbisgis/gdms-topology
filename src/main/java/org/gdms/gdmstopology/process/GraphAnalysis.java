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

import org.gdms.data.NoSuchTableException;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.indexes.IndexException;
import org.gdms.data.schema.Metadata;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.model.GraphSchema;
import org.orbisgis.progress.ProgressMonitor;

/**
 *
 * @author ebocher IRSTV FR CNRS 2488
 */
public class GraphAnalysis {

        protected static int ID_FIELD_INDEX = -1;
        protected static int SOURCE_FIELD_INDEX = -1;
        protected static int TARGET_FIELD_INDEX = -1;

        /**
         * A method to check if the dataset metadata contains the column named source.
         * @param nodes
         *
         * @throws DriverException
         */
        protected static boolean checkSourceColumn(DataSet nodes) throws DriverException {
                Metadata nodesMD = nodes.getMetadata();
                SOURCE_FIELD_INDEX = nodesMD.getFieldIndex(GraphSchema.SOURCE_NODE);
                if (SOURCE_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table must contains a field named " + GraphSchema.SOURCE_NODE);
                }

                return true;
        }

        /**
         * A method to check if the dataset metadata contains requiered column names.
         * {id, source, target}         
         * @param nodes
         *
         * @throws DriverException
         */
        protected static boolean checkMetadata(DataSet nodes) throws DriverException {
                Metadata nodesMD = nodes.getMetadata();
                ID_FIELD_INDEX = nodesMD.getFieldIndex(GraphSchema.ID);
                SOURCE_FIELD_INDEX = nodesMD.getFieldIndex(GraphSchema.SOURCE_NODE);
                TARGET_FIELD_INDEX = nodesMD.getFieldIndex(GraphSchema.TARGET_NODE);
                if (ID_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table must contains a field named " + GraphSchema.ID);
                }

                if (SOURCE_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table must contains a field named " + GraphSchema.SOURCE_NODE);
                }
                if (TARGET_FIELD_INDEX == -1) {
                        throw new IllegalArgumentException("The table must contains a field named " + GraphSchema.TARGET_NODE);
                }
                return true;
        }

        /**
         * A method to create an alphanumeric index on the field source
         * stored in the datasource nodes.
         * @param dsf
         * @param nodes
         * @param pm
         * @throws GraphException 
         */
        protected static void initIndex(DataSourceFactory dsf, DataSet nodes, ProgressMonitor pm) throws GraphException {
                try {
                        if (!dsf.getIndexManager().isIndexed(nodes, GraphSchema.SOURCE_NODE)) {
                                dsf.getIndexManager().buildIndex(nodes, GraphSchema.SOURCE_NODE, pm);
                        }
                } catch (IndexException ex) {
                        throw new GraphException("Unable to create index.", ex);
                } catch (NoSuchTableException ex) {
                        throw new GraphException("Unable to find the table.", ex);
                }
        }
}
