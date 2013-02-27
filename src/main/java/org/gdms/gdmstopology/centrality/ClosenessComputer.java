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

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.gdms.data.DataSourceFactory;
import org.gdms.data.schema.DefaultMetadata;
import org.gdms.data.schema.Metadata;
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
 * Calculates closeness centrality.
 *
 * @author Adam Gouge
 */
public abstract class ClosenessComputer extends AbstractExecutorFunctionHelper {

    /**
     * The data set.
     */
    protected final DataSet dataSet;
    /**
     * Orientation.
     */
    protected final int orientation;

    /**
     * Constructs a new {@link ClosenessComputer}.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param pm  The progress monitor used to track the progress of the
     *            calculation.
     */
    public ClosenessComputer(DataSourceFactory dsf,
                             DataSet dataSet,
                             ProgressMonitor pm,
                             int orientation) {
        super(dsf, pm);
        this.dataSet = dataSet;
        this.orientation = orientation;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getOutputTableSuffix() {
        return GraphSchema.CLOSENESS_CENTRALITY;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Metadata createMetadata() {
        return new DefaultMetadata(
                new Type[]{
                    TypeFactory.createType(Type.INT),
                    TypeFactory.createType(Type.DOUBLE)},
                new String[]{
                    GraphSchema.ID,
                    GraphSchema.CLOSENESS_CENTRALITY});
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void storeResultsInDriver(
            Map results,
            DiskBufferDriver driver) {

        Iterator<Integer> it = results.keySet().iterator();

        try {
            while (it.hasNext()) {

                final int node = it.next();
                final Double closeness = (Double) results.get(node);

                Value[] valuesToAdd =
                        new Value[]{
                    // ID
                    ValueFactory.createValue(node),
                    // Closeness
                    ValueFactory.createValue(closeness)
                };

                driver.addValues(valuesToAdd);
            }
        } catch (DriverException ex) {
            Logger.getLogger(GraphAnalyzer.class.getName()).
                    log(Level.SEVERE, STORAGE_ERROR, ex);
        }
    }
}
