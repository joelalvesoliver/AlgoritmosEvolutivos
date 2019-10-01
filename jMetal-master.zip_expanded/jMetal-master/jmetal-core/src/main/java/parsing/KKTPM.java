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
public class KKTPM {

    private final double kktpm;
    private final int funEvalCount;

    public KKTPM(double kktpm, int funEvalCount) {
        this.kktpm = kktpm;
        this.funEvalCount = funEvalCount;
    }

    /**
     * @return the kktpm
     */
    public double getKktpm() {
        return kktpm;
    }

    /**
     * @return the funEvalCount
     */
    public int getFunEvalCount() {
        return funEvalCount;
    }
}
