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
import org.gdms.gdmstopology.functionhelpers.FunctionHelper;
import org.gdms.gdmstopology.graphcreator.GraphCreator;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.model.GraphSchema;
import org.javanetworkanalyzer.data.VUCent;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.alg.ConnectivityInspector;
import org.orbisgis.progress.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of utilities to measure the connectivity of a given graph.
 *
 * @author Adam Gouge
 */
public class GraphConnectivityInspector extends FunctionHelper {

    /**
     * The edges data set.
     */
    protected final DataSet edges;
    /**
     * Metadata for
     * {@link org.gdms.gdmstopology.function.ST_ConnectedComponents}.
     */
    public static final Metadata MD = new DefaultMetadata(
            new Type[]{
        TypeFactory.createType(Type.INT),
        TypeFactory.createType(Type.INT)},
            new String[]{
        GraphSchema.ID,
        GraphSchema.CONNECTED_COMPONENT});
    /**
     * A logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(GraphConnectivityInspector.class);

    /**
     * Constructor.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param pm  The progress monitor used to track the progress of the
     *            calculation.
     */
    public GraphConnectivityInspector(DataSourceFactory dsf,
                                      ProgressMonitor pm,
                                      DataSet edges) {
        super(dsf, pm);
        this.edges = edges;
    }

    /**
     * Returns a JGraphT {@link ConnectivityInspector} on a given graph.
     *
     * @return A {@link ConnectivityInspector} on the given graph.
     */
    protected Object getConnectivityInspector() {
        KeyedGraph<VUCent, Edge> g =
                new GraphCreator<VUCent, Edge>(edges, GraphSchema.UNDIRECT,
                                               VUCent.class, Edge.class)
                .prepareGraph();
        return new ConnectivityInspector<VUCent, Edge>(
                (UndirectedGraph<VUCent, Edge>) g);
    }

    @Override
    public Metadata createMetadata() {
        return MD;
    }

    @Override
    protected void computeAndStoreResults(DiskBufferDriver driver) {
        storeResults(
                ((ConnectivityInspector<VUCent, Edge>) getConnectivityInspector()).
                connectedSets(), driver);
    }

    /**
     * Stores each node with its connected component number in the given driver.
     *
     * @param connectedSets The connected sets
     * @param driver        The driver
     */
    protected void storeResults(List<Set<VUCent>> connectedSets,
                                DiskBufferDriver driver) {
        // Record the connected components in the DiskBufferDriver.
        int connectedComponentNumber = 1;
        for (Set<VUCent> set : connectedSets) {
            for (VUCent node : set) {
                try {
                    driver.addValues(
                            new Value[]{
                        // Node ID
                        ValueFactory.createValue(node.getID()),
                        // Component number
                        ValueFactory.createValue(connectedComponentNumber)
                    });
                } catch (DriverException ex) {
                    LOGGER.error("Problem storing connected component number "
                                 + "{} for node {}.", connectedComponentNumber,
                                 node.getID());
                }
            }
            connectedComponentNumber++;
        }
    }
}
