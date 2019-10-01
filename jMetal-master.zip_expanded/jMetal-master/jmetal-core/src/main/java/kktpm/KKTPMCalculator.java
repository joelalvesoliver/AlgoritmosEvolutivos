package kktpm;


import exceptions.EvaluationException;
import exceptions.MisplacedTokensException;
import exceptions.TooManyDecimalPointsException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NotPositiveException;
import org.apache.commons.math3.exception.OutOfRangeException;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import parsing.Derivative;
import parsing.KKTPM;
import parsing.LagrangeMultipliers;
import parsing.OptimizationProblem;

/**
 * This class provides a set of methods for calculating all approximations of
 * KKTPM (Karush Kuhn Tucker Proximity Measure). The approximations provided are
 * Direct, Adjusted, Projected and Approximate (which is the average of the
 * former three, and which is simply called KKTPM). As a byproduct the set of
 * Lagrange multipliers is also calculated. The methods provided by this class
 * accepts vectors (in the form of single dimensional arrays), matrices (in the
 * form of two-dimensional arrays) and OptimizationProblem objects.
 *
 * @author Haitham
 */
public class KKTPMCalculator {

    private static String zErrorMessage
            = "The ideal point vector must be either null (for single "
            + "objective problems) or equal in length to the "
            + "number of objectives.";

    // <editor-fold defaultstate="collapsed" desc="Lagrange Multipliers">
    public static LagrangeMultipliers getLagrangeMultiplers(
            OptimizationProblem problem,
            double[] z) throws EvaluationException,
            TooManyDecimalPointsException,
            MisplacedTokensException {
        return getLagrangeMultiplers(problem, z, 0.0);
    }

    /**
     * Calculates the set of Lagrange multipliers for the given optimization
     * problem with respect to the given ideal point. Notice that the returned
     * values are valid only for the current set point (in the problem object).
     * If the current point changed, the "already returned" Lagrange multipliers
     * will no longer represent the problem object.
     *
     * @param problem encapsulates all the attributes of an optimization
     * problem.
     * @param z the ideal point (need not be the true ideal point of the
     * problem)
     * @return an array of Lagrange multipliers.
     * @throws EvaluationException
     * @throws TooManyDecimalPointsException
     * @throws MisplacedTokensException
     */
    public static LagrangeMultipliers getLagrangeMultiplers(
            OptimizationProblem problem,
            double[] z,
            double rho) throws EvaluationException,
            TooManyDecimalPointsException,
            MisplacedTokensException {
        // The ideal point must be either null (for single objective problems)
        // or equal in length to the number of objectives.
        if (z != null && z.length != problem.getObjectivesCount()) {
            throw new IllegalArgumentException(zErrorMessage);
        }
        // Create the datastructures required for storing problme information.
        int varCount = problem.getTotalVariablesCount();
        int objCount = problem.getObjectivesCount();
        int conCount = problem.getConstraintsCount();
        double[] x = new double[varCount];
        double[] f = new double[objCount];
        double[] g = new double[conCount];
        double[][] jacobianF
                = new double[objCount][varCount];
        double[][] jacobianG
                = new double[conCount][varCount];
        // Extract problem information
        int numericalFunEval = extractProblemInfo(problem, x, objCount, f, conCount, g, jacobianF, jacobianG);
        // Calculate and return Lagrange multipliers
        double[] lagrangeMultiplers = getLagrangeMultiplers(
                x,
                f,
                z,
                g,
                jacobianF,
                jacobianG,
                rho);
        return new LagrangeMultipliers(lagrangeMultiplers, numericalFunEval);
    }

    /**
     * Calculates and returns the set of Lagrange multipliers at the specified
     * point. (assumes rho = 0.0)
     *
     * @param x the specified at which the direct KKTPM should be calculated.
     * @param f objective functions values at the specified point.
     * @param z the ideal point used to calculate the direct KKTPM.
     * @param g constraints values at the specified point.
     * @param jacobianF partial derivatives of all objectives with respect to
     * all variables at the specified point.
     * @param jacobianG partial derivatives of all constraints with respect to
     * all variables at the specified point.
     * @return the vector of Lagrange multipliers at the specified point
     */
    public static double[] getLagrangeMultiplers(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG) {
        return getLagrangeMultiplers(x, f, z, g, jacobianF, jacobianG, 0.0);
    }

    /**
     * Calculates and returns the set of Lagrange multipliers at the specified
     * point.
     *
     * @param x the specified at which the direct KKTPM should be calculated.
     * @param f objective functions values at the specified point.
     * @param z the ideal point used to calculate the direct KKTPM.
     * @param g constraints values at the specified point.
     * @param jacobianF partial derivatives of all objectives with respect to
     * all variables at the specified point.
     * @param jacobianG partial derivatives of all constraints with respect to
     * all variables at the specified point.
     * @param rho Augmented ASF (AASF) parameter
     * @return the vector of Lagrange multipliers at the specified point
     */
    public static double[] getLagrangeMultiplers(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG,
            double rho) {

        if (z == null) {
            if (f.length != 1) {
                throw new IllegalArgumentException(zErrorMessage);
            }
        } else if (z.length != f.length) {
            throw new IllegalArgumentException(zErrorMessage);
        }
        RealVector xv = new ArrayRealVector(x);
        RealVector fv = new ArrayRealVector(f);
        RealVector gv = new ArrayRealVector(g);
        // Calculate the weight
        RealVector w;
        if (fv.getDimension() == 1) {
            w = new ArrayRealVector(new double[]{1});
        } else {
            RealVector zv = new ArrayRealVector(z);
            RealVector zf = fv.subtract(zv);
            double norm = zf.getNorm();
            w = zf.mapDivide(norm);
        }
        // Objective partial derivatives
        RealMatrix jacobianFMatrix = MatrixUtils.createRealMatrix(jacobianF);
        // Constraints partial derivatives
        RealMatrix jacobianGMatrix = MatrixUtils.createRealMatrix(jacobianG);
        // Form A_m
        RealMatrix am = MatrixUtils.createRealMatrix(fv.getDimension(), xv.getDimension());
        for (int i = 0; i < jacobianFMatrix.getRowDimension(); i++) {
            RealVector amRow = jacobianFMatrix.getRowVector(i).mapMultiply(1 / w.getEntry(i));
            // AASF (Augmented ASF)
            RealVector aasfTermVector = new ArrayRealVector(xv.getDimension());
            for (int j = 0; j < aasfTermVector.getDimension(); j++) {
                RealVector ones = new ArrayRealVector(/*aasfTermVector.getDimension()*/w.getDimension(), 1);
                RealVector oneOverW = ones.ebeDivide(w);
                aasfTermVector.setEntry(j, rho * oneOverW.dotProduct(jacobianFMatrix.getColumnVector(j)));
            }
            am.setRowVector(i, amRow.add(aasfTermVector));
        }

//        // TO-BE-REMOVED-START
//        System.out.println("am_java = [ ...");
//        for (int i = 0; i < am.getRowDimension(); i++) {
//            for (int j = 0; j < am.getColumnDimension(); j++) {
//                System.out.format("%15.10f", am.getEntry(i, j));
//                if(i == am.getRowDimension() - 1 && j == am.getColumnDimension() - 1) {
//                    System.out.println("];");
//                } else if(j == am.getColumnDimension() - 1) {
//                    System.out.println("; ...");
//                } else {
//                    System.out.print(" ");
//                }
//            }
//        }
//        // TO-BE-REMOVED-END
        
        // Form A_j
        RealMatrix aj = MatrixUtils.createRealMatrix(gv.getDimension(), xv.getDimension());
        for (int i = 0; i < jacobianGMatrix.getRowDimension(); i++) {
            aj.setRowVector(i, jacobianGMatrix.getRowVector(i));
        }

//        // TO-BE-REMOVED-START
//        System.out.println("aj_java = [ ...");
//        for (int i = 0; i < aj.getRowDimension(); i++) {
//            for (int j = 0; j < aj.getColumnDimension(); j++) {
//                System.out.format("%15.10f", aj.getEntry(i, j));
//                if(i == aj.getRowDimension() - 1 && j == aj.getColumnDimension() - 1) {
//                    System.out.println("];");
//                } else if(j == aj.getColumnDimension() - 1) {
//                    System.out.println("; ...");
//                } else {
//                    System.out.print(" ");
//                }
//            }
//        }
//        // TO-BE-REMOVED-END

        // Form the big matrix used later for factorization
        // Start by forming the four sub-matrices
        RealMatrix topLeft = am.multiply(am.transpose()).add(
                MatrixUtils.createRealMatrix(
                        fv.getDimension(), fv.getDimension()).scalarAdd(1));
        RealMatrix topRight = am.multiply(aj.transpose());
        RealMatrix bottomLeft = aj.multiply(am.transpose());
        RealMatrix bottomRight = aj.multiply(aj.transpose()).add(MatrixUtils.createRealDiagonalMatrix(gv.ebeMultiply(gv).toArray()));
        // Create an empty big matrix to hold the four sub-matrices
        RealMatrix bigA = MatrixUtils.createRealMatrix(
                topLeft.getRowDimension() + bottomLeft.getRowDimension(),
                topLeft.getColumnDimension() + topRight.getColumnDimension());
        // Combine the four sub-matrices into the big matrix.
        for (int i = 0; i < bigA.getRowDimension(); i++) {
            for (int j = 0; j < bigA.getColumnDimension(); j++) {
                RealMatrix matrix; // Which matrix of the four will you copy from
                int iOffset; // Used for mapping (i) to the designated row in (matrix)
                int jOffset; // Used for mapping (j) to the designated column in (matrix)
                // Determine (matrix), (iOffset) and (jOffset) based on (i) and (j).
                if (i < topLeft.getRowDimension()) {
                    iOffset = 0;
                    if (j < topLeft.getColumnDimension()) {
                        jOffset = 0;
                        matrix = topLeft;
                    } else {
                        jOffset = topLeft.getColumnDimension();
                        matrix = topRight;
                    }
                } else {
                    iOffset = topLeft.getRowDimension();
                    if (j < topLeft.getColumnDimension()) {
                        jOffset = 0;
                        matrix = bottomLeft;
                    } else {
                        jOffset = topLeft.getColumnDimension();
                        matrix = bottomRight;
                    }
                }
                // Fill the next position in (bigA)
                bigA.setEntry(i, j, matrix.getEntry(i - iOffset, j - jOffset));
            }
        }
        // Create (b), the right-hand-side vector
        RealVector b = new ArrayRealVector(fv.getDimension() + gv.getDimension());
        for (int i = 0; i < fv.getDimension(); i++) {
            b.addToEntry(i, 1);
        }
        // Solve the system of linear equations (bigA)(u) = (b) so that each
        // multiplier must be non-negative.
        int tmpCounter = 0;
        RealVector u;
        while (true) {
            // Solve
            DecompositionSolver solver = new LUDecomposition(bigA, 1e-100).getSolver();

            // Solve the system of linear equations
            u = solver.solve(b);

//            // TO-BE-REMOVED-START
//            System.out.print("u = [");
//            for (int i = 0; i < u.getDimension(); i++) {
//                System.out.format("%20.15f", u.getEntry(i));
//                if (i == u.getDimension() - 1) {
//                    System.out.println("];");
//                } else {
//                    System.out.print(" ");
//                }
//            }
//            // TO-BE-REMOVED-END
            // If a negative multiplier exists, remove its equation and re-solve.
            int firstNonZeroIndex = getFirstNegativeMultipierIndex(u);
            if (firstNonZeroIndex == -1) {
                break;
            } else {
                // Set all corresponding row values to Zero
                for (int i = 0; i < bigA.getColumnDimension(); i++) {
                    bigA.setEntry(firstNonZeroIndex, i, 0);
                }
                // Set all corresponding column values to zero
                for (int i = 0; i < bigA.getRowDimension(); i++) {
                    bigA.setEntry(i, firstNonZeroIndex, 0);
                }
                // Set the intersection between column and row to one (for some reason)
                bigA.setEntry(firstNonZeroIndex, firstNonZeroIndex, 1);
                // Set the corresponding value in vector b to Zero
                b.setEntry(firstNonZeroIndex, 0);
            }
            tmpCounter++;
        }
        // Return vector (u)
        return u.toArray();
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Direct KKTPM">
    /**
     * Calculates and returns the direct KKTPM (Karush Khun Tucker Proximity
     * Measure) at the specified point (x-vector). (assumes rho = 0.0)
     *
     * @param problem encapsulates all the attributes of an optimization
     * problem.
     * @param z the ideal point (need not be the true ideal point of the
     * problem)
     * @return value of the direct KKTPM
     * @throws EvaluationException
     * @throws TooManyDecimalPointsException
     * @throws MisplacedTokensException
     */
    public static KKTPM getDirectKKTPM(
            OptimizationProblem problem,
            double[] z) throws
            EvaluationException,
            TooManyDecimalPointsException,
            MisplacedTokensException {
        return getDirectKKTPM(problem, z, 0.0);
    }

    /**
     * Calculates and returns the direct KKTPM (Karush Khun Tucker Proximity
     * Measure) at the specified point (x-vector).
     *
     * @param problem encapsulates all the attributes of an optimization
     * problem.
     * @param z the ideal point (need not be the true ideal point of the
     * problem)
     * @param rho Augmented ASF parameter
     * @return value of the direct KKTPM
     * @throws EvaluationException
     * @throws TooManyDecimalPointsException
     * @throws MisplacedTokensException
     */
    public static KKTPM getDirectKKTPM(
            OptimizationProblem problem,
            double[] z,
            double rho) throws
            EvaluationException,
            TooManyDecimalPointsException,
            MisplacedTokensException {
        // The ideal point must be either null (for single objective problems)
        // or equal in length to the number of objectives.
        if (z != null && z.length != problem.getObjectivesCount()) {
            throw new IllegalArgumentException(zErrorMessage);
        }
        // Create the datastructures required for storing problme information.
        int varCount = problem.getTotalVariablesCount();
        int objCount = problem.getObjectivesCount();
        int conCount = problem.getConstraintsCount();
        double[] x = new double[varCount];
        double[] f = new double[objCount];
        double[] g = new double[conCount];
        double[][] jacobianF
                = new double[objCount][varCount];
        double[][] jacobianG
                = new double[conCount][varCount];
        // Extract problem information
        int numericalFunEval = extractProblemInfo(problem, x, objCount, f, conCount, g, jacobianF, jacobianG);
        // Calculate and return the final KKTPM
        double directKKTPM = getDirectKKTPM(x, f, z, g, jacobianF, jacobianG, rho);
        return new KKTPM(directKKTPM, numericalFunEval);
    }

    /**
     * Calculates direct KKTPM at the specified point. (assumes rho = 0.0)
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @return direct KKTPM
     */
    public static double getDirectKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG) {
        return getDirectKKTPM(x, f, z, g, jacobianF, jacobianG, 0.0);
    }

    /**
     * Calculates direct KKTPM at the specified point.
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @param rho Augmented ASF (AASF) parameter
     * @return direct KKTPM
     */
    public static double getDirectKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG,
            double rho) {
        double[] u = getLagrangeMultiplers(x, f, z, g, jacobianF, jacobianG, rho);
        //return getDirectKKTPM(f, g, u);
        return getDirectKKTPM(x, f, z, g, jacobianF, jacobianG, u, rho);
    }

//    /**
//     * Calculates direct KKTPM, given objectives, constraints and Lagrange
//     * multipliers.
//     *
//     * @param f the specified point in objective space
//     * @param g constraints values
//     * @param u Lagrange multipliers
//     * @return direct KKTPM
//     */
//    public static double getDirectKKTPM(
//            double[] f,
//            double[] g,
//            double[] u) throws
//            DimensionMismatchException,
//            OutOfRangeException,
//            NotPositiveException {
//        RealVector fv = new ArrayRealVector(f);
//        RealVector gv = new ArrayRealVector(g);
//        RealVector uv = new ArrayRealVector(u);
//        RealVector ones = new ArrayRealVector(fv.getDimension()).mapAdd(1);
//        double kktpmDirect = 1
//                - ones.dotProduct(uv.getSubVector(0, fv.getDimension()))
//                - Math.pow(gv.dotProduct(uv.getSubVector(
//                        fv.getDimension(), gv.getDimension())), 2);
//        return kktpmDirect;
//    }
    /**
     * Calculates direct KKTPM, given objectives, constraints and Lagrange
     * multipliers. (assumes rho = 0.0)
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @param u Lagrange multipliers
     * @return direct KKTPM
     */
    public static double getDirectKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG,
            double[] u) throws
            DimensionMismatchException,
            OutOfRangeException,
            NotPositiveException {
        return getDirectKKTPM(x, f, z, g, jacobianF, jacobianG, u, 0.0);
    }

    /**
     * Calculates direct KKTPM, given objectives, constraints and Lagrange
     * multipliers.
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @param u Lagrange multipliers
     * @param rho Augmented ASF (AASF) parameter
     * @return direct KKTPM
     */
    public static double getDirectKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG,
            double[] u,
            double rho) throws
            DimensionMismatchException,
            OutOfRangeException,
            NotPositiveException {
        RealVector xv = new ArrayRealVector(x);
        RealVector fv = new ArrayRealVector(f);
        RealVector gv = new ArrayRealVector(g);
        RealVector uv = new ArrayRealVector(u);

        // Calculate the weight
        RealVector w;
        if (fv.getDimension() == 1) {
            w = new ArrayRealVector(new double[]{1});
        } else {
            RealVector zv = new ArrayRealVector(z);
            RealVector zf = fv.subtract(zv);
            double norm = zf.getNorm();
            w = zf.mapDivide(norm);
        }
        // Objective partial derivatives
        RealMatrix jacobianFMatrix = MatrixUtils.createRealMatrix(jacobianF);
        // Constraints partial derivatives
        RealMatrix jacobianGMatrix = MatrixUtils.createRealMatrix(jacobianG);
        // Form A_m
        RealMatrix am = MatrixUtils.createRealMatrix(fv.getDimension(), xv.getDimension());
        for (int i = 0; i < jacobianFMatrix.getRowDimension(); i++) {
            RealVector amRow = jacobianFMatrix.getRowVector(i).mapMultiply(1 / w.getEntry(i));
            // AASF (Augmented ASF)
            RealVector aasfTermVector = new ArrayRealVector(xv.getDimension());
            for (int j = 0; j < aasfTermVector.getDimension(); j++) {
                RealVector ones = new ArrayRealVector(/*aasfTermVector.getDimension()*/w.getDimension(), 1);
                RealVector oneOverW = ones.ebeDivide(w);
                aasfTermVector.setEntry(j, rho * oneOverW.dotProduct(jacobianFMatrix.getColumnVector(j)));
            }
            am.setRowVector(i, amRow.add(aasfTermVector));
        }
        // Form A_j
        RealMatrix aj = MatrixUtils.createRealMatrix(gv.getDimension(), xv.getDimension());
        for (int i = 0; i < jacobianGMatrix.getRowDimension(); i++) {
            aj.setRowVector(i, jacobianGMatrix.getRowVector(i));
        }

        // Get um out of uv
        RealVector um = uv.getSubVector(0, fv.getDimension());
        // Get the summation of elements in um
        double umSum = 0;
        for (int i = 0; i < um.getDimension(); i++) {
            umSum += um.getEntry(i);
        }
        // Form the matrix [am;aj]
        RealMatrix amaj = MatrixUtils.createRealMatrix(
                am.getRowDimension() + aj.getRowDimension(),
                am.getColumnDimension());
        for (int i = 0; i < am.getRowDimension(); i++) {
            for (int j = 0; j < am.getColumnDimension(); j++) {
                amaj.setEntry(i, j, am.getEntry(i, j));
            }
        }
        for (int i = 0; i < aj.getRowDimension(); i++) {
            for (int j = 0; j < aj.getColumnDimension(); j++) {
                amaj.setEntry(i + am.getRowDimension(), j, aj.getEntry(i, j));
            }
        }
        // Perform the multiplication [am;aj]'*uvecn
        RealVector tempV = amaj.transpose().operate(uv);
        // Sum the squares of the values in tempV
        double sumV = 0;
        for (int i = 0; i < tempV.getDimension(); i++) {
            sumV += Math.pow(tempV.getEntry(i), 2);
        }
        // Calculate and return the final Direct KKTPM
        double kktpmDirect;
        // DOES IT WORK FOR BOTH CONSTRAINED AND UNCONSTRAINED ?! (CHECK REQUIRED)
        kktpmDirect = Math.pow(1 - umSum, 2) + sumV;
        return kktpmDirect;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Projected KKTPM">
    /**
     * Calculates and returns the projected KKTPM (Karush Khun Tucker Proximity
     * Measure) at the specified point (x-vector). (assumes rho = 0.0)
     *
     * @param problem encapsulates all the attributes of an optimization
     * problem.
     * @param z the ideal point (need not be the true ideal point of the
     * problem)
     * @return value of the direct KKTPM
     * @throws EvaluationException
     * @throws TooManyDecimalPointsException
     * @throws MisplacedTokensException
     */
    public static KKTPM getProjectedKKTPM(
            OptimizationProblem problem,
            double[] z) throws
            EvaluationException,
            TooManyDecimalPointsException,
            MisplacedTokensException {
        return getProjectedKKTPM(problem, z, 0.0);
    }

    /**
     * Calculates and returns the projected KKTPM (Karush Khun Tucker Proximity
     * Measure) at the specified point (x-vector).
     *
     * @param problem encapsulates all the attributes of an optimization
     * problem.
     * @param z the ideal point (need not be the true ideal point of the
     * problem)
     * @param rho Augmented ASF parameter
     * @return value of the direct KKTPM
     * @throws EvaluationException
     * @throws TooManyDecimalPointsException
     * @throws MisplacedTokensException
     */
    public static KKTPM getProjectedKKTPM(
            OptimizationProblem problem,
            double[] z,
            double rho) throws
            EvaluationException,
            TooManyDecimalPointsException,
            MisplacedTokensException {
        // The ideal point must be either null (for single objective problems)
        // or equal in length to the number of objectives.
        if (z != null && z.length != problem.getObjectivesCount()) {
            throw new IllegalArgumentException(zErrorMessage);
        }
        // Create the datastructures required for storing problme information.
        int varCount = problem.getTotalVariablesCount();
        int objCount = problem.getObjectivesCount();
        int conCount = problem.getConstraintsCount();
        double[] x = new double[varCount];
        double[] f = new double[objCount];
        double[] g = new double[conCount];
        double[][] jacobianF
                = new double[objCount][varCount];
        double[][] jacobianG
                = new double[conCount][varCount];
        // Extract problem information
        int numericalFunEval = extractProblemInfo(problem, x, objCount, f, conCount, g, jacobianF, jacobianG);
        // Calculate and return the final KKTPM
        double projectedKKTPM = getProjectedKKTPM(x, f, z, g, jacobianF, jacobianG, rho);
        return new KKTPM(projectedKKTPM, numericalFunEval);
    }

    /**
     * Calculates projected KKTPM at the specified point. (assumes rho = 0.0)
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @return projected KKTPM
     */
    public static double getProjectedKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG) {
        return getProjectedKKTPM(x, f, z, g, jacobianF, jacobianG, 0.0);
    }

    /**
     * Calculates projected KKTPM at the specified point.
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @param rho Augmented ASF (AASF) parameter
     * @return projected KKTPM
     */
    public static double getProjectedKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG,
            double rho) {
        double[] u = getLagrangeMultiplers(x, f, z, g, jacobianF, jacobianG, rho);
        //double kktpmDirect = getDirectKKTPM(f, g, u);
        double kktpmDirect = getDirectKKTPM(x, f, z, g, jacobianF, jacobianG, u, rho);
        return getProjectedKKTPM(f, g, u, kktpmDirect);
    }

    /**
     * Calculates projected KKTPM, given objectives, constraints, Lagrange
     * multipliers and the already calculated direct KKTPM.
     *
     * @param f the specified point in objective space
     * @param g constraints values
     * @param u Lagrange multipliers
     * @param kktpmDirect the already calculated direct KKTPM for the same point
     * @return projected KKTPM
     */
    public static double getProjectedKKTPM(
            double[] f,
            double[] g,
            double[] u,
            double kktpmDirect) throws
            OutOfRangeException,
            DimensionMismatchException,
            NotPositiveException {
        RealVector fv = new ArrayRealVector(f);
        RealVector gv = new ArrayRealVector(g);
        RealVector uv = new ArrayRealVector(u);
        double kktpmProjected = gv.dotProduct(
                gv.mapMultiply(kktpmDirect).subtract(
                        uv.getSubVector(fv.getDimension(), gv.getDimension())))
                / (1 + gv.dotProduct(gv));
        return kktpmProjected;
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Adjusted KKTPM">
    /**
     * Calculates and returns the adjusted KKTPM (Karush Khun Tucker Proximity
     * Measure) at the specified point (x-vector). (assumes rho = 0.0)
     *
     * @param problem encapsulates all the attributes of an optimization
     * problem.
     * @param z the ideal point (need not be the true ideal point of the
     * problem)
     * @return value of the direct KKTPM
     * @throws EvaluationException
     * @throws TooManyDecimalPointsException
     * @throws MisplacedTokensException
     */
    public static KKTPM getAdjustedKKTPM(
            OptimizationProblem problem,
            double[] z) throws
            EvaluationException,
            TooManyDecimalPointsException,
            MisplacedTokensException {
        return getAdjustedKKTPM(problem, z, 0.0);
    }

    /**
     * Calculates and returns the adjusted KKTPM (Karush Khun Tucker Proximity
     * Measure) at the specified point (x-vector).
     *
     * @param problem encapsulates all the attributes of an optimization
     * problem.
     * @param z the ideal point (need not be the true ideal point of the
     * problem)
     * @param rho Augmented ASF parameter
     * @return value of the direct KKTPM
     * @throws EvaluationException
     * @throws TooManyDecimalPointsException
     * @throws MisplacedTokensException
     */
    public static KKTPM getAdjustedKKTPM(
            OptimizationProblem problem,
            double[] z,
            double rho) throws
            EvaluationException,
            TooManyDecimalPointsException,
            MisplacedTokensException {
        // The ideal point must be either null (for single objective problems)
        // or equal in length to the number of objectives.
        if (z != null && z.length != problem.getObjectivesCount()) {
            throw new IllegalArgumentException(zErrorMessage);
        }
        // Create the datastructures required for storing problme information.
        int varCount = problem.getTotalVariablesCount();
        int objCount = problem.getObjectivesCount();
        int conCount = problem.getConstraintsCount();
        double[] x = new double[varCount];
        double[] f = new double[objCount];
        double[] g = new double[conCount];
        double[][] jacobianF
                = new double[objCount][varCount];
        double[][] jacobianG
                = new double[conCount][varCount];
        // Extract problem information
        int numericalFunEval = extractProblemInfo(problem, x, objCount, f, conCount, g, jacobianF, jacobianG);
        // Calculate and return the final KKTPM
        double adjustedKKTPM = getAdjustedKKTPM(x, f, z, g, jacobianF, jacobianG, rho);
        return new KKTPM(adjustedKKTPM, numericalFunEval);
    }

    /**
     * Calculates adjusted KKTPM at the specified point. (assumes rho = 0.0)
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @return adjusted KKTPM
     */
    public static double getAdjustedKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG) {
        return getAdjustedKKTPM(x, f, z, g, jacobianF, jacobianG, 0.0);
    }

    /**
     * Calculates adjusted KKTPM at the specified point.
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @param rho Augmented ASF (AASF) parameter
     * @return adjusted KKTPM
     */
    public static double getAdjustedKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG,
            double rho) {
        double[] u = getLagrangeMultiplers(x, f, z, g, jacobianF, jacobianG, rho);
        return getAdjustedKKTPM(f, g, u);
    }

    /**
     * Calculates adjusted KKTPM, given objectives, constraints and Lagrange
     * multipliers.
     *
     * @param f the specified point in objective space
     * @param g constraints values
     * @param u Lagrange multipliers
     * @return adjusted KKTPM
     */
    public static double getAdjustedKKTPM(
            double[] f,
            double[] g,
            double[] u) throws
            DimensionMismatchException,
            OutOfRangeException,
            NotPositiveException {
        RealVector fv = new ArrayRealVector(f);
        RealVector gv = new ArrayRealVector(g);
        RealVector uv = new ArrayRealVector(u);
        double kktpmAdjusted = -1 * gv.dotProduct(
                uv.getSubVector(fv.getDimension(), gv.getDimension()));
        return kktpmAdjusted;
    }
    //</editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Approximate KKTPM">
    /**
     * Calculates and returns the KKTPM (Karush Khun Tucker Proximity Measure)
     * at the specified point (x-vector). (assumes rho = 0.0)
     *
     * @param problem encapsulates all the attributes of an optimization
     * problem.
     * @param z the ideal point (need not be the true ideal point of the
     * problem)
     * @return value of the direct KKTPM
     * @throws EvaluationException
     * @throws TooManyDecimalPointsException
     * @throws MisplacedTokensException
     */
    public static KKTPM getKKTPM(
            OptimizationProblem problem,
            double[] z) throws
            EvaluationException,
            TooManyDecimalPointsException,
            MisplacedTokensException {
        return getKKTPM(problem, z, 0.0);
    }

    /**
     * Calculates and returns the KKTPM (Karush Khun Tucker Proximity Measure)
     * at the specified point (x-vector).
     *
     * @param problem encapsulates all the attributes of an optimization
     * problem.
     * @param z the ideal point (need not be the true ideal point of the
     * problem)
     * @param rho Augmented ASF parameter
     * @return value of the direct KKTPM
     * @throws EvaluationException
     * @throws TooManyDecimalPointsException
     * @throws MisplacedTokensException
     */
    public static KKTPM getKKTPM(
            OptimizationProblem problem,
            double[] z,
            double rho) throws
            EvaluationException,
            TooManyDecimalPointsException,
            MisplacedTokensException {
        // The ideal point must be either null (for single objective problems)
        // or equal in length to the number of objectives.
        if (z != null && z.length != problem.getObjectivesCount()) {
            throw new IllegalArgumentException(zErrorMessage);
        }
        // Create the datastructures required for storing problme information.
        int varCount = problem.getTotalVariablesCount();
        int objCount = problem.getObjectivesCount();
        int conCount = problem.getConstraintsCount();
        double[] x = new double[varCount];
        double[] f = new double[objCount];
        double[] g = new double[conCount];
        double[][] jacobianF
                = new double[objCount][varCount];
        double[][] jacobianG
                = new double[conCount][varCount];
        // Extract problem information
        int numericalFunEval = extractProblemInfo(problem, x, objCount, f, conCount, g, jacobianF, jacobianG);
        // Calculate and return the final KKTPM
        double kktpm = getKKTPM(x, f, z, g, jacobianF, jacobianG, rho);
        return new KKTPM(kktpm, numericalFunEval);
    }

//    /**
//     * Calculates KKTPM, given objectives, constraints and Lagrange multipliers.
//     *
//     * @param f the specified point in objective space
//     * @param g constraints values
//     * @param u Lagrange multipliers
//     * @return KKTPM
//     */
//    public static double getKKTPM(
//            double[] f,
//            double[] g,
//            double[] u) throws
//            DimensionMismatchException,
//            OutOfRangeException,
//            NotPositiveException {
//        //double kktpmDirect = getDirectKKTPM(f, g, u);
//        double kktpmDirect = getDirectKKTPM(f, g, u);
//        double kktpmProjected = getProjectedKKTPM(f, g, u, kktpmDirect);
//        double kktpmAdjusted = getAdjustedKKTPM(f, g, u);
//        return (kktpmDirect + kktpmProjected + kktpmAdjusted) / 3;
//    }    
    /**
     * Calculates KKTPM, given objectives, constraints and Lagrange multipliers.
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @param rho Augmented ASF parameter
     * @param u Lagrange multipliers
     * @return KKTPM
     */
    public static double getKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG,
            double[] u,
            double rho) throws
            DimensionMismatchException,
            OutOfRangeException,
            NotPositiveException {
        //double kktpmDirect = getDirectKKTPM(f, g, u);
        double kktpmDirect = getDirectKKTPM(x, f, z, g, jacobianF, jacobianG, u, rho);
        double kktpmProjected = getProjectedKKTPM(f, g, u, kktpmDirect);
        double kktpmAdjusted = getAdjustedKKTPM(f, g, u);
        if (kktpmAdjusted > kktpmDirect) {
            return (kktpmDirect + kktpmProjected + kktpmAdjusted) / 3;
        } else {
            return kktpmDirect;
        }
    }

    /**
     * Calculates KKTPM, given objectives, constraints and Lagrange multipliers.
     * (assumes rho = 0.0)
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @param u Lagrange multipliers
     * @return KKTPM
     */
    public static double getKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG,
            double[] u) throws
            DimensionMismatchException,
            OutOfRangeException,
            NotPositiveException {
        return getKKTPM(x, f, z, g, jacobianF, jacobianG, u, 0.0);
    }

    /**
     * Calculates KKTPM at the specified point. (assumes rho = 0.0)
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @return KKTPM
     */
    public static double getKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG) {
        return getKKTPM(x, f, z, g, jacobianF, jacobianG, 0.0);
    }

    /**
     * Calculates KKTPM at the specified point.
     *
     * @param x the specified point in design/decision space
     * @param f the specified point in objective space
     * @param z ideal point (need not be the true ideal point of the problem)
     * @param g constraints values
     * @param jacobianF matrix of objectives first derivatives
     * @param jacobianG matrix of constraints first derivatives
     * @param rho Augmented ASF (AASF) parameter
     * @return KKTPM
     */
    public static double getKKTPM(
            double[] x,
            double[] f,
            double[] z,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG,
            double rho) {
        double[] u = getLagrangeMultiplers(x, f, z, g, jacobianF, jacobianG, rho);
        //double kktpmDirect = getDirectKKTPM(f, g, u);
        double kktpmDirect = getDirectKKTPM(x, f, z, g, jacobianF, jacobianG, u, rho);
        // Check if you need the approximation
        if (isApproximationRequired(u, g)) {
            double kktpmAdjusted = getAdjustedKKTPM(f, g, u);
            double kktpmProjected = getProjectedKKTPM(f, g, u, kktpmDirect);
            return (kktpmDirect + kktpmAdjusted + kktpmProjected) / 3;
        } else {
            return kktpmDirect;
        }
//        if (kktpmAdjusted > kktpmDirect) {
//            return (kktpmDirect + kktpmAdjusted + kktpmProjected) / 3;
//        } else {
//            return kktpmDirect;
//        }
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Private Utility Methods">
    /**
     * This utility function is used to get the index of the first negative
     * Lagrange multiplier.
     *
     * @param u the vector of all Lagrange multipliers
     * @return the index of the first negative Lagrange multiplier or -1
     * otherwise
     */
    private static int getFirstNegativeMultipierIndex(RealVector u) {
        for (int i = 0; i < u.getDimension(); i++) {
            if (u.getEntry(i) < 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * This utility method is used to extract information from the optimization
     * problem object into the arrays sent as parameters. These arrays will be
     * used later in KKTPM calculations.
     *
     * @param problem
     * @param x
     * @param objCount
     * @param f
     * @param conCount
     * @param g
     * @param jacobianF
     * @param jacobianG
     * @throws EvaluationException
     * @throws Throwable
     */
    private static int extractProblemInfo(
            OptimizationProblem problem,
            double[] x,
            int objCount,
            double[] f,
            int conCount,
            double[] g,
            double[][] jacobianF,
            double[][] jacobianG) throws EvaluationException, TooManyDecimalPointsException, MisplacedTokensException {
        // The number of additional function evaluations consumed due to numerical gradient evaluations
        int numericalEvalCount = 0;
        // Get the x-vector (it should include both variables and vectors)
        int varIndex = 0;
        // Add variables
        Iterator<Map.Entry<String, Double>> varIt = problem.getVariablesIterator();
        while (varIt.hasNext()) {
            x[varIndex++] = varIt.next().getValue();
        }
        // Add vectors elements
        Iterator<Map.Entry<String, double[]>> vecIt = problem.getVectorsIterator();
        for (int i = 0; vecIt.hasNext(); i++) {
            double[] v = vecIt.next().getValue();
            for (int j = 0; j < v.length; j++) {
                x[varIndex++] = v[j];
            }
        }
        // Get the objectives
        for (int i = 0; i < objCount; i++) {
            f[i] = problem.getObjective(i);
        }
        // Get the constraints
        for (int i = 0; i < conCount; i++) {
            g[i] = problem.getConstraint(i);
        }
        // Get objectives derivatives
        for (int i = 0; i < objCount; i++) {
            varIndex = 0;
            // Set derivatives of variables
            varIt = problem.getVariablesIterator();
            while (varIt.hasNext()) {
                Derivative partialDerivative = problem.getObjectivePartialDerivative(
                        i,
                        varIt.next().getKey());
                jacobianF[i][varIndex++] = partialDerivative.getDerivative();
                numericalEvalCount += partialDerivative.getFunEvalCount();
            }
            // Set derivatives of vectors elements
            vecIt = problem.getVectorsIterator();
            while (vecIt.hasNext()) {
                Map.Entry<String, double[]> nameVecPair = vecIt.next();
                for (int j = 0; j < nameVecPair.getValue().length; j++) {
                    Derivative partialDerivative = problem.getObjectivePartialDerivative(i, nameVecPair.getKey() + "[" + (j + 1) + "]");
                    jacobianF[i][varIndex] = partialDerivative.getDerivative();
                    numericalEvalCount += partialDerivative.getFunEvalCount();
                    varIndex++;
                }
            }
        }
        // Get constraints derivatives
        for (int i = 0; i < conCount; i++) {
            varIndex = 0;
            // Set derivatives of variables
            varIt = problem.getVariablesIterator();
            while (varIt.hasNext()) {
                Derivative partialDerivative = problem.getConstraintPartialDerivative(
                        i,
                        varIt.next().getKey());
                jacobianG[i][varIndex++] = partialDerivative.getDerivative();
                numericalEvalCount += partialDerivative.getFunEvalCount();
            }
            // Set derivatives of vectors elements
            vecIt = problem.getVectorsIterator();
            while (vecIt.hasNext()) {
                Map.Entry<String, double[]> nameVecPair = vecIt.next();
                for (int j = 0; j < nameVecPair.getValue().length; j++) {
                    Derivative partialDerivative = problem.getConstraintPartialDerivative(
                            i,
                            nameVecPair.getKey() + "[" + (j + 1) + "]");
                    jacobianG[i][varIndex] = partialDerivative.getDerivative();
                    numericalEvalCount += partialDerivative.getFunEvalCount();
                    varIndex++;
                }
            }
        }
        return numericalEvalCount;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Testing (main)">
    /**
     * Just for testing
     *
     * @param args unused
     * @throws java.lang.Throwable
     */
    public static void main(String[] args) throws Throwable {
        // ====================================================================
        // Using raw input data
        // --------------------------------------------------------------------
        System.out.println("----------------------------------");
        System.out.println(" Using Raw Input ");
        System.out.println("----------------------------------");
        calculateUsingRawData();
        // ====================================================================
        // Using an optimization problem object
        // --------------------------------------------------------------------
        System.out.println("----------------------------------");
        System.out.println(" Using OptimizationProblem Object ");
        System.out.println("----------------------------------");
        calculateUsingProblemObject();
        // ====================================================================
        // Calculate Lagrange Multipliers Independently
        // --------------------------------------------------------------------
        System.out.println("----------------------------------");
        System.out.println(" Using Lagrange Multipliers ");
        System.out.println("----------------------------------");
        //calculateLagrangeIndependently();
    }

    private static void calculateUsingRawData() {
        // Raw input data
        double[] x = new double[]{1};
        double[] f = new double[]{1};
        double[] z = null;
        double[] g = new double[]{-0.5};
        double[][] jacobianF = {{2}}; // Two functions & three variables
        double[][] jacobianG = {{-1}}; // Four constraints & three variables
        // Calculations (ASF)
        System.out.println("<ASF>");
        double kktpmDirect = getDirectKKTPM(x, f, z, g, jacobianF, jacobianG);
        double kktpmAdjusted = getAdjustedKKTPM(x, f, z, g, jacobianF, jacobianG);
        double kktpmProjected = getProjectedKKTPM(x, f, z, g, jacobianF, jacobianG);
        double kktpm = getKKTPM(x, f, z, g, jacobianF, jacobianG);
        // Display results
        System.out.format("%12s  = %10.6f%n", "Direct KKTPM", kktpmDirect);
        System.out.format("%12s  = %10.6f%n", "Adj. KKTPM", kktpmAdjusted);
        System.out.format("%12s  = %10.6f%n", "Proj. KKTPM", kktpmProjected);
        System.out.format("%12s  = %10.6f%n", "KKTPM", kktpm);
        // Calculation (AASF)
        System.out.println("<AASF> (rho = 0.01)");
        kktpmDirect = getDirectKKTPM(x, f, z, g, jacobianF, jacobianG, 0.01);
        kktpmAdjusted = getAdjustedKKTPM(x, f, z, g, jacobianF, jacobianG, 0.01);
        kktpmProjected = getProjectedKKTPM(x, f, z, g, jacobianF, jacobianG, 0.01);
        kktpm = getKKTPM(x, f, z, g, jacobianF, jacobianG, 0.01);
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
        problem.setConstraintPartialDerivative(0, "x1", "0-1");
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
//        problem.setConstraintPartialDerivative(0, "x1", "0-1");
//        // Calculate Lagrange multipliers
//        double[] u = KKTPMCalculator.getLagrangeMultiplers(problem, z);
//        // Caclulations (using the calculated Lagrange multipliers)
//        //double kktpmDirect = KKTPMCalculator.getDirectKKTPM(f, g, u);
//        double kktpmDirect = KKTPMCalculator.getDirectKKTPM(x, f, z, g, jacobianF, jacobianG, u);
//        double kktpmAdjusted = KKTPMCalculator.getAdjustedKKTPM(f, g, u);
//        double kktpmProjected = KKTPMCalculator.getProjectedKKTPM(f, g, u, kktpmDirect);
//        double kktpm = KKTPMCalculator.getKKTPM(f, g, u);
//        // Display results
//        System.out.format("%12s  = %10.6f%n", "Direct KKTPM", kktpmDirect);
//        System.out.format("%12s  = %10.6f%n", "Adj. KKTPM", kktpmAdjusted);
//        System.out.format("%12s  = %10.6f%n", "Proj. KKTPM", kktpmProjected);
//        System.out.format("%12s  = %10.6f%n", "KKTPM", kktpm);
//    }
    // </editor-fold>
    private static boolean isApproximationRequired(double[] u, double[] g) {
        RealVector um = new ArrayRealVector(u, 0, u.length - g.length);
        RealVector uj = new ArrayRealVector(u, u.length - g.length, g.length);
        RealVector gv = new ArrayRealVector(g);
        double sum = 0;
        for (int i = 0; i < um.getDimension(); i++) {
            sum += um.getEntry(i);
        }
        RealVector negativeGv = gv.mapMultiply(-1);
        double ujgj = negativeGv.dotProduct(uj);
        if(sum + ujgj * (1 + ujgj) > 1) {
            return true;
        } else {
            return false;
        }
    }
}
