package com.jrdev.whatsappplatform.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // Ignora campos extra que el cliente mande por error
public class InventarioUniversalResponseDto {

    private boolean exito;
    private String mensaje;
    private List<ProductoItem> datos;

    // Clase interna estática para los ítems de la lista
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProductoItem {
        private String id;
        private String nombre;
        private int cantidad;
        private boolean disponible;
        private double precio;
    }
}