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
package org.gdms.gdmstopology.process;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import java.io.File;
import java.io.IOException;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.NonEditableDataSourceException;
import org.gdms.data.indexes.rtree.DiskRTree;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.schema.MetadataUtilities;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.model.GraphMetadataFactory;
import org.gdms.gdmstopology.model.GraphSchema;
import org.orbisgis.progress.ProgressMonitor;

/**
 * Builds a graph (nodes + edges) from the given input data.
 *
 * Concretely, {@link #buildGraph} uses an RTreeDisk on the given input network
 * to produce two tables, nodes and edges. These tables contain unique ids
 * assigned to each node (intersections) and edge edge (lines cut by
 * intersections).
 *
 * @author Erwan Bocher, Adam Gouge
 */
public class NetworkGraphBuilder {

    /**
     * Used to parse the data set.
     */
    private DataSourceFactory dsf;
    /**
     * Progress monitor.
     */
    private ProgressMonitor pm;
    /**
     * Used here to create {@link com.vividsolutions.jts.geom.Point}s (for
     * nodes).
     */
    private static final GeometryFactory GF = new GeometryFactory();
    /**
     * Nodes within a radius r=tolerance of each other will be considered a
     * single node.
     */
    private double tolerance = 0.0;
    /**
     * Boolean indicating whether the envelope around a point coordinate should
     * be expanded by the given tolerance to look for nearby nodes.
     */
    private boolean expandByTolerance = false;
    /**
     * Boolean indicating whether edges should be oriented from higher elevation
     * to lower elevation.
     */
    boolean orientBySlope = false;
    /**
     * The output name to prefix ".nodes" and ".edges".
     */
    private String output_name;

    /**
     * This class is used to order edges and create required nodes to build a
     * network graph
     *
     * @param dsf Used to parse the data set.
     * @param pm  Progress monitor.
     */
    public NetworkGraphBuilder(DataSourceFactory dsf, ProgressMonitor pm) {
        this.dsf = dsf;
        this.pm = pm;
    }

    /**
     * Sets whether edges should be oriented from higher elevation to lower
     * elevation.
     *
     * @param orientBySlope True iff edges should be oriented from higher
     *                      elevation to lower elevation.
     */
    public void setOrientBySlope(boolean orientBySlope) {
        this.orientBySlope = orientBySlope;
    }

    /**
     * Sets the tolerance.
     *
     * @param tolerance The tolerance.
     */
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

    /**
     * Sets the output table name.
     *
     * @param output_name The output table name.
     */
    public void setOutput_name(String output_name) {
        this.output_name = output_name;
    }

    /**
     * Create the two data structure nodes and edges using a RTree disk. This
     * method limits the overhead when the all nodes are ordered.
     *
     * @param dataSet Original dataset from which to build the graph.
     *
     * @throws DriverException
     * @throws IOException
     * @throws NonEditableDataSourceException
     */
    public void buildGraph(DataSet dataSet)
            throws DriverException,
            IOException,
            NonEditableDataSourceException {

        // Get the geometry field index.
        int geomFieldIndex = MetadataUtilities.getSpatialFieldIndex(
                dataSet.getMetadata());

        // Make sure there is a geometry field.
        if (geomFieldIndex == -1) {
            throw new DriverException(
                    "The table must contain a geometry field");
        } else {

            // Start the task.
            pm.startTask("Creating the graph", 100);

            // RTREE
            String diskRTreePath = dsf.getTempFile();
            DiskRTree diskRTree = new DiskRTree();
            File diskRTreeFile = new File(diskRTreePath);
            diskRTree.newIndex(diskRTreeFile);

            // METADATA
            // The original metadata from the input table.
            Metadata originalMD = dataSet.getMetadata();
            // The edge metadata.
            DefaultMetadata edgeMedata = GraphMetadataFactory
                    .createEdgeMetadata(originalMD);

            // DISKBUFFERDRIVERS
            // Create a DiskBufferDriver for the nodes table.
            DiskBufferDriver nodesDriver =
                    new DiskBufferDriver(
                    dsf.getResultFile("gdms"),
                    GraphMetadataFactory.createNodesMetadata());
            // Create a DiskBufferDriver for the edges table.
            DiskBufferDriver edgesDriver =
                    new DiskBufferDriver(dsf.getResultFile("gdms"),
                                         edgeMedata);

            // INDICES
            // Set the indices for the id, start_node, and end_node.
            int idIndex = edgeMedata.getFieldIndex(GraphSchema.ID);
            int startIndex = edgeMedata.getFieldIndex(GraphSchema.START_NODE);
            int endIndex = edgeMedata.getFieldIndex(GraphSchema.END_NODE);
            // COUNTERS
            int edgeGID = 0;
            int nodesGID = 1;

            // Go through the DataSet.
            for (Value[] row : dataSet) {

                // Check if we should cancel.
                if (edgeGID >= 100 && edgeGID % 100 == 0) {
                    if (pm.isCancelled()) {
                        break;
                    }
                }

                // Initialize a new row to be added to the edges driver.
                Value[] edgesRow =
                        initializeEdgeRow(row, edgeMedata.getFieldCount());
                // Add an id.
                edgesRow[idIndex] = ValueFactory.createValue(edgeGID++);

                // Get the geometry.
                Geometry geom = row[geomFieldIndex].getAsGeometry();
                // Get the start node and end node coordinates from the geometry.
                Coordinate[] cc = geom.getCoordinates();
                Coordinate startNodeCoord = cc[0];
                Coordinate endNodeCoord = cc[cc.length - 1];

                // If orienting by slope, check if the end is higher than the
                // start. If so, then switch start and end coordinates.
                if (orientBySlope && startNodeCoord.z < endNodeCoord.z) {
                    Coordinate tmpCoord = startNodeCoord;
                    startNodeCoord = endNodeCoord;
                    endNodeCoord = tmpCoord;
                }

                // If we have a positive tolerance and this geometry's length
                // is at least as big as the tolerance, then will expand an 
                // envelope around a Coordinate by the given tolerance.
                // TODO: Should we set expandByTolerance back to false otherwise?
                if (tolerance > 0 && geom.getLength() >= tolerance) {
                    expandByTolerance = true;
                }
                // Add the start node.
                nodesGID = addNodeToEdgesRowAndNodesDriver(
                        diskRTree, edgesRow, nodesDriver,
                        nodesGID, startNodeCoord, startIndex);
                // Add the end node.
                nodesGID = addNodeToEdgesRowAndNodesDriver(
                        diskRTree, edgesRow, nodesDriver,
                        nodesGID, endNodeCoord, endIndex);

                // Add the edges row to the edges table.
                edgesDriver.addValues(edgesRow);
            }
            // Clean up.
            cleanUp(nodesDriver, edgesDriver, diskRTreeFile);
        }
    }

    /**
     * Initiates a new edge row as a copy of the given original row with space
     * for new values.
     *
     * @param originalRow     The original row to be copied
     * @param edgesFieldCount The number of fields in the edge row
     *
     * @return A newly initialized edge row ready to be filled in.
     *
     * @see GraphMetadataFactory#createEdgeMetadata
     */
    private Value[] initializeEdgeRow(Value[] originalRow,
                                      int edgesFieldCount) {
        if (edgesFieldCount < originalRow.length) {
            throw new IllegalStateException("The edges row cannot have "
                    + "fewer fields than the original row.");
        } else {
            // Initialize the new row.
            final Value[] edgesRow = new Value[edgesFieldCount];
            // Copy over the old values.
            System.arraycopy(originalRow, 0, edgesRow, 0, originalRow.length);
            return edgesRow;
        }
    }

    /**
     * Uses the given {@link DiskRTree} to find nearby nodes and stick them
     * together up to the given tolerance; inserts the node into the edges row
     * and the nodes table.
     *
     * @param diskRTree   The DiskRTree
     * @param edgesRow    The edges row
     * @param nodesDriver The nodes table
     * @param nodesGID    The nodes GID
     * @param nodeCoord   The node's coordinate
     * @param nodeIndex   Where to insert the node in the edge row
     *
     * @return The nodes GID, incremented if necessary.
     *
     * @throws IOException
     * @throws DriverException
     */
    private int addNodeToEdgesRowAndNodesDriver(DiskRTree diskRTree,
                                                final Value[] edgesRow,
                                                DiskBufferDriver nodesDriver,
                                                int nodesGID,
                                                Coordinate nodeCoord,
                                                int nodeIndex)
            throws IOException, DriverException {
        // Get an envelope around (on) the given coordinate.
        Envelope envelope = new Envelope(nodeCoord);
        // Expand the envelope by tolerance if necessary.
        if (expandByTolerance) {
            envelope.expandBy(tolerance);
        }
        // See if there are any other nodes in the envelope that we should
        // stick together into a single node.
        int[] nearbyNodeIds = diskRTree.query(envelope);
        // If there is one, then add the previously found node to the edges row
        // since we are sticking this node to the one found before.
        if (nearbyNodeIds.length > 0) {
            edgesRow[nodeIndex] = ValueFactory.createValue(nearbyNodeIds[0]);
        } // Otherwise, just add this node.
        else {
            // Add this node's id to the edge row at the node's index.
            edgesRow[nodeIndex] = ValueFactory.createValue(nodesGID);
            // Add this node's coordinate (as a POINT) and its id to the 
            // nodes table.
            nodesDriver.addValues(new Value[]{
                ValueFactory.createValue(GF.createPoint(nodeCoord)),
                ValueFactory.createValue(nodesGID)});
            // Insert the envelope around this node at the nodesGID row index.
            diskRTree.insert(envelope, nodesGID);
            // Increment the nodesGID counter.
            nodesGID++;
        }
        return nodesGID;
    }

    /**
     * Clean up: register the nodes and edges tables, delete the RTree and end
     * the task.
     *
     * @param nodesDriver   Nodes driver
     * @param edgesDriver   Edges driver
     * @param diskRTreeFile RTree file to be deleted
     *
     * @throws DriverException
     */
    private void cleanUp(DiskBufferDriver nodesDriver,
                         DiskBufferDriver edgesDriver,
                         File diskRTreeFile)
            throws DriverException {
        // Finished writing.
        nodesDriver.writingFinished();
        edgesDriver.writingFinished();

        // The datasources will be registered as a schema
        String ds_nodes_name = dsf.getSourceManager().getUniqueName(
                output_name + ".nodes");
        dsf.getSourceManager().
                register(ds_nodes_name, nodesDriver.getFile());

        String ds_edges_name = dsf.getSourceManager().getUniqueName(
                output_name + ".edges");
        dsf.getSourceManager().
                register(ds_edges_name, edgesDriver.getFile());

        //Remove the Rtree on disk
        diskRTreeFile.delete();

        // End the task.
        pm.endTask();
    }
}
