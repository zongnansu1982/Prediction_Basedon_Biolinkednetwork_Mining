package application.computation.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;


public class Extractor {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
			BufferedWriter bw=new BufferedWriter(new FileWriter(new File("data/network_bi.nt")));
			BufferedReader br = new BufferedReader(new FileReader(new File("data/network.nt")));
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
						
						if(p.equals("<http://bio2rdf.org/MultiPartiteNetwork_vocabulary:Drug-Target>")
								&s.startsWith("<http://bio2rdf.org/drugbank:DB")
								&o.startsWith("<http://bio2rdf.org/drugbank:BE")){
							bw.write(line+"\n");
						}
					}
				}
			}
			br.close();
			bw.flush();
			bw.close();
	}

}
