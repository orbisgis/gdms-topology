/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV Institute as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministry
 * of Ecology and Sustainable Development.
 *
 * GDMS-Topology is distributed under GPL 3 license. It is produced by the
 * "Atelier SIG" team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR
 * 2488.
 *
 * Copyright (C) 2009-2013 IRSTV (FR CNRS 2488)
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
 * directly: info_at_orbisgis.org
 */
package org.gdms.gdmstopology.centrality;

import com.graphhopper.sna.centrality.DFSForStrahler;
import com.graphhopper.sna.data.StrahlerInfo;
import com.graphhopper.storage.Graph;
import java.util.Map;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.indexes.IndexException;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.gdmstopology.functionhelpers.TableFunctionHelper;
import org.gdms.gdmstopology.graphcreator.UnweightedGraphCreator;
import org.gdms.gdmstopology.model.GraphSchema;
import org.orbisgis.progress.ProgressMonitor;

/**
 * Helper class to calculate the Strahler numbers of the nodes of a given tree.
 *
 * @author Adam Gouge
 */
public class StrahlerAnalyzer extends TableFunctionHelper {

    /**
     * The data set.
     */
    protected final DataSet dataSet;
    /**
     * Orientation.
     */
    protected final int orientation;
    /**
     * Root node.
     */
    protected final int rootNode;
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
        GraphSchema.STRAHLER_NUMBER});
    /**
     * A logger.
     */
    protected static final org.apache.log4j.Logger LOGGER;

    /**
     * Static block to set the logger level.
     */
    static {
        LOGGER = org.apache.log4j.Logger.getLogger(GraphAnalyzer.class);
        LOGGER.setLevel(org.apache.log4j.Level.TRACE);
    }

    /**
     * Constructor.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param pm  The progress monitor used to track the progress of the
     *            calculation.
     */
    public StrahlerAnalyzer(DataSourceFactory dsf,
                            ProgressMonitor pm,
                            DataSet dataSet,
                            int orientation,
                            int rootNode) {
        super(dsf, pm);
        this.dataSet = dataSet;
        this.orientation = orientation;
        this.rootNode = rootNode;
    }

    @Override
    protected Metadata createMetadata() {
        return MD;
    }

    @Override
    protected void computeAndStoreResults(DiskBufferDriver driver) {
        Graph graph = null;
        try {
            graph = new UnweightedGraphCreator(dataSet, orientation)
                    .prepareGraph();
        } catch (IndexException ex) {
            LOGGER.trace("", ex);
        }
        DFSForStrahler dfs = null;
        try {
            dfs = new DFSForStrahler(graph,
                                     StrahlerInfo.class,
                                     rootNode);
        } catch (Exception ex) {
            LOGGER.trace("", ex);
        }

        Map<Integer, StrahlerInfo> results = null;
        if (dfs != null) {
            results = dfs.calculate();
        }
        if (results != null) {
            // TODO: Store results here.
        }
    }
}
