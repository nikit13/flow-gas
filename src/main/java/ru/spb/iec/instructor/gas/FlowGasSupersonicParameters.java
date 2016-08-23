package ru.spb.iec.instructor.gas;

public class FlowGasSupersonicParameters extends FlowGasParameters {

    private final boolean supersonic;

    public FlowGasSupersonicParameters(boolean supersonic) {
        this.supersonic = supersonic;
    }

    @Override
    protected double getLambdaI(double q_lambda_i) {
        if (Math.abs(q_lambda_i) >1e-9) {
            return TgdCalcModule.SearchLambda(q_lambda_i, supersonic);
        } else {
            return 0;
        }
    }
}
