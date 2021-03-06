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

import org.javanetworkanalyzer.data.VCent;
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
import org.gdms.gdmstopology.functionhelpers.FunctionHelper;
import org.gdms.gdmstopology.model.GraphSchema;
import org.javanetworkanalyzer.data.PathLengthData;
import org.javanetworkanalyzer.model.EdgeCent;
import org.jgrapht.Graph;
import org.orbisgis.progress.ProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Calculates network parameters such as centrality indices on the nodes of a
 * given graph and writes them to a table.
 *
 * @author Adam Gouge
 */
public abstract class GraphAnalyzer<V extends VCent, E extends EdgeCent, S extends PathLengthData>
        extends FunctionHelper {

    /**
     * The data set.
     */
    protected final DataSet dataSet;
    /**
     * Orientation.
     */
    protected final int orientation;
    /**
     * Error message when the analyzer cannot be prepared.
     */
    protected static final String ANALYZER_PREP_ERROR =
            "Could not prepare analyzer.";
    /**
     * Result metadata.
     */
    public static final Metadata MD = new DefaultMetadata(
            new Type[]{
        TypeFactory.createType(Type.INT),
        TypeFactory.createType(Type.DOUBLE),
        TypeFactory.createType(Type.DOUBLE)},
            new String[]{
        GraphSchema.ID,
        GraphSchema.BETWEENNESS_CENTRALITY,
        GraphSchema.CLOSENESS_CENTRALITY});
    private static final Logger LOGGER =
            LoggerFactory.getLogger(GraphAnalyzer.class);
    private Graph<V, E> graph;

    /**
     * Constructs a new {@link GraphAnalyzer}.
     *
     * @param dsf         The {@link DataSourceFactory} used to parse the data
     *                    set.
     * @param dataSet     The data set.
     * @param pm          The progress monitor used to track the progress of the
     *                    calculation.
     * @param orientation The orientation.
     *
     */
    public GraphAnalyzer(DataSourceFactory dsf,
                         DataSet dataSet,
                         ProgressMonitor pm,
                         int orientation) {
        super(dsf, pm);
        this.dataSet = dataSet;
        this.orientation = orientation;
    }

    /**
     * Prepares the graph analyzer.
     *
     * @return The graph analyzer.
     */
    protected abstract org.javanetworkanalyzer.analyzers.GraphAnalyzer<V, E, S> prepareAnalyzer();

    @Override
    protected Metadata createMetadata() {
        return MD;
    }

    /**
     * Returns a {@link DiskBufferDriver} for edge betweenness centrality.
     *
     * @return A {@link DiskBufferDriver} for edge betweenness centrality
     */
    public DiskBufferDriver getEdgesDriver()  {
        DiskBufferDriver edgesDriver = null;
        try {
            edgesDriver = new DiskBufferDriver(dsf, new DefaultMetadata(
                    new Type[]{
                            TypeFactory.createType(Type.INT),
                            TypeFactory.createType(Type.DOUBLE)},
                    new String[]{
                            GraphSchema.ID,
                            GraphSchema.BETWEENNESS_CENTRALITY}));
            for (E e : graph.edgeSet()) {
                edgesDriver.addValues(
                        ValueFactory.createValue(e.getID()),
                        ValueFactory.createValue(e.getBetweenness()));
            }
        } catch (DriverException e) {
            LOGGER.error("Could not initialize edges driver", e);
        }

        try {
            edgesDriver.writingFinished();
            edgesDriver.open();
        } catch (DriverException e) {
            LOGGER.error("Edges driver could not be opened.", e);
        }
        return edgesDriver;
    }

    @Override
    protected void computeAndStoreResults(
            DiskBufferDriver driver) {

        // TODO: We no longer return the results this way.
        Map<Integer, V> results = null;

        org.javanetworkanalyzer.analyzers.GraphAnalyzer<V, E, S> analyzer =
                prepareAnalyzer();
        try {
            analyzer.computeAll();
        } catch (Exception ex) {
            throw new IllegalStateException("Problem doing graph analysis.", ex);
        }

        graph = analyzer.getGraph();

        for (V node : graph.vertexSet()) {
            Value[] valuesToAdd =
                    new Value[]{
                // ID
                ValueFactory.createValue(node.getID()),
                // Betweenness
                ValueFactory.createValue(node.getBetweenness()),
                // Closeness
                ValueFactory.createValue(node.getCloseness())
            };
            try {
                driver.addValues(valuesToAdd);
            } catch (DriverException ex) {
                LOGGER.error("Problem storing centrality indices "
                        + "for node " + node.getID(), ex);
            }
        }
    }
}
