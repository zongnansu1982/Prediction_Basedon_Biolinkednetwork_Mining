package application.computation.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class EdgeEmbedding {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	public HashMap<String, Integer> idx;
	public HashMap<Integer, ArrayList<Double>> vectors;
	
	public void readIdex(String indexfile) throws NumberFormatException, IOException{
		 idx=new HashMap<>();
	    	BufferedReader br=new BufferedReader(new FileReader(new File(indexfile)));
	    	String line=null;
	    	while((line=br.readLine())!=null){
	    		String[] elements=line.split(" ");
	    		idx.put(elements[0], Integer.valueOf(elements[1]));
	    	}
	    	br.close();
	 }
	 
	public void readModel(String model) throws NumberFormatException, IOException{
			vectors=new HashMap<>();
	    	BufferedReader br=new BufferedReader(new FileReader(new File(model)));
	    	String line=null;
	    	while((line=br.readLine())!=null){
	    		String[] elements=line.split("\t");
	    		ArrayList<Double> list=new ArrayList<>();
	    		String[] v=elements[1].split(" ");
	    		for (int i = 0; i < v.length; i++) {
	    			list.add(Double.valueOf(v[i]));
				}
	    		vectors.put(Integer.valueOf(elements[0]), list);
	    	}
	    	br.close();
	 }
	
	public EdgeEmbedding(String modelfile, String idxfile) throws NumberFormatException, IOException{
		readModel(modelfile);
		readIdex(idxfile);
	}
	
	
	public ArrayList<Double> getEdgeVec(String node_1, String node_2, String method){
		return BinaryOperator.operate(vectors.get(idx.get(node_1)), vectors.get(idx.get(node_2)), method);
	}
	

}
