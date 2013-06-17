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
package org.gdms.gdmstopology.parse;

import org.gdms.data.types.Type;
import org.gdms.data.types.TypeFactory;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.memory.MemoryDataSetDriver;
import org.gdms.gdmstopology.function.ST_ShortestPathLength;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the parsing methods of the graph function parser.
 *
 * @author Adam Gouge
 */
public class GraphFunctionParserTest {

    @Test
    public void parseDestinationsTest() {
        int[] actual = new GraphFunctionParser().parseDestinationsString(
                ValueFactory.createValue("2, 34,   217"));
        int[] expected = new int[]{2, 34, 217};
        for (int i = 0; i < expected.length; i++) {
            assertTrue(actual[i] == expected[i]);
        }
    }

    @Test
    public void testDirectedString() {
        GraphFunctionParser p = new GraphFunctionParser();
        String testString = "  diRecTEd - adfasd ";
        MemoryDataSetDriver edges =
                new MemoryDataSetDriver(
                new String[]{"adfasd"},
                new Type[]{TypeFactory.createType(Type.INT)});
        
        assertTrue(p.isDirectedString(testString));
        assertFalse(p.isReversedString(testString));
        assertFalse(p.isUndirectedString(testString));
        assertFalse(p.isWeightsString(testString));

        p.parseStringArgument(edges, ValueFactory.createValue(testString));
        assertEquals(ST_ShortestPathLength.DIRECTED, p.getGlobalOrientation());
        assertEquals("adfasd", p.getEdgeOrientationColumnName());
        assertEquals(null, p.getWeightsColumn());
    }

    @Test
    public void testReversedString() {
        GraphFunctionParser p = new GraphFunctionParser();
        String testString = "  rEVersEd - bdqesd    ";
        MemoryDataSetDriver edges =
                new MemoryDataSetDriver(
                new String[]{"bdqesd"},
                new Type[]{TypeFactory.createType(Type.INT)});
        
        assertFalse(p.isDirectedString(testString));
        assertTrue(p.isReversedString(testString));
        assertFalse(p.isUndirectedString(testString));
        assertFalse(p.isWeightsString(testString));

        p.parseStringArgument(edges, ValueFactory.createValue(testString));
        assertEquals(ST_ShortestPathLength.REVERSED, p.getGlobalOrientation());
        assertEquals("bdqesd", p.getEdgeOrientationColumnName());
        assertEquals(null, p.getWeightsColumn());
    }

    @Test
    public void testUndirectedString() {
        GraphFunctionParser p = new GraphFunctionParser();
        String testString = "  UnDiRecTEd - q w98765e4 er2 3  ";
        assertFalse(p.isDirectedString(testString));
        assertFalse(p.isReversedString(testString));
        assertTrue(p.isUndirectedString(testString));
        assertFalse(p.isWeightsString(testString));

        p.parseStringArgument(null, ValueFactory.createValue(testString));
        assertEquals(ST_ShortestPathLength.UNDIRECTED, p.getGlobalOrientation());
        assertEquals(null, p.getEdgeOrientationColumnName());
        assertEquals(null, p.getWeightsColumn());
    }

    @Test
    public void testWeightsString() {
        GraphFunctionParser p = new GraphFunctionParser();
        String testString = "  Anything_OtherThanOrientation997_But"
                            + "NoSPACESorIllegalCharacters";

        assertFalse(p.isDirectedString(testString));
        assertFalse(p.isReversedString(testString));
        assertFalse(p.isUndirectedString(testString));
        assertTrue(p.isWeightsString(testString));

        p.parseStringArgument(null, ValueFactory.createValue(testString));
        assertEquals(null, p.getGlobalOrientation());
        assertEquals(null, p.getEdgeOrientationColumnName());
        assertEquals("Anything_OtherThanOrientation997_But"
                     + "NoSPACESorIllegalCharacters",
                     p.getWeightsColumn());
    }
}
