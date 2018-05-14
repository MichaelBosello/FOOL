package ast;

public class IdNode implements Node {

  private String id;
  private int nestingLevel;
  private STentry entry;
  
  public IdNode (String i, STentry st, int nl) {
   id=i;
   entry=st;
   nestingLevel=nl;
  }
  
  public String toPrint(String s) {
	   return s+"Id:" + id + " at nestinglevel "+nestingLevel+"\n" + 
              entry.toPrint(s+"  ") ;  
  }

  public Node typeCheck() {
	  //ora possibile
	/*if (entry.getType() instanceof ArrowTypeNode) {
	  System.out.println("Wrong usage of function identifier");
	  System.exit(0);
	}*/ 
	  if (entry.isMethod()) {
		  System.out.println("Wrong usage of method identifier");
		  System.exit(0);
	  }
	  if (entry.getType() instanceof ClassTypeNode) {
		  System.out.println("Wrong usage of function identifier");
		  System.exit(0);
	  }
	return entry.getType();
  }
  
  public String codeGeneration() {
	  
	  String getAR=""; //recupero l'AR in cui � dichiarata la variable che sto usando
	  for (int i=0;i<nestingLevel-entry.getNestinglevel();i++)
		            //differenza di nesting level tra dove sono e la dichiarazione di "id"
		  getAR+="lw\n";
	  
	  //Se variabile metto valore, se funzione metto indirizzo AR dove e' dichiarata la funzione
	  String code = "push "+entry.getOffset()+"\n"+ //metto l'offset sullo stack
             "lfp\n"+getAR+ //risalgo la catena statica e ottengo l'indirizzo dell'AR della variabile	 
			 "add\n"+
             "lw\n"; //carico sullo stack il valore all'indirizzo ottenuto
	  if(entry.getType() instanceof ArrowTypeNode)//metto indirizzo funzione che si trova a offset -1
		  code += "push "+ (entry.getOffset() - 1) +"\n"+ //metto l'offset sullo stack
		             "lfp\n"+getAR+ //risalgo la catena statica e ottengo l'indirizzo dell'AR della variabile	 
					 "add\n"+
		             "lw\n"; //carico sullo stack il valore all'indirizzo ottenuto
	  return code;
  }

}  