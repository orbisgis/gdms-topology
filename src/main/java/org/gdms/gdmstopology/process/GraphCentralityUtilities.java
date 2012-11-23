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

import dk.aaue.sna.alg.centrality.CentralityResult;
import dk.aaue.sna.alg.centrality.FreemanClosenessCentrality;
import java.util.Map;
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
import org.gdms.gdmstopology.model.DWMultigraphDataSource;
import org.gdms.gdmstopology.model.EdgeReversedGraphDataSource;
import org.gdms.gdmstopology.model.GDMSValueGraph;
import org.gdms.gdmstopology.model.GraphEdge;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.model.WMultigraphDataSource;
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
        // Calculate and record the centrality indices according
        // to the given graph type.
        if (graphType == GraphSchema.DIRECT) {
            DWMultigraphDataSource dWMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
            dWMultigraphDataSource.setWeightFieldIndex(weightColumnName);
            DiskBufferDriver closenessCentralityDriver = calculateClosenessCentralityIndices(dsf, dWMultigraphDataSource, pm);
            writeToTable(dsf, closenessCentralityDriver, outputTablePrefix);
        } else if (graphType == GraphSchema.DIRECT_REVERSED) {
            DWMultigraphDataSource dWMultigraphDataSource = new DWMultigraphDataSource(dsf, dataSet, pm);
            dWMultigraphDataSource.setWeightFieldIndex(weightColumnName);
            EdgeReversedGraphDataSource edgeReversedGraphDataSource = new EdgeReversedGraphDataSource(dWMultigraphDataSource);
            DiskBufferDriver closenessCentralityDriver = calculateClosenessCentralityIndices(dsf, edgeReversedGraphDataSource, pm);
            writeToTable(dsf, closenessCentralityDriver, outputTablePrefix);
        } else if (graphType == GraphSchema.UNDIRECT) {
            WMultigraphDataSource wMultigraphDataSource = new WMultigraphDataSource(dsf, dataSet, pm);
            wMultigraphDataSource.setWeightFieldIndex(weightColumnName);
            DiskBufferDriver closenessCentralityDriver = calculateClosenessCentralityIndices(dsf, wMultigraphDataSource, pm);
            writeToTable(dsf, closenessCentralityDriver, outputTablePrefix);
        } else {
            throw new GraphException("Only three types of graphs "
                    + "are allowed: enter 1 if the graph is "
                    + "directed, 2 if it is directed and you wish to reverse the "
                    + "orientation of the edges, and 3 if the graph is undirected. "
                    + "If no orientation is specified, the graph is assumed "
                    + "to be directed.");
        }
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
            GDMSValueGraph<Integer, GraphEdge> graph,
            ProgressMonitor pm)
            throws GraphException, DriverException {

        // PRELIMINARIES

        // Make a new FreemanClosenessCentrality from the given graph.
        FreemanClosenessCentrality<Integer, GraphEdge> alg = new FreemanClosenessCentrality<Integer, GraphEdge>(graph);

        // CALCULATION

        // Calculate the centrality indices.
        CentralityResult<Integer> result = alg.calculate();
//        // Recover them in a list.
        java.util.List<Map.Entry<Integer, Double>> sortedCentralityIndicesList = result.getSorted();
//        // Get an iterator to go through the list
        java.util.ListIterator<Map.Entry<Integer, Double>> sortedCentralityIndicesListIterator = sortedCentralityIndicesList.listIterator();

        // WRITING

//        // Get an iterator on the DataSet.
//        Iterator<Value[]> iterator = dataSet.iterator();
//        // Obtain the original metadata from the input table to which we will append the metadata for the edges.
//        DefaultMetadata originalMetadata = new DefaultMetadata(dataSet.getMetadata());
//        // Count the number of fields in the input table.
//        int sourceFieldsCount = originalMetadata.getFieldCount();

//        // Set a counter for checking if the task has been cancelled.
//        int count = 0;
//        // Go through the DataSet
//        while (iterator.hasNext()) {
//            // Obtain a row.
//            Value[] values = iterator.next();
//            // See if the task has been cancelled.
//            if (count >= 100 && count % 100 == 0) {
//                if (pm.isCancelled()) {
//                    break;
//                }
//            }
//            // Prepare the new row which will be the old row with new values appended.
//            final Value[] newValues = new Value[fieldsCount];
//
//            count++;
//        }

        // Create the metadata for the new table that will hold the 
        // closeness centrality indices.
        Metadata md = new DefaultMetadata(
                new Type[]{
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.DOUBLE)},
                new String[]{
                    GraphSchema.ID,
                    GraphSchema.CLOSENESS_CENTRALITY});

        // Create a DiskBufferDriver to store the centrality indices.
        DiskBufferDriver diskBufferDriver = new DiskBufferDriver(dsf, md);

        // Record the centrality indices in the DiskBufferDriver.
        while (sortedCentralityIndicesListIterator.hasNext()) {
            Map.Entry<Integer, Double> nextEntry = sortedCentralityIndicesListIterator.next();
            diskBufferDriver.addValues(
                    new Value[]{
                        // Node ID
                        ValueFactory.createValue(nextEntry.getKey()),
                        // Centrality index
                        ValueFactory.createValue(nextEntry.getValue())
                    });
        }

        // CLEAN UP

        // We are done writing.
        diskBufferDriver.writingFinished();
        // The task is done.
        pm.endTask();
        // So close the DiskBufferDriver.
        diskBufferDriver.close();
        // And return it.
        return diskBufferDriver;
    }

    /**
     * Writes the results of the closeness centrality calculation to a new
     * table.
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
