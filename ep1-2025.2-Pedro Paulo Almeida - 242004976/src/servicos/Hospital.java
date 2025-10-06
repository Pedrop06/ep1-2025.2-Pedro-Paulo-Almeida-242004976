package servicos; 

import modelos.*; 
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.time.LocalDateTime;
import java.time.LocalDate; 
import java.time.format.DateTimeParseException;


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
    public void cadastrarPaciente(Paciente p) { 
        this.pacientes.add(p); 
    }
    public void cadastrarMedico(Medico m) { 
        this.medicos.add(m); 
    }

    // Método original (mantido para compatibilidade, ex: fromCSV)
    public boolean agendarConsulta(Paciente p, Medico m, LocalDateTime dataHora, String local) {
        Consulta novaConsulta = new Consulta(p, m, dataHora, local);
        return this.consultas.add(novaConsulta);
    }
    
    // NOVO MÉTODO: Agendar Consulta por CPF e CRM
    public boolean agendarConsulta(String cpfPaciente, String crmMedico, LocalDateTime dataHora, String local) {
        Paciente p = buscarPacientePorCpf(cpfPaciente);
        Medico m = buscarMedicoPorCrm(crmMedico);
        
        if (p == null) {
            System.out.println("[ERRO] Paciente com CPF " + cpfPaciente + " não encontrado.");
            return false;
        }
        if (m == null) {
            System.out.println("[ERRO] Médico com CRM " + crmMedico + " não encontrado.");
            return false;
        }
        
        // Chama o método original que recebe os objetos
        return agendarConsulta(p, m, dataHora, local);
    }
    
    // Método original (mantido para compatibilidade, ex: fromCSV)
    public boolean agendarInternacao(Paciente p, Medico m, LocalDate dataEntrada, int numeroQuarto) {
        Internacao novaInternacao = new Internacao(p, m, dataEntrada, numeroQuarto);
        return this.internacoes.add(novaInternacao);
    }

    // NOVO MÉTODO: Agendar Internação por CPF e CRM
    public boolean agendarInternacao(String cpfPaciente, String crmMedico, LocalDate dataEntrada, int numeroQuarto) {
        Paciente p = buscarPacientePorCpf(cpfPaciente);
        Medico m = buscarMedicoPorCrm(crmMedico);
        
        if (p == null) {
            System.out.println("[ERRO] Paciente com CPF " + cpfPaciente + " não encontrado.");
            return false;
        }
        if (m == null) {
            System.out.println("[ERRO] Médico com CRM " + crmMedico + " não encontrado.");
            return false;
        }
        
        // Chama o método original que recebe os objetos
        return agendarInternacao(p, m, dataEntrada, numeroQuarto);
    }


    public boolean concluirERemoverConsulta(int indice, String diagnostico) {
        if (indice >= 0 && indice < consultas.size()) {
            Consulta c = consultas.get(indice);
            
            // 1. Conclui a consulta (atualiza status e diagnóstico no objeto)
            c.concluir(diagnostico);
            
            // 2. Adiciona ao histórico do paciente
            c.getPaciente().adicionarConsulta(c);
            
            // 3. Remove da lista de consultas ativas 
            consultas.remove(indice);
            
            return true;
        }
        return false;
    }

       public boolean concluirERemoverInternacao(int indice, LocalDate dataSaida) {
        if (indice >= 0 && indice < internacoes.size()) {
            Internacao i = internacoes.get(indice);
            
            // 1. Conclui a internação (atualiza status e data de saída no objeto)
            i.finalizar(dataSaida);
            
            // 2. Adiciona ao histórico do paciente
            i.getPaciente().adicionarInternacao(i);
            
            // 3. Remove da lista de internações ativas
            internacoes.remove(indice);
            
            return true;
        }
        return false;
    }

    // --- MÉTODOS DE BUSCA (EXISTENTES) ---

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


    // --- PERSISTÊNCIA (CSV) ---
    public void salvarDadosCSV() {
        // Salvamento de Médicos
        try (PrintWriter writer = new PrintWriter(new FileWriter(MEDICO_FILE))) {
            writer.println("TIPO;NOME;CPF;IDADE;CRM;ESPECIALIDADE;CUSTO_CONSULTA");
            for (Medico m : medicos) {
                writer.println(m.toCSV());
            }
            System.out.println("[INFO] Médicos salvos com sucesso.");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao salvar arquivo de médicos: " + e.getMessage());
        }

        // Salvamento de Pacientes (incluindo Pacientes Especiais)
        try (PrintWriter writer = new PrintWriter(new FileWriter(PACIENTE_FILE))) {
            writer.println("TIPO;NOME;CPF;IDADE;PLANO_NOME(apenas_especial)");
            for (Paciente p : pacientes) {
                writer.println(p.toCSV());
            }
            System.out.println("[INFO] Pacientes salvos com sucesso.");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao salvar arquivo de pacientes: " + e.getMessage());
        }

        // Salvamento de Consultas
        try (PrintWriter writer = new PrintWriter(new FileWriter(CONSULTA_FILE))) {
            writer.println("PACIENTE_CPF;MEDICO_CRM;DATA_HORA;LOCAL;STATUS;DIAGNOSTICO;VALOR_COBRADO");
            for (Consulta c : consultas) {
                writer.println(c.toCSV());
            }
            System.out.println("[INFO] Consultas ativas salvas com sucesso.");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao salvar arquivo de consultas: " + e.getMessage());
        }

        // Salvamento de Internações
        try (PrintWriter writer = new PrintWriter(new FileWriter(INTERNACAO_FILE))) {
            writer.println("PACIENTE_CPF;MEDICO_CRM;DATA_ENTRADA;DATA_SAIDA;QUARTO;CUSTO_DIARIO;STATUS");
            for (Internacao i : internacoes) {
                writer.println(i.toCSV());
            }
            System.out.println("[INFO] Internações ativas salvas com sucesso.");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao salvar arquivo de internações: " + e.getMessage());
        }
    }

    public void carregarDadosCSV() {
        // 1. Carregar Planos (já estão fixos, nada a fazer aqui, mas em um sistema real seriam carregados)
        
        // 2. Carregar Médicos
        try (BufferedReader reader = new BufferedReader(new FileReader(MEDICO_FILE))) {
            reader.readLine(); // Pular o cabeçalho
            String line;
            while ((line = reader.readLine()) != null) {
                Medico m = Medico.fromCSV(line);
                if (m != null) {
                    this.medicos.add(m);
                }
            }
            System.out.println("[INFO] Médicos carregados: " + this.medicos.size());
        } catch (FileNotFoundException e) {
            System.out.println("[AVISO] Arquivo de médicos não encontrado. Iniciando com lista vazia.");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao carregar arquivo de médicos: " + e.getMessage());
        }
        
        // 3. Carregar Pacientes
        try (BufferedReader reader = new BufferedReader(new FileReader(PACIENTE_FILE))) {
            reader.readLine(); // Pular o cabeçalho
            String line;
            while ((line = reader.readLine()) != null) {
                // Passamos a lista de planos para o método estático para que ele encontre a referência
                Paciente p = Paciente.fromCSV(line, this.planos); 
                if (p != null) {
                    this.pacientes.add(p);
                }
            }
            System.out.println("[INFO] Pacientes carregados: " + this.pacientes.size());
        } catch (FileNotFoundException e) {
            System.out.println("[AVISO] Arquivo de pacientes não encontrado. Iniciando com lista vazia.");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao carregar arquivo de pacientes: " + e.getMessage());
        }
        
        // 4. Carregar Consultas (precisa de Médicos e Pacientes já carregados)
        try (BufferedReader reader = new BufferedReader(new FileReader(CONSULTA_FILE))) {
            reader.readLine(); // Pular o cabeçalho
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Consulta c = Consulta.fromCSV(line, this);
                    if (c != null && c.getStatus() == StatusConsulta.AGENDADA) {
                        this.consultas.add(c);
                    } else if (c != null && c.getStatus() != StatusConsulta.AGENDADA) {
                         // Se for concluída/cancelada, adiciona ao histórico do paciente
                         c.getPaciente().adicionarConsulta(c);
                    }
                } catch (DateTimeParseException e) {
                     System.out.println("[ERRO] Formato de data/hora inválido em Consulta: " + line);
                }
            }
            System.out.println("[INFO] Consultas ativas carregadas: " + this.consultas.size());
        } catch (FileNotFoundException e) {
            System.out.println("[AVISO] Arquivo de consultas não encontrado. Iniciando com lista vazia.");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao carregar arquivo de consultas: " + e.getMessage());
        }

         // 5. Carregar Internações (precisa de Médicos e Pacientes já carregados)
        try (BufferedReader reader = new BufferedReader(new FileReader(INTERNACAO_FILE))) {
            reader.readLine(); // Pular o cabeçalho
            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    Internacao i = Internacao.fromCSV(line, this);
                    if (i != null && i.getStatus().equalsIgnoreCase("Ativa")) {
                        this.internacoes.add(i);
                    } else if (i != null && !i.getStatus().equalsIgnoreCase("Ativa")) {
                         // Se for concluída/cancelada, adiciona ao histórico do paciente
                         i.getPaciente().adicionarInternacao(i);
                    }
                } catch (DateTimeParseException e) {
                     System.out.println("[ERRO] Formato de data inválido em Internação: " + line);
                }
            }
            System.out.println("[INFO] Internações ativas carregadas: " + this.internacoes.size());
        } catch (FileNotFoundException e) {
            System.out.println("[AVISO] Arquivo de internações não encontrado. Iniciando com lista vazia.");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao carregar arquivo de internações: " + e.getMessage());
        }
    }

    // --- MÉTODOS DE RELATÓRIO ---
    public List<PlanoSaude> getPlanos() { return planos; }
    public List<Paciente> getPacientes() { return pacientes; }
    public List<Medico> getMedicos() { return medicos; }
    public List<Consulta> getConsultas() { return consultas; }
    public List<Internacao> getInternacoes() { return internacoes; }

    public void relatorioPacientes() {
        System.out.println("\n--- RELATÓRIO DE PACIENTES ---");
        if (pacientes.isEmpty()) { 
             System.out.println("Nenhum paciente cadastrado."); 
        } else {
            for (Paciente p : pacientes) {
                System.out.println(p.exibirDetalhes()); 
            }
        }
    }
    
    public void relatorioMedicos() {
        System.out.println("\n--- RELATÓRIO DE MÉDICOS ---");
        if (medicos.isEmpty()) { 
             System.out.println("Nenhum médico cadastrado."); 
        } else {
            for (Medico m : medicos) {
                System.out.println(m.exibirDetalhes());
            }
        }
    }
    
    public void relatorioConsultas() {
        System.out.println("\n--- RELATÓRIO DE CONSULTAS ATIVAS/CONCLUÍDAS ---");

        // Exibe ativas
        System.out.println("\n-- Consultas AGENDADAS (Lista principal) --");
         if (consultas.isEmpty()) { 
             System.out.println("Nenhuma consulta AGENDADA."); 
        } else {
            // Adiciona o índice na exibição para facilitar a conclusão
            for (int i = 0; i < consultas.size(); i++) {
                Consulta c = consultas.get(i);
                System.out.printf("[%d] %s%n", i, c.toString());
            }
        }
        
        // Exibe histórico (Concluídas/Canceladas)
        System.out.println("\n-- Histórico de Consultas (Nos Pacientes) --");
         boolean temHistorico = false;
        for(Paciente p : pacientes) {
            if (!p.getHistoricoConsultas().isEmpty()) {
                System.out.println("Histórico de " + p.getNome() + ":");
                for (Consulta c : p.getHistoricoConsultas()) {
                    System.out.println("  " + c.toString());
                }
                temHistorico = true;
            }
        }
        if (!temHistorico) {
            System.out.println("Nenhum registro de consulta concluída/cancelada.");
        }
    }
    
    public void relatorioInternacoes() {
        System.out.println("\n--- RELATÓRIO DE INTERNAÇÕES ATIVAS/CONCLUÍDAS ---");

        // Exibe ativas
        System.out.println("\n-- Internações ATIVAS (Lista principal) --");
         if (internacoes.isEmpty()) { 
             System.out.println("Nenhuma internação ATIVA."); 
        } else {
            // Adiciona o índice na exibição para facilitar a conclusão
            for (int i = 0; i < internacoes.size(); i++) {
                Internacao internacao = internacoes.get(i);
                System.out.printf("[%d] %s%n", i, internacao.toString());
            }
        }
        
        // Exibe histórico (Concluídas/Canceladas)
        System.out.println("\n-- Histórico de Internações (Nos Pacientes) --");
         boolean temHistorico = false;
        for(Paciente p : pacientes) {
            if (!p.getHistoricoInternacoes().isEmpty()) {
                System.out.println("Histórico de " + p.getNome() + ":");
                for (Internacao i : p.getHistoricoInternacoes()) {
                    System.out.println("  " + i.toString());
                }
                 temHistorico = true;
            }
        }
        if (!temHistorico) {
            System.out.println("Nenhum registro de internação concluída/cancelada.");
        }
    }
}