package ast;

import lib.*;

public class AndNode implements Node {

  private Node left;
  private Node right;
  
  public AndNode (Node l, Node r) {
   left=l;
   right=r;
  }
  
  public String toPrint(String s) {
   return s+"And\n" + left.toPrint(s+"  ")  
                     + right.toPrint(s+"  ") ; 
  }

  public Node typeCheck() {
   if ( ! ( FOOLlib.isSubtype(left.typeCheck(), new BoolTypeNode()) &&
		    FOOLlib.isSubtype(right.typeCheck(), new BoolTypeNode()) ) ) {
			  System.out.println("Non boolean in and");
			  System.exit(0);	
   }
   return new BoolTypeNode();
  }
  
  public String codeGeneration() {
	  String l1 = FOOLlib.freshLabel();
	  String l2 = FOOLlib.freshLabel();
	  return left.codeGeneration()+
			     "push 0\n"+
			     "beq "+l1+"\n"+
			     right.codeGeneration()+
			     "push 0\n"+
			     "beq "+l1+"\n"+
			     "push 1\n"+
			     "b "+l2+"\n"+
			     l1 + ": \n"+
			     "push 0\n"+
		         l2 + ": \n";
  }

}  