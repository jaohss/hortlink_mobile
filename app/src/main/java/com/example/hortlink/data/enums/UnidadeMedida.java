package com.example.hortlink.data.enums;

public enum UnidadeMedida {
    KILOGRAMA("Kg", "Quilograma"),
    GRAMA("g", "Grama"),
    CAIXA("Cx", "Caixa"),
    DUZIA("Dz", "Duzia"),
    BANDEJA("Bandeja", "Bandeja"),
    UNIDADE("Un", "Unidade"),
    MOLHO("Molho", "Molho");

    // Propriedades do Enum
    private final String simbolo;
    private final String label;

    // Construtor
    UnidadeMedida(String simbolo, String label) {
        this.simbolo = simbolo;
        this.label = label;
    }

    // Getters para usar na sua aplicação
    public String getSimbolo() {
        return simbolo;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public String toString() {
        // É isso que vai aparecer escrito na lista do produtor!
        return label + " (" + simbolo + ")";
        // Exemplo: "Quilograma (Kg)"
    }

}
