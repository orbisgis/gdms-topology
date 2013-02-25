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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
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
import org.gdms.gdmstopology.model.GraphEdge;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.model.WMultigraphDataSource;
import org.jgrapht.alg.ConnectivityInspector;
import org.jgrapht.graph.DirectedMultigraph;
import org.orbisgis.progress.ProgressMonitor;

/**
 * A collection of utilities to measure the connectivity of a given graph.
 *
 * @author Adam Gouge
 */
public class GraphConnectivityUtilities extends GraphAnalysis {

    /**
     * Returns a JGraphT {@link ConnectivityInspector} on a given graph.
     *
     * @param dsf              The {@link DataSourceFactory} used to parse the
     *                         data set.
     * @param dataSet          The graph data set.
     * @param weightColumnName The string specifying the name of the weight
     *                         column specifying the weight of each edge.
     * @param graphType        An integer specifying the type of graph: 1 if
     *                         directed, 2 if directed and we wish to reverse the
     *                         direction of the edges, 3 if undirected.
     * @param pm               Used to track the progress of the calculation.
     *
     * @return A {@link ConnectivityInspector} on the given graph.
     *
     * @throws DriverException
     * @throws GraphException
     */
    public static ConnectivityInspector<Integer, GraphEdge> getConnectivityInspector(
            DataSourceFactory dsf,
            DataSet dataSet,
            String weightColumnName,
            int graphType,
            ProgressMonitor pm) throws DriverException,
            GraphException {
        // Return a connectivity inspector according to the graph type.
        if (graphType == GraphSchema.DIRECT) {
            DWMultigraphDataSource dWMultigraphDataSource =
                    new DWMultigraphDataSource(dsf, dataSet, pm);
            dWMultigraphDataSource.setWeightFieldIndex(weightColumnName);
            return new ConnectivityInspector(dWMultigraphDataSource);
        } else if (graphType == GraphSchema.DIRECT_REVERSED) {
            DWMultigraphDataSource dWMultigraphDataSource =
                    new DWMultigraphDataSource(dsf, dataSet, pm);
            dWMultigraphDataSource.setWeightFieldIndex(weightColumnName);
            EdgeReversedGraphDataSource edgeReversedGraph =
                    new EdgeReversedGraphDataSource(dWMultigraphDataSource);
            return new ConnectivityInspector(edgeReversedGraph);
        } else if (graphType == GraphSchema.UNDIRECT) {
            WMultigraphDataSource wMultigraphDataSource =
                    new WMultigraphDataSource(dsf, dataSet, pm);
            wMultigraphDataSource.setWeightFieldIndex(weightColumnName);
            return new ConnectivityInspector(wMultigraphDataSource);
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
     * Registers a new table listing all the vertices of a graph and to which
     * connected component (numbered consecutively) they belong.
     *
     * @param dsf       The {@link DataSourceFactory} used to parse the data
     *                  set.
     * @param inspector A {@link ConnectivityInspector} on the given graph used
     *                  to identify the connected components.
     *
     * @throws DriverException
     */
    public static void registerConnectedComponents(
            DataSourceFactory dsf,
            ConnectivityInspector inspector) throws DriverException {

        // Recover the list of connected components.
        List<Set<Integer>> connectedComponentsList = inspector.connectedSets();
        // Get an iterator on the list.
        Iterator<Set<Integer>> connectedComponentsListIterator = connectedComponentsList.
                iterator();

        // Create the metadata for the new table that will hold the first connected component
        Metadata md = new DefaultMetadata(
                new Type[]{
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.INT)},
                new String[]{
                    GraphSchema.ID,
                    GraphSchema.CONNECTED_COMPONENT});

        // Create a DiskBufferDriver to store the centrality indices.
        DiskBufferDriver connectedComponentsDriver =
                new DiskBufferDriver(dsf, md);

        // Record the connected components in the DiskBufferDriver.
        int connectedComponentNumber = 1;
        while (connectedComponentsListIterator.hasNext()) {
            // Get the next connected component
            Set<Integer> connectedComponent =
                    connectedComponentsListIterator.next();
            // Get an iterator on this component.
            Iterator<Integer> connectedComponentIterator =
                    connectedComponent.iterator();
            while (connectedComponentIterator.hasNext()) {
                connectedComponentsDriver.addValues(
                        new Value[]{
                            // Node ID
                            ValueFactory.createValue(
                            connectedComponentIterator.next()),
                            // Component number
                            ValueFactory.createValue(connectedComponentNumber)
                        });
            }
            connectedComponentNumber++;
        }

        // CLEAN UP - Register the table.
        connectedComponentsDriver.writingFinished();
        connectedComponentsDriver.open();
        String newTableName = dsf.getSourceManager().getUniqueName(
                GraphSchema.CONNECTED_COMPONENTS);
        dsf.getSourceManager().register(newTableName, connectedComponentsDriver.
                getFile());
    }
}
