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

import org.gdms.data.DataSource;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.TopologySetupTest;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.sql.function.FunctionException;
import org.junit.Test;
import org.orbisgis.progress.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Adam Gouge
 */
public class ST_GraphAnalysisTest extends TopologySetupTest {

    private static final String LENGTH = "length";
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ST_GraphAnalysisTest.class);

    @Test
    public void unweightedDirectedTest() {
        try {
            evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                     ValueFactory.createValue(1),
                     GraphSchema.DIRECT);
        } catch (Exception ex) {
        }
        //        if (CHECK_RESULTS) {
        //            checkBetweenness(new double[]{
        //                1.0, 0.0, 1.0,
        //                0.0, 0.0, 0.6666666666666666}, graph);
        //            checkCloseness(new double[]{
        //                0.0, 0.4166666666666667, 0.0,
        //        }
        //        }
    }

    @Test
    public void unweightedReversedTest() {
        try {
            evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                     ValueFactory.createValue(1),
                     GraphSchema.DIRECT_REVERSED);
        } catch (Exception ex) {
        }
//        if (CHECK_RESULTS) {
//            checkBetweenness(new double[]{
//                0.375, 0.0, 1.0,
//                0.0, 0.0, 1.0}, graph);
//            checkCloseness(new double[]{
//                0.0, 0.0, 0.0,
//                0.0, 0.0, 0.0}, graph);
//        }
    }

    @Test
    public void unweightedUndirectedTest() throws DriverException {
        try {
            evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                     ValueFactory.createValue(1),
                     GraphSchema.UNDIRECT);
        } catch (Exception ex) {
        }
//        if (CHECK_RESULTS) {
//            checkBetweenness(new double[]{
//                0.5, 0.0, 1.0,
//                0.0, 0.0, 0.75}, graph);
//            checkCloseness(new double[]{
//                0.5, 0.4166666666666667, 0.625,}
//        }
    }

    @Test
    public void weightedDirectedTest() {
        try {
            evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                     ValueFactory.createValue(LENGTH),
                     GraphSchema.DIRECT);
        } catch (Exception ex) {
        }
//        if (CHECK_RESULTS) {
//            checkBetweenness(new double[]{
//                0.75, 0.0, 1.0,
//                0.0, 0.0, 1.0}, graph);
//            checkCloseness(new double[]{
//                0.0, 0.0035327735482214143, 0.0,
//                0.0, 0.0, 0.0}, graph);
//        }
    }

    @Test
    public void weightedReversedTest() {
        try {
            evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                     ValueFactory.createValue(LENGTH),
                     GraphSchema.DIRECT_REVERSED);
        } catch (Exception ex) {
        }
//        if (CHECK_RESULTS) {
//            checkBetweenness(new double[]{
//                0.75, 0.0, 1.0,
//                0.0, 0.0, 1.0}, graph);
//            checkCloseness(new double[]{
//                0.0, 0.0, 0.0,
//                0.0, 0.0, 0.0}, graph);
//        }
    }

    @Test
    public void weightedUndirectedTest() {
        try {
            evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                     ValueFactory.createValue(LENGTH),
                     GraphSchema.UNDIRECT);
        } catch (Exception ex) {
        }
//        if (CHECK_RESULTS) {
//            checkBetweenness(new double[]{
//                0.5714285714285714, 0.0, 1.0,
//                0.0, 0.0, 0.8571428571428571}, graph);
//
//            checkCloseness(new double[]{
//                0.003787491035823884, 0.0035327735482214143, 0.0055753940798198886,
//                0.0032353723348164448, 0.003495002741097083, 0.0055753940798198886
//            }, graph);
//        }
    }

    private void evaluate(DataSource ds, Value weight, int graphType) {
        String orientation = "Orientation: ";
        orientation += (graphType == GraphSchema.UNDIRECT)
                ? " Undirected."
                : (graphType == GraphSchema.DIRECT)
                ? " Directed."
                : (graphType == GraphSchema.DIRECT_REVERSED)
                ? " Reversed."
                : " Invalid";
        LOGGER.debug(orientation);
        try {
            ds.open();
        } catch (DriverException ex) {
            LOGGER.info("Could not open datasource.");
        }
        DataSet[] tables = new DataSet[]{ds};
        try {
            new ST_GraphAnalysis().evaluate(
                    dsf,
                    tables,
                    new Value[]{weight,
                ValueFactory.createValue(graphType)},
                    new NullProgressMonitor());
        } catch (FunctionException ex) {
            LOGGER.error("Impossible to perform graph analysis.");
        }
        try {
            ds.close();
        } catch (DriverException ex) {
            LOGGER.info("Could not close datasource.");
        }
    }
}
