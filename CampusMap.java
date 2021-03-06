package hw7;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.lang.Math;
import hw4.*;

/**
 * CampusMap is a graph generated by CU campus data csv files.
 * 
 */

public class CampusMap {
	public Graph<Entity, Double> graph;
	public HashMap<String, String> nameid;
	public HashMap<String, Entity> entityinfo;

	/**
	 * Default constructor generating an empty CampusMap.
	 */
	public CampusMap() {
		graph = new Graph<Entity, Double>();
		nameid = new HashMap<String, String>();
		entityinfo = new HashMap<String, Entity>();
	}

	/**
	 * Fill the CampusMap with data from node and edge csv files.
	 * 
	 * @param nodefile
	 *            csv file containing node information
	 * @param edgefile
	 *            csv file containing edge information
	 * @effect fill CampusMap with data
	 * @modify Graph and HashMap
	 */
	public void createCampusMap(String nodeFile, String edgeFile) throws IOException {
		// read node file
		Parser.parseNode(nodeFile, nameid, entityinfo);
		// read edge file
		HashSet<HashSet<String>> edgeList = new HashSet<HashSet<String>>();
		Parser.parseEdge(edgeFile, edgeList);
		// set up distance of edge
		for (HashSet<String> edge : edgeList) {
			ArrayList<String> arrayedge = new ArrayList<String>(edge);
			String id1 = arrayedge.get(0);
			String id2 = arrayedge.get(1);
			Entity p1 = entityinfo.get(id1);
			Entity p2 = entityinfo.get(id2);
			double x = p1.getX() - p2.getX();
			double y = p1.getY() - p2.getY();
			double distance = Math.sqrt((x * x) + (y * y));
			graph.addEdge(p1, p2, distance);
			graph.addEdge(p2, p1, distance);
		}
			
	}

	/**
	 * List all buildings in the CampusMap
	 * 
	 * @return a string of all buildings and its id
	 */
	public String listAllBuildings() {
		ArrayList<String> buildings = new ArrayList<String>();

		for (Entry<String, Entity> entityEntry : entityinfo.entrySet()) {
			String name = entityEntry.getValue().getName();
			if (!name.contains("Intersection")) {
				buildings.add(name + "," + entityEntry.getKey() + "\n");
			}
		}

		String ret = "";
		Collections.sort(buildings);

		for (String building : buildings) {
			ret += building;
		}

		return ret;
	}
	
	public String getAllBuildings() {
		ArrayList<String> buildings = new ArrayList<String>();

		for (Entry<String, Entity> entityEntry : entityinfo.entrySet()) {
			String name = entityEntry.getValue().getName();
			if (!name.contains("Intersection")) {
				buildings.add(name + ","  + "\n");
			}
		}

		String ret = "";
		Collections.sort(buildings);

		for (String building : buildings) {
			ret += building;
		}

		return ret;
	}
	
	
	public ArrayList<Edge<Entity, Double>> getPath(String name1, String name2) {
		
		
		String start = name1;
		String end = name2;
		boolean isInt = true;
		try {
			Integer.parseInt(start);
		} catch (Exception e) {
			isInt = false;
		}
		String id1, id2;
		
		if (!isInt) {
			id1 = this.nameid.get(start);
		} else {
			id1 = start;
		}
		isInt = true;
		try {
			Integer.parseInt(end);
		} catch (Exception e) {
			isInt = false;
		}
		if (!isInt) {
			id2 = this.nameid.get(end);
		} else {
			id2 = end;
		}
		ArrayList<Edge<Entity, Double>> allLocation = new ArrayList<Edge<Entity, Double>>();
		Entity e1 = entityinfo.get(id1);
		Entity e2 = entityinfo.get(id2);
		
		if (!e1.equals(e2)) {
			if (e1.getName().equals("Intersection") && !e2.getName().equals("Intersection")) {
				return allLocation;
			}
			if (!e1.getName().equals("Intersection") && e2.getName().equals("Intersection")) {
				return allLocation;
			}
			if (e1.getName().equals("Intersection") && e2.getName().equals("Intersection")) {
				return allLocation;
			}
		} else {
			if (e1.getName().equals("Intersection")) {
				return allLocation;
			} else {
				return allLocation;
			}
		}
		// dijkstra setup
		PriorityQueue<ArrayList<Edge<Entity, Double>>> queue = new PriorityQueue<ArrayList<Edge<Entity, Double>>>(100,
				(p1, p2) -> compare(p1, p2));
		Set<String> visited = new HashSet<String>();
		ArrayList<Edge<Entity, Double>> edges = new ArrayList<Edge<Entity, Double>>();
		edges.add(new Edge<Entity, Double>(e1, e1, 0.000));
		queue.add(edges);
		
		// run dijkstra algorithm on graph
		while (!queue.isEmpty()) {
			ArrayList<Edge<Entity, Double>> minPath = queue.poll();
			Entity minEnd = minPath.get(minPath.size() - 1).getChild();
			double minCost = minPath.get(minPath.size() - 1).getName();
			// successful path
			if (minEnd.equals(e2)) {
				Iterator<Edge<Entity, Double>> itr = minPath.iterator();
				List<Edge<Entity, Double>> edgelist = new ArrayList<Edge<Entity, Double>>();
				while (itr.hasNext())
					edgelist.add(itr.next());
				for (int i = 0; i < edgelist.size(); i++) {
					allLocation.add(edgelist.get(i));
				}
				return allLocation;
			}
			// seen path
			if (visited.contains(minEnd.getId())) {
				continue;
			}
			// unseen path
			Set<Edge<Entity, Double>> alledge = graph.getEdges(minEnd);
			for (Edge<Entity, Double> e : alledge) {
				if (!visited.contains(e.getChild().getId())) {
					double newCost = minCost + e.getName();
					ArrayList<Edge<Entity, Double>> newPath = new ArrayList<Edge<Entity, Double>>(minPath);
					newPath.add(new Edge<Entity, Double>(minEnd, e.getChild(), newCost));
					queue.add(newPath);
				}
			}
			// mark seen
			visited.add(minEnd.getId());
		}
		return allLocation;
	}

	/**
	 * find the shortest path between given building using Dijkstra's algorithm.
	 * 
	 * @param name1
	 *            name of starting node
	 * @param name2
	 *            name of ending node
	 * @return string of path from name1 to name2 or string of exception
	 */
	public String findPath(String name1, String name2) {
		String start = name1;
		String end = name2;
		boolean isInt = true;
		try {
			Integer.parseInt(start);
		} catch (Exception e) {
			isInt = false;
		}
		String id1, id2;
		if (!isInt) {
			id1 = this.nameid.get(start);
		} else {
			id1 = start;
		}
		isInt = true;
		try {
			Integer.parseInt(end);
		} catch (Exception e) {
			isInt = false;
		}
		if (!isInt) {
			id2 = this.nameid.get(end);
		} else {
			id2 = end;
		}
		if (id1 == null) {
			if (id2 == null) {
				if (start == end) {
					return "Unknown building: [" + name1 + "]\n";
				} else {
					return "Unknown building: [" + name1 + "]\n" + "Unknown building: [" + name2 + "]\n";
				}
			} else {
				return "Unknown building: [" + name1 + "]\n";
			}
		}
		if (id2 == null && id1 != null) {
			return "Unknown building: [" + name2 + "]\n";
		}
		Entity e1 = entityinfo.get(id1);
		Entity e2 = entityinfo.get(id2);

		if (!e1.equals(e2)) {
			if (e1.getName().equals("Intersection") && !e2.getName().equals("Intersection")) {
				return "Unknown building: [" + name1 + "]\n";
			}
			if (!e1.getName().equals("Intersection") && e2.getName().equals("Intersection")) {
				return "Unknown building: [" + name2 + "]\n";
			}
			if (e1.getName().equals("Intersection") && e2.getName().equals("Intersection")) {
				return "Unknown building: [" + name1 + "]\n" + "Unknown building: [" + name2 + "]\n";
			}
		} else {
			if (e1.getName().equals("Intersection")) {
				return "Unknown building: [" + name1 + "]\n";
			} else {
				return "Path from " + e1.getName() + " to " + e1.getName() + ":\n"
						+ "Total distance: 0.0 feet.\n";
			}
		}
		// dijkstra setup
		PriorityQueue<ArrayList<Edge<Entity, Double>>> queue = new PriorityQueue<ArrayList<Edge<Entity, Double>>>(100,
				(p1, p2) -> compare(p1, p2));
		Set<String> visited = new HashSet<String>();
		ArrayList<Edge<Entity, Double>> edges = new ArrayList<Edge<Entity, Double>>();
		edges.add(new Edge<Entity, Double>(e1, e1, 0.000));
		queue.add(edges);
		// run dijkstra algorithm on graph
		while (!queue.isEmpty()) {
			ArrayList<Edge<Entity, Double>> minPath = queue.poll();
			Entity minEnd = minPath.get(minPath.size() - 1).getChild();
			double minCost = minPath.get(minPath.size() - 1).getName();
			// successful path
			if (minEnd.equals(e2)) {
				String route = "";
				Iterator<Edge<Entity, Double>> itr = minPath.iterator();
				List<Edge<Entity, Double>> edgelist = new ArrayList<Edge<Entity, Double>>();
				while (itr.hasNext())
					edgelist.add(itr.next());
				double cost = 0.000;
				for (int i = 1; i < edgelist.size(); i++) {
					String direction = getDirection(edgelist.get(i).getChild(), edgelist.get(i - 1).getChild());
					String second = edgelist.get(i).getChild().getName();
					if (second == "Intersection") {
						second += " ";
						second += edgelist.get(i).getChild().getId();
					}
					double edge = edgelist.get(i).getName() - edgelist.get(i - 1).getName();
					route += "	Walk " + direction + " to (" + second + ")\n";
					cost += edge;
				}
				route += "Total distance: " + String.format("%.1f", cost*0.8) + " feet.\n";
				return "Path from " + e1.getName() + " to " + e2.getName() + ":\n" + route;
			}
			// seen path
			if (visited.contains(minEnd.getId())) {
				continue;
			}
			// unseen path
			Set<Edge<Entity, Double>> alledge = graph.getEdges(minEnd);
			for (Edge<Entity, Double> e : alledge) {
				if (!visited.contains(e.getChild().getId())) {
					double newCost = minCost + e.getName();
					ArrayList<Edge<Entity, Double>> newPath = new ArrayList<Edge<Entity, Double>>(minPath);
					newPath.add(new Edge<Entity, Double>(minEnd, e.getChild(), newCost));
					queue.add(newPath);
				}
			}
			// mark seen
			visited.add(minEnd.getId());
		}
		return "There is no path from " + e1.getName() + " to " + e2.getName() + ".\n";
	}

	/**
	 * Comparator for PQ
	 * 
	 * @param p1
	 * @param p2
	 * @return result
	 */
	public static int compare(ArrayList<Edge<Entity, Double>> p1, ArrayList<Edge<Entity, Double>> p2) {
		double weight1 = 0;
		double weight2 = 0;
		for (int a = 0; a < p1.size(); a++) {
			weight1 += (p1.get(a).getName());
		}
		for (int a = 0; a < p2.size(); a++) {
			weight2 += (p2.get(a).getName());
		}
		return Double.compare(weight1, weight2);
	}

	/**
	 * Direction finder for two building/intersection's relative direction.
	 * 
	 * @param starting
	 *            building
	 * @param ending
	 *            building
	 * @return String representing direction
	 */
	public String getDirection(Entity start, Entity end) {
		double x1 = start.getX();
		double y1 = start.getY();
		double x2 = end.getX();
		double y2 = end.getY();
		double angle;
		//
		if (x2 < x1) {
			angle = Math.toDegrees(Math.acos((y2 - y1) / (Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)))));
			if (0 <= angle && angle < 22.5)
				return "North";
			else if (22.5 <= angle && angle < 67.5)
				return "NorthEast";
			else if (67.5 <= angle && angle < 112.5)
				return "East";
			else if (112.5 <= angle && angle < 157.5)
				return "SouthEast";
			else
				return "South";
		} else {
			angle = Math.toDegrees(Math.acos((y2 - y1) / (Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)))));
			if (0 <= angle && angle < 22.5)
				return "South";
			else if (22.5 <= angle && angle < 67.5)
				return "SouthWest";
			else if (67.5 <= angle && angle < 112.5)
				return "West";
			else if (112.5 <= angle && angle < 157.5)
				return "NorthWest";
			else
				return "North";
		}
	}
}