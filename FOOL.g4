grammar FOOL;

@header{
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import ast.*;
import lib.FOOLlib;
}

@parser::members{
private int nestingLevel = 0;
private int globalOffset = -2;
private ArrayList<HashMap<String,STentry>> symTable = new ArrayList<HashMap<String,STentry>>();
//livello ambiente con dichiarazioni piu' esterno è 0 (prima posizione ArrayList) invece che 1 (slides)
//il "fronte" della lista di tabelle è symTable.get(nestingLevel)
private HashMap<String, HashMap<String,STentry>> classTable = new HashMap<>();
// tiene traccia delle virtual table (symbol table che include le entry di campi e metodi ereditati su cui non e' stato fatto overriding)
}

@lexer::members {
int lexicalErrors=0;
}

/*------------------------------------------------------------------
 * PARSER RULES
 *------------------------------------------------------------------*/
 
prog returns [Node ast]
	: {HashMap<String,STentry> hm = new HashMap<String,STentry> ();
       symTable.add(hm);}          
	  (   e=exp 
          {$ast = new ProgNode($e.ast);} 
	      | LET 
	      {ProgLetInNode prog = new ProgLetInNode(); $ast=prog;}
	      ( c=cllist {prog.addClass($c.astlist);}(d=declist {prog.addDec($d.astlist);})?
	        | d=declist {prog.addDec($d.astlist);}
	      ) IN e=exp {prog.addExp($e.ast);}      
	  ) 
	  {symTable.remove(nestingLevel);}
      SEMIC ;

cllist returns [ArrayList<DecNode> astlist]
  : {$astlist= new ArrayList<DecNode>() ;}   
  	    ( CLASS c=ID {
  					ClassNode cl = new ClassNode($c.text);
  					ClassTypeNode ctype = new ClassTypeNode();
  					cl.setSymType(ctype);
  					HashMap<String,STentry> virtualTable = new HashMap<String,STentry> ();
  					HashSet<String> declaration = new HashSet<>();
  				 }
  		(EXTENDS p=ID {
  			   //cercare la dichiarazione
	           STentry superEntry=(symTable.get(0)).get($p.text);
	           if (superEntry==null)
	           {System.out.println("Class "+$p.text+" at line "+$p.line+" not declared");
	            System.exit(0);} 
  			   cl = new ClassNode($c.text,$p.text,superEntry);
  			   ctype = new ClassTypeNode(superEntry.getType());
  			   cl.setSymType(ctype);
  			   HashMap<String,STentry> parentVirtualTable = classTable.get($p.text);
  			   if(parentVirtualTable == null)
	          	 {System.out.println("Class "+$p.text+" at line "+$p.line+" not declared");
	             System.exit(0);} 
	             for (String key : parentVirtualTable.keySet()) {
	             	virtualTable.put( key, new STentry(parentVirtualTable.get(key)) );
				 }
  			   FOOLlib.mapSuperType($c.text, $p.text);
  		})? 
  		{
  			$astlist.add(cl); 
  			HashMap<String,STentry> hm = symTable.get(nestingLevel);
			if ( hm.put($c.text,new STentry(nestingLevel,ctype,globalOffset--)) != null  )
				{System.out.println("Class id "+$c.text+" at line "+$c.line+" already declared");
				System.exit(0);
			}
			classTable.put($c.text, virtualTable);
  		}
  	  {
  	  	nestingLevel++;
  	  	symTable.add(virtualTable);
	     int fieldOffset= (-ctype.getFields().size()) -1;
	     int methodOffset=ctype.getMethods().size();
	  }
  		   LPAR (pid=ID COLON pt=type
  		   		{ 
                  FieldNode fpar = new FieldNode($pid.text,$pt.ast); 
                  cl.addField(fpar);
                  
                  if(declaration.contains($pid.text))
                  {System.out.println("Field id "+$pid.text+" at line "+$pid.line+" already declared");
                   System.exit(0);}
                  declaration.add($pid.text);
                  
                  if(virtualTable.get($pid.text) == null)
                  	virtualTable.put($pid.text,new STentry(nestingLevel,$pt.ast,fieldOffset--));
                  else
                  {
                  	virtualTable.get($pid.text).addType($pt.ast);
                  	virtualTable.get($pid.text).setNestinglevel(nestingLevel);
                  }
                  ctype.addField($pt.ast, (-virtualTable.get($pid.text).getOffset()) - 1);                  
                  fpar.setOffset(virtualTable.get($pid.text).getOffset());
                }
  		   	(COMMA pid=ID COLON pt=type
  		   		{ 
                  FieldNode fcpar = new FieldNode($pid.text,$pt.ast); 
                  cl.addField(fcpar);
                  
                  if(declaration.contains($pid.text))
                  {System.out.println("Field id "+$pid.text+" at line "+$pid.line+" already declared");
                   System.exit(0);}
                  declaration.add($pid.text);
                  
                  if(virtualTable.get($pid.text) == null)
                  	virtualTable.put($pid.text,new STentry(nestingLevel,$pt.ast,fieldOffset--));
                  else
                  {
                  	virtualTable.get($pid.text).addType($pt.ast);
                  	virtualTable.get($pid.text).setNestinglevel(nestingLevel);
                  }
                  ctype.addField($pt.ast, (-virtualTable.get($pid.text).getOffset()) - 1);                  
                  fcpar.setOffset(virtualTable.get($pid.text).getOffset());
                }
  		   	)*
  		   )? 
  		   RPAR    
           CLPAR
           ( 
			  FUN i=ID COLON t=type
	              {
	               MethodNode f = new MethodNode($i.text,$t.ast);      
	               cl.addMethod(f);
	               
	               if(declaration.contains($i.text))
                  {System.out.println("Method id "+$i.text+" at line "+$i.line+" already declared");
                   System.exit(0);}
                  declaration.add($i.text);
	               
	               STentry entry=new STentry(nestingLevel, methodOffset++, true);
	               if(virtualTable.get($i.text) == null)
	               	 virtualTable.put($i.text,entry);
	               else
	               {
	               	 methodOffset--;
                  	 entry.setOffset(virtualTable.get($i.text).getOffset());
                  	 virtualTable.replace($i.text,entry);
                   }
                   f.setOffset(virtualTable.get($i.text).getOffset());
                   	ctype.addMethod($t.ast, virtualTable.get($i.text).getOffset());
                   
                   
	                //creare una nuova hashmap per la symTable
	                nestingLevel++;
	                HashMap<String,STentry> hmn = new HashMap<String,STentry> ();
	                symTable.add(hmn);
	                }
              LPAR {ArrayList<Node> parTypes = new ArrayList<Node>();
              	    int paroffset=1;
                    }
              (fid=ID COLON fty=hotype
                  { 
                  parTypes.add($fty.ast);
                  ParNode pa = new ParNode($fid.text,$fty.ast); //creo nodo ParNode
                  f.addPar(pa);                                 //lo attacco al FunNode con addPar
                  if($fty.ast instanceof ArrowTypeNode)
              		  paroffset++; 
                  if ( hmn.put($fid.text,new STentry(nestingLevel,$fty.ast,paroffset++)) != null  ) //aggiungo dich a hmn
                  {System.out.println("Parameter id "+$fid.text+" at line "+$fid.line+" already declared");
                   System.exit(0);}
                  }
	              (COMMA id=ID COLON ty=hotype
	                    {
	                    parTypes.add($ty.ast);
	                    ParNode par = new ParNode($id.text,$ty.ast);
	                    f.addPar(par);
	                    if($ty.ast instanceof ArrowTypeNode)
	              			paroffset++; 
	                    if ( hmn.put($id.text,new STentry(nestingLevel,$ty.ast,paroffset++)) != null  )
	                    {System.out.println("Parameter id "+$id.text+" at line "+$id.line+" already declared");
	                     System.exit(0);}
	                    }
	                )*
                )? 
              RPAR {entry.addType(new ArrowTypeNode(parTypes,$t.ast));
              		f.setSymType(entry.getType());}
	                     (LET
	                     	{ArrayList<DecNode> var = new ArrayList<DecNode>() ;
	   							int varoffset=-2;
	  						}  
	                     	(VAR vi=ID COLON vt=type ASS ve=exp
	                     		{VarNode v = new VarNode($vi.text,$vt.ast,$ve.ast);  
					             var.add(v);                                 
					             if ( hmn.put($vi.text,new STentry(nestingLevel,$vt.ast,varoffset--)) != null  )
					             {System.out.println("Var id "+$vi.text+" at line "+$vi.line+" already declared");
					              System.exit(0);}
					            }	
	                     	SEMIC)* IN
	                     	)? 
	          e=exp 
	          {f.addBody($e.ast);
               //rimuovere la hashmap corrente poiche' esco dallo scope               
               symTable.remove(nestingLevel--);    
              }
        	  SEMIC
        	)*                
            CRPAR
            {symTable.remove(nestingLevel--); }
          )+; 

declist	returns [ArrayList<DecNode> astlist]        
	: {$astlist= new ArrayList<DecNode>() ;
	   int offset= -2;
	  }     
	  ( (
            VAR i=ID COLON ht=hotype ASS e=exp 
            {VarNode v = new VarNode($i.text,$ht.ast,$e.ast);  
             $astlist.add(v);                                 
             HashMap<String,STentry> hm = symTable.get(nestingLevel);
             if(nestingLevel == 0){
             	if ( hm.put($i.text,new STentry(nestingLevel,$ht.ast,globalOffset--)) != null  )
             		{System.out.println("Var id "+$i.text+" at line "+$i.line+" already declared");
             		 System.exit(0);}
              	if($ht.ast instanceof ArrowTypeNode)
              	  	globalOffset--; 
             }else{
             	if ( hm.put($i.text,new STentry(nestingLevel,$ht.ast,offset--)) != null  )
             		{System.out.println("Var id "+$i.text+" at line "+$i.line+" already declared");
              		System.exit(0);}
              	if($ht.ast instanceof ArrowTypeNode)
              		  offset--; 
             }
              
            }  
      |  
            FUN i=ID COLON t=type
              {//inserimento di ID nella symtable
               FunNode f = new FunNode($i.text,$t.ast);      
               $astlist.add(f);                              
               HashMap<String,STentry> hm = symTable.get(nestingLevel);
               STentry entry;
               if(nestingLevel == 0){
               		entry=new STentry(nestingLevel,globalOffset--);
              	  	globalOffset--;
               }else{
               		entry=new STentry(nestingLevel,offset--);
              	  	offset--;
               }
                 
               if ( hm.put($i.text,entry) != null  )
               {System.out.println("Fun id "+$i.text+" at line "+$i.line+" already declared");
                System.exit(0);}
                //creare una nuova hashmap per la symTable
                nestingLevel++;
                HashMap<String,STentry> hmn = new HashMap<String,STentry> ();
                symTable.add(hmn);
                }
              LPAR {ArrayList<Node> parTypes = new ArrayList<Node>();
              	    int paroffset=1;
                    }
                //DA QUI ESERCITAZIONE
                (fid=ID COLON fty=hotype
                  { 
                  parTypes.add($fty.ast);
                  ParNode fpar = new ParNode($fid.text,$fty.ast); //creo nodo ParNode
                  f.addPar(fpar);                                 //lo attacco al FunNode con addPar
                  if($fty.ast instanceof ArrowTypeNode)
              		  paroffset++; 
                  if ( hmn.put($fid.text,new STentry(nestingLevel,$fty.ast,paroffset++)) != null  ) //aggiungo dich a hmn
                  {System.out.println("Parameter id "+$fid.text+" at line "+$fid.line+" already declared");
                   System.exit(0);}
                   
                  }
                  (COMMA id=ID COLON ty=hotype
                    {
                    parTypes.add($ty.ast);
                    ParNode par = new ParNode($id.text,$ty.ast);
                    f.addPar(par);
                    if($ty.ast instanceof ArrowTypeNode)
              			paroffset++; 
                    if ( hmn.put($id.text,new STentry(nestingLevel,$ty.ast,paroffset++)) != null  )
                    {System.out.println("Parameter id "+$id.text+" at line "+$id.line+" already declared");
                     System.exit(0);}
                     
                    }
                  )*
                )? 
                //FINO A QUI ESERCITAZIONE
              RPAR {entry.addType(new ArrowTypeNode(parTypes,$t.ast));
              		f.setSymType(entry.getType());}
              (LET d=declist IN {f.addDec($d.astlist);})? e=exp
              {f.addBody($e.ast);
               //rimuovere la hashmap corrente poiche' esco dallo scope               
               symTable.remove(nestingLevel--);    
              }
      ) SEMIC
    )+          
	;
	
hotype  returns [Node ast]
	: t=type {$ast= $t.ast;}
    | a=arrow {$ast= $a.ast;}
    ;
	
type	returns [Node ast]
  	: INT  {$ast=new IntTypeNode();}
 	| BOOL {$ast=new BoolTypeNode();} 
 	| i=ID   {$ast=new RefTypeNode($i.text);}
  	;	

arrow  returns [Node ast]
 	: LPAR {ArrayList<Node> hoTypes = new ArrayList<Node>();}
 	(h=hotype {hoTypes.add($h.ast);}(COMMA h=hotype {hoTypes.add($h.ast);})* )?
 	 RPAR ARROW t=type {$ast=new ArrowTypeNode(hoTypes,$t.ast);};    

//FINO A QUI

exp	returns [Node ast]
 	: f=term {$ast= $f.ast;}
 	    (PLUS l=term
 	     {$ast= new PlusNode ($ast,$l.ast);}
		 | MINUS l=term
 	     {$ast= new MinusNode ($ast,$l.ast);}
 	     | OR l=term
 	     {$ast= new OrNode ($ast,$l.ast);}
 	    )*
 	;
 	
term	returns [Node ast]
	: f=factor {$ast= $f.ast;}
	    (TIMES l=factor
	     {$ast= new MultNode ($ast,$l.ast);}
	     | DIV l=factor
	     {$ast= new DivNode ($ast,$l.ast);}
	     | AND l=factor
	     {$ast= new AndNode ($ast,$l.ast);}
	    )*
	;
	
factor	returns [Node ast]
	: f=value {$ast= $f.ast;}
	    (EQ l=value 
	     {$ast= new EqualNode ($ast,$l.ast);}
	     |GE l=value 
	     {$ast= new GreatEqualNode ($ast,$l.ast);}
	     |LE l=value 
	     {$ast= new LessEqualNode ($ast,$l.ast);}
	    )*
 	;	 	
 
value	returns [Node ast]
	: n=INTEGER   
	  {$ast= new IntNode(Integer.parseInt($n.text));}  
	| MINUS n=INTEGER   
	  {$ast= new IntNode(Integer.parseInt("-" + $n.text));} 
	| TRUE 
	  {$ast= new BoolNode(true);}  
	| FALSE
	  {$ast= new BoolNode(false);}  
	| NULL
	  {$ast= new EmptyNode();}
	| NEW i=ID 
	  {//cercare la dichiarazione
           STentry entry=(symTable.get(0)).get($i.text);
           if (entry==null)
           {System.out.println("Class Id "+$i.text+" at line "+$i.line+" not declared");
            System.exit(0);}               
	  } 
	  LPAR
	  {ArrayList<Node> arglist = new ArrayList<Node>();} 
	  ( a=exp {arglist.add($a.ast);} 
	   	(COMMA a=exp {arglist.add($a.ast);} )*
	  )? 
	  RPAR
	  {$ast= new NewNode($i.text,entry,arglist,nestingLevel);} 
	   
	| LPAR e=exp RPAR
	  {$ast= $e.ast;}  
	| IF x=exp THEN CLPAR y=exp CRPAR 
		   ELSE CLPAR z=exp CRPAR 
	  {$ast= new IfNode($x.ast,$y.ast,$z.ast);}	 
	| NOT LPAR e=exp RPAR 
	{$ast= new NotNode($e.ast);}
	| PRINT LPAR e=exp RPAR	
	  {$ast= new PrintNode($e.ast);}
	| i=ID 
	  {//cercare la dichiarazione
           int j=nestingLevel;
           STentry entry=null; 
           while (j>=0 && entry==null)
             entry=(symTable.get(j--)).get($i.text);
           if (entry==null)
           {System.out.println("Id "+$i.text+" at line "+$i.line+" not declared");
            System.exit(0);}               
	   $ast= new IdNode($i.text,entry,nestingLevel);} 
		   ( LPAR
		   	 {ArrayList<Node> arglist = new ArrayList<Node>();} 
		   	 ( a=exp {arglist.add($a.ast);} 
		   	 	(COMMA a=exp {arglist.add($a.ast);} )*
		   	 )? 
		   	 RPAR
		   	 {$ast= new CallNode($i.text,entry,arglist,nestingLevel);} 
	   	   | DOT f=ID LPAR
		   	 {ArrayList<Node> arglist = new ArrayList<Node>();} 
		   	 ( a=exp {arglist.add($a.ast);} 
		   	 	(COMMA a=exp {arglist.add($a.ast);} )*
		   	 )? 
		   	 RPAR
		   	 {
		   	 	if(!(entry.getType() instanceof RefTypeNode))
		   	 	{System.out.println("Id "+ $i.text +" at line "+$i.line+" is not a object reference");
	              System.exit(0);}  
		   	   RefTypeNode rtype = (RefTypeNode) entry.getType();
		   	   HashMap<String,STentry> cr = classTable.get(rtype.getId());
		   	   if (cr==null)
	             {System.out.println("Rference "+ rtype.getId() +" at line "+$i.line+" not found");
	              System.exit(0);}  
	           STentry methodEntry = cr.get($f.text);
	           if (methodEntry==null)
	             {System.out.println("Method "+$f.text+" at line "+$f.line+" not declared");
	              System.exit(0);}      
		   	 	$ast= new ClassCallNode($i.text,entry,methodEntry,arglist,nestingLevel);} 
		   )?
 	; 

  		
/*------------------------------------------------------------------
 * LEXER RULES
 *------------------------------------------------------------------*/

PLUS  	: '+' ;
MINUS   : '-' ;
TIMES   : '*' ;
DIV 	: '/' ;
LPAR	: '(' ;
RPAR	: ')' ;
CLPAR	: '{' ;
CRPAR	: '}' ;
SEMIC 	: ';' ;
COLON   : ':' ; 
COMMA	: ',' ;
DOT	    : '.' ;
OR	    : '||';
AND	    : '&&';
NOT	    : '!' ;
GE	    : '>=' ;
LE	    : '<=' ;
EQ	    : '==' ;	
ASS	    : '=' ;
TRUE	: 'true' ;
FALSE	: 'false' ;
IF	    : 'if' ;
THEN	: 'then';
ELSE	: 'else' ;
PRINT	: 'print' ;
LET     : 'let' ;	
IN      : 'in' ;	
VAR     : 'var' ;
FUN	    : 'fun' ; 
CLASS	: 'class' ; 
EXTENDS : 'extends' ;	
NEW 	: 'new' ;	
NULL    : 'null' ;	  
INT	    : 'int' ;
BOOL	: 'bool' ;
ARROW   : '->' ; 	
INTEGER : '0' | (('1'..'9')('0'..'9')*) ; 

ID  	: ('a'..'z'|'A'..'Z')('a'..'z' | 'A'..'Z' | '0'..'9')* ;


WHITESP  : ( '\t' | ' ' | '\r' | '\n' )+    -> channel(HIDDEN) ;

COMMENT : '/*' (.)*? '*/' -> channel(HIDDEN) ;
 
ERR   	 : . { System.out.println("Invalid char: "+ getText()); lexicalErrors++; } -> channel(HIDDEN); 
