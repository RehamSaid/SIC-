package systemProject;

public class SicLine {

	private String location;
	private String line;
	private String label;
	private String instruction;
	private String reference;
	private String objectCode ="";
	private boolean commentLine;

	public SicLine() {
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getInstruction() {
		return instruction;
	}

	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public boolean isCommentLine() {
		return commentLine;
	}

	public void setCommentLine(boolean commentLine) {
		this.commentLine = commentLine;
	}

	public String getLine() {
		return line;
	}

	public void setLine(String line) {
		this.line = line;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getObjectCode() {
		return objectCode;
	}

	public void setObjectCode(String objectCode) {
		this.objectCode = objectCode;
	}

	@Override
	public String toString() {
		return commentLine ? line : location+ "\t" +label + "\t" + instruction + "\t\t" + reference+"\t\t"+ objectCode;
	}
}
