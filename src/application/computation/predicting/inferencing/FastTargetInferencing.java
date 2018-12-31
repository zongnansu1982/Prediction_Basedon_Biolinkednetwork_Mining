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
import java.util.Map.Entry;
import java.util.Set;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

import application.computation.embedding.deepwalk.DeepWalkMethod;
import application.computation.embedding.node2vec.Node2vecMethod;


public class FastTargetInferencing {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	
	public static String deepwalk="deepwalk";
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

	public FastTargetInferencing(String modelfile, String idxfile) throws NumberFormatException, IOException{
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
	
	
	public FastTargetInferencing(){
	}
	
	public void feedGold(String removedfile) throws IOException{
		goldmap=new HashMap<>();
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
					if(goldmap.containsKey(s)){
						goldmap.get(s).add(o);
					}else{
						HashSet<String> set=new HashSet<>();
						set.add(o);
						goldmap.put(s, set);
					}
				}
			}
		}
	}
	
	
	public void feedSVMGold(String removedfile) throws IOException{
		goldmap=new HashMap<>();
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
					String q = quard[3].toString().trim();
					if(q.endsWith("<+1>")){
						if(goldmap.containsKey(s)){
							goldmap.get(s).add(o);
						}else{
							HashSet<String> set=new HashSet<>();
							set.add(o);
							goldmap.put(s, set);
						}	
					}
				}
			}
		}
	}
	
	
	public HashSet<String> getDrugAsQueries() throws IOException{
		HashSet<String> set=new HashSet<>();
		
		for(String query:goldmap.keySet()){
			set.add(query);
		}
		return set;
	}
	
	public HashMap<String,HashSet<String>> getTargetAsQueries(String datafile) throws IOException{
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
						
						if(queries.contains(s)){
							if(map.containsKey(s)){
								map.get(s).add(o);
							}else{
								HashSet<String> set= new HashSet<>();
								set.add(o);
								map.put(s, set);
							}
							
						}
						
					}
				}
			}
		}
		return map;
	}
	
	public HashMap<String,HashMap<String, Double>> predictBySimilarTarget(String idxfile,HashMap<String,HashSet<String>> queries, HashMap<String,HashSet<String>> asspciations,HashSet<String> allTargets) throws Exception{
		HashMap<String,HashMap<String, Double>> results=new DeepWalkMethod().getSimilarTarget( modelfile, idxfile,queries, asspciations, allTargets);
		return results;
	}
	
	
	public List<Map.Entry<String, Double>> predictBySimilarTarget(String idxfile, HashMap<String,HashSet<String>> queries, HashSet<String> allTargets) throws Exception{
		List<Map.Entry<String, Double>> results=new DeepWalkMethod().getSimilarTarget( modelfile,idxfile, queries, allTargets);
		return results;
	}
	
	
	public HashMap<String,List<Map.Entry<String, Double>>> predictBySimilarTarget(String idxfile,HashMap<String,HashSet<String>> queries,int topN) throws Exception{
		HashMap<String,List<Map.Entry<String, Double>>> results=new DeepWalkMethod().getSimilarTarget( modelfile, idxfile, queries,  topN);
		return results;
	}
	
	
	public HashMap<String,HashMap<String, Double>> predictBySimilarDrug(String idxfile,HashSet<String> queries,HashMap<String,HashSet<String>> associations
			,HashSet<String> allDrug, HashSet<String> allTarget) throws Exception{
		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarDrug( modelfile, idxfile,queries,  allDrug);
		HashMap<String,HashMap<String, Double>> results=getAssociateTargets_method1(tmp, associations,allTarget);
		return results;
	}
	
	public HashMap<String,HashMap<String, Double>> predictBySimilarDrug(HashSet<String> queries,HashMap<String,HashSet<String>> associations
			,HashSet<String> allDrug, HashSet<String> allTarget) throws Exception{
		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarDrug( modelfile, idxfile, queries,  allDrug);
		HashMap<String,HashMap<String, Double>> results=getAssociateTargets_method1(tmp, associations,allTarget);
		return results;
	}
	
	public HashMap<String,HashMap<String, Double>> predictBySimilarDrug(HashSet<String> queries,HashMap<String,HashSet<String>> associations
			,HashSet<String> allDrug, HashSet<String> allTarget, HashMap<String,HashSet<String>> candidates) throws Exception{
		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarDrug( modelfile, idxfile, queries,  allDrug);
		HashMap<String,HashMap<String, Double>> results=getAssociateTargets_method1(tmp, associations,allTarget);
		fillempties(results, candidates);
		return results;
	}
	
	
	/**
	 * kkk
	 * @param queries
	 * @param associations
	 * @param allDrug
	 * @param allTarget
	 * @param negatives
	 * @param positives
	 * @param type
	 * @return
	 * @throws Exception
	 */
	
	
	public HashMap<String, Double> predictBySimilarDrug(String query_drug, String query_target, HashMap<String,HashSet<String>> associations
			,HashSet<String> allDrug, String type) throws Exception{
		HashMap<String, Double> result_tmp = null;
//		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarDrug( modelfile, idxfile, queries,  allDrug);
		HashMap<String,Double> tmp=new Node2vecMethod().getSimilarDrug( modelfile, idxfile, query_drug,  allDrug);
		
		
		if(type.equals("fs")){
			result_tmp=getAssociateTargets_method1(normalize_single_fs(tmp), associations,query_drug,query_target);
		}
		
		if(type.equals("sd")){
			result_tmp=getAssociateTargets_method1(normalize_single_sd(tmp), associations,query_drug,query_target);
		}
		
		HashSet<String> set=new HashSet<String>();
		for(Entry<String,HashSet<String>> entry:associations.entrySet()) {
			for(String string:entry.getValue()) {
				if(string.equals(query_target)) {
					set.add(entry.getKey());
				}
			}
		}
		
		HashMap<String, Double> result = new HashMap<String, Double>();
		for(Entry<String,Double> entry:result_tmp.entrySet()) {
			result.put(query_drug+" "+entry.getKey(), entry.getValue()/set.size());
		}
		
		
		return result;
	}
	
	
	public HashMap<String,HashMap<String, Double>> predictBySimilarDrug(HashSet<String> queries,HashMap<String,HashSet<String>> associations
			,HashSet<String> allDrug, HashSet<String> allTarget, HashMap<String,HashSet<String>> negatives, Set<String> positives, String type) throws Exception{
		HashMap<String,HashMap<String, Double>> results = null;
//		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarDrug( modelfile, idxfile, queries,  allDrug);
		HashMap<String,HashMap<String,Double>> tmp=new Node2vecMethod().getSimilarDrug( modelfile, idxfile, queries,  allDrug);
		
		
		/**
		 * orignial deepwalk for sd, node2vec for fs
		 */
//		if(type.equals(this.deepwalk)){
//			
//			HashMap<String,HashMap<String, Double>> result_tmp=getAssociateTargets_method1(normalize_sd(tmp), associations,allTarget);
//			results= refineResults(result_tmp, negatives,positives);	
//		}
//		
//		if(type.equals(this.node2vec)){
//			HashMap<String,HashMap<String, Double>> result_tmp=getAssociateTargets_method1(normalize_fs(tmp), associations,allTarget);
//			results= refineResults(result_tmp, negatives,positives);	
//		}

		
		if(type.equals("fs")){
			HashMap<String,HashMap<String, Double>> result_tmp=getAssociateTargets_method1(normalize_fs(tmp), associations,allTarget);
			
			// fixed here
			results= refineResults(result_tmp, negatives,positives);	
		}
		
		if(type.equals("sd")){
			HashMap<String,HashMap<String, Double>> result_tmp=getAssociateTargets_method1(normalize_sd(tmp), associations,allTarget);
			// fixed here
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


/**
 * kkk
 * @param map
 * @return
 */
public HashMap<String,Double> normalize_single_sd(HashMap<String,Double> map){
	
	HashMap<String,HashMap<String,Double>> result=new HashMap<>();
	int number=0;
	Double mean=0.0;
	
		for(Entry<String,Double> entry_2:map.entrySet()){
			number++;
			mean+=entry_2.getValue();
		}
	
	mean=mean/number;
	Double sd=0.0;
		for(Entry<String,Double> entry_2:map.entrySet()){
			sd+=Math.pow(entry_2.getValue()-mean, 2);
		}
	sd=Math.sqrt(sd/(number-1));
        
		HashMap<String,Double> tmp_map=new HashMap<>();
		for(Entry<String,Double> entry_2:map.entrySet()){
			Double z=(entry_2.getValue()-mean)/sd;
			tmp_map.put(entry_2.getKey(), z);
		}
	
	return tmp_map;
}


	public HashMap<String,HashMap<String,Double>> normalize_fs(HashMap<String,HashMap<String,Double>> map) throws IOException{
		
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
		
	        
		for(Entry<String,HashMap<String,Double>> entry_1:map.entrySet()){
			HashMap<String,Double> tmp_map=new HashMap<>();
			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
				Double tmp=(entry_2.getValue()-min)/(max-min);
				Double value=minSection+tmp*(maxSection - minSection);
				tmp_map.put(entry_2.getKey(), value);
//				bw.write(value+"\n");
			}
			result.put(entry_1.getKey(), tmp_map);
		}
//		bw.flush();
//		bw.close();
		return result;
	}
	
	/**
	 * kkk
	 * @param map
	 * @return
	 * @throws IOException
	 */
public HashMap<String,Double> normalize_single_fs(HashMap<String,Double> map) throws IOException{
		
		HashMap<String,HashMap<String,Double>> result=new HashMap<>();
		
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
		
	        
			HashMap<String,Double> tmp_map=new HashMap<>();
			for(Entry<String,Double> entry_2:map.entrySet()){
				Double tmp=(entry_2.getValue()-min)/(max-min);
				Double value=minSection+tmp*(maxSection - minSection);
				tmp_map.put(entry_2.getKey(), value);
			}
		return tmp_map;
	}

	public HashMap<String,HashMap<String, Double>> refineResults(HashMap<String,HashMap<String, Double>> results, HashMap<String,HashSet<String>> negatives, Set<String> positives){
		
		
//		Double min=Double.MAX_VALUE;
//		for(Entry<String,HashMap<String,Double>> entry_1:results.entrySet()){
//			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
//				if(entry_2.getValue()<min){
//					min=entry_2.getValue();
//				}
//			}
//		}
		
		HashMap<String,HashMap<String, Double>> map=new HashMap<>();
		
		HashMap<String,Double> tmp=new HashMap<>();
		
		for(String positive: positives){
			String[] elements=positive.split(" ");
			if(results.containsKey(elements[0])){
				if(results.get(elements[0]).containsKey(elements[1])){
					tmp.put(positive, results.get(elements[0]).get(elements[1]));
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
	
	public HashMap<String,List<Map.Entry<String, Double>>> predictBySimilarDrug(String idxfile,String datafile,HashSet<String> queries,int topN) throws Exception{
		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarDrug( modelfile, idxfile, queries,  topN);
		HashMap<String,List<Map.Entry<String, Double>>> results=getAssociateTargets(tmp, datafile, topN);
		return results;
	}
	
	
	public HashMap<String,List<Map.Entry<String, Double>>> predictBySimilarDrug(String datafile,HashSet<String> queries,int topN) throws Exception{
		HashMap<String,HashMap<String,Double>> tmp=new DeepWalkMethod().getSimilarDrug( modelfile, idxfile, queries,  topN);
		HashMap<String,List<Map.Entry<String, Double>>> results=getAssociateTargets(tmp, datafile, topN);
		return results;
	}
	
	public HashMap<String,List<Map.Entry<String, Double>>> getAssociateTargets(HashMap<String,HashMap<String,Double>> input, String datafile, int topN) throws IOException{
		
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
						
							if(associations.containsKey(s)){
								associations.get(s).add(o);
							}else{
								HashSet<String> set= new HashSet<>();
								set.add(o);
								associations.put(s, set);
							}
							
						
					}
				}
			}
		}
		
		for(Entry<String,HashMap<String,Double>> entry:input.entrySet()){
//			System.err.println("======================================");
//			System.err.println(entry.getKey());
			
			HashMap<String,Double> alist=new HashMap<>();
			for(String drug:entry.getValue().keySet()){
				if(associations.containsKey(drug)){
					for(String target:associations.get(drug)){
						if(!alist.containsKey(target)){
							alist.put(target, entry.getValue().get(drug));
//							System.err.println(drug+" associated drug score: "+entry.getValue().get(string));
						}else{
							Double value=alist.get(target);
							alist.put(target, entry.getValue().get(drug)+value);
						}
					}
				}
			}
			
			List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(alist.entrySet());  
			  
			Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {  
			    @Override  
			    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {  
			         return o2.getValue().compareTo(o1.getValue()); // 降序  
//			        return o1.getValue().compareTo(o2.getValue()); // 升序  
			    }  
			}); 
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
		
//			System.err.println("======================================");
			results.put(entry.getKey(), list.subList(0, length-1));
		}
		return results;
		
	}
	
	
	public HashMap<String,List<Map.Entry<String, Double>>> getAssociateTargets(HashMap<String,HashMap<String,Double>> input, String datafile, HashSet<String> allTarget, int topN) throws IOException{
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
						
							if(associations.containsKey(s)){
								associations.get(s).add(o);
							}else{
								HashSet<String> set= new HashSet<>();
								set.add(o);
								associations.put(s, set);
							}
							
						
					}
				}
			}
		}
		
		
		
		for(Entry<String,HashMap<String,Double>> entry1:input.entrySet()){
			HashMap<String,Double> tmp= new HashMap<>();
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
	
	
	/**
	 * kkk
	 * @param input
	 * @param associations
	 * @param target
	 * @return
	 * @throws IOException
	 */
	public HashMap<String, Double> getAssociateTargets_method1(HashMap<String,Double> input, 
			HashMap<String,HashSet<String>> associations, String drug,String target) throws IOException{
		
			HashMap<String,Double> tmp= new HashMap<>();
			for(Entry<String,Double> entry_2:input.entrySet()){
					if(associations.containsKey(entry_2.getKey())){
						for(String t:associations.get(entry_2.getKey())){
							if(t.equals(target)){
									
									if(!associations.containsKey(drug)){
										if(tmp.containsKey(target)){
											tmp.put(target, tmp.get(target)+entry_2.getValue());
										}else{
											tmp.put(target, entry_2.getValue());
										}
									}else{
										if(!associations.get(drug).contains(target)){
											if(tmp.containsKey(target)){
												tmp.put(target, tmp.get(target)+entry_2.getValue());
											}else{
												tmp.put(target, entry_2.getValue());
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
			HashMap<String,HashSet<String>> associations, HashSet<String> allTarget) throws IOException{
		HashMap<String,HashMap<String, Double>>  results=new HashMap<>();
		
		for(Entry<String,HashMap<String,Double>> entry_1:input.entrySet()){
			HashMap<String,Double> tmp= new HashMap<>();
			for(Entry<String,Double> entry_2:entry_1.getValue().entrySet()){
					if(associations.containsKey(entry_2.getKey())){
						for(String target:associations.get(entry_2.getKey())){
							if(allTarget.contains(target)){
								if(!associations.containsKey(entry_1.getKey())){
									if(tmp.containsKey(target)){
										tmp.put(target, tmp.get(target)+entry_2.getValue());
									}else{
										tmp.put(target, entry_2.getValue());
									}
								}else{
									if(!associations.get(entry_1.getKey()).contains(target)){
										if(tmp.containsKey(target)){
											tmp.put(target, tmp.get(target)+entry_2.getValue());
										}else{
											tmp.put(target, entry_2.getValue());
										}
									}	
								}
								
								
							}	
						}
					}
				}
			
			results.put(entry_1.getKey(), tmp);
		}
		
		return results;
		
	}
	
	
	public List<Map.Entry<String, Double>> getAssociateTargets(HashMap<String,Double> input, String datafile, HashSet<String> allTarget) throws IOException{
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
						
							if(associations.containsKey(s)){
								associations.get(s).add(o);
							}else{
								HashSet<String> set= new HashSet<>();
								set.add(o);
								associations.put(s, set);
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
			
		for(String target:allTarget){
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
	
	
}
