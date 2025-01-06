package org.sully.d2;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRegex {

	public static void main(String[] args) {
		String x = "Razor Bow,Demon Fletch | +12 to Mana,+158 to Attack Rating,+36% Enhanced Damage, | Sk0QIIAAZWzTIXoCB00MBLuyA";
		
		Pattern p = Pattern.compile("\\+([0-9]+)% Enhanced Damage");
		Matcher m = p.matcher(x);
		if (m.find()) {
			System.out.println(m.group(1));
		}
	}
}
