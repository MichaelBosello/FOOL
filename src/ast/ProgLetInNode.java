package ast;
import java.util.ArrayList;
import lib.*;

public class ProgLetInNode implements Node {

  private ArrayList<DecNode> declist;
  private Node exp;
  private Node classNode;
  
  public void addDec(ArrayList<DecNode> d) {
	  declist=d; 
  }
  
  public void addClass(DecNode c) {
	  classNode=c; 
  }
  
  public void addExp(Node e) {
	  exp=e;
  }
  
  public String toPrint(String s) {
	 String declstr="";
	 for (Node dec:declist){declstr+=dec.toPrint(s+"  ");};
	 String classPrint = "";
	 if(classNode != null)
		 classPrint = classNode.toPrint(s+"  ");
     return s+"ProgLetIn\n" + classPrint + declstr + exp.toPrint(s+"  "); 
  }

  public Node typeCheck() {
    for (Node dec:declist){dec.typeCheck();};
    if(classNode != null)
    	classNode.typeCheck();
    return exp.typeCheck();
  }
    
  public String codeGeneration() {
	    String declCode="";
	    for (Node dec:declist){declCode+=dec.codeGeneration();};
	    if(classNode != null)
		return "push 0\n"+
			   classNode.codeGeneration()+
			   declCode+
			   exp.codeGeneration()+"halt\n"+ FOOLlib.getCode();
	    return "push 0\n"+
		   declCode+
		   exp.codeGeneration()+"halt\n"+ FOOLlib.getCode();
  }
}  