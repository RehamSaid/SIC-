package systemProject;

public class Operation {
	
	private String name;
	private String opcode;
	
	public Operation(String name, String opcode) {
		this.name = name;
		this.opcode = opcode;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getOpcode() {
		return opcode;
	}

	public void setOpcode(String opcode) {
		this.opcode = opcode;
	}
	
	
	
}
