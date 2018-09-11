package org.uma.jmetal.algorithm.multiobjective.nsgaiii;

import java.util.ArrayList;
import java.util.List;

import org.uma.jmetal.solution.Solution;

public class user <S extends Solution<?>>{
	private String algoritmo;
	private String processar;
	private ArrayList Solucoes;
    private ArrayList Objectivos;
    
    
    public user() {}
    
    public user(String name,String processar,ArrayList Solucoes, ArrayList Objectivos) {
        this.algoritmo = name;
        this.processar = processar;
        this.Solucoes = Solucoes;
        this.Objectivos = Objectivos;
    }
    
    public String getAlgoritmo() {
    	return algoritmo;
    }
    public String getProcessar() {
    	return processar;
    }
    
    public ArrayList getSolucoes() {
    	return Solucoes;
    }
    
    public ArrayList getObjetivos()
    {
    	return Objectivos;
    }
    
    public String toString() {
    	return "Name: "+this.algoritmo+", Processar: "+this.processar+", Solucoes: "+this.Solucoes+", Objetivos: "+this.Objectivos+"";
    }
    
}