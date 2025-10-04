package main.java.modelos;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
        return String.format("Paciente: %s | Médico: %s | Data: %s | Local: %s | Status: %s | Diagnóstico: %s | Valor: R$%.2f",
                paciente.getNome(), medico.getNome(), dataHora.format(formatter), local, status, valorCobrado);
    }
}