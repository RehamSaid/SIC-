package systemProject;

public class Util {
	
	public static String addLeftZeros(String number, int numberOfDigits) {
		while(number.length() < numberOfDigits){
			number = "0" + number;
		}
		return number;
	}
	
}
