package servicos; 

import modelos.*; 
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Scanner;



public class Hospital implements Serializable {
    
    private List<Paciente> pacientes;
    private List<Medico> medicos;
    private List<Consulta> consultas;
    private List<Internacao> internacoes;
    private List<PlanoSaude> planos;

    private static final String MEDICO_FILE = "medicos.csv";
    private static final String PACIENTE_FILE = "pacientes.csv";
    private static final String CONSULTA_FILE = "consultas.csv";
    private static final String INTERNACAO_FILE = "internacoes.csv";

    public Hospital() {
        this.pacientes = new ArrayList<>();
        this.medicos = new ArrayList<>();
        this.consultas = new ArrayList<>();
        this.internacoes = new ArrayList<>();
        this.planos = new ArrayList<>();
        
        // Planos fixos (padrão)
        this.planos.add(new PlanoSaude("Plano Bronze", 0.10, false));
        this.planos.add(new PlanoSaude("Plano VIP", 0.30, true)); 
    }

    // --- MÉTODOS DE CADASTRO E NEGÓCIO ---
    public void cadastrarPaciente(Paciente p) { this.pacientes.add(p); }
    public void cadastrarMedico(Medico m) { this.medicos.add(m); }
    
    // Método de agendamento de consulta
    public boolean agendarConsulta(Paciente paciente, Medico medico, LocalDateTime dataHora, String local) {
        // Regra de Negócio: Não pode agendar consulta com status diferente de AGENDADA para a mesma data/hora/médico
        for (Consulta c : consultas) {
            if (c.getMedico().equals(medico) && c.getDataHora().equals(dataHora) && c.getStatus().equals(StatusConsulta.AGENDADA)) {
                System.out.println("[ERRO] O médico " + medico.getNome() + " já tem uma consulta agendada para este horário.");
                return false;
            }
        }
        
        Consulta novaConsulta = new Consulta(paciente, medico, dataHora, local);
        this.consultas.add(novaConsulta);
        paciente.adicionarConsulta(novaConsulta); 
        return true;
    }

    // ADIÇÃO: Método de agendamento de Internação
    public boolean agendarInternacao(Paciente p, Medico m, LocalDate dataEntrada, int numeroQuarto) {
        // Regra: Quarto não pode estar ocupado na data de entrada
        for (Internacao i : internacoes) {
            if (i.getNumeroQuarto() == numeroQuarto && i.getStatus().equals("Ativa")) {
                System.out.println("[ERRO] O quarto " + numeroQuarto + " está atualmente ocupado.");
                return false;
            }
        }
        
        Internacao novaInternacao = new Internacao(p, m, dataEntrada, numeroQuarto);
        this.internacoes.add(novaInternacao);
        p.adicionarInternacao(novaInternacao);
        return true;
    }
    
    // SALVAR DADOS CSV
    public void salvarDadosCSV() {
        // 1. SALVA MÉDICOS
        try (PrintWriter pw = new PrintWriter(new FileWriter(MEDICO_FILE))) {
            for (Medico m : medicos) { pw.println(m.toCSV()); }
            System.out.println("[INFO] Médicos salvos em " + MEDICO_FILE);
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao salvar médicos: " + e.getMessage());
        }

        // 2. SALVA PACIENTES
        try (PrintWriter pw = new PrintWriter(new FileWriter(PACIENTE_FILE))) {
            for (Paciente p : pacientes) { pw.println(p.toCSV()); }
            System.out.println("[INFO] Pacientes salvos em " + PACIENTE_FILE);
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao salvar pacientes: " + e.getMessage());
        }

        // 3. SALVA CONSULTAS
        try (PrintWriter pw = new PrintWriter(new FileWriter(CONSULTA_FILE))) {
            for (Consulta c : consultas) { pw.println(c.toCSV()); }
            System.out.println("[INFO] Consultas salvas em " + CONSULTA_FILE);
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao salvar consultas: " + e.getMessage());
        }
        
        // 4. SALVA INTERNAÇÕES
        try (PrintWriter pw = new PrintWriter(new FileWriter(INTERNACAO_FILE))) {
            for (Internacao i : internacoes) { pw.println(i.toCSV()); }
            System.out.println("[INFO] Internações salvas em " + INTERNACAO_FILE);
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao salvar internações: " + e.getMessage());
        }
    }

    //CARREGAR DADOS CSV (ATUALIZADO)
    public void carregarDadosCSV() {
        // 1. CARREGA MÉDICOS
        try (Scanner s = new Scanner(new File(MEDICO_FILE))) {
            while (s.hasNextLine()) {
                String line = s.nextLine();
                Medico m = Medico.fromCSV(line); 
                if (m != null) { this.medicos.add(m); }
            }
            System.out.println("[INFO] " + medicos.size() + " médicos carregados de " + MEDICO_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] Arquivo de médicos (" + MEDICO_FILE + ") não encontrado. Criará novo na saída.");
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) { 
            System.out.println("[ERRO] Falha ao ler arquivo de médicos: " + e.getMessage());
        }


        // 2. CARREGA PACIENTES 
        try (Scanner s = new Scanner(new File(PACIENTE_FILE))) {
            while (s.hasNextLine()) {
                String line = s.nextLine();
                Paciente p = Paciente.fromCSV(line, planos); 
                if (p != null) { this.pacientes.add(p); }
            }
            System.out.println("[INFO] " + pacientes.size() + " pacientes carregados de " + PACIENTE_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] Arquivo de pacientes (" + PACIENTE_FILE + ") não encontrado. Criará novo na saída.");
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) { 
            System.out.println("[ERRO] Falha ao ler arquivo de pacientes: " + e.getMessage());
        }
        
        
        // 3. CARREGA CONSULTAS (DEPOIS de Pacientes e Médicos)
        try (Scanner s = new Scanner(new File(CONSULTA_FILE))) {
            while (s.hasNextLine()) {
                String line = s.nextLine();
                Consulta c = Consulta.fromCSV(line, this); 
                if (c != null) {
                    this.consultas.add(c);
                    c.getPaciente().adicionarConsulta(c); 
                }
            }
            System.out.println("[INFO] " + consultas.size() + " consultas carregadas de " + CONSULTA_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] Arquivo de consultas (" + CONSULTA_FILE + ") não encontrado. Criará novo na saída.");
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException | DateTimeParseException e) {
            System.out.println("[ERRO] Falha ao ler arquivo de consultas: " + e.getMessage());
        }
        
        // 4. CARREGA INTERNAÇÕES (DEPOIS de Pacientes e Médicos)
        try (Scanner s = new Scanner(new File(INTERNACAO_FILE))) {
            while (s.hasNextLine()) {
                String line = s.nextLine();
                Internacao i = Internacao.fromCSV(line, this); 
                if (i != null) {
                    this.internacoes.add(i);
                    i.getPaciente().adicionarInternacao(i); 
                }
            }
            System.out.println("[INFO] " + internacoes.size() + " internações carregadas de " + INTERNACAO_FILE);
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] Arquivo de internações (" + INTERNACAO_FILE + ") não encontrado. Criará novo na saída.");
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException | DateTimeParseException e) {
            System.out.println("[ERRO] Falha ao ler arquivo de internações: " + e.getMessage());
        }
    }


    //GETTERS E BUSCAS ESSENCIAIS
    public List<Paciente> getPacientes() { return pacientes; }
    public List<Medico> getMedicos() { return medicos; }
    public List<PlanoSaude> getPlanos() { return planos; }
    public List<Internacao> getInternacoes() { 
        return internacoes; 
    } 

    public Paciente buscarPacientePorCpf(String cpf) {
        return pacientes.stream()
                .filter(p -> p.getCpf().equals(cpf))
                .findFirst()
                .orElse(null);
    }

    public Medico buscarMedicoPorCrm(String crm) {
        return medicos.stream()
                .filter(m -> m.getCrm().equals(crm))
                .findFirst()
                .orElse(null);
    }

    //RELATÓRIOS
    public void relatorioPacientes() {
        System.out.println("\n--- RELATÓRIO DE PACIENTES CADASTRADOS ---");
        if (pacientes.isEmpty()) { System.out.println("Nenhum paciente cadastrado."); return; }
        for (Paciente p : pacientes) {
            System.out.println(p.exibirDetalhes());
        }
    }

    public void relatorioMedicos() {
        System.out.println("\n--- RELATÓRIO DE MÉDICOS CADASTRADOS ---");
        if (medicos.isEmpty()) { System.out.println("Nenhum médico cadastrado."); return; }
        for (Medico m : medicos) {
            System.out.println(m.exibirDetalhes());
        }
    }
    
    public void relatorioConsultas() {
        System.out.println("\n--- RELATÓRIO DE CONSULTAS AGENDADAS/CONCLUÍDAS ---");
        if (consultas.isEmpty()) { System.out.println("Nenhuma consulta registrada."); return; }
        for (Consulta c : consultas) {
            System.out.println(c.toString());
        }
    }
    
    public void relatorioInternacoes() {
        System.out.println("\n--- RELATÓRIO DE INTERNAÇÕES ---");
        if (internacoes.isEmpty()) { System.out.println("Nenhuma internação registrada."); return; }
        for (Internacao i : internacoes) {
            System.out.println(i.toString());
        }
    }
}