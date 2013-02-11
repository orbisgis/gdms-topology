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
package org.gdms.gdmstopology.centrality;

import org.gdms.data.types.Type;
import org.gdms.data.values.Value;
import org.gdms.sql.function.FunctionException;

/**
 * A helper class to parse {@link Value} arguments for SQL functions which
 * return results in an output table.
 *
 * @author Adam Gouge
 */
public class OutputFunctionParser {

    /**
     * Default output table prefix;
     */
    public static final String DEFAULT_OUTPUT_TABLE_PREFIX = "output";

    /**
     * Returns the output table prefix.
     *
     * @param value The {@link Value} argument.
     *
     * @return The output table prefix.
     *
     * @throws FunctionException
     */
    public static String parseOutputTablePrefix(Value value) throws
            FunctionException {
        final int slotType = value.getType();
        if (slotType == Type.STRING) {
            final String outputTablePrefix = value.getAsString();
            System.out.println("Setting the output table prefix "
                    + "to be \'" + outputTablePrefix
                    + "\'.");
            return outputTablePrefix;
        } else {
            throw new FunctionException(
                    "Please enter a string output table prefix.");
        }
    }
}
