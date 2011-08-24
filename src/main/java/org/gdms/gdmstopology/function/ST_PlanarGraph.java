/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. OrbisGIS is
 * distributed under GPL 3 license. It is produced by the "Atelier SIG" team of
 * the IRSTV Institute <http://www.irstv.cnrs.fr/> CNRS FR 2488.
 *
 * 
 *  Team leader Erwan BOCHER, scientific researcher,
 * 
 *  User support leader : Gwendall Petit, geomatic engineer.
 *
 *
 * Copyright (C) 2007 Erwan BOCHER, Fernando GONZALEZ CORTES, Thomas LEDUC
 *
 * Copyright (C) 2010 Erwan BOCHER, Pierre-Yves FADET, Alexis GUEGANNO, Maxence LAURENT, Adelin PIAU
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 *
 * or contact directly:
 * info _at_ orbisgis.org
 */
package org.gdms.gdmstopology.function;

import java.io.IOException;
import org.gdms.data.DataSourceCreationException;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.NonEditableDataSourceException;
import org.gdms.data.SQLDataSourceFactory;
import org.gdms.data.indexes.IndexException;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.driver.driverManager.DriverLoadException;
import org.gdms.gdmstopology.process.PlanarGraphBuilder;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.FunctionSignature;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.executor.AbstractExecutorFunction;
import org.gdms.sql.function.executor.ExecutorFunctionSignature;
import org.gdms.sql.function.table.TableArgument;
import org.gdms.sql.function.table.TableDefinition;
import org.orbisgis.progress.ProgressMonitor;

public class ST_PlanarGraph extends AbstractExecutorFunction {

        @Override
        public String getName() {
                return "ST_PlanarGraph";
        }

        @Override
        public String getSqlOrder() {
                return "EXECUTE ST_PlanarGraph(mytable, 'output_table_name');";
        }

        @Override
        public String getDescription() {
                return "Build a planar graph based on polygons";
        }

        @Override
        public void evaluate(SQLDataSourceFactory dsf, DataSet[] tables,
                Value[] values, ProgressMonitor pm) throws FunctionException {
                try {
                        PlanarGraphBuilder planarGraph = new PlanarGraphBuilder(dsf, pm);
                        if (values.length == 1) {
                                planarGraph.setOutput_name(values[0].getAsString());
                        } else {
                                planarGraph.setOutput_name(dsf.getUID());
                        }
                        planarGraph.buildGraph(tables[0]);
                        planarGraph.createPolygonAndTopology();
                } catch (IOException e) {
                        throw new FunctionException(e);
                } catch (DriverLoadException e) {
                        throw new FunctionException(e);
                } catch (DriverException e) {
                        throw new FunctionException(e);
                } catch (NonEditableDataSourceException e) {
                        throw new FunctionException(e);
                } catch (NoSuchTableException e) {
                        throw new FunctionException(e);
                } catch (IndexException e) {
                        throw new FunctionException(e);
                } catch (DataSourceCreationException e) {
                        throw new FunctionException(e);
                }
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new ExecutorFunctionSignature(new TableArgument(TableDefinition.GEOMETRY)),
                                new ExecutorFunctionSignature(new TableArgument(TableDefinition.GEOMETRY), ScalarArgument.STRING)};
        }
}
