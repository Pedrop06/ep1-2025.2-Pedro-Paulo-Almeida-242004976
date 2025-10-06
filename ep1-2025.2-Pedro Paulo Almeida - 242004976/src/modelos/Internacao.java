package modelos;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Internacao implements Serializable {
    private Paciente paciente;
    private Medico medicoResponsavel;
    private LocalDate dataEntrada;
    private LocalDate dataSaida; // Null se ainda internado
    private int numeroQuarto;
    private double custoDiario = 200.00; // Custo diário base fixo
    private String status; // Ativa, Concluída, Cancelada

    public Internacao(Paciente paciente, Medico medicoResponsavel, LocalDate dataEntrada, int numeroQuarto) {
        this.paciente = paciente;
        this.medicoResponsavel = medicoResponsavel;
        this.dataEntrada = dataEntrada;
        this.numeroQuarto = numeroQuarto;
        this.status = "Ativa";
    }

    // Getters
    public Paciente getPaciente() { return paciente; }
    public int getNumeroQuarto() { return numeroQuarto; }
    public String getStatus() { return status; }
    public LocalDate getDataEntrada() { return dataEntrada; }

    // Finalizar internação
    public void finalizar(LocalDate dataSaida) {
        this.dataSaida = dataSaida;
        this.status = "Concluída";
    }
    
    public void cancelar() {
        this.status = "Cancelada";
    }

    // Regra de Negócio: Cálculo de Custo
    public double calcularCustoTotal() {
        if (dataSaida == null || status.equals("Ativa")) {
            return -1.0; // Internação ativa, custo não finalizado
        }

        long dias = ChronoUnit.DAYS.between(dataEntrada, dataSaida);
        dias = dias == 0 ? 1 : dias; // Mínimo de 1 dia

        double custoFinal = dias * custoDiario;

        // Regra do Plano Especial (Internação gratuita < 7 dias)
        if (dias < 7 && paciente instanceof PacienteEspecial) {
            PacienteEspecial pe = (PacienteEspecial) paciente;
            if (pe.getPlano().isInternaGarantida()) {
                custoFinal = 0.0;
            }
        }

        return custoFinal;
    }

    @Override
    public String toString() {
        String dataS = (dataSaida == null) ? "N/A" : dataSaida.toString();
        String custo = (status.equals("Concluída")) ? String.format("R$%.2f", calcularCustoTotal()) : status;

        return String.format("Paciente: %s | Médico: %s | Quarto: %d | Entrada: %s | Saída: %s | Custo: %s",
                paciente.getNome(), medicoResponsavel.getNome(), numeroQuarto, dataEntrada.toString(), dataS, custo);
    }
}
