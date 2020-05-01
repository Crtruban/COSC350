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
			for (int i = 0; i < edges.size(); i++)
				if (edges.get(i).getStart() == s && edges.get(i).getEnd() == e)
					return;
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

		// Update an edge already on the graph
		public void updateEdge(int s, int e, int w) {
			removeEdge(s, e, w);
			edges.add(new Edge(s, e, w));
			edges.add(new Edge(e, s, w));
		}

		// Use the Bellman-Ford algorithm to find the shortest path
		public int[] bellmanFord(int s, int e) {
			int[] dist = new int[vertices.size()];
			Integer[] pred = new Integer[vertices.size()];
			int distToStart;
			int distToEnd;

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

			// Return the lowest distance and next hop
			int[] toReturn = { pred[vertices.indexOf(e)], dist[vertices.indexOf(e)] };
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
		int messageDestination, messageDistance, howManyTimes;

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

		while (true) {
			connectionSocket = welcomeSocket.accept();

			// Prepare for input from the client
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));

			// Read and parse a DVR message from the client
			dvrMessage = inFromClient.readLine();
			howManyTimes = Character.getNumericValue(dvrMessage.charAt(0));
			brokenMessage = dvrMessage.split("\\(");
			for (int i = 0; i < howManyTimes; i++) {
				messageDestination = getDestinationFromMessage(brokenMessage[i + 1]);
				messageDistance = getDistanceFromMessage(brokenMessage[i + 1]);

				// Update the rows on the routing table that are affected by the DVR message
				updateByMessage(destination, nextHop, distance, messageDestination, messageDistance);

				// Create a graph from the routing table
				graph = graphFromTable(destination, nextHop, distance);

				// Update the the routing table using the Bellman Ford algorithm on the graph
				updateByGraph(destination, nextHop, distance, graph);
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

	// Update columns of the routing table using a DVR message
	public static void updateByMessage(LinkedList<Integer> destination, LinkedList<Integer> nextHop, LinkedList<Integer> distance, int messageDestination, int messageDistance) {
		int originalDistance = 0;
		// Update the row that contains messageDestination as the destination
		for (int i = 0; i < destination.size(); i++) {
			if (destination.get(i) == messageDestination) {
				originalDistance = distance.get(i);
				if (destination.get(i) == nextHop.get(i))
					distance.set(i, messageDistance);
				else if (nextHop.get(i) != 0 && distance.get(destination.indexOf(nextHop.get(i))) > messageDistance) {
					distance.set(i, messageDistance);
					nextHop.set(i, destination.get(i));
				}
				else if (originalDistance < messageDistance)
					distance.set(i, messageDistance);
			}
		}
		// Update any rows that contain messageDestination as the Next Hop and recursively repeat with destination as messageDestination
		for (int i = 0; i < destination.size(); i++) {
			if (nextHop.get(i) == messageDestination && destination.get(i) != messageDestination) {
				distance.set(i, distance.get(i) - (originalDistance - messageDistance));
				updateByMessage(destination, nextHop, distance, destination.get(i), distance.get(i) - (originalDistance - messageDistance));
			}
		}
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
		for (int i = 0; i < distance.size(); i++) {
			if (distance.get(i) == 0) {
				start = destination.get(i);
				break;
			}
		}

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
	public static void updateByGraph(LinkedList<Integer> destination, LinkedList<Integer> nextHop, LinkedList<Integer> distance, WeightedGraph graph) {
		int start = 0;
		int[] results = new int[2];

		// Determine the start
		for (int i = 0; i < distance.size(); i++) {
			if (distance.get(i) == 0) {
				start = destination.get(i);
				break;
			}
		}

		// Determine the distance and predecessor to each destination from start
		for (int i = 0; i < destination.size(); i++) {
			if (destination.get(i) != start) {
				results = graph.bellmanFord(start, destination.get(i));
				nextHop.set(i, results[0]);
				distance.set(i, results[1]);
			}
		}

		// Correct all Next Hops that are equal to the start
		for (int i = 0; i < nextHop.size(); i++)
			if (nextHop.get(i) == start)
				nextHop.set(i, destination.get(i));
	}
}
