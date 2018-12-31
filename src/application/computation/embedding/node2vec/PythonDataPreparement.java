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

import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;

public class PythonDataPreparement {

	public static void main(String[] args) throws IOException {
		
	}

	public static void index(String allData, String idexFile) throws IOException {
		System.out.println("Loading graph");
		HashSet<String> nodes = new HashSet<>();
		BufferedReader br = new BufferedReader(new FileReader(allData));
		String line = null;

		while ((line = br.readLine()) != null) {
			// parse the line text to get the edge info
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);
			while (nxp.hasNext()) {
				org.semanticweb.yars.nx.Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();
				nodes.add(s);
				nodes.add(o);
			}
		}
		int i = 0;
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(idexFile)));
		HashMap<String, Integer> idx = new HashMap();
		for (String node : nodes) {
			idx.put(node, i);
			bw1.write(node + " " + i + "\n");
			i++;
		}
		bw1.flush();
		bw1.close();
		br.close();
	}

	public static HashMap<String, Integer> readIndex(String indexfile) throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(indexfile));
		String line = null;
		HashMap<String, Integer> map = new HashMap<>();
		while ((line = br.readLine()) != null) {
			// parse the line text to get the edge info
			String[] elements = line.split(" ");
			map.put(elements[0], Integer.valueOf(elements[1]));

		}
		br.close();
		return map;
	}

	public static void inverseIndex(String indexfile, String outfile) throws NumberFormatException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(indexfile));
		BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));
		String line = null;
		HashMap<String, Integer> map = new HashMap<>();
		while ((line = br.readLine()) != null) {
			// parse the line text to get the edge info
			String[] elements = line.split(" ");
			map.put(elements[0], Integer.valueOf(elements[1]));
			bw.write(elements[1] + " " + elements[0] + "\n");
		}
		bw.flush();
		bw.close();
		br.close();
	}

	public static void convertData(HashMap<String, Integer> map, String targetData, String convertedData)
			throws IOException {
		System.out.println("converting file: " + convertedData);
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(convertedData)));
		BufferedReader br = new BufferedReader(new FileReader(targetData));
		String line = null;

		while ((line = br.readLine()) != null) {
			// parse the line text to get the edge info
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);
			while (nxp.hasNext()) {
				org.semanticweb.yars.nx.Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();
				if (map.containsKey(s) && map.containsKey(o)) {
					bw2.write(map.get(s) + " " + map.get(o) + "\n");
				} else {
					System.out.println("not contained in idx : " + line);
					System.exit(0);
				}

			}
		}
		bw2.flush();
		bw2.close();
		br.close();
	}
	
	
	
	public static void convertData(HashMap<String, Integer> map, String targetData, String refinedTargetData,String convertedData)
			throws IOException {
		System.out.println("converting file: " + convertedData);
		BufferedWriter bw1 = new BufferedWriter(new FileWriter(new File(refinedTargetData)));
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(new File(convertedData)));
		BufferedReader br = new BufferedReader(new FileReader(targetData));
		String line = null;

		while ((line = br.readLine()) != null) {
			// parse the line text to get the edge info
			String[] content=line.split("\t");
			InputStream inputStream = new ByteArrayInputStream(content[0].getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);
			while (nxp.hasNext()) {
				org.semanticweb.yars.nx.Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();
				if (map.containsKey(s) && map.containsKey(o)) {
					bw1.write(content[0]+"\n");
					bw2.write(map.get(s) + " " + map.get(o) + "\n");
				} else {
					System.out.println("not contained in idx : " + line);
					if(!map.containsKey(s)) {
						System.out.println(s+" is missing");
					}
					if(!map.containsKey(o)) {
						System.out.println(o+" is missing");
					}
				}

			}
		}
		bw2.flush();
		bw2.close();
		bw1.flush();
		bw1.close();
		br.close();
	}

	public static void convertDatas(HashMap<String, Integer> map, String path) throws IOException {
		for (File dir : new File(path).listFiles()) {
			for (File file : dir.listFiles()) {
				String absolutePath = file.getAbsolutePath().trim();
				System.out.println(absolutePath);
				if (file.getName().equals("traininging.nt")) {
					String name = absolutePath.substring(0, absolutePath.lastIndexOf("\\")) + "\\traininging.edgelist";
					System.err.println(name);
					convertData(map, absolutePath, name);
				}
				if (file.getName().equals("testing.nt")) {

					String name = absolutePath.substring(0, absolutePath.lastIndexOf("\\")) + "\\testing.edgelist";
					System.err.println(name);
					convertData(map, absolutePath, name);

				}
				if (file.getName().equals("testing_base.nt")) {
					String name = absolutePath.substring(0, absolutePath.lastIndexOf("\\")) + "\\testing_base.edgelist";
					System.err.println(name);
					convertData(map, absolutePath, name);
				}
			}
		}
	}

	
	
	
	public static void generate_netowrk(String networkfile, String idxfile, String datafile_for_node2vec) throws IOException {
			generateNewIndx(networkfile,
					idxfile);
			HashMap<String, Integer> idx = readIdx(idxfile);
			convertData(idx, networkfile,
					datafile_for_node2vec);
			//example of datafile_for_node2vec: network_input.edgelist
	}
	

	public static HashMap<String, Integer> readIdx(String file) throws IOException {
		HashMap<String, Integer> map = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(new File(file)));
		String line = null;
		while ((line = br.readLine()) != null) {
			String[] elements = line.split(" ");
			map.put(elements[1], Integer.valueOf(elements[0]));
		}
		br.close();
		return map;
	}

	public static void generateNewIndx(String input, String idxFile) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(input)));
		String line = null;
		HashSet<String> nodes = new HashSet<>();
		while ((line = br.readLine()) != null) {
			InputStream inputStream = new ByteArrayInputStream(line.getBytes());
			NxParser nxp = new NxParser();
			nxp.parse(inputStream);

			while (nxp.hasNext()) {
				Node[] quard = nxp.next();
				String s = quard[0].toString().trim();
				String p = quard[1].toString().trim();
				String o = quard[2].toString().trim();

				if (s.startsWith("<http://") & o.startsWith("<http://")) {
					nodes.add(s);
					nodes.add(o);
				}
			}
		}
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(idxFile)));
		int i = 0;
		for (String node : nodes) {
			bw.write(i + " " + node + "\n");
			i++;
		}
		br.close();
		bw.flush();
		bw.close();
	}

}
