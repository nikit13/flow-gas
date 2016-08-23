package ru.spb.iec.instructor.gas;

/**
 * Мамулечке привет
 * 
 * @author Alexey Pismak
 * @category Mathematical
 */
public class TgdCalcModule {

    /* international standard atmosphere */
    private static final double[][] isa = { { 288.15, 1.01325 }, { 281.65, 0.89876 }, { 275.15, 0.79501 },
            { 268.66, 0.70121 }, { 262.17, 0.6166 }, { 255.68, 0.54048 }, { 249.19, 0.47218 }, { 242.7, 0.41105 },
            { 236.22, 0.35652 }, { 229.73, 0.30801 }, { 223.25, 0.265 }, { 216.77, 0.227 }, { 216.65, 0.19399 },
            { 216.65, 0.1658 }, { 216.65, 0.1417 }, { 216.65, 0.12112 } };

    public static double[][] getIsa() {
        return isa.clone();
    }

    /**
     * Функция вычисления работы компрессора
     * 
     * @param pi_k
     *            степень повышения давления
     * @param tv
     *            температура воздуха на входе в компрессор
     * @param eta_k
     *            к.п.д. компрессора
     * @param k
     *            - показатель адиабаты (may be)
     * @return работа компрессора [Дж/кг] или -1, если ошибка данных
     */
    public static double Lk_calc(double pi_k, double tv, double eta_k, double k) {
        double magicNumber = 1005;

        /* k == 0 */
        if (Math.abs(k) < 1e-10) {
            k = 1.4D;
        }
        if (eta_k < 1e-10) /* <= */ {
            throw new IllegalStateException("КПД не может иметь значение " + eta_k);
        } else if (pi_k < 1) {
            throw new IllegalStateException("Степень повышения давления воздуха в компрессоре не может быть меньше 1");
        } else if (tv < 1e-10) /* <= */ {
            throw new IllegalStateException("Абсолютная температура не может быть меньше 0");
        } else {
            return magicNumber * tv * (Math.pow(pi_k, ((k - 1) / k)) - 1) / eta_k;
        }
    }

    /**
     * Функция вычисления температуры по известной работе
     * 
     * @param lk
     *            работа, подведенная к 1 кг газа
     * @param tv
     *            температура воздуха на входе в компрессор
     * @param bPres
     *            <code>true</code> если сжатие воздуха, иначе
     *            <code>false</code>
     * @param k
     *            показатель адиабаты
     * @return температура в выходном сечении [K] или -1, если ошибка данных
     */
    public static double tk_calk(double lk, double tv, boolean bPres, double k) {
        double smallMagicNumber = 1005;
        double largeMagicNumber = 1155;

        if (Math.abs(k) < 1e-10) {
            k = bPres ? 1.4D : 1.33D;
        }
        double result = -1;
        if (lk < 0) {
            throw new IllegalStateException("Работа в термодинамическом процессе должна больше 0");
        } else if (tv < 1e-10) {
            throw new IllegalStateException("Абсолютная температура не может быть меньше 0");
        } else {
            if (bPres) {
                if (k - 1.4 < 1e-10) {
                    result = tv + lk / smallMagicNumber;
                } else if (k - 1.33 < 1e-10) {
                    result = tv + lk / largeMagicNumber;
                }
            } else {
                if (k - 1.4 < 1e-10) {
                    result = tv - lk / smallMagicNumber;
                } else if (k - 1.33 < 1e-10) {
                    result = tv - lk / largeMagicNumber;
                }
            }
        }
        return result;
    }

    /**
     * Функция определения относительного расхода топлива m_t в предположении,
     * что 2% тепла недопулучаем из-за несовершества организации процесса
     * сгорания углеводородного топлива (Hu=43100 кДж.кг) по известным
     * температурам на входе в к.с и выходной температуре газа
     * 
     * @param tk
     *            температура на входе в камеру сгорания
     * @param tg
     *            температура газа на выходе из камеры сгорания
     * @param eta_kc
     *            показатель адиабаты
     * @return относительный расход топлива или -1, если нет результата расчета
     */
    public static double m_t_calc(double tk, double tg, double eta_kc) {
        if (Math.abs(eta_kc) < 1e-10) {
            eta_kc = 0.98D;
        }
        double result = -1;
        if (tk < 0) {
            throw new IllegalStateException("Абсолютная температура не может быть меньше 0");
        } else if (tg < 1e-10) {
            throw new IllegalStateException("Коэффициент полноты сгорания не моожет быть больше 1");
        } else if (eta_kc > 1) {
            throw new IllegalStateException("Коэффициент полноты сгорания не моожет быть больше 1");
        } else {
            double tmp = 1145 + 0.245 * (tg - 1000) * (tg - tk);
            if (eta_kc - 0.98 < 1e-10) {
                result = tmp / (43100000 * 0.98);
            } else {
                result = tmp / 43100000 / eta_kc;
            }
        }
        return result;
    }

    /**
     * Функция определения pi_t
     * 
     * @param lt
     *            работа турбины
     * @param tg
     *            температура газа на входе в турбину
     * @param eta_t
     *            к.п.д турбины
     * @param k
     *            непонятно чёйто
     * @return степень понижения давления газов в турбине или -1, если нет
     *         результата расчета
     */
    public static double pi_t_calc(double lt, double tg, double eta_t, double k) {
        double magicNumber = 1155;
        if (Math.abs(k) < 1e-10) {
            k = 1.33D;
        }
        if (eta_t < 1e-10 || eta_t > 1) {
            throw new IllegalStateException("Значение КПД должно лежать в пределах 0...1");
        }
        double result = -1;
        if (tg < 0) {
            throw new IllegalStateException("Абсолютная температура не может быть меньше 0");
        } else if (k < 0) {
            throw new IllegalStateException("Показатель адиабаты для газа не может быть отрицательным числом");
        } else {
            double f_pi_st_k = lt / (eta_t * magicNumber * tg);
            if (1 - f_pi_st_k < 0) {
                return result;
            } else {
                result = Math.pow((1 / (1 - f_pi_st_k)), (k / (k - 1)));
            }
        }
        return result;
    }

    /**
     * Функция вычисления работы турбины
     * 
     * @param pi_t
     *            степень понижения давления в турбине
     * @param tg
     *            температура газана входе в турбину
     * @param eta_t
     *            к.п.д. турбины
     * @param k
     *            непонятно чёйто
     * @return работа турбины [Дж/кг] или -1, если ошибка данных
     */
    public static double tl_calc(double pi_t, double tg, double eta_t, double k) {
        double multKoeff = 1005;
        k = 1.4D;
        if (Math.abs(k) < 1e-10) {
            k = 1.33D;
            multKoeff = 1155;
        }
        if (eta_t < 1e-10) {
            throw new IllegalStateException("К.П.Д. неможет иметь значение " + eta_t);
        } else if (pi_t < 1) {
            throw new IllegalStateException("Степень понижения давления газа в турбине не может быть меньше 1");
        } else if (tg < 1e-10) {
            throw new IllegalStateException("Абсолютная температура не может быть меньше 0");
        } else {
            return multKoeff * tg * ((1 - 1 / (Math.pow(pi_t, ((k - 1) / k))))) * eta_t;
        }
    }

    /**
     * Функция вычисления скорости истечения газа из сопла
     * 
     * @param pi_c
     *            степень понижения давления газа в сопле
     * @param tg
     *            температура газа на на входе в сопло
     * @param fi_rs
     *            коэффициент истечения из сопла
     * @param k
     *            снова тут этот мусор...
     * @return скорость истечения [м/с] или -1, если ошибка данных
     */
    public static double Cc_calc(double pi_c, double tg, double fi_rs, double k) {
        double r = 287.5;
        if (Math.abs(k) < 1e-10 || k - 1.33 < 1e-10) {
            k = 1.33;
            r = 289;
        } else if (k - 1.4 < 1e-10) {
            r = 287.5;
        }
        if (fi_rs < 1e-10) {
            throw new IllegalStateException("Коэффициент истечения газа из сопла неможет иметь значение " + fi_rs);
        } else if (pi_c < 1) {
            throw new IllegalStateException("Степень понижения давления газа в сопле не может быть меньше 1");
        } else if (tg < 1e-10) {
            throw new IllegalStateException("Абсолютная температура не может быть меньше 0");
        } else {
            return fi_rs * Math.sqrt((2 * k * r * tg / (k - 1)) * (1 - 1 / Math.pow(pi_c, ((k - 1) / k))));
        }
    }

    /**
     * Функция определения Пк по известной работе
     * 
     * @param lk
     *            работа компрессора
     * @param tb
     *            температура на входе в компрессор
     * @param eta_k
     *            к.п.д. компрессора
     * @param k
     * @return pi_k если успешно определно или -1 если нахождение не возможно
     */
    public static double pi_k_lk(double lk, double tb, double eta_k, double k) {
        double cp = 0;
        if (Math.abs(k) < 1e-10) {
            k = 1.4;
            cp = 1005;
        } else if (k - 1.33 < 1e-10) {
            cp = 1150;
        }
        return Math.pow((lk * eta_k / (cp * tb) + 1), (k / (k - 1)));
    }

    /**
     * Вычисление Ср по заданным температуре в начале и конце процесса
     * расширения газа (имеет смысл учитывать при температурах более 1000К)
     * 
     * @param tb
     *            начальная температура
     * @param te
     *            конечная температура
     * @return Cp
     */
    public static double calc_cp(double tb, double te) {
        if (te - 1000 > 1e-10) {
            return 1145 + 0.245 * (tb - 1000);
        } else {
            return 1155;
        }
    }

    /**
     * Расчет постоянной для определения расхода газа
     * 
     * @param k
     *            показатель адиабаты
     * @return постоянная расхода газа, или -1, если расчет невозможен
     */
    public static double calc_m(double k) {
        double r = 0;
        if (Math.abs(k - 1.4) < 1e-10) {
            r = 287.5;
        } else if (Math.abs(k - 1.33) < 1e-10) {
            r = 289;
        } else {
            throw new IllegalStateException("Недопустимое значение показателя адиабаты");
        }
        return Math.sqrt(k / r * Math.pow((2 / (k + 1)), ((k + 1) / (k - 1))));
    }

    /**
     * Расчет газодинамической функции q(lambda)
     * 
     * @param lambda
     *            относительная скорость
     * @param k
     * @return относительная плотность тока
     */
    public static double calc_q_lambda(double lambda, double k) {
        if (Math.abs(k) < 1e-10) {
            k = 1.4;
        }
        if (lambda < 0 || lambda - 2.45 > 1e-10) {
            throw new IllegalStateException("Некорректное значение относительной скорости течения газа");
        } else {
            double tmp = Math.pow(((k + 1) / 2), (1 / (k - 1)));
            return tmp * Math.pow((1 - (k - 1) / (k + 1) * Math.pow(lambda, 2)), (1 / (k - 1))) * lambda;
        }
    }

    /**
     * Расчет газодинамической функции pi(lambda)
     * 
     * @param lambda
     *            относительная скорость
     * @param k
     * @return функция статического давления
     */
    public static double calc_pi_lambda(double lambda, double k) {
        if (Math.abs(k) < 1e-10) {
            k = 1.4;
        }
        if (lambda < 0 || lambda > 2.45) {
            throw new IllegalStateException("Некорректное значение относительной скорости течения газа");
        } else {
            double a = 1 - (k - 1) / (k + 1) * Math.pow(lambda, 2);
            return Math.pow(a, (k / (k - 1)));
        }
    }

    /**
     * Функция, которая, я надеюсь, не нужна...
     * 
     * @param lambda
     *            относительная скорость
     * @param k
     * @return че то там
     */
    public static double calc_tau_lambda(double lambda, double k) {
        if (Math.abs(k) < 1e-10) {
            k = 1.4;
        }
        if (lambda < 0 || lambda > 2.45) {
            throw new IllegalStateException("Некорректное значение относительной скорости течения газа");
        } else {
            return 1 - (k - 1) / (k + 1) * Math.pow(lambda, 2);
        }
    }

    public static double SearchLambda(double qLambdaZ, boolean bZvuk) {
        final double k = 1.4;
        double lambda_i = -1;
        double err_ql = 0.01;
        double eps = 0.005;
        double qLambdaR;

        if (qLambdaZ <= 0 || qLambdaZ > 1) {
            throw new IllegalStateException("Относительная плотность не может иметь значение " + qLambdaZ);
        }

        if (!bZvuk) {
            lambda_i = (qLambdaZ <= 0.8) ? qLambdaZ / 1.5 : qLambdaZ / 1.5 + 0.1;
            while (Math.abs(err_ql) > eps) {
                qLambdaR = Math.pow((k + 1) / 2, 1 / (k - 1)) * lambda_i
                        * Math.pow(1 - ((k - 1) / (k + 1) * Math.pow(lambda_i, 2)), 1 / (k - 1));
                err_ql = (qLambdaR - qLambdaZ) / qLambdaZ;
                lambda_i = lambda_i - lambda_i * err_ql / 1.5;
            }
        } else {
            lambda_i = (qLambdaZ <= 0.8) ? 2 - qLambdaZ / 1.5 : 2 - qLambdaZ / 1.5 + 0.15;
            while (Math.abs(err_ql) > eps) {
                qLambdaR = Math.pow((k + 1) / 2, 1 / (k - 1)) * lambda_i
                        * Math.pow(1 - ((k - 1) / (k + 1) * Math.pow(lambda_i, 2)), 1 / (k - 1));
                err_ql = (qLambdaR - qLambdaZ) / qLambdaZ;
                lambda_i = lambda_i + lambda_i * err_ql / 8;
            }
        }

        return lambda_i;
    }
}