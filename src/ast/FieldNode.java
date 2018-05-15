package ast;
public class FieldNode implements DecNode {

  private String id;
  private Node type;
  private int offset;
  
  public FieldNode (String i, Node t) {
   id=i;
   type=t;
  }
  
  public String toPrint(String s) {
	   return s+"Field:" + id +" offset: " + offset + "\n"
			   +type.toPrint(s+"  ") ; 
  }

  //non utilizzato
  public Node typeCheck() {return null;}

  //non utilizzato
  public String codeGeneration() {return "";}
  
  @Override
  public Node getSymType() {
	return type;
  }
  
  public int getOffset() {
  	return offset;
  }

  public void setOffset(int offset) {
  	this.offset = offset;
  }

}  