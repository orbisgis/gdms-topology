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
package org.gdms.gdmstopology;

import org.gdms.gdmstopology.function.ST_BlockIdentity;
import org.gdms.gdmstopology.function.ST_FindReachableEdges;
import org.gdms.gdmstopology.function.ST_Graph;
import org.gdms.gdmstopology.function.ST_MFindReachableEdges;
import org.gdms.gdmstopology.function.ST_MShortestPath;
import org.gdms.gdmstopology.function.ST_MShortestPathLength;
import org.gdms.gdmstopology.function.ST_PlanarGraph;
import org.gdms.gdmstopology.function.ST_ShortestPath;
import org.gdms.gdmstopology.function.ST_ShortestPathLength;
import org.gdms.gdmstopology.function.ST_ToLineNoder;
import org.gdms.sql.function.FunctionManager;

/**
 *
 * @author ebocher
 */
public class GdmsTopologyFunctionRegister {

        /**
         * A class to register all topological functions.
         */
        private GdmsTopologyFunctionRegister() {
        }

        /**
         * Register all functions from gdms-topology.
         */
        public static void register() {
                FunctionManager.addFunction(ST_BlockIdentity.class);
                FunctionManager.addFunction(ST_Graph.class);
                FunctionManager.addFunction(ST_PlanarGraph.class);
                FunctionManager.addFunction(ST_ShortestPath.class);
                FunctionManager.addFunction(ST_ShortestPathLength.class);
                FunctionManager.addFunction(ST_ToLineNoder.class);
                FunctionManager.addFunction(ST_MShortestPathLength.class);
                FunctionManager.addFunction(ST_FindReachableEdges.class);
                FunctionManager.addFunction(ST_MFindReachableEdges.class);
                FunctionManager.addFunction(ST_MShortestPath.class);
        }
}
