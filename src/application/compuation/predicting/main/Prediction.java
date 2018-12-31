package application.compuation.predicting.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import application.computation.embedding.node2vec.EmbeddingPostProcessing;
import application.computation.predicting.classficiation.Classification;
import application.computation.predicting.classficiation.WekaDataGenerator_negativeSelection_case12;
import application.computation.predicting.inferencing.FastTargetInferencing;
import application.computation.predicting.inferencing.InferencePrediction;
import application.computation.predicting.negativeSelection.PU_ns;
import application.computation.util.BSIBean;
import application.computation.util.BinaryOperator;

public class Prediction {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		Prediction prediction=new Prediction();
		prediction.trainModel();
		prediction.predict("<http://bio2rdf.org/drugbank:DB00377>", "<http://bio2rdf.org/drugbank:BE0000557>");
	}
	
	
	String classifier=Classification.svm;
	Integer trainSpace_type= 4;
	String stardard_type = "fs";
	String pu_type = PU_ns.nb;
	String method=BinaryOperator.Hadamard;
	Double spyRate = 0.1;
	Double space=1.0;
	Integer dimension=64;
	Integer valid_interaction=-1;
	String thread_method="z_5";
	Double BSI_rate = 0.5;
	Double BSI_PU_rate = 0.5;
	
	String modelfile="model/entire_training_64_40_5_10_2.0_1.0_tab.emb";
	String idxfile="data/network.idx";
	String datafile="data/network_bi.nt";
	
	String arff_trainfile="model/arff_train.arff";
	String arff_testfile="model/arff_test.arff";
	String classification_model_file="model/classification.model";
	
	
	public Prediction (String datafile, String modelfile, String idxfile) {
		this.modelfile=modelfile;
		this.idxfile=idxfile;
		this.datafile=datafile;
	}
	
	public Prediction () {
		
	}
	public void trainModel() throws Exception {
		new File(new java.io.File( "." ).getCanonicalPath() + "/tmp/").mkdirs();
		WekaDataGenerator_negativeSelection_case12 g_1 = new WekaDataGenerator_negativeSelection_case12(
				trainSpace_type,
				new java.io.File( "." ).getCanonicalPath() + "/tmp/tain_tmp.arff",
				new java.io.File( "." ).getCanonicalPath()  + "/tmp/test_tmp.arff",
				datafile, dimension, method, modelfile, idxfile, pu_type, space, spyRate,
				thread_method, valid_interaction);

		WekaDataGenerator_negativeSelection_case12.writeARFF_train(dimension, method, datafile, idxfile,
				modelfile, arff_trainfile, g_1.getTrain_negatives()) ;
		
		
		Classification.trainModel(arff_trainfile, classifier,classification_model_file);
	
	}
	public HashMap<String, Double>  predict(String drug, String target) throws Exception {

			HashMap<String,String> arff_idx=writeARFF_test_single(
				dimension, method, drug+" "+target, idxfile,
				modelfile, arff_testfile);
		
			BSIBean bean = new InferencePrediction().network_inferences(modelfile, idxfile,
					datafile, drug,target, stardard_type);

			HashMap<String, Double> dbsi = bean.getDbsi();

			HashMap<String, Double> tbsi = bean.getTbsi();

			HashMap<String, Double> results_tmp = merge(dbsi, tbsi, BSI_rate);
			
			
			HashMap<String, Double> results_classification=Classification.predictWithModel(classification_model_file, arff_idx, arff_testfile);
			
			HashMap<String, Double> results=merge(results_tmp, results_classification, BSI_PU_rate);

			for(Entry<String,Double> entry:results.entrySet()) {
				System.out.println(entry.getKey()+" -> "+entry.getValue());
			}
			
			return results ;
		
	}
	
	
	public static HashMap<String, Double> merge(
			HashMap<String, Double> re_1, HashMap<String, Double> re_2, Double rate) {
		
		HashMap<String, Double> map = new HashMap<>();
		
		if(re_1.size()==re_2.size()) {

			for (Entry<String, Double> entry_2 : re_1.entrySet()) {
				map.put(entry_2.getKey(), entry_2.getValue() * rate);
			}
			
			for (Entry<String, Double> entry_2 : re_2.entrySet()) {
				if(map.containsKey(entry_2.getKey())) {
					map.put(entry_2.getKey(), map.get(entry_2.getKey())+entry_2.getValue() * (1-rate));
				}else {
					map.put(entry_2.getKey(), entry_2.getValue() * (1-rate));
				}
			}
			
		}else {
			System.out.println("re_1 size not equals to re_2. ");
			System.exit(0);
		}
			

		
		return map;
	}

	
	
	public static HashMap<String,String> writeARFF_test_single(
			int vectorsize, String binaryOperator, String pair, String idxFile,
			String modelFile, String arffTest) throws Exception {
		HashMap<String, String> idx = getIdx(idxFile);
		HashMap<String, ArrayList<Double>> vectors = readModel(modelFile, idx);
		
		HashMap<String,String> arff_idx=new HashMap<>();
		 
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
			HashMap<String, ArrayList<Double>> vectors, BufferedWriter bw, HashMap<String,String> arff_idx) throws IOException {

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
