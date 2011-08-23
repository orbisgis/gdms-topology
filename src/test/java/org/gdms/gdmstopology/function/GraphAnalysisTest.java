package org.gdms.gdmstopology.function;

import org.gdms.data.DataSource;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.ObjectDriver;
import org.gdms.gdmstopology.TopologySetUpTest;

/**
 *
 * @author ebocher
 */
public class GraphAnalysisTest extends TopologySetUpTest {

        public void testST_ShortestPath() throws Exception {
                ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
                DataSource[] tables = new DataSource[]{dsf.getDataSource(GRAPH_EDGES)};
                int source = 3;
                int target = 4;
                ObjectDriver result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target)}, null);
                result.start();
                assertTrue(result.getRowCount() == 1);
                assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING ( 191.77905737704918 272.73798360655735 10, 213.28322950819674 185.6593606557377 20 )")));
                result.stop();

                source = 4;
                target = 3;
                result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target)}, null);
                assertTrue(result == null);

                source = 4;
                target = 3;
                result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue(true)}, null);
                result.start();
                assertTrue(result.getRowCount() == 1);
                assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING ( 191.77905737704918 272.73798360655735 10, 213.28322950819674 185.6593606557377 20 )")));
                result.stop();

                source = 3;
                target = 8;
                result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue(true)}, null);
                result.start();
                assertTrue(result.getRowCount() == 3);
                result.stop();
        }
}
