/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parsing;

/**
 *
 * @author seadahai
 */
public class Derivative {
    
    private final double derivative;
    private final int funEvalCount;

    public Derivative(double derivative, int funCount) {
        this.derivative = derivative;
        this.funEvalCount = funCount;
    }
    
    /**
     * @return the derivative
     */
    public double getDerivative() {
        return derivative;
    }

    /**
     * @return the funEvalCount
     */
    public int getFunEvalCount() {
        return funEvalCount;
    }
}