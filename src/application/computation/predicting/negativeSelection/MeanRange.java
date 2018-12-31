package application.computation.predicting.negativeSelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MeanRange {
	public static int findCrossOver(Double arr[], int low, int high, Double x)
    {
        // Base cases
        if (arr[high] <= x) // x is greater than all
            return high;
        if (arr[low] > x)  // x is smaller than all
            return low;
 
        // Find the middle point
        int mid = (low + high)/2;  /* low + (high - low)/2 */
 
        /* If x is same as middle element, then return mid */
        if (arr[mid] <= x && arr[mid+1] > x)
            return mid;
 
        /* If x is greater than arr[mid], then either arr[mid + 1]
          is ceiling of x or ceiling lies in arr[mid+1...high] */
        if(arr[mid] < x)
            return findCrossOver(arr, mid+1, high, x);
 
        return findCrossOver(arr, low, mid - 1, x);
    }
 
    // This function prints k closest elements to x in arr[].
    // n is the number of elements in arr[]
 public  static  ArrayList<Integer> findKclosest(Double arr[], Double x, int k, int n)
    {
	 
	 	ArrayList<Integer> index=new ArrayList<>();
        // Find the crossover point
        int l = findCrossOver(arr, 0, n-1, x); 
        int r = l+1;   // Right index to search
        int count = 0; // To keep track of count of elements
                       // already printed
 
        // If x is present in arr[], then reduce left index
        // Assumption: all elements in arr[] are distinct
        if (arr[l] == x) l--;
 
        // Compare elements on left and right of crossover
        // point to find the k closest elements
        while (l >= 0 && r < n && count < k)
        {
            if (x - arr[l] < arr[r] - x){
            	index.add(l);
            	l--;
//            	System.out.print(arr[l--]+" ");
            }
            else{
            	index.add(r);
            	r++;
//                System.out.print(arr[r++]+" ");
            }
            count++;
        }
 
        // If there are no more elements on right side, then
        // print left elements
        while (count < k && l >= 0)
        {
        	index.add(l);
        	l--;
//            System.out.print(arr[l--]+" ");
            count++;
        }
 
 
        // If there are no more elements on left side, then
        // print right elements
        while (count < k && r < n)
        {
        	index.add(r);
        	r++;
//            System.out.print(arr[r++]+" ");
            count++;
        }
        return index;
    }
    
 
 public static HashSet<String> findValuesInMeanRange(List<Map.Entry<String, Double>> local_list, int size){
	 
	 Double arr[]=new Double[local_list.size()];
	 Double mean=0.0;
	 for (int i = 0; i < local_list.size(); i++) {
		 arr[i]=local_list.get(i).getValue();
		 mean+=arr[i];
	}
	 mean=mean/local_list.size();
	 ArrayList<Integer> index=findKclosest(arr, mean, size, local_list.size());
//	 System.out.println("mean: "+mean);
	 HashSet<String> set=new HashSet<>();
	 for (int i = 0; i < index.size(); i++) {
//		 System.out.println(index.get(i)+"-> "+arr[index.get(i)]+" key: "+local_list.get(index.get(i)).getKey());
		 set.add(local_list.get(index.get(i)).getKey());
	}
	 return set;
 }
 
 
public static HashSet<String> findValuesInMedianRange(List<Map.Entry<String, Double>> local_list, int size){
	 HashSet<String> set=new HashSet<>();
	 Double arr[]=new Double[local_list.size()];
	 for (int i = 0; i < local_list.size(); i++) {
		 arr[i]=local_list.get(i).getValue();
	}
	 if(local_list.size()%2==0){
		 int idx_1=local_list.size()/2;
		 int idx_2=idx_1-1;
		 Double mean=(local_list.get(idx_1).getValue()+local_list.get(idx_2).getValue())/2;
		 
		 
		 if(size%2==0){
			 
			 int number_p=(size-2)/2;
			 int number_n=(size-2)/2;
			 
			 while(number_p>0){
				 set.add(local_list.get(idx_1+number_p).getKey());
				 number_p--;
			 }
			 while(number_n>0){
				 set.add(local_list.get(idx_2-number_n).getKey());
				 number_n--;
			 }

		 }else{
			 int number_p=(size-3)/2;
			 int number_n=(size-3)/2;
			 int number=(size-3)/2;
			 while(number_p>0){
				 set.add(local_list.get(idx_1+number_p).getKey());
				 number_p--;
			 }
			 while(number_n>0){
				 set.add(local_list.get(idx_2-number_n).getKey());
				 number_n--;
			 }
			 Double up_value=local_list.get(idx_1+number+1).getValue();
			 Double down_value=local_list.get(idx_2-number-1).getValue();
			 if((up_value-mean)<=(mean-down_value)){
				 set.add(local_list.get(idx_1+number+1).getKey());
			 }else{
				 set.add(local_list.get(idx_2-number-1).getKey());
			 }			
			 
		 }
		 
		 
	 }else{
		 int  idx=(local_list.size()-1)/2;
		 set.add(local_list.get(idx).getKey());
		 
		 if(size%2==0){
			 int number_p=(size-2)/2;
			 int number_n=(size-2)/2;
			 int number=(size-2)/2;
			 while(number_p>0){
				 set.add(local_list.get(idx+number_p).getKey());
				 number_p--;
			 }
			 while(number_n>0){
				 set.add(local_list.get(idx-number_n).getKey());
				 number_n--;
			 }
			 Double m_value=local_list.get(idx).getValue();
			 Double up_value=local_list.get(idx+number+1).getValue();
			 Double down_value=local_list.get(idx-number-1).getValue();
			 if((up_value-m_value)<=(m_value-down_value)){
				 set.add(local_list.get(idx+number+1).getKey());
			 }else{
				 set.add(local_list.get(idx-number-1).getKey());
			 }
		 }else{
			 
			 int number_p=(size-1)/2;
			 int number_n=(size-1)/2;
			 
			 while(number_p>0){
				 set.add(local_list.get(idx+number_p).getKey());
				 number_p--;
			 }
			 while(number_n>0){
				 set.add(local_list.get(idx-number_n).getKey());
				 number_n--;
			 }
		 }
	 }
	 return set;
 }

 
public static void main(String[] args) throws Exception {
	// TODO Auto-generated method stub
	  
	 
	    /* Driver program to check above functions */
	        Double arr[] = {12.0, 16.0, 22.0, 30.0, 35.0, 39.0, 42.0,
	        		43.1, 45.0,45.7, 47.4, 48.0, 50.0, 53.0, 55.0
	                    };
//	        int n = arr.length;
//	        Double x = 45.0; 
//	        int k = 4;
//	        ArrayList<Integer> index=findKclosest(arr, x, k, n);
//	        for (int i = 0; i < index.size(); i++) {
//				System.out.println(index.get(i)+"-> "+arr[index.get(i)]);
//			}
	
	HashMap<String,Double> map=new HashMap<>();
	for (int i = 0; i < arr.length; i++) {
		map.put(arr[i].toString(), arr[i]);
	}
	
	// map转换成list进行排序
    List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String,Double>>(map.entrySet());

    // 排序
    Collections.sort(list,new Comparator<Map.Entry<String,Double>>() {
        @Override
        public int compare(Entry<String, Double> o1,
                Entry<String, Double> o2) {
            // TODO Auto-generated method stub
            return o1.getValue().compareTo(o2.getValue()); // 从小到大
//        	return o2.getValue().compareTo(o1.getValue()); // 从大到小
        }
    });
    System.out.println(list.size());
//  findValuesInMeanRange(list, 5);   
    System.out.println( findValuesInMedianRange(list, 7));
}
}
