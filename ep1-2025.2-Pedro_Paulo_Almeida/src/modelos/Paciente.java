package modelos;


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

    //Implementação do método abstrato
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
        
     //toCSV para Paciente Comum
    public String toCSV() {
        // Formato: TIPO;NOME;CPF;IDADE
        return "PACIENTE_C;" + super.toString(); 
    }
    
    //fromCSV para Paciente (Método de fábrica)
    public static Paciente fromCSV(String csvLine, List<PlanoSaude> planos) {
        String[] parts = csvLine.split(";");
        if (parts.length < 4) return null; 

        String tipo = parts[0]; 
        String nome = parts[1];
        String cpf = parts[2];
        int idade = Integer.parseInt(parts[3]);

        if (tipo.equals("PACIENTE_E") && parts.length >= 5) {
            // Lógica para Paciente Especial (parts[4] = nome do plano)
            String nomePlano = parts[4];
            PlanoSaude plano = planos.stream()
                .filter(p -> p.getNome().equalsIgnoreCase(nomePlano))
                .findFirst()
                .orElse(null);
            
            // Fallback: se o plano não for encontrado, trata como comum
            if (plano == null) {
                return new Paciente(nome, cpf, idade);
            }
            return new PacienteEspecial(nome, cpf, idade, plano);
        } else if (tipo.equals("PACIENTE_C")) {
            return new Paciente(nome, cpf, idade);
        }
        return null;
    }
}