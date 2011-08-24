package org.gdms.gdmstopology.function;

import org.junit.Test;
import org.gdms.data.DataSource;
import org.gdms.data.values.Value;
import org.gdms.data.values.ValueFactory;
import org.gdms.driver.DataSet;
import org.gdms.gdmstopology.TopologySetUpTest;

import static org.junit.Assert.*;

/**
 *
 * @author ebocher
 */
public class GraphAnalysisTest extends TopologySetUpTest {

        @Test
        public void testST_ShortestPath() throws Exception {
                ST_ShortestPath sT_ShortestPath = new ST_ShortestPath();
                DataSource ds = dsf.getDataSource(GRAPH_EDGES);
                ds.open();
                DataSet[] tables = new DataSet[]{ds};
                int source = 3;
                int target = 4;
                DataSet result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target)}, null);
                assertTrue(result.getRowCount() == 1);
                assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING ( 191.77905737704918 272.73798360655735 10, 213.28322950819674 185.6593606557377 20 )")));


                source = 4;
                target = 3;
                result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target)}, null);
                assertTrue(result.getRowCount()==0);

                source = 4;
                target = 3;
                result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue(true)}, null);
               
                assertTrue(result.getRowCount() == 1);
                assertTrue(result.getFieldValue(0, 0).getAsGeometry().equals(wktReader.read("LINESTRING ( 191.77905737704918 272.73798360655735 10, 213.28322950819674 185.6593606557377 20 )")));
                

                source = 3;
                target = 8;
                result = sT_ShortestPath.evaluate(dsf, tables, new Value[]{ValueFactory.createValue(source), ValueFactory.createValue(target), ValueFactory.createValue(true)}, null);
                
                assertTrue(result.getRowCount() == 3);
                ds.close();
               
        }
}
