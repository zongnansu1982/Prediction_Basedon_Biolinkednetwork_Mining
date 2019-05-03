package application.compuation.predicting.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import application.computation.predicting.classficiation.Classification;
import application.computation.predicting.classficiation.WekaDataGenerator_negativeSelection_case12;
import application.computation.predicting.inferencing.InferencePrediction;
import application.computation.predicting.negativeSelection.PU_ns;
import application.computation.util.BSIBean;
import application.computation.util.BinaryOperator;

public class Predictor {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
//		Predictor predictor=new Predictor(args[0], args[1], args[2]);
//		predictor.predict(args[3]);
		Predictor predictor=new Predictor();
//		predictor.predict_bykeywords("Glycinamid,Genome polyprotein");
		predictor.predict_bykeywords("DB02236,BE0004148\nDB07099,BE0004385\nDB01934,BE0001791");
	}

	String classifier = Classification.svm;
	Integer trainSpace_type = 4;
	String stardard_type = "fs";
	String pu_type = PU_ns.nb;
	String method = BinaryOperator.Hadamard;
	Double spyRate = 0.1;
	Double space = 1.0;
	Integer dimension = 64;
	Integer valid_interaction = -1;
	String thread_method = "z_5";
	Double BSI_rate = 0.5;
	Double BSI_PU_rate = 0.5;

	String embeddingfile = "model/entire_training_64_40_5_10_2.0_1.0_tab.emb";
	String idxfile = "data/network.idx";
	String networkfile = "data/network_bi.nt";

	String arff_trainfile = "model/arff_train.arff";
	String arff_testfile = "model/arff_test.arff";
	String classification_model_file = "model/classification.model";
	String labelfile = "data/labels.tsv";
	String prediction_result_file="data/prediction_result.csv";
	public Predictor(String networkfile, String idxfile, String embeddingfile) {
		this.embeddingfile = embeddingfile;
		this.idxfile = idxfile;
		this.networkfile = networkfile;
	}
	
	public Predictor() {
	}

	public HashSet<String> getEntity(String string, HashMap<String, String> labels, int position) {
		HashSet<String> set = new HashSet<>();
		for (Entry<String, String> entry : labels.entrySet()) {
			if (entry.getKey().toLowerCase().contains(string)) {
				if (position == 0) {
					if (entry.getKey().contains("drugbank:DB")) {
						set.add(entry.getValue());
					}
				}
				if (position == 1) {
					if (entry.getKey().contains("drugbank:BE")) {
						set.add(entry.getValue());
					}
				}

			}
		}
		return set;
	}

	public void predict_bykeywords(String pairs) throws Exception {

		HashMap<String, String> labels = readLabel();
		String[] lines = pairs.toLowerCase().split("\n");
		
		HashMap<String, Double> all_results=new HashMap<>();
		Double min=Double.MAX_VALUE;
		Double max=Double.MIN_VALUE;
		for (String line : lines) {
			String[] elements = line.split(",");
			HashSet<String> drugs = new HashSet<>();
			HashSet<String> targets = new HashSet<>();

			drugs = getEntity(elements[0].trim(), labels, 0);
			targets = getEntity(elements[1].trim(), labels, 1);
//			System.out.println(drugs);
//			System.out.println(targets);
			HashSet<String> c_pairs=new HashSet<>();
			
			for(String drug:drugs){
				for(String target: targets){
					c_pairs.add(drug + " " + target);
				}
			}
			System.out.println(c_pairs.size());
			HashMap<String, Double> results=new HashMap<>();
			HashMap<String, String> arff_idx = writeARFF_test_batch(dimension, method, c_pairs, idxfile,
					embeddingfile, arff_testfile);

			HashMap<String,BSIBean> beans = new InferencePrediction().network_inferences(embeddingfile, idxfile, networkfile, c_pairs, stardard_type);

			for(Entry<String,BSIBean> entry_1:beans.entrySet()){
				
				HashMap<String, Double> dbsi = entry_1.getValue().getDbsi();

				HashMap<String, Double> tbsi = entry_1.getValue().getTbsi();

				
				HashMap<String, Double> results_tmp = merge(dbsi, tbsi, BSI_rate);
				for(Entry<String,Double> entry_2:results_tmp.entrySet()){
					results.put(entry_2.getKey(), entry_2.getValue());
				}
				
			}
			HashMap<String, Double> results_classification = Classification.predictWithModel(classification_model_file,
					arff_idx, arff_testfile);
			
			
			HashMap<String, Double> results_print = merge(results, results_classification, BSI_PU_rate);	
			
			for(Entry<String,Double> entry:results_print.entrySet()){
				all_results.put(entry.getKey(), entry.getValue());	
				if(entry.getValue()>max){
					max=entry.getValue();
				}
				if(entry.getValue()<min){
					min=entry.getValue();
				}
			}
			
		}

		BufferedWriter bw =new BufferedWriter(new FileWriter(new File(prediction_result_file)));

		for (Entry<String, Double> entry : all_results.entrySet()) {
			Double value=(entry.getValue()-min)/(max-min);
			System.out.println(entry.getKey() + " -> " + value);
			bw.write(entry.getKey() + " " + value+"\n");
		}
		bw.flush();
		bw.close();
	}

	
	public void predict_byID(String pairs) throws Exception {

		String[] lines = pairs.toLowerCase().split("\n");
		
		HashMap<String, Double> all_results=new HashMap<>();
		Double min=Double.MAX_VALUE;
		Double max=Double.MIN_VALUE;
		for (String line : lines) {
			String[] elements = line.split(",");
			String drug="<http://bio2rdf.org/drugbank:"+elements[0]+">";
			String target="<http://bio2rdf.org/drugbank:"+elements[1]+">";

			HashMap<String, String> arff_idx = writeARFF_test_single(dimension, method, drug + " " + target, idxfile,
					embeddingfile, arff_testfile);

			BSIBean bean = new InferencePrediction().network_inferences(embeddingfile, idxfile, networkfile, drug, target,
					stardard_type);

			HashMap<String, Double> dbsi = bean.getDbsi();

			HashMap<String, Double> tbsi = bean.getTbsi();

			HashMap<String, Double> results_tmp = merge(dbsi, tbsi, BSI_rate);

			HashMap<String, Double> results_classification = Classification.predictWithModel(classification_model_file,
					arff_idx, arff_testfile);

			HashMap<String, Double> results = merge(results_tmp, results_classification, BSI_PU_rate);
			
			
			for(Entry<String,Double> entry:results.entrySet()){
				all_results.put(entry.getKey(), entry.getValue());	
				if(entry.getValue()>max){
					max=entry.getValue();
				}
				if(entry.getValue()<min){
					min=entry.getValue();
				}
			}
			
		}

		BufferedWriter bw =new BufferedWriter(new FileWriter(new File(prediction_result_file)));

		for (Entry<String, Double> entry : all_results.entrySet()) {
			Double value=(entry.getValue()-min)/(max-min);
			System.out.println(entry.getKey() + " -> " + value);
			bw.write(entry.getKey() + " " + value+"\n");
		}
		bw.flush();
		bw.close();
	}
	
	public HashMap<String, Double> predict(String drug, String target) throws Exception {

		HashMap<String, String> arff_idx = writeARFF_test_single(dimension, method, drug + " " + target, idxfile,
				embeddingfile, arff_testfile);

		BSIBean bean = new InferencePrediction().network_inferences(embeddingfile, idxfile, networkfile, drug, target,
				stardard_type);

		HashMap<String, Double> dbsi = bean.getDbsi();

		HashMap<String, Double> tbsi = bean.getTbsi();

		HashMap<String, Double> results_tmp = merge(dbsi, tbsi, BSI_rate);

		HashMap<String, Double> results_classification = Classification.predictWithModel(classification_model_file,
				arff_idx, arff_testfile);

		HashMap<String, Double> results = merge(results_tmp, results_classification, BSI_PU_rate);

		for (Entry<String, Double> entry : results.entrySet()) {
			System.out.println(entry.getKey() + " -> " + entry.getValue());
		}

		return results;

	}

	public static HashMap<String, Double> merge(HashMap<String, Double> re_1, HashMap<String, Double> re_2,
			Double rate) {

		HashMap<String, Double> map = new HashMap<>();

		if (re_1.size() == re_2.size()) {

			for (Entry<String, Double> entry_2 : re_1.entrySet()) {
				map.put(entry_2.getKey(), entry_2.getValue() * rate);
			}

			for (Entry<String, Double> entry_2 : re_2.entrySet()) {
				if (map.containsKey(entry_2.getKey())) {
					map.put(entry_2.getKey(), map.get(entry_2.getKey()) + entry_2.getValue() * (1 - rate));
				} else {
					map.put(entry_2.getKey(), entry_2.getValue() * (1 - rate));
				}
			}

		} else {
			System.out.println("re_1 size not equals to re_2. ");
			System.exit(0);
		}

		return map;
	}

	public static HashMap<String, String> writeARFF_test_single(int vectorsize, String binaryOperator, String pair,
			String idxFile, String modelFile, String arffTest) throws Exception {
		HashMap<String, String> idx = getIdx(idxFile);
		HashMap<String, ArrayList<Double>> vectors = readModel(modelFile, idx);

		HashMap<String, String> arff_idx = new HashMap<>();

		BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File(arffTest)));
		bw3.write("@relation 'associations'\n");

		if (binaryOperator.equals(BinaryOperator.Add)) {
			for (int i = 0; i < vectorsize * 2; i++) {
				bw3.write("@attribute 'attribute_" + i + "' numeric\n");
			}

		} else {
			for (int i = 0; i < vectorsize; i++) {
				bw3.write("@attribute 'attribute_" + i + "' numeric\n");
			}
		}

		bw3.write("@attribute 'Class' { 'true', 'false'}\n");
		bw3.write("@data\n");

		writeTest_single(pair, binaryOperator, vectors, bw3, arff_idx);

		bw3.flush();

		bw3.close();
		return arff_idx;
	}
	
	
	public static HashMap<String, String> writeARFF_test_batch(int vectorsize, String binaryOperator, HashSet<String> pairs,
			String idxFile, String modelFile, String arffTest) throws Exception {
		HashMap<String, String> idx = getIdx(idxFile);
		HashMap<String, ArrayList<Double>> vectors = readModel(modelFile, idx);

		HashMap<String, String> arff_idx = new HashMap<>();

		BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File(arffTest)));
		bw3.write("@relation 'associations'\n");

		if (binaryOperator.equals(BinaryOperator.Add)) {
			for (int i = 0; i < vectorsize * 2; i++) {
				bw3.write("@attribute 'attribute_" + i + "' numeric\n");
			}

		} else {
			for (int i = 0; i < vectorsize; i++) {
				bw3.write("@attribute 'attribute_" + i + "' numeric\n");
			}
		}

		bw3.write("@attribute 'Class' { 'true', 'false'}\n");
		bw3.write("@data\n");

		writeTest_batch(pairs, binaryOperator, vectors, bw3, arff_idx);

		bw3.flush();

		bw3.close();
		return arff_idx;
	}

	public static HashMap<String, String> getIdx(String idx) throws IOException {
		HashMap<String, String> map = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new FileReader(new File(idx)));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] element = line.split(" ");
			map.put(element[0], element[1]);
		}
		br.close();
		return map;
	}

	public static HashMap<String, ArrayList<Double>> readModel(String modelFile, HashMap<String, String> idx)
			throws NumberFormatException, IOException {

		BufferedReader br = new BufferedReader(new FileReader(new File(modelFile)));
		String line = null;
		HashMap<String, ArrayList<Double>> map = new HashMap<>();
		while ((line = br.readLine()) != null) {
			String[] elements = line.split("\t");

			if (elements.length > 2) {
				String name = idx.get(elements[0]);
				ArrayList<Double> list = new ArrayList<>();
				for (int i = 1; i < elements.length; i++) {
					list.add(Double.valueOf(elements[i]));
				}
				if (name != null) {
					map.put(name, list);
				} else {
					System.out.println("@@@ null error: " + modelFile + " : " + elements[0]);
					System.exit(0);
				}
			}

		}
		br.close();
		return map;
	}

	public static void writeTest_single(String test_pair, String binaryOperator,
			HashMap<String, ArrayList<Double>> vectors, BufferedWriter bw, HashMap<String, String> arff_idx)
			throws IOException {

		String[] elements = test_pair.split(" ");
		String drug = elements[0];
		String target = elements[1];
		ArrayList<Double> vec_1 = vectors.get(drug);
		ArrayList<Double> vec_2 = vectors.get(target);
		if (vec_1 != null && vec_2 != null) {
			ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
			StringBuffer sb = new StringBuffer();
			StringBuffer sb_1 = new StringBuffer();
			for (int i = 0; i < vec.size(); i++) {
				sb.append(vec.get(i)).append(",");
				sb_1.append(vec.get(i)).append(" ");
			}
			arff_idx.put(sb_1.toString().trim(), test_pair);
			sb.append("true");
			bw.write(sb.toString().trim() + "\n");
		} else {
			System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
			System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
			System.exit(0);
		}

		bw.flush();
	}
	
	public static void writeTest_batch(HashSet<String> test_pairs, String binaryOperator,
			HashMap<String, ArrayList<Double>> vectors, BufferedWriter bw, HashMap<String, String> arff_idx)
			throws IOException {

		for(String test_pair:test_pairs){
			String[] elements = test_pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				StringBuffer sb_1 = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
					sb_1.append(vec.get(i)).append(" ");
				}
				arff_idx.put(sb_1.toString().trim(), test_pair);
				sb.append("true");
				bw.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}

			bw.flush();	
		}
		
		
		
		
	}

	public HashMap<String, String> readLabel() throws IOException {
		HashMap<String, String> map = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(new File(labelfile)));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] elements = line.split("\t");
			map.put(elements[1], elements[0]);
		}
		return map;
	}
}
