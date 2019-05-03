package application.computation.predicting.inferencing;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import application.computation.util.BSIBean;

public class InferencePrediction {

	public static void main(String[] args) throws Exception {

	}

	

	
	public static void mergeResultsToFrom(HashMap<String, HashMap<String, Double>> re_1,
			HashMap<String, HashMap<String, Double>> re_2) {
		for (Entry<String, HashMap<String, Double>> entry1 : re_2.entrySet()) {
			for (Entry<String, Double> entry2 : entry1.getValue().entrySet()) {
				if (re_1.containsKey(entry2.getKey())) {
					if (re_1.get(entry2.getKey()).containsKey(entry1.getKey())) {
						Double value = re_1.get(entry2.getKey()).get(entry1.getKey());
						re_1.get(entry2.getKey()).put(entry1.getKey(), value + entry2.getValue());
					} else {
						re_1.get(entry2.getKey()).put(entry1.getKey(), entry2.getValue());
					}

				} else {
					HashMap<String, Double> map = new HashMap<>();
					map.put(entry1.getKey(), entry2.getValue());
					re_1.put(entry2.getKey(), map);
				}
			}
		}
	}

	public static HashMap<String, HashMap<String, Double>> mergeResultsToFrom(HashMap<String, HashMap<String, Double>> re_1,
			HashMap<String, HashMap<String, Double>> re_2, Double rate) {
		HashMap<String, HashMap<String, Double>> map_1=new HashMap<>();
		for(Entry<String,HashMap<String,Double>> entry_1:re_1.entrySet()){
			HashMap<String,Double> map=new HashMap<>();
			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
				map.put(entry_2.getKey(), entry_2.getValue()*rate);
			}
			map_1.put(entry_1.getKey(), map);
		}
		
		HashMap<String, HashMap<String, Double>> map_2=new HashMap<>();
		for(Entry<String,HashMap<String,Double>> entry_1:re_2.entrySet()){
			HashMap<String,Double> map=new HashMap<>();
			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
				map.put(entry_2.getKey(), entry_2.getValue()*(1-rate));
			}
			map_2.put(entry_1.getKey(), map);
		}
		
		for (Entry<String, HashMap<String, Double>> entry1 : map_2.entrySet()) {
			for (Entry<String, Double> entry2 : entry1.getValue().entrySet()) {
				if (map_1.containsKey(entry2.getKey())) {
					if (map_1.get(entry2.getKey()).containsKey(entry1.getKey())) {
						Double value = map_1.get(entry2.getKey()).get(entry1.getKey());
						map_1.get(entry2.getKey()).put(entry1.getKey(), value + entry2.getValue());
					} else {
						map_1.get(entry2.getKey()).put(entry1.getKey(), entry2.getValue());
					}

				} else {
					HashMap<String, Double> map = new HashMap<>();
					map.put(entry1.getKey(), entry2.getValue());
					map_1.put(entry2.getKey(), map);
				}
			}
		}
		return map_1;
	}
	
	
	
	

	
	
	
	
	/**kkk
	 * FastTargetInferencing.deepwalk
	 * FastTargetInferencing.node2vec
	 * @param type
	 * @param modelfile
	 * @param idxfile
	 * @param datafile
	 * @param removefile
	 * @param bw
	 * @throws Exception
	 */
	public BSIBean network_inferences(String modelfile, String idxfile, String datafile, String drug, String target, String type) throws Exception {
		
		BSIBean bean =new BSIBean();
		long seed=1024;
		
		HashSet<String> allTarget = getAllTarget(datafile);
		HashSet<String> allDrug = getAllDrug(datafile);

		HashMap<String, HashSet<String>> drugToTargetassociations = getDrugTargetAssociations(datafile);
		HashMap<String, HashSet<String>> TargetToDrugassociations = getTargetDrugAssociations(datafile);
		
		
		FastTargetInferencing predict2 = new FastTargetInferencing(modelfile, idxfile);

	
		HashMap<String, Double> results2 = predict2.predictBySimilarDrug(drug,target,
				drugToTargetassociations, allDrug,type);

		FastDrugInferencing predict3 = new FastDrugInferencing(modelfile, idxfile);


		HashMap<String, Double> results3 = predict3.predictBySimilarTarget(target, drug,
				TargetToDrugassociations, allTarget,type);
		
		bean.setDbsi(results2);
		bean.setTbsi(results3);
		return bean;
	}
	
	/**
	 * batch 20190503
	 * @param modelfile
	 * @param idxfile
	 * @param datafile
	 * @param pairs
	 * @param type
	 * @return
	 * @throws Exception
	 */
public HashMap<String,BSIBean> network_inferences(String modelfile, String idxfile, String datafile, HashSet<String> pairs, String type) throws Exception {
		
		
		long seed=1024;
		HashMap<String,BSIBean> map=new HashMap<>();
		HashSet<String> allTarget = getAllTarget(datafile);
		HashSet<String> allDrug = getAllDrug(datafile);

		HashMap<String, HashSet<String>> drugToTargetassociations = getDrugTargetAssociations(datafile);
		HashMap<String, HashSet<String>> TargetToDrugassociations = getTargetDrugAssociations(datafile);
		
		
		FastTargetInferencing predict2 = new FastTargetInferencing(modelfile, idxfile);

		FastDrugInferencing predict3 = new FastDrugInferencing(modelfile, idxfile);
		
		for(String pair:pairs){
			String[] element=pair.split(" ");
			BSIBean bean =new BSIBean();
			
			HashMap<String, Double> results2 = predict2.predictBySimilarDrug(element[0],element[1],
					drugToTargetassociations, allDrug,type);

			HashMap<String, Double> results3 = predict3.predictBySimilarTarget(element[1], element[0],
					TargetToDrugassociations, allTarget,type);
			
			bean.setDbsi(results2);
			bean.setTbsi(results3);
			map.put(pair, bean);
		}
		
		
		
		
		return map;
	}
	
	
	public HashMap<String, HashSet<String>> getAllPotentials(HashMap<String, HashSet<String>> gold,
			HashSet<String> allpotentials, HashMap<String, HashSet<String>> exsistings) {

		HashMap<String, HashSet<String>> all = new HashMap<>();

		for (Entry<String, HashSet<String>> entry : gold.entrySet()) {
			HashSet<String> set = new HashSet<>();
			for (String string : entry.getValue()) {
				set.add(string);
			}
			for (String string : allpotentials) {
				if (!exsistings.get(entry.getKey()).contains(string)) {
					set.add(string);
				}
			}
			all.put(entry.getKey(), set);
		}

		return all;
	}

	public HashSet<String> getCandidate(String candidateNodeFile) throws Exception {
		HashSet<String> set = new HashSet<>();
		BufferedReader br = new BufferedReader(new FileReader(new File(candidateNodeFile)));
		String line = null;
		while ((line = br.readLine()) != null) {
			set.add(line);
		}
		br.close();
		return set;
	}

	public HashSet<String> getAllTarget(String datafile, HashSet<String> nodes) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(datafile)));
		String line = null;
		HashSet<String> set = new HashSet<>();
		while ((line = br.readLine()) != null) {
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);

			while (nxp.hasNext()) {
				Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();

				if (s.startsWith("<http://bio2rdf.org/drugbank:BE") & nodes.contains(s)) {
					set.add(s);
				}
				if (o.startsWith("<http://bio2rdf.org/drugbank:BE") & nodes.contains(o)) {
					set.add(o);
				}
			}
		}
		br.close();
		return set;
	}

	public HashSet<String> getAllDrug(String datafile, HashSet<String> nodes) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(datafile)));
		String line = null;
		HashSet<String> set = new HashSet<>();
		while ((line = br.readLine()) != null) {
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);

			while (nxp.hasNext()) {
				Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();

				if (s.startsWith("<http://bio2rdf.org/drugbank:DB") & nodes.contains(s)) {
					set.add(s);
				}
				if (o.startsWith("<http://bio2rdf.org/drugbank:DB") & nodes.contains(o)) {
					set.add(o);
				}
			}
		}
		br.close();
		return set;
	}

	
	
	public HashSet<String> getAllTarget(String datafile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(datafile)));
		String line = null;
		HashSet<String> set = new HashSet<>();
		while ((line = br.readLine()) != null) {
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);

			while (nxp.hasNext()) {
				Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();

				if (s.startsWith("<http://bio2rdf.org/drugbank:BE")) {
					set.add(s);
				}
				if (o.startsWith("<http://bio2rdf.org/drugbank:BE")) {
					set.add(o);
				}
			}
		}
		br.close();
		return set;
	}

	public HashSet<String> getAllDrug(String datafile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(datafile)));
		String line = null;
		HashSet<String> set = new HashSet<>();
		while ((line = br.readLine()) != null) {
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);

			while (nxp.hasNext()) {
				Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();

				if (s.startsWith("<http://bio2rdf.org/drugbank:DB")) {
					set.add(s);
				}
				if (o.startsWith("<http://bio2rdf.org/drugbank:DB")) {
					set.add(o);
				}
			}
		}
		br.close();
		return set;
	}
	
	
	public HashMap<String, HashSet<String>> getDrugTargetAssociations(String datafile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(datafile)));
		String line = null;
		HashMap<String, HashSet<String>> associations = new HashMap<>();
		while ((line = br.readLine()) != null) {
			if (!line.contains("\"")) {
				InputStream inputStream = new ByteArrayInputStream(line.getBytes());
				NxParser nxp = new NxParser();
				nxp.parse(inputStream);
				while (nxp.hasNext()) {

					Node[] quard = nxp.next();
					String s = quard[0].toString().trim();
					String p = quard[1].toString().trim();
					String o = quard[2].toString().trim();
					if (s.startsWith("<http://bio2rdf.org/drugbank:DB")
							& p.equals("<http://bio2rdf.org/MultiPartiteNetwork_vocabulary:Drug-Target>")
							& o.startsWith("<http://bio2rdf.org/drugbank:BE")) {

						if (associations.containsKey(s)) {
							associations.get(s).add(o);
						} else {
							HashSet<String> set = new HashSet<>();
							set.add(o);
							associations.put(s, set);
						}

					}
				}
			}
		}
		br.close();
		return associations;
	}

	public HashMap<String, HashSet<String>> getTargetDrugAssociations(String datafile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(datafile)));
		String line = null;
		HashMap<String, HashSet<String>> associations = new HashMap<>();
		while ((line = br.readLine()) != null) {
			if (!line.contains("\"")) {
				InputStream inputStream = new ByteArrayInputStream(line.getBytes());
				NxParser nxp = new NxParser();
				nxp.parse(inputStream);
				while (nxp.hasNext()) {

					Node[] quard = nxp.next();
					String s = quard[0].toString().trim();
					String p = quard[1].toString().trim();
					String o = quard[2].toString().trim();
					if (s.startsWith("<http://bio2rdf.org/drugbank:DB")
							& p.equals("<http://bio2rdf.org/MultiPartiteNetwork_vocabulary:Drug-Target>")
							& o.startsWith("<http://bio2rdf.org/drugbank:BE")) {

						if (associations.containsKey(o)) {
							associations.get(o).add(s);
						} else {
							HashSet<String> set = new HashSet<>();
							set.add(s);
							associations.put(o, set);
						}

					}
				}
			}
		}
		br.close();
		return associations;
	}
}
