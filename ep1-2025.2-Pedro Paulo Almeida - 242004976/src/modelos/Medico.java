package modelos;


public class Medico extends Pessoa {
    private String crm;
    private String especialidade;
    private double custoConsulta; 

    public Medico(String nome, String cpf, int idade, String crm, String especialidade, double custoConsulta) {
        super(nome, cpf, idade);
        this.crm = crm;
        this.especialidade = especialidade;
        this.custoConsulta = custoConsulta;
    }

    // Getters específicos
    public String getCrm() { return crm; }
    public String getEspecialidade() { return especialidade; }
    public double getCustoConsulta() { return custoConsulta; }

    @Override
    public String exibirDetalhes() {
        return super.toString() + " - CRM: " + crm + ", Especialidade: " + especialidade + ", Custo Base: " + custoConsulta;
    }

    // formatação csv
    public String toCSV() {
        return "MEDICO;" + super.toString() + ";" + crm + ";" + especialidade + "; " + custoConsulta;
    }

    // Método estático para recriar objeto a partir da linha CSV
    public static Medico fromCSV(String csvLine) {
        String[] parts = csvLine.split(";");
        // Verifica se tem o número correto de partes para Medico
        if (parts.length < 8) return null; 
        
        // MEDICO;Nome;CPF;Idade;CRM;Especialidade;Custo
        String nome = parts[1];
        String cpf = parts[2];
        int idade = Integer.parseInt(parts[3]);
        String crm = parts[4];
        String especialidade = parts[5];
        double custoConsulta = Double.parseDouble(parts[6]);

        return new Medico(nome, cpf, idade, crm, especialidade, custoConsulta);
    }
}