package org.uma.jmetal.experiment;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.uma.jmetal.algorithm.multiobjective.nsgaiii.util.ReferencePoint;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.fileoutput.FileOutputContext;
import org.uma.jmetal.util.fileoutput.impl.DefaultFileOutputContext;

public class RefPointsGenerator {
	public static ArrayList<double[]> getReferencePointsCopy(int n_objectives, Vector<Integer> numberOfDivisions ) {
		  List<ReferencePoint<DoubleSolution>> referencePoints = new Vector<>() ;
		  ArrayList<double[]> pontos = new ArrayList<>();
		  (new ReferencePoint<DoubleSolution>()).generateReferencePoints(referencePoints, n_objectives , numberOfDivisions);
		  for (ReferencePoint<DoubleSolution> r : referencePoints) {
				double[] ponto = new double[n_objectives];
				for(int i = 0; i < n_objectives; i++) {
					ponto[i] = r.position.get(i);
				}
				pontos.add(ponto);
			}
		  return pontos;
	 }
	public static void main(String[] args) {
		Vector<Integer> numberOfDivisions = new Vector<>(1);
		numberOfDivisions.add(12);
		ArrayList<double[]> pontos = RefPointsGenerator.getReferencePointsCopy(10, numberOfDivisions);
		for (double[] ds : pontos) {
			System.out.println(Arrays.toString(ds));
		}
  	
	}
}