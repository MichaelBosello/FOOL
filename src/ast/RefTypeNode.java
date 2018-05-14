package ast;

public class RefTypeNode implements Node {

  private String id;
  
  public RefTypeNode (String id) {
   this.id = id;
  }

  public String getId () { 
	    return id;
  }
  
  public String toPrint(String s) {
	   return s+"RefType id:" + id + "\n";  
  }

  //non utilizzato
  public Node typeCheck() {return null;}

  //non utilizzato
  public String codeGeneration() {return "";}

}  