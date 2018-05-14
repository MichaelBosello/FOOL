package ast;

import lib.FOOLlib;

public class GreatEqualNode implements Node {

  private Node left;
  private Node right;
  
  public GreatEqualNode (Node l, Node r) {
   left=l;
   right=r;
  }
  
  public String toPrint(String s) {
   return s+"GreatEqual\n" + left.toPrint(s+"  ")   
                      + right.toPrint(s+"  ") ; 
  }
    
  public Node typeCheck() {
	Node l= left.typeCheck();  
	Node r= right.typeCheck();  
    if ( !(FOOLlib.isSubtype(l, r) || FOOLlib.isSubtype(r, l)) ) {
      System.out.println("Incompatible types in greatequal");
	  System.exit(0);	
    }  
    return new BoolTypeNode();
  }
  
  public String codeGeneration() {
	  String l1 = FOOLlib.freshLabel();
	  String l2 = FOOLlib.freshLabel();
	  String l3 = FOOLlib.freshLabel();
	  return left.codeGeneration()+
			     right.codeGeneration()+
			     "bleq "+l1+"\n"+
			     "push 1\n"+
			     "b "+l3+"\n"+
			     l1 + ": \n"+
			     left.codeGeneration()+
			     right.codeGeneration()+
			     "beq "+l2+"\n"+
			     "push 0\n"+
			     "b "+l3+"\n"+
			     l2 + ": \n"+
			     "push 1\n"+
		         l3 + ": \n";
  }
  
}  