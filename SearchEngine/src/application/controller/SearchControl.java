package application.controller;


import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.Map.Entry;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import application.*;

public class SearchControl implements Initializable {

	@FXML
	private TextField textbox1;
	String txtbx1;
	@FXML
	private Button button1;
	@FXML 
	private TextArea txtarea;
	@FXML
	private void btnonclick(ActionEvent event) throws IOException, SQLException {
		txtbx1 = textbox1.getText();
		
		
		ArrayList<String> allWords, stopWords;
		HashMap<String, Double> result = new HashMap<>();
		ArrayList<Search.Node> resultSet = new ArrayList<>();
		stopWords = (ArrayList<String>) Files.readAllLines(Paths.get("stop_words.txt"));
		String searchString = txtbx1.toLowerCase().trim();
		allWords = new ArrayList<String>(Arrays.asList(searchString.split(" ")));
		allWords.removeAll(stopWords);
		System.out.println(allWords.stream().collect(Collectors.joining(" ")));
		
		for (String s : allWords) {
			Search obj = new Search();
			obj.searchTerm("http://localhost/New%20folder/cricket.html", s);
			for (Search.Node n : obj.result) {
//				System.out.printf("%.4f\t%s\n", n.pageRank, n.URL);
				if (!result.containsKey(n.URL))
					result.put(n.URL, n.pageRank);
				else
					result.put(n.URL, result.get(n.URL)+n.pageRank);
			}
		}
		
		for (Entry<String, Double> e : result.entrySet())
			resultSet.add(new Search.Node(e.getKey(), e.getValue()));
		
		resultSet.sort(new Comparator<Search.Node>() {
			@Override
			public int compare(Search.Node o1, Search.Node o2) {
				return (o1.pageRank>o2.pageRank)?-1:(o1.pageRank<o2.pageRank)?1:0;
			}
		});
		
		double denominator = 1;
		if (resultSet.size() != 0)
			denominator = resultSet.get(0).pageRank + resultSet.get(resultSet.size()-1).pageRank;
		
		StringBuffer resultString = new StringBuffer();
		for(Search.Node n : resultSet) {
			System.out.printf("%.4f\t%s\n", n.pageRank/denominator, n.URL);
			resultString.append("\n\t"+n.URL+"\n");
		}
		txtarea.setText(resultString.toString());
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
}