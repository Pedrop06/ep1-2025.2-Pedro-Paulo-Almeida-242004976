package main.java.modelos;


import java.util.ArrayList;
import java.util.List;

// Herança: Paciente herda de Pessoa
public class Paciente extends Pessoa {
    private List<Consulta> historicoConsultas;
    private List<Internacao> historicoInternacoes;

    public Paciente(String nome, String cpf, int idade) {
        super(nome, cpf, idade);
        this.historicoConsultas = new ArrayList<>();
        this.historicoInternacoes = new ArrayList<>();
    }

    // Getters
    public List<Consulta> getHistoricoConsultas() { return historicoConsultas; }
    public List<Internacao> getHistoricoInternacoes() { return historicoInternacoes; }

    public void adicionarConsulta(Consulta c) { this.historicoConsultas.add(c); }
    public void adicionarInternacao(Internacao i) { this.historicoInternacoes.add(i); }

    // Polimorfismo: Implementação do método abstrato
    @Override
    public String exibirDetalhes() {
        // Exibe detalhes básicos e o tipo
        return super.toString() + " - Tipo: Comum";
    }

    // Polimorfismo: Regra de desconto - Método que será sobrescrito
    public double calcularCustoConsulta(double custoBaseMedico) {
        // Regra de Negócio: Desconto para 60+ anos
        double descontoIdoso = (this.getIdade() >= 60) ? 0.15 : 0.0;
        return custoBaseMedico * (1.0 - descontoIdoso);
    }
    
    // Formato para CSV (Tipo;Nome;CPF;Idade;PlanoNome)
    public String toCSV() {
        return "PACIENTE_C;" + super.toString() + ";N/A";
    }
}