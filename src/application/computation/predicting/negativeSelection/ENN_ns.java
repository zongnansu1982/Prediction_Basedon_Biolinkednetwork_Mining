package application.computation.predicting.negativeSelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import weka.classifiers.functions.LibSVM;
import weka.classifiers.meta.OneClassClassifier;
import weka.core.AttributeStats;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.*;

public class ENN_ns {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	public Instances train;
	public int numberOfNN=100;
	public HashMap<String, String> negative_names;
	public ENN_ns(HashMap<String, String> n_map, Instances data, int nearestNN){
		this.train=data;
		this.numberOfNN=nearestNN;
		this.negative_names=n_map;
	}
	
	
	public HashMap<String, Double> getTopN() throws Exception{
		
		HashMap<String, Double> map_tmp=new HashMap<>();
		
		Instances sourceInstances = this.train;
		int sampleSize = this.train.numInstances();
		
		int nClassIndex = this.train.classIndex(); // Get the class attribute
		AttributeStats stats = sourceInstances.attributeStats(nClassIndex);
		
		int nClasses = stats.distinctCount; // Gets the number of distinct classes
		int[] nNerarestNeighborsClasses = new int[nClasses];  // To get the majority class of the retrieved neighbours
		
		NearestNeighbourSearch NB = new KDTree(); // subclass of abstract NearestNeighbourSearch();
		
//		NearestNeighbourSearch NB = new weka.core.neighboursearch.KDTree(sourceInstances);
//		NearestNeighbourSearch NB = new BallTree(sourceInstances);
		
		// ENN - For all instances, an instance is kept if its class and 
		// the majority of its k nearest neighbours have the same class value
		Double min=Double.MAX_VALUE;
		Double max=Double.MIN_VALUE;
		NB.setInstances(sourceInstances);
	    for (int i = 0; i < sampleSize; i++) {
	    	
	    	if(i%5000==0){
	    		System.out.println(" enn percentage: "+(double)i/sampleSize);
	    	}
	    	
	    	// 1. Current Instance
	    	Instance target=train.instance(i); 
	    	
	    	if(target.value(nClassIndex)==target.classAttribute().indexOfValue("false")){
	    		 // 2. Get k neighbours of current instance (target)
		        Instances neighbours = NB.kNearestNeighbours(target, this.numberOfNN);
	            
		        // 3. Get majority class value of the k nearest neighbours
		        
	            // 3.1. Initialize counters of classes
	            for(int nIdx=0; nIdx < nClasses; nIdx++){
	                nNerarestNeighborsClasses[nIdx] = 0;
	            }
	            int classValue;
	     	    
	            // 3.2 Get the number of neighbours per class value
		     	//System.out.println("For neighbour: " + target.toString());
		     	//System.out.println("  with class: " + target.classValue());
	            int positive_neighbours=0;
		     	for (int j = 0; j < neighbours.numInstances(); j++) {
	                classValue = (int) neighbours.get(j).classValue();
	                nNerarestNeighborsClasses[classValue]++;
	                if(classValue==target.classAttribute().indexOfValue("true")){
	                	positive_neighbours++;
	                }
		     		//System.out.println("\t -> " + neighbours.get(j).toString());
		        	//System.out.println("\t -> Class: " + neighbours.get(j).classValue());
		   	    }
		     	
		     	StringBuffer sb=new StringBuffer();
		     	for (int j = 0; j < nClassIndex-1; j++) {
					sb.append(target.value(j)).append(" ");
				}
		     	String instance_name=sb.toString().trim();
		     	Double value=((double)positive_neighbours)/100;
		     	map_tmp.put(instance_name, value);
		     	
		     	if(value>max){
		     		max=value;
		     	}
		     	if(value<min){
		     		min=value;
		     	}
	    	}
	    }
	    	HashMap<String, Double> map=new HashMap<>();
	    	for(Entry<String,Double> entry:map_tmp.entrySet()){
	    		map.put(this.negative_names.get(entry.getKey()), (entry.getValue() - min) / (max - min));
	    	}
	    	
	    	return map;
	       
	}

}
