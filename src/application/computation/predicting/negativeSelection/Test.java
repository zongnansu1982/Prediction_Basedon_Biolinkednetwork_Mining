package application.computation.predicting.negativeSelection;

import weka.classifiers.CollectiveEvaluation;

public class Test {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		Instances train = DataSource.read("D:/data/drug-taget-network/Databases/data/weka/diabetes.arff");
	    train.setClassIndex(train.numAttributes() - 1);
	   
	    System.out.println(train.classAttribute().indexOfValue("true"));
	    System.out.println(train.classAttribute().indexOfValue("false"));
	    
	    for (int i = 0; i < 10; i++) {
		System.out.println(train.get(i)+" --> "+train.get(i).classValue()+" "+train.classAttribute().indexOfValue("true"));	
		train.get(i).setClassValue(train.classAttribute().indexOfValue("true"));
		System.err.println(train.get(i)+" --> "+train.get(i).classValue()+" "+train.classAttribute().indexOfValue("true"));	
		}
	    
	   
	    System.exit(0);
	    
	    // load unlabeled data, set class
	    Instances test = DataSource.read("D:/data/drug-taget-network/Databases/data/weka/diabetes.arff");
	    test.setClassIndex(test.numAttributes() - 1);
		
		NaiveBayes_local nb=new NaiveBayes_local();
		nb.setDoNotCheckCapabilities(true);
		nb.buildClassifier(train);
		
		Evaluation eval = new Evaluation(train);
		eval.evaluateModel(nb, test);
		
		
		
		
		System.out.println("auc roc: "+eval.areaUnderROC(0)+" true recall: "+eval.recall(0)+" false precision: "+eval.precision(1));
		
		
		nb.updateClassifier(train);
		
		eval = new Evaluation(train);
		eval.evaluateModel(nb, test);
		System.out.println("auc roc: "+eval.areaUnderROC(0)+" true recall: "+eval.recall(0)+" false precision: "+eval.precision(1));
		
		
		
		
		
		
//		for (int i = 0; i < test.size(); i++) {
////			Double value=nb.classifyInstance(test.get(i));
//			double[] values=nb.distributionForInstance(test.get(i));
//			System.out.println(i+" -> "+values[0]+" "+values[1]);
//		}
	
	
	}

}
