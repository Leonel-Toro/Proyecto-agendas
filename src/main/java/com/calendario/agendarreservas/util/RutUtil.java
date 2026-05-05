package com.calendario.agendarreservas.util;

public final class RutUtil {

    private RutUtil() {}

    public static boolean esValido(String rut) {
        if (rut == null || rut.isBlank()) return false;

        String clean = rut.replaceAll("[.\\s]", "").toUpperCase();

        if (!clean.matches("\\d{7,8}-[\\dK]")) return false;

        int dash = clean.indexOf('-');
        String numStr = clean.substring(0, dash);
        char dv = clean.charAt(dash + 1);

        int esperado = calcularDigito(numStr);
        char esperadoChar = esperado == 10 ? 'K' : (char) ('0' + esperado);

        return dv == esperadoChar;
    }

    private static int calcularDigito(String numStr) {
        int[] mult = {2, 3, 4, 5, 6, 7};
        int sum = 0;
        for (int i = numStr.length() - 1, idx = 0; i >= 0; i--, idx++) {
            sum += (numStr.charAt(i) - '0') * mult[idx % 6];
        }
        int result = 11 - (sum % 11);
        return result == 11 ? 0 : result;
    }
}
