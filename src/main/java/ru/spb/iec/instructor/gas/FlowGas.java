package ru.spb.iec.instructor.gas;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class FlowGas implements Initializable {

    private final double D2 = 0.4;
    private final double D1 = 0.65;
    private final double length = 0.60; // длина сопла
    private final double l = 0.025;

    private final FlowGasParameters parameters = new FlowGasParameters();

    Stage mainStage;

    @FXML
    Slider fullP;

    @FXML
    Slider fullT;

    @FXML
    Spinner<Number> flyHeight;

    @FXML
    Slider nozzleDiameter;

    @FXML
    Label nozzleDiameterLabel;

    @FXML
    Label fullPLabel;

    @FXML
    Label fullTLabel;

    @FXML
    TextField gv;

    @FXML
    TextField availableReduction;

    @FXML
    TextField realReduction;

    @FXML
    TextField staticGaugePressure;

    @FXML
    TextField staticAbsolutePressure;

    @FXML
    TextField gaugePressure;

    @FXML
    Label gaugePressureLabel;

    @FXML
    TextField ph;

    @FXML
    LineChart<Double, Double> temperatureGraphic;

    @FXML
    LineChart<Double, Double> pressureGraphic;

    @FXML
    LineChart<Double, Double> accelerationGraphic;

    private boolean subsonic;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        changeP();
        changeT();
        changeNozzleDiameter();
        fullP.valueProperty().addListener((observable, oldValue, newValue) -> changeP());
        fullT.valueProperty().addListener((observable, oldValue, newValue) -> changeT());
        nozzleDiameter.valueProperty().addListener((observable, oldValue, newValue) -> changeNozzleDiameter());
        setSubsonicNozzle();

        flyHeight.valueProperty().addListener((obs, o, n) -> compute());
        fullP.valueProperty().addListener((obs, o, n) -> compute());
        fullT.valueProperty().addListener((obs, o, n) -> compute());
        nozzleDiameter.valueProperty().addListener((obs, o, n) -> compute());
        compute();
    }

    public void setMainStage(Stage mainStage) {
        this.mainStage = mainStage;
    }

    @FXML
    public void exitApplication() {
        mainStage.close();
    }

    @FXML
    public void setSubsonicNozzle() {
        changeNozzleType(true);
        gaugePressure.clear();
    }

    @FXML
    public void setSupersonicNozzle() {
        changeNozzleType(false);
    }

    public boolean isSubsonic() {
        return subsonic;
    }

    private void changeNozzleType(boolean subsonic) {
        this.subsonic = subsonic;
        nozzleDiameterLabel.setDisable(subsonic);
        nozzleDiameter.setDisable(subsonic);
        gaugePressureLabel.setDisable(subsonic);
        gaugePressure.setDisable(subsonic);
    }

    /**
     * @return высота полета (км)
     */
    public int getFlyHeight() {
        return flyHeight.valueProperty().get().intValue();
    }

    /**
     * Произвести расчет
     */
    public void compute() {
        computePh();
        computeAvailableReduction();
        computeRealReduction();
        computeStaticGaugePressure();
        computeStaticAbsolutePressure();
        computeGv();
        if (!isSubsonic()) {
            computeGaugePressure();
        }
    }

    /**
     * Посчитать атмосферное давление
     */
    public void computePh() {
        ph.textProperty().set(String.format("%.2f", getStandartPh()));
    }

    /**
     * Посчитать располагаемую степень понижения давления
     */
    public void computeAvailableReduction() {
        availableReduction.textProperty().set(String.format("%.2f", getAvailableReduction()));
    }

    /**
     * @return располагаемая степень понижения давления
     */
    private double getAvailableReduction() {
        return getAbsoluteFullP() / getStandartPh();
    }

    /**
     * Посчитать реальную степень понижения давления
     */
    public void computeRealReduction() {
        realReduction.textProperty().set(String.format("%.2f", getRealReduction()));
    }

    /**
     * Посчитать статическое абсолютное давление на срезе сопла
     */
    public void computeStaticAbsolutePressure() {
        staticAbsolutePressure.textProperty().set(String.format("%.2f", getStaticAbsolutePressure()));
    }

    public void setStaticGaugePressure(double value) {
        staticGaugePressure.textProperty().set(String.format("%.2f", value));
    }

    /**
     * Посчитать расход топлива
     */
    public void computeGv() {
        gv.textProperty().set(String.format("%.2f", getGv()));
    }

    /**
     * @return реальная степень понижения давления
     */
    public double getRealReduction() {
        return Math.min(getAvailableReduction(), 1.89);
    }

    /**
     * @param h
     *            высота (км)
     * @return атмосферное давление
     */
    public double getStandartPh(int h) {
        return TgdCalcModule.getIsa()[h][1];
    }

    /**
     * @return абсолютное давление на входе в сопло
     */
    public double getAbsoluteFullP() {
        return getFullP() + getStandartPh();
    }

    public double getFullP() {
        return round(fullP.valueProperty().doubleValue(), 2);
    }

    public double getFullT() {
        return round(fullT.valueProperty().doubleValue(), 0);
    }

    /**
     * Отрисовка графика температуры
     */
    @FXML
    public void drawGraphics() {
        Series<Double, Double> seriesA = new Series<>();
        seriesA.setName("a");
        Series<Double, Double> seriesC = new Series<>();
        seriesC.setName("C");
        Series<Double, Double> seriesT = new Series<>();
        seriesT.setName("T");
        Series<Double, Double> seriesP = new Series<>();
        seriesP.setName("p");

        // длина шага в метрах
        final double ntag = (D1 - D2) / 2 / length;

        final double fullP = getAbsoluteFullP();
        final double fullT = getFullT();
        final double gv = getGv();

        for (double y = 0; y <= length; y += l) {
            // диаметр сечения
            final double diameter = D2 + 2 * (length - y) * ntag;

            final double c = parameters.computeC(diameter, fullP, fullT, gv);
            final double p = parameters.computeP(diameter, fullP, fullT, gv);
            final double t = parameters.computeT(diameter, fullP, fullT, gv);
            final double a = parameters.computeA(diameter, fullP, fullT, gv);

            final double x = y;
            seriesA.getData().add(new Data<>(x, a));
            seriesC.getData().add(new Data<>(x, c));
            seriesT.getData().add(new Data<>(x, t));
            seriesP.getData().add(new Data<>(x, p));

            // if (y == length) {
            // setStaticGaugePressure(p - getStandartPh());
            // }
        }

        if (!isSubsonic()) {
            final double d = getD() / 100;
            final double tg = (d - D2) / 2 / length;

            FlowGasParameters parameters = new FlowGasSupersonicParameters(
                    getStaticAbsolutePressure() > getStandartPh());

            for (double y = 0; y < length; y += l) {
                final double diameter = D2 + 2 * y * tg;

                final double c = parameters.computeC(diameter, fullP, fullT, gv);
                final double p = parameters.computeP(diameter, fullP, fullT, gv);
                final double t = parameters.computeT(diameter, fullP, fullT, gv);
                final double a = parameters.computeA(diameter, fullP, fullT, gv);

                final double x = y + length;
                seriesA.getData().add(new Data<>(x, a));
                seriesC.getData().add(new Data<>(x, c));
                seriesT.getData().add(new Data<>(x, t));
                seriesP.getData().add(new Data<>(x, p));
            }
        }

        clearGraphics();
        temperatureGraphic.getData().add(seriesT);
        accelerationGraphic.getData().add(seriesA);
        accelerationGraphic.getData().add(seriesC);
        pressureGraphic.getData().add(seriesP);
    }

    public void computeStaticGaugePressure() {
        setStaticGaugePressure(
                parameters.getStaticGaugePressure(D1, getAbsoluteFullP(), getFullT(), getGv(), getStandartPh()));
    }

    public void computeGaugePressure() {
        FlowGasSupersonicParameters parameters = new FlowGasSupersonicParameters(
                getStaticAbsolutePressure() > getStandartPh());
        setGaugePressure(parameters.getStaticGaugePressure(getD() / 100, getAbsoluteFullP(), getFullT(), getGv(),
                getStandartPh()));
    }

    public void setGaugePressure(double value) {
        gaugePressure.textProperty().set(String.format("%.3f", value));
    }

    private double getStandartPh() {
        return getStandartPh(getFlyHeight());
    }

    @FXML
    public void clearGraphics() {
        temperatureGraphic.getData().clear();
        accelerationGraphic.getData().clear();
        pressureGraphic.getData().clear();
    }

    /**
     * @return статическое абсолютное давление на срезе сопла
     */
    public double getStaticAbsolutePressure() {
        final double availableReduction = getAvailableReduction();
        final double absoluteFullP = getAbsoluteFullP();
        final double standartPh = getStandartPh();
        return (availableReduction > 1.89) ? absoluteFullP / 1.89 : standartPh;
    }

    /**
     * @return расход газа через сопло
     */
    public double getGv() {
        final double absoluteFullP = getAbsoluteFullP(); // p1_full
        final double fullT = getFullT(); // T_full
        final double realReduction = getRealReduction(); // pi_r
        final double k = 1.4;
        final double c2 = TgdCalcModule.Cc_calc(realReduction, fullT, 1, k);
        final double R = 287.5;
        final double a_kr = Math.sqrt((2 * k / (k + 1)) * R * fullT);
        final double lambda2 = c2 / a_kr;
        final double q_lambda = TgdCalcModule.calc_q_lambda(lambda2, k);
        final double D2 = 0.4;
        final double F2 = Math.PI * Math.pow(D2, 2) / 4;
        return 0.0404 * absoluteFullP * 1e5 * F2 * q_lambda / Math.sqrt(fullT);
    }

    /**
     * @return диаметер сопла (см)
     */
    public double getD() {
        return round(nozzleDiameter.valueProperty().doubleValue(), 0);
    }

    private void changeP() {
        this.fullPLabel.textProperty().set("P = " + String.format("%.2f", getFullP()) + " Бар");
    }

    private void changeT() {
        this.fullTLabel.textProperty().set("T = " + String.format("%.0f", getFullT()) + " K");
    }

    private void changeNozzleDiameter() {
        this.nozzleDiameterLabel.textProperty().set("D = " + String.format("%.0f", getD()) + " см");
    }

    public static double round(double value, int scale) {
        return Math.round(value * Math.pow(10, scale)) / Math.pow(10, scale);
    }

}
