/**
 * GDMS-Topology is a library dedicated to graph analysis. It is based on the
 * JGraphT library available at <http://www.jgrapht.org/>. It enables computing
 * and processing large graphs using spatial and alphanumeric indexes.
 *
 * This version is developed at French IRSTV institut as part of the EvalPDU
 * project, funded by the French Agence Nationale de la Recherche (ANR) under
 * contract ANR-08-VILL-0005-01 and GEBD project funded by the French Ministery
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
package org.gdms.gdmstopology.function;

import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.TopologySetUpTest;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.sql.function.FunctionException;
import org.junit.Test;
import org.orbisgis.progress.NullProgressMonitor;

/**
 * Several tests of centrality metrics.
 *
 * @author Adam Gouge
 */
public class CentralityTest extends TopologySetUpTest {

    // private static final String GRAPH_NANTES_EDGES = "nantes_1.edges";
    /**
     * Tests the closeness centrality calculation on a 2D graph with the given
     * orientation and with all edge weights equal to one.
     */
    public void testClosenessGraph2DAllWeightsOne(int graphType) throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException,
            FunctionException {
        ST_ClosenessCentrality sT_ClosenessCentrality = new ST_ClosenessCentrality();
        DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
        ds.open();
        DataSet[] tables = new DataSet[]{ds};
        String testName = "GRAPH2D ALL WEIGHTS ONE - Orientation: ";
        testName += (graphType == GraphSchema.UNDIRECT)
                ? " Undirected."
                : (graphType == GraphSchema.DIRECT)
                ? " Directed."
                : (graphType == GraphSchema.DIRECT_REVERSED)
                ? " Reversed."
                : " Invalid";
        System.out.println("\n" + testName);
        sT_ClosenessCentrality.evaluate(
                dsf,
                tables,
                new Value[]{
                    ValueFactory.createValue(1),
                    ValueFactory.createValue(graphType),},
                new NullProgressMonitor());
        ds.close();
    }

    /**
     * Tests the closeness centrality calculation on a 2D graph considered as an
     * undirected graph with all edge weights equal to one.
     *
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     * @throws DriverException
     * @throws FunctionException
     */
    @Test
    public void testClosenessGraph2DUndirectedAllWeightsOne() throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException,
            FunctionException {
        testClosenessGraph2DAllWeightsOne(GraphSchema.UNDIRECT);
    }

    /**
     * Tests the closeness centrality calculation on a 2D graph considered as a
     * directed graph with all edge weights equal to one.
     *
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     * @throws DriverException
     * @throws FunctionException
     */
    @Test
    public void testClosenessGraph2DDirectedAllWeightsOne() throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException,
            FunctionException {
        testClosenessGraph2DAllWeightsOne(GraphSchema.DIRECT);
    }

    /**
     * Tests the closeness centrality calculation on a 2D graph considered as a
     * directed graph with all edge weights equal to one and all edge
     * orientations reversed.
     *
     * @throws NoSuchTableException
     * @throws DataSourceCreationException
     * @throws DriverException
     * @throws FunctionException
     */
    @Test
    public void testClosenessGraph2DReversedAllWeightsOne() throws
            NoSuchTableException,
            DataSourceCreationException,
            DriverException,
            FunctionException {
        testClosenessGraph2DAllWeightsOne(GraphSchema.DIRECT_REVERSED);
    }
//    /**
//     * Tests the closeness centrality calculation on a Nantes with the given
//     * orientation and with all edge weights equal to one.
//     */
//    public void testClosenessNantesWeighted(int graphType) throws
//            NoSuchTableException,
//            DataSourceCreationException,
//            DriverException,
//            FunctionException {
//        ST_ClosenessCentrality sT_ClosenessCentrality = new ST_ClosenessCentrality();
//        dsf.getSourceManager().register(GRAPH_NANTES_EDGES,
//                new File(internalData + GRAPH_NANTES_EDGES + ".shp"));
//        DataSource ds = dsf.getDataSource(GRAPH_NANTES_EDGES);
//        ds.open();
//        DataSet[] tables = new DataSet[]{ds};
//        String testName = "NANTES WEIGHTED - Orientation: ";
//        testName += (graphType == GraphSchema.UNDIRECT)
//                ? " Undirected."
//                : (graphType == GraphSchema.DIRECT)
//                ? " Directed."
//                : (graphType == GraphSchema.DIRECT_REVERSED)
//                ? " Reversed."
//                : " Invalid";
//        System.out.println("\n" + testName);
//        sT_ClosenessCentrality.evaluate(
//                dsf,
//                tables,
//                new Value[]{
//                    ValueFactory.createValue("weight"),
//                    ValueFactory.createValue(graphType)},
//                new NullProgressMonitor());
//        ds.close();
//    }
//
//    /**
//     * Tests the closeness centrality calculation on Nantes considered as an
//     * undirected graph with weighted edges.
//     *
//     * @throws NoSuchTableException
//     * @throws DataSourceCreationException
//     * @throws DriverException
//     * @throws FunctionException
//     */
//    @Test
//    public void testClosenessNantesUndirectedAllWeightsOne() throws
//            NoSuchTableException,
//            DataSourceCreationException,
//            DriverException,
//            FunctionException {
//        testClosenessNantesWeighted(GraphSchema.UNDIRECT);
//    }
//    /**
//     * Tests the closeness centrality calculation on a Nantes with the given
//     * orientation and with all edge weights equal to one.
//     */
//    public void testClosenessNantesAllWeightsOne(int graphType) throws
//            NoSuchTableException,
//            DataSourceCreationException,
//            DriverException,
//            FunctionException {
//        ST_ClosenessCentrality sT_ClosenessCentrality = new ST_ClosenessCentrality();
//        dsf.getSourceManager().register(GRAPH_NANTES_EDGES, new File(internalData + GRAPH_NANTES_EDGES + ".shp"));
//        DataSource ds = dsf.getDataSource(GRAPH_NANTES_EDGES);
//        ds.open();
//        DataSet[] tables = new DataSet[]{ds};
//        String testName = "NANTES ALL WEIGHTS ONE - Orientation: ";
//        testName += (graphType == GraphSchema.UNDIRECT) ? 
//                " Undirected." 
//                : (graphType == GraphSchema.DIRECT) ? 
//                " Directed." 
//                : (graphType == GraphSchema.DIRECT_REVERSED) ? 
//                " Reversed." 
//                : " Invalid";
//        System.out.println("\n" + testName);
//        sT_ClosenessCentrality.evaluate(
//                dsf,
//                tables,
//                new Value[]{
//                    ValueFactory.createValue(1),
//                    ValueFactory.createValue(graphType)},
//                new NullProgressMonitor());
//        ds.close();
//    }
//    
//    /**
//     * Tests the closeness centrality calculation on Nantes considered as an
//     * undirected graph with all edge weights equal to one.
//     *
//     * @throws NoSuchTableException
//     * @throws DataSourceCreationException
//     * @throws DriverException
//     * @throws FunctionException
//     */
//    @Test
//    public void testClosenessNantesUndirectedAllWeightsOne() throws
//            NoSuchTableException,
//            DataSourceCreationException,
//            DriverException,
//            FunctionException {
//        testClosenessNantesAllWeightsOne(GraphSchema.UNDIRECT);
//    }
//    @Test
//    public void testPossibleArguments() throws NoSuchTableException,
//            DataSourceCreationException, DriverException, FunctionException {
//        ST_ClosenessCentrality sT_ClosenessCentrality = new ST_ClosenessCentrality();
//        DataSource ds = dsf.getDataSource(GRAPH2D_EDGES);
//        ds.open();
//        DataSet[] tables = new DataSet[]{ds};
//
//        // ONLY REQUIRED PARAMETERS
//
//        System.out.println("\nRequired Argument TEST 1: (1).\n");
//        sT_ClosenessCentrality.evaluate(
//                dsf,
//                tables,
//                new Value[]{
//                    ValueFactory.createValue(1)
//                },
//                new NullProgressMonitor());
//
////        System.out.println("\nRequired Argument TEST 2: ('length').\n");
////        sT_ClosenessCentrality = new ST_ClosenessCentrality();
////        sT_ClosenessCentrality.evaluate(
////                dsf,
////                tables,
////                new Value[]{
////                    ValueFactory.createValue("length")
////                },
////                new NullProgressMonitor());
//
//        // WITH ONE OPTIONAL PARAMETER.
//
//        System.out.println("\n1 Optional Argument TEST 1: 1 (2).\n");
//        sT_ClosenessCentrality = new ST_ClosenessCentrality();
//        sT_ClosenessCentrality.evaluate(
//                dsf,
//                tables,
//                new Value[]{
//                    ValueFactory.createValue(1),
//                    ValueFactory.createValue(GraphSchema.DIRECT_REVERSED)
//                },
//                new NullProgressMonitor());
//
//        System.out.println("\n1 Optional Argument TEST 2: 1 ('output').\n");
//        sT_ClosenessCentrality = new ST_ClosenessCentrality();
//        sT_ClosenessCentrality.evaluate(
//                dsf,
//                tables,
//                new Value[]{
//                    ValueFactory.createValue(1),
//                    ValueFactory.createValue("output")
//                },
//                new NullProgressMonitor());
//
////        System.out.println("\n1 Optional Argument TEST 3: 'length' (2).\n");
////        sT_ClosenessCentrality = new ST_ClosenessCentrality();
////        sT_ClosenessCentrality.evaluate(
////                dsf,
////                tables,
////                new Value[]{
////                    ValueFactory.createValue("length"),
////                    ValueFactory.createValue(GraphSchema.DIRECT_REVERSED)
////                },
////                new NullProgressMonitor());
//
////        System.out.println("\n1 Optional Argument TEST 6: 'length' ('output').\n");
////        sT_ClosenessCentrality = new ST_ClosenessCentrality();
////        sT_ClosenessCentrality.evaluate(
////                dsf,
////                tables,
////                new Value[]{
////                    ValueFactory.createValue("length"),
////                    ValueFactory.createValue("output")
////                },
////                new NullProgressMonitor());
//
//        // WITH TWO OPTIONAL PARAMETERS.
//
//        System.out.println("\n2 Optional Arguments TEST 1: 1 (2, 'output').\n");
//        sT_ClosenessCentrality = new ST_ClosenessCentrality();
//        sT_ClosenessCentrality.evaluate(
//                dsf,
//                tables,
//                new Value[]{
//                    ValueFactory.createValue(1),
//                    ValueFactory.createValue(GraphSchema.DIRECT_REVERSED),
//                    ValueFactory.createValue("output")
//                },
//                new NullProgressMonitor());
//
//        System.out.println("\n2 Optional Arguments TEST 2: 1 ('output', 3).\n");
//        sT_ClosenessCentrality = new ST_ClosenessCentrality();
//        sT_ClosenessCentrality.evaluate(
//                dsf,
//                tables,
//                new Value[]{
//                    ValueFactory.createValue(1),
//                    ValueFactory.createValue("output"),
//                    ValueFactory.createValue(GraphSchema.UNDIRECT)
//                },
//                new NullProgressMonitor());
//
////        System.out.println("\n2 Optional Arguments TEST 3: 'length' (3, 'output').\n");
////        sT_ClosenessCentrality = new ST_ClosenessCentrality();
////        sT_ClosenessCentrality.evaluate(
////                dsf,
////                tables,
////                new Value[]{
////                    ValueFactory.createValue("length"),
////                    ValueFactory.createValue(GraphSchema.UNDIRECT),
////                    ValueFactory.createValue("output")
////                },
////                new NullProgressMonitor());
//
////        System.out.println("\n2 Optional Arguments TEST 4: 'length' ('output', 3).\n");
////        sT_ClosenessCentrality = new ST_ClosenessCentrality();
////        sT_ClosenessCentrality.evaluate(
////                dsf,
////                tables,
////                new Value[]{
////                    ValueFactory.createValue("length"),
////                    ValueFactory.createValue("output"),
////                    ValueFactory.createValue(GraphSchema.UNDIRECT)
////                },
////                new NullProgressMonitor());
//
//        ds.close();
//    }
}
