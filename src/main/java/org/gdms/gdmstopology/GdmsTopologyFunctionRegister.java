package org.gdms.gdmstopology;

import org.gdms.gdmstopology.function.ST_BlockIdentity;
import org.gdms.gdmstopology.function.ST_Graph;
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

        private GdmsTopologyFunctionRegister() {
        }

        /**
         * Register all gdms-topology functions.
         */
        public static void register() {
                FunctionManager.addFunction(ST_BlockIdentity.class);
                FunctionManager.addFunction(ST_Graph.class);
                FunctionManager.addFunction(ST_PlanarGraph.class);
                FunctionManager.addFunction(ST_ShortestPath.class);
                FunctionManager.addFunction(ST_ShortestPathLength.class);
                FunctionManager.addFunction(ST_ToLineNoder.class);

        }
}
