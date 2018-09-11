package org.uma.jmetal.algorithm.multiobjective.nsgaiii;
import java.util.ArrayList;


public class retorno {
	
	    private ArrayList retorno;
	    
	    
	    public retorno() {}
	    
	    public retorno(ArrayList retorno) {
	        
	        this.retorno = retorno;
	        //this.isDeveloper = developed;
	    }
	    
	    public ArrayList getRetorno()
	    {
	    	return retorno;
	    }
	    
	    public String toString() {
	    	return "retorno: "+this.retorno+"";
	    }
}
