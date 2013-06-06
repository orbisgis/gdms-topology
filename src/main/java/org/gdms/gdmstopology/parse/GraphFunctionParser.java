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
package org.gdms.gdmstopology.parse;

import java.util.Arrays;
import org.gdms.data.types.Type;
import org.gdms.data.values.Value;
import org.gdms.gdmstopology.model.GraphSchema;
import org.gdms.sql.function.FunctionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A helper class to parse {@link Value} arguments for SQL functions related to
 * graphs.
 *
 * @author Adam Gouge
 */
public class GraphFunctionParser {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GraphFunctionParser.class);

    /**
     * Return the name of the weights column or null for unweighted graphs.
     *
     * @param value The {@link Value} argument.
     *
     * @return The name of the weights column or null for unweighted graphs.
     *
     * @throws FunctionException
     */
    public static String parseWeight(Value value) {
        final int slotType = value.getType();
        // Make sure it is only initialized once.
        final String weights;
        if (slotType == Type.INT) {
            final int allWeightsOneInt = value.getAsInt();
            if (allWeightsOneInt == 1) {
                LOGGER.debug("Setting all weights equal to 1.");
                weights = null;
            } else { // We only accept 1 or a string.
                throw new IllegalArgumentException(
                        "Either enter 1 to set all weights equal to 1 "
                        + "or specify the name of the weight column.");
            }
        } else if (slotType == Type.STRING) {
            // Set the weights column name.
            weights = value.getAsString();
            LOGGER.debug("Setting the weight column name to be \'{}\'.",
                         weights);
        } else {
            throw new IllegalArgumentException(
                    "Either enter 1 to set all weights equal to 1 "
                    + "or specify the name of the weight column.");
        }
        return weights;
    }

    /**
     * Returns the graph orientation.
     *
     * @param value The {@link Value} argument.
     *
     * @return the graph orientation.
     *
     * @throws FunctionException
     */
    public static int parseOrientation(Value value) {
        final int slotType = value.getType();
        if (slotType == Type.INT) {
            final int orientation = value.getAsInt();
            if (LOGGER.isDebugEnabled()) {
                String graphType = (orientation == GraphSchema.DIRECT)
                        ? "directed"
                        : (orientation == GraphSchema.DIRECT_REVERSED)
                        ? "reversed"
                        : (orientation == GraphSchema.UNDIRECT)
                        ? "undirected"
                        : "ERROR";
                LOGGER.debug("Setting the orientation to be {}.", graphType);
            }
            if (orientation != GraphSchema.DIRECT
                && orientation != GraphSchema.DIRECT_REVERSED
                && orientation != GraphSchema.UNDIRECT) {
                throw new IllegalArgumentException(
                        "Please enter a valid orientation. "
                        + orientation + " is not valid");
            }
            return orientation;
        } else {
            throw new IllegalArgumentException(
                    "Please enter an integer orientation.");
        }
    }

    /**
     * Returns {@code true} if the given orientation is valid.
     *
     * @param orientation The orientation to be checked.
     *
     * @return {@code true} if the given orientation is valid.
     */
    public static boolean validOrientation(int orientation) {
        final Integer[] possibleOrientations = new Integer[]{
            GraphSchema.DIRECT,
            GraphSchema.DIRECT_REVERSED,
            GraphSchema.UNDIRECT};
        if (Arrays.asList(possibleOrientations).
                contains(orientation)) {
            return true;
        }
        return false;
    }

    /**
     * Returns a vertex from the given command line argument.
     *
     * @param value The {@link Value} argument.
     *
     * @return A vertex from the given command line argument.
     *
     * @throws FunctionException
     */
    private static int parseVertex(Value value) {
        final int slotType = value.getType();
        if (slotType == Type.INT) {
            final int vertex = value.getAsInt();
            return vertex;
        } else {
            throw new IllegalArgumentException(
                    "Please enter an integer vertex.");
        }
    }

    /**
     * Returns the source vertex from the given command line argument.
     *
     * @param value The {@link Value} argument.
     *
     * @return The source vertex.
     *
     * @throws FunctionException
     */
    public static int parseSource(Value value) {
        final int source = parseVertex(value);
        LOGGER.debug("Setting the source to be {}.", source);
        return source;
    }

    /**
     * Returns the target vertex from the given command line argument.
     *
     * @param value The {@link Value} argument.
     *
     * @return The target vertex.
     *
     * @throws FunctionException
     */
    public static int parseTarget(Value value) {
        final int target = parseVertex(value);
        LOGGER.debug("Setting the target to be {}.", target);
        return target;
    }

    /**
     * Returns an array of destination ids from the given command line argument.
     *
     * @param value The {@link Value} argument
     *
     * @return An array of destination ids
     */
    public static int[] parseDestinations(Value value) {
        final int slotType = value.getType();
        if (slotType != Type.STRING) {
            throw new IllegalArgumentException(
                    "Please specify the destinations "
                    + "in a comma-separated string.");
        } else {
            String destList = value.getAsString();
            String[] destStringArray = destList.split(",");
            int[] destIntArray = new int[destStringArray.length];
            for (int i = 0; i < destIntArray.length; i++) {
                String dString = destStringArray[i].replaceAll("\\s", "");
                destIntArray[i] = Integer.valueOf(dString);
            }
            return destIntArray;
        }
    }
}
