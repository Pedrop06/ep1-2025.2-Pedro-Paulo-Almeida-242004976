package modelos;


import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeParseException; 
import servicos.Hospital;

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
        
        if (!status.equals("Concluída")) {
            return -1.0; // Internação ativa ou cancelada, custo não finalizado
        }
        
        // Se o status for "Concluída", dataSaida deve ter sido definida no método finalizar()
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

    public String toCSV() {
        String dataSaidaStr = (dataSaida == null) ? "N/A" : dataSaida.toString();
        // Formato: PACIENTE_CPF;MEDICO_CRM;DATA_ENTRADA;DATA_SAIDA;QUARTO;CUSTO_DIARIO;STATUS
        return paciente.getCpf() + ";" + medicoResponsavel.getCrm() + ";" + dataEntrada.toString() + ";" +
               dataSaidaStr + ";" + numeroQuarto + ";" + custoDiario + ";" + status;
    }

    // ADIÇÃO: fromCSV para Internacao
    public static Internacao fromCSV(String csvLine, Hospital hospital) throws DateTimeParseException {
        String[] parts = csvLine.split(";");
        if (parts.length != 7) return null; 

        // 1. Busca Paciente e Médico pelo CPF/CRM
        String pacienteCpf = parts[0];
        String medicoCrm = parts[1];
        Paciente p = hospital.buscarPacientePorCpf(pacienteCpf);
        Medico m = hospital.buscarMedicoPorCrm(medicoCrm);
        
        if (p == null || m == null) {
            return null; // Não carrega se não encontrar a referência
        }

        // 2. Parsers
        LocalDate dataEntrada = LocalDate.parse(parts[2]);
        String dataSaidaStr = parts[3];
        LocalDate dataSaida = dataSaidaStr.equals("N/A") ? null : LocalDate.parse(dataSaidaStr);
        int numeroQuarto = Integer.parseInt(parts[4]);
        double custoDiario = Double.parseDouble(parts[5]);
        String status = parts[6];

        // 3. Reconstrução
        Internacao i = new Internacao(p, m, dataEntrada, numeroQuarto); 
        
        // Ajusta campos que não são definidos no construtor
        i.dataSaida = dataSaida;
        i.custoDiario = custoDiario;
        i.status = status;
        
        return i; 
    }
}
