package ast;
import java.util.ArrayList;

import lib.FOOLlib;

public class FunNode implements DecNode {

  private String id;
  private Node type; 
  private ArrayList<DecNode> parlist = new ArrayList<DecNode>(); // campo "parlist" che � lista di Node
  private ArrayList<DecNode> declist = new ArrayList<DecNode>(); 
  private Node exp;
  private Node symType;
  
  public FunNode (String i, Node t) {
   id=i;
   type=t;
  }
  
  public void addDec (ArrayList<DecNode> d) {
    declist=d;
  }  

  public void addBody (Node b) {
	exp=b;
  }  

  public void addPar (DecNode p) { //metodo "addPar" che aggiunge un nodo a campo "parlist"
   parlist.add(p);  
  }  
  
  public String toPrint(String s) {
		 String parlstr="";
		 for (Node par:parlist){parlstr+=par.toPrint(s+"  ");};
		 String declstr="";
		 for (Node dec:declist){declstr+=dec.toPrint(s+"  ");};
	   return s+"Fun:" + id +"\n"
			   +type.toPrint(s+"  ")
			   +parlstr
			   +declstr
               +exp.toPrint(s+"  ") ; 
  }
  
  public Node typeCheck() {
	  for (Node dec:declist){dec.typeCheck();};
      if (! FOOLlib.isSubtype(exp.typeCheck(),type)) {
			  System.out.println("Incompatible value for variable");
			  System.exit(0);
	  }
      return null;
  }
	     
public String codeGeneration() {
	String funl = FOOLlib.freshFunLabel();
	
	String declCode="";
    for (Node dec:declist){declCode+=dec.codeGeneration();};

	String popDecl="";
    for (DecNode dec:declist){popDecl+="pop\n";
    if(dec.getSymType() instanceof ArrowTypeNode)
    	popDecl+="pop\n";};

	String popParl="";
    for (DecNode par:parlist){popParl+="pop\n";
    if(par.getSymType() instanceof ArrowTypeNode)
    	popParl+="pop\n";};
    
	FOOLlib.putCode(funl+":\n"+
	  "cfp\n"+ //setta $fp allo $sp
	  "lra\n"+ //inserimento Return Address
	  declCode+
      exp.codeGeneration()+
      "srv\n"+ //pop del return value e memorizzazione in $rv
      popDecl+ //una pop per ogni dichiarazione
      "sra\n"+ //pop del Return Address e memorizzazione in $ra
      "pop\n" + //pop di AL
      popParl + 
      "sfp\n" + // ripristino il $fp al valore del CL 
      "lrv\n" + // risultato della funzione sullo stack
      "lra\n" + "js\n" // salta a $ra
	  );	  	  
	
	return "lfp\n"+
		   "push "+funl+"\n";
  }
  
  	public void setSymType(Node symType) {
		this.symType = symType;
	}
  
  	@Override
	public Node getSymType() {
		return symType;
	}

}  