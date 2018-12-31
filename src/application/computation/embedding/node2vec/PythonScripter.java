package application.computation.embedding.node2vec;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * 
 * @author zongn
 *
 */
public class PythonScripter {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * https://github.com/aditya-grover/node2vec
	 * 
	 * @param node2vec_parameters
	 * @param script_file
	 * @param datafile
	 * @param modelfile
	 * @throws IOException
	 */
	public static void write(String node2vec_parameters, String script_file, String datafile, String modelfile)
			throws IOException {

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(script_file)));
		String[] element = node2vec_parameters.split("_"); // exmaple: "64_40_5_10_2.0_1.0"
		String dimension = element[0];
		String length = element[1];
		String window = element[2];
		String numberOfWalk = element[3];
		String p = element[4];
		String q = element[5];

		// example:String
		// modelfile="node2vec_"+dimension+"_"+length+"_"+window+"_"+numberOfWalk+"_"+p+"_"+q+".emb";

		String string = "python src/main.py" + " --input " + datafile + " --output " + modelfile + " --workers " + 13
				+ " --dimensions " + dimension + " --walk-length " + length + " --num-walks " + numberOfWalk
				+ " --window-size " + window + " --iter " + 1 + " --p " + p + " --q " + q;
		bw.write(string + "\n");
		System.out.println(string);

		bw.flush();
		bw.close();
	}

}
