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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.gdms.data.types.Type;
import org.gdms.data.values.Value;
import org.gdms.driver.DataSet;
import org.gdms.driver.DriverException;
import org.gdms.gdmstopology.function.ST_ShortestPathLength;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.DIRECTED;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.REVERSED;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.SEPARATOR;
import static org.gdms.gdmstopology.function.ST_ShortestPathLength.UNDIRECTED;
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

    /**
     * Weight column name.
     */
    private String weightsColumn = null;
    /**
     * Global orientation string.
     */
    private String globalOrientation = null;
    /**
     * Edge orientation string.
     */
    private String edgeOrientationColumnName = null;
    /**
     * Logger
     */
    private static final Logger LOGGER =
            LoggerFactory.getLogger(GraphFunctionParser.class);

    /**
     * Returns the weight column name.
     *
     * @return the weight column name
     */
    public String getWeightsColumn() {
        return weightsColumn;
    }

    /**
     * Returns the global orientation string.
     *
     * @return The global orientation string
     */
    public String getGlobalOrientation() {
        return globalOrientation;
    }

    /**
     * Returns the edge orientation string.
     *
     * @return The edge orientation string
     */
    public String getEdgeOrientationColumnName() {
        return edgeOrientationColumnName;
    }

    /**
     * Parse the optional arguments.
     *
     * @param values   Arguments array
     * @param argIndex Index of the first optional argument
     */
    public void parseOptionalArguments(DataSet edges, Value[] values,
                                       int argIndex) {
        // If more than two optional arguments are given, ignore the extras.
        int argIndexLimiter = argIndex + 2;
        while (argIndex < values.length && argIndex < argIndexLimiter) {
            parseStringArgument(edges, values[argIndex++]);
        }
    }

    /**
     * Parse possible String arguments for graph functions, namely weight and
     * orientation.
     *
     * @param edges The edges
     * @param value A given argument to parse.
     */
    protected void parseStringArgument(DataSet edges, Value value) {
        if (value.getType() != Type.STRING) {
            throw new IllegalArgumentException(
                    "Weights and orientations must be specified as strings.");
        } else {
            String v = value.getAsString();
            if (!parseOrientation(edges, v)) {
                if (!parseWeight(v)) {
                    throw new IllegalArgumentException(
                            "Unrecognized string argument.");
                }
            }
        }
    }

    /**
     * Recovers the global (and edge) orientation(s) from the given string,
     * making sure the edge orientation column exists in the given data set.
     *
     * @param edges Data set
     * @param v     String
     *
     * @return True if the orientations were correctly parsed.
     *
     * @throws IllegalArgumentException
     */
    protected boolean parseOrientation(DataSet edges, String v) throws
            IllegalArgumentException {
        if (isDirectedString(v) || isReversedString(v)) {
            if (isDirectedString(v)) {
                globalOrientation = DIRECTED;
            } else if (isReversedString(v)) {
                globalOrientation = REVERSED;
            }
            edgeOrientationColumnName =
                    getEdgeOrientationColumnName(edges, v);
            LOGGER.info("Global orientation = '{}'.", globalOrientation);
            LOGGER.info("Edge orientation column name = '{}'.",
                        edgeOrientationColumnName);
            return true;
        } else if (isUndirectedString(v)) {
            globalOrientation = UNDIRECTED;
            if (!v.trim().equalsIgnoreCase(UNDIRECTED)) {
                LOGGER.warn("Edge orientations are ignored for undirected "
                            + "graphs.");
            }
            LOGGER.info("Global orientation = '{}'.", globalOrientation);
            return true;
        }
        return false;
    }

    /**
     * Recovers the weight column name from the given string.
     *
     * @param v String
     *
     * @return True if the weight column name was correctly parsed.
     */
    protected boolean parseWeight(String v) {
        if (isWeightsString(v)) {
            weightsColumn = v.trim();
            checkForIllegalCharacters(weightsColumn);
            LOGGER.info("Weights column name = '{}'.", v);
            return true;
        }
        return false;
    }

    /**
     * Makes sure the given string contains only letters, numbers and
     * underscores.
     *
     * @param v String
     */
    private void checkForIllegalCharacters(String v) {
        Matcher m = Pattern.compile("[^_0-9A-Za-z]").matcher(v);
        String illegalCharacters = "";
        while (m.find()) {
            illegalCharacters += "\"" + m.group() + "\", ";
        }
        if (!illegalCharacters.equals("")) {
            LOGGER.warn("Illegal character: " + illegalCharacters);
        }
    }

    /**
     * Returns true if the given string contains the string "directed" but not
     * "undirected".
     *
     * @param s String
     *
     * @return Whether or not the string represents a directed graph.
     */
    protected boolean isDirectedString(String s) {
        if (s.toLowerCase().contains(DIRECTED)
            && !isUndirectedString(s)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given string contains the string "reversed".
     *
     * @param s String
     *
     * @return Whether or not the string represents a reversed graph.
     */
    protected boolean isReversedString(String s) {
        if (s.toLowerCase().contains(REVERSED)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given string contains the string "undirected".
     *
     * @param s String
     *
     * @return Whether or not the string represents an undirected graph.
     */
    protected boolean isUndirectedString(String s) {
        if (s.toLowerCase().contains(UNDIRECTED)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if the given string does not represent a graph orientation.
     *
     * @param s String
     *
     * @return Whether or not the string represents a weighted graph, since
     *         weight and orientation are the only optional arguments.
     */
    protected boolean isWeightsString(String s) {
        return !(isDirectedString(s) || isReversedString(s)
                 || isUndirectedString(s));
    }

    private String getEdgeOrientationColumnName(DataSet edges,
                                                String v) {
        if (!v.contains(SEPARATOR)) {
            throw new IllegalArgumentException(
                    "Bad orientation format. Enter "
                    + ST_ShortestPathLength.POSSIBLE_ORIENTATIONS + ".");
        } else {
            // Extract the global and edge orientations.
            String[] s = v.split(SEPARATOR);
            if (s.length == 2) {
                // Return just the edge orientation column name and not the
                // global orientation.
                String edgeOrient = s[1].trim();
                if (!checkColumnExistence(edges, edgeOrient)) {
                    checkForIllegalCharacters(edgeOrient);
                }
                return edgeOrient;
            } else {
                throw new IllegalArgumentException(
                        "You must specify both global and edge orientations for "
                        + "directed or reversed graphs. Separate them by "
                        + "a '" + SEPARATOR + "'.");
            }
        }
    }

    /**
     * Makes sure the edges table contains the given edge orientation column
     * name.
     *
     * @param edges                     Edges table
     * @param edgeOrientationColumnName edge orientation column name
     *
     * @throws IllegalArgumentException If the column was not found in the edges
     *                                  table.
     */
    private boolean checkColumnExistence(DataSet edges,
                                         String edgeOrientationColumnName)
            throws IllegalArgumentException {
        try {
            if (edges != null) {
                // Make sure this column exists.
                if (!Arrays.asList(edges.getMetadata()
                        .getFieldNames())
                        .contains(edgeOrientationColumnName)) {
                    throw new IllegalArgumentException(
                            "Column '" + edgeOrientationColumnName
                            + "' not found in the edges table.");
                } else {
                    return true;
                }
            } else {
                LOGGER.error("Null edges column.");
            }
        } catch (DriverException ex) {
            LOGGER.error("Problem verifying existence of column {}.",
                         edgeOrientationColumnName);
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
    private int parseVertex(Value value) {
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
    public int parseSource(Value value) {
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
    public int parseTarget(Value value) {
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
    public int[] parseDestinationsString(Value value) {
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
