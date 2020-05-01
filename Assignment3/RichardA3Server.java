import java.util.Scanner;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class RichardA3Server {

	// Nested class for an undirected weighted graph
	public static class WeightedGraph {
		// Nested class for edge
		private class Edge {
			// Private Edge data
			private int start;
			private int end;
			private int weight;

			// Alternate constructor
			public Edge(int start, int end, int weight) {
				this.start = start;
				this.end = end;
				this.weight = weight;
			}

			// Return the start vertex
			public int getStart() {
				return start;
			}

			// Return the end vertex
			public int getEnd() {
				return end;
			}

			// Return the weight
			public int getWeight() {
				return weight;
			}

		}

		// Private Weighted Graph data
		private LinkedList<Integer> vertices;
		private LinkedList<Edge> edges;

		// Default constructor
		public WeightedGraph() {
			vertices = new LinkedList<Integer>();
			edges = new LinkedList<Edge>();
		}

		// Add a vertex to the graph
		public void addVertex(int v) {
			if (!vertices.contains(v))
				vertices.add(v);
		}

		// Add an edge to the graph
		public void addEdge(int s, int e, int w) {
			edges.add(new Edge(s, e, w));
			edges.add(new Edge(e, s, w));
		}

		// Remove an edge from the graph
		public void removeEdge(int s, int e, int w) {
			for (int i = 0; i < edges.size(); i++) {
				if (edges.get(i).getStart() == s && edges.get(i).getEnd() == e)
					edges.remove(i);
				else if (edges.get(i).getStart() == e && edges.get(i).getEnd() == s)
					edges.remove(i);
			}
		}

		// Determine if a graph has an edge
		public boolean hasEdge(int s, int e) {
			for(Edge edge : edges) {
				if(edge.getStart()==s && edge.getEnd()==e)
					return true;
				else if(edge.getStart()==e && edge.getEnd()==s)
					return true;
			}
			return false;
		}

		// Update an edge already on the graph
		public void updateEdge(int s, int e, int w) {
			removeEdge(s, e, w);
			edges.add(new Edge(s, e, w));
			edges.add(new Edge(e, s, w));
		}

		// Use the Bellman-Ford algorithm to find the shortest path
		public int[] bellmanFordModified(int s, int e) {
			int[] dist = new int[vertices.size()];
			Integer[] pred = new Integer[vertices.size()];
			int distToStart, distToEnd;
			int currVertex;

			// Initialize
			for (int i = 0; i < vertices.size(); i++) {
				dist[i] = Integer.MAX_VALUE - 10000;
				pred[i] = null;
			}
			dist[vertices.indexOf(s)] = 0;

			// Main Loop
			for (int i = 0; i < vertices.size(); i++) {
				for (Edge edge : edges) {
					distToStart = dist[vertices.indexOf(edge.getStart())];
					distToEnd = dist[vertices.indexOf(edge.getEnd())];
					if (distToStart + edge.getWeight() < distToEnd) {
						dist[vertices.indexOf(edge.getEnd())] = distToStart + edge.getWeight();
						pred[vertices.indexOf(edge.getEnd())] = edge.getStart();
					}
				}
			}

			//Determine next hop based on predecessor
			currVertex=e;
			while(pred[vertices.indexOf(currVertex)] != s)
				currVertex=pred[vertices.indexOf(currVertex)];

			// Return the next hop and lowest distance
			int[] toReturn = { currVertex, dist[vertices.indexOf(e)] };
			return toReturn;
		}
	}

	// The driver method
	public static void main(String[] args) throws IOException {
		Scanner fileIn = null;
		LinkedList<Integer> destination = new LinkedList<Integer>();
		LinkedList<Integer> nextHop = new LinkedList<Integer>();
		LinkedList<Integer> distance = new LinkedList<Integer>();
		WeightedGraph graph = new WeightedGraph();
		Socket connectionSocket;
		ServerSocket welcomeSocket;
		BufferedReader inFromClient;
		String[] brokenMessage;
		String dvrMessage;
		int messageDestination, messageDistance, howManyTimes, clientNode=0;

		// Insert a Scanner into rt.txt
		try {
			fileIn = new Scanner(new File("rt.txt"));
		} catch (FileNotFoundException e) {
			System.err.println("--File not Found--");
			System.exit(-1);
		}

		// Ignore the first four lines
		for (int i = 0; i < 4; i++)
			fileIn.nextLine();

		// Create the columns
		createColumn(fileIn, destination);
		createColumn(fileIn, nextHop);
		createColumn(fileIn, distance);

		// Ensure the columns are the same length
		if (destination.size() != nextHop.size() || destination.size() != distance.size()) {
			System.err.println("File Formatted Incorrectly");
			System.err.println("--------------------------");
			System.err.println("Destination Entries: " + destination.size());
			System.err.println("Next Hop Entries: " + nextHop.size());
			System.err.println("Distance Entries: " + distance.size());
			System.exit(-1);
		}

		// Print the initial routing table
		System.out.println("Initial Routing Table:");
		printRoutingTable(destination, nextHop, distance);

		// Open TCP socket on port 6789
		welcomeSocket = new ServerSocket(6789);

		// Infinite loop to always allow connection for one client
		while (true) {
			// Accept a connection
			connectionSocket = welcomeSocket.accept();

			// Prepare for input from the client
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

			// Read and parse a DVR message from the client
			dvrMessage = inFromClient.readLine();
			howManyTimes = Character.getNumericValue(dvrMessage.charAt(0));
			brokenMessage = dvrMessage.split("\\(");

			// Determine client node
			for(int i=1;i<howManyTimes;i++)
				if(getDistanceFromMessage(brokenMessage[i])==0)
					clientNode=getDestinationFromMessage(brokenMessage[i]);

			//For every pair in the message
			for (int i = 0; i < howManyTimes; i++) {
				// Determine the destination and distance of the current DVR pair
				messageDestination = getDestinationFromMessage(brokenMessage[i + 1]);
				messageDistance = getDistanceFromMessage(brokenMessage[i + 1]);

				// If the DVR pair is not the distance from the client to itself
				if(messageDestination!=clientNode && messageDistance!=0) {
					// Create a graph from the routing table
					graph = graphFromTable(destination, nextHop, distance);

					// Add an edge from client to destination with a weight of distance, removing any old edges
					if(graph.hasEdge(clientNode, messageDestination)) {
						graph.removeEdge(clientNode, messageDestination, messageDistance);
						graph.removeEdge(messageDestination, clientNode, messageDistance);
					}
					graph.addEdge(clientNode, messageDestination, messageDistance);

					// Update the the routing table using the Bellman Ford algorithm on the graph
					updateTableByGraph(destination, nextHop, distance, graph);
				}
			}
			// Print the resulting routing table
			System.out.println("\nUpdated Routing Table After Message \"" + dvrMessage + "\":");
			printRoutingTable(destination, nextHop, distance);
		}
	}

	// Create a column of integers from one line of a file
	public static void createColumn(Scanner fileIn, LinkedList<Integer> column) {
		String currLine;
		String[] values;
		currLine = fileIn.nextLine();
		values = currLine.split("\\s");
		for (String value : values)
			if (!value.isBlank())
				column.add(Integer.parseInt((value)));
	}

	// Print the routing table in a nicely formatted manner
	public static void printRoutingTable(LinkedList<Integer> destination, LinkedList<Integer> nextHop, LinkedList<Integer> distance) {
		System.out.println(",-----------------------------------,");
		System.out.println("| Destination   Next Hop   Distance |");
		System.out.println("|===================================|");
		for (int i = 0; i < destination.size(); i++)
			System.out.printf("| %-13d %-10d %-8d |\n", destination.get(i), nextHop.get(i), distance.get(i));
		System.out.println("'-----------------------------------'");
	}

	// Get the destination from a DVR message
	public static int getDestinationFromMessage(String message) {
		String working = "";
		int curr = 0;

		while (message.charAt(curr) != ',') {
			if (!Character.isWhitespace(message.charAt(curr)))
				working = working + "" + message.charAt(curr);
			curr++;
		}

		return Integer.parseInt(working);
	}

	// Get the distance from a DVR message
	public static int getDistanceFromMessage(String message) {
		String working = "";
		int curr = 0;

		while (message.charAt(curr) != ',')
			curr++;
		curr++;
		while (message.charAt(curr) != ')') {
			if (!Character.isWhitespace(message.charAt(curr)))
				working = working + "" + message.charAt(curr);
			curr++;
		}

		return Integer.parseInt(working);
	}

	// Create a undirected weighted graph using a routing table
	public static WeightedGraph graphFromTable(LinkedList<Integer> destination, LinkedList<Integer> nextHop, LinkedList<Integer> distance) {
		WeightedGraph graph = new WeightedGraph();
		int start = 0;
		int temp;

		// Create vertices based on the destination column
		for (Integer v : destination)
			graph.addVertex(v);

		// Determine the start
		for (int i = 0; i < distance.size(); i++)
			if (distance.get(i) == 0)
				start = destination.get(i);

		// Create edges that connect directly to start
		for (int i = 0; i < destination.size(); i++)
			if (destination.get(i) == nextHop.get(i))
				graph.addEdge(start, destination.get(i), distance.get(i));

		// Create every other edge
		for (int i = 0; i < destination.size(); i++) {
			if (destination.get(i) != start && destination.get(i) != nextHop.get(i)) {
				temp = distance.get(i) - distance.get(destination.indexOf(nextHop.get(i)));
				graph.addEdge(destination.get(i), nextHop.get(i), temp);
			}
		}

		return graph;
	}

	// Update columns of the routing table using the Bellman Ford algorithm and a graph
	public static void updateTableByGraph(LinkedList<Integer> destination, LinkedList<Integer> nextHop, LinkedList<Integer> distance, WeightedGraph graph) {
		int start = 0;
		int[] results = new int[2];

		// Determine the start
		for (int i = 0; i < distance.size(); i++)
			if (distance.get(i) == 0)
				start = destination.get(i);

		// Determine the distance and next hop to each destination from start
		for (int i = 0; i < destination.size(); i++) {
			if (destination.get(i) != start) {
				results = graph.bellmanFordModified(start, destination.get(i));
				nextHop.set(i, results[0]);
				distance.set(i, results[1]);
			}
		}
	}
}
