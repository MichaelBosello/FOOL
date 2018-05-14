package ast;

import lib.FOOLlib;

public class NotNode implements Node {

  private Node exp;
  
  public NotNode (Node e) {
   exp=e;
  }
  
  public String toPrint(String s) {
   return s+"Not\n" + exp.toPrint(s+"  ") ;
  }

  public Node typeCheck() {
	   if ( ! FOOLlib.isSubtype(exp.typeCheck(), new BoolTypeNode()) ) {
				  System.out.println("Non boolean in not");
				  System.exit(0);	
	   }
	   return new BoolTypeNode();
  }
      
  public String codeGeneration() {
	  String l1 = FOOLlib.freshLabel();
	  String l2 = FOOLlib.freshLabel();
	  return exp.codeGeneration()+
			     "push 1\n"+
			     "beq "+l1+"\n"+
			     "push 1\n"+
			     "b "+l2+"\n"+
			     l1 + ": \n"+
			     "push 0\n"+
			     l2 + ": \n";
			     
  }

}  