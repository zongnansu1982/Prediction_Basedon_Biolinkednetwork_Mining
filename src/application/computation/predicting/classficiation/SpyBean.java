package application.computation.predicting.classficiation;

import java.util.HashMap;

public class SpyBean {

	
	public HashMap<String,String> test_pairs;
	
	public HashMap<String, String> getTest_pairs() {
		return test_pairs;
	}



	public void setTest_pairs(HashMap<String, String> test_pairs) {
		this.test_pairs = test_pairs;
	}



	public HashMap<String,String> remian_positive;
	public HashMap<String, String> getRemian_positive() {
		return remian_positive;
	}



	public void setRemian_positive(HashMap<String, String> remian_positive) {
		this.remian_positive = remian_positive;
	}



	public HashMap<String, String> getSpy_positive() {
		return spy_positive;
	}



	public void setSpy_positive(HashMap<String, String> spy_positive) {
		this.spy_positive = spy_positive;
	}



	public HashMap<String, String> getNegative() {
		return negative;
	}



	public void setNegative(HashMap<String, String> negative) {
		this.negative = negative;
	}



	public HashMap<String,String> spy_positive;
	public HashMap<String,String> negative;
	
	
	

}
