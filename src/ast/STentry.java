package ast;
public class STentry {
   
  private int nl;
  private Node type;
  private int offset;
  private boolean isMethod = false;
  
  public STentry (int n, int os) {
	  nl=n;
	  offset=os;
  } 
  
  public STentry (int n, Node t, int os) {
	  nl=n;
	  type=t;
	  offset=os;
  } 
  
  public STentry (int n, int os, boolean isMethod) {
	  nl=n;
	  offset=os;
	  this.isMethod = isMethod;
  } 
  
  public STentry (int n, Node t, int os, boolean isMethod) {
	  nl=n;
	  type=t;
	  offset=os;
	  this.isMethod = isMethod;
  } 
  
  public STentry(STentry copy) {
	  nl=copy.getNestinglevel();
	  type=copy.getType();
	  offset=copy.getOffset();
	  this.isMethod = copy.isMethod(); 
  }
  
  public void addType(Node t) {
	  type=t;
  }

  public Node getType() {
	  return type;
  }
  
  public void setOffset(int o) {
	  offset = o;
  }
  
  public int getOffset() {
	  return offset;
  }
  
  public void setNestinglevel(int n) {
	  nl=n;
  } 
  
  public int getNestinglevel() {
	  return nl;
  }   
  
  public boolean isMethod() {
	  return isMethod;
  }
    
  public String toPrint(String s) {
	   return s+"STentry: nestlev " + Integer.toString(nl) +"\n"+
			  s+"STentry: type\n " +
			      type.toPrint(s+"  ") +
			  s+"STentry: offset " + Integer.toString(offset) +"\n"
			  ;  
  }
  
}  