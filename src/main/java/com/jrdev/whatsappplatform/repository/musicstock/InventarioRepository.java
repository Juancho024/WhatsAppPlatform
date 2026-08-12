package com.jrdev.whatsappplatform.repository.musicstock;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public class InventarioRepository {

    private final JdbcTemplate jdbcTemplate;

    // Aquí le decimos: "Usa la conexión de MusicStock, NO la de WhatsApp"
    public InventarioRepository(@Qualifier("musicStockJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public String obtenerCatalogoFormateado() {
        // Tu misma consulta SQL exacta, directo de tu DAO de MusicStock
        String sql = "SELECT " +
                "p.nombre, " +
                "p.precioVenta, " +
                "p.esSeriable, " +
                "p.stock, " +
                "CASE " +
                "    WHEN p.estado = 'DESCONTINUADO' THEN 'DESCONTINUADO' " +
                "    WHEN p.esSeriable = true AND COUNT(CASE WHEN a.estado = 'DISPONIBLE' THEN 1 END) > 0 THEN 'DISPONIBLE' " +
                "    WHEN p.esSeriable = true AND COUNT(CASE WHEN a.estado = 'DISPONIBLE' THEN 1 END) = 0 THEN 'AGOTADO' " +
                "    ELSE p.estado " +
                "END AS estado_calculado, " +
                "COUNT(CASE WHEN a.estado = 'DISPONIBLE' THEN 1 END) AS stock_calculado " +
                "FROM productos p " +
                "LEFT JOIN articulos a ON p.idProducto = a.idProducto AND p.esSeriable = true " +
                "GROUP BY p.idProducto, p.nombre, p.precioVenta, p.esSeriable, p.stock, p.estado";

        List<String> lineasCatalogo = jdbcTemplate.query(sql, (rs, rowNum) -> {
            String nombre = rs.getString("nombre");
            double precio = rs.getDouble("precioVenta");
            boolean esSeriable = rs.getBoolean("esSeriable");
            String estado = rs.getString("estado_calculado");

            // Calculamos el stock real
            int stockFinal = esSeriable ? rs.getInt("stock_calculado") : rs.getInt("stock");
            boolean disponible = stockFinal > 0 && !"DESCONTINUADO".equals(estado) && !"AGOTADO".equals(estado);

            // Armamos el texto para el WhatsApp
            return "📦 *" + nombre + "*\n" +
                    "💵 Precio: $" + precio + "\n" +
                    "📊 Estado: " + (disponible ? "En Stock (" + stockFinal + " disp.)" : "Agotado") + "\n";
        });

        if (lineasCatalogo.isEmpty()) {
            return "No encontré productos disponibles en este momento.";
        }

        StringBuilder sb = new StringBuilder("✅ *Catálogo de MusicStock:*\n\n");
        for (String linea : lineasCatalogo) {
            sb.append(linea).append("\n");
        }
        return sb.toString();
    }
}