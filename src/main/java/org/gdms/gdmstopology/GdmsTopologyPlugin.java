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
package org.gdms.gdmstopology;

import org.gdms.data.DataSourceFactory;
import org.gdms.gdmstopology.function.*;
import org.gdms.plugins.GdmsPlugIn;


/**
 *
 * @author ebocher
 */


public class GdmsTopologyPlugin implements GdmsPlugIn{
        private DataSourceFactory dsf;

        @Override
        public void load(DataSourceFactory dsf) {
                this.dsf=dsf;
                dsf.getFunctionManager().addFunction(ST_BlockIdentity.class);
                dsf.getFunctionManager().addFunction(ST_Graph.class);
                dsf.getFunctionManager().addFunction(ST_PlanarGraph.class);
                dsf.getFunctionManager().addFunction(ST_ShortestPath.class);
                dsf.getFunctionManager().addFunction(ST_ShortestPathLength.class);
                dsf.getFunctionManager().addFunction(ST_ToLineNoder.class);
                dsf.getFunctionManager().addFunction(ST_MShortestPathLength.class);
                dsf.getFunctionManager().addFunction(ST_FindReachableEdges.class);
                dsf.getFunctionManager().addFunction(ST_MFindReachableEdges.class);
                dsf.getFunctionManager().addFunction(ST_MShortestPath.class);
                dsf.getFunctionManager().addFunction(ST_SubGraphStatistics.class);
        }

        @Override
        public void unload() {
              
        }

        @Override
        public String getName() {
               return "GDMS topology plugin";
        }

        @Override
        public String getVersion() {
                return "1.2";
        }
        
}
