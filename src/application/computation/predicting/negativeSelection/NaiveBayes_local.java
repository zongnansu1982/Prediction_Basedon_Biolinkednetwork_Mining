package application.computation.predicting.negativeSelection;

public class NaiveBayes_local extends AbstractClassifier implements Classifier {
	
	private Double[] posteriors;
	private boolean loop=true;
	private final Double converage_differnce =0.01;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7550409893545527343L;

	/** number of classes */
	protected int numClasses;

	/** counts, means, standard deviations, priors..... */
	//protected double[.....

	public NaiveBayes_local() {
		// TODO Auto-generated constructor stub
	}

	private AttributeStatistics attributeStatistics[][];
	private double[] classesStatistics;

	
	public Instances updateTraining(Instances orignialData) throws Exception{
		
		
		if(this.posteriors==null){
			posteriors=new Double[orignialData.size()];
			for (int i = 0; i < orignialData.size(); i++) {
				posteriors[i]=0.0;
			}
		}
		
		Instances data=new Instances(orignialData.stringFreeStructure());
		data.setClass(orignialData.classAttribute());
		data.setClassIndex(orignialData.classIndex());
		data.setRelationName(orignialData.relationName());		
		
		Double difference=0.0;
		
		for (int i = 0; i < orignialData.size(); i++) {
			
				double[] values=distributionForInstance(orignialData.get(i));
				
				
				
				if(values!=null){
					difference+=(values[data.classAttribute().indexOfValue("true")]-posteriors[i]);
					
					Double value=(values[data.classAttribute().indexOfValue("true")]-posteriors[i]);
				
					if(Double.isNaN(difference)||Double.isNaN(values[data.classAttribute().indexOfValue("true")])
							||Double.isNaN(posteriors[i])){
						
						System.out.println(orignialData.get(i));	
						
						System.out.println(values[0]+" "+values[1]);
						
						System.out.println(values[data.classAttribute().indexOfValue("true")]+" - "+posteriors[i]+" = "+value+" -> "+difference);	
						System.exit(0);
					}
					
					posteriors[i]=values[data.classAttribute().indexOfValue("true")];	
				}
				
				Instance old_ins=orignialData.get(i);
				Instance new_ins=new DenseInstance(old_ins.numAttributes());
				
				for (int j = 0; j < old_ins.numAttributes(); j++) {
					new_ins.setValue(j, old_ins.value(j));
				}
				
				if(old_ins.classValue()==orignialData.classAttribute().indexOfValue("false")){
					if(classifyInstance(old_ins)==data.classAttribute().indexOfValue("true")){
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
	
public Instances updateTraining(Instances orignialData, int itr) throws Exception{
		
		
		if(this.posteriors==null){
			posteriors=new Double[orignialData.size()];
			for (int i = 0; i < orignialData.size(); i++) {
				posteriors[i]=0.0;
			}
		}
		
		Instances data=new Instances(orignialData.stringFreeStructure());
		data.setClass(orignialData.classAttribute());
		data.setClassIndex(orignialData.classIndex());
		data.setRelationName(orignialData.relationName());		
		
		Double difference=0.0;
		
		for (int i = 0; i < orignialData.size(); i++) {
			
				double[] values=distributionForInstance(orignialData.get(i));
				
				
				if(values!=null){
					difference+=(values[data.classAttribute().indexOfValue("true")]-posteriors[i]);
					
					Double value=(values[data.classAttribute().indexOfValue("true")]-posteriors[i]);
					
					if(itr>6){
						System.out.println(values[data.classAttribute().indexOfValue("true")]+" - "+posteriors[i]+" = "+value+" -> "+difference);						
					}
					
					posteriors[i]=values[data.classAttribute().indexOfValue("true")];	
				}
				
				Instance old_ins=orignialData.get(i);
				Instance new_ins=new DenseInstance(old_ins.numAttributes());
				
				for (int j = 0; j < old_ins.numAttributes(); j++) {
					new_ins.setValue(j, old_ins.value(j));
				}
				
				if(old_ins.classValue()==orignialData.classAttribute().indexOfValue("false")){
					if(classifyInstance(old_ins)==data.classAttribute().indexOfValue("true")){
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
	
	public void updateClassifier(Instances orignialData) throws Exception {
		int i=0;
		// this is new data
		while(loop&&i<100){
			i++;
			System.out.println("iteration: "+i);
			Instances new_data=updateTraining(orignialData);
			updateDataStatistics(new_data);	
		}
		
	  }

	public void findNegatives(){
		
	}
	
	
	
	@Override
	public void buildClassifier(Instances data) throws Exception {
		
		numClasses = data.numClasses();
		
		// remove instances with missing class
		data.deleteWithMissingClass();

		initializeDataStatistics(data);

		calculateDataStatistics(data);

	}
	
	
	 private void updateDataStatistics(Instances data)
	    {
		 	updateAttributesStatistics(data); // P(x|y)
			updateClassesStatistics(data); // P(y)
	    }
	 
	 
	 
    private void initializeDataStatistics(Instances data)
    {
        int numClasses = data.numClasses();
        int numAttributes = data.numAttributes();

        attributeStatistics = new AttributeStatistics[numClasses][numAttributes];
        classesStatistics = new double[numClasses];
    }

	private void calculateDataStatistics(Instances data) {
		calculateAttributesStatistics(data); // P(x|y)
		calculateClassesStatistics(data); // P(y)
	}
	
	private void updateClassesStatistics(Instances data) {
		AttributeStatistics classesStats = new ClassStatistics(data.classAttribute(), data);
		classesStats.calculate();
		classesStatistics = classesStats.getStatistics();
	}

	private void calculateClassesStatistics(Instances data) {

		AttributeStatistics classesStats = new ClassStatistics(data.classAttribute(), data);
		classesStats.calculate();
		classesStatistics = classesStats.getStatistics();
	}

	
	private void updateAttributesStatistics(Instances data) {
		for(int attributeIndex = 0; attributeIndex < data.numAttributes() - 1; attributeIndex++) {

			Attribute attribute = data.attribute(attributeIndex);

			for(int classIndex = 0; classIndex < data.numClasses(); classIndex++) {
				if(!attribute.isNominal())
				{
                    attributeStatistics[classIndex][attributeIndex] = new NumericAttributeStatistics(classIndex, attribute, data);
				}
			}
		}
	}
	
	
	private void calculateAttributesStatistics(Instances data) {
		for(int attributeIndex = 0; attributeIndex < data.numAttributes() - 1; attributeIndex++) {

			Attribute attribute = data.attribute(attributeIndex);

			for(int classIndex = 0; classIndex < data.numClasses(); classIndex++) {
				if(attribute.isNominal())
				{
                    attributeStatistics[classIndex][attributeIndex] = new NominalAttributeStatistic(classIndex, attribute, data);
                } else {
                    attributeStatistics[classIndex][attributeIndex] = new NumericAttributeStatistics(classIndex, attribute, data);
				}
			}
		}
	}

	private double calculateNormalizationFactorForInstance(Instance instance)
	{
		//P(X)
		double normalizationFactor = 0;
		for (int classValue = 0 ; classValue < numClasses ; classValue++){
			normalizationFactor+= calculatePrioriProbabilityForInstance(instance, classValue)*classesStatistics[classValue];
		}
		return normalizationFactor;
	}
	
	
	private double calculateNormalizationFactorForInstance(Instance instance,double[] posteriors)
	{
		//P(X)
		double normalizationFactor = 0;
		for (int classValue = 0 ; classValue < numClasses ; classValue++){
			normalizationFactor+= calculatePrioriProbabilityForInstance(instance, classValue)*classesStatistics[classValue];
		}
		return normalizationFactor;
	}
	
	private double calculatePrioriProbabilityForInstance(Instance instance, int classValue){
		double probability = 1;
		for (int attributeIndex = 0; attributeIndex< instance.numAttributes()-1; attributeIndex++){
			double attributeValue = instance.toDoubleArray()[attributeIndex];

			if(instance.attribute(attributeIndex).isNominal())
				probability *= getNominalAttributeProbability(classValue, attributeIndex, (int) attributeValue);
			else {
				probability *= getNumericAttributeProbability(classValue, attributeIndex, attributeValue);
			}
		}
		return probability;
	}




	private double getNumericAttributeProbability(int classValue, int attributeIndex, double attributeValue) {
		double[] stats = attributeStatistics[classValue][attributeIndex].getStatistics();
		double mean = stats[0];
		double std = stats[1];
		return normalDistributionProbabilityDensityFunction(attributeValue, mean, std);
	}

	private double normalDistributionProbabilityDensityFunction(double value, double mean, double std)
	{
		double factor = 1/(std * Math.sqrt(Math.PI));
		double expArgument = (-Math.pow((value-mean), 2))/(2*Math.pow(std,2));
		return factor * Math.exp(expArgument);
	}

	private double getNominalAttributeProbability(int classValue, int attributeIndex, int attributeValue) {
		return attributeStatistics[classValue][attributeIndex].getStatistics()[attributeValue];
	}


	@Override
	public double[] distributionForInstance(Instance instance) throws Exception {
		double[] distribution = new double[numClasses];
		
			double normalizationFactor = calculateNormalizationFactorForInstance(instance);

			for (int classValue = 0; classValue< numClasses ; classValue++) {

				double aPriori = calculatePrioriProbabilityForInstance(instance, classValue);
				double classProbability = classesStatistics[classValue];
				distribution[classValue] = (aPriori*classProbability)/normalizationFactor;
			}
			// Remember to normalize probabilities!
				
		return distribution;    
	}
	
	@Override
	public double classifyInstance(Instance instance) throws Exception {
		double classValue = 0.0;
		double max = Double.MIN_VALUE;
		double[] dist = distributionForInstance(instance);
		
		for(int i = 0; i < dist.length; i++) {
			if(dist[i] > max) {
				classValue = i;
				max = dist[i];
			}
		}
		
		return classValue;
	}

	@Override
	public Capabilities getCapabilities() {
		// TODO Auto-generated method stub
		Capabilities result = super.getCapabilities();
	    result.disableAll();

	    // attributes
	    result.enable(Capability.NOMINAL_ATTRIBUTES);
	    result.enable(Capability.NUMERIC_ATTRIBUTES);
	    result.enable( Capability.MISSING_VALUES );

	    // class
	    result.enable(Capability.NOMINAL_CLASS);
	    result.enable(Capability.MISSING_CLASS_VALUES);

	    // instances
	    result.setMinimumNumberInstances(0);

	    return result;
	}

}