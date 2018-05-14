package ast;
import java.util.ArrayList;

public class ClassTypeNode implements Node {

  private ArrayList<Node> allFields = new ArrayList<>();
  private ArrayList<Node> allMethods = new ArrayList<>();
  
  public ClassTypeNode () {}
  
  public ClassTypeNode (Node cp) {
	  ClassTypeNode copy = (ClassTypeNode) cp;
	  allFields = new ArrayList<>(copy.allFields);
	  allMethods = new ArrayList<>(copy.allMethods);
  }
  
  public ClassTypeNode (ArrayList<Node> f, ArrayList<Node> m) {
   allFields=f;
   allMethods=m;
  }
  
  public void addField(Node element, int index) {
	  allFields.add(index, element);
  }
  public void addMethod(Node element, int index) {
	  allMethods.add(index, element);
  }

  public ArrayList<Node> getFields () { 
	    return allFields;
  }
	  
  public ArrayList<Node> getMethods () { 
	    return allMethods;
  }
  
  public String toPrint(String s) {
	 String flstr="";
	 for (Node field:allFields){flstr+=field.toPrint(s+"  ");};
	 for (Node method:allMethods){flstr+=method.toPrint(s+"  ");};
     return flstr;
  }

  //non utilizzato
  public Node typeCheck() {return null;}

  //non utilizzato
  public String codeGeneration() {return "";}

}  