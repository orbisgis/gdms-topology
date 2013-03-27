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
package org.gdms.gdmstopology.functionhelpers;

import org.gdms.data.DataSourceFactory;
import org.gdms.data.schema.Metadata;
import org.gdms.driver.DiskBufferDriver;
import org.gdms.driver.DriverException;
import org.orbisgis.progress.ProgressMonitor;

/**
 * A helper class to store the results calculated by an SQL function.
 *
 * @author Adam Gouge
 */
public abstract class FunctionHelper {

    /**
     * Used to parse the data set.
     */
    protected final DataSourceFactory dsf;
    /**
     * Progress monitor.
     */
    protected final ProgressMonitor pm;
    /**
     * Error returned when there is a problem storing the results in a driver.
     */
    protected static final String STORAGE_ERROR =
            "Can't store values in driver.";

    /**
     * Constructor.
     *
     * @param dsf The {@link DataSourceFactory} used to parse the data set.
     * @param pm  The progress monitor used to track the progress of the
     *            calculation.
     */
    public FunctionHelper(DataSourceFactory dsf,
                          ProgressMonitor pm) {
        this.dsf = dsf;
        this.pm = pm;
    }

    /**
     * Computes and stores results in a newly created {@link DiskBufferDriver}
     * and cleans up.
     *
     * @return The {@link DiskBufferDriver} holding the results.
     *
     * @throws DriverException
     */
    public DiskBufferDriver doWork() throws DriverException {

        DiskBufferDriver driver =
                new DiskBufferDriver(dsf, createMetadata());

        computeAndStoreResults(driver);

        cleanUp(driver, pm);

        return driver;
    }

    /**
     * Creates the metadata for the results.
     *
     * @return The metadata.
     */
    protected abstract Metadata createMetadata();

    /**
     * Computes and stores the results in a {@link DiskBufferDriver}.
     *
     * @param driver The driver.
     *
     */
    protected abstract void computeAndStoreResults(DiskBufferDriver driver);

    /**
     * Cleans up.
     *
     * @param driver The driver.
     * @param pm     The progress monitor.
     *
     * @throws DriverException
     */
    private void cleanUp(DiskBufferDriver driver, ProgressMonitor pm)
            throws DriverException {
        // We are done writing.
        driver.writingFinished();
        // The task is done.
        pm.endTask();
        // Open the DiskBufferDriver.
        driver.open();
    }
}
