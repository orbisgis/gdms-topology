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

import java.util.Arrays;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.DIRECTED;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.EDGE_ORIENTATION_COLUMN;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.REVERSED;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.SEPARATOR;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.UNDIRECTED;
import org.gdms.gdmstopology.graphcreator.GraphCreator;
import org.gdms.gdmstopology.model.GraphSchema;
import org.javanetworkanalyzer.data.VUBetw;
import org.javanetworkanalyzer.model.Edge;
import org.javanetworkanalyzer.model.KeyedGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.orbisgis.progress.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A collection of utilities to the strongly connected components of the given
 * <b>directed</b> graph.
 *
 * @author Adam Gouge
 */
public class GraphStrongConnectivityInspector extends GraphConnectivityInspector {

    /**
     * Orientation argument value, which should be of the form
     * 'globalOrientation - edgeOrientationColumnName'.
     */
    private Value orientationArgument;
    /**
     * Global orientation string.
     */
    private String globalOrientation = null;
    /**
     * Edge orientation string.
     */
    private String edgeOrientationColumnName = null;
    /**
     * A logger.
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(GraphStrongConnectivityInspector.class);

    /**
     * Constructor.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param pm  The progress monitor used to track the progress of the
     *            calculation.
     */
    public GraphStrongConnectivityInspector(DataSourceFactory dsf,
                                            ProgressMonitor pm,
                                            DataSet edges,
                                            Value orientationArgument) {
        super(dsf, pm, edges);
        this.orientationArgument = orientationArgument;
    }

    @Override
    protected StrongConnectivityInspector<VUBetw, Edge> getConnectivityInspector() {
        return new StrongConnectivityInspector<VUBetw, Edge>(
                (DirectedGraph<VUBetw, Edge>) prepareGraph());
    }

    protected KeyedGraph<VUBetw, Edge> prepareGraph() {

        parseStringArgument(edges, orientationArgument);

        // Get the graph orientation.
        int graphType = -1;
        if (globalOrientation != null) {
            graphType = globalOrientation.equalsIgnoreCase(DIRECTED)
                    ? GraphSchema.DIRECT
                    : globalOrientation.equalsIgnoreCase(REVERSED)
                    ? GraphSchema.DIRECT_REVERSED
                    : globalOrientation.equalsIgnoreCase(UNDIRECTED)
                    ? GraphSchema.UNDIRECT
                    : -1;
        } else if (graphType == -1) {
            LOGGER.warn("Assuming a directed graph.");
            graphType = GraphSchema.DIRECT;
        }

        return new GraphCreator<VUBetw, Edge>(edges, graphType,
                                              edgeOrientationColumnName,
                                              VUBetw.class, Edge.class)
                .prepareGraph();
    }

    /**
     * Parse possible String arguments for {@link ST_ShortestPathLength}, namely
     * weight and orientation.
     *
     * @param value A given argument to parse.
     */
    private void parseStringArgument(DataSet edges, Value value) {
        if (value.getType() == Type.STRING) {
            String v = value.getAsString();
            // See if this is a directed (or reversed graph.
            if ((v.toLowerCase().contains(DIRECTED)
                 && !v.toLowerCase().contains(UNDIRECTED))
                || v.toLowerCase().contains(REVERSED)) {
                if (!v.contains(SEPARATOR)) {
                    throw new IllegalArgumentException(
                            "You must specify the name of the edge orientation "
                            + "column. Enter '" + DIRECTED + " " + SEPARATOR
                            + " " + EDGE_ORIENTATION_COLUMN + "' or '"
                            + REVERSED + " " + SEPARATOR + " "
                            + EDGE_ORIENTATION_COLUMN + "'.");
                } else {
                    // Extract the global and edge orientations.
                    String[] globalAndEdgeOrientations = v.split(SEPARATOR);
                    if (globalAndEdgeOrientations.length == 2) {
                        // And remove whitespace.
                        // TODO: Find a cleaner way to recover the orientation.
                        globalOrientation = globalAndEdgeOrientations[0]
                                .replaceAll("\\s", "");
                        edgeOrientationColumnName = globalAndEdgeOrientations[1]
                                .replaceAll("\\s", "");
                        try {
                            // Make sure this column exists.
                            if (!Arrays.asList(edges.getMetadata()
                                    .getFieldNames())
                                    .contains(edgeOrientationColumnName)) {
                                throw new IllegalArgumentException(
                                        "Column '" + edgeOrientationColumnName
                                        + "' not found in the edges table.");
                            }
                        } catch (DriverException ex) {
                            LOGGER.error("Problem verifying existence of "
                                         + "column {}.",
                                         edgeOrientationColumnName);
                        }
                        LOGGER.info(
                                "Global orientation = '{}', edge orientation "
                                + "column name = '{}'.", globalOrientation,
                                edgeOrientationColumnName);
                        // TODO: Throw an exception if no edge orientations are given.
                    } else {
                        throw new IllegalArgumentException(
                                "You must specify both global and edge orientations for "
                                + "directed or reversed graphs. Separate them by "
                                + "a '" + SEPARATOR + "'.");
                    }
                }
            } else if (v.toLowerCase().contains(UNDIRECTED)) {
                LOGGER.error("Impossible to calculate the strongly connected "
                             + "components of an undirected graph.");
            } else {
                LOGGER.error("Unrecognized orientation {}'.", v);
            }
        } else {
            throw new IllegalArgumentException("Orientation "
                                               + "must be specified as a string.");
        }
    }

    @Override
    public Metadata createMetadata() {
        return MD;
    }

    @Override
    protected void computeAndStoreResults(DiskBufferDriver driver) {
        storeResults(getConnectivityInspector().stronglyConnectedSets(),
                     driver);
    }
}
