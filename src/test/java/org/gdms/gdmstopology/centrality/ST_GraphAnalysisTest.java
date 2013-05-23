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
import static org.junit.Assert.assertEquals;

/**
 * Tests {@link ST_GraphAnalysis} on a 2D graph for all combinations of
 * (un)weighted, (un)directed/reversed.
 *
 * @author Adam Gouge
 */
public class ST_GraphAnalysisTest extends TopologySetupTest {

    private static final String LENGTH = "length";
    private static final String UNWEIGHTED_NAME =
            "output.unweighted.graph_analysis";
    private static final String WEIGHTED_NAME =
            "output.weighted.graph_analysis";
    private static final double TOLERANCE = 0.0;
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ST_GraphAnalysisTest.class);

    /**
     * Evaluates {@link ST_GraphAnalysis} on the given datasource considered as
     * a(n) (un)weighted graph with the given edge orientation.
     *
     * @param ds          Datasource
     * @param weight      1 or weight column name
     * @param orientation Orientation
     *
     * @see GraphSchema.DIRECT, GraphSchema.DIRECT_REVERSED,
     * GraphSchema.UNDIRECT
     */
    private DataSet evaluate(DataSource ds, Value weight, int orientation) {
        try {
            ds.open();
        } catch (DriverException ex) {
            LOGGER.info("Could not open datasource.");
        }
        DataSet[] tables = new DataSet[]{ds};
        DataSet result = null;
        try {
            result = new ST_GraphAnalysis()
                    .evaluate(dsf,
                              tables,
                              new Value[]{weight,
                ValueFactory.createValue(orientation)
            },
                              new NullProgressMonitor());
        } catch (FunctionException ex) {
            LOGGER.error("Impossible to perform graph analysis.");
        }
        try {
            ds.close();
        } catch (DriverException ex) {
            LOGGER.info("Could not close datasource.");
        }
        return result;
    }

    @Test
    public void unweightedDirectedTest() throws DriverException {
        DataSet result = null;
        try {
            result = evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                              ValueFactory.createValue(1),
                              GraphSchema.DIRECT);
        } catch (Exception ex) {
        }
        // CHECK RESULTS
        if (result != null) {
            try {
                for (int i = 0; i < result.getRowCount(); i++) {
                    Value[] row = result.getRow(i);
                    int id = row[0].getAsInt();
                    double betweenness = row[1].getAsDouble();
                    double closeness = row[2].getAsDouble();
                    if (id == 2 || id == 4 || id == 5) {
                        assertEquals(0.0, betweenness, TOLERANCE);
                    } else if (id == 1 || id == 3) {
                        assertEquals(1.0, betweenness, TOLERANCE);
                    } else if (id == 6) {
                        assertEquals(0.6666666666666666, betweenness, TOLERANCE);
                    } else {
                        LOGGER.error("Unexpected vertex {}", id);
                    }
                    if (id == 1 || id == 3 || id == 4 || id == 5 || id == 6) {
                        assertEquals(0.0, closeness, TOLERANCE);
                    } else if (id == 2) {
                        assertEquals(0.4166666666666667, closeness, TOLERANCE);
                    } else {
                        LOGGER.error("Unexpected vertex {}", id);
                    }
                }
            } catch (DriverException ex) {
                LOGGER.error("Problem checking results.");
            }
        } else {
            throw new IllegalStateException("Null results!");
        }
    }

    @Test
    public void unweightedReversedTest() {
        DataSet result = null;
        try {
            result = evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                              ValueFactory.createValue(1),
                              GraphSchema.DIRECT_REVERSED);
        } catch (Exception ex) {
        }
        // CHECK RESULTS
        if (result != null) {
            try {
                for (int i = 0; i < result.getRowCount(); i++) {
                    Value[] row = result.getRow(i);
                    int id = row[0].getAsInt();
                    double betweenness = row[1].getAsDouble();
                    double closeness = row[2].getAsDouble();
                    if (id == 2 || id == 4 || id == 5) {
                        assertEquals(0.0, betweenness, TOLERANCE);
                    } else if (id == 3 || id == 6) {
                        assertEquals(1.0, betweenness, TOLERANCE);
                    } else if (id == 1) {
                        assertEquals(0.375, betweenness, TOLERANCE);
                    } else {
                        LOGGER.error("Unexpected vertex {}", id);
                    }
                    // All vertices have closeness zero.
                    assertEquals(0.0, closeness, TOLERANCE);
                }
            } catch (DriverException ex) {
                LOGGER.error("Problem checking results.");
            }
        } else {
            throw new IllegalStateException("Null results!");
        }
    }

    @Test
    public void unweightedUndirectedTest() {
        DataSet result = null;
        try {
            result = evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                              ValueFactory.createValue(1),
                              GraphSchema.UNDIRECT);
        } catch (Exception ex) {
        }
        // CHECK RESULTS
        if (result != null) {
            try {
                for (int i = 0; i < result.getRowCount(); i++) {
                    Value[] row = result.getRow(i);
                    int id = row[0].getAsInt();
                    double betweenness = row[1].getAsDouble();
                    double closeness = row[2].getAsDouble();
                    if (id == 2 || id == 4 || id == 5) {
                        assertEquals(0.0, betweenness, TOLERANCE);
                    } else if (id == 3) {
                        assertEquals(1.0, betweenness, TOLERANCE);
                    } else if (id == 6) {
                        assertEquals(0.75, betweenness, TOLERANCE);
                    } else if (id == 1) {
                        assertEquals(0.5, betweenness, TOLERANCE);
                    } else {
                        LOGGER.error("Unexpected vertex {}", id);
                    }
                    if (id == 3 || id == 6) {
                        assertEquals(0.625, closeness, TOLERANCE);
                    } else if (id == 2 || id == 5) {
                        assertEquals(0.4166666666666667, closeness, TOLERANCE);
                    } else if (id == 1) {
                        assertEquals(0.5, closeness, TOLERANCE);
                    } else if (id == 4) {
                        assertEquals(0.35714285714285715, closeness, TOLERANCE);
                    } else {
                        LOGGER.error("Unexpected vertex {}", id);
                    }
                }
            } catch (DriverException ex) {
                LOGGER.error("Problem checking results.");
            }
        } else {
            throw new IllegalStateException("Null results!");
        }
    }

    @Test
    public void weightedDirectedTest() {
        DataSet result = null;
        try {
            result = evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                              ValueFactory.createValue(LENGTH),
                              GraphSchema.DIRECT);
        } catch (Exception ex) {
        }
        // CHECK RESULTS
        if (result != null) {
            try {
                for (int i = 0; i < result.getRowCount(); i++) {
                    Value[] row = result.getRow(i);
                    int id = row[0].getAsInt();
                    double betweenness = row[1].getAsDouble();
                    double closeness = row[2].getAsDouble();
                    if (id == 2 || id == 4 || id == 5) {
                        assertEquals(0.0, betweenness, TOLERANCE);
                    } else if (id == 3 || id == 6) {
                        assertEquals(1.0, betweenness, TOLERANCE);
                    } else if (id == 1) {
                        assertEquals(0.75, betweenness, TOLERANCE);
                    } else {
                        LOGGER.error("Unexpected vertex {}", id);
                    }
                    if (id == 1 || id == 3 || id == 4 || id == 5 || id == 6) {
                        assertEquals(0.0, closeness, TOLERANCE);
                    } else if (id == 2) {
                        assertEquals(0.0035327735482214143, closeness, TOLERANCE);
                    } else {
                        LOGGER.error("Unexpected vertex {}", id);
                    }
                }
            } catch (DriverException ex) {
                LOGGER.error("Problem checking results.");
            }
        } else {
            throw new IllegalStateException("Null results!");
        }
    }

    @Test
    public void weightedReversedTest() {
        DataSet result = null;
        try {
            result = evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                              ValueFactory.createValue(LENGTH),
                              GraphSchema.DIRECT_REVERSED);
        } catch (Exception ex) {
        }
        // CHECK RESULTS
        if (result != null) {
            try {
                for (int i = 0; i < result.getRowCount(); i++) {
                    Value[] row = result.getRow(i);
                    int id = row[0].getAsInt();
                    double betweenness = row[1].getAsDouble();
                    double closeness = row[2].getAsDouble();
                    if (id == 2 || id == 4 || id == 5) {
                        assertEquals(0.0, betweenness, TOLERANCE);
                    } else if (id == 3 || id == 6) {
                        assertEquals(1.0, betweenness, TOLERANCE);
                    } else if (id == 1) {
                        assertEquals(0.75, betweenness, TOLERANCE);
                    } else {
                        LOGGER.error("Unexpected vertex {}", id);
                    }
                    // All vertices have closeness zero.
                    assertEquals(0.0, closeness, TOLERANCE);
                }
            } catch (DriverException ex) {
                LOGGER.error("Problem checking results.");
            }
        } else {
            throw new IllegalStateException("Null results!");
        }
    }

    @Test
    public void weightedUndirectedTest() {
        DataSet result = null;
        try {
            result = evaluate(dsf.getDataSource(GRAPH2D_EDGES),
                              ValueFactory.createValue(LENGTH),
                              GraphSchema.UNDIRECT);
        } catch (Exception ex) {
        }
        // CHECK RESULTS
        if (result != null) {
            try {
                for (int i = 0; i < result.getRowCount(); i++) {
                    Value[] row = result.getRow(i);
                    int id = row[0].getAsInt();
                    double betweenness = row[1].getAsDouble();
                    double closeness = row[2].getAsDouble();
                    if (id == 2 || id == 4 || id == 5) {
                        assertEquals(0.0, betweenness, TOLERANCE);
                    } else if (id == 3) {
                        assertEquals(1.0, betweenness, TOLERANCE);
                    } else if (id == 1) {
                        assertEquals(0.5714285714285714, betweenness, TOLERANCE);
                    } else if (id == 6) {
                        assertEquals(0.8571428571428571, betweenness, TOLERANCE);
                    } else {
                        LOGGER.error("Unexpected vertex {}", id);
                    }
                    if (id == 3 || id == 6) {
                        assertEquals(0.0055753940798198886, closeness, TOLERANCE);
                    } else if (id == 1) {
                        assertEquals(0.003787491035823884, closeness, TOLERANCE);
                    } else if (id == 2) {
                        assertEquals(0.0035327735482214143, closeness, TOLERANCE);
                    } else if (id == 4) {
                        assertEquals(0.0032353723348164448, closeness, TOLERANCE);
                    } else if (id == 5) {
                        assertEquals(0.003495002741097083, closeness, TOLERANCE);
                    } else {
                        LOGGER.error("Unexpected vertex {}", id);
                    }
                }
            } catch (DriverException ex) {
                LOGGER.error("Problem checking results.");
            }
        } else {
            throw new IllegalStateException("Null results!");
        }
    }
}
