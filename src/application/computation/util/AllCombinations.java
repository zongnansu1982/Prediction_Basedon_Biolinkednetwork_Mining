package application.computation.util;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class AllCombinations {
	public static void main(String[] args){
		Set<String> set = new HashSet<String> ();
		set.add("a");
		set.add("b");
		set.add("c");
		set.add("d");
		HashMap<Integer,Set<Set<String>>> subset = getSubset(set);
		for(Entry<Integer,Set<Set<String>>> entry:subset.entrySet())
		{
			System.out.println(entry.getKey()+" "+entry.getValue());
		}
	}
	
	public static String[] getBinaryValue(Set<String> set)
	{
		int size = set.size();
		int m = (int)Math.pow(2,size) - 1;
		String[] result = new String[m+1];
     		for(int i=m;i>-1;i--)
		{
			StringBuffer sb = new StringBuffer(Integer.toBinaryString(i));
			int length = sb.length();
                        if(length < size)
			{
				for(int j=0;j<size-length;j++)
				{
					sb.insert(0, "0");
				}
			}
			result[i] = sb.toString();
		}
		return result;
	}


	public static Set<Set<String>> powerSet(Set<String> originalSet) {
        Set<Set<String>> sets = new HashSet<Set<String>>();
        if (originalSet.isEmpty()) {
            sets.add(new HashSet<String>());
            return sets;
        }
        List<String> list = new ArrayList<String>(originalSet);
        String head = list.get(0);
        Set<String> rest = new HashSet<String>(list.subList(1, list.size()));
        for (Set<String> set : powerSet(rest)) {
            Set<String> newSet = new HashSet<String>();
            newSet.add(head);
            newSet.addAll(set);
            sets.add(newSet);
            sets.add(set);
        }
        return sets;
    }
	
	public static HashMap<Integer, Set<Set<String>>> getSubset(Set<String> originalSet)
	{	
		Set<Set<String>> set=powerSet(originalSet);
		
		HashMap<Integer, Set<Set<String>>> map=new HashMap<>();
		
		for (Set<String> subset:set) {
		int size=subset.size();
		
		if(map.containsKey(size)){
			Set<String> set2=new HashSet<String>();
			for(String string:subset){
				set2.add(string);
			}
			map.get(size).add(set2);
		}else{
			Set<Set<String>> s=new HashSet<>();
			Set<String> set2=new HashSet<String>();
			for(String string:subset){
				set2.add(string);
			}
			s.add(set2);
			map.put(size, s);
		}
	}
	
	
	return map;
		
	}
}
