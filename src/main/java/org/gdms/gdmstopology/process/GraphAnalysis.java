package org.gdms.gdmstopology.process;

import java.util.List;
import org.gdms.gdmstopology.model.GraphEdge;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.WeightedGraph;


/**
 *
 * @author ebocher
 */
public class GraphAnalysis {

        private GraphAnalysis() {
        }

        public static List<GraphEdge> getShortestPath(WeightedGraph<Integer, GraphEdge> graph,
                Integer startNode, Integer endNode) {
                return DijkstraShortestPath.findPathBetween(graph, startNode, endNode);
        }
}
