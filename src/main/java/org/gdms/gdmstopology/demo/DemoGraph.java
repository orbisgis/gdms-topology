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
package org.gdms.gdmstopology.demo;

import java.io.File;
import java.util.List;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.DataSourceFactory;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.function.ST_ShortestPath;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.utils.GraphWriter;
import org.gdms.gdmstopology.utils.RandomGraphCreator;
import org.gdms.sql.engine.ParseException;
import org.gdms.sql.function.FunctionManager;
import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;

/**
 *
 * @author Erwan Bocher
 */
public class DemoGraph {

        private static DataSourceFactory dsf;
        public static String path_edges = "/tmp/graph_edges.gdms";
        public static String path_nodes = "/tmp/graph_nodes.gdms";

        /**
         * @param args the command line arguments
         */
        public static void main(String[] args) throws Exception {
                dsf = new DataSourceFactory("/tmp/orbis", "/tmp/orbis");
                GDMSGraphPath(path_edges);
                //GDMSGraphStatistic(path_edges);
                //GDMSGraphDistance(path_edges);
                //GDMSGraphWriter(path_nodes, path_edges, 100000,500000);

        }

        public static void JGraphT() {
                long start = System.currentTimeMillis();
                Graph< Integer, DefaultEdge> graph = RandomGraphCreator.createRandomWeightedMultigraph(100, 1310);
                List<DefaultEdge> list = DijkstraShortestPath.findPathBetween(graph, 32, 1);
                System.out.println(list != null);
                long end = System.currentTimeMillis();
                System.out.println("Duration " + (end - start));
                }

        /**
         * A method to test the ShortestPath function
         * @param filePath
         * @throws DataSourceCreationException
         * @throws DriverException
         * @throws GraphException 
         */
        public static void GDMSGraphPath(String filePath) throws DataSourceCreationException, DriverException, GraphException, ParseException {
                long start = System.currentTimeMillis();                
                
                dsf.getFunctionManager().addFunction(ST_ShortestPath.class);
                
                dsf.getSourceManager().register("graph_edges", new File(filePath));

                DataSource result = dsf.getDataSourceFromSQL("SELECT * from  ST_ShortestPath(graph_edges, 85287, 56134, \'cost\', 3);");

                result.open();
                System.out.println("Objects : " + result.getRowCount());
                
                
                for (int i = 0; i < result.getRowCount(); i++) {
                        System.out.println("Path "+ i+ " -> "+  result.getGeometry(i, 0).toString());
                }
                
                result.close();

                long end = System.currentTimeMillis();
                System.out.println("Duration " + (end - start));
                }

//        public static void GDMSGraphStatistic(String filePath) throws DataSourceCreationException, DriverException, GraphException {
//                long start = System.currentTimeMillis();
//                DataSource dsEdges = dsf.getDataSource(new File(filePath));
//                dsEdges.open();
//                GraphUtilities.getGraphStatistics(dsf, dsEdges, "length", 3, new NullProgressMonitor());
//                dsEdges.close();
//                long end = System.currentTimeMillis();
//                System.out.println("Duration " + (end - start));
//                }


//        public static void GDMSGraphDistance(String filePath) throws DataSourceCreationException, DriverException, GraphException {
//                long start = System.currentTimeMillis();
//                DataSource dsEdges = dsf.getDataSource(new File(filePath));
//                dsEdges.open();
//                GraphPath.getShortestPathLength(dsf, dsEdges,1, "length", 3, new NullProgressMonitor());
//                dsEdges.close();
//                long end = System.currentTimeMillis();
//                System.out.println("Duration " + (end - start));
//        }

        private static void GDMSGraphWriter(String path_nodes, String path_edges, int nbNodes, int nbEdges) throws DriverException {
                Graph<Integer, DefaultEdge> graph = RandomGraphCreator.createRandomWeightedMultigraph(nbNodes, nbEdges);
                GraphWriter.saveAsGDMSFile(graph, path_nodes, path_edges);
        }
}
