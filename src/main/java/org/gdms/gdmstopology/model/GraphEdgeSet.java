package org.gdms.gdmstopology.model;

import java.util.HashSet;
import java.util.Iterator;
import org.gdms.driver.DriverException;

/**
 *
 * @author ebocher
 */
public class GraphEdgeSet extends HashSet<GraphEdge> {
        private final GDMSGraph gdmsGraph;
        

        public GraphEdgeSet(GDMSGraph gdmsGraph) {
                this.gdmsGraph=gdmsGraph;
                System.out.println("Index "+ gdmsGraph.END_NODE_FIELD_INDEX);
        }

        @Override
        public Iterator<GraphEdge> iterator() {
                try {
                        return new GraphEdgeIterator(gdmsGraph);
                } catch (DriverException ex) {
                        throw new IllegalStateException(ex);
                }
        }
}
