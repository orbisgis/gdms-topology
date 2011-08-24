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

import org.gdms.gdmstopology.process.NetworkGraphBuilder;
import java.io.IOException;
import org.gdms.data.NonEditableDataSourceException;
import org.gdms.data.SQLDataSourceFactory;
import org.gdms.data.schema.Metadata;
import org.gdms.data.values.Value;
import org.gdms.driver.DriverException;
import org.gdms.driver.driverManager.DriverLoadException;
import org.gdms.sql.function.FunctionSignature;
import org.orbisgis.progress.ProgressMonitor;
import org.gdms.data.types.Type;
import org.gdms.driver.DataSet;
import org.gdms.sql.function.FunctionException;
import org.gdms.sql.function.ScalarArgument;
import org.gdms.sql.function.executor.AbstractExecutorFunction;
import org.gdms.sql.function.executor.ExecutorFunctionSignature;
import org.gdms.sql.function.table.TableDefinition;

public class ST_Graph extends AbstractExecutorFunction {

        @Override
        public String getName() {
                return "ST_Graph";
        }

        @Override
        public String getSqlOrder() {
                return "select ST_Graph(the_geom [,tolerance]) from myTable;";
        }

        @Override
        public String getDescription() {
                return "Build a graph based on geometries order. A tolerance can be used to snap vertex.";
        }

        @Override
        public void evaluate(SQLDataSourceFactory dsf, DataSet[] tables, 
                        Value[] values, ProgressMonitor pm) throws FunctionException {
                try {
                        final DataSet sds = tables[0];                       
                        
                        NetworkGraphBuilder graphNetwork = new NetworkGraphBuilder(dsf, pm);
                        if (values.length == 2) {
                                if (values[1].getType() == Type.BOOLEAN) {
                                        graphNetwork.setDim3(values[1].getAsBoolean());
                                } else {
                                        graphNetwork.setTolerance(values[1].getAsDouble());
                                }
                        } else if (values.length == 3) {
                                graphNetwork.setTolerance(values[1].getAsDouble());
                                graphNetwork.setDim3(values[2].getAsBoolean());
                        }
                        graphNetwork.buildGraph(sds);
                } catch (IOException e) {
                        throw new FunctionException(e);
                } catch (DriverLoadException e) {
                        throw new FunctionException(e);
                } catch (DriverException e) {
                        throw new FunctionException(e);
                } catch (NonEditableDataSourceException e) {
                        throw new FunctionException(e);
                }
        }

        public Metadata getMetadata(Metadata[] tables) throws DriverException {
                return null;
        }

        public TableDefinition[] getTablesDefinitions() {
                return new TableDefinition[]{TableDefinition.GEOMETRY};
        }

        @Override
        public FunctionSignature[] getFunctionSignatures() {
                return new FunctionSignature[]{
                                new ExecutorFunctionSignature(ScalarArgument.GEOMETRY),
                                new ExecutorFunctionSignature(ScalarArgument.GEOMETRY, ScalarArgument.BOOLEAN),
                                new ExecutorFunctionSignature(ScalarArgument.GEOMETRY, ScalarArgument.DOUBLE),
                                new ExecutorFunctionSignature(ScalarArgument.GEOMETRY, ScalarArgument.DOUBLE, ScalarArgument.BOOLEAN),
                };
        }
}
