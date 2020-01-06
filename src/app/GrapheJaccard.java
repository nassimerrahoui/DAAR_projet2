package app;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class GrapheJaccard {
	private ArrayList<Node> sommets;

	public GrapheJaccard(ArrayList<String> files, double edgeThreshold) {
		sommets = new ArrayList<Node>();

		for (String filename : files)
			sommets.add(new Node(filename));

		for (int i = 0; i < sommets.size(); i++)
			sommets.get(i).setVoisins(sommets, edgeThreshold);
	}

	public ArrayList<Node> getSommets() {
		return sommets;
	}

	public Node getNode(String nom) {
		for (Node n : sommets)
			if (n.getName().equals(nom))
				return n;
		return null;
	}

	public static double closeness_stream(Node n, ArrayList<Node> nodes) {
		@SuppressWarnings("unchecked")
		ArrayList<Node> documents = (ArrayList<Node>) nodes.clone();

		// Cas ou le document a etudier est dans la liste
		documents.remove(n);

		HashMap<String, Integer> doc_occurences = n.getIndex();

		double denominateur = documents.stream().map(x -> x.distanceJaccard(doc_occurences)).reduce(0.0, Double::sum);

		if (denominateur == 0.0)
			return 0;

		return (documents.size()) / denominateur;
	}

	public String toString() {
		String result = "";
		for (Node n : sommets) {
			result += n.getName() + "  =>  ";
			HashMap<Node, Double> voisins = n.getVoisins();
			for (Node voisin : voisins.keySet()) {
				result += voisin.getName() + " : " + Math.round(voisins.get(voisin) * 1000.0) / 1000.0 + "  ";
			}
			result += "\n";
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	public void toJSONFile() {
		JSONArray graphe = new JSONArray();
		for (Node n : sommets) {
			JSONObject document = new JSONObject();
			document.put("titre", n.getName());

			JSONArray voisins_json = new JSONArray();
			HashMap<Node, Double> voisins = n.getVoisins();
			for (Node voisin : voisins.keySet()) {
				JSONObject voisin_json = new JSONObject();
				voisin_json.put("nom", voisin.getName());
				voisin_json.put("distance", Math.round(voisins.get(voisin) * 1000.0) / 1000.0);
				voisins_json.add(voisin_json);
			}

			document.put("voisins", voisins_json);
			graphe.add(document);
		}

		try (FileWriter file = new FileWriter("grapheJaccard.json")) {

			file.write(graphe.toJSONString());
			file.flush();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
