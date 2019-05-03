package application.computation.predicting.negativeSelection;

import java.util.HashMap;

import weka.core.Instance;
import weka.core.Instances;
import weka.core.neighboursearch.NearestNeighbourSearch;

public class ENN_ns_runnable implements Runnable {
	
	
	public int number=0;
	public NearestNeighbourSearch NB ;
	public Instances instances;
	public HashMap<String,Double> enn_results;
	
	public ENN_ns_runnable(NearestNeighbourSearch nb, Instances data, HashMap<String,Double> enn_results){
		this.NB=nb;
		this.instances=data;
		this.number=data.numInstances();
		this.enn_results=enn_results;
	}
	
	@Override
	public void run() {
		 for (int i = 0; i < number; i++) {
		    	
		    	if(i%5000==0){
		    		System.out.println(" enn percentage: "+(double)i/number);
		    	}
		    	
		    	// 1. Current Instance
		    	Instance target=instances.instance(i); 
		    	
		    	if(target.value(instances.classAttribute())==target.classAttribute().indexOfValue("false")){
		    		 // 2. Get k neighbours of current instance (target)
			        Instances neighbours = null;
					try {
						neighbours = NB.kNearestNeighbours(target, 100);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
		            
		     	    
		            // 3.2 Get the number of neighbours per class value
			     	//System.out.println("For neighbour: " + target.toString());
			     	//System.out.println("  with class: " + target.classValue());
		            int positive_neighbours=0;
			     	for (int j = 0; j < neighbours.numInstances(); j++) {
		                int classValue = (int) neighbours.get(j).classValue();
		                if(classValue==target.classAttribute().indexOfValue("true")){
		                	positive_neighbours++;
		                }
			     		//System.out.println("\t -> " + neighbours.get(j).toString());
			        	//System.out.println("\t -> Class: " + neighbours.get(j).classValue());
			   	    }
			     	
			     	StringBuffer sb=new StringBuffer();
			     	for (int j = 0; j < instances.classIndex()-1; j++) {
						sb.append(target.value(j)).append(" ");
					}
			     	String instance_name=sb.toString().trim();
			     	Double value=((double)positive_neighbours)/100;
			     	enn_results.put(instance_name, value);
		    	}
		    }
	}

}
