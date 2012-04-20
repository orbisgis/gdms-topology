package org.gdms.gdmstopology.model;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;

/**
 *
 * @author ebocher
 */
public class GraphEdgeIterator implements Iterator<GraphEdge> {

        private final long count;
        private long index;
        private final GDMSGraph gdmsGraph;

        public GraphEdgeIterator(GDMSGraph gdmsGraph) throws DriverException {
                if (gdmsGraph == null) {
                        throw new NullPointerException("The graph cannot be null!");
                }
                this.gdmsGraph = gdmsGraph;
                count = gdmsGraph.getRowCount();
        }

        @Override
        public boolean hasNext() {
                return index < count;
        }

        @Override
        public GraphEdge next() {
                GraphEdge graphEdge;
                try {
                        graphEdge = gdmsGraph.getGraphEdge(index);
                } catch (DriverException ex) {
                       throw new IllegalStateException(ex);
                }
                index++;
                return graphEdge;
        }

        @Override
        public void remove() {
                throw new UnsupportedOperationException();
        }
}
