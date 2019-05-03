package application.computation.predicting.classficiation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import de.bwaldvogel.liblinear.SolverType;
import hr.irb.fastRandomForest.FastRandomForest;
import weka.classifiers.Classifier;

import weka.classifiers.functions.LibLINEAR;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.converters.ConverterUtils.DataSource;

public class Classification {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	public static final String logistic="Logistic";
	public static final String forest="Forest";
	public static final String svm="SVM";
	public static final String j48="J48";
	public static final String xgboost="XGBoost";
	
public static HashMap<String,Double> predictWithModel(String model_file,HashMap<String,String> arff_idx, String testFile) throws Exception{
	
	
	Classifier cls = (Classifier) weka.core.SerializationHelper.read(model_file); 
	
	 HashMap<String,Double>  result=new HashMap<>();
	 
    // load test data, set class
    Instances test = DataSource.read(testFile);
    test.setClassIndex(test.numAttributes() - 1);
    
    // configure classifier
    
    // evaluate classifier
    System.out.println(" model testing ....");
    
    
    for (int i = 0; i < test.numInstances(); i++) {
		Double value=cls.distributionForInstance(test.get(i))[test.attribute(test.numAttributes() - 1).indexOfValue("true")];
		StringBuffer sb =new StringBuffer();
		for (int j = 0; j < test.get(i).numAttributes()-1; j++) {
			sb.append(test.get(i).value(j)).append(" ");
		}
		String pair=arff_idx.get(sb.toString().trim());
		
		if(pair!=null){
			result.put(pair, value);
		}else{
			System.out.println(" NULL value found in test ");
			System.exit(0);
		}
	}
    return result;
}
public static Classifier trainModel(String trainFile, String classifier, String model_file) throws Exception{
	
	Instances train = DataSource.read(trainFile);
    train.setClassIndex(train.numAttributes() - 1);
    
    
    // configure classifier
    System.out.println(" data loaded ....");
    
    HashMap<String,HashMap<String,Double> > result=new HashMap<>();
    
    Classifier cls = null;
    if(classifier.equals(logistic)){
             
    	cls=new LibLINEAR();
	    String[] options=new String[2];
	    options[0]="-S";
	    options[1]="0";
	    ((LibLINEAR) cls).setOptions(options);
    }
    
    
    if(classifier.equals(svm)){
        
    	cls = new LibSVM();
    	((LibSVM) cls).setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
    	((LibSVM) cls).setSVMType(new SelectedTag(LibSVM.SVMTYPE_C_SVC, LibSVM.TAGS_SVMTYPE));
//    	((LibSVM) cls).setWeights(weight+" 1.0");
    }
    
    
    if(classifier.equals(forest)){
        
    	cls=new FastRandomForest();
    	((FastRandomForest) cls).setNumTrees(500);
//    	RandomForest rf=new RandomForest();
//    	rf.setBagSizePercent(100);
//    	rf.setNumIterations(500);
    }
    
    if(classifier.equals(j48)){
        
    	cls = new J48();
    	((J48) cls).setConfidenceFactor((float) 0.25);
    	((J48) cls).setMinNumObj(2);
    }
    
    System.out.println(" model building ....");
    
    // build classifier
    cls.buildClassifier(train);
    
    // evaluate classifier
    System.out.println(" model testing ....");
    
    
    weka.core.SerializationHelper.write(model_file, cls);
    
    return cls;
}

}
