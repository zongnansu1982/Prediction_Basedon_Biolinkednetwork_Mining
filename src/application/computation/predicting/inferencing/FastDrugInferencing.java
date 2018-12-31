package application.computation.predicting.inferencing;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import application.computation.embedding.deepwalk.DeepWalkMethod;
import application.computation.embedding.node2vec.Node2vecMethod;


public class FastDrugInferencing {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

	}
	public static String deeplwalk="deepwalk";
	public static String node2vec="node2vec";
	
	HashMap<Integer,String> idxmap;
	String modelfile;
	String idxfile;
	public HashMap<String,HashSet<String>> goldmap;
	public HashMap<String, HashSet<String>> getGoldmap() {
		return goldmap;
	}

	public void setGoldmap(HashMap<String, HashSet<String>> goldmap) {
		this.goldmap = goldmap;
	}

	public FastDrugInferencing(String modelfile, String idxfile) throws NumberFormatException, IOException{
		this.modelfile=modelfile;
		this.idxfile=idxfile;
		if(idxfile!=null){
			idxmap=new HashMap<>();
			BufferedReader br  =new BufferedReader(new FileReader(new File(idxfile)));
			String line=null;
			while((line=br.readLine())!=null){
				String[] s=line.split(" ");
				idxmap.put(Integer.valueOf(s[0]), s[1]);
			}
			br.close();
		}
	}
	public FastDrugInferencing(){
	}
	
	public void feedGold(String removedfile) throws IOException{
		goldmap=new HashMap<>();
		HashSet<String> queries=new HashSet<>();
		BufferedReader br = new BufferedReader(new FileReader(new File(removedfile)));
		String line=null;
		while((line=br.readLine())!=null){
			if(!line.contains("\"")){
				InputStream inputStream = new ByteArrayInputStream(line.getBytes());
				NxParser nxp = new NxParser();
				nxp.parse(inputStream);
				while (nxp.hasNext()) {
					Node[] quard = nxp.next();
					String s = quard[0].toString().trim();
					String p = quard[1].toString().trim();
					String o = quard[2].toString().trim();
					if(goldmap.containsKey(o)){
						goldmap.get(o).add(s);
					}else{
						HashSet<String> set=new HashSet<>();
						set.add(s);
						goldmap.put(o, set);
					}
				}
			}
		}
	}
	
	
	public HashSet<String> getTargetAsQueries() throws IOException{
		HashSet<String> set=new HashSet<>();
		for(String query:goldmap.keySet()){
			set.add(query);
		}
		return set;
	}
	
	public HashMap<String,HashSet<String>> getDrugAsQueries(String datafile) throws IOException{
		HashMap<String,HashSet<String>> map=new HashMap<>();
		
		HashSet<String> queries=new HashSet<>();
		for(String query:goldmap.keySet()){
			queries.add(query);
		}
		
		BufferedReader br = new BufferedReader(new FileReader(new File(datafile)));
		String line=null;
		while((line=br.readLine())!=null){
			if(!line.contains("\"")){
				InputStream inputStream = new ByteArrayInputStream(line.getBytes());
				NxParser nxp = new NxParser();
				nxp.parse(inputStream);
				while (nxp.hasNext()) {
					Node[] quard = nxp.next();
					String s = quard[0].toString().trim();
					String p = quard[1].toString().trim();
					String o = quard[2].toString().trim();
					if(s.startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/")
							&p.equals("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/target>")
							&o.startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/targets/")){
						
						if(queries.contains(o)){
							if(map.containsKey(o)){
								map.get(o).add(s);
							}else{
								HashSet<String> set= new HashSet<>();
								set.add(s);
								map.put(o, set);
							}
							
						}
						
					}
				}
			}
		}
		return map;
	}
	
	public HashMap<String,HashMap<String, Double>> predictBySimilarDrug(String idxfile,HashMap<String,HashSet<String>> queries,HashMap<String,HashSet<String>> associations,
			HashSet<String> allDrug) throws Exception{
		HashMap<String,HashMap<String, Double>> results=new DeepWalkMethod().getSimilarDrug( modelfile, idxfile,queries,  associations, allDrug);
		return results;
	}
	
	public List<Map.Entry<String, Double>> predictBySimilarDrug(String idxfile, HashMap<String,HashSet<String>> queries,HashSet<String> allTargets) throws Exception{
		List<Map.Entry<String, Double>>  results=new DeepWalkMethod().getSimilarDrug( modelfile,idxfile, queries, allTargets);
		return results;
	}
	
	
	public HashMap<String,HashMap<String,Double>> predictBySimilarTarget(String idxfile,HashSet<String> queries, HashMap<String,HashSet<String>> associations,
			HashSet<String> allDrugs, HashSet<String> allTargets ) throws Exception{
		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarTarget( modelfile, idxfile, queries,  allTargets); 
		HashMap<String,HashMap<String,Double>> results=getAssociateTargets_method1(tmp, associations, allDrugs);
		return results;
	}
	
	
	public HashMap<String,HashMap<String,Double>> predictBySimilarTarget(HashSet<String> queries, HashMap<String,HashSet<String>> associations,
			HashSet<String> allDrugs, HashSet<String> allTargets ) throws Exception{
		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarTarget( modelfile, idxfile, queries,  allTargets); 
		HashMap<String,HashMap<String,Double>> results=getAssociateTargets_method1(tmp, associations, allDrugs);
		return results;
	}
	
	public HashMap<String,HashMap<String,Double>> predictBySimilarTarget(HashSet<String> queries, HashMap<String,HashSet<String>> associations,
			HashSet<String> allDrugs, HashSet<String> allTargets ,HashMap<String,HashSet<String>> candidates) throws Exception{
		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarTarget( modelfile, idxfile, queries,  allTargets); 
		HashMap<String,HashMap<String,Double>> results=getAssociateTargets_method1(tmp, associations, allDrugs);
		fillempties( results, candidates);
		return results;
	}
	
	
	
	/**
	 * kkk
	 * @param queries
	 * @param associations
	 * @param allDrugs
	 * @param allTargets
	 * @param negatives
	 * @param positives
	 * @param type
	 * @return
	 * @throws Exception
	 */
	public HashMap<String,Double> predictBySimilarTarget(String query_target, String query_drug, HashMap<String,HashSet<String>> associations,
			 HashSet<String> allTargets , String type) throws Exception{

		/**
		 * marker
		 */
		HashMap<String,Double> tmp=new Node2vecMethod().getSimilarTarget( modelfile, idxfile, query_target,  allTargets); 
		
		HashMap<String,Double> result_tmp = null;
		if(type.equals("fs")){
			result_tmp=getAssociateTargets_method1(normalize_single_fs(tmp), associations, query_target, query_drug);
		}
		
		if(type.equals("sd")){
			 result_tmp=getAssociateTargets_method1(normalize_single_fs(tmp), associations, query_target, query_drug);
		}
		
		HashSet<String> set=new HashSet<String>();
		for(Entry<String,HashSet<String>> entry:associations.entrySet()) {
			for(String string:entry.getValue()) {
				if(string.equals(query_drug)) {
					set.add(entry.getKey());
				}
			}
		}
		
		HashMap<String, Double> result = new HashMap<String, Double>();
		for(Entry<String,Double> entry:result_tmp.entrySet()) {
			result.put(entry.getKey()+" "+query_target, entry.getValue()/set.size());
		}
		
		
		return result;
		
	}
	
	
	public HashMap<String,HashMap<String,Double>> predictBySimilarTarget(HashSet<String> queries, HashMap<String,HashSet<String>> associations,
			HashSet<String> allDrugs, HashSet<String> allTargets ,HashMap<String,HashSet<String>> negatives, Set<String> positives, String type) throws Exception{
		HashMap<String,HashMap<String, Double>> results = null;

		/**
		 * marker
		 */
//		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarTarget( modelfile, idxfile, queries,  allTargets); 
		HashMap<String,HashMap<String,Double>> tmp=new Node2vecMethod().getSimilarTarget( modelfile, idxfile, queries,  allTargets); 
//		if(type.equals(this.deeplwalk)){
//			HashMap<String,HashMap<String,Double>> result_tmp=getAssociateTargets_method1(normalize_sd(tmp), associations, allDrugs);
//			results= refineResults(result_tmp, negatives,positives);	
//		}
//		
//		if(type.equals(this.node2vec)){
//			HashMap<String,HashMap<String,Double>> result_tmp=getAssociateTargets_method1(normalize_fs(tmp), associations, allDrugs);
//			results= refineResults(result_tmp, negatives,positives);	
//		}
		
		/**
		 * test diffetnt fs and sd
		 * 
		 */
		
		if(type.equals("fs")){
			HashMap<String,HashMap<String,Double>> result_tmp=getAssociateTargets_method1(normalize_fs(tmp), associations, allDrugs);
			// fixed here
			results= refineResults(result_tmp, negatives,positives);	
		}
		
		if(type.equals("sd")){
			HashMap<String,HashMap<String,Double>> result_tmp=getAssociateTargets_method1(normalize_sd(tmp), associations, allDrugs);
			// fixed here
			results= refineResults(result_tmp, negatives,positives);	
		}
		
		
		return results;
	}
	
	
	public HashMap<String,HashMap<String,Double>> predictBySimilarTarget(HashSet<String> queries, HashMap<String,HashSet<String>> associations,
			HashSet<String> allDrugs, HashSet<String> allTargets ,HashMap<String,HashSet<String>> negatives, Set<String> positives) throws Exception{
		HashMap<String,HashMap<String, Double>> results = null;
			HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarTarget( modelfile, idxfile, queries,  allTargets); 
			HashMap<String,HashMap<String,Double>> result_tmp=getAssociateTargets_method1(normalize_sd(tmp), associations, allDrugs);
			results= refineResults(result_tmp, negatives,positives);	
		
		return results;
	}
	
	public HashMap<String,HashMap<String,Double>> predictBySimilarTarget_m2(HashSet<String> queries, HashMap<String,HashSet<String>> associations,
			HashSet<String> allDrugs, HashSet<String> allTargets ,HashMap<String,HashSet<String>> negatives, Set<String> positives, String type) throws Exception{
		HashMap<String,HashMap<String, Double>> results = null;
		if(type.equals(FastDrugInferencing.deeplwalk)){
			HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarTarget( modelfile, idxfile, queries,  allTargets); 
			HashMap<String,HashMap<String,Double>> result_tmp=getAssociateTargets_method2(normalize_sd(tmp), associations, allDrugs);
			results= refineResults(result_tmp, negatives,positives);	
		}
		
		if(type.equals(FastDrugInferencing.node2vec)){
			HashMap<String,HashMap<String,Double>> tmp=new Node2vecMethod().getSimilarTarget( modelfile, idxfile, queries,  allTargets); 
			HashMap<String,HashMap<String,Double>> result_tmp=getAssociateTargets_method1(normalize_fs(tmp), associations, allDrugs);
			results= refineResults(result_tmp, negatives,positives);	
		}
		
		return results;
	}
	
	
	public HashMap<String,HashMap<String,Double>> predictBySimilarTarget_m3(HashSet<String> queries, HashMap<String,HashSet<String>> associations,
			HashSet<String> allDrugs, HashSet<String> allTargets ,HashMap<String,HashSet<String>> negatives, Set<String> positives, String type) throws Exception{
		HashMap<String,HashMap<String, Double>> results = null;
		if(type.equals(FastDrugInferencing.deeplwalk)){
			HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarTarget( modelfile, idxfile, queries,  allTargets); 
			HashMap<String,HashMap<String,Double>> result_tmp=getAssociateTargets_method3(normalize_sd(tmp), associations, allDrugs);
			results= refineResults(result_tmp, negatives,positives);	
		}
		
		if(type.equals(FastDrugInferencing.node2vec)){
			HashMap<String,HashMap<String,Double>> tmp=new Node2vecMethod().getSimilarTarget( modelfile, idxfile, queries,  allTargets); 
			HashMap<String,HashMap<String,Double>> result_tmp=getAssociateTargets_method1(normalize_fs(tmp), associations, allDrugs);
			results= refineResults(result_tmp, negatives,positives);	
		}
		
		return results;
	}
	
	
	
public HashMap<String,HashMap<String,Double>> normalize_mean(HashMap<String,HashMap<String,Double>> map){
		
		HashMap<String,HashMap<String,Double>> result=new HashMap<>();
		int number=0;
		Double mean=0.0;
		for(Entry<String,HashMap<String,Double>> entry_1:map.entrySet()){
			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
				number++;
				mean+=entry_2.getValue();
			}
		}
		mean=mean/number;
	        
		for(Entry<String,HashMap<String,Double>> entry_1:map.entrySet()){
			HashMap<String,Double> tmp_map=new HashMap<>();
			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
				Double z=(entry_2.getValue()-mean);
				tmp_map.put(entry_2.getKey(), z);
			}
			result.put(entry_1.getKey(), tmp_map);
		}
		
		return result;
	}

public HashMap<String,HashMap<String,Double>> normalize_sd(HashMap<String,HashMap<String,Double>> map){
		
		HashMap<String,HashMap<String,Double>> result=new HashMap<>();
		int number=0;
		Double mean=0.0;
		for(Entry<String,HashMap<String,Double>> entry_1:map.entrySet()){
			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
				number++;
				mean+=entry_2.getValue();
			}
		}
		mean=mean/number;
		Double sd=0.0;
		for(Entry<String,HashMap<String,Double>> entry_1:map.entrySet()){
			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
				sd+=Math.pow(entry_2.getValue()-mean, 2);
			}
		}
		sd=Math.sqrt(sd/(number-1));
	        
		for(Entry<String,HashMap<String,Double>> entry_1:map.entrySet()){
			HashMap<String,Double> tmp_map=new HashMap<>();
			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
				Double z=(entry_2.getValue()-mean)/sd;
					tmp_map.put(entry_2.getKey(), z);	
				
			}
			result.put(entry_1.getKey(), tmp_map);
		}
		
		return result;
	}

public HashMap<String,HashMap<String,Double>> normalize_fs(HashMap<String,HashMap<String,Double>> map){
		
		HashMap<String,HashMap<String,Double>> result=new HashMap<>();
		
		Double min=Double.MAX_VALUE;
		Double max=Double.MIN_VALUE;		
		Double maxSection=1.0;
		Double minSection=-1.0;
		for(Entry<String,HashMap<String,Double>> entry_1:map.entrySet()){
			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
				if(entry_2.getValue()>max){
					max=entry_2.getValue();
				}
				if(entry_2.getValue()<min){
					min=entry_2.getValue();
				}
			}
		}
		
//		 double tmp = (originalData[i]-min)/(max-min);  
//	     double value = minSection + tmp*(maxSection - minSection);  
	        
		for(Entry<String,HashMap<String,Double>> entry_1:map.entrySet()){
			HashMap<String,Double> tmp_map=new HashMap<>();
			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
				Double tmp=(entry_2.getValue()-min)/(max-min);
				Double value=minSection+tmp*(maxSection - minSection);
				tmp_map.put(entry_2.getKey(), value);
			}
			result.put(entry_1.getKey(), tmp_map);
		}
		
		return result;
	}


/**
 * kkk
 * @param map
 * @return
 */
public HashMap<String,Double> normalize_single_fs(HashMap<String,Double> map){
	
	HashMap<String,HashMap<String,Double>> result=new HashMap<>();
	
	Double min=Double.MAX_VALUE;
	Double max=Double.MIN_VALUE;		
	Double maxSection=1.0;
	Double minSection=-1.0;
		for(Entry<String,Double> entry_2:map.entrySet()){
			if(entry_2.getValue()>max){
				max=entry_2.getValue();
			}
			if(entry_2.getValue()<min){
				min=entry_2.getValue();
			}
		}
	
//	 double tmp = (originalData[i]-min)/(max-min);  
//     double value = minSection + tmp*(maxSection - minSection);  
        
		HashMap<String,Double> tmp_map=new HashMap<>();
		for(Entry<String,Double> entry_2:map.entrySet()){
			Double tmp=(entry_2.getValue()-min)/(max-min);
			Double value=minSection+tmp*(maxSection - minSection);
			tmp_map.put(entry_2.getKey(), value);
		}
	
	return tmp_map;
}

	
public HashMap<String,HashMap<String, Double>> refineResults(HashMap<String,HashMap<String, Double>> results, HashMap<String,HashSet<String>> negatives, Set<String> positives){
		
	
//	Double min=Double.MAX_VALUE;
//	for(Entry<String,HashMap<String,Double>> entry_1:results.entrySet()){
//		for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
//			if(entry_2.getValue()<min){
//				min=entry_2.getValue();
//			}
//		}
//	}
	
		HashMap<String,HashMap<String, Double>> map=new HashMap<>();
		
		HashMap<String,Double> tmp=new HashMap<>();
		
		for(String positive: positives){
			String[] elements=positive.split(" ");
			if(results.containsKey(elements[1])){
				if(results.get(elements[1]).containsKey(elements[0])){
					tmp.put(elements[1]+" "+elements[0], results.get(elements[1]).get(elements[0]));
				}
			}
		}
		
		for(Entry<String,HashSet<String>> entry:negatives.entrySet()){
			if(results.containsKey(entry.getKey())){
				for(String string:entry.getValue()){
					if(results.get(entry.getKey()).containsKey(string)){
						tmp.put(entry.getKey()+" "+string, results.get(entry.getKey()).get(string));
					}else{
						tmp.put(entry.getKey()+" "+string, 0.0);
					}
				}
			}else{
				for(String string:entry.getValue()){
					
					tmp.put(entry.getKey()+" "+string, 0.0);
				}
			}
		}
		
		for(Entry<String,Double> entry:tmp.entrySet()){
			String[] elements=entry.getKey().split(" ");
			
			if(map.containsKey(elements[0])){
				map.get(elements[0]).put(elements[1], entry.getValue());
			}else{
				HashMap<String,Double> submap=new HashMap<>();
				submap.put(elements[1], entry.getValue());
				map.put(elements[0], submap);
			}
		}
		return map;
	}

	public void fillempties(HashMap<String,HashMap<String, Double>> results, HashMap<String,HashSet<String>> candidates){
		for(Entry<String,HashSet<String>> entry:candidates.entrySet()){
			
			if(results.containsKey(entry.getKey())){
				for(String string:entry.getValue()){
					if(!results.get(entry.getKey()).containsKey(string)){
						results.get(entry.getKey()).put(string, 0.0);
					}
				}
			}else{
				
				HashMap<String, Double> submap=new HashMap<>();
				for(String string:entry.getValue()){
					submap.put(string, 0.0);
				}
				results.put(entry.getKey(), submap);
			}
		}
	}
	
	public HashMap<String,List<Map.Entry<String, Double>>> predictBySimilarTarget(String idxfile, String datafile,HashSet<String> queries,int topN) throws Exception{
		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarTarget( modelfile, idxfile, queries,  topN);
		HashMap<String,List<Map.Entry<String, Double>>> results=getAssociateDrugs(tmp, datafile, topN);
		return results;
	}
	
	public HashMap<String,List<Map.Entry<String, Double>>> predictBySimilarDrug(String idxfile, HashMap<String,HashSet<String>> queries,int topN) throws Exception{
		HashMap<String,List<Map.Entry<String, Double>>> results=new DeepWalkMethod().getSimilarDrug( modelfile,idxfile, queries,  topN);
		return results;
	}
	
	
	public List<Map.Entry<String, Double>> getAssociateDrugs(HashMap<String,Double> input, String datafile, HashSet<String> allDrug) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(datafile)));
		HashMap<String,List<Map.Entry<String, Double>>> results=new HashMap<>();
		String line=null;
		HashMap<String,HashSet<String>> associations=new HashMap<>();
		while((line=br.readLine())!=null){
			if(!line.contains("\"")){
				InputStream inputStream = new ByteArrayInputStream(line.getBytes());
				NxParser nxp = new NxParser();
				nxp.parse(inputStream);
				while (nxp.hasNext()) {
				
					Node[] quard = nxp.next();
					String s = quard[0].toString().trim();
					String p = quard[1].toString().trim();
					String o = quard[2].toString().trim();
					if(s.startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/")
							&p.equals("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/target>")
							&o.startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/targets/")){
						
							if(associations.containsKey(o)){
								associations.get(o).add(s);
							}else{
								HashSet<String> set= new HashSet<>();
								set.add(s);
								associations.put(o, set);
							}
							
						
					}
				}
			}
		}
		
		HashMap<String,Double> tmp= new HashMap<>();
		
		for(Entry<String,Double> entry:input.entrySet()){
//			System.err.println("======================================");
//			System.err.println(entry.getKey());
				if(associations.containsKey(entry.getKey())){
					for(String drug:associations.get(entry.getKey())){
						if(tmp.containsKey(drug)){
							tmp.put(drug, tmp.get(drug)+entry.getValue());
//							System.err.println(drug+" associated drug score: "+entry.getValue().get(string));
						}else{
							tmp.put(drug, entry.getValue());
						}
					}
				}
		}
		
		for(String target:allDrug){
			if(!tmp.containsKey(target)){
				tmp.put(target, 0.0);
			}
		}
			
			List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(tmp.entrySet());  
			  
			Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {  
			    @Override  
			    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {  
			         return o2.getValue().compareTo(o1.getValue()); // 降序  
//			        return o1.getValue().compareTo(o2.getValue()); // 升序  
			    }  
			}); 
			
			Double max=list.get(0).getValue();
			Double min=list.get(list.size()-1).getValue();
			
			
			for (int i = 0; i < list.size(); i++) {
				list.get(i).setValue((list.get(i).getValue()-min)/(max-min));
			}
		
		return list;
		
	}
	
	
	/**
	 * kkk
	 * @param input
	 * @param associations
	 * @param allDrug
	 * @return
	 * @throws IOException
	 */
	
	public HashMap<String, Double> getAssociateTargets_method1(HashMap<String,Double> input, 
			HashMap<String,HashSet<String>> associations, String target, String drug) throws IOException{
			HashMap<String,Double> tmp= new HashMap<>();
			for(Entry<String,Double> entry:input.entrySet()){
					if(associations.containsKey(entry.getKey())){
						for(String d:associations.get(entry.getKey())){
							if(d.equals(drug)){
								
								if(!associations.containsKey(target)){
									if(tmp.containsKey(drug)){
										tmp.put(drug, tmp.get(drug)+entry.getValue());
									}else{
										tmp.put(drug, entry.getValue());
									}
								}else{
									if(!associations.get(target).contains(drug)){
										if(tmp.containsKey(drug)){
											tmp.put(drug, tmp.get(drug)+entry.getValue());
										}else{
											tmp.put(drug, entry.getValue());
										}
								}	
							}
						}
					}
				}
			}
			
		return tmp;
		
	}
	public HashMap<String,HashMap<String, Double>> getAssociateTargets_method1(HashMap<String,HashMap<String,Double>> input, 
			HashMap<String,HashSet<String>> associations, HashSet<String> allDrug) throws IOException{
		HashMap<String,HashMap<String, Double>>  results=new HashMap<>();
		for(Entry<String,HashMap<String,Double>> entry1:input.entrySet()){
			HashMap<String,Double> tmp= new HashMap<>();
			for(Entry<String,Double> entry:entry1.getValue().entrySet()){
					if(associations.containsKey(entry.getKey())){
						for(String target:associations.get(entry.getKey())){
							if(allDrug.contains(target)){
								
								if(!associations.containsKey(entry1.getKey())){
									if(tmp.containsKey(target)){
										tmp.put(target, tmp.get(target)+entry.getValue());
									}else{
										tmp.put(target, entry.getValue());
									}
								}else{
									if(!associations.get(entry1.getKey()).contains(target)){
										if(tmp.containsKey(target)){
											tmp.put(target, tmp.get(target)+entry.getValue());
										}else{
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
	
	
	public HashMap<String,HashMap<String, Double>> getAssociateTargets_method2(HashMap<String,HashMap<String,Double>> input, 
			HashMap<String,HashSet<String>> associations, HashSet<String> allDrug) throws IOException{
		HashMap<String,HashMap<String, Double>>  results=new HashMap<>();
		
		
		HashMap<String,HashSet<String>> inverse_associations=new HashMap<String,HashSet<String>>();
		
		for(Entry<String,HashSet<String>> entry:associations.entrySet()){
			for(String string:entry.getValue()){
				if(inverse_associations.containsKey(string)){
					inverse_associations.get(string).add(entry.getKey());
				}else{
					HashSet<String> set=new HashSet<>();
					set.add(entry.getKey());
					inverse_associations.put(string, set);
				}
			}
		}
		
		for(Entry<String,HashMap<String,Double>> entry1:input.entrySet()){
			HashMap<String,Double> tmp= new HashMap<>();
			for(Entry<String,Double> entry:entry1.getValue().entrySet()){
					if(associations.containsKey(entry.getKey())){
						for(String target:associations.get(entry.getKey())){
							if(allDrug.contains(target)){
								int weight=inverse_associations.get(target).size();
								
								if(!associations.containsKey(entry1.getKey())){
									if(tmp.containsKey(target)){
										tmp.put(target, tmp.get(target)+entry.getValue()*weight);
									}else{
										tmp.put(target, entry.getValue()*weight);
									}
								}else{
									if(!associations.get(entry1.getKey()).contains(target)){
										if(tmp.containsKey(target)){
											tmp.put(target, tmp.get(target)+entry.getValue()*weight);
										}else{
											tmp.put(target, entry.getValue()*weight);
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
	
	public HashMap<String,HashMap<String, Double>> getAssociateTargets_method3(HashMap<String,HashMap<String,Double>> input, 
			HashMap<String,HashSet<String>> associations, HashSet<String> allDrug) throws IOException{
		HashMap<String,HashMap<String, Double>>  results=new HashMap<>();
		
		
		HashMap<String,HashSet<String>> inverse_associations=new HashMap<String,HashSet<String>>();
		
		for(Entry<String,HashSet<String>> entry:associations.entrySet()){
			for(String string:entry.getValue()){
				if(inverse_associations.containsKey(string)){
					inverse_associations.get(string).add(entry.getKey());
				}else{
					HashSet<String> set=new HashSet<>();
					set.add(entry.getKey());
					inverse_associations.put(string, set);
				}
			}
		}
		
		for(Entry<String,HashMap<String,Double>> entry1:input.entrySet()){
			HashMap<String,Double> tmp= new HashMap<>();
			for(Entry<String,Double> entry:entry1.getValue().entrySet()){
					if(associations.containsKey(entry.getKey())){
						for(String target:associations.get(entry.getKey())){
							if(allDrug.contains(target)){
								int weight=inverse_associations.get(target).size();
								
								if(!associations.containsKey(entry1.getKey())){
									if(tmp.containsKey(target)){
										tmp.put(target, tmp.get(target)+entry.getValue()*Math.pow(weight,2));
									}else{
										tmp.put(target, entry.getValue()*Math.pow(weight,2));
									}
								}else{
									if(!associations.get(entry1.getKey()).contains(target)){
										if(tmp.containsKey(target)){
											tmp.put(target, tmp.get(target)+entry.getValue()*Math.pow(weight,2));
										}else{
											tmp.put(target, entry.getValue()*Math.pow(weight,2));
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
	public HashMap<String,List<Map.Entry<String, Double>>> getAssociateDrugs(HashMap<String,HashMap<String,Double>> input, String datafile, HashSet<String> allDrug, int topN) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(datafile)));
		HashMap<String,List<Map.Entry<String, Double>>> results=new HashMap<>();
		String line=null;
		HashMap<String,HashSet<String>> associations=new HashMap<>();
		while((line=br.readLine())!=null){
			if(!line.contains("\"")){
				InputStream inputStream = new ByteArrayInputStream(line.getBytes());
				NxParser nxp = new NxParser();
				nxp.parse(inputStream);
				while (nxp.hasNext()) {
				
					Node[] quard = nxp.next();
					String s = quard[0].toString().trim();
					String p = quard[1].toString().trim();
					String o = quard[2].toString().trim();
					if(s.startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/")
							&p.equals("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/target>")
							&o.startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/targets/")){
						
							if(associations.containsKey(o)){
								associations.get(o).add(s);
							}else{
								HashSet<String> set= new HashSet<>();
								set.add(s);
								associations.put(o, set);
							}
							
						
					}
				}
			}
		}
		
		HashMap<String,Double> tmp= new HashMap<>();
		
		
		for(Entry<String,HashMap<String,Double>> entry1:input.entrySet()){
			for(Entry<String,Double> entry:entry1.getValue().entrySet()){
//				System.err.println("======================================");
//				System.err.println(entry.getKey());
					if(associations.containsKey(entry.getKey())){
						for(String drug:associations.get(entry.getKey())){
							if(tmp.containsKey(drug)){
								tmp.put(drug, tmp.get(drug)+entry.getValue());
//								System.err.println(drug+" associated drug score: "+entry.getValue().get(string));
							}else{
								tmp.put(drug, entry.getValue());
							}
						}
					}
			}
				
				List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(tmp.entrySet());  
				  
				Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {  
				    @Override  
				    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {  
				         return o2.getValue().compareTo(o1.getValue()); // 降序  
//				        return o1.getValue().compareTo(o2.getValue()); // 升序  
				    }  
				}); 
				
				
				results.put(entry1.getKey(), list);
		}
		
		
		return results;
		
	}
	
	public HashMap<String,List<Map.Entry<String, Double>>> getAssociateDrugs(HashMap<String,HashMap<String,Double>> input, String datafile, int topN) throws IOException{
			BufferedReader br = new BufferedReader(new FileReader(new File(datafile)));
			HashMap<String,List<Map.Entry<String, Double>>> results=new HashMap<>();
			String line=null;
			HashMap<String,HashSet<String>> associations=new HashMap<>();
			while((line=br.readLine())!=null){
				if(!line.contains("\"")){
					InputStream inputStream = new ByteArrayInputStream(line.getBytes());
					NxParser nxp = new NxParser();
					nxp.parse(inputStream);
					while (nxp.hasNext()) {
					
						Node[] quard = nxp.next();
						String s = quard[0].toString().trim();
						String p = quard[1].toString().trim();
						String o = quard[2].toString().trim();
						if(s.startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/")
								&p.equals("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugbank/target>")
								&o.startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/targets/")){
							
								if(associations.containsKey(o)){
									associations.get(o).add(s);
								}else{
									HashSet<String> set= new HashSet<>();
									set.add(s);
									associations.put(o, set);
								}
								
							
						}
					}
				}
			}
			
			for(Entry<String,HashMap<String,Double>> entry:input.entrySet()){
//				System.err.println("======================================");
//				System.err.println(entry.getKey());
				
				HashMap<String,Double> alist=new HashMap<>();
				for(String target:entry.getValue().keySet()){
					if(associations.containsKey(target)){
						for(String drug:associations.get(target)){
							if(!alist.containsKey(drug)){
								alist.put(drug, entry.getValue().get(target));
//								System.err.println(drug+" associated drug score: "+entry.getValue().get(string));
							}else{
								Double value=alist.get(drug);
								alist.put(drug, entry.getValue().get(target)+value);
							}
						}
					}
				}
//				System.out.println("alist size: "+alist.size()+" similar targets : "+entry.getValue().size());
				List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(alist.entrySet());  
				  
				Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {  
				    @Override  
				    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {  
				         return o2.getValue().compareTo(o1.getValue()); // 降序  
//				        return o1.getValue().compareTo(o2.getValue()); // 升序  
				    }  
				}); 
				if(list.size()!=0){
					Double max=list.get(0).getValue();
					
					int length=0;
					if(topN>list.size()){
						length=list.size();
					}else{
						length=topN;
						
					}
					Double min=list.get(length-1).getValue();
					
					for (int i = 0; i < length; i++) {
						list.get(i).setValue((list.get(i).getValue()-min)/(max-min));
					}
				
//					System.err.println("======================================");
					results.put(entry.getKey(), list.subList(0, length-1));	
				}else{
					results.put(entry.getKey(), list);
				}
				
			}
			return results;
			
		
	}
	
	
	
	
	
	public Double precision(HashMap<String,ArrayList<String>> results){
		int hit=0;
		int all=0;
		for(Entry<String,ArrayList<String>> entry:results.entrySet()){
			for(String target:entry.getValue()){
				all++;
				if(goldmap.containsKey(entry.getKey())){
					if(goldmap.get(entry.getKey()).contains(target)){
//						System.out.println(entry.getKey()+" is correctly predicted: "+target);
						hit++;
					}	
				}
			}
		}
		
		return (double) hit/all;
	}
	
	public Double recall(HashMap<String,List<Map.Entry<String, Double>>> results){
		int hit=0;
		int all=0;
		for(Entry<String,HashSet<String>> entry:goldmap.entrySet()){
			for(String target:entry.getValue()){
				all++;
				if(results.containsKey(entry.getKey())){
					for(Map.Entry<String, Double> e:results.get(entry.getKey())){
						if(e.getKey().contains(target)){
							hit++;
							break;
						}
					}
					
				}
			}
		}
		
		return (double) hit/all;
	}
	
	
	
}
