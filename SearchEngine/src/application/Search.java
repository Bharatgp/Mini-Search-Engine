//import java.io.IOException;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//
//class Edge {
//	Node from, to;
//	boolean relevant;
//	
//	public Edge(Node from, Node to) {
//		this.from = from;
//		this.to = to;
//		relevant = false;
//	}
//}
//
//class Node {
//	String URL;
//	ArrayList<Edge> outgoingEdges;
//	
//	public Node(String URL) {
//		this.URL = URL;
//		outgoingEdges = new ArrayList<>();
//	}
//}
//
//class Graph {
//	HashMap<String, Node> map;
//	
//	public Graph() {
//		map = new HashMap<>();
//	}
//	
//	void insertNode(String URL) {
//		map.put(URL, new Node(URL));
//	}
//	
//	void insertEdge(Edge edge) {
//		map.get(edge.from.URL).outgoingEdges.add(edge);
//	}
//	
//	void insertEdge(String fromURL, String toURL) {
//		map.get(fromURL).outgoingEdges.add(new Edge(map.get(fromURL), map.get(toURL)));
//	}
//	
//	Edge getEdge(String fromURL, String toURL) {
//		Edge edge = null;
//		for (Edge e : map.get(fromURL).outgoingEdges)
//			if (e.to.URL.equals(toURL)) {
//				edge = e;
//				break;
//			}
//		return edge;
//	}
//	
//	void print() {
//		HashMap<String, Character> names = new HashMap<>();
//		char ch = 'A';
//		
//		System.out.println("Nodes:");
//		for (Node n : map.values()) {
//			System.out.println(ch+"\t"+n.URL);
//			names.put(n.URL, ch);
//			ch++;
//		}
//		
//		System.out.println("Edges:");
//		for (Node n : map.values()) {
//			System.out.print(names.get(n.URL)+" -> ");
//			for (Edge e : n.outgoingEdges) {
////				if (e.relevant)
//					System.out.print(names.get(e.to.URL)+", ");
//			}
//			System.out.println();
//		}
//	}
//}
//
//class Main {
//	
//	public static DB db = new DB();
//	public static Graph graph;
//	public static int path[][];
//	
//	public static void main(String[] args) throws SQLException, IOException {
//		db.runSql2("TRUNCATE Record;");
//		graph = new Graph();
//		
//		graph.insertNode("");
//		
//		processPage("", "http://localhost/New%20folder/");
//		
//		graph.print();
//		
//		int x = graph.map.size();
//		path = new int[x][x];
//		createAdjacencyMatrix(graph, path);
//		for (int i = 0; i < x; i++) {
//			for (int j = 0; j < x; j++) {
//				System.out.print(path[i][j]+"\t");
//			}
//			System.out.println();
//		}
//	}
// 
//	public static void processPage(String fromURL, String URL) throws SQLException, IOException{
//		//check if the given URL is already in database
//		String sql = "select * from Record where URL = '"+URL+"'";
//		ResultSet rs = db.runSql(sql);
//		
//		if (!graph.map.containsKey(URL))
//			graph.insertNode(URL);
//		graph.insertEdge(fromURL, URL);
//		
//		if (rs.next()) {
//			
//		} else {
//			//store the URL to database to avoid parsing again
//			sql = "INSERT INTO  `Crawler`.`Record` " + "(`URL`) VALUES " + "(?);";
//			PreparedStatement stmt = db.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
//			stmt.setString(1, URL);
//			stmt.execute();
// 
//			//get useful information
//			Document doc = Jsoup.connect(URL).get();
//
//			if(doc.text().contains("Bharat")) {
//				graph.getEdge(fromURL, URL).relevant = true;
//				System.out.println(URL);
//			}
//			
//			//get all links and recursively call the processPage method
//			Elements questions = doc.select("a[href]");
//			for(Element link: questions) {
//				if(link.attr("href").contains("localhost/"))
//					processPage(URL, link.attr("abs:href"));
//			}
//		}
//	}
//	
//	public static void createAdjacencyMatrix(Graph graph, int path[][]) {
//		HashMap<Node, Integer> indices = new HashMap<>();
//		int i = 0;
//		for (Node n : graph.map.values()) {
//			indices.put(n, i);
//			i++;
//		}
//		
//		for (Node n : graph.map.values())
//			for (Edge e : n.outgoingEdges)
////				if (e.relevant)
//					path[indices.get(e.from)][indices.get(e.to)] = 1;
//	}
//}

package application;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Comparator;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
 
public class Search {
	
	public static class Node {
		public String URL;
		public double pageRank;
		
		public Node(String URL, double pageRank) {
			this.URL = URL;
			this.pageRank = pageRank;
		}
	}
	
	public static DB db = new DB();
	public ArrayList<String> nodes;
	public ArrayList<Node> result;
	public int path[][];
	
	public Search() throws SQLException {
		nodes = new ArrayList<>();
		result = new ArrayList<>();
		db.runSql2("TRUNCATE Record;");
	}
	
	public void searchTerm(String URL, String term) throws SQLException, IOException {
//		System.out.println("Searching: "+term);
		result = new ArrayList<>();
		int s = URL.indexOf('/')+2;
		String base = URL.substring(s, URL.indexOf('/', s)+1);
		processPage(URL, base, term);
		path = new int[nodes.size()][nodes.size()];
		createAdjacencyMatrix(nodes, path);
//		for (int i = 0; i < nodes.size(); i++) {
//			for (int j = 0; j < nodes.size(); j++)
//				System.out.print(path[i][j]+"\t");
//			System.out.println();
//		}
		result = calculatePageRank(nodes, path);
	}
	
	public void processPage(String URL, String base, String term) throws SQLException {
		//check if the given URL is already in database
		String sql = "select * from Record where URL = '"+URL+"'";
		ResultSet rs = db.runSql(sql);
		if(rs.next()){
 
		}else{
			//store the URL to database to avoid parsing again
			sql = "INSERT INTO  `Crawler`.`Record` " + "(`URL`) VALUES " + "(?);";
			PreparedStatement stmt = db.conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			stmt.setString(1, URL);
			stmt.execute();
 
			//get useful information
			Document doc;
			try {
				doc = Jsoup.connect(URL).get();
					if(doc.text().toLowerCase().contains(term.toLowerCase())){
						nodes.add(URL);
//						System.out.println(URL);
					}
		 
					//get all links and recursively call the processPage method
					Elements questions = doc.select("a[href]");
					for(Element link: questions){
						if(link.attr("href").contains(base) || link.attr("href").contains("http"))
							processPage(link.attr("abs:href"), base, term);
					}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public static void createAdjacencyMatrix(ArrayList<String> nodes, int path[][]) throws IOException {
		for (String i : nodes) {
			Document document = Jsoup.connect(i).get();
			Elements elements = document.select("a[href]");
			for (String j : nodes) {
				if (j != i) {
					for (Element element : elements) {
						if(element.attr("href").contains(j)) {
							path[nodes.indexOf(i)][nodes.indexOf(j)] = 1;
						}
					}
				}
			}
		}
	}
	
	public static ArrayList<Node> calculatePageRank(ArrayList<String> nodes, int path[][]) {
		final double DAMPING_FACTOR = 0.85;
		
		ArrayList<Node> result = new ArrayList<>();
		int iteration = 0, n = nodes.size();
		int i, j, k;
		
		// Initializing page ranks
		for (String s : nodes)
			result.add(new Node(s, 1.0/n));
		
		double temp[] = new double[n];
		while (iteration < 2) {
			
			// Store the PageRank for All Nodes in Temporary Array
			for(k=0; k < n; k++) {
				temp[k] = result.get(k).pageRank;
				result.get(k).pageRank = 0;
			}
			
			for(i = 0; i < n; i++) {
				for(j = 0; j < n; j++) {
					if(path[j][i] == 1) {
						k = 0;
						int outgoingLinks = 0;  // Count the Number of Outgoing Links for each (jth) Node
						while(k < n) {
							if(path[j][k] == 1)
								outgoingLinks += 1; // Counter for Outgoing Links
							k += 1;
				        }
				        // Calculate PageRank     
				        result.get(i).pageRank += temp[j] * (1.0/outgoingLinks);
			         }
				}
			}
			iteration++;
		}
			
		// Add the Damping Factor to PageRank
		for(k = 0; k < n; k++)
			result.get(k).pageRank = (1 - DAMPING_FACTOR) + DAMPING_FACTOR*result.get(k).pageRank;
			
		Comparator<Node> comp = new Comparator<Search.Node>() {
			@Override
			public int compare(Node o1, Node o2) {
				return (o1.pageRank>o2.pageRank)?-1:(o1.pageRank<o2.pageRank)?1:0;
			}			
		};
		result.sort(comp);
		return result;
	}
}


