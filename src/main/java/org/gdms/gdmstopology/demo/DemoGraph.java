package org.gdms.gdmstopology.demo;

import java.io.File;
import java.util.List;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.DataSourceFactory;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.process.GraphPath;
import org.gdms.gdmstopology.process.GraphUtilities;
import org.gdms.gdmstopology.utils.RandomGraphCreator;
import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
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
                dsf = new DataSourceFactory("/tmp/orbis", "/tmp/orbis");
                //GDMSGraphPath(path_edges);
                GDMSGraphStatistic(path_edges);
                //GDMSGraphDistance(path_edges);

        }

        public static void JGraphT() {
                long start = System.currentTimeMillis();
                Graph< Integer, DefaultEdge> graph = RandomGraphCreator.createRandomWeightedMultigraph(100, 1310);
                List<DefaultEdge> list = DijkstraShortestPath.findPathBetween(graph, 32, 1);
                System.out.println(list != null);
                long end = System.currentTimeMillis();
                System.out.println("Duration " + (end - start));
                }

        public static void GDMSGraphPath(String filePath) throws DataSourceCreationException, DriverException, GraphException {
                long start = System.currentTimeMillis();

                DataSource dsEdges = dsf.getDataSource(new File(filePath));

                dsEdges.open();
                DiskBufferDriver result = GraphPath.getShortestPath(dsf, dsEdges, 44772, 1, "length", 3, new NullProgressMonitor());

                result.open();
                System.out.println("Objects : " + result.getRowCount());
                result.close();
                dsEdges.close();

                long end = System.currentTimeMillis();
                System.out.println("Duration " + (end - start));
                }

        public static void GDMSGraphStatistic(String filePath) throws DataSourceCreationException, DriverException, GraphException {
                long start = System.currentTimeMillis();
                DataSource dsEdges = dsf.getDataSource(new File(filePath));
                dsEdges.open();
                GraphUtilities.getGraphStatistics(dsf, dsEdges, "length", 3, new NullProgressMonitor());
                dsEdges.close();
                long end = System.currentTimeMillis();
                System.out.println("Duration " + (end - start));
                }


         public static void GDMSGraphDistance(String filePath) throws DataSourceCreationException, DriverException, GraphException {
                long start = System.currentTimeMillis();
                DataSource dsEdges = dsf.getDataSource(new File(filePath));
                dsEdges.open();
                GraphPath.getShortestPathLength(dsf, dsEdges,1, "length", 3, new NullProgressMonitor());
                dsEdges.close();
                long end = System.currentTimeMillis();
                System.out.println("Duration " + (end - start));
        }
}
