package servicos; 


import modelos.*; 
import java.util.ArrayList;
import java.util.List;
import java.io.*;
import java.time.LocalDateTime;


public class Hospital implements Serializable {
    
    private List<Paciente> pacientes;
    private List<Medico> medicos;
    private List<Consulta> consultas;
    private List<Internacao> internacoes;
    private List<PlanoSaude> planos;

    private static final String MEDICO_FILE = "medicos.csv";
    private static final String PACIENTE_FILE = "pacientes.csv";

    public Hospital() {
        this.pacientes = new ArrayList<>();
        this.medicos = new ArrayList<>();
        this.consultas = new ArrayList<>();
        this.internacoes = new ArrayList<>();
        this.planos = new ArrayList<>();
        
        // Planos fixos (padrão)
        this.planos.add(new PlanoSaude("Plano Bronze", 0.10, false));
        this.planos.add(new PlanoSaude("Plano VIP", 0.30, true)); // internaGarantida = true (Regra do Plano Especial)
    }

    // --- MÉTODOS DE CADASTRO E NEGÓCIO ---
    public void cadastrarPaciente(Paciente p) { this.pacientes.add(p); }
    public void cadastrarMedico(Medico m) { this.medicos.add(m); }
    public void cadastrarPlanoSaude(PlanoSaude ps) { this.planos.add(ps); }

    public boolean agendarConsulta(Paciente p, Medico m, LocalDateTime dataHora, String local) {
        // Validação de Conflito de Agendamento (Médico/Hora OU Local/Hora)
        for (Consulta c : consultas) {
            
            boolean medicoOcupado = c.getMedico().getCpf().equals(m.getCpf()) 
                                    && c.getDataHora().equals(dataHora) 
                                    && c.getStatus() == StatusConsulta.AGENDADA;
                                    
           
            boolean localOcupado = c.getLocal().equalsIgnoreCase(local) 
                                   && c.getDataHora().equals(dataHora) 
                                   && c.getStatus() == StatusConsulta.AGENDADA;
            
            if (medicoOcupado || localOcupado) {
                return false; 
            }
        }
        
        Consulta novaConsulta = new Consulta(p, m, dataHora, local);
        consultas.add(novaConsulta);
        p.adicionarConsulta(novaConsulta); 
        return true;
    }

    // --- PERSISTÊNCIA EM CSV ---

    public void salvarDadosCSV() {
        salvarMedicosCSV();
        salvarPacientesCSV();
        // Consultas e Internacoes não estão sendo salvas, mas o esqueleto do código está completo
        System.out.println("\n[SUCESSO] Dados de Medicos e Pacientes salvos nos arquivos CSV.");
    }

    private void salvarMedicosCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(MEDICO_FILE))) {
            writer.write("Tipo;Nome;CPF;Idade;CRM;Especialidade;CustoConsulta\n"); 
            for (Medico m : medicos) {
                // Usa o toCSV() corrigido de Medico.java
                writer.write(m.toCSV() + "\n");
            }
        } catch (IOException e) {
            System.out.println("\n[ERRO] Falha ao salvar médicos: " + e.getMessage());
        }
    }

    private void salvarPacientesCSV() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(PACIENTE_FILE))) {
            writer.write("Tipo;Nome;CPF;Idade;PlanoNome\n"); 
            for (Paciente p : pacientes) {
                // Usa o toCSV() de Paciente.java (Comum ou Especial)
                writer.write(p.toCSV() + "\n");
            }
        } catch (IOException e) {
            System.out.println("\n[ERRO] Falha ao salvar pacientes: " + e.getMessage());
        }
    }

    public void carregarDadosCSV() {
        carregarMedicosCSV();
        carregarPacientesCSV();
        System.out.println("[INFO] Dados de Medicos e Pacientes carregados dos arquivos CSV.");
    }
    
    private void carregarMedicosCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader(MEDICO_FILE))) {
            reader.readLine(); // Pula o cabeçalho
            String line;
            while ((line = reader.readLine()) != null) {
                Medico m = Medico.fromCSV(line); // Usa o fromCSV() corrigido
                if (m != null) {
                    // Evita duplicação caso carregue mais de uma vez
                    if (medicos.stream().noneMatch(med -> med.getCpf().equals(m.getCpf()))) {
                         this.medicos.add(m);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] Arquivo de médicos (medicos.csv) não encontrado. Criará novo na saída.");
        } catch (IOException e) {
            System.out.println("[ERRO] Falha ao ler arquivo de médicos: " + e.getMessage());
        }
    }

    private void carregarPacientesCSV() {
        try (BufferedReader reader = new BufferedReader(new FileReader(PACIENTE_FILE))) {
            reader.readLine(); // Pula o cabeçalho
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length < 5) continue; 
                
                String nome = parts[1];
                String cpf = parts[2];
                int idade = Integer.parseInt(parts[3]);
                String tipo = parts[0];

                if (pacientes.stream().anyMatch(p -> p.getCpf().equals(cpf))) {
                    continue; // Pula se já existir (evita duplicação)
                }

                if (tipo.equals("PACIENTE_C")) {
                    this.pacientes.add(new Paciente(nome, cpf, idade));
                } else if (tipo.equals("PACIENTE_E")) {
                    String planoNome = parts[4];
                    // Busca a referência do objeto PlanoSaude pelo nome
                    PlanoSaude plano = planos.stream()
                                            .filter(p -> p.getNome().equals(planoNome))
                                            .findFirst()
                                            .orElse(null);
                    if (plano != null) {
                        this.pacientes.add(new PacienteEspecial(nome, cpf, idade, plano));
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("[INFO] Arquivo de pacientes (pacientes.csv) não encontrado. Criará novo na saída.");
        } catch (IOException | NumberFormatException e) {
            System.out.println("[ERRO] Falha ao ler arquivo de pacientes: " + e.getMessage());
        }
    }
    
    // --- GETTERS E RELATÓRIOS (Simplificado) ---
    public List<Paciente> getPacientes() { return pacientes; }
    public List<Medico> getMedicos() { return medicos; }
    public List<PlanoSaude> getPlanos() { return planos; }

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
}