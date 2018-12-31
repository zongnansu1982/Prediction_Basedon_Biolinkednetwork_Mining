package application.computation.util;

import java.util.HashMap;
import java.util.HashSet;

public class BSIBean {
	HashMap<String, Double> dbsi;
	public HashMap<String, Double> getDbsi() {
		return dbsi;
	}
	public void setDbsi( HashMap<String, Double> dbsi) {
		this.dbsi = dbsi;
	}
	public HashMap<String, Double> getTbsi() {
		return tbsi;
	}
	public void setTbsi(HashMap<String, Double> tbsi) {
		this.tbsi = tbsi;
	}
	public  HashMap<String, Double> getHybrid() {
		return hybrid;
	}
	public void setHybrid(HashMap<String, Double> hybrid) {
		this.hybrid = hybrid;
	}
	HashMap<String, Double> tbsi;
	HashMap<String, Double> hybrid;
	
	HashSet<String> test_negative;
	public HashSet<String> getTest_negative() {
		return test_negative;
	}
	public void setTest_negative(HashSet<String> test_negative) {
		this.test_negative = test_negative;
	}
}
