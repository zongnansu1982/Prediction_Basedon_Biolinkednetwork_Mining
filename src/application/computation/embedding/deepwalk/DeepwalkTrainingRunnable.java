package application.computation.embedding.deepwalk;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.deeplearning4j.graph.api.Vertex;
import org.deeplearning4j.graph.graph.Graph;
import org.deeplearning4j.graph.iterator.RandomWalkIterator;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeepwalkTrainingRunnable implements Runnable {
	String datafile;
	String modelfile;
	String idxfile;
	int numberOfwalk=40;
	Double learningRate=0.01;
	int vector=100;
	int window=5;
	int length=40;
	private static Logger log = LoggerFactory.getLogger(DeepwalkTrainingRunnable.class);
	public DeepwalkTrainingRunnable(String datafile, String modelfile, String idxfile,
			int numberOfwalk, Double learningRate, int vector, int window, int length){
		
		this.datafile=datafile;
		this.modelfile=modelfile;
		this.idxfile=idxfile;
		this.numberOfwalk=numberOfwalk;
		this.learningRate=learningRate;
		this.vector=vector;
		this.window=window;
		this.length=length;
		
	}
	
	
	

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		HashSet<String> nodes= new HashSet<>();
		HashSet<String> triples= new HashSet<>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(new File(datafile)));
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String line=null;
		try {
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
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(new File(idxfile)));
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		ArrayList<Vertex<String>> vlist= new ArrayList<Vertex<String>>();
		HashMap<String,Integer> idx= new HashMap<>();
		int i=0;
		for(String node:nodes){
			Vertex<String> v= new Vertex<String>(i, node);
			vlist.add(v);
			try {
				bw.write(i+" "+node+"\n");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			idx.put(node, i);
			i++;
		}
		try {
			bw.flush();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			bw.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		Graph<String, String> graph=new Graph<>(vlist);
		for(String triple:triples){
			String[] elements=triple.split(" ");
			graph.addEdge(idx.get(elements[0]), idx.get(elements[2]), elements[1], false); // undirected graph, original code
		}
		
//		for (int j = 0; j < nodes.size(); j++) {
//			if(graph.getVertexDegree(j)==0){
//				graph.addEdge(j, j, "<http://www.w3.org/2002/07/owl#sameAs>", true);
//			}
//		}
		
		 RandomWalkIterator iter =  new RandomWalkIterator<>(graph, length);
	        // Split on white spaces in the line to get words

	     log.info("Building model....");
	        
	     Deepwalk_enhenced<String, String> walk = new Deepwalk_enhenced.Builder()
	    		 .learningRate(learningRate)
	    		 .numberOfWalk(numberOfwalk)
	    		 .vectorSize(vector)
	    		 .windowSize(window)
	    		 .build();
	     walk.initialize(graph);
	     log.info("Fitting Word2Vec model....");
	     walk.fit(iter);

	     log.info("Writing word vectors to text file....");

	        // Write word vectors
	    
	     try {
			LocalGraphVectorSerializer.writeGraphVectors(walk, modelfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	     
	}

}
