package application.computation.util;

import java.math.BigDecimal;
import java.util.ArrayList;

public class BinaryOperator {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

	public static String Average = "Average";
	public static String Hadamard = "Hadamard";
	public static String Weighted_L1 = "Weighted_L1";
	public static String Weighted_L2 = "Weighted_L2";
	public static String Add = "Add";
	public static ArrayList<Double> operate(ArrayList<Double> vec_1, ArrayList<Double> vec_2, String method) {
		ArrayList<Double> vec = new ArrayList<>();
		
		if (method.equals(Add)) {
			for (int i = 0; i < vec_1.size(); i++) {
				BigDecimal bd=new BigDecimal(vec_1.get(i));
				Double value_1=bd.setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue();
				vec.add(value_1);
			}
			
			for (int i = 0; i < vec_2.size(); i++) {
				BigDecimal bd=new BigDecimal(vec_2.get(i));
				Double value_1=bd.setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue();
				vec.add(value_1);
			}
		}else{
			for (int i = 0; i < vec_1.size(); i++) {
				Double value=0.0;
				
				if (method.equals(Add)) {
					value=(vec_1.get(i)+vec_2.get(i))/2;
				}
				if (method.equals(Average)) {
					value=(vec_1.get(i)+vec_2.get(i))/2;
				}
				if (method.equals(Hadamard)) {
					value=vec_1.get(i)*vec_2.get(i);
				}
				if (method.equals(Weighted_L1)) {
					value=Math.abs(vec_1.get(i)-vec_2.get(i));
				}
				if (method.equals(Weighted_L2)) {
					value=Math.pow((vec_1.get(i)-vec_2.get(i)), 2);
				}
				BigDecimal bd=new BigDecimal(value);
				Double value_1=bd.setScale(3,BigDecimal.ROUND_HALF_UP).doubleValue();
				vec.add(value_1);
			}	
		}
		
		return vec;
	}

}
