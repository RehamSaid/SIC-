package systemProject;

import static systemProject.Util.addLeftZeros;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Sic {

	private boolean firstStart = true;
	private String locationCounter;
	private String startingAddress = "000000";
	private String programLength = "000000";
	private final HashMap<String, Operation> opTable;
	private final HashMap<String, String> symbolTable;
	private final ArrayList<SicLine> instructionLines;
	private final ArrayList<String> errors;

	public Sic(final String operationsFile) throws FileNotFoundException {
		locationCounter = "0000";
		opTable = new HashMap<>();
		symbolTable = new HashMap<>();
		instructionLines = new ArrayList<>();
		errors = new ArrayList<>();
		fillOptab(operationsFile);
	}

	private void printOpTable() {
		System.out.println("------------------");
		System.out.println("Operation Table:");
		System.out.println("Name" + "\t" + "Opcode");
		for (Map.Entry<String, Operation> entry : opTable.entrySet()) {
			String key = entry.getKey();
			Operation value = entry.getValue();
			System.out.println(key + "\t" + value.getOpcode());
		}
		System.out.println("------------------");
	}

	private void printSymbolTable() {
		System.out.println("------------------");
		System.out.println("Symbol Table:");
		System.out.println("Label" + "\t" + "Location");
		for (Map.Entry<String, String> entry : symbolTable.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			System.out.println(key + "\t" + value);
		}
		System.out.println("------------------");
	}

	private void fillOptab(final String operationsFile) throws FileNotFoundException {
		Scanner s = new Scanner(new File(operationsFile));
		while (s.hasNext()) {
			String line = s.nextLine();
			String[] text = line.split("\t");
			String name = text[0];
			String opcode = text[1];
			Operation operation = new Operation(name, opcode);
			opTable.put(name, operation);
		}
	}

	private int calculateIncrementValue(String instruction, String reference) {
		if (instruction.equalsIgnoreCase("WORD")) {
			return 3*(reference.split(",").length);
		} else if (instruction.equalsIgnoreCase("RESW")) {
			return 3 * Integer.parseInt(reference);
		} else if (instruction.equalsIgnoreCase("BYTE")) {
			if (reference.charAt(0) == 'X') {
				int length = (reference.length() - 3);
				return (length / 2) + (length % 2);
			} else if (reference.charAt(0) == 'C') {
				return reference.length() - 3;
			} else {
				errors.add("Invalid Byte Operand: " + reference);
				return 0;
			}
		} else if (instruction.equalsIgnoreCase("RESB")) {
			return Integer.parseInt(reference);
		} else if (instruction.equalsIgnoreCase("END") || instruction.equalsIgnoreCase("START")) {
			return 0;
		} else {
			if (!instruction.isEmpty()) {
				if (opTable.containsKey(instruction)) {
					return 3;
				} else if(instruction.charAt(0)=='+'){
					return 4;
				}
				
				else {
					errors.add("Invalid Operation: " + instruction);
					return 0;
			
				}
			}
		}
		return 0;
	}

	private void updateLocationCounter(int incrementValue) {
		int locationCounterIntegerValue = Integer.parseInt(locationCounter, 16);
		locationCounterIntegerValue = locationCounterIntegerValue + incrementValue;
		locationCounter = addLeftZeros(Integer.toHexString(locationCounterIntegerValue), 4);
	}

	private void calculateProgramLength() {
		int locationCounterIntegerValue = Integer.parseInt(locationCounter, 16);
		int startingAddressIntegerValue = Integer.parseInt(startingAddress, 16);
		int programLengthIntegerValue = locationCounterIntegerValue - startingAddressIntegerValue;
		programLength = addLeftZeros(Integer.toHexString(programLengthIntegerValue), 6);
	}

	private void pass1(final String sourceFileName) throws IOException {
		File copyFile = new File("copy.txt");
		FileWriter fw = new FileWriter(copyFile);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write("Loc\tLabel\tInstruction\tOperand");
		bw.newLine();
		Scanner s = new Scanner(new File(sourceFileName));
		int incrementValue = 0;
		String instruction = "";
		while (s.hasNext() && !instruction.equalsIgnoreCase("END")) {
			String inputLine = s.nextLine();
			SicLine sicLine = new SicLine();
			if (inputLine.charAt(0) != '.') {
				String[] text = inputLine.split("\t");
				String label = text[0].strip();
				instruction = text[1].strip();
				
				String reference = "";
				if (text.length == 3) {
					reference = text[2].strip();
				}
				if (instruction.equalsIgnoreCase("START")) {
					if (firstStart) {
						firstStart = false;
						locationCounter = reference;
						startingAddress = addLeftZeros(locationCounter, 6);
					} else {
						errors.add("Multiple START: " + instruction);
					}
				}

				updateLocationCounter(incrementValue);

				if (label != null && !label.isEmpty()) {
					if (symbolTable.containsKey(label)) {
						errors.add("Duplicate Label: " + label);
					} else {
						symbolTable.put(label, locationCounter);
					}
				}

				incrementValue = calculateIncrementValue(instruction, reference);

				sicLine.setLocation(locationCounter);
				sicLine.setLabel(label);
				sicLine.setInstruction(instruction);
				sicLine.setReference(reference);
			} else {
				sicLine.setLine(inputLine);
				sicLine.setCommentLine(true);
			}
			bw.write(sicLine.toString());
			bw.newLine();
			instructionLines.add(sicLine);
		}
		calculateProgramLength();
		System.out.println("Staring Address: " + startingAddress);
		System.out.println("Program Length: " + programLength);
		bw.close();
		instructionLines.forEach(System.out::println);
		errors.forEach(System.out::println);
	}

	private void pass2() throws IOException {
		File HTEFile = new File("HTE.txt");
		FileWriter fwHTE = new FileWriter(HTEFile);
		BufferedWriter bwHTE = new BufferedWriter(fwHTE);
		String HTEConsole = "";
		File outputFile = new File("pass2.txt");
		FileWriter fwOutputFile = new FileWriter(outputFile);
		BufferedWriter bwOutputFile = new BufferedWriter(fwOutputFile);

		System.out.println("Loc\tLabel\tInstruction\tOperand\t\tObject Code");
		bwOutputFile.write("Loc\tLabel\tInstruction\tOperand\t\tObject Code");
		bwOutputFile.newLine();

		HTEConsole= HTEConsole+getHeaderFileContent()+"\n";
		bwHTE.write(getHeaderFileContent());
		bwHTE.newLine();

		String textObjectCode = "";
		String textStartingAddress = "";
		String textLength = "";

		for (SicLine line : instructionLines) {
			if (!line.isCommentLine()) {
				String referenceAddress = "";
				String objectCode = "";
				line.setObjectCode(objectCode);
				if (opTable.containsKey(line.getInstruction())) {

					// get object code from instruction
					objectCode = opTable.get(line.getInstruction()).getOpcode();

					// get reference address from operand
					if (!line.getReference().isEmpty()) {
						if (!line.getReference().endsWith(",X")) {
							if (symbolTable.containsKey(line.getReference())) {
								referenceAddress = symbolTable.get(line.getReference());
							} else {
								referenceAddress = "0000";
								errors.add("Undefined Symbol: " + line.getReference());
							}
						} else {
							if (symbolTable.containsKey(line.getReference().split(",")[0])) {
								referenceAddress = symbolTable.get(line.getReference().split(",")[0]);
								referenceAddress = Integer.toHexString(
										Integer.parseInt(referenceAddress, 16) | Integer.parseInt("8000", 16));
							} else {
								referenceAddress = "0000";
								errors.add("Undefined Symbol: " + line.getReference());
							}
						}
					} else {
						referenceAddress = "0000";
					}
					line.setObjectCode(objectCode + referenceAddress);
				} else if (line.getInstruction().equalsIgnoreCase("BYTE")) {
					if (line.getReference().charAt(0) == 'X') {
						objectCode = line.getReference().substring(2, line.getReference().length() - 1);
					} else if (line.getReference().charAt(0) == 'C') {
						String characters = line.getReference().substring(2, line.getReference().length() - 1);
						for (int i = 0; i < characters.length(); i++) {
							int asciiIntegerValue = characters.charAt(i);
							String asciiHexValue = addLeftZeros(Integer.toHexString(asciiIntegerValue), 2);
							objectCode = objectCode.concat(asciiHexValue);
						}
					}
					line.setObjectCode(objectCode);
				} else if (line.getInstruction().equalsIgnoreCase("WORD")) {
					String []words=line.getReference().split(",");
					for(String word:words) {
						objectCode = objectCode.concat(addLeftZeros(Integer.toHexString(Integer.parseInt(word)), 6)) ;
					}	
						line.setObjectCode(objectCode);
				} else {
					errors.add("Invalid Operation: " + line.getInstruction());
				}
				if (textObjectCode.isEmpty() && !line.getObjectCode().isEmpty()) {
					// start new text record
					textStartingAddress = addLeftZeros(line.getLocation(), 6);
					textObjectCode = line.getObjectCode();
				} else if (!line.getObjectCode().isEmpty()) {
					// if line has object code
					// and text record has buffered object code
					if (textObjectCode.length() + line.getObjectCode().length() <= 60) {
						textObjectCode = textObjectCode.concat(line.getObjectCode());
					} else {
						int length = textObjectCode.length();
						int bytesLength = (length / 2) + (length % 2);
						textLength = addLeftZeros(Integer.toHexString(bytesLength), 2);
						HTEConsole= HTEConsole+getTextFileContent(textStartingAddress, textLength, textObjectCode)+"\n";
						bwHTE.write(getTextFileContent(textStartingAddress, textLength, textObjectCode));
						bwHTE.newLine();
						textStartingAddress = addLeftZeros(line.getLocation(), 6);
						textObjectCode = line.getObjectCode();
					}
				} else if (!textObjectCode.isEmpty()) {
					// if line doesn't have object code
					// and text record has buffered object code
					int length = textObjectCode.length();
					int bytesLength = (length / 2) + (length % 2);
					textLength = addLeftZeros(Integer.toHexString(bytesLength), 2);
					HTEConsole= HTEConsole+getTextFileContent(textStartingAddress, textLength, textObjectCode)+"\n";
					bwHTE.write(getTextFileContent(textStartingAddress, textLength, textObjectCode));
					bwHTE.newLine();
					textObjectCode = "";
				}
			}
			System.out.println(line);
			bwOutputFile.write(line.toString());
			bwOutputFile.newLine();
		}

		HTEConsole= HTEConsole+getEndFileContent();
		bwHTE.write(getEndFileContent());
		bwHTE.newLine();
		System.out.println("------------------");
		System.out.println("HTE Record:");
		System.out.println(HTEConsole);
		
		bwHTE.close();
		bwOutputFile.close();
	}

	private String getHeaderFileContent() {
		int programNameIndex = 0;
		while (instructionLines.get(programNameIndex).isCommentLine()) {
			programNameIndex++;
		}
		String programName = instructionLines.get(programNameIndex).getLabel();
		return "H" + String.format("%6.6s", programName) + startingAddress + programLength;
	}
	
	private String getTextFileContent(String textStartingAddress, String textLength, String textObjectCode) {
		return "T" + textStartingAddress + textLength + textObjectCode;
	}
	
	private String getEndFileContent() {
		return "E" + startingAddress;
	}

	public static void main(String[] args) throws IOException {

		Scanner s = new Scanner(System.in);
		System.out.println("Enter Operations file name");
		String operationsFile = s.next()+".txt";
		System.out.println("Enter Source file name");
		String sourceFile = s.next() +".txt";

		Sic sic = new Sic(operationsFile);
		sic.printOpTable();
		sic.pass1(sourceFile);
		sic.printSymbolTable();
		sic.pass2();
	}

}
