package org.gdms.gdmstopology;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.WKTReader;
import java.io.File;
import junit.framework.TestCase;
import org.gdms.data.SQLDataSourceFactory;
import org.junit.After;
import org.junit.Before;
import org.orbisgis.utils.FileUtils;

/**
 *
 * @author ebocher
 */
public class TopologySetUpTest {

        protected static final GeometryFactory gf = new GeometryFactory();
        protected SQLDataSourceFactory dsf;
        protected WKTReader wktReader;
        protected String GRAPH = "graph";
        protected String GRAPH_EDGES = "graph_edges";
        protected String GRAPH_NODES = "graph_nodes";
        public static String internalData = new String("src/test/resources/");

        @Before
        public  void setUp() throws Exception {
                //Create a folder to save all results
                File backUpFile = new File("target/backup");
                FileUtils.deleteDir(backUpFile);
                backUpFile.mkdir();
                //Create the datasourcefactory that uses the folder
                dsf = new SQLDataSourceFactory("target/backup", "target/backup");

                //Create some geometries
                wktReader = new WKTReader();
                dsf.getSourceManager().register(GRAPH, new File(internalData + "graph.shp"));
                dsf.getSourceManager().register(GRAPH_EDGES, new File(internalData + "graph_edges.shp"));
                dsf.getSourceManager().register(GRAPH_NODES, new File(internalData + "graph_nodes.shp"));
        }

        @After
        public  void tearDown() throws Exception {
                //Delete the folder that contains result
                File backUpFile = new File("target/backup");
                FileUtils.deleteDir(backUpFile);
        }
}
