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

    private DataSourceFactory dsf;
    private ProgressMonitor pm;
    GeometryFactory gf = new GeometryFactory();
    private double tolerance = 0;
    private boolean expand = false;
    boolean zDirection = false;
    private String output_name;
    private boolean dim3 = false;

    /**
     * This class is used to order edges and create required nodes to build a
     * network graph
     *
     * @param dsf
     * @param pm
     */
    public NetworkGraphBuilder(DataSourceFactory dsf, ProgressMonitor pm) {
        this.dsf = dsf;
        this.pm = pm;
    }

    /**
     *
     * @return if true is the graph is oriented coordinate the z value of the
     *         start and end coordinates
     */
    public boolean isZDirection() {
        return zDirection;
    }

    /**
     * Set if the graph must ne oriented according the z value of the start and
     * end coordinates
     *
     * @param dim3
     */
    public void setZDirection(boolean zDirection) {
        this.zDirection = zDirection;
    }

    /**
     * Set if the z value of the coordinate must be used to order the nodes.
     *
     * @param dim3
     */
    public void setDim3(boolean dim3) {
        this.dim3 = dim3;
    }

    /**
     *
     * @return if the z value of the coordinate must be used.
     */
    public boolean isDim3() {
        return dim3;
    }

    /**
     * Tolerance is used to merge closed nodes
     *
     * @param tolerance
     */
    public void setTolerance(double tolerance) {
        this.tolerance = tolerance;
    }

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
            // Create a DiskBufferDriver for the nodes table.
            DiskBufferDriver nodesDriver =
                    new DiskBufferDriver(
                    dsf.getResultFile("gdms"),
                    GraphMetadataFactory.createNodesMetadata());

            // Get the path of a new file in the temporary directory.
            String diskTreePath = dsf.getTempFile();
            // TODO: What does this do?
            DiskRTree diskRTree = new DiskRTree();
            diskRTree.newIndex(new File(diskTreePath));

            // Obtain the original metadata from the input table
            Metadata originalMD = dataSet.getMetadata();
            // Count the number of fields in the input table.
            int srcFieldsCount = originalMD.getFieldCount();

            // Create the edge metadata.
            DefaultMetadata edgeMedata =
                    GraphMetadataFactory.createEdgeMetadata(originalMD);
            // Count the number of fields in the edge metadata.
            int fieldsCount = edgeMedata.getFieldCount();

            // Set the indices for the id, start_node, and end_node.
            int idIndex = edgeMedata.getFieldIndex(GraphSchema.ID);
            int startIndex = edgeMedata.getFieldIndex(GraphSchema.START_NODE);
            int endIndex = edgeMedata.getFieldIndex(GraphSchema.END_NODE);

            // Create a DiskBufferDriver for the edges table.
            DiskBufferDriver edgesDriver =
                    new DiskBufferDriver(dsf.getResultFile("gdms"), edgeMedata);

            int gidNode = 1;
            pm.startTask("Create the graph", 100);

            int count = 0;
            // Go through the DataSet.
            for (Value[] row : dataSet) {
                // See if the task has been cancelled.
                if (count >= 100 && count % 100 == 0) {
                    if (pm.isCancelled()) {
                        break;
                    }
                }
                // Prepare the new row which will be the old row with
                // new values appended.
                final Value[] newValues = new Value[fieldsCount];
                // Copy over the old values.
                System.arraycopy(row, 0, newValues, 0, srcFieldsCount);
                // Add an id.
                newValues[idIndex] = ValueFactory.createValue(count++);
                // Get the geometry.
                Geometry geom = row[geomFieldIndex].getAsGeometry();
                // Get the length of the geometry.
                double length = geom.getLength();
                // Whether or not to expand ... // TODO
                if (tolerance > 0 && length >= tolerance) {
                    expand = true;
                }
                // Get the coordinates of the geometry.
                Coordinate[] cc = geom.getCoordinates();
                // Get the start coordinate.
                Coordinate start = cc[0];
                // Get the end coordinate.
                Coordinate end = cc[cc.length - 1];
                // If the graph is to be oriented by z-values, perform
                // the proper orientation.
                if (isZDirection()) {
                    if (start.z < end.z) {
                        Coordinate tmpStart = start;
                        start = end;
                        end = tmpStart;
                    }
                }

                // Do start node work.
                gidNode = expansionWork(newValues, diskRTree, nodesDriver,
                                        gidNode, start, startIndex);

                Envelope endEnvelope = new Envelope(end);
                if (expand) {
                    endEnvelope.expandBy(tolerance);
                }
                int[] gidsEnd = diskRTree.query(endEnvelope);
                if (gidsEnd.length == 0) {
                    newValues[endIndex] =
                            ValueFactory.createValue(gidNode);
                    nodesDriver.addValues(new Value[]{
                        ValueFactory.createValue(gf.createPoint(end)),
                        ValueFactory.createValue(gidNode)});
                    diskRTree.insert(endEnvelope, gidNode);
                    gidNode++;
                } else {
                    newValues[endIndex] =
                            ValueFactory.createValue(gidsEnd[0]);
                }

                edgesDriver.addValues(newValues);
            }
            cleanUp(nodesDriver, edgesDriver, diskTreePath);
        }
    }

    /**
     * Clean up: register the nodes and edges tables, delete the RTree and end
     * the task.
     *
     * @param nodesDriver  Nodes driver
     * @param edgesDriver  Edges driver
     * @param diskTreePath Path to the RTree
     *
     * @throws DriverException
     */
    private void cleanUp(DiskBufferDriver nodesDriver,
                         DiskBufferDriver edgesDriver,
                         String diskTreePath)
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
        new File(diskTreePath).delete();
        pm.endTask();
    }

    private int expansionWork(final Value[] row,
                              DiskRTree diskRTree,
                              DiskBufferDriver nodesDriver,
                              int gidNode,
                              Coordinate coord,
                              int index)
            throws IOException, DriverException {
        // Expansion work ... // TODO
        Envelope envelope = new Envelope(coord);
        if (expand) {
            envelope.expandBy(tolerance);
        }
        // TODO
        int[] gidsStart = diskRTree.query(envelope);
        if (gidsStart.length == 0) {
            row[index] =
                    ValueFactory.createValue(gidNode);
            nodesDriver.addValues(new Value[]{
                ValueFactory.createValue(gf.createPoint(coord)),
                ValueFactory.createValue(gidNode)});
            diskRTree.insert(envelope, gidNode);
            gidNode++;
        } else {
            row[index] =
                    ValueFactory.createValue(gidsStart[0]);
        }
        return gidNode;
    }
}
