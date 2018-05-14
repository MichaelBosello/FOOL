package ast;
import java.util.ArrayList;

import lib.FOOLlib;

public class ClassCallNode implements Node {

	private String id;
	private int nestingLevel;
	private STentry entry;
	private STentry methodEntry;
	private ArrayList<Node> parlist = new ArrayList<Node>(); 

	public ClassCallNode (String i, STentry st, STentry methodEntry, ArrayList<Node> p, int nl) {
		id=i;
		entry=st;
		parlist=p;
		nestingLevel=nl;
		this.methodEntry = methodEntry;
	}


	public String toPrint(String s) {
		String parlstr="";
		for (Node par:parlist){parlstr+=par.toPrint(s+"  ");};
		return s+"ClassCall:" + id + " at nestinglevel "+nestingLevel+"\n"  +
		entry.toPrint(s+"  ") +  
		methodEntry.toPrint(s+"  ") + 
		parlstr;
	}

	public Node typeCheck() {	 
		ArrowTypeNode t=null;
		if (methodEntry.getType() instanceof ArrowTypeNode) t=(ArrowTypeNode) methodEntry.getType(); 
		else {
			System.out.println("Invocation of a non-function "+methodEntry.getType().getClass());
			System.exit(0);
		}
		ArrayList<Node> p = t.getParList();
		if ( !(p.size() == parlist.size()) ) {
			System.out.println("Wrong number of parameters in the invocation of "+id);
			System.exit(0);
		} 
		for (int i=0; i<parlist.size(); i++) 
			if ( !(FOOLlib.isSubtype( (parlist.get(i)).typeCheck(), p.get(i)) ) ) {
				System.out.println("Wrong type for "+(i+1)+"-th parameter in the invocation of "+id+ 
						" expected: " + p.get(i).getClass() + " passed: " + (parlist.get(i)).typeCheck().getClass() );
				if(p.get(i) instanceof ArrowTypeNode && parlist.get(i).typeCheck() instanceof ArrowTypeNode) {
					ArrowTypeNode aHO = (ArrowTypeNode) p.get(i);
					ArrowTypeNode bHO = (ArrowTypeNode) parlist.get(i).typeCheck();
					System.out.println("	expected");
					for(int index = 0; index < aHO.getParList().size(); index++) {
						System.out.println("	" + aHO.getParList().get(index).getClass());
					}
					System.out.println("	->" + aHO.getRet().getClass());
					System.out.println("	getted");
					for(int index = 0; index < bHO.getParList().size(); index++) {
						System.out.println("	" + bHO.getParList().get(index).getClass());
					}
					System.out.println("	->" + bHO.getRet().getClass());
				}
				System.exit(0);
			} 
		return t.getRet();
	}

	public String codeGeneration() {
		String parCode=""; 
		for (int i=parlist.size()-1; i>=0; i--)
			parCode+=parlist.get(i).codeGeneration();

		String getAR=""; //recupero l'AR in cui � dichiarata la funzione che sto usando
		for (int i=0;i<nestingLevel-entry.getNestinglevel();i++)
			//differenza di nesting level tra dove sono e la dichiarazione di "id"
			getAR+="lw\n";
		return //allocazione della mia parte dell'AR della funzione che sto chiamando
				"lfp\n"+ //CL
				parCode + //allocazione valori parametri

				"push "+(entry.getOffset())+"\n"+ //metto l'offset sullo stack
				"lfp\n"+getAR+ //risalgo la catena statica e ottengo l'indirizzo dell'AR della variabile	 
				"add\n"+
				"lw\n"+ //carico indirizzo AR (Access Link)

			    "push "+(entry.getOffset())+"\n"+ //metto l'offset sullo stack
			 	"lfp\n"+getAR+ //risalgo la catena statica e ottengo l'indirizzo dell'AR della variabile	 
				"add\n"+
				"lw\n"+//object pointer
				
				"lw\n"+//dispatch pointer
				
				"push "+(methodEntry.getOffset())+"\n"+//offset metodo
				"add\n"+
				
				"lw\n"+//indirizzo metodo
			    "js\n";
	}

}  