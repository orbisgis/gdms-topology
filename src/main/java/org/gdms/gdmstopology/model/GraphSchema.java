package org.gdms.gdmstopology.model;

/**
 *
 * @author ebocher
 */
public final class GraphSchema {

        public static final String START_NODE = "start_node";
        public static final String END_NODE = "end_node";
        public static final String ID = "id";
        public static final String WEIGTH = "weigth";
        public static final String LEFT_FACE = "left_polygon";
        public static final String RIGHT_FACE = "right_polygon";
        public static String WEIGTH_SUM = "weigth_sum";

        /**
         * Some fields needed for the input datasource.
         */
        private GraphSchema() {
        }
}
