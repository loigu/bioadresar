package cz.hnutiduha.bioadresar.util;

import java.util.HashMap;
import java.util.Map;

public class Alphabet {
	private static Map<Character, Character> MAP_NORM;
	
	static { // Greek characters normalization
		MAP_NORM = new HashMap<Character, Character>();
		MAP_NORM.put('á', 'a');
		MAP_NORM.put('č', 'c');
		MAP_NORM.put('ď', 'd');
		MAP_NORM.put('é', 'e');
		MAP_NORM.put('ě', 'e');
		MAP_NORM.put('í', 'i');
		MAP_NORM.put('ň', 'n');
		MAP_NORM.put('ó', 'o');
		MAP_NORM.put('ř', 'r');
		MAP_NORM.put('š', 's');
		MAP_NORM.put('ť', 't');
		MAP_NORM.put('ú', 'u');
		MAP_NORM.put('ů', 'u');
		MAP_NORM.put('ý', 'y');
		MAP_NORM.put('ž', 'z');
		MAP_NORM.put('Á', 'a');
		MAP_NORM.put('Č', 'c');
		MAP_NORM.put('Ď', 'd');
		MAP_NORM.put('É', 'e');
		MAP_NORM.put('Ě', 'e');
		MAP_NORM.put('Í', 'i');
		MAP_NORM.put('Ň', 'n');
		MAP_NORM.put('Ó', 'o');
		MAP_NORM.put('Ř', 'r');
		MAP_NORM.put('Š', 's');
		MAP_NORM.put('Ť', 't');
		MAP_NORM.put('Ú', 'u');
		MAP_NORM.put('Ů', 'u');
		MAP_NORM.put('Ý', 'y');
		MAP_NORM.put('Ž', 'z');
	}
	
	public static String removeAccents(String s)
	{
	    if (s == null) {
	        return null;
	    }
	    StringBuilder sb = new StringBuilder(s);
	
	    for(int i = 0; i < s.length(); i++) {
	        Character c = MAP_NORM.get(sb.charAt(i));
	        if(c != null) {
	            sb.setCharAt(i, c.charValue());
	        }
	    }
	
	    return sb.toString();
	}
}
