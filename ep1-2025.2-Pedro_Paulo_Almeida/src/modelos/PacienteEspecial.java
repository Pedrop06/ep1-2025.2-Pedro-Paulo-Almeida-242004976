package modelos;


public class PacienteEspecial extends Paciente  {
    private PlanoSaude plano;

    public PacienteEspecial(String nome, String cpf, int idade, PlanoSaude plano) {
        super(nome, cpf, idade);
        this.plano = plano;
    }

    public PlanoSaude getPlano() { return plano; }

    //Sobrescrita do método exibirDetalhes
    @Override
    public String exibirDetalhes() {
        return super.toString() + " - Tipo: Especial, Plano: " + plano.getNome();
    }

    //Sobrescrita do cálculo do custo (Regra de Negócio: Desconto)
    @Override
    public double calcularCustoConsulta(double custoBaseMedico) {
        // 1. Aplica desconto do plano de saúde
        double custoAposPlano = custoBaseMedico * (1.0 - plano.getDescontoGeral());

        // 2. Aplica desconto para 60+ anos sobre o valor já com desconto do plano
        if (this.getIdade() >= 60) {
            return custoAposPlano * (1.0 - 0.15); // Mais 15% de desconto
        }

        return custoAposPlano;
    }

    // Formato para CSV (Tipo;Nome;CPF;Idade;PlanoNome)
   @Override
    public String toCSV() { // SUBSTITUIR
        return "PACIENTE_E;" + super.toString() + ";" + plano.getNome();
    }
}
