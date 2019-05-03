package application.computation.predicting.negativeSelection;

import hr.irb.fastRandomForest.FastRandomForest;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.collective.meta.YATSI;
import weka.classifiers.functions.LibLINEAR;
import weka.classifiers.functions.LibSVM;
import weka.classifiers.trees.J48;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;

public class PU_ns {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	
	public static final String llgc = "LLGC";
	public static final String yatsi = "YATSI";
	public static final String logistic = "Logistic";
	public static final String forest = "Forest";
	public static final String svm = "SVM";
	public static final String j48 = "J48";
	public static final String nb="NaiveBayes";
	
	public Classifier classifier;
	public String classifier_type;
	public Double[] posteriors;
	
	Instances orignialData;
	Instances unlabelData;
	
	public final Double converage_differnce=0.1;
	
	public boolean loop=true;
	
	public PU_ns(Instances orignialData) throws Exception{
		this.posteriors=new Double[orignialData.size()];
		this.posteriors=new Double[orignialData.size()];
			for (int i = 0; i < orignialData.size(); i++) {
				posteriors[i]=0.0;
			}
		this.classifier_type=nb;
		this.orignialData=orignialData;
		updateClassifier(orignialData,this.classifier_type);
		this.loop=true;
	}
	public PU_ns(Instances orignialData,Instances unlableData) throws Exception{
		this.posteriors=new Double[orignialData.size()];
		this.posteriors=new Double[orignialData.size()];
			for (int i = 0; i < orignialData.size(); i++) {
				posteriors[i]=0.0;
			}
		this.classifier_type=nb;
		this.unlabelData=unlableData;
		this.orignialData=orignialData;
		updateClassifier(orignialData,this.classifier_type);
		this.loop=true;
	}
	
	public PU_ns(Instances orignialData, String classifier_name) throws Exception{
		
		this.posteriors=new Double[orignialData.size()];
		this.posteriors=new Double[orignialData.size()];
			for (int i = 0; i < orignialData.size(); i++) {
				posteriors[i]=0.0;
			}
		this.classifier_type=classifier_name;
		this.orignialData=orignialData;
		updateClassifier(orignialData,this.classifier_type);
		this.loop=true;
	}
	
	public PU_ns(Instances orignialData, Instances unlableData, String classifier_name) throws Exception{
		
		this.posteriors=new Double[orignialData.size()];
		this.posteriors=new Double[orignialData.size()];
			for (int i = 0; i < orignialData.size(); i++) {
				posteriors[i]=0.0;
			}
		this.classifier_type=classifier_name;
		this.orignialData=orignialData;
		this.unlabelData=unlableData;
		updateClassifier(orignialData,this.classifier_type);
		this.loop=true;
	}
	
	
public Classifier getClassifer(){
	return this.classifier;
}
	
public Instances updateTraining() throws Exception{
		
		Instances data=new Instances(orignialData.stringFreeStructure());
		data.setClass(orignialData.classAttribute());
		data.setClassIndex(orignialData.classIndex());
		data.setRelationName(orignialData.relationName());		
		
		Double difference=0.0;
		
		for (int i = 0; i < orignialData.size(); i++) {
			
				double[] values=classifier.distributionForInstance(orignialData.get(i));
				
				if(values!=null){
					if(!Double.isNaN(values[data.classAttribute().indexOfValue("true")])){
						difference+=Math.abs((values[data.classAttribute().indexOfValue("true")]-posteriors[i]));
						posteriors[i]=values[data.classAttribute().indexOfValue("true")];		
					}
				}
				
				Instance old_ins=orignialData.get(i);
				Instance new_ins=new DenseInstance(old_ins.numAttributes());
				
				for (int j = 0; j < old_ins.numAttributes(); j++) {
					new_ins.setValue(j, old_ins.value(j));
				}
				
				if(old_ins.classValue()==orignialData.classAttribute().indexOfValue("false")){
					if(classifier.classifyInstance(old_ins)==data.classAttribute().indexOfValue("true")){
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
		updateClassifier(new_data,this.classifier_type);	
	}
	
  }
	
	

	public void updateClassifier(Instances data, String classifier_name) throws Exception{
		
		if(this.unlabelData!=null){
			
			this.classifier = new YATSI();
			
			Classifier s_classiifer = null;
			if(classifier_name.equals(forest)){
				 s_classiifer=new FastRandomForest();
				((FastRandomForest) s_classiifer).setNumTrees(500);
			}
			
			if(classifier_name.equals(j48)){
				s_classiifer = new J48();
				((J48) s_classiifer).setConfidenceFactor((float) 0.25);
				((J48) s_classiifer).setMinNumObj(2);
			}

			if (classifier_name.equals(logistic)) {
				s_classiifer = new LibLINEAR();
				String[] options = new String[2];
				options[0] = "-S";
				options[1] = "0";
				((LibLINEAR) s_classiifer).setOptions(options);
			}
			if (classifier_name.equals(svm)) {
				s_classiifer= new LibSVM();
				((LibSVM) s_classiifer).setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
				((LibSVM) s_classiifer).setSVMType(new SelectedTag(LibSVM.SVMTYPE_C_SVC, LibSVM.TAGS_SVMTYPE));
			}
			
			if (classifier_name.equals(nb)) {
				s_classiifer = new NaiveBayes();
			}	
			
			((YATSI) this.classifier).setClassifier(s_classiifer);
			((YATSI) this.classifier).setKNN(100);
			((YATSI) this.classifier).setNoWeights(true);

			((YATSI)this.classifier).buildClassifier(data, this.unlabelData);
		}else{
			if(classifier_name.equals(forest)){
				this.classifier=new FastRandomForest();
				((FastRandomForest) this.classifier).setNumTrees(500);
			}
			
			if(classifier_name.equals(j48)){
				this.classifier = new J48();
				((J48) this.classifier).setConfidenceFactor((float) 0.25);
				((J48) this.classifier).setMinNumObj(2);
			}

			if (classifier_name.equals(logistic)) {
				this.classifier = new LibLINEAR();
				String[] options = new String[2];
				options[0] = "-S";
				options[1] = "0";
				((LibLINEAR) this.classifier).setOptions(options);
			}
			if (classifier_name.equals(svm)) {
				this.classifier = new LibSVM();
				((LibSVM) this.classifier).setKernelType(new SelectedTag(LibSVM.KERNELTYPE_RBF, LibSVM.TAGS_KERNELTYPE));
				((LibSVM) this.classifier).setSVMType(new SelectedTag(LibSVM.SVMTYPE_C_SVC, LibSVM.TAGS_SVMTYPE));
			}
			if (classifier_name.equals(nb)) {
				this.classifier = new NaiveBayes();
			}
			this.classifier.buildClassifier(data);
		}
		
		
	}

}
