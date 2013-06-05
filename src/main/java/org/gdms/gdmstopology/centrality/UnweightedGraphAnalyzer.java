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

import org.javanetworkanalyzer.data.VUCent;
import org.javanetworkanalyzer.progress.DefaultProgressMonitor;
import org.gdms.data.DataSourceFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import static org.gdms.gdmstopology.centrality.GraphAnalyzer.ANALYZER_PREP_ERROR;
import static org.gdms.gdmstopology.centrality.GraphAnalyzer.LOGGER;
import org.gdms.gdmstopology.graphcreator.GraphCreator;
import org.gdms.gdmstopology.model.GraphException;
import org.javanetworkanalyzer.data.UnweightedPathLengthData;
import org.javanetworkanalyzer.model.Edge;
import org.orbisgis.progress.ProgressMonitor;

/**
 * A {@link GraphAnalyzer} for unweighted graphs.
 *
 * @author Adam Gouge
 */
public class UnweightedGraphAnalyzer
        extends GraphAnalyzer<VUCent, Edge, UnweightedPathLengthData> {

    protected final String edgeOrientationColumnName;

    /**
     * Constructs a new {@link UnweightedGraphAnalyzer}.
     *
     * @param dsf         The {@link DataSourceFactory} used to parse the data
     *                    set.
     * @param dataSet     The data set.
     * @param pm          The progress monitor used to track the progress of the
     *                    calculation.
     * @param orientation The orientation.
     *
     * @throws DriverException
     * @throws GraphException
     */
    public UnweightedGraphAnalyzer(
            DataSourceFactory dsf,
            DataSet dataSet, ProgressMonitor pm,
            int orientation,
            String edgeOrientationColumnName) {
        super(dsf, dataSet, pm, orientation);
        this.edgeOrientationColumnName = edgeOrientationColumnName;
    }

    /**
     * Constructs a new {@link UnweightedGraphAnalyzer}.
     *
     * @param dsf         The {@link DataSourceFactory} used to parse the data
     *                    set.
     * @param dataSet     The data set.
     * @param pm          The progress monitor used to track the progress of the
     *                    calculation.
     * @param orientation The orientation.
     *
     * @throws DriverException
     * @throws GraphException
     */
    public UnweightedGraphAnalyzer(
            DataSourceFactory dsf,
            DataSet dataSet, ProgressMonitor pm,
            int orientation) {
        this(dsf, dataSet, pm, orientation, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected org.javanetworkanalyzer.analyzers.UnweightedGraphAnalyzer<Edge> prepareAnalyzer() {
        try {
            return new org.javanetworkanalyzer.analyzers.UnweightedGraphAnalyzer(
                    new GraphCreator(dataSet,
                                     orientation,
                                     edgeOrientationColumnName,
                                     VUCent.class,
                                     Edge.class).prepareGraph(),
                    new DefaultProgressMonitor());
        } catch (Exception ex) {
            LOGGER.trace(ANALYZER_PREP_ERROR, ex);
        }
        return null;
    }
}
