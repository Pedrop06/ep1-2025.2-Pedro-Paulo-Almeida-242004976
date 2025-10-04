package main.java.modelos;

import java.io.Serializable;

public abstract class Pessoa implements Serializable {
    private String nome;
    private String cpf;
    private int idade;

    public Pessoa(String nome, String cpf, int idade) {
        this.nome = nome;
        this.cpf = cpf;
        this.idade = idade;
    }

    // Getters e Setters 
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public int getIdade() { return idade; }
    
    // Método abstrato
    public abstract String exibirDetalhes();

    @Override
    public String toString() {
        return nome + ";" + cpf + ";" + idade; 
    }
}