package lib;

import java.util.ArrayList;
import java.util.HashMap;

import ast.*;

public class FOOLlib {
	
  public static final int MEMSIZE = 10000;
    
  private static int labCount=0; 

  private static int funLabCount=0; 
  
  private static String funCode="";
  
  private static HashMap<String,String> superType = new HashMap<>();
  
  private static ArrayList< ArrayList<String> > dispatchTables = new ArrayList<>();
  
  //valuta se il tipo "a" � <= al tipo "b", dove "a" e "b" sono tipi di base: int o bool
  public static boolean isSubtype (Node a, Node b) {
	  if(a instanceof BoolTypeNode || a instanceof IntTypeNode)
		  return a.getClass().equals(b.getClass()) ||
	    	   ( (a instanceof BoolTypeNode) && (b instanceof IntTypeNode) );  
	  if(a instanceof ArrowTypeNode && b instanceof ArrowTypeNode) {
		  ArrowTypeNode aHO = (ArrowTypeNode) a;
		  ArrowTypeNode bHO = (ArrowTypeNode) b;
		  //covarianza del tipo di ritorno e controvarianza dei parametri
		  if(isSubtype(aHO.getRet(), bHO.getRet()) && aHO.getParList().size() == bHO.getParList().size()) {
			  for(int index = 0; index < aHO.getParList().size(); index++) {
				  if(!aHO.getParList().get(index).getClass().equals(bHO.getParList().get(index).getClass()) && 
						  isSubtype(aHO.getParList().get(index), bHO.getParList().get(index)))
					  return false;//covarianza
			  }
			  return true;//controvarianza
		  }
	  }
	  if(a instanceof EmptyTypeNode && b instanceof RefTypeNode) {
		  return true;
	  }
	  if(a instanceof RefTypeNode && b instanceof RefTypeNode) {
		  RefTypeNode aRef = (RefTypeNode) a;
		  RefTypeNode bRef = (RefTypeNode) b;
		  if(aRef.getId().equals(bRef.getId()))
			  return true;
		  String sub = superType.get(aRef.getId());
		  while(sub != null) {
			  if(sub.equals(bRef.getId()))
				  return true;
			  sub = superType.get(sub);
		  }
	  }
	  
	  return false;
  }
  
  public static String freshLabel() {
	  return "label"+(labCount++);
  }

  public static String freshFunLabel() {
	  return "function"+(funLabCount++);
  }

  public static void putCode(String c) {
	  funCode+="\n"+c; //aggiunge una linea vuota di separazione prima della funzione
  }
  
  public static String getCode() {
	  return funCode; 
  }
  
  public static void addDispatchTable(ArrayList<String> dispatchTable) {
	  dispatchTables.add(dispatchTable);
  }
  
  public static ArrayList< ArrayList<String> > getDispatchTables() {
	  return dispatchTables;
  }
  
  public static void mapSuperType(String className, String superName) {
	  superType.put(className, superName);
  }

}
