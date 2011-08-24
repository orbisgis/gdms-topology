package org.gdms.gdmstopology.model;

import java.io.File;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceFactory;
import org.jgrapht.traverse.ClosestFirstIterator;
import org.orbisgis.progress.NullProgressMonitor;

/**
 *
 * @author ebocher
 */
public class DemoGraph {

        private static DataSourceFactory dsf;
        public static String path_edges = "/tmp/graph_edges.shp";
        public static String path_nodes = "/tmp/graph_nodes.shp";

        /**
         * @param args the command line arguments
         */
        public static void main(String[] args) throws Exception {


                dsf = new DataSourceFactory();

                DataSource dsEdges = dsf.getDataSource(new File(path_edges));

                dsEdges.open();

                WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, dsEdges, new NullProgressMonitor());



                int source = 44772;
                int target = 100;

                System.out.println(wMultigraphDataSource.containsVertex(source));
                System.out.println(wMultigraphDataSource.containsVertex(target));

                long start = System.currentTimeMillis();

                /*List<GraphEdge> list = GraphAnalysis.getShortestPath(wMultigraphDataSource, source, target);

                if (list != null) {
                double sum =0;
                for (GraphEdge graphEdge : list) {
                System.out.println(graphEdge.toString());
                sum+=graphEdge.getWeight();
                }
                System.out.println(sum);
                }*/

                ClosestFirstIterator<Integer, GraphEdge> cl = new ClosestFirstIterator<Integer, GraphEdge>(
                        wMultigraphDataSource, source);

                int k = 0;
                while (cl.hasNext()) {
                        Integer node = cl.next();
                        if (node != source) {
                                double length = cl.getShortestPathLength(node);
                                System.out.println("For node : " + node + " lenght : " + length + " : " + k++);
                        }
                }
                /*
                HashSet<Integer> srcNodes = new HashSet<Integer>();

                // for (int i = 1; i < 2; i++) {
                // srcNodes.add(i);
                // }

                srcNodes.add(1);

                HashSet<Integer> targetNodes = new HashSet<Integer>();

                for (int i = 2; i < 10000; i++) {
                targetNodes.add(i);
                }

                for (Integer srcNode : srcNodes) {
                if (wMultigraphDataSource.containsVertex(srcNode)) {
                ClosestFirstIterator<Integer, GraphEdge> cl = new ClosestFirstIterator<Integer, GraphEdge>(
                wMultigraphDataSource, srcNode);
                int k = 0;
                while (cl.hasNext()) {
                Integer node = cl.next();
                if (node != srcNode) {
                if (targetNodes.contains(node)) {
                double length = cl.getShortestPathLength(node);
                System.out.println(length + " : " + k++);
                }
                }

                }

                } else {
                }
                
                }*/

                dsEdges.close();

                long end = System.currentTimeMillis();
                System.out.println("Duration " + (end - start));
        }
}
