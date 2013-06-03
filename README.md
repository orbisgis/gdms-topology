# GDMS-Topology [![Build  Status](https://travis-ci.org/irstv/gdms-topology.png?branch=master)](https://travis-ci.org/irstv/gdms-topology)

GDMS-Topology is an [OSGi](http://www.osgi.org/Main/HomePage) plugin that brings
shortest path calculations and network analysis functionality to
[OrbisGIS](http://www.orbisgis.org/).
Underneath the hood, GDMS-Topology uses
[Java-Network-Analyzer](https://github.com/agouge/Java-Network-Analyzer).

Networks (transportation, hydrological, etc.) are represented as [mathematical
graphs](http://en.wikipedia.org/wiki/Graph_theory) which may be considered to be
directed, edge-reversed or undirected. For directed (and reversed) graphs, the
user may specify individual edge orientations. The user may specify individual
edge weights, or omit them to consider the graph as unweighted.

### Shortest path calculations: `ST_ShortestPathLength`
Optimized algorithms are provided for computing distances:

* **One-to-One**: Source to destination
* **One-to-All**: Source to all possible destinations
* **Many-to-Many**: Distance matrices

### Network analysis:
[`ST_GraphAnalysis`](https://github.com/irstv/gdms-topology/wiki/Graph-analysis-on-a-transportation-network)
In order to study the global structure of a network, it is possible to calculate
the following standard centrality measures:

* [Betweenness centrality](http://en.wikipedia.org/wiki/Betweenness_centrality)
* [Closeness
  centrality](http://en.wikipedia.org/wiki/Centrality#Closeness_centrality)

### Other functionalities
* `ST_StrahlerStreamOrder`: [Strahler stream
  order](http://en.wikipedia.org/wiki/Strahler_number) for hydrological networks
