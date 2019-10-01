/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import exceptions.EvaluationException;
import exceptions.MisplacedTokensException;
import exceptions.TooManyDecimalPointsException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import kktpm.KKTPMCalculator;
import static kktpm.KKTPMCalculator.getAdjustedKKTPM;
import static kktpm.KKTPMCalculator.getDirectKKTPM;
import static kktpm.KKTPMCalculator.getKKTPM;
import static kktpm.KKTPMCalculator.getProjectedKKTPM;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import parsing.OptimizationProblem;
import parsing.XMLParser;
import static kktpm.KKTPMCalculator.getAdjustedKKTPM;
import static kktpm.KKTPMCalculator.getDirectKKTPM;
import static kktpm.KKTPMCalculator.getKKTPM;
import static kktpm.KKTPMCalculator.getProjectedKKTPM;
import static kktpm.KKTPMCalculator.getAdjustedKKTPM;
import static kktpm.KKTPMCalculator.getDirectKKTPM;
import static kktpm.KKTPMCalculator.getKKTPM;
import static kktpm.KKTPMCalculator.getProjectedKKTPM;
import static kktpm.KKTPMCalculator.getAdjustedKKTPM;
import static kktpm.KKTPMCalculator.getDirectKKTPM;
import static kktpm.KKTPMCalculator.getKKTPM;
import static kktpm.KKTPMCalculator.getProjectedKKTPM;
import static kktpm.KKTPMCalculator.getAdjustedKKTPM;
import static kktpm.KKTPMCalculator.getDirectKKTPM;
import static kktpm.KKTPMCalculator.getKKTPM;
import static kktpm.KKTPMCalculator.getProjectedKKTPM;
import static kktpm.KKTPMCalculator.getAdjustedKKTPM;
import static kktpm.KKTPMCalculator.getDirectKKTPM;
import static kktpm.KKTPMCalculator.getKKTPM;
import static kktpm.KKTPMCalculator.getProjectedKKTPM;
import static kktpm.KKTPMCalculator.getAdjustedKKTPM;
import static kktpm.KKTPMCalculator.getDirectKKTPM;
import static kktpm.KKTPMCalculator.getKKTPM;
import static kktpm.KKTPMCalculator.getProjectedKKTPM;
import static kktpm.KKTPMCalculator.getAdjustedKKTPM;
import static kktpm.KKTPMCalculator.getDirectKKTPM;
import static kktpm.KKTPMCalculator.getKKTPM;
import static kktpm.KKTPMCalculator.getProjectedKKTPM;

/**
 *
 * @author Haitham
 */
public class Test {

    /**
     * Just for testing
     *
     * @param args unused
     * @throws java.lang.Throwable
     */
    public static void main(String[] args) throws Throwable {
//        // ====================================================================
//        // Using raw input data
//        // --------------------------------------------------------------------
//        System.out.println("----------------------------------");
//        System.out.println(" Using Raw Input ");
//        System.out.println("----------------------------------");
//        calculateUsingRawData();
//        // ====================================================================
//        // Using an optimization problem object
//        // --------------------------------------------------------------------
//        System.out.println("----------------------------------");
//        System.out.println(" Using OptimizationProblem Object ");
//        System.out.println("----------------------------------");
//        calculateUsingProblemObject();
//        // ====================================================================
//        // Calculate Lagrange Multipliers Independently
//        // --------------------------------------------------------------------
//        System.out.println("----------------------------------");
//        System.out.println(" Using Lagrange Multipliers ");
//        System.out.println("----------------------------------");
//        //calculateLagrangeIndependently();
    	calculateUsingProblemObject();
    }

    private static void calculateUsingRawData() {
        // Raw input data
        double[] x = new double[]{1};
        double[] f = new double[]{1};
        double[] z = null;
        double[] g = new double[]{-0.5};
        double[][] jacobianF = {{2}}; // Two functions & three variables
        double[][] jacobianG = {{-1}}; // Four constraints & three variables
        // Calculations
        double kktpmDirect = getDirectKKTPM(x, f, z, g, jacobianF, jacobianG);
        double kktpmAdjusted = getAdjustedKKTPM(x, f, z, g, jacobianF, jacobianG);
        double kktpmProjected = getProjectedKKTPM(x, f, z, g, jacobianF, jacobianG);
        double kktpm = getKKTPM(x, f, z, g, jacobianF, jacobianG);
        // Display results
        System.out.format("%12s  = %10.6f%n", "Direct KKTPM", kktpmDirect);
        System.out.format("%12s  = %10.6f%n", "Adj. KKTPM", kktpmAdjusted);
        System.out.format("%12s  = %10.6f%n", "Proj. KKTPM", kktpmProjected);
        System.out.format("%12s  = %10.6f%n", "KKTPM", kktpm);
    }

    private static void calculateUsingProblemObject() throws
            Throwable,
            TooManyDecimalPointsException,
            MisplacedTokensException,
            EvaluationException {
        // Create an optimization problem object
        double[] x = new double[]{1};
        OptimizationProblem problem = new OptimizationProblem();
        for (int i = 0; i < x.length; i++) {
            problem.setVariable("x" + (i + 1), x[i]);
        }
        problem.addObjective("x1^2");
        problem.addConstraint("0.5-x1");
        problem.setObjectivePartialDerivative(0, "x1", "2*x1");
        problem.setConstraintPartialDerivative(0, "x1", "-1");
        // Caclulations
        double kktpmDirect = KKTPMCalculator.getDirectKKTPM(problem, null).getKktpm();
        double kktpmAdjusted = KKTPMCalculator.getAdjustedKKTPM(problem, null).getKktpm();
        double kktpmProjected = KKTPMCalculator.getProjectedKKTPM(problem, null).getKktpm();
        double kktpm = KKTPMCalculator.getKKTPM(problem, null).getKktpm();
        // Display results
        System.out.format("%12s  = %10.6f%n", "Direct KKTPM", kktpmDirect);
        System.out.format("%12s  = %10.6f%n", "Adj. KKTPM", kktpmAdjusted);
        System.out.format("%12s  = %10.6f%n", "Proj. KKTPM", kktpmProjected);
        System.out.format("%12s  = %10.6f%n", "KKTPM", kktpm);
    }

//    private static void calculateLagrangeIndependently() throws
//            DimensionMismatchException,
//            MisplacedTokensException,
//            TooManyDecimalPointsException,
//            EvaluationException,
//            NotPositiveException,
//            OutOfRangeException,
//            Throwable {
//        // Create an optimization problem object
//        double[] z = null;
//        double[] x = new double[]{1};
//        double[] f = new double[]{1};
//        double[] g = new double[]{-0.5};
//        OptimizationProblem problem = new OptimizationProblem();
//        for (int i = 0; i < x.length; i++) {
//            problem.setVariable("x" + (i + 1), x[i]);
//        }
//        problem.addObjective("x1^2");
//        problem.addConstraint("0.5-x1");
//        problem.setObjectivePartialDerivative(0, "x1", "2*x1");
//        problem.setConstraintPartialDerivative(0, "x1", "-1");
//        // Calculate Lagrange multipliers
//        double[] u = KKTPMCalculator.getLagrangeMultiplers(problem, z);
//        // Caclulations (using the calculated Lagrange multipliers)
//        double kktpmDirect = KKTPMCalculator.getDirectKKTPM(f, g, u);
//        double kktpmAdjusted = KKTPMCalculator.getAdjustedKKTPM(f, g, u);
//        double kktpmProjected = KKTPMCalculator.getProjectedKKTPM(f, g, u, kktpmDirect);
//        double kktpm = KKTPMCalculator.getKKTPM(f, g, u);
//        // Display results
//        System.out.format("%12s  = %10.6f%n", "Direct KKTPM", kktpmDirect);
//        System.out.format("%12s  = %10.6f%n", "Adj. KKTPM", kktpmAdjusted);
//        System.out.format("%12s  = %10.6f%n", "Proj. KKTPM", kktpmProjected);
//        System.out.format("%12s  = %10.6f%n", "KKTPM", kktpm);
//    }
    public static void calculateKktpm() throws TooManyDecimalPointsException, MisplacedTokensException, Throwable {
        // Problem file
        String problemFilePath = "E:\\KKTPM\\Java KKTPM\\XML\\osy.xml";
        // Population file
        String populationFilePath = "F:\\POST-GECCO-2016\\Validating Matlab vs Java\\OSY\\osy_var_gen_0025.dat";
        // Output file
        String outFile = "F:\\POST-GECCO-2016\\Validating Matlab vs Java\\OSY\\osy_kktpms_java.txt";
        // Ideal (Utopian) point
        double[] z = {-300, -0.05};
        // ---------------------------------------------------------------------
        // Load the problem
        OptimizationProblem problem = XMLParser.readXML(new File(problemFilePath));
        // Set the constant PI
        problem.setConstant("pi", Math.PI);
        problem.setConstant("e", Math.E);
        // Display the loaded problem
        System.out.println(problem);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(populationFilePath));
            List<Double> kktpmList = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] splits = line.split(" ");
                double[] x = new double[splits.length];
                for (int i = 0; i < splits.length; i++) {
                    x[i] = Double.parseDouble(splits[i].trim());
                }
                // -----------------------
                // ONLY FOR ZDT-3
//                if(x[0] < 0.000001) {
//                    x[0] = 0.000001;
//                }
                // -----------------------
                // Set x
                problem.setAllVariables(x);
                // Calculate KKTPM
                double kktpm = KKTPMCalculator.getKKTPM(problem, z, 0.001).getKktpm();
                System.out.println(kktpm);
                kktpmList.add(kktpm);
            }
            // write the reusults to a file
            write(kktpmList, outFile);
        } catch (IOException ex) {
            System.out.println("ERROR>> " + ex.toString());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private static void write(List<Double> numList, String filePath)
            throws IOException {
        PrintWriter printer = null;
        try {
            printer = new PrintWriter(filePath);
            for (Double num : numList) {
                printer.format("%9.8f%n", num);
            }
        } finally {
            if (printer != null) {
                printer.close();
            }
        }
    }
}
