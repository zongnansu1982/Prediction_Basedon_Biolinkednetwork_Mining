package application.computation.predicting.negativeSelection;

/**
 * Created by Rafal on 12.01.2017.
 */
public interface AttributeStatistics {

    void calculate();

    double[] getStatistics();
    
    void update();
}
