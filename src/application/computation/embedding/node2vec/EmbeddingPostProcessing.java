package application.computation.embedding.node2vec;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Random;

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

public class EmbeddingPostProcessing {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
	}
	
	

	public static void generateNewIndx(String input, String idxFile) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(new File(input)));
		String line=null;
		HashSet<String> nodes=new HashSet<>(); 
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
				}
			}
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(idxFile)));
		int i=0;
		for(String node:nodes){
			bw.write(i+" "+node+"\n");
			i++;
		}
		br.close();
		bw.flush();
		bw.close();
	}
	
	
	
	
	public static void changeDelimiter(String input, String output) throws IOException{
		
			System.out.println(input);
			BufferedReader br=new BufferedReader(new FileReader(input));
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File(output)));
			String line=null;
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				if(elements.length>2){
					String line1=line.replaceAll(" ", "\t");
					bw.write(line1+"\n");
//					String url=elements[0];
//					if(url!=null){
//						StringBuffer sb=new StringBuffer();
//						for (int i = 1; i < elements.length; i++) {
//							sb.append("\t").append(elements[i]);
//						}	
//						bw.write(url+"\t"+sb.toString().trim()+"\n");
//					}else{
//						System.out.println("Null detected ...");
//						System.out.println(line);
//						System.exit(0);
//					}	
				}
			}
			bw.flush();
			bw.close();
			br.close();
	}

	
	public static double generateRandomValue(Random random){
		double mx,mn,r;
		mx=1;
		mn=-1;
		r=mn+random.nextDouble()*(mx-mn);
		return r;
	}
	public static void changeDelimiter_random(String input, String output) throws IOException{
		
		System.out.println(input);
		BufferedReader br=new BufferedReader(new FileReader(input));
		BufferedWriter bw=new BufferedWriter(new FileWriter(new File(output)));
		String line=null;
		Random random=new Random(1024);
		while((line=br.readLine())!=null){
			String[] elements=line.split("\t");
			if(elements.length>2){
				String url=elements[0];
				if(url!=null){
					StringBuffer sb=new StringBuffer();
					sb.append(url+" ");
					for (int i = 1; i < elements.length; i++) {
						sb.append(generateRandomValue(random)+" ");
					}
					String line1=sb.toString().replaceAll(" ", "\t");
					bw.write(line1+"\n");
				}else{
					System.out.println("Null detected ...");
					System.out.println(line);
					System.exit(0);
				}	
			}
		}
		bw.flush();
		bw.close();
		br.close();
}
	
	
	public static void writeNewEmb(HashMap<String,String> mapping, String oldemb, String newemb) throws IOException{
			BufferedReader br=new BufferedReader(new FileReader(oldemb));
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File(newemb)));
			String line=null;
			while((line=br.readLine())!=null){
				String[] elements=line.split(" ");
				if(elements.length>2){
					String url=mapping.get(elements[0]);
					if(url!=null){
						StringBuffer sb=new StringBuffer();
						for (int i = 1; i < elements.length; i++) {
							sb.append("\t").append(elements[i]);
						}	
						bw.write(url+sb.toString()+"\n");
					}else{
						System.out.println("Null detected ...");
						System.out.println(line);
						System.exit(0);
					}	
				}
			}
			bw.flush();
			bw.close();
			br.close();
	}
	
	
	
	public static HashMap<String,String> getmapping(String oldidx, String deepwalkidx) throws IOException{
		 HashMap<String,String> oldIdx=readIdx(oldidx);
		 HashMap<String,String> deepIdx=readIdx(deepwalkidx);
		 
		 HashMap<String,String> mapping=new HashMap<>();
		 for(Entry<String,String> entry:deepIdx.entrySet()){
			 mapping.put(oldIdx.get(entry.getKey()), entry.getValue());
		 }
		 return mapping;
	}
	
	public static HashMap<String,String> readIdx(String file) throws IOException{
		HashMap<String,String> map=new HashMap<>();
		BufferedReader br =new BufferedReader(new FileReader(new File(file)));
		String line=null;
		while((line=br.readLine())!=null){
			String[] elements=line.split(" ");
			map.put(elements[1], elements[0]);
		}
		br.close();
		return map;
	}
	
}
