/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV institute as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministry
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
package org.gdms.gdmstopology.centrality;

import com.graphhopper.sna.data.NodeBetweennessInfo;
import com.graphhopper.storage.Graph;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.model.GraphSchema;
import org.orbisgis.progress.ProgressMonitor;

/**
 * Calculates network parameters such as centrality indices on the nodes of a
 * given graph and writes them to a table.
 *
 * @author Adam Gouge
 */
public abstract class GraphAnalyzer {

    /**
     * Used to parse the data set.
     */
    protected final DataSourceFactory dsf;
    /**
     * The data set.
     */
    protected final DataSet dataSet;
    /**
     * Progress monitor.
     */
    protected final ProgressMonitor pm;
    /**
     * Orientation.
     */
    protected final int graphType;

    /**
     * Constructs a new {@link GraphAnalyzer}.
     *
     * @param dsf       The {@link DataSourceFactory} used to parse the data
     *                  set.
     * @param dataSet   The data set.
     * @param pm        The progress monitor used to track the progress of the
     *                  calculation.
     * @param graphType The orientation.
     *
     * @throws DriverException
     * @throws GraphException
     */
    public GraphAnalyzer(DataSourceFactory dsf,
                         DataSet dataSet,
                         ProgressMonitor pm,
                         int graphType)
            throws DriverException,
            GraphException {
        this.dsf = dsf;
        this.dataSet = dataSet;
        this.pm = pm;
        this.graphType = graphType;
    }

    /**
     * Creates the graph, performs the analysis, stores the results in a
     * {@link DiskBufferDriver}, writes them to a table and returns the results.
     *
     * @param outputTablePrefix The output table prefix.
     *
     * @return The results.
     *
     * @throws GraphException
     * @throws DriverException
     * @throws IOException
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     */
    public HashMap<Integer, NodeBetweennessInfo> doAnalysis(
            String outputTablePrefix)
            throws GraphException,
            DriverException,
            IOException,
            NoSuchTableException,
            DataSourceCreationException {

        // Prepare the graph.
        Graph graph = prepareGraph(graphType);

        // Compute all graph analysis parameters.
        HashMap<Integer, NodeBetweennessInfo> results = computeAll(graph);

        // Get the DiskBufferDriver to store.
        DiskBufferDriver driver = createDriver(results);

        // Write it to a table.
        writeToTable(driver, outputTablePrefix);

        // Return the results.
        return results;
    }

    /**
     * Prepares the graph according to the given orientation.
     *
     * @param graphType The orientation.
     *
     * @return The newly prepared graph.
     *
     * @throws DriverException
     * @throws GraphException
     */
    // TODO: Migrate the implementations from the GraphLoaderUtilities
    // class to the subclasses of this class.
    protected abstract Graph prepareGraph(int graphType)
            throws DriverException,
            GraphException;

    /**
     * Computes all network parameters on the given graph.
     *
     * @param graph The graph.
     *
     * @return The result.
     *
     * @see GraphAnalyzer#doAnalysis(java.lang.String).
     */
    protected abstract HashMap<Integer, NodeBetweennessInfo> computeAll(
            Graph graph);

    /**
     * Creates and returns the {@link DiskBufferDriver} after storing the
     * results inside.
     *
     * @param results The results to be stored.
     *
     * @return The {@link DiskBufferDriver} holding the results.
     *
     * @throws DriverException
     */
    private DiskBufferDriver createDriver(
            HashMap<Integer, NodeBetweennessInfo> results) throws
            DriverException {

        final Metadata metadata = createMetadata();

        DiskBufferDriver driver = new DiskBufferDriver(dsf, metadata);

        storeResultsInDriver(results, driver);

        cleanUp(driver, pm);

        return driver;
    }

    /**
     * Writes the results stored in the driver to a new table.
     *
     * @param driver            The driver.
     * @param outputTablePrefix The output table prefix.
     *
     * @return The name of the table.
     *
     * @throws DriverException
     */
    private String writeToTable(DiskBufferDriver driver,
                                String outputTablePrefix)
            throws DriverException {
        // TODO: Necessary?
        driver.open();
        // Register the result in a new output table.
        String outputTableName = dsf.getSourceManager().
                getUniqueName(
                outputTablePrefix
                + "." + GraphSchema.GRAPH_ANALYSIS);
        dsf.getSourceManager().register(
                outputTableName,
                driver.getFile());
        return outputTableName;
    }

    /**
     * Creates the metadata for the results.
     *
     * @return The metadata.
     */
    private Metadata createMetadata() {
        return new DefaultMetadata(
                new Type[]{
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.DOUBLE),
                    TypeFactory.createType(Type.DOUBLE)},
                new String[]{
                    GraphSchema.ID,
                    GraphSchema.BETWEENNESS_CENTRALITY,
                    GraphSchema.CLOSENESS_CENTRALITY});
    }

    /**
     * Stores the results in a {@link DiskBufferDriver}.
     *
     * @param results The results.
     * @param driver  The driver.
     *
     * @throws DriverException
     */
    private void storeResultsInDriver(
            HashMap<Integer, NodeBetweennessInfo> results,
            DiskBufferDriver driver) throws DriverException {

        long start = System.currentTimeMillis();

        Set<Integer> keySet = results.keySet();
        Iterator<Integer> it = keySet.iterator();

        while (it.hasNext()) {

            final int node = it.next();
            final NodeBetweennessInfo nodeNBInfo = results.get(node);

            Value[] valuesToAdd =
                    new Value[]{
                // ID
                ValueFactory.createValue(node),
                // Betweenness
                ValueFactory.createValue(nodeNBInfo.getBetweenness()),
                // Closeness
                ValueFactory.createValue(nodeNBInfo.getCloseness())
            };

            driver.addValues(valuesToAdd);
        }

        long stop = System.currentTimeMillis();
        System.out.println("Stored graph analysis results in "
                + (stop - start) + " ms.");
    }

    /**
     * Cleans up.
     *
     * @param driver The driver.
     * @param pm     The progress monitor.
     *
     * @throws DriverException
     */
    private void cleanUp(DiskBufferDriver driver, ProgressMonitor pm)
            throws DriverException {
        // We are done writing.
        driver.writingFinished();
        // The task is done.
        pm.endTask();
        // So close the DiskBufferDriver.
        driver.close();
    }
}
