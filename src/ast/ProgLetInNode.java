package ast;
import java.util.ArrayList;
import lib.*;

public class ProgLetInNode implements Node {

  private ArrayList<DecNode> declist = new ArrayList<DecNode>();
  private ArrayList<DecNode> classNode = new ArrayList<DecNode>();
  private Node exp;
  
  public void addDec(ArrayList<DecNode> d) {
	  declist=d; 
  }
  
  public void addClass(ArrayList<DecNode> c) {
	  classNode=c; 
  }
  
  public void addExp(Node e) {
	  exp=e;
  }
  
  public String toPrint(String s) {
	 String declstr="";
	 for (Node dec:declist){declstr+=dec.toPrint(s+"  ");};
	 String classPrint = "";
	 for (Node cl:classNode){classPrint+=cl.toPrint(s+"  ");};
     return s+"ProgLetIn\n" + classPrint + declstr + exp.toPrint(s+"  "); 
  }

  public Node typeCheck() {
    for (Node dec:declist){dec.typeCheck();};
    for (Node cl:classNode){cl.typeCheck();};
    return exp.typeCheck();
  }
    
  public String codeGeneration() {
	    String declCode="";
	    for (Node dec:declist){declCode+=dec.codeGeneration();};
	    String classCode = "";
	    for (Node cl:classNode){classCode+=cl.codeGeneration();};
		return "push 0\n"+
			   classCode+
			   declCode+
			   exp.codeGeneration()+"halt\n"+ FOOLlib.getCode();
  }
}  