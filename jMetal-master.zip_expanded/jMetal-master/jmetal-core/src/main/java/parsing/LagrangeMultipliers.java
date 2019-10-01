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
public class LagrangeMultipliers {
    
    private final double[] lagrangeMultipliers;
    private final int funEvalCount;

    public LagrangeMultipliers(double[] lagrangeMultipliers, int evalVount) {
        this.lagrangeMultipliers = lagrangeMultipliers;
        this.funEvalCount = evalVount;
    }

    /**
     * @return the lagrangeMultipliers
     */
    public double[] getLagrangeMultipliers() {
        return lagrangeMultipliers;
    }

    /**
     * @return the funEvalCount
     */
    public int getFunEvalCount() {
        return funEvalCount;
    }
}
