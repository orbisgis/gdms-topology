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

import com.vividsolutions.jts.algorithm.CGAlgorithms;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.operation.distance.DistanceOp;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.indexes.DefaultSpatialIndexQuery;
import org.gdms.data.indexes.IndexException;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.gdms.driver.DataSet;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;
import org.orbisgis.progress.ProgressMonitor;

/**
 * Aggregates geometries by blocks determined by a spatial predicate.
 *
 * <p> If a comma-separated list of field names is given after the geometry
 * field, e.g., <center>
 * {@code SELECT ST_BlockIdentity(the_geom, 'the_geom, myId, myDesc') FROM ...}),
 * </center> <br> then these fields are returned in the output for the selected
 * geometries. Unique IDs are best suited for this use, but anything can be
 * used. By default if nothing is specified, all fields of the input table are
 * kept.
 *
 * <p> The algorithm uses an index to get the nearest geometries of a fixed
 * geometry and then uses the <a
 * href="http://www.vividsolutions.com/jts/jtshome.htm">JTS</a> Class
 * {@link com.vividsolutions.jts.operation.distance.DistanceOp} to check if the
 * (smallest) distance between the two is 0. If it is, the two are grouped and
 * the same algorithm is computed on the newly added geometries, until the group
 * does not expand anymore.
 *
 * <p> <i>Note</i>: This is a <b>BRUTE-FORCE</b>, <b>NON-ROBUST</b> algorithm.
 *
 * <p> Using {@link com.vividsolutions.jts.operation.distance.DistanceOp} is
 * <i>much</i> faster than using {@code geom1.touches(geom2)}, because it does
 * not need to compute the DE-9IM intersection matrix. However it is still a
 * brute-force algorithm on the lines and points of both geometries. Also, it
 * uses a non-robust computational geometric algorithm in the JTS Class
 * {@link com.vividsolutions.jts.algorithm.CGAlgorithms}. This could be improved
 * by writing a custom JTS {@code TouchesOp}.
 *
 * <p> October 12, 2012: Documentation updated by Adam Gouge.
 *
 * @author Antoine Gourlay
 * @author Erwan Bocher
 */
public class ST_BlockIdentity extends AbstractTableFunction {

    /**
     *
     */
    private HashSet<Integer> idsToProcess;
    /**
     *
     */
    private int[] fieldIds;
    /**
     *
     */
    private DataSet dataSet;
    /**
     *
     */
    private int geomFieldIndex;
    /**
     *
     */
    private String geomField;
    /**
     *
     */
    private DiskBufferDriver diskBufferDriver;

    /**
     * Evaluates the function.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param tables The input table.
     * @param values Array containing the optional arguments.
     * @param pm The progress monitor used to track the progress of the graph
     * construction.
     * @throws FunctionException
     */
    @Override
    public DataSet evaluate(DataSourceFactory dsf, DataSet[] tables,
            Value[] values, ProgressMonitor pm) throws
            FunctionException {
        dataSet = tables[0];
        //We need to read our source.                
        try {
            Metadata metadata = dataSet.getMetadata();
            String[] fieldNames;

            if (values.length == 2) {
                geomField = values[0].getAsString();
                fieldNames = values[1].getAsString().split(", *");
            } else {
                geomField = values[0].getAsString();
                fieldNames = metadata.getFieldNames();
            }

            geomFieldIndex = metadata.getFieldIndex(geomField);
            if (geomFieldIndex != -1) {
                fieldIds = new int[fieldNames.length];
                for (int i = 0; i < fieldNames.length; i++) {
                    fieldIds[i] = metadata.getFieldIndex(fieldNames[i]);
                }




                pm.startTask("Building indexes", 100);
                // build indexes
                if (!dsf.getIndexManager().isIndexed(dataSet, geomField)) {
                    dsf.getIndexManager().buildIndex(dataSet, geomField, pm);
                }
                pm.endTask();


                //Populate a hashset with all row ids
                idsToProcess = new HashSet<Integer>();
                for (int i = 0; i < dataSet.getRowCount(); i++) {
                    idsToProcess.add(i);
                }

                // results
                DefaultMetadata met = new DefaultMetadata();
                for (int i = 0; i < fieldIds.length; i++) {
                    met.addField(fieldNames[i], metadata.getFieldType(fieldIds[i]));
                }
                met.addField("block_id", TypeFactory.createType(Type.LONG));

                diskBufferDriver = new DiskBufferDriver(dsf, met);


                int blockId = 1;
                while (!idsToProcess.isEmpty()) {

                    // starts the block
                    int start = idsToProcess.iterator().next();
                    HashSet<Integer> block = new HashSet<Integer>();
                    block.add(start);

                    // aggregates the block
                    aggregateNeighbours(dsf, start, block);

                    // writes the block
                    Iterator<Integer> it = block.iterator();
                    while (it.hasNext()) {
                        final Integer next = it.next();
                        Value[] res = new Value[fieldIds.length + 1];
                        for (int i = 0; i < fieldIds.length; i++) {
                            res[i] = dataSet.getFieldValue(next, fieldIds[i]);
                        }
                        res[fieldIds.length] = ValueFactory.createValue(blockId);
                        diskBufferDriver.addValues(res);
                    }

                    // mark all those geometries as processed
                    idsToProcess.removeAll(block);

                    blockId++;
                }
                pm.endTask();
                diskBufferDriver.writingFinished();
                pm.endTask();
                diskBufferDriver.open();
                return diskBufferDriver;
            } else {
                throw new FunctionException("The table doesn't contain the geometry field " + geomField);
            }

        } catch (DriverException ex) {
            throw new FunctionException(ex);
        } catch (NoSuchTableException ex) {
            throw new FunctionException(ex);
        } catch (IndexException ex) {
            throw new FunctionException(ex);
        }
    }

    @Override
    public void workFinished() throws DriverException {
        if (diskBufferDriver != null) {
            diskBufferDriver.close();
        }
    }

    private void aggregateNeighbours(DataSourceFactory dsf, int id, Set<Integer> agg) throws DriverException {
        int size = agg.size();

        Set<Integer> re = relativesOf(dsf, id, agg);
        agg.addAll(re);
        int nSize = agg.size();


        if (nSize == size) {
            // blockSize has not changed, there is no more blocks to add
            return;
        } else {
            Iterator<Integer> it = re.iterator();
            while (it.hasNext()) {
                aggregateNeighbours(dsf, it.next(), agg);
            }
        }
    }

    private Set<Integer> relativesOf(DataSourceFactory dsf, int id, Set<Integer> excluded) throws DriverException {
        Geometry geom = dataSet.getFieldValue(id, geomFieldIndex).getAsGeometry();

        // query index
        DefaultSpatialIndexQuery query = new DefaultSpatialIndexQuery(geomField, geom.getEnvelopeInternal());
        Iterator<Integer> s = dataSet.queryIndex(dsf, query);

        HashSet<Integer> h = new HashSet<Integer>();
        while (s.hasNext()) {
            int i = s.next();

            // i != id to prevent adding itself
            // !excluded.contains(i) to filter already added geometries
            //      (this is O(1) while the next test is far from it...)
            // test if both geoms are at 0 distance, i.e. touches
            if (i != id && !excluded.contains(i) && DistanceOp.isWithinDistance(geom, dataSet.getFieldValue(i, geomFieldIndex).getAsGeometry(), 0)) {
                h.add(i);
            }
        }
        return h;
    }

    /**
     * Returns the name of this function. This name will be used in SQL
     * statements.
     *
     * @return The name of this function.
     */
    @Override
    public String getName() {
        return "ST_BlockIdentity";
    }

    /**
     * Returns a description of this function.
     *
     * @return A description of this function.
     */
    @Override
    public String getDescription() {
        return "Return all geometry blocks. A geometry block is a set of connected geometries.";
    }

    /**
     * Returns an example query using this function.
     *
     * @return An example query using this function.
     */
    @Override
    public String getSqlOrder() {
        return "SELECT * from ST_BlockIdentity(table, the_geom [, 'the_geom, titi, toto' ])";
    }

    /**
     * Hack to be able to inject into the map without a {@code CREATE TABLE}.
     *
     * @param tables Metadata objects of input tables.
     * @return The corresponding output metadata.
     * @throws DriverException
     */
    @Override
    public Metadata getMetadata(Metadata[] tables) throws DriverException {
        // Hack to be able to inject into the map without a CREATE TABLE
        return new DefaultMetadata(
                new Type[]{
                    TypeFactory.createType(Type.GEOMETRY)
                },
                new String[]{
                    "the_geom"
                });
    }

    /**
     * Returns an array of all possible signatures of this function. Multiple
     * signatures arise from some arguments being optional.
     *
     * <p> Possible signatures:
     *
     * @return An array of all possible signatures of this function.
     */
    // TODO: Finish possible signatures in the documentation.
    // TODO: Why is one GEOMETRY field new and the other not?
    @Override
    public FunctionSignature[] getFunctionSignatures() {
        return new FunctionSignature[]{
                    // First signature.
                    new TableFunctionSignature(
                    TableDefinition.GEOMETRY,
                    new TableArgument(TableDefinition.GEOMETRY),
                    ScalarArgument.STRING),
                    // Second signature.
                    new TableFunctionSignature(
                    TableDefinition.GEOMETRY,
                    new TableArgument(TableDefinition.GEOMETRY),
                    ScalarArgument.STRING,
                    ScalarArgument.STRING)
                };
    }
}
