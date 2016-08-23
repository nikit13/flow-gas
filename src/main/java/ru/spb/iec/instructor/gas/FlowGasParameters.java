package ru.spb.iec.instructor.gas;

public class FlowGasParameters {

    private final double m = 0.0404;
    private final double k = 1.4;
    private final double R = 287.5;

    public double computeAkr(double fullT) {
        return Math.sqrt((2 * k / (k + 1)) * R * fullT);
    }

    public double computeA(double diameter, double fullP, double fullT, double gv) {
        final double t = computeT(diameter, fullP, fullT, gv);
        return Math.sqrt(k * R * t);
    }

    public double computeC(double diameter, double fullP, double fullT, double gv) {
        final double a_kr = computeAkr(fullT);
        // площадь сечения
        final double Fi = Math.PI * diameter * diameter / 4;
        final double q_lambda_i = getQLambdaI(fullP, fullT, gv, Fi);

        final double lambda_i = getLambdaI(q_lambda_i);

        return lambda_i * a_kr;
    }

    public double computeP(double diameter, double fullP, double fullT, double gv) {
        // площадь сечения
        final double Fi = Math.PI * diameter * diameter / 4;
        final double q_lambda_i = getQLambdaI(fullP, fullT, gv, Fi);

        final double lambda_i = getLambdaI(q_lambda_i);

        final double pi_lambda = TgdCalcModule.calc_pi_lambda(lambda_i, k);
        return pi_lambda * fullP;
    }

    private double getQLambdaI(double fullP, double fullT, double gv, final double Fi) {
        return gv * Math.sqrt(fullT) / (m * fullP * 1e5 * Fi);
    }

    public double computeT(double diameter, double fullP, double fullT, double gv) {
        // площадь сечения
        final double Fi = Math.PI * diameter * diameter / 4;
        final double q_lambda_i = getQLambdaI(fullP, fullT, gv, Fi);

        final double lambda_i = getLambdaI(q_lambda_i);

        final double tau_lambda_i = TgdCalcModule.calc_tau_lambda(lambda_i, k);
        return tau_lambda_i * fullT;
    }

    protected double getLambdaI(final double q_lambda_i) {
        return (q_lambda_i != 0) ? TgdCalcModule.SearchLambda(q_lambda_i, false) : 0;
    }

    public double getStaticGaugePressure(double diameter, double fullP, double fullT, double gv, double ph) {
        return computeP(diameter, fullP, fullT, gv) - ph;
    }
}
