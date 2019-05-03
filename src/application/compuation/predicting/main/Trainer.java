package application.compuation.predicting.main;

import java.io.File;

import application.computation.predicting.classficiation.Classification;
import application.computation.predicting.classficiation.WekaDataGenerator_negativeSelection_case12;
import application.computation.predicting.negativeSelection.PU_ns;
import application.computation.util.BinaryOperator;

public class Trainer {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	String classifier=Classification.svm;
	Integer trainSpace_type= 4;
	String stardard_type = "fs";
	String pu_type = PU_ns.nb;
	String method=BinaryOperator.Hadamard;
	Double spyRate = 0.1;
	Double space=1.0;
	Integer dimension=64;
	Integer valid_interaction=-1;
	String thread_method="z_5";
	Double BSI_rate = 0.5;
	Double BSI_PU_rate = 0.5;
	
	String embeddingfile="model/entire_training_64_40_5_10_2.0_1.0_tab.emb";
	String idxfile="data/network.idx";
	String networkfile="data/network_bi.nt";
	
	String arff_trainfile="model/arff_train.arff";
	String arff_testfile="model/arff_test.arff";
	String classification_model_file="model/classification.model";
	
	
	public Trainer (String networkfile, String idxfile, String embeddingfile) {
		this.embeddingfile=embeddingfile;
		this.idxfile=idxfile;
		this.networkfile=networkfile;
	}
	
	public void trainModel() throws Exception {
		new File(new java.io.File( "." ).getCanonicalPath() + "/tmp/").mkdirs();
		WekaDataGenerator_negativeSelection_case12 g_1 = new WekaDataGenerator_negativeSelection_case12(
				trainSpace_type,
				new java.io.File( "." ).getCanonicalPath() + "/tmp/tain_tmp.arff",
				new java.io.File( "." ).getCanonicalPath()  + "/tmp/test_tmp.arff",
				networkfile, dimension, method, embeddingfile, idxfile, pu_type, space, spyRate,
				thread_method, valid_interaction);

		WekaDataGenerator_negativeSelection_case12.writeARFF_train(dimension, method, networkfile, idxfile,
				embeddingfile, arff_trainfile, g_1.getTrain_negatives()) ;
		
		Classification.trainModel(arff_trainfile, classifier,classification_model_file);
	
	}
}
