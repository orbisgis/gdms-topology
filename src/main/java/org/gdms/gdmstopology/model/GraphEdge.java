package org.gdms.gdmstopology.model;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * Encapsulate a Feature into a DefaultWeightedEdge [NOTE : this is the only
 * method I found to use weighted graph, because AbstractBaseGraph use the
 * following assert : assert (e instanceof DefaultWeightedEdge)] collections.
 * 
 * @author Michael Michaud
 * @version 0.1 (2007-04-21)
 */
public class GraphEdge extends DefaultWeightedEdge {

       
        private static final long serialVersionUID = -1862257432554700123L;
        private Integer source;
        private Integer target;
        private double weight = 1;
        private  long rowId =-1;

         /**
         * The graph edge object used to manage edge properties.
         */
        public GraphEdge(Integer sourceVertex, Integer targetVertex, double weight, long rowId) {
                this.source = sourceVertex;
                this.target = targetVertex;
                this.weight = weight;
                this.rowId=rowId;
        }       
       

        @Override
        public double getWeight() {
                return weight;
        }

        @Override
        public Integer getSource() {
                return source;
        }

        @Override
        public Integer getTarget() {
                return target;
        }

        public long getRowId() {
                return rowId;
        }
        

        @Override
        public String toString() {
                return "(" + source + " : " + target + " : " + weight + ")";
        }
}
