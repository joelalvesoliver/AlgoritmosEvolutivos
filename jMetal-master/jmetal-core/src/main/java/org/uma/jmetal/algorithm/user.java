package org.uma.jmetal.algorithm;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.Solution;

public class user <S extends Solution<?>>{
	private String algoritmo;
	private ArrayList Solucoes;
    private ArrayList Objectivos;
    
    
    public user() {}
    
    public user(String name, ArrayList Solucoes, ArrayList Objectivos) {
        this.algoritmo = name;
        this.Solucoes = Solucoes;
        this.Objectivos = Objectivos;
    }
    
    public String getAlgoritmo() {
    	return algoritmo;
    }
    
    public ArrayList getSolucoes() {
    	return Solucoes;
    }
    
    public ArrayList getObjetivos()
    {
    	return Objectivos;
    }
    
    public String toString() {
    	return "Name: "+this.algoritmo+", Solucoes: "+this.Solucoes+", Objetivos: "+this.Objectivos+"";
    }
    
}
