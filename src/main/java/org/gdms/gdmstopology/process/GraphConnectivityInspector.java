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
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
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
import org.gdms.gdmstopology.functionhelpers.TableFunctionHelper;
import org.gdms.gdmstopology.model.DWMultigraphDataSource;
import org.gdms.gdmstopology.model.EdgeReversedGraphDataSource;
import org.gdms.gdmstopology.model.GraphEdge;
import org.gdms.gdmstopology.model.GraphException;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.gdmstopology.model.WMultigraphDataSource;
import org.jgrapht.alg.ConnectivityInspector;
import org.orbisgis.progress.ProgressMonitor;

/**
 * A collection of utilities to measure the connectivity of a given graph.
 *
 * @author Adam Gouge
 */
public class GraphConnectivityInspector extends TableFunctionHelper {

    /**
     * The data set.
     */
    protected final DataSet dataSet;
    /**
     * Orientation.
     */
    protected final int orientation;
    /**
     * Weight column name.
     */
    private final String weightColumnName;
    /**
     * Error message when the inspector cannot be prepared.
     */
    protected static final String INSPECTOR_PREP_ERROR =
            "Could not prepare the connectivity inspector.";
    /**
     * Error message when the inspector cannot be prepared.
     */
    protected static final String ORIENTATION_ERROR =
            "Only three types of graphs "
            + "are allowed: enter 1 if the graph is "
            + "directed, 2 if it is directed and you wish to reverse the "
            + "orientation of the edges, and 3 if the graph is undirected. "
            + "If no orientation is specified, the graph is assumed "
            + "to be directed.";
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
    protected static final Logger LOGGER;

    /**
     * Static block to set the logger level.
     */
    static {
        LOGGER = Logger.getLogger(GraphConnectivityInspector.class);
        LOGGER.setLevel(Level.TRACE);
    }

    /**
     * Constructor.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param pm  The progress monitor used to track the progress of the
     *            calculation.
     */
    public GraphConnectivityInspector(DataSourceFactory dsf,
                                      ProgressMonitor pm,
                                      DataSet dataSet,
                                      int orientation,
                                      String weightColumnName) {
        super(dsf, pm);
        this.dataSet = dataSet;
        this.orientation = orientation;
        this.weightColumnName = weightColumnName;
    }

    /**
     * Returns a JGraphT {@link ConnectivityInspector} on a given graph.
     *
     * @return A {@link ConnectivityInspector} on the given graph.
     *
     * @throws DriverException
     * @throws GraphException
     */
    private ConnectivityInspector<Integer, GraphEdge> getConnectivityInspector()
            throws DriverException, GraphException {
        // Return a connectivity inspector according to the graph type.
        if (orientation == GraphSchema.DIRECT) {
            DWMultigraphDataSource dWMultigraphDataSource =
                    new DWMultigraphDataSource(dsf, dataSet, pm);
            dWMultigraphDataSource.setWeightFieldIndex(weightColumnName);
            return new ConnectivityInspector(dWMultigraphDataSource);
        } else if (orientation == GraphSchema.DIRECT_REVERSED) {
            DWMultigraphDataSource dWMultigraphDataSource =
                    new DWMultigraphDataSource(dsf, dataSet, pm);
            dWMultigraphDataSource.setWeightFieldIndex(weightColumnName);
            EdgeReversedGraphDataSource edgeReversedGraph =
                    new EdgeReversedGraphDataSource(dWMultigraphDataSource);
            return new ConnectivityInspector(edgeReversedGraph);
        } else if (orientation == GraphSchema.UNDIRECT) {
            WMultigraphDataSource wMultigraphDataSource =
                    new WMultigraphDataSource(dsf, dataSet, pm);
            wMultigraphDataSource.setWeightFieldIndex(weightColumnName);
            return new ConnectivityInspector(wMultigraphDataSource);
        } else {
            throw new GraphException(ORIENTATION_ERROR);
        }
    }

    @Override
    public Metadata createMetadata() {
        return MD;
    }

    @Override
    protected void computeAndStoreResults(DiskBufferDriver driver) {

        ConnectivityInspector<Integer, GraphEdge> inspector = null;
        try {
            inspector = getConnectivityInspector();
        } catch (Exception ex) {
            LOGGER.trace(INSPECTOR_PREP_ERROR, ex);
        }

        if (inspector != null) {

            int connectedComponentNumber = 1;

            // Record the connected components in the DiskBufferDriver.
            for (Iterator<Set<Integer>> it =
                    inspector.connectedSets().iterator();
                    it.hasNext();) {
                try {
                    for (Iterator<Integer> cc =
                            it.next().iterator();
                            cc.hasNext();) {
                        driver.addValues(
                                new Value[]{
                            // Node ID
                            ValueFactory.createValue(
                            cc.next()),
                            // Component number
                            ValueFactory.createValue(connectedComponentNumber)
                        });
                    }
                } catch (Exception ex) {
                    LOGGER.trace(STORAGE_ERROR, ex);
                }
                connectedComponentNumber++;
            }
        } else {
            LOGGER.trace("Null inspector.");
        }
    }
}
