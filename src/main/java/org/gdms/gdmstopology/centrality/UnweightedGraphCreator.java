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

import com.graphhopper.storage.Graph;
import java.util.Iterator;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;

/**
 * Creates an unweighted graph with a specified orientation from the given
 * {@link DataSet}.
 *
 * @author Adam Gouge
 */
public class UnweightedGraphCreator extends GraphCreator {

    /**
     * Used to make sure all edges receive a weight of exactly 1 when
     * appropriate.
     */
    private final static double ALL_WEIGHTS_ONE = 1.0;

    /**
     * Constructs a new {@link UnweightedGraphCreator}.
     *
     * @param dataSet     The data set.
     * @param orientation The orientation.
     *
     */
    public UnweightedGraphCreator(DataSet dataSet, int orientation) {
        super(dataSet, orientation);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getWeightColumnName() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadDirectedEdges(Graph graph,
                                     int startNodeIndex,
                                     int endNodeIndex,
                                     int weightFieldIndex) {
        // The weightFieldIndex is ignored.
        Iterator<Value[]> iterator = dataSet.iterator();
        while (iterator.hasNext()) {
            Value[] row = iterator.next();
            graph.edge(row[startNodeIndex].getAsInt(),
                       row[endNodeIndex].getAsInt(),
                       ALL_WEIGHTS_ONE,
                       false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void loadUndirectedEdges(Graph graph,
                                       int startNodeIndex,
                                       int endNodeIndex,
                                       int weightFieldIndex) {
        // The weightFieldIndex is ignored.
        Iterator<Value[]> iterator = dataSet.iterator();
        while (iterator.hasNext()) {
            Value[] row = iterator.next();
            graph.edge(row[startNodeIndex].getAsInt(),
                       row[endNodeIndex].getAsInt(),
                       ALL_WEIGHTS_ONE,
                       true);
        }
    }
}
