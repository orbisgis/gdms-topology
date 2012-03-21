/**
 * GDMS-Topology  is a library dedicated to graph analysis. It's based on the JGraphT available at
 * http://www.jgrapht.org/. It ables to compute and process large graphs using spatial
 * and alphanumeric indexes.
 * This version is developed at French IRSTV institut as part of the
 * EvalPDU project, funded by the French Agence Nationale de la Recherche
 * (ANR) under contract ANR-08-VILL-0005-01 and GEBD project
 * funded by the French Ministery of Ecology and Sustainable Development .
 *
 * GDMS-Topology  is distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 * GDMS-Topology is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * GDMS-Topology is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * GDMS-Topology. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://trac.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
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
import org.gdms.data.schema.MetadataUtilities;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.model.GraphSchema;
import org.orbisgis.progress.ProgressMonitor;

/**
 *
 * @author ebocher
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
         * This class is used to order edges and create requiered nodes to build a network graph
         * @param dsf
         * @param pm
         */
        public NetworkGraphBuilder(DataSourceFactory dsf, ProgressMonitor pm) {
                this.dsf = dsf;
                this.pm = pm;
        }

        /**
         * 
         * @return if true is the graph is oriented coordinate 
         * the z value of the start and end coordinates
         */
        public boolean isZDirection() {
                return zDirection;
        }

        /**
         * Set if the graph must ne oriented according the z value of 
         * the start and end coordinates
         * 
         * @param dim3 
         */
        public void setZDirection(boolean zDirection) {
                this.zDirection = zDirection;
        }

        /**
         * Set if the z value of the coordinate must be used to order the nodes.
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
         * @param tolerance
         */
        public void setTolerance(double tolerance) {
                this.tolerance = tolerance;
        }

        public void setOutput_name(String output_name) {
                this.output_name = output_name;
        }

        /**
         * Create the two data structure nodes and edges using a RTree disk.
         * This method limits the overhead when the all nodes are ordered.
         * @param sds
         * @throws DriverException
         * @throws IOException
         * @throws NonEditableDataSourceException
         */
        public void buildGraph(DataSet dataSet) throws DriverException, IOException, NonEditableDataSourceException {
                pm.startTask("Create the graph", 100);

                int geomFieldIndex = MetadataUtilities.getSpatialFieldIndex(dataSet.getMetadata());

                if (geomFieldIndex != -1) {                        
                        DefaultMetadata nodeMedata = new DefaultMetadata(new Type[]{
                                        TypeFactory.createType(Type.GEOMETRY),
                                        TypeFactory.createType(Type.INT)}, new String[]{"the_geom",
                                        GraphSchema.ID});

                        DiskBufferDriver nodesDriver = new DiskBufferDriver(dsf.getResultFile("gdms"), nodeMedata);

                        String diskTreePath = dsf.getTempFile();
                        DiskRTree diskRTree = new DiskRTree();
                        diskRTree.newIndex(new File(diskTreePath));

                        DefaultMetadata edgeMedata = new DefaultMetadata(dataSet.getMetadata());
                        int srcFieldsCount = edgeMedata.getFieldCount();

                        edgeMedata.addField(GraphSchema.ID, TypeFactory.createType(Type.INT));
                        edgeMedata.addField(GraphSchema.START_NODE, TypeFactory.createType(Type.INT));
                        edgeMedata.addField(GraphSchema.END_NODE, TypeFactory.createType(Type.INT));

                        int fieldsCount = edgeMedata.getFieldCount();

                        int idIndex = srcFieldsCount;
                        int initialIndex = srcFieldsCount + 1;
                        int finalIndex = srcFieldsCount + 2;

                        DiskBufferDriver edgesDriver = new DiskBufferDriver(dsf.getResultFile("gdms"), edgeMedata);

                        long rowCount = dataSet.getRowCount();
                        int gidNode = 1;

                        for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
                                if (rowIndex / 100 == rowIndex / 100.0) {
                                        if (pm.isCancelled()) {
                                                break;
                                        } else {
                                                pm.progressTo((int) (100 * rowIndex / rowCount));
                                        }
                                }
                                final Value[] fieldsValues = dataSet.getRow(rowIndex);
                                final Value[] newValues = new Value[fieldsCount];
                                System.arraycopy(fieldsValues, 0, newValues, 0,
                                        srcFieldsCount);
                                newValues[idIndex] = ValueFactory.createValue(rowIndex + 1);
                                Geometry geom = fieldsValues[geomFieldIndex].getAsGeometry();
                                double length = geom.getLength();
                                if (tolerance > 0 && length >= tolerance) {
                                        expand = true;
                                }
                                Coordinate[] cc = geom.getCoordinates();
                                Coordinate start = cc[0];
                                Coordinate end = cc[cc.length - 1];

                                if (isZDirection()) {
                                        if (start.z < end.z) {
                                                Coordinate tmpStart = start;
                                                start = end;
                                                end = tmpStart;
                                        }
                                }
                                Envelope startEnvelope = new Envelope(start);
                                if (expand) {
                                        startEnvelope.expandBy(tolerance);
                                }
                                int[] gidsStart = diskRTree.query(startEnvelope);
                                if (gidsStart.length == 0) {
                                        newValues[initialIndex] =
                                                ValueFactory.createValue(gidNode);
                                        nodesDriver.addValues(new Value[]{ValueFactory.createValue(gf.createPoint(start)),
                                                        ValueFactory.createValue(gidNode)});
                                        diskRTree.insert(startEnvelope, gidNode);
                                        gidNode++;
                                } else {
                                        newValues[initialIndex] =
                                                ValueFactory.createValue(gidsStart[0]);
                                }
                                Envelope endEnvelope = new Envelope(end);
                                if (expand) {
                                        endEnvelope.expandBy(tolerance);
                                }
                                int[] gidsEnd = diskRTree.query(endEnvelope);
                                if (gidsEnd.length == 0) {
                                        newValues[finalIndex] =
                                                ValueFactory.createValue(gidNode);
                                        nodesDriver.addValues(new Value[]{ValueFactory.createValue(gf.createPoint(end)),
                                                        ValueFactory.createValue(gidNode)});
                                        diskRTree.insert(endEnvelope, gidNode);
                                        gidNode++;
                                } else {
                                        newValues[finalIndex] =
                                                ValueFactory.createValue(gidsEnd[0]);
                                }

                                edgesDriver.addValues(newValues);

                        }
                        nodesDriver.writingFinished();
                        edgesDriver.writingFinished();


                        //The datasources will be registered as a schema
                        String ds_nodes_name = dsf.getSourceManager().getUniqueName(output_name + ".nodes");
                        dsf.getSourceManager().register(ds_nodes_name, nodesDriver.getFile());

                        String ds_edges_name = dsf.getSourceManager().getUniqueName(output_name + ".edges");
                        dsf.getSourceManager().register(ds_edges_name, edgesDriver.getFile());

                        //Remove the Rtree on disk
                        new File(diskTreePath).delete();
                } else {
                        throw new DriverException("The table must contains a geometry field");
                }
        }
}
