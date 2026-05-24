package com.example.hortlink.util;

public class DistanciaUtil {
    /**
     * Fórmula de Haversine — calcula distância em km entre dois pontos geográficos.
     */
    public static double calcularKm(double lat1, double lon1, double lat2, double lon2) {
        final double RAIO_TERRA_KM = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return RAIO_TERRA_KM * c;
    }

}
