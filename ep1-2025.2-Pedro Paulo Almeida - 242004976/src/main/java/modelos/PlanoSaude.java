package main.java.modelos;

import java.io.Serializable;

public class PlanoSaude implements Serializable {
    private String nome;
    private double descontoGeral; 
    private boolean internaGarantida; 
    public PlanoSaude(String nome, double descontoGeral, boolean internaGarantida) {
        this.nome = nome;
        this.descontoGeral = descontoGeral;
        this.internaGarantida = internaGarantida;
    }

    // Getters para encapsulamento e regras de negócio
    public String getNome() { return nome; }
    public double getDescontoGeral() { return descontoGeral; }
    public boolean isInternaGarantida() { return internaGarantida; }

    @Override
    public String toString() {
        return nome;
    }
}
