package application.computation.predicting.classficiation;
  
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.deeplearning4j.graph.models.GraphVectors;
import org.deeplearning4j.graph.models.loader.GraphVectorSerializer;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import application.computation.embedding.deepwalk.DeepWalkMethod;
import application.computation.embedding.node2vec.EmbeddingPostProcessing;
import application.computation.embedding.node2vec.Node2vecMethod;
import application.computation.predicting.negativeSelection.ENN_ns;
import application.computation.predicting.negativeSelection.ENN_ns_runnable;
import application.computation.predicting.negativeSelection.MeanRange;
import application.computation.predicting.negativeSelection.NaiveBayes_local;
import application.computation.predicting.negativeSelection.NaiveBayes_ns;
import application.computation.predicting.negativeSelection.PU_ns;
import application.computation.util.BinaryOperator;
import weka.classifiers.functions.LibSVM;
import weka.core.Instance;

import java.util.Random;
import java.util.TreeMap;

public class WekaDataGenerator_negativeSelection_case12 {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
	}

	HashMap<String, String> idx;
	HashSet<String> candidates;
	HashSet<String> allTargets;
	HashSet<String> allDrugs;
	HashSet<String> train_negatives;
	public HashSet<String> getTrain_negatives() {
		return train_negatives;
	}

	public void setTrain_negatives(HashSet<String> train_negatives) {
		this.train_negatives = train_negatives;
	}

	public HashSet<String> getTest_negatives() {
		return test_negatives;
	}

	public void setTest_negatives(HashSet<String> test_negatives) {
		this.test_negatives = test_negatives;
	}

	HashSet<String> test_negatives;

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

				if (s.startsWith("<http://bio2rdf.org/drugbank:BE") ) {
					set.add(s);
				}
				if (o.startsWith("<http://bio2rdf.org/drugbank:BE") ) {
					set.add(o);
				}
			}
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

				if (s.startsWith("<http://bio2rdf.org/drugbank:DB") ) {
					set.add(s);
				}
				if (o.startsWith("<http://bio2rdf.org/drugbank:DB") ) {
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

	public static ArrayList<String> readFile(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line = null;
		ArrayList<String> list = new ArrayList<>();
		boolean dataline = false;
		while ((line = br.readLine()) != null) {
			if (!dataline) {
				if (line.startsWith("@data")) {
					dataline = true;
				}
			} else {
				String[] elements = line.split(",");
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < elements.length - 1; i++) {
					sb.append(elements[i] + ",");
				}
				list.add(sb.toString().trim());
			}
		}
		return list;
	}

	public static boolean checkFiles(String file1, String file2) throws IOException {
		boolean issame = true;
		ArrayList<String> list1 = readFile(file1);
		ArrayList<String> list2 = readFile(file2);
		for (int i = 0; i < list2.size(); i++) {
			if (!list2.get(i).equals(list1.get(i))) {
				issame = false;
				break;
			}
		}

		return issame;
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



	
	
	
	
	/**
	 * kkk
	 * @param testSpace_type
	 * @param trainSpace_type
	 * @param tmpStoreFile1
	 * @param tmpStoreFile2
	 * @param trainFile
	 * @param testFile
	 * @param vectorsize
	 * @param binaryOperator
	 * @param modelFile
	 * @param idxFile
	 * @param pu_type
	 * @param spaces
	 * @param spyRate
	 * @param thread_method
	 * @param valid_interactoin
	 * @throws Exception
	 */
	public WekaDataGenerator_negativeSelection_case12(int trainSpace_type, String tmpStoreFile1, String tmpStoreFile2, String trainFile, 
			int vectorsize, String binaryOperator, String modelFile, String idxFile,
			String pu_type, Double spaces, Double spyRate, String thread_method,
			int valid_interactoin) throws Exception {
		
		long seed = 1024;

		allTargets = getAllTarget(trainFile);
		allDrugs = getAllDrug(trainFile);
		
		train_negatives = getTrainNegative(trainSpace_type, allDrugs, allTargets, trainFile, seed, modelFile,
				idxFile, binaryOperator, vectorsize, tmpStoreFile1,tmpStoreFile2, spyRate, pu_type, spaces, valid_interactoin, thread_method);
	}
	
	
	/**
	 * kkk
	 * @param trainSpace_type
	 * @param allDrugs
	 * @param allTargets
	 * @param trainFile
	 * @param testFile
	 * @param testNegatives
	 * @param seed
	 * @param modelfile
	 * @param idxfile
	 * @param binaryOperator
	 * @param vectorSize
	 * @param tempStoreFile1
	 * @param tempStoreFile2
	 * @param spyRate
	 * @param pu_type
	 * @param spaces
	 * @param valid_interaction
	 * @param thread_method
	 * @return
	 * @throws Exception
	 */
	public HashSet<String> getTrainNegative(int trainSpace_type, HashSet<String> allDrugs, HashSet<String> allTargets, String trainFile,
			 long seed, String modelfile, String idxfile,
			String binaryOperator, int vectorSize, String tempStoreFile1, String tempStoreFile2,Double spyRate, String pu_type, Double spaces, 
			int valid_interaction, String thread_method) throws Exception {
		

		Random random = new Random(seed);
		
		HashSet<String> drugs_type2=readDrugs(trainFile); 
		HashSet<String> targets_type2=readTargets(trainFile); 
		
		
		HashSet<String> trainSet = readDrugTargetAssociation(trainFile);
		HashSet<String> positives = trainSet;

		HashSet<String> pairs = new HashSet<>();
		
		List<String> drugList = new ArrayList<String>();
		List<String> targetList = new ArrayList<String>();
		
			for (String drug : allDrugs) {
				drugList.add(drug);
			}
			for (String target : allTargets) {
				targetList.add(target);
			}	
		

		Collections.shuffle(drugList, random);
		Collections.shuffle(targetList, random);

		HashMap<String, Double> sim_map = new HashMap<>();
		HashSet<String> potentialNegatives = new HashSet<>();

		HashMap<String, HashSet<String>> drugTargets = getDrugTargetAssociations(trainFile);
		HashMap<String, HashSet<String>> targetDrugs = getTargetDrugAssociations(trainFile);

		BufferedReader br = new BufferedReader(new FileReader(new File(idxfile)));
		String line = null;
		HashMap<String, Integer> deepwak_idx = new HashMap<>();
		HashMap<Integer, String> deepwak_iidx = new HashMap<>();
		while ((line = br.readLine()) != null) {
			String[] elements = line.split(" ");
			deepwak_idx.put(elements[1], Integer.valueOf(elements[0]));
			deepwak_iidx.put(Integer.valueOf(elements[0]), elements[1]);
		}

		Double min = Double.MAX_VALUE;
		Double max = Double.MIN_VALUE;

		
		int drugSize=drugList.size();
		int targetSize=targetList.size();
		
		System.out.println("============step 4===============");
		
		System.out.println("spaces:"+spaces);
		System.out.println("trainSet:"+trainSet.size());
		
			while(pairs.size() < (trainSet.size() *spaces)){
				int drug_idx=random.nextInt(drugSize);
				int target_idx=random.nextInt(targetSize);
				
				String drug=drugList.get(drug_idx);
				String target=targetList.get(target_idx);
				
				if (!trainSet.contains(drug + " " + target)) {
							int num = 0;
							if (targetDrugs.containsKey(target)) {
								HashSet<String> drugs = targetDrugs.get(target);
								for (String d : drugs) {
									num++;
								}
							}

							if (drugTargets.containsKey(drug)) {
								HashSet<String> targets = drugTargets.get(drug);
								for (String t : targets) {
									num++;
								}
							}
							if(num>valid_interaction){
								
								if(trainSpace_type==1){
									
								}
								
								if(trainSpace_type==2){
									
								}
								if(trainSpace_type==3){
									if(allDrugs.contains(drug)&&allTargets.contains(target)){
										if(!(drugs_type2.contains(drug)&&targets_type2.contains(target))){
											pairs.add(drug + " " + target);	
										}
									}
								}
								if(trainSpace_type==4){
									if(drugs_type2.contains(drug)&&targets_type2.contains(target)){
										pairs.add(drug + " " + target);	
									}
								}	
								if(trainSpace_type==5){
								
								}
								if(trainSpace_type==6){
									
								}
								if(trainSpace_type==7){
									pairs.add(drug + " " + target);	
								}
								
								
								if(trainSpace_type==8){
									
								}
								
								
								if(trainSpace_type==9){
									
								}
								
								
								if(trainSpace_type==10){
									
								}
									
						}
					}
			}
		
		
		System.out.println("candidate negative: " + pairs.size());
		potentialNegatives = pairs;
		idx = getIdx(idxfile);

		HashMap<String, ArrayList<Double>> vectors = readModel(modelfile, idx);

		List<String> positivesList = new ArrayList<String>();
		for (String p : positives) {
			positivesList.add(p);
		}

		Collections.shuffle(positivesList, random);

		HashSet<String> spy_positives = new HashSet<>();
		HashSet<String> remain_positives = new HashSet<>();

		for (int i = 0; i < (int) (spyRate * positivesList.size()); i++) {
			spy_positives.add(positivesList.get(i));
		}

		for (String positive : positives) {
			if (!spy_positives.contains(positive)) {
				remain_positives.add(positive);
			}
		}
		potentialNegatives.retainAll(pairs);

		System.out.println("potentialNegatives (before spying): " + potentialNegatives.size());
		
		SpyBean spybean = writetrain(remain_positives, spy_positives, potentialNegatives, binaryOperator, vectors,
				vectorSize, tempStoreFile1,tempStoreFile2, false);
		
		Instances train = DataSource.read(tempStoreFile1);
		train.setClassIndex(train.numAttributes() - 1);
		
		
		HashMap<String, String> n_map = new HashMap<>();

		// load unlabeled data, set class
		Instances test = DataSource.read(tempStoreFile2);
		test.setClassIndex(test.numAttributes() - 1);
		
		HashMap<String,Double> pu_score_map;
		
			pu_score_map=new HashMap<>();
			PU_ns pu = new PU_ns(train);
			pu.build();
			
			for (int i = 0; i < test.size(); i++) {
				Instance instance = test.get(i);
				StringBuffer sb = new StringBuffer();
				for (int j = 0; j < test.numAttributes() - 1; j++) {
					sb.append(instance.value(j)).append(" ");
				} //娌℃湁甯lass鍊�
				double[] values = pu.getClassifer().distributionForInstance(instance);
				Double score=values[test.classAttribute().indexOfValue("true")];
				pu_score_map.put(sb.toString().trim(), score);
			}
		
		Double mean = 0.0;
		Double median=0.0;
		ArrayList<Double> values_sd = new ArrayList<>();
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;		

		System.out.println("pu_score_map --> "+pu_score_map.size());
		
		for (int i = 0; i < test.size(); i++) {
			Instance instance = test.get(i);
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < test.numAttributes() - 1; j++) {
				sb.append(instance.value(j)).append(" ");
			}
			
			Double	 score=pu_score_map.get(sb.toString().trim());
			
			if (spybean.getNegative().containsKey(sb.toString().trim())) {
				n_map.put(sb.toString().trim(), spybean.getNegative().get(sb.toString().trim()));
			}
			if (spybean.getSpy_positive().containsKey(sb.toString().trim())) {
//				sp_map.put(sb.toString().trim(), spybean.getSpy_positive().get(sb.toString().trim()));
				// double[] values = nb.distributionForInstance(instance);
				if (score< min) {
					min = score;
				}
				if (score > max) {
					max = score;
				}
				mean += score;
				values_sd.add(score);
			}
			if (spybean.getRemian_positive().containsKey(sb.toString().trim())) {
//				rp_map.put(sb.toString().trim(), spybean.getRemian_positive().get(sb.toString().trim()));
			}
		}
		
		
		// 鑷畾涔塁omparator瀵硅薄锛岃嚜瀹氫箟鎺掑簭
		Comparator c = new Comparator<Double>() {
			@Override
			public int compare(Double o1, Double o2) {
				// TODO Auto-generated method stub
				if (o2 < o1)
					return 1;
				// 娉ㄦ剰锛侊紒杩斿洖鍊煎繀椤绘槸涓�瀵圭浉鍙嶆暟锛屽惁鍒欐棤鏁堛�俲dk1.7浠ュ悗灏辨槸杩欐牱銆�
				// else return 0; //鏃犳晥
				else
					return -1;
			}
		};
		Collections.sort(values_sd);
		
		median=values_sd.get(values_sd.size()/2);
		// System.out.println("n_map: " + n_map.size());
		// System.out.println("sp_map: " + sp_map.size());
		// System.out.println("rp_map: " + rp_map.size());
		Double less_15=values_sd.get((int)(values_sd.size()*0.15));
		Double less_5=values_sd.get((int)(values_sd.size()*0.05));
		
		Double sd = 0.0;
		mean = mean / values_sd.size();
		for (int i = 0; i < values_sd.size(); i++) {
			sd += Math.pow(values_sd.get(i) - mean, 2);
		}
		sd = Math.sqrt(sd) / (values_sd.size() - 1);

		Double threashhold = 0.0;
		
		if(thread_method.equals("mean")){
			threashhold=mean;
		}else
		
		if(thread_method.equals("median")){
			threashhold=median;
		}else

		if(thread_method.equals("min")){
			threashhold=min;
		}else
		
		if(thread_method.equals("less_15")){
			threashhold=less_15;
		}else
				
		if(thread_method.equals("less_5")){
			threashhold=less_5;
		}else
		
		if(thread_method.equals("z_5")){
			threashhold=mean - 1.96 * sd;
		}else
		
		if(thread_method.equals("z_1")){
			threashhold=mean - 2.58 * sd;
		}else
		{
			System.out.println("+++++++++++++++++++++++++++++++++");
			System.out.println("error: wrong thread method");
			System.exit(0);	
		}

		HashSet<String> negatives = new HashSet<>();

		HashMap<String, Double> negatives_map = new HashMap<>();

		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;

		for (int i = 0; i < test.size(); i++) {
			Instance instance = test.get(i);
			
			StringBuffer sb = new StringBuffer();
			for (int j = 0; j < test.numAttributes() - 1; j++) {
				sb.append(instance.value(j)).append(" ");
			}
			
			if (n_map.containsKey(sb.toString().trim())) {
				// negatives_map.put(n_map.get(instance),
				// nb.distributionForInstance(instance)[test.classAttribute().indexOfValue("true")]);
				Double score = pu_score_map.get(sb.toString().trim());
				if (score <= threashhold) {
					negatives_map.put(n_map.get(sb.toString().trim()), score);
					if (score < min) {
						min = score;
					}
					if (score > max) {
						max = score;
					}

				}
			}
		}

		HashMap<String, Double> PU_score = new HashMap<>();

		for (Entry<String, Double> entry : negatives_map.entrySet()) {
			 PU_score.put(entry.getKey(), (entry.getValue()-min)/(max-min));
		}


		// map杞崲鎴恖ist杩涜鎺掑簭
		List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(PU_score.entrySet());

		// 鎺掑簭
		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
			@Override
			public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
				// TODO Auto-generated method stub

				return o1.getValue().compareTo(o2.getValue()); // bottom to up
			}
		});

		// System.out.println("threadhold: " + threashhold+" min: "+min + " max:
		// "+max+" mean: "+mean+" sd: "+sd+"
		// l%"+values_sd.get((int)(values_sd.size()*0.15)));

		
		for (int i = 0; (i < list.size()) && (i < trainSet.size()); i++) {
			negatives.add(list.get(i).getKey());
		}
		
		System.out.println("ranked list --> " + list.size());
		System.out.println("train negative size --> " + negatives.size());
		System.out.println("negative spying finished ...");
		return negatives;
	}
	
	
	
	

	public HashMap<String,String> writeARFF(int vectorsize, String binaryOperator, String trainFile, String testFile, String idxFile,
			String modelFile, String arffTrain, String arffUnlable, String arffTest) throws Exception {
		idx = getIdx(idxFile);
		HashMap<String, ArrayList<Double>> vectors = readModel(modelFile, idx);

		
		 HashMap<String,String> arff_idx=new HashMap<>();
		 
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(arffTrain)));
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(arffUnlable)));
		BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File(arffTest)));
		bw1.write("@relation 'associations'\n");
		bw2.write("@relation 'associations'\n");
		bw3.write("@relation 'associations'\n");

		if (binaryOperator.equals(BinaryOperator.Add)) {
			for (int i = 0; i < vectorsize * 2; i++) {
				bw1.write("@attribute 'attribute_" + i + "' numeric\n");
				bw2.write("@attribute 'attribute_" + i + "' numeric\n");
				bw3.write("@attribute 'attribute_" + i + "' numeric\n");
			}

		} else {

			for (int i = 0; i < vectorsize; i++) {
				bw1.write("@attribute 'attribute_" + i + "' numeric\n");
				bw2.write("@attribute 'attribute_" + i + "' numeric\n");
				bw3.write("@attribute 'attribute_" + i + "' numeric\n");
			}

		}

		bw1.write("@attribute 'Class' { 'true', 'false'}\n");
		bw1.write("@data\n");
		bw2.write("@attribute 'Class' { 'true', 'false'}\n");
		bw2.write("@data\n");
		bw3.write("@attribute 'Class' { 'true', 'false'}\n");
		bw3.write("@data\n");

		writeTrain(trainFile, binaryOperator, vectors, train_negatives, bw1);
		writeUnlabel(testFile, test_negatives, binaryOperator, vectors, bw2);
		writeTest(testFile, test_negatives, binaryOperator, vectors, bw3, arff_idx);

		bw1.flush();
		bw2.flush();
		bw3.flush();

		bw1.close();
		bw2.close();
		bw3.close();
		return arff_idx;
	}
	
	
	public static void writeARFF_train(int vectorsize, String binaryOperator, String trainFile, String idxFile,
			String modelFile, String arffTrain, HashSet<String> train_negatives) throws Exception {
		HashMap<String, String> idx = getIdx(idxFile);
		HashMap<String, ArrayList<Double>> vectors = readModel(modelFile, idx);

		BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(arffTrain)));
		bw1.write("@relation 'associations'\n");

		if (binaryOperator.equals(BinaryOperator.Add)) {
			for (int i = 0; i < vectorsize * 2; i++) {
				bw1.write("@attribute 'attribute_" + i + "' numeric\n");
			}

		} else {

			for (int i = 0; i < vectorsize; i++) {
				bw1.write("@attribute 'attribute_" + i + "' numeric\n");
			}

		}

		bw1.write("@attribute 'Class' { 'true', 'false'}\n");
		bw1.write("@data\n");

		writeTrain(trainFile, binaryOperator, vectors, train_negatives, bw1);

		bw1.flush();

		bw1.close();
	}

	
	public static HashMap<String,String> writeARFF_test(
			HashSet<String> test_negatives,
			int vectorsize, String binaryOperator, String testFile, String idxFile,
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

		writeTest(testFile, test_negatives, binaryOperator, vectors, bw3, arff_idx);

		bw3.flush();

		bw3.close();
		return arff_idx;
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
	
	
	public static HashMap<String,String> writeARFF(HashSet<String> train_negatives,
			HashSet<String> test_negatives,
			int vectorsize, String binaryOperator, String trainFile, String testFile, String idxFile,
			String modelFile, String arffTrain, String arffUnlable, String arffTest) throws Exception {
		HashMap<String, String> idx = getIdx(idxFile);
		HashMap<String, ArrayList<Double>> vectors = readModel(modelFile, idx);

		
		 HashMap<String,String> arff_idx=new HashMap<>();
		 
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(arffTrain)));
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(arffUnlable)));
		BufferedWriter bw3 = new BufferedWriter(new FileWriter(new File(arffTest)));
		bw1.write("@relation 'associations'\n");
		bw2.write("@relation 'associations'\n");
		bw3.write("@relation 'associations'\n");

		if (binaryOperator.equals(BinaryOperator.Add)) {
			for (int i = 0; i < vectorsize * 2; i++) {
				bw1.write("@attribute 'attribute_" + i + "' numeric\n");
				bw2.write("@attribute 'attribute_" + i + "' numeric\n");
				bw3.write("@attribute 'attribute_" + i + "' numeric\n");
			}

		} else {

			for (int i = 0; i < vectorsize; i++) {
				bw1.write("@attribute 'attribute_" + i + "' numeric\n");
				bw2.write("@attribute 'attribute_" + i + "' numeric\n");
				bw3.write("@attribute 'attribute_" + i + "' numeric\n");
			}

		}

		bw1.write("@attribute 'Class' { 'true', 'false'}\n");
		bw1.write("@data\n");
		bw2.write("@attribute 'Class' { 'true', 'false'}\n");
		bw2.write("@data\n");
		bw3.write("@attribute 'Class' { 'true', 'false'}\n");
		bw3.write("@data\n");

		writeTrain(trainFile, binaryOperator, vectors, train_negatives, bw1);
		writeUnlabel(testFile, test_negatives, binaryOperator, vectors, bw2);
		writeTest(testFile, test_negatives, binaryOperator, vectors, bw3, arff_idx);

		bw1.flush();
		bw2.flush();
		bw3.flush();

		bw1.close();
		bw2.close();
		bw3.close();
		return arff_idx;
	}
	
	

	public static void writeTrain(String trainFile, String binaryOperator, HashMap<String, ArrayList<Double>> vectors,
			HashSet<String> trainNegative, BufferedWriter bw) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(trainFile)));
		String line = null;
		while ((line = br.readLine()) != null) {
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);
			while (nxp.hasNext()) {
				Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();

				if (p.equals("<http://bio2rdf.org/MultiPartiteNetwork_vocabulary:Drug-Target>")) {
					ArrayList<Double> vec_1 = vectors.get(s);
					ArrayList<Double> vec_2 = vectors.get(o);
					if (vec_1 != null && vec_2 != null) {
						ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
						StringBuffer sb = new StringBuffer();
						for (int i = 0; i < vec.size(); i++) {
							sb.append(vec.get(i)).append(",");
						}
						sb.append("true");
						bw.write(sb.toString().trim() + "\n");
					} else {
						System.out.println("@@@ null error find at vectors: " + vec_1 + " " + s);
						System.out.println("@@@ null error find at vectors: " + vec_2 + " " + o);
						System.exit(0);
					}

				}
			}
		}

		for (String negative : trainNegative) {
			String[] element = negative.split(" ");
			ArrayList<Double> vec_1 = vectors.get(element[0]);
			ArrayList<Double> vec_2 = vectors.get(element[1]);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
				}
				sb.append("false");
				bw.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + element[0]);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + element[1]);
				System.exit(0);
			}

		}

		bw.flush();
	}

	

	public static HashSet<String> readDrugTargetAssociation(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line = null;
		HashSet<String> set = new HashSet<>();
		HashSet<String> drugs = new HashSet<>();
		HashSet<String> targets = new HashSet<>();
		while ((line = br.readLine()) != null) {
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);
			while (nxp.hasNext()) {
				Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();
				if (p.equals("<http://bio2rdf.org/MultiPartiteNetwork_vocabulary:Drug-Target>")) {
					set.add(s + " " + o);
					drugs.add(s);
					targets.add(o);
				}
			}
		}
		// System.out.println("drugs: "+drugs.size());
		// System.out.println("targets: "+targets.size());
		// System.out.println("links: "+set.size());
		return set;
	}

	

	

	
	
	
public HashMap<String,Double> normalize(HashMap<String,Double> map, String type){
		
	
		HashMap<String,Double> result=new HashMap<>();
	
		if(type.equals("sd")){
			int number=0;
			Double mean=0.0;
			for(Entry<String,Double> entry_1:map.entrySet()){
					number++;
					mean+=entry_1.getValue();
			}
			mean=mean/number;
			Double sd=0.0;
			for(Entry<String,Double> entry_1:map.entrySet()){
					sd+=Math.pow(entry_1.getValue()-mean, 2);
			}
			sd=Math.sqrt(sd/(number-1));
		        
			for(Entry<String,Double> entry_1:map.entrySet()){
					Double z=(entry_1.getValue()-mean)/sd;
					result.put(entry_1.getKey(), z);
			}	
		}
		if(type.equals("fs")){
			Double min=Double.MAX_VALUE;
			Double max=Double.MIN_VALUE;		
			Double maxSection=1.0;
			Double minSection=-1.0;
			for(Entry<String,Double> entry_1:map.entrySet()){
						if(entry_1.getValue()>max){
							max=entry_1.getValue();
						}
						if(entry_1.getValue()<min){
							min=entry_1.getValue();
						}	
			}
		        
			for(Entry<String,Double> entry_1:map.entrySet()){
					Double tmp=(entry_1.getValue()-min)/(max-min);
					Double value=minSection+tmp*(maxSection - minSection);
				result.put(entry_1.getKey(), value);
			}
		}
		
		
		
		return result;
	}
	
	

	public HashSet<String> readDrugs(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line = null;
		HashSet<String> drugs = new HashSet<>();
		while ((line = br.readLine()) != null) {
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);
			while (nxp.hasNext()) {
				Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();
				if (p.equals("<http://bio2rdf.org/MultiPartiteNetwork_vocabulary:Drug-Target>")) {
					drugs.add(s);
				}
			}
		}
		// System.out.println("drugs: "+drugs.size());
		// System.out.println("targets: "+targets.size());
		// System.out.println("links: "+set.size());
		return drugs;
	}
	
	
	public HashSet<String> readTargets(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line = null;
		HashSet<String> targets = new HashSet<>();
		while ((line = br.readLine()) != null) {
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);
			while (nxp.hasNext()) {
				Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();
				if (p.equals("<http://bio2rdf.org/MultiPartiteNetwork_vocabulary:Drug-Target>")) {
					targets.add(o);
				}
			}
		}
		// System.out.println("drugs: "+drugs.size());
		// System.out.println("targets: "+targets.size());
		// System.out.println("links: "+set.size());
		return targets;
	}
	

	
	

	public void underSampling(String balacningMethod) {

		// DistributionBasedBalance balance=new DistributionBasedBalance();
		// balance.setInputFormat(instanceInfo);
		// Instances newData = Filter.useFilter(data, remove);
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

	public HashMap<String, HashMap<String, Double>> refineDrugTarget(HashMap<String, HashMap<String, Double>> map,
			HashSet<String> set) {

		HashMap<String, HashMap<String, Double>> result = new HashMap<>();
		for (Entry<String, HashMap<String, Double>> entry1 : map.entrySet()) {
			for (Entry<String, Double> entry2 : entry1.getValue().entrySet()) {
				if (set.contains(entry1.getKey() + " " + entry2.getKey())) {
					if (result.containsKey(entry1.getKey())) {
						result.get(entry1.getKey()).put(entry2.getKey(), entry2.getValue());
					} else {
						HashMap<String, Double> tmp = new HashMap<>();
						tmp.put(entry2.getKey(), entry2.getValue());
						result.put(entry1.getKey(), tmp);
					}
				}
			}
		}
		return result;
	}

	public HashMap<String, HashMap<String, Double>> refineTargetDrug(HashMap<String, HashMap<String, Double>> map,
			HashSet<String> set) {

		HashMap<String, HashMap<String, Double>> result = new HashMap<>();
		for (Entry<String, HashMap<String, Double>> entry1 : map.entrySet()) {
			for (Entry<String, Double> entry2 : entry1.getValue().entrySet()) {
				if (set.contains(entry2.getKey() + " " + entry1.getKey())) {
					if (result.containsKey(entry1.getKey())) {
						result.get(entry1.getKey()).put(entry2.getKey(), entry2.getValue());
					} else {
						HashMap<String, Double> tmp = new HashMap<>();
						tmp.put(entry2.getKey(), entry2.getValue());
						result.put(entry1.getKey(), tmp);
					}
				}
			}
		}
		return result;
	}

	public HashMap<String, HashMap<String, Double>> getAssociateTargets_method1(
			HashMap<String, HashMap<String, Double>> input, HashMap<String, HashSet<String>> associations,
			HashSet<String> allTarget) throws IOException {
		HashMap<String, HashMap<String, Double>> results = new HashMap<>();

		for (Entry<String, HashMap<String, Double>> entry1 : input.entrySet()) {
			HashMap<String, Double> tmp = new HashMap<>();
			for (Entry<String, Double> entry : entry1.getValue().entrySet()) {
				// System.err.println("======================================");
				// System.err.println(entry.getKey());
				if (associations.containsKey(entry.getKey())) {
					for (String target : associations.get(entry.getKey())) {
						if (allTarget.contains(target)) {
							// System.err.println( " =========> "
							// +entry1.getKey()+" "+entry.getKey());
							if (!associations.containsKey(entry1.getKey())) {
								if (tmp.containsKey(target)) {
									tmp.put(target, tmp.get(target) + entry.getValue());
									// System.err.println(drug+" associated drug
									// score: "+entry.getValue().get(string));
								} else {
									tmp.put(target, entry.getValue());
								}
							} else {
								if (!associations.get(entry1.getKey()).contains(target)) {
									if (tmp.containsKey(target)) {
										tmp.put(target, tmp.get(target) + entry.getValue());
										// System.err.println(drug+" associated
										// drug score:
										// "+entry.getValue().get(string));
									} else {
										tmp.put(target, entry.getValue());
									}
								}
							}

						}
					}
				}
			}

			results.put(entry1.getKey(), tmp);
		}

		return results;

	}

	public HashMap<String, HashMap<String, Double>> normalize_sd(HashMap<String, HashMap<String, Double>> map) {

		HashMap<String, HashMap<String, Double>> result = new HashMap<>();
		int number = 0;
		Double mean = 0.0;
		for (Entry<String, HashMap<String, Double>> entry_1 : map.entrySet()) {
			for (Entry<String, Double> entry_2 : entry_1.getValue().entrySet()) {
				number++;
				mean += entry_2.getValue();
			}
		}
		mean = mean / number;
		Double sd = 0.0;
		for (Entry<String, HashMap<String, Double>> entry_1 : map.entrySet()) {
			for (Entry<String, Double> entry_2 : entry_1.getValue().entrySet()) {
				sd += Math.pow(entry_2.getValue() - mean, 2);
			}
		}
		sd = Math.sqrt(sd / (number - 1));

		for (Entry<String, HashMap<String, Double>> entry_1 : map.entrySet()) {
			HashMap<String, Double> tmp_map = new HashMap<>();
			for (Entry<String, Double> entry_2 : entry_1.getValue().entrySet()) {
				Double z = (entry_2.getValue() - mean) / sd;
				tmp_map.put(entry_2.getKey(), z);
			}
			result.put(entry_1.getKey(), tmp_map);
		}

		return result;
	}

	public HashSet<String> getAssoication(String file) throws IOException {
		HashSet<String> set = new HashSet<>();
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line = null;
		while ((line = br.readLine()) != null) {
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);
			while (nxp.hasNext()) {
				Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();
				if (p.equals("<http://bio2rdf.org/MultiPartiteNetwork_vocabulary:Drug-Target>")) {
					set.add(s + " " + o);
				}
			}
		}
		return set;
	}

	

	public void writeTest(String testFile, HashSet<String> negatives, String binaryOperator,
			HashMap<String, ArrayList<Double>> vectors, BufferedWriter bw) throws IOException {
		HashSet<String> positives = readDrugTargetAssociation(testFile);

		for (String pair : negatives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
				}
				sb.append("false");
				bw.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}

		}

		for (String pair : positives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
				}
				sb.append("true");
				bw.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}

		}

		bw.flush();
	}
	
	
	
	public static void writeTest(String testFile, HashSet<String> negatives, String binaryOperator,
			HashMap<String, ArrayList<Double>> vectors, BufferedWriter bw, HashMap<String,String> arff_idx) throws IOException {
		HashSet<String> positives = readDrugTargetAssociation(testFile);

		for (String pair : negatives) {
			String[] elements = pair.split(" ");
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
				arff_idx.put(sb_1.toString().trim(), pair);
				sb.append("false");
				bw.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}

		}

		for (String pair : positives) {
			String[] elements = pair.split(" ");
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
				arff_idx.put(sb_1.toString().trim(), pair);
				sb.append("true");
				bw.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}

		}

		bw.flush();
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
	
	public SpyBean writetrain(HashSet<String> remain_positives, HashSet<String> spy_positives,
			HashSet<String> negatives, String binaryOperator, HashMap<String, ArrayList<Double>> vectors,
			int vectorsize, String trainfile, String testfile, boolean using_spy_for_train) throws IOException {
		SpyBean bean = new SpyBean();
		HashMap<String, String> rp_map = new HashMap<>();
		HashMap<String, String> sp_map = new HashMap<>();
		HashMap<String, String> n_map = new HashMap<>();

		BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(trainfile)));
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(testfile)));
		
		bw1.write("@relation 'negative selection training'\n");
		bw2.write("@relation 'negative selection testing'\n");
		
		if (binaryOperator.equals(BinaryOperator.Add)) {
			for (int i = 0; i < vectorsize * 2; i++) {
				bw1.write("@attribute 'attribute_" + i + "' numeric\n");
				bw2.write("@attribute 'attribute_" + i + "' numeric\n");
			}

		} else {
			for (int i = 0; i < vectorsize; i++) {
				bw1.write("@attribute 'attribute_" + i + "' numeric\n");
				bw2.write("@attribute 'attribute_" + i + "' numeric\n");
			}
		}

		bw1.write("@attribute 'Class' { 'true', 'false'}\n");
		bw1.write("@data\n");
		
		bw2.write("@attribute 'Class' { 'true', 'false'}\n");
		bw2.write("@data\n");
		

		for (String pair : negatives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				StringBuffer sb_map = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
					sb_map.append(vec.get(i)).append(" ");
				}
				n_map.put(sb_map.toString().trim(), pair);
				sb.append("false");
				bw1.write(sb.toString().trim() + "\n");
				bw2.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}
		}

		for (String pair : spy_positives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb1 = new StringBuffer();
				StringBuffer sb2 = new StringBuffer();
				StringBuffer sb_map = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb1.append(vec.get(i)).append(",");
					sb2.append(vec.get(i)).append(",");
					sb_map.append(vec.get(i)).append(" ");
				}
				sp_map.put(sb_map.toString().trim(), pair);
				if(using_spy_for_train){
					sb1.append("false");
					bw1.write(sb1.toString().trim()+"\n");
				}
				sb2.append("true");
				bw2.write(sb2.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}
		}

		for (String pair : remain_positives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				StringBuffer sb_map = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
					sb_map.append(vec.get(i)).append(" ");
				}
				rp_map.put(sb_map.toString().trim(), pair);
				sb.append("true");
				bw1.write(sb.toString() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}

		}

		bean.setNegative(n_map);
		bean.setRemian_positive(rp_map);
		bean.setSpy_positive(sp_map);
		bw1.flush();
		bw1.close();
		bw2.flush();
		bw2.close();
		return bean;
	}
	
	
	
	public SpyBean writetrain(HashSet<String> remain_positives, HashSet<String> spy_positives,
			HashSet<String> negatives, String binaryOperator, HashMap<String, ArrayList<Double>> vectors,
			int vectorsize, String trainfile, String testfile) throws IOException {
		SpyBean bean = new SpyBean();
		HashMap<String, String> rp_map = new HashMap<>();
		HashMap<String, String> sp_map = new HashMap<>();
		HashMap<String, String> n_map = new HashMap<>();
		HashMap<String, String> test_map = new HashMap<>();
		
		
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(trainfile)));
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(testfile)));
		
		bw1.write("@relation 'negative selection training'\n");
		bw2.write("@relation 'negative selection testing'\n");
		
		for (int i = 0; i < vectorsize; i++) {
				bw1.write("@attribute 'attribute_" + i + "' numeric\n");
				bw2.write("@attribute 'attribute_" + i + "' numeric\n");
		}

		bw1.write("@attribute 'Class' { 'true', 'false'}\n");
		bw1.write("@data\n");
		
		bw2.write("@attribute 'Class' { 'true', 'false'}\n");
		bw2.write("@data\n");
		

		for (String pair : negatives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				StringBuffer sb_map = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
					sb_map.append(vec.get(i)).append(" ");
				}
				test_map.put(sb_map.toString().trim(), pair);
				n_map.put(sb_map.toString().trim(), pair);
				sb.append("false");
				bw1.write(sb.toString().trim() + "\n");
				bw2.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}
		}

		for (String pair : spy_positives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb1 = new StringBuffer();
				StringBuffer sb2 = new StringBuffer();
				StringBuffer sb_map = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb1.append(vec.get(i)).append(",");
					sb2.append(vec.get(i)).append(",");
					sb_map.append(vec.get(i)).append(" ");
				}
				sp_map.put(sb_map.toString().trim(), pair);
				test_map.put(sb_map.toString().trim(), pair);
				sb1.append("false");
				bw1.write(sb1.toString().trim()+"\n");
				
				sb2.append("true");
				bw2.write(sb2.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}
		}

		for (String pair : remain_positives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				StringBuffer sb_map = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
					sb_map.append(vec.get(i)).append(" ");
				}
				rp_map.put(sb_map.toString().trim(), pair);
				sb.append("true");
				bw1.write(sb.toString() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}

		}

		bean.setNegative(n_map);
		bean.setRemian_positive(rp_map);
		bean.setSpy_positive(sp_map);
		bean.setTest_pairs(test_map);
		bw1.flush();
		bw1.close();
		bw2.flush();
		bw2.close();
		return bean;
	}
	
	public SpyBean writetrain_enn(HashSet<String> positives,
			HashSet<String> negatives, String binaryOperator, HashMap<String, ArrayList<Double>> vectors,
			int vectorsize, String trainfile) throws IOException {
		SpyBean bean = new SpyBean();

		HashMap<String, String> n_map = new HashMap<>();
		HashMap<String, String> test_map = new HashMap<>();
		
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(trainfile)));
		
		bw1.write("@relation 'negative selection training'\n");
		
		for (int i = 0; i < vectorsize; i++) {
				bw1.write("@attribute 'attribute_" + i + "' numeric\n");
		}

		bw1.write("@attribute 'Class' { 'true', 'false'}\n");
		bw1.write("@data\n");

		for (String pair : negatives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				StringBuffer sb_map = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
					sb_map.append(vec.get(i)).append(" ");
				}
				test_map.put(sb_map.toString().trim(), pair);
				n_map.put(sb_map.toString().trim(), pair);
				sb.append("false");
				bw1.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}
		}


		for (String pair : positives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				StringBuffer sb_map = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
					sb_map.append(vec.get(i)).append(" ");
				}
				test_map.put(sb_map.toString().trim(), pair);
				sb.append("true");
				bw1.write(sb.toString() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}

		}

		bean.setNegative(n_map);
		bean.setTest_pairs(test_map);
		bw1.flush();
		bw1.close();
		return bean;
	}
	

	public void writeTest_one(String testFile, HashSet<String> negatives, String binaryOperator,
			HashMap<String, ArrayList<Double>> vectors, BufferedWriter bw) throws IOException {
		HashSet<String> positives = readDrugTargetAssociation(testFile);

		for (String pair : negatives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
				}
				sb.append("?");
				bw.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}

		}

		for (String pair : positives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
				}
				sb.append("true");
				bw.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}
		}
		bw.flush();
	}

	public static void writeUnlabel(String testFile, HashSet<String> negatives, String binaryOperator,
			HashMap<String, ArrayList<Double>> vectors, BufferedWriter bw) throws IOException {
		HashSet<String> positives = readDrugTargetAssociation(testFile);

		for (String pair : negatives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
				}
				sb.append("?");
				bw.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}

		}

		for (String pair : positives) {
			String[] elements = pair.split(" ");
			String drug = elements[0];
			String target = elements[1];
			ArrayList<Double> vec_1 = vectors.get(drug);
			ArrayList<Double> vec_2 = vectors.get(target);
			if (vec_1 != null && vec_2 != null) {
				ArrayList<Double> vec = BinaryOperator.operate(vec_1, vec_2, binaryOperator);
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < vec.size(); i++) {
					sb.append(vec.get(i)).append(",");
				}
				sb.append("?");
				bw.write(sb.toString().trim() + "\n");
			} else {
				System.out.println("@@@ null error find at vectors: " + vec_1 + " " + drug);
				System.out.println("@@@ null error find at vectors: " + vec_2 + " " + target);
				System.exit(0);
			}

		}

		bw.flush();
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
