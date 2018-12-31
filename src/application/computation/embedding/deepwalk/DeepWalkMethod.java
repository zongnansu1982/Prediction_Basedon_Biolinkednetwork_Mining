package application.computation.embedding.deepwalk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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

import org.deeplearning4j.graph.api.Vertex;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.models.GraphVectors;
import org.deeplearning4j.graph.models.loader.GraphVectorSerializer;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeepWalkMethod  {
	
	public static void main(String[] args) throws Exception {
		
		
	    	
//	    	new DeepWalkMethod().training("data/network_test.nt", 
//	    			"data/network_test_deepwalk.model",
//	    			"data/network_test_deepwalk.idx", 
//	    			40,
//	    			0.01, 
//	    			10, 
//	    			5, 
//	    			10);
//		new DeepWalkMethod().test_getSimilarDrug("D:/data/drug-taget-network/Databases/data/output/network_remain_deepwalk_64_40_5_10_0.01.model",
//				"D:/data/drug-taget-network/Databases/data/output/network_remain_deepwalk_64_40_5_10_0.01.idx",
//				"<http://bio2rdf.org/drugbank:BE0001475>");
		
}

	
	private static Logger log = LoggerFactory.getLogger(DeepWalkMethod.class);
	
	public void training(String input, String modelfile, String idxfile, int numberOfWalk, Double learningRate, int vector, int windowsize, int length) throws Exception {
		// TODO Auto-generated method stub
		HashSet<String> nodes= new HashSet<>();
		HashSet<String> triples= new HashSet<>();
		BufferedReader br = new BufferedReader(new FileReader(new File(input)));
		String line=null;
		while((line=br.readLine())!=null){
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);
			
			while (nxp.hasNext()) {
				Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();
				
				if(s.startsWith("<http://")&o.startsWith("<http://")){
					nodes.add(s);
					nodes.add(o);
					triples.add(s+" "+p+" "+o);
				}
			}
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(idxfile)));
		ArrayList<Vertex<String>> vlist= new ArrayList<Vertex<String>>();
		HashMap<String,Integer> idx= new HashMap<>();
		int i=0;
		for(String node:nodes){
			Vertex<String> v= new Vertex<String>(i, node);
			vlist.add(v);
			bw.write(i+" "+node+"\n");
			idx.put(node, i);
			i++;
		}
		bw.flush();
		bw.close();
		
		System.out.println(idxfile+" writing finished ...");
		
		Graph<String, String> graph=new Graph<>(vlist);
		for(String triple:triples){
			String[] elements=triple.split(" ");
			graph.addEdge(idx.get(elements[0]), idx.get(elements[2]), elements[1], false); // undirected graph, original code
		}

	     log.info("Building model....");
	        
	     Deepwalk_enhenced<String, String> walk = new Deepwalk_enhenced.Builder().
	    		 learningRate(learningRate).
	    		 numberOfWalk(numberOfWalk).
	    		 vectorSize(vector).
	    		 windowSize(windowsize).
	    		 build(10);
	     
	     walk.initialize(graph);
	     log.info("Fitting Word2Vec model....");
	     walk.fit(graph, length);

	     log.info("Writing word vectors to text file....");

	        // Write word vectors
	    
	     LocalGraphVectorSerializer.writeGraphVectors(walk, modelfile);
		
	}
	
	
	
	
	 public HashMap<String,HashMap<String,Double>> getSimilarDrug(String modelfile, String idxfile,Set<String> queries,
	    		Set<String> allDrugs) throws Exception {
			// TODO Auto-generated method stub
		 BufferedReader br = new BufferedReader(new FileReader(new File(idxfile)));
			String line=null;
			HashMap<String,Integer> idx= new HashMap<>();
			HashMap<Integer,String> iidx= new HashMap<>();
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				idx.put(elements[1], Integer.valueOf(elements[0]));
				iidx.put(Integer.valueOf(elements[0]), elements[1]);
			}
			
		 HashMap<String,HashMap<String,Double>> map=new HashMap<>();
		 GraphVectors graphvector = GraphVectorSerializer.loadTxtVectors(new File(modelfile));
		 int i=0;
			for(String query:queries){
				i++;
//				if(i%queries.size()==0){
//					System.out.println((double)i/queries.size());
//				}
				HashMap<String,Double> amap=new HashMap<>();
				
				for(String drug:allDrugs){
					if(!query.equals(drug)){
//						System.out.println(drug +" "+idx.get(drug));
						
						double distance=graphvector.similarity(idx.get(query), idx.get(drug));
						amap.put(drug, distance);
					}
				}
				
				map.put(query, amap);
			}
			return map;
		}
	 
	 
	/**
	 * search from drug
	 * @param modelfile
	 * @param idxfile
	 * @param queries
	 * @param topN
	 * @return
	 * @throws Exception
	 */
	 public HashMap<String,HashMap<String,Double>> getSimilarDrug(String modelfile, String idxfile, HashSet<String> queries, int topN) throws Exception {
			// TODO Auto-generated method stub
		 	BufferedReader br = new BufferedReader(new FileReader(new File(idxfile)));
			String line=null;
			HashMap<String,Integer> idx= new HashMap<>();
			HashMap<Integer,String> iidx= new HashMap<>();
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				idx.put(elements[1], Integer.valueOf(elements[0]));
				iidx.put(Integer.valueOf(elements[0]), elements[1]);
			}
			
			
			HashMap<String,HashMap<String,Double>> map=new HashMap<>();
			GraphVectors graphvector = GraphVectorSerializer.loadTxtVectors(new File(modelfile));
			for(String query:queries){
				HashMap<String,Double> amap=new HashMap<>();
//				System.out.println(query+" "+idx.get(query));
				if(idx.containsKey(query)){
					int[] lst=graphvector.verticesNearest(idx.get(query), topN*10);
					for(int i:lst){
						if(amap.size()<topN){
							String target=iidx.get(lst[i]);
				        	if(target.startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/")&graphvector.similarity(idx.get(query), lst[i])>0.0){
				        		amap.put(target, graphvector.similarity(idx.get(query), lst[i]));
						}
						}
					}		
				}
				
				map.put(query, amap);
			}
			
			return map;
		}
	
	 public HashMap<String,HashMap<String,Double>> getSimilarTarget(String modelfile, String idxfile,HashSet<String> queries, Set<String> allTargets) throws Exception {
			// TODO Auto-generated method stub
		 BufferedReader br = new BufferedReader(new FileReader(new File(idxfile)));
			String line=null;
			HashMap<String,Integer> idx= new HashMap<>();
			HashMap<Integer,String> iidx= new HashMap<>();
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				idx.put(elements[1], Integer.valueOf(elements[0]));
				iidx.put(Integer.valueOf(elements[0]), elements[1]);
			}
			
	    	HashMap<String,HashMap<String,Double>> map=new HashMap<>();
	    	GraphVectors graphvector = GraphVectorSerializer.loadTxtVectors(new File(modelfile));
			for(String query:queries){
				HashMap<String,Double> amap=new HashMap<>();
				for(String target:allTargets){
					if(!query.equals(target)){
						double distance=graphvector.similarity(idx.get(query), idx.get(target));
//						double sim=(1-Math.acos(distance)/Math.PI);
//						java.util.Random random=new java.util.Random();
//						double distance=random.nextDouble();
						amap.put(target, distance);
					}
				}
				map.put(query, amap);
			}
			return map;
		}
	 
	 
	 public Double getSimilar(GraphVectors graphvector, HashMap<String,Integer> idx, String query1, String query2) throws Exception {
			// TODO Auto-generated method stub
	    	double distance=graphvector.similarity(idx.get(query1), idx.get(query2));
			return distance;
		}
	 
 
	 
	 /**
	  * search from target
	  * @param modelfile
	  * @param queries
	  * @param topN
	  * @return
	  * @throws Exception
	  */
	 public HashMap<String,HashMap<String,Double>> getSimilarTarget(String modelfile, String idxfile, HashSet<String> queries, int topN) throws Exception {
			// TODO Auto-generated method stub
		 BufferedReader br = new BufferedReader(new FileReader(new File(idxfile)));
			String line=null;
			HashMap<String,Integer> idx= new HashMap<>();
			HashMap<Integer,String> iidx= new HashMap<>();
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				idx.put(elements[1], Integer.valueOf(elements[0]));
				iidx.put(Integer.valueOf(elements[0]), elements[1]);
			}
			
		 
		 HashMap<String,HashMap<String,Double>> map=new HashMap<>();
			GraphVectors graphvector = GraphVectorSerializer.loadTxtVectors(new File(modelfile));
			for(String query:queries){
				HashMap<String,Double> amap=new HashMap<String,Double>();
				if(idx.containsKey(query)){
					int[] lst=graphvector.verticesNearest(idx.get(query), topN*10);
					for(int i:lst){
						if(amap.size()<topN){
							String target=iidx.get(lst[i]);
				        	if(target.startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/targets/")&graphvector.similarity(idx.get(query), lst[i])>0.0){
				        		amap.put(target, graphvector.similarity(idx.get(query), lst[i]));
				        	}
						}
					}	
				}
				
				map.put(query, amap);
			}
			
			return map;
		}
	 
	 
	 public HashMap<String,HashMap<String, Double>> getSimilarDrug(String modelfile, String idxfile, HashMap<String,HashSet<String>> queries, 
	    		HashMap<String,HashSet<String>> associations, HashSet<String> allTarget) throws Exception {
	  		// TODO Auto-generated method stub
	  		
		 BufferedReader br = new BufferedReader(new FileReader(new File(idxfile)));
			String line=null;
			HashMap<String,Integer> idx= new HashMap<>();
			HashMap<Integer,String> iidx= new HashMap<>();
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				idx.put(elements[1], Integer.valueOf(elements[0]));
				iidx.put(Integer.valueOf(elements[0]), elements[1]);
			}
			
		 HashMap<String,HashMap<String, Double>> map=new HashMap<>();
		 GraphVectors graphvector = GraphVectorSerializer.loadTxtVectors(new File(modelfile));
	  		
	  		for(Entry<String,HashSet<String>> entry:queries.entrySet()){
	  			HashMap<String,Double> rankings=new HashMap<>();
	  			for(String query:entry.getValue()){
	  				for(String target:allTarget){
	  					if(!entry.getValue().contains(target)){ // this is same with existing
	  						Double sim=graphvector.similarity(idx.get(query), idx.get(target));
				        		if(rankings.containsKey(target)){
				        			rankings.put(target, sim+rankings.get(target));
				        		}else{
				        			rankings.put(target, sim);
				        		}
	  						}
	  					}
	  				}
	  			map.put(entry.getKey(), rankings);
	  		}
	  		
	  		return map;
	  	}
	 
	 public List<Map.Entry<String, Double>> getSimilarDrug(String modelfile, String idxfile,HashMap<String,HashSet<String>> queries,HashSet<String> candidates) throws Exception {
			// TODO Auto-generated method stub
		 BufferedReader br = new BufferedReader(new FileReader(new File(idxfile)));
			String line=null;
			HashMap<String,Integer> idx= new HashMap<>();
			HashMap<Integer,String> iidx= new HashMap<>();
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				idx.put(elements[1], Integer.valueOf(elements[0]));
				iidx.put(Integer.valueOf(elements[0]), elements[1]);
			}
			
			HashMap<String,List<Map.Entry<String, Double>>> map=new HashMap<>();
			GraphVectors graphvector = GraphVectorSerializer.loadTxtVectors(new File(modelfile));
			
			HashMap<String,Double> rankings=new HashMap<>();
			HashSet<String> drugs=new HashSet<>();
			for(Entry<String,HashSet<String>> entry:queries.entrySet()){
				drugs.addAll(entry.getValue());
			}
			
			
				
			for(String d:drugs){
				for(String drug:candidates){
					if(!drugs.contains(drug)
			        			&graphvector.similarity(idx.get(d), idx.get(drug))>0.0){
			        	Double sim=graphvector.similarity(idx.get(d), idx.get(drug));
			        		if(rankings.containsKey(drug)){
			        			rankings.put(drug, sim+rankings.get(drug));
			        		}else{
			        			rankings.put(drug, sim);
			        		}
						}
					}
					
				}
			for(String drug:candidates){
				if(!rankings.containsKey(drug)){
					rankings.put(drug, 0.0);
				}
			}
			
			List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(rankings.entrySet());  
			  
			Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {  
			    @Override  
			    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {  
			         return o2.getValue().compareTo(o1.getValue()); // 降序  
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
	  * search from target
	  * @param modelfile
	  * @param queries
	  * @param topN
	  * @return
	  * @throws Exception
	  */
	 public HashMap<String,List<Map.Entry<String, Double>>> getSimilarDrug(String modelfile, String idxfile,HashMap<String,HashSet<String>> queries, int topN) throws Exception {
			// TODO Auto-generated method stub
		 
		 	BufferedReader br = new BufferedReader(new FileReader(new File(idxfile)));
			String line=null;
			HashMap<String,Integer> idx= new HashMap<>();
			HashMap<Integer,String> iidx= new HashMap<>();
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				idx.put(elements[1], Integer.valueOf(elements[0]));
				iidx.put(Integer.valueOf(elements[0]), elements[1]);
			}
			
			HashMap<String,List<Map.Entry<String, Double>>> map=new HashMap<>();
			GraphVectors graphvector = GraphVectorSerializer.loadTxtVectors(new File(modelfile));
			for(Entry<String,HashSet<String>> entry:queries.entrySet()){
				HashMap<String,Double> rankings=new HashMap<>();
				for(String query:entry.getValue()){
					int targetCounter=0;
					if(idx.containsKey(query)){
						int[] lst=graphvector.verticesNearest(idx.get(query), topN*10);
						for(int i:lst){
							if(targetCounter<topN){
								String target=iidx.get(lst[i]);
					        	if(target.startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/drugs/")&!entry.getValue().contains(target)
					        			&graphvector.similarity(idx.get(query), lst[i])>0.0){
					        		targetCounter++;
					        		Double sim=graphvector.similarity(idx.get(query), lst[i]);
					        		if(rankings.containsKey(target)){
					        			rankings.put(target, sim+rankings.get(target));
					        		}else{
					        			rankings.put(target, sim);
					        		}
					        	}
							}
						}	
					}
					
				}
				
				List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(rankings.entrySet());  
	    		  
	    		Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {  
	    		    @Override  
	    		    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {  
	    		         return o2.getValue().compareTo(o1.getValue()); // 降序  
	    		    }  
	    		}); 
	    		
	    		
	    		
	    		
	    		Double max=list.get(0).getValue();
	    		int length=0;
	    		Double min=0.0;
	    		if(topN>list.size()){
	    			length=list.size();
	    		}else{
	    			length=topN;
	    		}
	    		min=list.get(length-1).getValue();
	    		Double nor=max-min;
	    		
	    		for (int i = 0; i < length; i++) {
	    			list.get(i).setValue((list.get(i).getValue()-min)/nor);	
	    			
	    		
//	    			System.err.println(list.get(i).getKey()+" "+list.get(i).getValue());
//	    			System.err.println("========================================");
	    		}
	    		
	    		map.put(entry.getKey(), list.subList(0, length-1));
			}
			
			return map;
		}
	 
	 public HashMap<String,HashMap<String, Double>> getSimilarTarget(String modelfile, String idxfile,HashMap<String,HashSet<String>> queries, 
	    		HashMap<String,HashSet<String>> associations, HashSet<String> allTarget) throws Exception {
	  		// TODO Auto-generated method stub
		 BufferedReader br = new BufferedReader(new FileReader(new File(idxfile)));
			String line=null;
			HashMap<String,Integer> idx= new HashMap<>();
			HashMap<Integer,String> iidx= new HashMap<>();
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				idx.put(elements[1], Integer.valueOf(elements[0]));
				iidx.put(Integer.valueOf(elements[0]), elements[1]);
			}	
		 
		 
		 HashMap<String,HashMap<String, Double>> map=new HashMap<>();
		 GraphVectors graphvector = GraphVectorSerializer.loadTxtVectors(new File(modelfile));
	  		
	  		for(Entry<String,HashSet<String>> entry:queries.entrySet()){
	  			HashMap<String,Double> rankings=new HashMap<>();
	  			for(String query:entry.getValue()){
	  				for(String target:allTarget){
	  					if(!entry.getValue().contains(target)){ // this is same with existing
	  						Double sim=graphvector.similarity(idx.get(query), idx.get(target));
	  						if(sim>0){
	  							rankings.put(target, sim);
	  						}	
	  						}
	  					}
	  				}
	  			map.put(entry.getKey(), rankings);
	  		}
	  		
	  		return map;
	  	}
	 
	 public List<Map.Entry<String, Double>> getSimilarTarget(String modelfile, String idxfile, HashMap<String,HashSet<String>> queries,HashSet<String> candidates) throws Exception {
			// TODO Auto-generated method stub
		 BufferedReader br = new BufferedReader(new FileReader(new File(idxfile)));
			String line=null;
			HashMap<String,Integer> idx= new HashMap<>();
			HashMap<Integer,String> iidx= new HashMap<>();
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				idx.put(elements[1], Integer.valueOf(elements[0]));
				iidx.put(Integer.valueOf(elements[0]), elements[1]);
			}
			
		 
			GraphVectors graphvector = GraphVectorSerializer.loadTxtVectors(new File(modelfile));
			
			HashMap<String,Double> rankings=new HashMap<>();
			HashSet<String> targets=new HashSet<>();
			for(Entry<String,HashSet<String>> entry:queries.entrySet()){
				targets.addAll(entry.getValue());
			}
			
			
				
			for(String t:targets){
				for(String target:candidates){
					if(!targets.contains(target)
			        			&graphvector.similarity(idx.get(t), idx.get(target))>0.0){
			        	Double sim=graphvector.similarity(idx.get(t), idx.get(target));
			        		if(rankings.containsKey(target)){
			        			rankings.put(target, sim+rankings.get(target));
			        		}else{
			        			rankings.put(target, sim);
			        		}
						}
					}
					
				}
				
			for(String target:candidates){
				if(!rankings.containsKey(target)){
					rankings.put(target, 0.0);
				}
			}
			
			
			List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(rankings.entrySet());  
			  
			Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {  
			    @Override  
			    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {  
			         return o2.getValue().compareTo(o1.getValue()); // 降序  
			    }  
			});
			
			Double max=list.get(0).getValue();
			Double min=list.get(list.size()-1).getValue();
			
			for (int i = 0; i < list.size(); i++) {
				list.get(i).setValue((list.get(i).getValue()-min)/(max-min));
			}
			
//			System.err.println("========================================");
//			System.err.println(entry.getKey());
			
			return list;
		}
	 
	 
	 /**
	  * search from drug
	  * @param modelfile
	  * @param idxfile
	  * @param queries
	  * @param topN
	  * @return
	  * @throws Exception
	  */
	 public HashMap<String,List<Map.Entry<String, Double>>> getSimilarTarget(String modelfile, String idxfile, HashMap<String,HashSet<String>> queries, int topN) throws Exception {
			// TODO Auto-generated method stub
		 	
		 	BufferedReader br = new BufferedReader(new FileReader(new File(idxfile)));
			String line=null;
			HashMap<String,Integer> idx= new HashMap<>();
			HashMap<Integer,String> iidx= new HashMap<>();
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				idx.put(elements[1], Integer.valueOf(elements[0]));
				iidx.put(Integer.valueOf(elements[0]), elements[1]);
			}
			
			HashMap<String,List<Map.Entry<String, Double>>> map=new HashMap<>();
			GraphVectors graphvector = GraphVectorSerializer.loadTxtVectors(new File(modelfile));
			for(Entry<String,HashSet<String>> entry:queries.entrySet()){
				HashMap<String,Double> rankings=new HashMap<>();
				for(String query:entry.getValue()){
					int targetCounter=0;
					if(idx.containsKey(query)){
						int[] lst=graphvector.verticesNearest(idx.get(query), topN*10);
						
						for(int i:lst){
							if(targetCounter<topN){
								
					        	if(iidx.get(lst[i]).startsWith("<http://www4.wiwiss.fu-berlin.de/drugbank/resource/targets/")&!entry.getValue().contains(iidx.get(lst[i]))
					        			&graphvector.similarity(idx.get(query), lst[i])>0.0){
					        		targetCounter++;
					        		Double sim=graphvector.similarity(idx.get(query), lst[i]);
					        		if(rankings.containsKey(iidx.get(lst[i]))){
					        			rankings.put(iidx.get(lst[i]), sim+rankings.get(iidx.get(lst[i])));
					        		}else{
					        			rankings.put(iidx.get(lst[i]), sim);
					        		}
					        	}
							}
						}	
					}
					
				}
				
				List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(rankings.entrySet());  
				  
				Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {  
				    @Override  
				    public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {  
				         return o2.getValue().compareTo(o1.getValue()); // 降序  
//				        return o1.getValue().compareTo(o2.getValue()); // 升序  
				    }  
				}); 
				
//				System.err.println("========================================");
//				System.err.println(entry.getKey());
				
				Double max=list.get(0).getValue();
				int length=0;
				Double min=0.0;
				if(topN>list.size()){
					length=list.size();
				}else{
					length=topN;
				}
				
				
				min=list.get(length-1).getValue();
				Double nor=max-min;
				
				for (int i = 0; i < length; i++) {
					list.get(i).setValue((list.get(i).getValue()-min)/nor);	
				
//					System.err.println(list.get(i).getKey()+" "+list.get(i).getValue());
//					System.err.println("========================================");
				}
				
				map.put(entry.getKey(), list.subList(0, length-1));
				
				
			}
			
			return map;
		}

}
