/*
 * OrbisGIS is a GIS application dedicated to scientific spatial simulation.
 * This cross-platform GIS is developed at French IRSTV institute and is able to
 * manipulate and create vector and raster spatial information. 
 * 
 * OrbisGIS is distributed under GPL 3 license. It is produced by the "Atelier SIG"
 * team of the IRSTV Institute <http://www.irstv.fr/> CNRS FR 2488.
 * 
 * Copyright (C) 2007-2012 IRSTV (FR CNRS 2488)
 * 
 * This file is part of OrbisGIS.
 * 
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 * 
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.gdms.gdmstopology;

import java.util.Dictionary;
import java.util.Hashtable;
import org.gdms.gdmstopology.function.ST_BlockIdentity;
import org.gdms.gdmstopology.centrality.ST_GraphAnalysis;
import org.gdms.gdmstopology.centrality.ST_Strahler;
import org.gdms.gdmstopology.function.ST_ConnectedComponents;
import org.gdms.gdmstopology.function.ST_FindReachableEdges;
import org.gdms.gdmstopology.function.ST_Graph;
import org.gdms.gdmstopology.function.ST_MFindReachableEdges;
import org.gdms.gdmstopology.function.ST_MShortestPath;
import org.gdms.gdmstopology.function.ST_MShortestPathLength;
import org.gdms.gdmstopology.function.ST_PlanarGraph;
import org.gdms.gdmstopology.function.ST_ShortestPath;
import org.gdms.gdmstopology.function.ST_ShortestPathLength;
import org.gdms.gdmstopology.function.ST_SubGraphStatistics;
import org.gdms.gdmstopology.function.ST_ToLineNoder;
import org.gdms.sql.function.Function;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Activator, register GDMS Sql function related to topology
 */
public class Activator implements BundleActivator {    
    private BundleContext context;        
    @Override
    public void start(BundleContext context) throws Exception {
        this.context = context;
        System.out.println("Activator of org.gdms.gdmstopology starting..");
        // Register dummy sql function service
        reg(new ST_BlockIdentity());
        reg(new ST_Graph());
        reg(new ST_PlanarGraph());
        reg(new ST_ShortestPath());
        reg(new ST_ShortestPathLength());
        reg(new ST_ToLineNoder());
        reg(new ST_MShortestPathLength());
        reg(new ST_FindReachableEdges());
        reg(new ST_MFindReachableEdges());
        reg(new ST_MShortestPath());
        reg(new ST_SubGraphStatistics());
        reg(new ST_ConnectedComponents());
        reg(new ST_GraphAnalysis());
        reg(new ST_Strahler());
    }
    private void reg(Function gdmsFunc) {
        // Dict for visual hint for service list 
        // inspect service capability #id
        Dictionary<String,String> prop = new Hashtable<String, String>();
        prop.put("name",gdmsFunc.getName()); 
        context.registerService(Function.class,
                gdmsFunc,
                prop);
    }
    @Override
    public void stop(BundleContext context) throws Exception {
        System.out.println("Activator of org.gdms.gdmstopology stopping..");
    }    
}
