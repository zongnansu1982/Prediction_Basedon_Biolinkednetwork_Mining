package application.computation.predicting.negativeSelection;

import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Created by Rafal on 12.01.2017.
 */
public class NominalAttributeStatistic implements AttributeStatistics {

    private double classValue;
    private Attribute attribute;
    private Instances instances;

    private double[] statistics;

    public NominalAttributeStatistic(int classValue, Attribute attribute, Instances instances){
        this.classValue = classValue;

        this.attribute = attribute;
        this.instances = instances;
    }
    
    
    @Override
	public void update(){
    	AttributeStats attributeStats = instances.attributeStats(attribute.index());
        int allValuesCount = attributeStats.totalCount;
        int distinctValuesCount = attributeStats.distinctCount;

        double[] attributeValues = instances.attributeToDoubleArray(attribute.index());
        double[] classValues = instances.attributeToDoubleArray(instances.classIndex());

        double classValueCount = Arrays.stream(classValues)
                .filter(value -> value == classValue)
                .count();

        double[] distinctValues = Arrays.stream(attributeValues)
                .distinct()
                .sorted()
                .toArray();

        statistics = new double[distinctValuesCount];

        for (int i = 0; i< distinctValuesCount ; i++){
            int finalI = i;
            statistics[i] = IntStream.range(0, classValues.length)
                    .filter(index -> classValues[index] == classValue)
                    .filter(index -> attributeValues[index] == distinctValues[finalI])
                    .count() / classValueCount;
        }
    }
    
    
    
    @Override
	public void calculate(){
        AttributeStats attributeStats = instances.attributeStats(attribute.index());
        int allValuesCount = attributeStats.totalCount;
        int distinctValuesCount = attributeStats.distinctCount;

        double[] attributeValues = instances.attributeToDoubleArray(attribute.index());
        double[] classValues = instances.attributeToDoubleArray(instances.classIndex());

        double classValueCount = Arrays.stream(classValues)
                .filter(value -> value == classValue)
                .count();

        double[] distinctValues = Arrays.stream(attributeValues)
                .distinct()
                .sorted()
                .toArray();

        statistics = new double[distinctValuesCount];

        for (int i = 0; i< distinctValuesCount ; i++){
            int finalI = i;
            statistics[i] = IntStream.range(0, classValues.length)
                    .filter(index -> classValues[index] == classValue)
                    .filter(index -> attributeValues[index] == distinctValues[finalI])
                    .count() / classValueCount;
        }
    }


    @Override
	public double[] getStatistics()
    {
        if(statistics == null)
            calculate();
        return statistics;
    }
}
