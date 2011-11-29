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
package org.gdms.gdmstopology.function;

import com.vividsolutions.jts.geom.Geometry;
import java.util.Collection;
import java.util.List;

import org.gdms.data.SQLDataSourceFactory;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DriverException;
import org.gdms.driver.DataSet;
import org.gdms.driver.driverManager.DriverLoadException;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.gdmstopology.process.LineNoder;
import org.gdms.sql.function.FunctionSignature;
import org.orbisgis.progress.ProgressMonitor;

import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.table.AbstractTableFunction;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.gdms.sql.function.table.TableFunctionSignature;

public class ST_ToLineNoder extends AbstractTableFunction {

        @SuppressWarnings({"unchecked", "static-access"})
        @Override
        public DataSet evaluate(SQLDataSourceFactory dsf, DataSet[] tables,
                Value[] values, ProgressMonitor pm) throws FunctionException {
                try {
                        final DataSet inSds = tables[0];
                        final LineNoder lineNoder = new LineNoder(inSds);

                        final Collection lines = lineNoder.getLines();
                        final Geometry nodedGeom = lineNoder.getNodeLines((List) lines);
                        final Collection<Geometry> nodedLines = lineNoder.toLines(nodedGeom);

                        // build and populate the resulting driver
                        final MemoryDataSetDriver driver = new MemoryDataSetDriver(
                                getMetadata(null));

                        int k = 0;
                        final int rowCount = nodedLines.size();
                        for (Geometry geometry : nodedLines) {
                                if (k / 100 == k / 100.0) {
                                        if (pm.isCancelled()) {
                                                break;
                                        } else {
                                                pm.progressTo((int) (100 * k / rowCount));
                                        }
                                }
                                driver.addValues(new Value[]{ValueFactory.createValue(k),
                                                ValueFactory.createValue(geometry)});
                                k++;
                        }
                        return driver;
                } catch (DriverException e) {
                        throw new FunctionException(e);
                } catch (DriverLoadException e) {
                        throw new FunctionException(e);
                }
        }

        @Override
        public String getDescription() {
                return "Build all intersection and convert the geometries into lines ";
        }

        @Override
        public String getSqlOrder() {
                return "select * from  ST_ToLineNoder(table) from myTable;";
        }

        @Override
        public String getName() {
                return "ST_ToLineNoder";
        }

        @Override
        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                return new DefaultMetadata(new Type[]{
                                TypeFactory.createType(Type.INT),
                                TypeFactory.createType(Type.GEOMETRY)}, new String[]{"gid",
                                "the_geom"});
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{new TableFunctionSignature(TableDefinition.GEOMETRY, new TableArgument(TableDefinition.GEOMETRY))};
        }
}
