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
package org.gdms.gdmstopology.utils;

import java.util.Arrays;

/**
 * Helper class to concatenate arrays.
 *
 * @author Adam Gouge
 */
public class ArrayConcatenator {

    /**
     * Concatenates the given arrays.
     *
     * @param <T>   The array type.
     * @param first The first array.
     * @param rest  The remaining arrays.
     *
     * @return The arrays concatenated together.
     */
    public static <T> T[] concatenate(T[] first, T[]... rest) {
        // Get the sum of the lengths of all the arrays.
        int totalLength = first.length;
        for (T[] array : rest) {
            totalLength += array.length;
        }
        // Get a copy of the first array.
        T[] result = Arrays.copyOf(first, totalLength);
        // Get the first offset.
        int offset = first.length;
        // Copy the remaining arrays.
        for (T[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            // Update the offset.
            offset += array.length;
        }
        return result;
    }
}
