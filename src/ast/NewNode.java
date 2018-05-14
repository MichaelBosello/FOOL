package ast;
import java.util.ArrayList;

import lib.FOOLlib;

public class NewNode implements Node {

	private String id;
	private int nestingLevel;
	private STentry entry;
	private ArrayList<Node> parlist = new ArrayList<Node>(); 

	public NewNode (String i, STentry st, ArrayList<Node> p, int nl) {
		id=i;
		entry=st;
		parlist=p;
		nestingLevel=nl;
	}


	public String toPrint(String s) {
		String parlstr="";
		for (Node par:parlist){parlstr+=par.toPrint(s+"  ");};
		return s+"New:" + id + " at nestinglevel "+nestingLevel+"\n"  +
		entry.toPrint(s+"  ") +  
		parlstr;
	}

	public Node typeCheck() {	 
		ClassTypeNode t=null;
		if (entry.getType() instanceof ClassTypeNode) t=(ClassTypeNode) entry.getType(); 
		else {
			System.out.println("Instantiation "+id);
			System.exit(0);
		}
		ArrayList<Node> p = t.getFields();
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
		return new RefTypeNode(id);
	}

	public String codeGeneration() {
		String cod="";
		for (int i=0; i< parlist.size(); i++)
			cod+=parlist.get(i).codeGeneration();
		for (int i=0; i< parlist.size(); i++)
			cod+= "lhp\n" + "sw\n" +
				  "lhp\n" + "push 1\n" + "add\n" + "shp\n";
		cod += "push " + (FOOLlib.MEMSIZE + entry.getOffset()) + "\nlw\n" + "lhp\n" + "sw\n";
		cod += "lhp\n" + "lhp\n" + "push 1\n" + "add\n" + "shp\n";
		return cod;
	}

}  