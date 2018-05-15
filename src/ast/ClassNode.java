package ast;
import java.util.ArrayList;

import lib.FOOLlib;

public class ClassNode implements DecNode {

  private String id;
  private String parent;
  private STentry superEntry;
  private ArrayList<DecNode> fields = new ArrayList<DecNode>(); // campo "parlist" che � lista di Node
  private ArrayList<DecNode> methods = new ArrayList<DecNode>(); 
  private Node symType;
  
  public ClassNode (String i) {
	   id=i;
  }
  
  public ClassNode (String i, String p, STentry pEntry) {
   id=i;
   parent=p;
   superEntry = pEntry;
  }
  
  public void addField (DecNode f) {
    fields.add(f);
  }  

  public void addMethod (DecNode m) {
	methods.add(m);
  }  
  
  public String toPrint(String s) {
		 String fieldlstr="";
		 for (Node field:fields){fieldlstr+=field.toPrint(s+"  ");};
		 String metlstr="";
		 for (Node met:methods){metlstr+=met.toPrint(s+"  ");};
		 String superPrint = "";
		 if(superEntry != null)
			 superPrint = superEntry.toPrint(s+"  ");
	   return s+"Class:" + id +" extends " + parent + "\n"
			   +superPrint
			   +fieldlstr
			   +metlstr;
  }
  
  public Node typeCheck() {
	  for (Node met:methods){met.typeCheck();};
	  
	  if(superEntry != null) {
		  ClassTypeNode type = (ClassTypeNode) symType;
		  ClassTypeNode superType = (ClassTypeNode) superEntry.getType();
		  
		  for(int i = 0; i < fields.size(); i++) {
			  FieldNode f = (FieldNode) fields.get(i);
			  int pos = -f.getOffset() -1;
			  if(pos < superType.getFields().size())
			  if(!FOOLlib.isSubtype(type.getFields().get(pos), superType.getFields().get(pos))) {
				  System.out.println("Incompatible field override super: " + superType.getFields().get(pos).toPrint("") + " sub: " + type.getFields().get(i).toPrint(""));
				  System.exit(0);
			  }
		  }
		  
		  for(int i = 0; i < methods.size(); i++) {
			  MethodNode m = (MethodNode) methods.get(i);
			  int pos = m.getOffset();
			  if(pos < superType.getMethods().size())
			  if(!FOOLlib.isSubtype(type.getMethods().get(pos), superType.getMethods().get(pos))) {
				  System.out.println("Incompatible method override super: " + superType.getMethods().get(pos).toPrint("") + " sub: " + type.getMethods().get(i).toPrint(""));
				  System.exit(0);
			  }
		  }
		  
		  /*for(int i = 0; i < superType.getFields().size(); i++) {
			  if(!FOOLlib.isSubtype(type.getFields().get(i), superType.getFields().get(i))) {
				  System.out.println("Incompatible field override super: " + superType.getFields().get(i).toPrint("") + " sub: " + type.getFields().get(i).toPrint(""));
				  System.exit(0);
			  }
		  }
		  for(int i = 0; i < superType.getMethods().size(); i++) {
			  if(!FOOLlib.isSubtype(type.getMethods().get(i), superType.getMethods().get(i))) {
				  System.out.println("Incompatible method override");
				  System.exit(0);
			  }
		  }*/
	  }
	  
	  return null;
  }
	     
  public String codeGeneration() {
	  ArrayList<String> dispatchTable = new ArrayList<>();
	  if(superEntry != null) {
		  dispatchTable = new ArrayList<>(FOOLlib.getDispatchTables().get((-superEntry.getOffset()) - 2));
	  }
	  FOOLlib.addDispatchTable(dispatchTable);
	  for(int i = 0; i < methods.size(); i++) {
		  MethodNode m = (MethodNode) methods.get(i);
		  m.codeGeneration();
		  dispatchTable.add(m.getOffset(),m.getLabel());
	  }
	  String cod = "lhp\n";
	  for(int i = 0; i < dispatchTable.size(); i++) {
		  cod += "push " + dispatchTable.get(i) + "\n"
				  + "lhp\n" + "sw\n"
				  + "lhp\n" + "push 1\n" + "add\n" + "shp\n";
	  }
	  
	  return cod;
  }
  
  	public void setSymType(Node symType) {
		this.symType = symType;
	}
  
  	@Override
	public Node getSymType() {
		return symType;
	}

}  