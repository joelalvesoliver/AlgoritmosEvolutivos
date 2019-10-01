/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

import core.MathExpressionParser;
import core.VariablesManager;
import exceptions.EvaluationException;
import exceptions.MisplacedTokensException;
import exceptions.TooManyDecimalPointsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import types.AbstractNode;
import utility.StringUtils;

/**
 * An instance of this class represents an optimization problem. An optimization
 * problem is composed of a set of variables (name-value pairs), a set of
 * objectives (mathematical formulas), a set of constraints (mathematical
 * formulas), objectives gradients (a mathematical formula for each objective)
 * and constraints gradients (a mathematical formula for each constraint). All
 * these mathematical formulas are stored in the form of objects that are
 * evaluated at runtime based on the current values of the variables.
 *
 * @author Haitham
 */
public class OptimizationProblem {

    // For numerical gradients
    public static final double DEFAULT_DELTA = 0.001;

    private final VariablesManager vm;
    private final List<AbstractNode> objList;
    private final List<AbstractNode> conList;
    private final List<AbstractNode[]> objGradListVariables;
    private final List<AbstractNode[][]> objGradListVectors;
    private final List<AbstractNode[]> conGradListVariables;
    private final List<AbstractNode[][]> conGradListVectors;
    // For numerical gradients
    private double delta;

    public OptimizationProblem() {
        vm = new VariablesManager();
        objList = new ArrayList<>();
        conList = new ArrayList<>();
        objGradListVariables = new ArrayList<>();
        objGradListVectors = new ArrayList<>();
        conGradListVariables = new ArrayList<>();
        conGradListVectors = new ArrayList<>();
        this.delta = DEFAULT_DELTA;
    }

    /**
     * Parses the string argument, then adds the resulting parse tree as a new
     * objective.
     *
     * @param objString the mathematical formula of the objective function.
     * @throws TooManyDecimalPointsException if thrown by
     * MathExpressionParser.parse(...)
     * @throws MisplacedTokensException if thrown by
     * MathExpressionParser.parse(...)
     * @throws Throwable if thrown by MathExpressionParser.parse(...)
     */
    public void addObjective(String objString) throws
            TooManyDecimalPointsException,
            MisplacedTokensException,
            Throwable {
        objList.add(MathExpressionParser.parse(objString, vm));
    }

//    /**
//     * Gets the value of the objective(<i>objIndex</i>) with respect to the
//     * specified <i>x</i> vector.
//     * @param objIndex the index of the designated objective
//     * @param x the vector used to evaluate the objective
//     * @return the value of the designated objective.
//     * @throws EvaluationException if the objective cannot be evaluated
//     */
//    public double getObjective(int objIndex, double[] x) throws EvaluationException {
//        Iterator<Map.Entry<String, Double>> it = vm.variablesIterator();
//        int i = 0;
//        while(it.hasNext()) {
//            Map.Entry<String, Double> entry = it.next();
//            entry.setValue(x[i++]);
//        }
//        return objList.get(objIndex).evaluate();
//    }
    /**
     * Gets the value of the objective(<i>objIndex</i>) with respect to the
     * current <i>x</i> vector.
     *
     * @param objIndex the index of the designated objective
     * @return the value of the designated objective.
     * @throws EvaluationException if the objective cannot be evaluated
     */
    public double getObjective(int objIndex) throws EvaluationException {
        return objList.get(objIndex).evaluate();
    }

    /**
     * Parses the string argument, then adds the resulting parse tree as a new
     * constraint.
     *
     * @param constString the mathematical formula of the constraint in the from
     * g(x) &le; 0.
     * @throws TooManyDecimalPointsException if thrown by
     * MathExpressionParser.parse(...)
     * @throws MisplacedTokensException if thrown by
     * MathExpressionParser.parse(...)
     * @throws Throwable if thrown by MathExpressionParser.parse(...)
     */
    public void addConstraint(String constString) throws
            TooManyDecimalPointsException,
            MisplacedTokensException,
            Throwable {
        conList.add(MathExpressionParser.parse(constString, vm));
    }

//    /**
//     * Gets the value of the constraint(<i>conIndex</i>) with respect to the
//     * specified <i>x</i> vector.
//     * @param conIndex the index of the designated constraint
//     * @param x the vector used to evaluate the objective
//     * @return the value of the designated constraint
//     * @throws EvaluationException if the constraint cannot be evaluated
//     */
//    public double getConstraint(int conIndex, double[] x) throws EvaluationException {
//        Iterator<Map.Entry<String, Double>> it = vm.variablesIterator();
//        int i = 0;
//        while(it.hasNext()) {
//            Map.Entry<String, Double> entry = it.next();
//            entry.setValue(x[i++]);
//        }
//        return conList.get(conIndex).evaluate();
//    }
    /**
     * Gets the value of the constraint(<i>conIndex</i>) with respect to the
     * current <i>x</i> vector.
     *
     * @param conIndex the index of the designated constraint
     * @return the value of the designated constraint
     * @throws EvaluationException if the constraint cannot be evaluated
     */
    public double getConstraint(int conIndex) throws EvaluationException {
        return conList.get(conIndex).evaluate();
    }

    /**
     * Sets the value of the constant named (constantName) to (value). If the
     * constant does not exist, it is created and initialized.
     *
     * @param constantName constant name
     * @param value newly assigned value
     */
    public void setConstant(String constantName, double value) {
        vm.setConstant(constantName, value);
    }

    /**
     * Sets the value of the variable named (varName) to (value). If the
     * variable does not exist, it is created and initialized.
     *
     * @param varName variable name
     * @param value newly assigned value
     */
    public void setVariable(String varName, double value) {
        vm.set(varName, value);
    }

    /**
     * Sets the values of all the variables at once. The variables are set to
     * the new values in the order at which they were added to the manager. Each
     * variable will retain its original name. Notice that This method cannot be
     * used to initialize the x-vector. It can only be used to change the values
     * of an already existing set of variables. For initialization, use
     * setVariable(...) to initialize each variable separately.
     *
     * @param x new x values
     */
    public void setAllVariables(double[] x) {
        if (getTotalVariablesCount() != x.length) {
            String errorMessage = "the size of the argument must be equal to "
                    + "the total number of variables currently existing. The "
                    + "total number of varaiables is the number of variables "
                    + "plus the number of elements in all vectors. This "
                    + "method cannot be used to initialize the x-vector. It "
                    + "can only be used to change the values of an already "
                    + "existing set of variables. For initialization, you have "
                    + "to initialize each variable separately using the "
                    + "setVariable(...) method.";
            throw new IllegalArgumentException(errorMessage);
        }
        // Set all variables values
        Iterator<Map.Entry<String, Double>> varIt = getVariablesIterator();
        int varIndex = 0;
        while (varIt.hasNext()) {
            Map.Entry<String, Double> entry = varIt.next();
            entry.setValue(x[varIndex++]);
        }
        // Set all vectors elements values
        Iterator<Map.Entry<String, double[]>> vecIt = getVectorsIterator();
        while (vecIt.hasNext()) {
            double[] v = vecIt.next().getValue();
            for (int i = 0; i < v.length; i++) {
                v[i] = x[varIndex++];
            }
        }
    }

    /**
     * Gets the value of a variable by its index. The index is specified by the
     * order of adding this variable to the variables list.
     *
     * @param index the index of the variable whose value is to be retrieved.
     * @return the value of variable(<i>index</i>)
     */
    public double getVariable(int index) {
        if (index < vm.getVariablesCount()) {
            double value = 0;
            Iterator<Map.Entry<String, Double>> it = vm.variablesIterator();
            for (int i = 0; it.hasNext(); i++) {
                if (i == index) {
                    value = it.next().getValue();
                }
                i++;
            }
            return value;
        } else {
            String message = String.format(
                    "Inavlid index (%d). Only (%d) variables exist.",
                    index,
                    vm.getVariablesCount());
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Gets the value of a variable by its name.
     *
     * @param varName the name of the variable whose value is to be retrieved.
     * @return the value of variable named <i>varName</i>
     */
    public double getVariable(String varName) {
        try {
            return vm.get(varName);
        } catch (NullPointerException ex) {
            String message = String.format("The variable (%s) does not exist.",
                    varName);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Gets an iterator over the variables currently included in the problem.
     *
     * @return an iterator of map entries, each representing a name-value pair
     */
    public Iterator<Map.Entry<String, Double>> getVariablesIterator() {
        return vm.variablesIterator();
    }

    /**
     * Sets the values of the vector named (vectorName) to (values). If the
     * vector does not exist, it is created and initialized.
     *
     * @param vectorName vector name
     * @param values newly assigned values
     */
    public void setVector(String vectorName, double[] values) {
        vm.setVector(vectorName, values);
    }

    /**
     * Gets a vector by its name.
     *
     * @param vectorName the name of the vector.
     * @return the array representing the vector.
     */
    public double[] getVector(String vectorName) {
        try {
            return vm.getVector(vectorName);
        } catch (NullPointerException ex) {
            String message = String.format("Vector (%s) does not exist.",
                    vectorName);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Gets the value of a vector element.
     *
     * @param vectorName the name of the vector from which a value is to be
     * retrieved.
     * @param index position of th element to be retrieved in vector
     * <i>vectorName</i>.
     * @return the value of element at position <i>index</i>
     */
    public double getVectorElement(String vectorName, int index) {
        try {
            double[] vector = vm.getVector(vectorName);
            return vector[index];
        } catch (NullPointerException ex) {
            String message = String.format("Vector (%s) does not exist.",
                    vectorName);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Sets the value of a vector element.
     *
     * @param vectorName the name of the vector in which the value is to be set.
     * @param index position of th element to be set in vector
     * <i>vectorName</i>.
     * @param value the new value to be set at position <i>index</i>
     */
    public void setVectorElement(String vectorName, int index, double value) {
        try {
            vm.getVector(vectorName)[index] = value;
        } catch (NullPointerException ex) {
            String message = String.format("Vector (%s) does not exist.",
                    vectorName);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Gets an iterator over the vectors currently included in the problem.
     *
     * @return an iterator of map entries, each representing a name-array pair.
     */
    public Iterator<Map.Entry<String, double[]>> getVectorsIterator() {
        return vm.vectorsIterator();
    }

    /**
     * Gets the number if variables currently set in the problem.
     *
     * @return number of variables currently available
     */
    public int getVariablesCount() {
        return vm.getVariablesCount();
    }

    /**
     * Gets the number if vectors currently set in the problem.
     *
     * @return number of vectors currently available
     */
    public int getVectorsCount() {
        return vm.getVectorsCount();
    }

    /**
     * Gets the total number of variables currently set in the problem. This is
     * equal to the number of variables plus the number of all vectors elements.
     *
     * @return total number of variables currently available (variables +
     * vectors elements)
     */
    public int getTotalVariablesCount() {
        int totalCount = vm.getVariablesCount();
        Iterator<Map.Entry<String, double[]>> vectorsIterator = getVectorsIterator();
        while (vectorsIterator.hasNext()) {
            totalCount += vectorsIterator.next().getValue().length;
        }
        return totalCount;
    }

    /**
     * Gets the number of objectives currently included in the problem.
     *
     * @return the number of objectives available.
     */
    public int getObjectivesCount() {
        return objList.size();
    }

    /**
     * Gets the number of constraints currently included in the problem.
     *
     * @return the number of constraints available.
     */
    public int getConstraintsCount() {
        return conList.size();
    }

    /**
     * Sets the partial derivative of objective <i>objIndex</i> with respect to
     * the variable named <i>varName</i>.
     *
     * @param objIndex the index of the objective function we derive from.
     * @param varName the name of the variable with respect to which we derive.
     * @param partialDerivativeString the partial derivative mathematical
     * formula.
     * @throws TooManyDecimalPointsException if thrown by
     * MathExpressionParser.parse(...)
     * @throws MisplacedTokensException if thrown by
     * MathExpressionParser.parse(...)
     * @throws Throwable if thrown by MathExpressionParser.parse(...)
     */
    public void setObjectivePartialDerivative(
            int objIndex,
            String varName,
            String partialDerivativeString) throws
            TooManyDecimalPointsException,
            MisplacedTokensException,
            Throwable {
        if (!varName.contains("[")) {
            setPartialDerivative(objIndex, varName, partialDerivativeString,
                    objGradListVariables);
        } else {
            // Retireve vector name
            String vecName = varName.substring(0, varName.indexOf('['));
            // Retrieve index
            int elementIndex = Integer.parseInt(varName.substring(
                    varName.indexOf('[') + 1, varName.indexOf(']'))) - 1;
            setPartialDerivative(objIndex, vecName, elementIndex,
                    partialDerivativeString, objGradListVectors);
        }
    }

    /**
     * Sets the partial derivative of constraint <i>conIndex</i> with respect to
     * the variable named <i>varName</i>.
     *
     * @param conIndex the index of the constraint we derive from.
     * @param varName the name of the variable with respect to which we derive.
     * @param partialDerivativeString the partial derivative mathematical
     * formula.
     * @throws TooManyDecimalPointsException if thrown by
     * MathExpressionParser.parse(...)
     * @throws MisplacedTokensException if thrown by
     * MathExpressionParser.parse(...)
     * @throws Throwable if thrown by MathExpressionParser.parse(...)
     */
    public void setConstraintPartialDerivative(
            int conIndex,
            String varName,
            String partialDerivativeString) throws
            TooManyDecimalPointsException,
            MisplacedTokensException,
            Throwable {
        if (!varName.contains("[")) {
            setPartialDerivative(conIndex, varName, partialDerivativeString,
                    conGradListVariables);
        } else {
            // Retireve vector name
            String vecName = varName.substring(0, varName.indexOf('['));
            // Retrieve index
            int elementIndex = Integer.parseInt(varName.substring(
                    varName.indexOf('[') + 1, varName.indexOf(']'))) - 1;
            setPartialDerivative(conIndex, vecName, elementIndex,
                    partialDerivativeString, conGradListVectors);
        }
    }

    /**
     * This utility function is used to set an objective or constraint partial
     * derivatives with respect to some vector element. The last argument
     * specifies which of them (objective or constraint) is targeted.
     *
     * @param index the index of the objective/constrained in
     * <i>targetList</i>.
     * @param vecName the vector element with respect to which we derive.
     * @param partialDerivativeString the partial derivative mathematical
     * formula.
     * @param targetList either objectives gradient list or constraints
     * gradients list.
     * @throws TooManyDecimalPointsException if thrown by
     * MathExpressionParser.parse(...)
     * @throws MisplacedTokensException if thrown by
     * MathExpressionParser.parse(...)
     * @throws Throwable if thrown by MathExpressionParser.parse(...)
     */
    private void setPartialDerivative(
            int index,
            String vectorName,
            int elementIndex,
            String partialDerivativeString,
            List<AbstractNode[][]> targetList) throws
            TooManyDecimalPointsException,
            MisplacedTokensException,
            Throwable {
        Iterator<Map.Entry<String, double[]>> it = vm.vectorsIterator();
        boolean vectorFound = false;

        for (int i = 0; it.hasNext(); i++) {
            Map.Entry<String, double[]> vecValPair = it.next();
            if (vectorName.equals(vecValPair.getKey())) {
                if (targetList.size() <= index) {
                    // Assume that you are trying to set the partial derivatives
                    // of obj(4) before/without setting the partial derivatives
                    // of obj(2) and obj(3). This means that your targetList
                    // size is now only 1 (which is the array of partial
                    // derivatives of obj(1) only). Now you need to add empty
                    // arrays (null at each position) for obj(2), obj(3) and
                    // ob(4) not only for obj(4). Then you can change the null
                    // values of obj(4) to the required partial derivatives.
                    // Doing this, targetList will eventually have the actual
                    // partial derivatives of obj(1) and obj(4) and will have
                    // nulls for obj(2) and obj(3).
                    for (int j = targetList.size(); j <= index; j++) {
                        AbstractNode[][] allVectorsGradients = new AbstractNode[vm.getVectorsCount()][];
                        Iterator<Map.Entry<String, double[]>> it2 = vm.vectorsIterator();
                        for (int k = 0; it2.hasNext(); k++) {
                            int vectorLength = it2.next().getValue().length;
                            allVectorsGradients[k] = new AbstractNode[vectorLength];
                        }
                        targetList.add(allVectorsGradients);
                    }
                }
                if (partialDerivativeString == null || partialDerivativeString.trim().isEmpty()) {
                    targetList.get(index)[i][elementIndex] = null;
                } else {
                    targetList.get(index)[i][elementIndex] = MathExpressionParser.parse(
                            partialDerivativeString, vm);
                }
                // Flag that the variable exists
                vectorFound = true;
                // Based on the core assumption that no two variables can have
                // the same name, we can stop now.
                break;
            }
        }
        if (!vectorFound) {
            String message = String.format("Vector (%s) does not exist.",
                    vectorName);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * This utility function is used to set an objective or constraint partial
     * derivative with respect to some variable. The last argument specifies
     * which of them is targeted.
     *
     * @param index the index of the objective/constrained in
     * <i>targetList</i>.
     * @param varName the name of the variable with respect to which we derive.
     * @param partialDerivativeString the partial derivative mathematical
     * formula.
     * @param targetList either objectives gradient list or constraints
     * gradients list.
     * @throws TooManyDecimalPointsException if thrown by
     * MathExpressionParser.parse(...)
     * @throws MisplacedTokensException if thrown by
     * MathExpressionParser.parse(...)
     * @throws Throwable if thrown by MathExpressionParser.parse(...)
     */
    private void setPartialDerivative(
            int index,
            String varName,
            String partialDerivativeString,
            List<AbstractNode[]> targetList) throws
            TooManyDecimalPointsException,
            MisplacedTokensException,
            Throwable {
        Iterator<Map.Entry<String, Double>> it = vm.variablesIterator();
        boolean variableFound = false;
        for (int i = 0; it.hasNext(); i++) {
            Map.Entry<String, Double> varValPair = it.next();
            if (varName.equals(varValPair.getKey())) {
                if (targetList.size() <= index) {
                    // Assume that you are trying to set the partial derivatives
                    // of obj(4) before/without setting the partial derivatives
                    // of obj(2) and obj(3). This means that your targetList
                    // size is now only 1 (which is the array of partial
                    // derivatives of obj(1) only). Now you need to add empty
                    // arrays (null at each position) for obj(2), obj(3) and
                    // ob(4) not only for obj(4). Then you can change the null
                    // values of obj(4) to the required partial derivatives.
                    // Doing this, targetList will eventually have the actual
                    // partial derivatives of obj(1) and obj(4) and will have
                    // nulls for obj(2) and obj(3).
                    for (int j = targetList.size(); j <= index; j++) {
                        targetList.add(new AbstractNode[vm.getVariablesCount()]);
                    }
                }
                if (partialDerivativeString == null || partialDerivativeString.trim().isEmpty()) {
                    targetList.get(index)[i] = null;
                } else {
                    targetList.get(index)[i] = MathExpressionParser.parse(
                            partialDerivativeString, vm);
                }
                // Flag that the variable exists
                variableFound = true;
                // Based on the core assumption that no two variables can have
                // the same name, we can stop now.
                break;
            }
        }
        if (!variableFound) {
            String message = String.format("Variable (%s) does not exist.",
                    varName);
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Gets the partial derivative of objective <i>objIndex</i> with respect to
     * the variable named <i>varName</i>.
     *
     * @param objIndex the index of the objective we need to derive.
     * @param varName the name of the variable with respect to which we get the
     * partial derivative.
     * @return the partial derivative of objective(<i>index</i>) with respect to
     * variable <i>varName</i>.
     * @throws TooManyDecimalPointsException if thrown by
     * MathExpressionParser.parse(...)
     * @throws MisplacedTokensException if thrown by
     * MathExpressionParser.parse(...)
     */
    public Derivative getObjectivePartialDerivative(
            int objIndex,
            String varName) throws
            TooManyDecimalPointsException,
            MisplacedTokensException,
            EvaluationException {
        if (!varName.contains("[")) {
            return getPartialDerivative(objIndex, varName, objGradListVariables);
        } else {
            // Retrieve vector name
            String vectorName = varName.substring(0, varName.indexOf('['));
            // Retrieve element index
            int elementIndex = Integer.parseInt(varName.substring(
                    varName.indexOf('[') + 1, varName.indexOf(']'))) - 1;
            return getPartialDerivative(objIndex, vectorName, elementIndex,
                    objGradListVectors);
        }
    }

    /**
     * Gets the partial derivative of constraint <i>conIndex</i> with respect to
     * the variable named <i>varName</i>.
     *
     * @param conIndex the index of the constraint we need to derive.
     * @param varName the name of the variable with respect to which we get the
     * partial derivative.
     * @return the partial derivative of constraint(<i>index</i>) with respect
     * to variable <i>varName</i>.
     * @throws TooManyDecimalPointsException if thrown by
     * MathExpressionParser.parse(...)
     * @throws MisplacedTokensException if thrown by
     * MathExpressionParser.parse(...)
     * @throws exceptions.EvaluationException
     */
    public Derivative getConstraintPartialDerivative(
            int conIndex,
            String varName) throws
            TooManyDecimalPointsException,
            MisplacedTokensException,
            EvaluationException {
        if (!varName.contains("[")) {
            return getPartialDerivative(conIndex, varName, conGradListVariables);
        } else {
            // Retrieve vector name
            String vectorName = varName.substring(0, varName.indexOf('['));
            // Retrieve element index
            int elementIndex = Integer.parseInt(varName.substring(
                    varName.indexOf('[') + 1, varName.indexOf(']'))) - 1;
            return getPartialDerivative(conIndex, vectorName, elementIndex,
                    conGradListVectors);
        }
    }

    /**
     * This function is used to get objective or constraint partial derivative
     * with respect to some variable. The last argument specifies which of them
     * (objective or constraint) is targeted.
     *
     * @param index the index of the objective/constraint in <i>targetList</i>.
     * @param varName the name of the variable with respect to which we get the
     * partial derivative.
     * @param gradientsTargetList either objectives gradient list or constraints
     * gradients list.
     * @return the partial derivative of obj/con(<i>index</i>) with respect to
     * variable <i>varName</i>.
     * @throws TooManyDecimalPointsException if thrown by
     * MathExpressionParser.parse(...)
     * @throws MisplacedTokensException if thrown by
     * MathExpressionParser.parse(...)
     * @throws Throwable if thrown by MathExpressionParser.parse(...)
     */
    public Derivative getPartialDerivative(
            int index,
            String varName,
            List<AbstractNode[]> gradientsTargetList) throws
            EvaluationException {
        Iterator<Map.Entry<String, Double>> it = vm.variablesIterator();
        for (int i = 0; it.hasNext(); i++) {
            Map.Entry<String, Double> varValPair = it.next();
            if (varName.equals(varValPair.getKey())) {
                if (gradientsTargetList.get(index)[i] == null) {
                    // Calculate the derivative numerically
                    double partialDerivative;
                    // Notice that one of the two following if statements MUST
                    // be executed. Because, the gradientsTargetList must be 
                    // either objGradListVariables or conGradListVariables.
                    // That's why the lines following this if-else block should
                    // never throw a NullPointerException.
                    List<AbstractNode> targetList = null;
                    if(gradientsTargetList == objGradListVariables) {
                        targetList = objList;
                    } else if((gradientsTargetList == conGradListVariables)) {
                        targetList = conList;
                    }
                    // Get the obj/con value at the current point
                    double currValue = targetList.get(index).evaluate();
                    // Move your point ahead with distance delta
                    setVariable(varName, getVariable(varName) + getDelta());
                    // Get the objective value after shifting
                    double shiftedValue = targetList.get(index).evaluate();
                    // Calculate the partial derivative using  Newton's difference quotient (also known as a first-order divided difference or forward difference)
                    partialDerivative = (shiftedValue - currValue) / getDelta();
                    // Return your point to where it was
                    setVariable(varName, getVariable(varName) - getDelta());
                    // Return (only one additional function evaluation is consumed by the forward difference method)
                    return new Derivative(partialDerivative, 1);
                } else {
                    return new Derivative(gradientsTargetList.get(index)[i].evaluate(), 0);
                }
            }
        }
        String message = String.format("The variable (%s) does not exist.",
                varName);
        throw new IllegalArgumentException(message);
    }

    /**
     * This function is used to get objective or constraint partial derivative
     * with respect to some vector element. The last argument specifies which of
     * them (objective or constraint) is targeted.
     *
     * @param index the index of the objective/constraint in <i>targetList</i>.
     * @param vectorName the name of the vector containing the element with
     * respect to which we get the partial derivative.
     * @param elementIndex the index of the designated element in vector
     * <i>vectorName</i>.
     * @param gradientsTargetList either objectives gradient list or constraints
     * gradients list.
     * @return the partial derivative of obj/con(<i>index</i>) with respect to
     * variable <i>varName</i>.
     * @throws TooManyDecimalPointsException if thrown by
     * MathExpressionParser.parse(...)
     * @throws MisplacedTokensException if thrown by
     * MathExpressionParser.parse(...)
     * @throws Throwable if thrown by MathExpressionParser.parse(...)
     */
    public Derivative getPartialDerivative(
            int index,
            String vectorName,
            int elementIndex,
            List<AbstractNode[][]> gradientsTargetList) throws
            EvaluationException {
        Iterator<Map.Entry<String, double[]>> it = vm.vectorsIterator();
        for (int i = 0; it.hasNext(); i++) {
            Map.Entry<String, double[]> vecValPair = it.next();
            if (vectorName.equals(vecValPair.getKey())) {
                if (gradientsTargetList.get(index)[i][elementIndex] == null) {
                    // Calculate the derivative numerically
                    double partialDerivative;

                    // Notice that one of the two following if statements MUST
                    // be executed. Because, the gradientsTargetList must be 
                    // either objGradListVariables or conGradListVariables.
                    // That's why the lines following this if-else block should
                    // never throw a NullPointerException.
                    List<AbstractNode> targetList = null;
                    if(gradientsTargetList == objGradListVectors) {
                        targetList = objList;
                    } else if((gradientsTargetList == conGradListVectors)) {
                        targetList = conList;
                    }
                    // Get the obj/con value at the current point
                    double currValue = targetList.get(index).evaluate();
                    // Move your point ahead with distance delta
                    setVectorElement(vectorName, elementIndex, getVectorElement(vectorName, elementIndex) + getDelta());
                    // Get the objective value after shifting
                    double shiftedValue = targetList.get(index).evaluate();
                    // Calculate the partial derivative using  Newton's difference quotient (also known as a first-order divided difference or forward difference)
                    partialDerivative = (shiftedValue - currValue) / getDelta();
                    // Return your point to where it was
                    setVectorElement(vectorName, elementIndex, getVectorElement(vectorName, elementIndex) - getDelta());
                    // Return (only one additional function evaluation is consumed by the forward difference method)
                    return new Derivative(partialDerivative, 1);
                } else {
                    return new Derivative(gradientsTargetList.get(index)[i][elementIndex].evaluate(), 0);
                }
            }
        }
        String message = String.format("The variable (%s) does not exist.",
                vectorName);
        throw new IllegalArgumentException(message);
    }

    /**
     * Execute the command sent as an argument.
     *
     * @param command the text of the command to be added.
     * @throws java.lang.IllegalAccessException
     * @throws exceptions.TooManyDecimalPointsException
     * @throws exceptions.MisplacedTokensException
     */
    public void executeCommand(String command) throws
            IllegalAccessException,
            IllegalArgumentException,
            TooManyDecimalPointsException,
            MisplacedTokensException,
            Throwable {
        AbstractNode parseTree = MathExpressionParser.parse(command, vm);
        parseTree.evaluate();
    }

    @Override
    public String toString() {
        Iterator<Map.Entry<String, Double>> varIt = getVariablesIterator();
        // Variables
        StringBuilder varSb = new StringBuilder();
        while (varIt.hasNext()) {
            Map.Entry<String, Double> entry = varIt.next();
            varSb.append(String.format(
                    "%s=%s", entry.getKey(), entry.getValue()));
            if (varIt.hasNext()) {
                varSb.append(", ");
            }
        }
        // Vectors
        Iterator<Map.Entry<String, double[]>> vecIt = getVectorsIterator();
        StringBuilder vecSb = new StringBuilder();
        while (vecIt.hasNext()) {
            Map.Entry<String, double[]> nameVecPair = vecIt.next();
            vecSb.append(String.format("%s=%s", nameVecPair.getKey(),
                    Arrays.toString(nameVecPair.getValue())));
            if (vecIt.hasNext()) {
                vecSb.append(", ");
            }
        }
        // Objectives
        StringBuilder objSb = new StringBuilder();
        for (int i = 0; i < getObjectivesCount(); i++) {
            // Get the partial derivatives
            StringBuilder parDeriv = new StringBuilder();
            // Get variables partial derivatives
            varIt = getVariablesIterator();
            int varIndex = 0;
            while (varIt.hasNext()) {
                Map.Entry<String, Double> entry = varIt.next();
                String prDv = (objGradListVariables.get(i)[varIndex] == null)
                        ? "none" : objGradListVariables.get(i)[varIndex].toString();
                // Append the string represting the partial derivative of the
                // current variable.
                parDeriv.append(StringUtils.getSpacesFreeText(String.format(
                        "df(%d)/d%s=%s",
                        i,
                        entry.getKey(),
                        prDv)));
                if (varIt.hasNext()) {
                    parDeriv.append(", ");
                }
                // Move to the next variable
                varIndex++;
            }
            // Get vector elements partial derivatives
            vecIt = getVectorsIterator();
            // Set a comma between variables derivatives and vectors elements
            // derivatives.
            if (vecIt.hasNext() && !parDeriv.toString().equals("")) {
                parDeriv.append(", ");
            }
            int vecIndex = 0;
            while (vecIt.hasNext()) {
                StringBuilder vecPrDv = new StringBuilder("[");
                Map.Entry<String, double[]> entry = vecIt.next();
                for (int j = 0; j < entry.getValue().length; j++) {
                    String elementPrDv;
                    if (objGradListVectors.get(i)[vecIndex][j] == null) {
                        elementPrDv = "none";
                    } else {
                        elementPrDv = objGradListVectors.get(i)[vecIndex][j].toString();
                    }
                    vecPrDv.append(elementPrDv);
                    if (j != entry.getValue().length - 1) {
                        vecPrDv.append(",");
                    }
                }
                vecPrDv.append("]");
                // Append the string representing the partial derivatives of all
                // current vector elements.
                parDeriv.append(StringUtils.getSpacesFreeText(String.format(
                        "df(%d)/d%s=%s",
                        i,
                        entry.getKey(),
                        vecPrDv)));
                if (vecIt.hasNext()) {
                    parDeriv.append(", ");
                }
                // Move to the next vector
                vecIndex++;
            }
            // Add the objective and the partial derivatives
            objSb.append(String.format("f(%d) = %s {%s}",
                    i, objList.get(i).toString(), parDeriv.toString()));
            if (i != getObjectivesCount() - 1) {
                objSb.append(", ");
            }
        }
        // Constraints
        StringBuilder constSb = new StringBuilder();
        for (int i = 0; i < getConstraintsCount(); i++) {
            // Get the partial derivatives
            StringBuilder parDeriv = new StringBuilder();
            // Get variables partial derivatives
            varIt = getVariablesIterator();
            int varIndex = 0;
            while (varIt.hasNext()) {
                Map.Entry<String, Double> entry = varIt.next();
                String prDv = (conGradListVariables.get(i)[varIndex] == null)
                        ? "none" : conGradListVariables.get(i)[varIndex].toString();
                parDeriv.append(StringUtils.getSpacesFreeText(String.format(
                        "df(%d)/d%s=%s",
                        i,
                        entry.getKey(),
                        prDv)));
                if (varIt.hasNext()) {
                    parDeriv.append(", ");
                }
                // Move to the next variable
                varIndex++;
            }
            // Get vector elements partial derivatives
            vecIt = getVectorsIterator();
            // Set a comma between variables derivatives and vectors elements
            // derivatives.
            if (vecIt.hasNext() && !parDeriv.toString().equals("")) {
                parDeriv.append(", ");
            }
            int vecIndex = 0;
            while (vecIt.hasNext()) {
                StringBuilder vecPrDv = new StringBuilder("[");
                Map.Entry<String, double[]> entry = vecIt.next();
                for (int j = 0; j < entry.getValue().length; j++) {
                    String elementPrDv;
                    if (conGradListVectors.get(i)[vecIndex][j] == null) {
                        elementPrDv = "none";
                    } else {
                        elementPrDv = conGradListVectors.get(i)[vecIndex][j].toString();
                    }
                    vecPrDv.append(elementPrDv);
                    if (j != entry.getValue().length - 1) {
                        vecPrDv.append(",");
                    }
                }
                vecPrDv.append("]");
                // Append the string representing the partial derivatives of all
                // current vector elements.
                parDeriv.append(StringUtils.getSpacesFreeText(String.format(
                        "df(%d)/d%s=%s",
                        i,
                        entry.getKey(),
                        vecPrDv)));
                if (vecIt.hasNext()) {
                    parDeriv.append(", ");
                }
                // Move to the next vector
                vecIndex++;
            }

            // Add the constraint and the partial derivatives
            constSb.append(String.format("f(%d) = %s {%s}",
                    i, conList.get(i).toString(), parDeriv.toString()));
            if (i != getConstraintsCount() - 1) {
                constSb.append(", ");
            }
        }
        // Integrate all the parts together
        String desc = String.format(
                "Variables{%s} - Vectors{%s} - Objectives{%s} - Constraints{%s}",
                varSb.toString(),
                vecSb.toString(),
                objSb.toString(),
                constSb.toString());
        // Return the full description
        return desc;
    }

    /**
     * For testing purposes.
     *
     * @param args not used
     */
    public static void main(String[] args) throws
            MisplacedTokensException,
            Throwable {
        // Create an optimization problem object
        OptimizationProblem problem = new OptimizationProblem();
        // Variables
        problem.setVariable("x1", 10);
        problem.setVariable("x2", 2);
        problem.setVariable("y1", 3);
        problem.setVariable("y3", 7);
        // Objectives
        problem.addObjective("2*x1+3*y1-y3^2");
        problem.addObjective("2*x1+x2+y1");
        // Constraints
        problem.addConstraint("x2^3-y3");
        problem.addConstraint("x1+y1");
        // Partial Derivatives (objective 1)
        problem.setObjectivePartialDerivative(0, "x1", "2");
        problem.setObjectivePartialDerivative(0, "x2", "0");
        problem.setObjectivePartialDerivative(0, "y1", "3");
        problem.setObjectivePartialDerivative(0, "y3", "0-2*y3");
        // Partial Derivatives (objective 2)
        problem.setObjectivePartialDerivative(1, "x1", "2");
        problem.setObjectivePartialDerivative(1, "x2", "1");
        problem.setObjectivePartialDerivative(1, "y1", "1");
        problem.setObjectivePartialDerivative(1, "y3", "0");
        // Partial Derivatives (constraint 1)
        problem.setConstraintPartialDerivative(0, "x1", "0");
        problem.setConstraintPartialDerivative(0, "x2", "3*x2^2");
        problem.setConstraintPartialDerivative(0, "y1", "0");
        problem.setConstraintPartialDerivative(0, "y3", "0-1");
        // Partial Derivatives (constraint 2)
        problem.setConstraintPartialDerivative(1, "x1", "1");
        problem.setConstraintPartialDerivative(1, "x2", "0");
        problem.setConstraintPartialDerivative(1, "y1", "1");
        problem.setConstraintPartialDerivative(1, "y3", "0");
        // Retrieve variables
        System.out.format("x1 = %f%n", problem.getVariable("x1"));
        System.out.format("x2 = %f%n", problem.getVariable("x2"));
        try {
            System.out.format("x3 = %f%n", problem.getVariable("x3"));
        } catch (Throwable ex) {
            System.out.println(ex.toString());
        }
        System.out.format("y1 = %f%n", problem.getVariable("y1"));
        System.out.format("y3 = %f%n", problem.getVariable("y3"));
        // Retrieve objectives
        System.out.format("obj(0) = %f%n", problem.getObjective(0));
        System.out.format("obj(1) = %f%n", problem.getObjective(1));
        // Retrieve constraints
        System.out.format("con(0) = %f%n", problem.getConstraint(0));
        System.out.format("con(1) = %f%n", problem.getConstraint(1));
        // Retrive partial derivatives
        // First objective
        System.out.format("PD(obj-0,x1) = %f%n", problem.getObjectivePartialDerivative(0, "x1"));
        System.out.format("PD(obj-0,x2) = %f%n", problem.getObjectivePartialDerivative(0, "x2"));
        try {
            System.out.format("PD(obj-0,x3) = %f%n", problem.getObjectivePartialDerivative(0, "x3"));
        } catch (Throwable ex) {
            System.out.println(ex.toString());
        }
        System.out.format("PD(obj-0,y1) = %f%n", problem.getObjectivePartialDerivative(0, "y1"));
        System.out.format("PD(obj-0,y3) = %f%n", problem.getObjectivePartialDerivative(0, "y3"));
        // Second objective
        System.out.format("PD(obj-1,x1) = %f%n", problem.getObjectivePartialDerivative(1, "x1"));
        System.out.format("PD(obj-1,x2) = %f%n", problem.getObjectivePartialDerivative(1, "x2"));
        System.out.format("PD(obj-1,y1) = %f%n", problem.getObjectivePartialDerivative(1, "y1"));
        System.out.format("PD(obj-1,y3) = %f%n", problem.getObjectivePartialDerivative(1, "y3"));
        // First constraint
        System.out.format("PD(con-0,x1) = %f%n", problem.getConstraintPartialDerivative(0, "x1"));
        System.out.format("PD(con-0,x2) = %f%n", problem.getConstraintPartialDerivative(0, "x2"));
        System.out.format("PD(con-0,y1) = %f%n", problem.getConstraintPartialDerivative(0, "y1"));
        System.out.format("PD(con-0,y3) = %f%n", problem.getConstraintPartialDerivative(0, "y3"));
        // Second constraint
        System.out.format("PD(con-1,x1) = %f%n", problem.getConstraintPartialDerivative(1, "x1"));
        System.out.format("PD(con-1,x2) = %f%n", problem.getConstraintPartialDerivative(1, "x2"));
        System.out.format("PD(con-1,y1) = %f%n", problem.getConstraintPartialDerivative(1, "y1"));
        System.out.format("PD(con-1,y3) = %f%n", problem.getConstraintPartialDerivative(1, "y3"));
        // Change all variables at once
        problem.setAllVariables(new double[]{-1, -2, -3, -4});
        // Retreive variables
        System.out.format("x1 = %f%n", problem.getVariable("x1"));
        System.out.format("x2 = %f%n", problem.getVariable("x2"));
        System.out.format("y1 = %f%n", problem.getVariable("y1"));
        System.out.format("y3 = %f%n", problem.getVariable("y3"));
        // Test after retreival
        System.out.format("obj-0(after retieval) = %f%n", problem.getObjective(0));
        System.out.format("obj-1(after retieval) = %f%n", problem.getObjective(1));
        // Change a variable (by name)
        problem.setVariable("y1", 100);
        // Add a variable (by name)
        problem.setVariable("new_variable", 77);
        // Retreive variables (using iterator)
        Iterator<Map.Entry<String, Double>> it = problem.getVariablesIterator();
        while (it.hasNext()) {
            Map.Entry<String, Double> entry = it.next();
            System.out.format("%s = %f%n", entry.getKey(), entry.getValue());
        }
        // Test after retreival
        System.out.format("obj-0(after retieval) = %f%n", problem.getObjective(0));
        System.out.format("obj-1(after retieval) = %f%n", problem.getObjective(1));
    }

    /**
     * @return the delta
     */
    public double getDelta() {
        return delta;
    }

    /**
     * @param delta the delta to set
     */
    public void setDelta(double delta) {
        this.delta = delta;
    }
}
