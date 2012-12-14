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
package org.gdms.gdmstopology.process;

import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphStorage;
import com.graphhopper.storage.RAMDirectory;
import com.graphhoppersna.centrality.UndirectedGraphAnalyzer;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.map.hash.TIntDoubleHashMap;
import java.util.Iterator;
import org.gdms.data.DataSourceFactory;
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
 * A collection of utilities to calculate various centrality indices on the
 * nodes of a given graph.
 *
 * @author Adam Gouge
 */
public class GraphCentralityUtilities extends GraphAnalysis {

    /**
     * Registers the closeness centrality indices of the nodes of the given
     * graph.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param dataSet The graph data set.
     * @param weightColumnName The string specifying the name of the weight
     * column specifying the weight of each edge.
     * @param graphType An integer specifying the type of graph: 1 if directed,
     * 2 if directed and we wish to reverse the direction of the edges, 3 if
     * undirected.
     * @param pm Used to track the progress of the calculation.
     *
     * @throws GraphException
     * @throws DriverException
     */
    public static void registerClosenessCentralityIndices(
            DataSourceFactory dsf,
            DataSet dataSet,
            String weightColumnName,
            String outputTablePrefix,
            int graphType,
            ProgressMonitor pm)
            throws GraphException, DriverException {
        // Create an undirected GraphHopper graph from the data set.
        // For now, we assume that we only store the graph in memory
        // and have no option to save it to disk for future use.

        // Initialize the graph.
        Graph graph = new GraphStorage(new RAMDirectory());
        // INSERT THE DATA INTO THE GRAPH.
        // Recover the edge Metadata.
        // TODO: Add a check to make sure the metadata was loaded correctly.
        Metadata edgeMetadata = dataSet.getMetadata();
        // Recover the indices of the start node, end node, and weight.
        int startNodeIndex = edgeMetadata.getFieldIndex(GraphSchema.START_NODE);
        int endNodeIndex = edgeMetadata.getFieldIndex(GraphSchema.END_NODE);
        int weightFieldIndex = edgeMetadata.getFieldIndex(weightColumnName);
        // Get an iterator on the data set.
        Iterator<Value[]> iterator = dataSet.iterator();
        // Add the edges according to the given graph type.
        if (graphType == GraphSchema.DIRECT) {
            while (iterator.hasNext()) {
                Value[] row = iterator.next();
                graph.edge(row[startNodeIndex].getAsInt(),
                        row[endNodeIndex].getAsInt(),
                        row[weightFieldIndex].getAsDouble(),
                        false);
            }
        } else if (graphType == GraphSchema.DIRECT_REVERSED) {
            while (iterator.hasNext()) {
                Value[] row = iterator.next();
                graph.edge(row[endNodeIndex].getAsInt(),
                        row[startNodeIndex].getAsInt(),
                        row[weightFieldIndex].getAsDouble(),
                        false);
            }
        } else if (graphType == GraphSchema.UNDIRECT) {
            while (iterator.hasNext()) {
                Value[] row = iterator.next();
                graph.edge(row[startNodeIndex].getAsInt(),
                        row[endNodeIndex].getAsInt(),
                        row[weightFieldIndex].getAsDouble(),
                        true);
            }
        } else {
            throw new GraphException("Only three types of graphs "
                    + "are allowed: enter 1 if the graph is "
                    + "directed, 2 if it is directed and you wish to reverse the "
                    + "orientation of the edges, and 3 if the graph is undirected. "
                    + "If no orientation is specified, the graph is assumed "
                    + "to be directed.");
        }
        // CALCULATE THE CLOSENESS CENTRALITY.
        DiskBufferDriver closenessCentralityDriver =
                calculateClosenessCentralityIndices(dsf, graph, pm);
        // Write the results to a new table.
        // TODO: Also write the geometries.
        writeToTable(dsf, closenessCentralityDriver, outputTablePrefix);
    }

    /**
     * Calculates the closeness centrality indices of all nodes of the given
     * graph.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param graph The graph on which we will calculate the closeness
     * centrality indices.
     * @param pm Used to track the progress of the calculation.
     *
     * @return A new {@link DiskBufferDriver} containing a list of all nodes and
     * their closeness centrality indices.
     *
     * @throws GraphException
     * @throws DriverException
     */
    public static DiskBufferDriver calculateClosenessCentralityIndices(
            DataSourceFactory dsf,
            Graph graph,
            ProgressMonitor pm) throws DriverException {

        // CALCULATION
        // Initialize an undirected graph analyzer.
        // TODO: Adapt this to directed graphs later.
        UndirectedGraphAnalyzer analyzer = new UndirectedGraphAnalyzer(graph);
        // Calculate the closeness centrality.
        // TODO: Would a list be a more efficient data structure?
        TIntDoubleHashMap closenessCentrality = analyzer.computeAll();


        // TRANSFER THE RESULTS FROM GRAPHHOPPER TO A DISKBUFFERDRIVER.
        // Create the metadata for the new table that will hold the 
        // closeness centrality indices.
        Metadata closenessMetadata = new DefaultMetadata(
                new Type[]{
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.DOUBLE)},
                new String[]{
                    GraphSchema.ID,
                    GraphSchema.CLOSENESS_CENTRALITY});
        // Create a DiskBufferDriver to store the centrality indices.
        DiskBufferDriver closenessBufferDriver = new DiskBufferDriver(dsf, closenessMetadata);
        // Get an iterator on the closeness centrality hash map.
        TIntDoubleIterator centralityIt = closenessCentrality.iterator();
        // Record the centrality indices in the DiskBufferDriver.
        // TODO: Are the results sorted?
        while (centralityIt.hasNext()) {
            centralityIt.advance();
            closenessBufferDriver.addValues(
                    new Value[]{
                        // Node ID
                        ValueFactory.createValue(centralityIt.key()),
                        // Centrality index
                        ValueFactory.createValue(centralityIt.value())
                    });
        }

        // CLEAN UP
        // We are done writing.
        closenessBufferDriver.writingFinished();
        // The task is done.
        pm.endTask();
        // So close the DiskBufferDriver.
        closenessBufferDriver.close();
        // And return it.
        return closenessBufferDriver;
    }

    /**
     * Writes the results of the closeness centrality calculation to a new table
     * in OrbisGIS.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param closenessCentralityDriver
     * @param outputTablePrefix
     * @throws DriverException
     */
    private static void writeToTable(
            DataSourceFactory dsf,
            DiskBufferDriver closenessCentralityDriver,
            String outputTablePrefix) throws DriverException {
        closenessCentralityDriver.open(); // TODO: Necessary?
        // Register the result in a new output table.
        String outputTableName = dsf.getSourceManager().
                getUniqueName(
                outputTablePrefix
                + "." + GraphSchema.CLOSENESS_CENTRALITY);
        dsf.getSourceManager().register(
                outputTableName,
                closenessCentralityDriver.getFile());
    }
}
