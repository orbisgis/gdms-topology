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

import com.vividsolutions.jts.io.ParseException;
import org.gdms.data.DataSource;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.gdmstopology.TopologySetupTest;
import org.gdms.gdmstopology.function.ST_Graph;
import org.gdms.gdmstopology.function.ST_ShortestPathLength;
import org.gdms.gdmstopology.graphcreator.GraphCreator;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.sql.function.FunctionException;
import org.junit.Test;
import org.orbisgis.progress.NullProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 * Tests {@link ST_Accessibility} on the Cormen graph under all possible
 * configurations.
 *
 * @author Adam Gouge
 */
public class ST_AccessibilityTest extends TopologySetupTest {

    private static final double TOLERANCE = 0.0;
    private static final Logger LOGGER =
            LoggerFactory.getLogger(ST_AccessibilityTest.class);

    @Test
    public void testWD() throws Exception {
        DataSet results = weightedAnalysis(
                GraphSchema.WEIGHT,
                "directed - " + GraphSchema.EDGE_ORIENTATION);
        // Check results.
        Metadata md = results.getMetadata();
        int idIndex = md.getFieldIndex(GraphSchema.ID);
        int closestDestIndex = md.getFieldIndex(GraphSchema.CLOSEST_DESTINATION);
        int distToClosestIndex = md.getFieldIndex(
                GraphSchema.DIST_TO_CLOSEST_DESTINATION);
        for (int i = 0; i < results.getRowCount(); i++) {
            Value[] row = results.getRow(i);
            int id = row[idIndex].getAsInt();
            int closestDest = row[closestDestIndex].getAsInt();
            double distToClosest = row[distToClosestIndex].getAsDouble();
            if (id == 1) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 5.0, TOLERANCE);
            } else if (id == 2) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 2.0, TOLERANCE);
            } else if (id == 3) {
                assertEquals(closestDest, 5);
                assertEquals(distToClosest, 4.0, TOLERANCE);
            } else if (id == 4 || id == 5) {
                assertEquals(closestDest, id);
                assertEquals(distToClosest, 0.0, TOLERANCE);
            }
        }
    }

    @Test
    public void testWR() throws Exception {
        DataSet results = weightedAnalysis(
                GraphSchema.WEIGHT,
                "reversed - " + GraphSchema.EDGE_ORIENTATION);
        // Check results.
        Metadata md = results.getMetadata();
        int idIndex = md.getFieldIndex(GraphSchema.ID);
        int closestDestIndex = md.getFieldIndex(GraphSchema.CLOSEST_DESTINATION);
        int distToClosestIndex = md.getFieldIndex(
                GraphSchema.DIST_TO_CLOSEST_DESTINATION);
        for (int i = 0; i < results.getRowCount(); i++) {
            Value[] row = results.getRow(i);
            int id = row[idIndex].getAsInt();
            int closestDest = row[closestDestIndex].getAsInt();
            double distToClosest = row[distToClosestIndex].getAsDouble();
            if (id == 1) {
                assertEquals(closestDest, 5);
                assertEquals(distToClosest, 7.0, TOLERANCE);
            } else if (id == 2) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 3.0, TOLERANCE);
            } else if (id == 3) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 4.0, TOLERANCE);
            } else if (id == 4 || id == 5) {
                assertEquals(closestDest, id);
                assertEquals(distToClosest, 0.0, TOLERANCE);
            }
        }
    }

    @Test
    public void testWU() throws Exception {
        DataSet results = weightedAnalysis(GraphSchema.WEIGHT, "undirected");
        // Check results.
        Metadata md = results.getMetadata();
        int idIndex = md.getFieldIndex(GraphSchema.ID);
        int closestDestIndex = md.getFieldIndex(GraphSchema.CLOSEST_DESTINATION);
        int distToClosestIndex = md.getFieldIndex(
                GraphSchema.DIST_TO_CLOSEST_DESTINATION);
        for (int i = 0; i < results.getRowCount(); i++) {
            Value[] row = results.getRow(i);
            int id = row[idIndex].getAsInt();
            int closestDest = row[closestDestIndex].getAsInt();
            double distToClosest = row[distToClosestIndex].getAsDouble();
            if (id == 1) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 5.0, TOLERANCE);
            } else if (id == 2) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 2.0, TOLERANCE);
            } else if (id == 3) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 3.0, TOLERANCE);
            } else if (id == 4 || id == 5) {
                assertEquals(closestDest, id);
                assertEquals(distToClosest, 0.0, TOLERANCE);
            }
        }
    }

    @Test
    public void testD() throws Exception {
        DataSet results = unweightedAnalysis(
                "directed - " + GraphSchema.EDGE_ORIENTATION);
        // Check results.
        Metadata md = results.getMetadata();
        int idIndex = md.getFieldIndex(GraphSchema.ID);
        int closestDestIndex = md.getFieldIndex(GraphSchema.CLOSEST_DESTINATION);
        int distToClosestIndex = md.getFieldIndex(
                GraphSchema.DIST_TO_CLOSEST_DESTINATION);
        for (int i = 0; i < results.getRowCount(); i++) {
            Value[] row = results.getRow(i);
            int id = row[idIndex].getAsInt();
            int closestDest = row[closestDestIndex].getAsInt();
            double distToClosest = row[distToClosestIndex].getAsDouble();
            if (id == 1) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 1.0, TOLERANCE);
            } else if (id == 2) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 1.0, TOLERANCE);
            } else if (id == 3) {
                assertEquals(closestDest, 5);
                assertEquals(distToClosest, 1.0, TOLERANCE);
            } else if (id == 4 || id == 5) {
                assertEquals(closestDest, id);
                assertEquals(distToClosest, 0.0, TOLERANCE);
            }
        }
    }

    @Test
    public void testR() throws Exception {
        DataSet results = unweightedAnalysis(
                "reversed - " + GraphSchema.EDGE_ORIENTATION);
        // Check results.
        Metadata md = results.getMetadata();
        int idIndex = md.getFieldIndex(GraphSchema.ID);
        int closestDestIndex = md.getFieldIndex(GraphSchema.CLOSEST_DESTINATION);
        int distToClosestIndex = md.getFieldIndex(
                GraphSchema.DIST_TO_CLOSEST_DESTINATION);
        for (int i = 0; i < results.getRowCount(); i++) {
            Value[] row = results.getRow(i);
            int id = row[idIndex].getAsInt();
            int closestDest = row[closestDestIndex].getAsInt();
            double distToClosest = row[distToClosestIndex].getAsDouble();
            if (id == 1) {
                assertEquals(closestDest, 5);
                assertEquals(distToClosest, 1.0, TOLERANCE);
            } else if (id == 2) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 1.0, TOLERANCE);
            } else if (id == 3) {
                if (closestDest != 4) {
                    assertEquals(closestDest, 5);
                }
                assertEquals(distToClosest, 1.0, TOLERANCE);
            } else if (id == 4 || id == 5) {
                assertEquals(closestDest, id);
                assertEquals(distToClosest, 0.0, TOLERANCE);
            }
        }
    }

    @Test
    public void testU() throws Exception {
        DataSet results = unweightedAnalysis("undirected");
        // Check results.
        Metadata md = results.getMetadata();
        int idIndex = md.getFieldIndex(GraphSchema.ID);
        int closestDestIndex = md.getFieldIndex(GraphSchema.CLOSEST_DESTINATION);
        int distToClosestIndex = md.getFieldIndex(
                GraphSchema.DIST_TO_CLOSEST_DESTINATION);
        for (int i = 0; i < results.getRowCount(); i++) {
            Value[] row = results.getRow(i);
            int id = row[idIndex].getAsInt();
            int closestDest = row[closestDestIndex].getAsInt();
            double distToClosest = row[distToClosestIndex].getAsDouble();
            if (id == 1) {
                if (closestDest != 4) {
                    assertEquals(closestDest, 5);
                }
                assertEquals(distToClosest, 1.0, TOLERANCE);
            } else if (id == 2) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 1.0, TOLERANCE);
            } else if (id == 3) {
                if (closestDest != 4) {
                    assertEquals(closestDest, 5);
                }
                assertEquals(distToClosest, 1.0, TOLERANCE);
            } else if (id == 4 || id == 5) {
                assertEquals(closestDest, id);
                assertEquals(distToClosest, 0.0, TOLERANCE);
            }
        }
    }

    @Test
    public void testDestinationTable() throws Exception {
        // Prepare the destination table.
        MemoryDataSetDriver destTable = new MemoryDataSetDriver(
                new String[]{
            ST_ShortestPathLength.DESTINATION},
                new Type[]{TypeFactory.createType(Type.INT)});
        destTable.addValues(new Value[]{ValueFactory.createValue(4)});
        destTable.addValues(new Value[]{ValueFactory.createValue(5)});

        DataSet[] tables = new DataSet[]{prepareEdges(), destTable};
        
        DataSet results = new ST_Accessibility()
                                   .evaluate(dsf,
                                             tables,
                                             new Value[]{
                               ValueFactory.createValue(GraphSchema.WEIGHT),
                               ValueFactory.createValue(
                               "directed - " + GraphSchema.EDGE_ORIENTATION)},
                                             new NullProgressMonitor());
        // Check results.
        Metadata md = results.getMetadata();
        int idIndex = md.getFieldIndex(GraphSchema.ID);
        int closestDestIndex = md.getFieldIndex(GraphSchema.CLOSEST_DESTINATION);
        int distToClosestIndex = md.getFieldIndex(
                GraphSchema.DIST_TO_CLOSEST_DESTINATION);
        for (int i = 0; i < results.getRowCount(); i++) {
            Value[] row = results.getRow(i);
            int id = row[idIndex].getAsInt();
            int closestDest = row[closestDestIndex].getAsInt();
            double distToClosest = row[distToClosestIndex].getAsDouble();
            if (id == 1) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 5.0, TOLERANCE);
            } else if (id == 2) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 2.0, TOLERANCE);
            } else if (id == 3) {
                assertEquals(closestDest, 5);
                assertEquals(distToClosest, 4.0, TOLERANCE);
            } else if (id == 4 || id == 5) {
                assertEquals(closestDest, id);
                assertEquals(distToClosest, 0.0, TOLERANCE);
            }
        }
    }
    
    @Test
    public void testOmittedOrientation() throws Exception {
        // Prepare the destination table.
        MemoryDataSetDriver destTable = new MemoryDataSetDriver(
                new String[]{
            ST_ShortestPathLength.DESTINATION},
                new Type[]{TypeFactory.createType(Type.INT)});
        destTable.addValues(new Value[]{ValueFactory.createValue(4)});
        destTable.addValues(new Value[]{ValueFactory.createValue(5)});

        DataSet[] tables = new DataSet[]{prepareEdges(), destTable};
        
        DataSet results = new ST_Accessibility()
                                   .evaluate(dsf,
                                             tables,
                                             new Value[]{
                               ValueFactory.createValue(GraphSchema.WEIGHT)},
                                             new NullProgressMonitor());
        // Check results.
        Metadata md = results.getMetadata();
        int idIndex = md.getFieldIndex(GraphSchema.ID);
        int closestDestIndex = md.getFieldIndex(GraphSchema.CLOSEST_DESTINATION);
        int distToClosestIndex = md.getFieldIndex(
                GraphSchema.DIST_TO_CLOSEST_DESTINATION);
        for (int i = 0; i < results.getRowCount(); i++) {
            Value[] row = results.getRow(i);
            int id = row[idIndex].getAsInt();
            int closestDest = row[closestDestIndex].getAsInt();
            double distToClosest = row[distToClosestIndex].getAsDouble();
            if (id == 1) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 5.0, TOLERANCE);
            } else if (id == 2) {
                assertEquals(closestDest, 4);
                assertEquals(distToClosest, 2.0, TOLERANCE);
            } else if (id == 3) {
                assertEquals(closestDest, 5);
                assertEquals(distToClosest, 4.0, TOLERANCE);
            } else if (id == 4 || id == 5) {
                assertEquals(closestDest, id);
                assertEquals(distToClosest, 0.0, TOLERANCE);
            }
        }
    }

    private DataSet weightedAnalysis(String weight, String orientation)
            throws Exception {
        return new ST_Accessibility()
                .evaluate(dsf,
                          new DataSet[]{prepareEdges()},
                          new Value[]{ValueFactory.createValue("4, 5"),
                                      ValueFactory.createValue(weight),
                                      ValueFactory.createValue(orientation)},
                          new NullProgressMonitor());
    }

    private DataSet unweightedAnalysis(String orientation)
            throws Exception {
        return new ST_Accessibility()
                .evaluate(dsf,
                          new DataSet[]{prepareEdges()},
                          new Value[]{ValueFactory.createValue("4, 5"),
                                      ValueFactory.createValue(orientation)},
                          new NullProgressMonitor());
    }

    public DataSet prepareEdges() throws FunctionException,
            DriverException,
            DataSourceCreationException, NoSuchTableException, ParseException {
        //                   1
        //           >2 ------------>3
        //          / |^           ->|^
        //       10/ / |      9   / / |
        //        / 2| |3    -----  | |
        //       /   | |    /      4| |6
        //      1<---------------   | |
        //       \   | |  /     7\  | |
        //       5\  | / /        \ | /
        //         \ v| /    2     \v|
        //          > 4 -----------> 5
        final MemoryDataSetDriver data =
                new MemoryDataSetDriver(
                new String[]{"the_geom"},
                new Type[]{TypeFactory.createType(Type.GEOMETRY)});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(1 2, 2 3)"))});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(2 3, 4 3)"))});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(2 3, 2 1)"))});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(2 1, 4 1)"))});
        // I had forgotten this edge in ST_ShortestPathLengthTest.
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(2 1, 4 3)"))});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(2 1, 2 3)"))});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(4 3, 4 1)"))});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(4 1, 4 3)"))});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(1 2, 2 1)"))});
        data.addValues(new Value[]{
            ValueFactory.createValue(
            wktReader.read("LINESTRING(4 1, 1 2)"))});

        DataSet[] tables = new DataSet[]{data};

        LOGGER.debug("\tDATA");
//        print(data);

        // Evaluate ST_Graph.
        new ST_Graph().evaluate(dsf,
                                tables,
                                // Tolerance, orient by elevation, output
                                new Value[]{ValueFactory.createValue(0),
                                            ValueFactory.createValue(false),
                                            ValueFactory.createValue("output")},
                                new NullProgressMonitor());

        // Check the nodes table.
        DataSource nodes = dsf.getDataSource("output.nodes");
        nodes.open();
        LOGGER.debug("\tNODES");
//        print(nodes);
        nodes.close();

        // Check the edges table.
        DataSource edges = dsf.getDataSource("output.edges");
        edges.open();
        LOGGER.debug("\tEDGES");
//        print(edges);

        // Add the default orientation (directed) to each edge.
        DefaultMetadata orientedMD = new DefaultMetadata(edges.getMetadata());
        orientedMD.addField(GraphSchema.EDGE_ORIENTATION,
                            TypeFactory.createType(Type.INT));
        MemoryDataSetDriver orientedEdges =
                new MemoryDataSetDriver(orientedMD);
        int orientIndex = orientedMD.getFieldIndex(GraphSchema.EDGE_ORIENTATION);
        for (int i = 0; i < edges.getRowCount(); i++) {
            Value[] oldRow = edges.getRow(i);
            final Value[] newRow = new Value[orientedMD.getFieldCount()];
            System.arraycopy(oldRow, 0, newRow, 0,
                             orientIndex);
            newRow[orientIndex] =
                    ValueFactory.createValue(GraphCreator.DIRECTED_EDGE);
            orientedEdges.addValues(newRow);
        }
        LOGGER.debug("\tORIENTED EDGES");
//        print(orientedEdges);

        // Add the edge weights
        double[] edgeWeights =
                new double[]{10.0, 1.0, 2.0, 2.0, 9.0, 3.0, 4.0, 6.0, 5.0, 7.0};
        DefaultMetadata weightedMD = new DefaultMetadata(orientedMD);
        weightedMD.addField(GraphSchema.WEIGHT,
                            TypeFactory.createType(Type.DOUBLE));
        MemoryDataSetDriver weightedEdges =
                new MemoryDataSetDriver(weightedMD);
        int weightIndex = weightedMD.getFieldIndex(GraphSchema.WEIGHT);
        for (int i = 0; i < orientedEdges.getRowCount(); i++) {
            Value[] oldRow = orientedEdges.getRow(i);
            final Value[] newRow = new Value[weightedMD.getFieldCount()];
            System.arraycopy(oldRow, 0, newRow, 0,
                             weightedMD.getFieldCount() - 1);
            newRow[weightedMD.getFieldCount() - 1] =
                    ValueFactory.createValue(edgeWeights[i]);
            weightedEdges.addValues(newRow);
        }
        LOGGER.debug("\tWEIGHTED EDGES");
//        print(weightedEdges);

        return weightedEdges;
    }

    /**
     * Prints the given table for debugging.
     *
     * @param table The table
     *
     * @throws DriverException
     */
    private void print(DataSet table) throws DriverException {
        String metadata = "";
        for (String name : table.getMetadata().getFieldNames()) {
            metadata += name + "\t";
        }
        LOGGER.debug(metadata);
        for (int i = 0; i < table.getRowCount(); i++) {
            String row = "";
            for (Value v : table.getRow(i)) {
                row += v + "\t";
            }
            LOGGER.debug(row);
        }
    }
}
