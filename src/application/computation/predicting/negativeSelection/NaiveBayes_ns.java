package application.computation.predicting.negativeSelection;


import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.evaluation.Evaluation;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class NaiveBayes_ns {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Instances train = DataSource.read("D:/data/drug-taget-network/Databases/data/weka/diabetes.arff");
	    train.setClassIndex(train.numAttributes() - 1);
	    
	    // load unlabeled data, set class
	    Instances test = DataSource.read("D:/data/drug-taget-network/Databases/data/weka/diabetes.arff");
	    test.setClassIndex(test.numAttributes() - 1);
		
	    NaiveBayes_ns nb=new NaiveBayes_ns(train);
	    
		Evaluation eval = new Evaluation(train);
		eval.evaluateModel(nb.getClassifer(), test);
		
		System.out.println("auc roc: "+eval.areaUnderROC(0)+" true recall: "+eval.recall(0)+" false precision: "+eval.precision(1));
		
		nb.build();
		
		eval = new Evaluation(train);
		eval.evaluateModel(nb.getClassifer(), test);
		System.out.println("auc roc: "+eval.areaUnderROC(0)+" true recall: "+eval.recall(0)+" false precision: "+eval.precision(1));
		
	}
	
	public NaiveBayes nb;
	
	public Double[] posteriors;
	
	
	Instances orignialData;
	
	public final Double converage_differnce=0.1;
	
	public boolean loop=true;
	
	public NaiveBayes_ns(Instances orignialData) throws Exception{
		this.posteriors=new Double[orignialData.size()];
		this.posteriors=new Double[orignialData.size()];
			for (int i = 0; i < orignialData.size(); i++) {
				posteriors[i]=0.0;
			}
		
		this.orignialData=orignialData;
		updateClassifier(orignialData);
		this.loop=true;
	}
	
public NaiveBayes getClassifer(){
	return this.nb;
}
	
public Instances updateTraining() throws Exception{
		
		Instances data=new Instances(orignialData.stringFreeStructure());
		data.setClass(orignialData.classAttribute());
		data.setClassIndex(orignialData.classIndex());
		data.setRelationName(orignialData.relationName());		
		
		Double difference=0.0;
		
		for (int i = 0; i < orignialData.size(); i++) {
			
				double[] values=nb.distributionForInstance(orignialData.get(i));
				
				if(values!=null){
					if(!Double.isNaN(values[data.classAttribute().indexOfValue("true")])){
						difference+=(values[data.classAttribute().indexOfValue("true")]-posteriors[i]);
						posteriors[i]=values[data.classAttribute().indexOfValue("true")];		
					}
				}
				
				Instance old_ins=orignialData.get(i);
				Instance new_ins=new DenseInstance(old_ins.numAttributes());
				
				for (int j = 0; j < old_ins.numAttributes(); j++) {
					new_ins.setValue(j, old_ins.value(j));
				}
				
				if(old_ins.classValue()==orignialData.classAttribute().indexOfValue("false")){
					if(nb.classifyInstance(old_ins)==data.classAttribute().indexOfValue("true")){
						new_ins.setValue(new_ins.numAttributes()-1,data.classAttribute().indexOfValue("true"));
					}else{
						new_ins.setValue(new_ins.numAttributes()-1,data.classAttribute().indexOfValue("false"));
					}
				}
				data.add(i,new_ins);
			}	
			
			if(difference<this.converage_differnce){
				loop=false;
			}
			System.out.println(difference);
		return data;
	}

	public void build() throws Exception {
	
	int i=0;
	// this is new data
	while(loop&&i<100){
		i++;
		System.out.println("iteration: "+i);
		Instances new_data=updateTraining();
		updateClassifier(new_data);	
	}
	
  }

	public void updateClassifier(Instances data) throws Exception{
		this.nb=new NaiveBayes();
		this.nb.setDoNotCheckCapabilities(true);
		this.nb.buildClassifier(data);
	}

}
