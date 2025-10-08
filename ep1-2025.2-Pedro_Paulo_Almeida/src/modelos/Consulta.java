package modelos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import servicos.Hospital;

public class Consulta implements Serializable {
    private Paciente paciente;
    private Medico medico; 
    private LocalDateTime dataHora;
    private String local;
    private StatusConsulta status;
    private String diagnostico;
    private double valorCobrado; // Valor final após descontos

    public Consulta(Paciente paciente, Medico medico, LocalDateTime dataHora, String local) {
        this.paciente = paciente;
        this.medico = medico;
        this.dataHora = dataHora;
        this.local = local;
        this.status = StatusConsulta.AGENDADA;
        
        // calcula o custo base do médico, aplicando a regra de desconto
        // específica do tipo de paciente (Comum ou Especial)
        this.valorCobrado = paciente.calcularCustoConsulta(medico.getCustoConsulta());
    }

    // Getters para leitura
    public Paciente getPaciente() { return paciente; }
    public Medico getMedico() { return medico; }
    public LocalDateTime getDataHora() { return dataHora; }
    public String getLocal() { return local; } 
    public StatusConsulta getStatus() { return status; }
    public String getDiagnostico() { return diagnostico;}
    public double getValorCobrado() { return valorCobrado; }

    // Métodos de Ação
    public void concluir(String diagnostico) {
        this.status = StatusConsulta.CONCLUIDA;
        this.diagnostico = diagnostico;
    }

    public void cancelar() {
        this.status = StatusConsulta.CANCELADA;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        String diag = (diagnostico == null) ? "N/A" : diagnostico; 
        
        return String.format("Paciente: %s | Médico: %s | Data: %s | Local: %s | Status: %s | Diagnóstico: %s | Valor: R$%.2f",
                paciente.getNome(), 
                medico.getNome(), 
                dataHora.format(formatter), 
                local, 
                status, 
                diag, 
                valorCobrado);
    }
    public String toCSV() {
        String diag = (diagnostico == null) ? "N/A" : diagnostico;
        // Formato: PACIENTE_CPF;MEDICO_CRM;DATA_HORA;LOCAL;STATUS;DIAGNOSTICO;VALOR_COBRADO
        return paciente.getCpf() + ";" + medico.getCrm() + ";" + dataHora.toString() + ";" +
               local + ";" + status.toString() + ";" + diag + ";" + valorCobrado;
    }

    // ADIÇÃO: fromCSV para Consulta
    public static Consulta fromCSV(String csvLine, Hospital hospital) throws DateTimeParseException {
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
        LocalDateTime dataHora = LocalDateTime.parse(parts[2]);
        String local = parts[3];
        StatusConsulta status = StatusConsulta.valueOf(parts[4]);
        String diagnostico = parts[5].equals("N/A") ? null : parts[5];
        double valorCobrado = Double.parseDouble(parts[6]);

        // 3. Reconstrução
        Consulta c = new Consulta(p, m, dataHora, local);
        
        // Ajusta campos que não são definidos no construtor
        c.status = status;
        c.diagnostico = diagnostico;
        c.valorCobrado = valorCobrado;
        
        return c; 
    }
}