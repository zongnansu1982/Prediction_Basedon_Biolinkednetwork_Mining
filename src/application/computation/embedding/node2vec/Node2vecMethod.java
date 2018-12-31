package application.computation.embedding.node2vec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.impl.DenseDoubleMatrix1D;

public class Node2vecMethod {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public HashMap<Integer,double[]> readModel(String modelFile) throws IOException{
		BufferedReader br=new BufferedReader(new FileReader(new File(modelFile)));
		String line=null;
		HashMap<Integer,double[]> map=new HashMap<>();
		while((line=br.readLine())!=null){
			String[] element=line.split("\t");
			int name=Integer.valueOf(element[0]);
			double[] array=new double[element.length-1];
			for (int i = 1; i < element.length; i++) {
				array[i-1]=Double.valueOf(element[i]);
			}
			map.put(name, array);
		}
		return map;
	}
	
	public Double similarity(double[] vec1,double[] vec2){
		DoubleMatrix1D a = new DenseDoubleMatrix1D(vec1);
		DoubleMatrix1D b = new DenseDoubleMatrix1D(vec2);
		double cosineDistance = a.zDotProduct(b)/Math.sqrt(a.zDotProduct(a)*b.zDotProduct(b));
		return cosineDistance;
	}
	
	
	/**
	 * kkk
	 */
	public HashMap<String,Double> getSimilarTarget(String modelfile, String idxfile,String query, HashSet<String> allTargets) throws Exception {
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
		HashMap<Integer,double[]> vectors=readModel(modelfile);
		
			HashMap<String,Double> amap=new HashMap<>();
			for(String target:allTargets){
				if(!query.equals(target)){
					double sim=similarity(vectors.get(idx.get(query)), vectors.get(idx.get(target)));
					amap.put(target, sim);
				}
			}
		return amap;
	}
	
	 public HashMap<String,HashMap<String,Double>> getSimilarTarget(String modelfile, String idxfile,HashSet<String> queries, HashSet<String> allTargets) throws Exception {
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
			HashMap<Integer,double[]> vectors=readModel(modelfile);
			
	    	HashMap<String,HashMap<String,Double>> map=new HashMap<>();
			for(String query:queries){
				HashMap<String,Double> amap=new HashMap<>();
				for(String target:allTargets){
					if(!query.equals(target)){
						double sim=similarity(vectors.get(idx.get(query)), vectors.get(idx.get(target)));
						amap.put(target, sim);
					}
				}
				map.put(query, amap);
			}
			return map;
		}
	 
	 public static double generateRandomValue(Random random){
			double mx,mn,r;
			mx=1;
			mn=-1;
			r=mn+random.nextDouble()*(mx-mn);
			return r;
		}
	 
	 public HashMap<String,HashMap<String,Double>> getSimilarDrug(String modelfile, String idxfile,HashSet<String> queries,
	    		HashSet<String> allDrugs) throws Exception {
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
		HashMap<Integer,double[]> vectors=readModel(modelfile);
		 HashMap<String,HashMap<String,Double>> map=new HashMap<>();
		 int i=0;
			for(String query:queries){
				i++;
				HashMap<String,Double> amap=new HashMap<>();
				
				for(String drug:allDrugs){
					if(!query.equals(drug)){
						double distance=similarity(vectors.get(idx.get(query)), vectors.get(idx.get(drug)));
						amap.put(drug, distance);
					}
				}
				
				map.put(query, amap);
			}
			return map;
		}
	 
	 
	 /**
	  * kkk
	  * @param modelfile
	  * @param idxfile
	  * @param queries
	  * @param allDrugs
	  * @return
	  * @throws Exception
	  */
	 public HashMap<String,Double> getSimilarDrug(String modelfile, String idxfile,String query,
	    		HashSet<String> allDrugs) throws Exception {
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
		HashMap<Integer,double[]> vectors=readModel(modelfile);
		HashMap<String,Double> amap=new HashMap<>();
				
				for(String drug:allDrugs){
					if(!query.equals(drug)){
						double distance=similarity(vectors.get(idx.get(query)), vectors.get(idx.get(drug)));
						amap.put(drug, distance);
					}
				}
				
			return amap;
		}
	 
	 public Double getSimilar(HashMap<Integer,double[]> vectors, HashMap<String,Integer> idx,String query_1, String query_2) throws Exception {
			// TODO Auto-generated method stub
			double distance=similarity(vectors.get(idx.get(query_1)), vectors.get(idx.get(query_2)));
				
			return distance;
		}
	 
}
