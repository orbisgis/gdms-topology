/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV institut as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministery
 * of Ecology and Sustainable Development.
 *
 * GDMS-Topology is distributed under GPL 3 license. It is produced by the
 * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
 * 2488.
 *
 * Copyright (C) 2009-2012 IRSTV (FR CNRS 2488)
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://wwwc.orbisgis.org/> or contact
 * directly: info_at_ orbisgis.org
 */
package org.gdms.gdmstopology;

import com.vividsolutions.jts.io.WKTReader;
import java.io.File;
import org.gdms.data.DataSourceFactory;
import org.junit.After;
import org.junit.Before;
import org.orbisgis.utils.FileUtils;

/**
 * Registers some data sources for use in unit tests.
 *
 * @author Erwan Bocher, Adam Gouge
 */
public abstract class TopologySetupTest {

    /**
     * Data source factory.
     */
    protected DataSourceFactory dsf;
    /**
     * Well-known text reader for constructing geometries.
     */
    protected WKTReader wktReader;
    /**
     * 2D graph.
     */
    protected String GRAPH2D = "graph2D";
    /**
     * 2D graph edges.
     */
    protected String GRAPH2D_EDGES = "graph2D_edges";
    /**
     * 2D graph nodes.
     */
    protected String GRAPH2D_NODES = "graph2D_nodes";
    /**
     * The shape file extension.
     */
    private static final String DOT_SHP = ".shp";
    /**
     * The resources folder.
     */
    protected static final String resourcesFolder =
            "src/test/resources/org/gdms/gdmstopology/";
    /**
     * Temporary folder for saving results.
     */
    public static File tmpFolder = new File("./target/gdmstopology");

    /**
     * Creates the temp folder and registers some data sources.
     *
     * @throws Exception
     */
    @Before
    public void setUp() throws Exception {

        // Make the temp folder, deleting any previous temp folder.
        if (tmpFolder.exists()) {
            FileUtils.deleteDir(tmpFolder);
        }
        tmpFolder.mkdir();

        // Initialize the DataSourceFactory that uses the folder.
        // We store both the sources and the temporary sources in the
        // temp folder.
        dsf = new DataSourceFactory(tmpFolder.getAbsolutePath(),
                                    tmpFolder.getAbsolutePath());

        // Initialize the WKTReader.
        wktReader = new WKTReader();

        // Register some data sources.
        dsf.getSourceManager()
                .register(GRAPH2D,
                          new File(resourcesFolder + GRAPH2D + DOT_SHP));
        dsf.getSourceManager()
                .register(GRAPH2D_EDGES,
                          new File(resourcesFolder + GRAPH2D_EDGES + DOT_SHP));
        dsf.getSourceManager()
                .register(GRAPH2D_NODES,
                          new File(resourcesFolder + GRAPH2D_NODES + DOT_SHP));
    }

    /**
     * Deletes the temp folder.
     *
     * @throws Exception
     */
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDir(tmpFolder);
    }
}
