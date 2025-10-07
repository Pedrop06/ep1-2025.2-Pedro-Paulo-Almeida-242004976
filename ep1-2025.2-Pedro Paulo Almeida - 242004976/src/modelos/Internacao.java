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
    public Medico getMedicoResponsavel() { return medicoResponsavel; } 
    public int getNumeroQuarto() { return numeroQuarto; }
    public String getStatus() { return status; }
    public LocalDate getDataEntrada() { return dataEntrada; }
    public LocalDate getDataSaida() { return dataSaida; }
    public double getCustoDiario() { return custoDiario; } 

    // Finalizar internação
    public void finalizar(LocalDate dataSaida) {
        this.dataSaida = dataSaida;
        this.status = "Concluída";
        System.out.println("[INFO] Internação finalizada em " + dataSaida);
    }
    
    // Método para calcular o custo total (Regra de Negócio)
    public double calcularCustoTotal() {
        // Se ainda está ativo, calcula até a data atual
        LocalDate dataFim = (dataSaida == null) ? LocalDate.now() : dataSaida;
        
        long dias = ChronoUnit.DAYS.between(dataEntrada, dataFim);
        // Garante pelo menos 1 dia de internação
        if (dias < 1) dias = 1; 

        double custoBase = dias * custoDiario;
        
        // Regra de Negócio: Plano com internação garantida (máximo 7 dias de graça)
        if (paciente instanceof PacienteEspecial) {
            PacienteEspecial pe = (PacienteEspecial) paciente;
            if (pe.getPlano().isInternaGarantida()) {
                long diasGratuitos = Math.min(dias, 7);
                custoBase -= diasGratuitos * custoDiario;
            }
        }
        
        // Garante que o custo não seja negativo
        return Math.max(0, custoBase);
    }

    @Override
    public String toString() {
        String saidaStr = dataSaida == null ? "Em andamento" : dataSaida.toString();
        String custoStr = status.equals("Concluída") ? String.format("Total: R$%.2f", calcularCustoTotal()) : "Custo Diário: R$200.00";

        return String.format("Internação - %s | %s - Entrada: %s, Saída: %s | Quarto: %d. %s",
             status, paciente.getNome(), dataEntrada, saidaStr, numeroQuarto, custoStr);
    }

    // Formato CSV
    public String toCSV() { 
        String dataSaidaStr = dataSaida == null ? "N/A" : dataSaida.toString();
        // PACIENTE_CPF;MEDICO_CRM;DATA_ENTRADA;DATA_SAIDA;QUARTO;CUSTO_DIARIO;STATUS
        return paciente.getCpf() + ";" + medicoResponsavel.getCrm() + ";" + dataEntrada.toString() + ";" +
               dataSaidaStr + ";" + numeroQuarto + ";" + custoDiario + ";" + status;
    }

    
    public static Internacao fromCSV(String csvLine, Hospital hospital) throws DateTimeParseException {
        String[] parts = csvLine.split(";");
        if (parts.length != 7) return null; 

        // 1. Busca Paciente e Médico pelo CPF/CRM
        String pacienteCpf = parts[0];
        String medicoCrm = parts[1];
        Paciente p = hospital.buscarPacientePorCpf(pacienteCpf);
        Medico m = hospital.buscarMedicoPorCrm(medicoCrm);
        
        if (p == null || m == null) {
            return null;
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
        i.custoDiario = custoDiario;
        i.status = status;
        i.dataSaida = dataSaida; 
        
        return i;
    }
}