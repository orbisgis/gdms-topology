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
package org.gdms.gdmstopology.graphcreator;

import org.gdms.data.DataSourceCreationException;
import org.gdms.data.NoSuchTableException;
import org.gdms.data.indexes.IndexException;
import org.gdms.driver.DriverException;
import org.gdms.sql.function.FunctionException;
import org.junit.Test;

/**
 * Tests {@link UnweightedGraphCreator}.
 *
 * @author Adam Gouge
 */
public class UnweightedGraphCreatorTest extends GraphCreatorTest {

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void test2DGraphDirected() throws NoSuchTableException,
            DataSourceCreationException, DriverException, FunctionException,
            IndexException {
        testGraphCreatorDirected(get2DGraphDataSource(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void test2DGraphReversed() throws NoSuchTableException,
            DataSourceCreationException, DriverException, FunctionException,
            IndexException {
        testGraphCreatorReversed(get2DGraphDataSource(), null);
    }

    /**
     * {@inheritDoc}
     */
    @Test
    @Override
    public void test2DGraphUndirected() throws NoSuchTableException,
            DataSourceCreationException, DriverException, FunctionException,
            IndexException {
        testGraphCreatorUndirected(get2DGraphDataSource(), null);
    }
}
