package util;

import modelos.*;
import servicos.*;

import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.LocalDate; // Adição
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;

public class Main {
    private static Hospital hospital;
    private static Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy"); // Adição

    public static void main(String[] args) {
        hospital = new Hospital();
        hospital.carregarDadosCSV(); // Carrega dados ao iniciar

        int opcao = -1;
        do {
            exibirMenuPrincipal();
            try {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Consome a quebra de linha
            } catch (InputMismatchException e) {
                System.out.println("\n[ERRO] Entrada inválida. Digite um número correspondente à opção.");
                scanner.nextLine(); // Limpa o buffer
                opcao = -1;
                continue;
            }

            switch (opcao) {
                case 1: menuCadastro(); break;
                case 2: menuAgendamento(); break;
                case 3: menuRelatorios(); break;
                case 0:
                    System.out.println("\nSaindo do Sistema...");
                    hospital.salvarDadosCSV(); // Salva em CSV antes de sair
                    break;
                default:
                    System.out.println("[ERRO] Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void exibirMenuPrincipal() {
        System.out.println("\n==================================");
        System.out.println("     SISTEMA DE GESTÃO HOSPITALAR   ");
        System.out.println("==================================");
        System.out.println("1. Cadastro");
        System.out.println("2. Agendamento");
        System.out.println("3. Relatórios");
        System.out.println("0. Sair e Salvar Dados");
        System.out.print("Escolha uma opção: ");
    }

    // ------------------------------------
    // MENU 1: CADASTRO
    // ------------------------------------
    private static void menuCadastro() {
        int opcao = -1;
        do {
            System.out.println("\n--- MENU DE CADASTRO ---");
            System.out.println("1. Cadastrar Paciente");
            System.out.println("2. Cadastrar Médico");
            System.out.println("0. Voltar ao Menu Principal");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = scanner.nextInt();
                scanner.nextLine(); 
            } catch (InputMismatchException e) {
                System.out.println("\n[ERRO] Entrada inválida. Digite um número correspondente à opção.");
                scanner.nextLine(); 
                opcao = -1;
                continue;
            }

            switch (opcao) {
                case 1: cadastrarPaciente(); break;
                case 2: cadastrarMedico(); break;
                case 0: break;
                default: System.out.println("[ERRO] Opção inválida.");
            }
        } while (opcao != 0);
    }
    
    private static void cadastrarPaciente() {
        System.out.println("\n--- CADASTRO DE PACIENTE ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("CPF: ");
        String cpf = scanner.nextLine();
        System.out.print("Idade: ");
        int idade;
        try {
            idade = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("[ERRO] Idade inválida.");
            scanner.nextLine(); 
            return;
        }

        System.out.print("É Paciente Especial? (S/N): ");
        String resposta = scanner.nextLine().toUpperCase();
        
        if (resposta.equals("S")) {
            List<PlanoSaude> planos = hospital.getPlanos();
            if (planos.isEmpty()) {
                System.out.println("[ERRO] Não há planos de saúde cadastrados.");
                return;
            }
            
            System.out.println("Escolha o Plano de Saúde:");
            for (int i = 0; i < planos.size(); i++) {
                System.out.printf("%d. %s (Desconto: %.0f%%)\n", i + 1, planos.get(i).getNome(), planos.get(i).getDescontoGeral() * 100);
            }
            System.out.print("Opção: ");
            int planoOpcao;
            try {
                planoOpcao = scanner.nextInt();
                scanner.nextLine();
            } catch (InputMismatchException e) {
                System.out.println("[ERRO] Opção de plano inválida.");
                scanner.nextLine(); 
                return;
            }
            
            if (planoOpcao > 0 && planoOpcao <= planos.size()) {
                PlanoSaude plano = planos.get(planoOpcao - 1);
                PacienteEspecial pe = new PacienteEspecial(nome, cpf, idade, plano);
                hospital.cadastrarPaciente(pe);
                System.out.println("[SUCESSO] Paciente Especial cadastrado!");
            } else {
                System.out.println("[ERRO] Opção de plano inválida. Cadastrando como Paciente Comum.");
                Paciente p = new Paciente(nome, cpf, idade);
                hospital.cadastrarPaciente(p);
            }
        } else {
            Paciente p = new Paciente(nome, cpf, idade);
            hospital.cadastrarPaciente(p);
            System.out.println("[SUCESSO] Paciente Comum cadastrado!");
        }
    }

    private static void cadastrarMedico() {
        System.out.println("\n--- CADASTRO DE MÉDICO ---");
        System.out.print("Nome: ");
        String nome = scanner.nextLine();
        System.out.print("CPF: ");
        String cpf = scanner.nextLine();
        System.out.print("Idade: ");
        int idade;
        try {
            idade = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("[ERRO] Idade inválida.");
            scanner.nextLine(); 
            return;
        }
        System.out.print("CRM: ");
        String crm = scanner.nextLine();
        System.out.print("Especialidade: ");
        String especialidade = scanner.nextLine();
        System.out.print("Custo da Consulta (ex: 150.00): ");
        double custo;
        try {
            custo = scanner.nextDouble();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("[ERRO] Custo da consulta inválido.");
            scanner.nextLine();
            return;
        }

        Medico m = new Medico(nome, cpf, idade, crm, especialidade, custo);
        hospital.cadastrarMedico(m);
        System.out.println("[SUCESSO] Médico cadastrado!");
    }
    
    // ------------------------------------
    // MENU 2: AGENDAMENTO (ATUALIZADO)
    // ------------------------------------
    private static void menuAgendamento() {
        int opcao = -1;
        do {
            System.out.println("\n--- MENU DE AGENDAMENTO ---");
            System.out.println("1. Agendar Consulta");
            System.out.println("2. Agendar Internação"); // Nova Opção
            System.out.println("0. Voltar ao Menu Principal");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = scanner.nextInt();
                scanner.nextLine(); 
            } catch (InputMismatchException e) {
                System.out.println("\n[ERRO] Entrada inválida. Digite um número correspondente à opção.");
                scanner.nextLine(); 
                opcao = -1;
                continue;
            }

            switch (opcao) {
                case 1: agendarConsulta(); break;
                case 2: agendarInternacao(); break; // Chama o novo método
                case 0: break;
                default: System.out.println("[ERRO] Opção inválida.");
            }
        } while (opcao != 0);
    }
    
    // Método auxiliar para buscar Paciente ou Médico (Requer os métodos buscarPacientePorCpf e buscarMedicoPorCrm no Hospital.java)
    private static Pessoa buscarPessoaPorDocumento(String tipo) {
        if (tipo.equals("Paciente")) {
            System.out.print("CPF do Paciente: ");
            String cpf = scanner.nextLine();
            Paciente p = hospital.buscarPacientePorCpf(cpf);
            if (p == null) {
                System.out.println("[ERRO] Paciente com CPF " + cpf + " não encontrado.");
                return null;
            }
            System.out.println("[INFO] Paciente selecionado: " + p.getNome());
            return p;
        } else if (tipo.equals("Médico")) {
            System.out.print("CRM do Médico: ");
            String crm = scanner.nextLine();
            Medico m = hospital.buscarMedicoPorCrm(crm);
            if (m == null) {
                System.out.println("[ERRO] Médico com CRM " + crm + " não encontrado.");
                return null;
            }
            System.out.println("[INFO] Médico selecionado: " + m.getNome());
            return m;
        }
        return null;
    }

    private static void agendarConsulta() {
        if (hospital.getPacientes().isEmpty() || hospital.getMedicos().isEmpty()) {
            System.out.println("[ERRO] É necessário ter Paciente e Médico cadastrados.");
            return;
        }

        // 1. Busca Paciente
        Paciente paciente = (Paciente) buscarPessoaPorDocumento("Paciente");
        if (paciente == null) return;
        
        // 2. Busca Médico
        Medico medico = (Medico) buscarPessoaPorDocumento("Médico");
        if (medico == null) return;

        // 3. Coleta data e local
        System.out.print("Data e Hora da Consulta (dd/MM/yyyy HH:mm): ");
        String dataHoraStr = scanner.nextLine();
        LocalDateTime dataHora;
        try {
            dataHora = LocalDateTime.parse(dataHoraStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("[ERRO] Formato de data e hora inválido. Use dd/MM/yyyy HH:mm.");
            return;
        }

        System.out.print("Local da Consulta (Ex: Sala 101): ");
        String local = scanner.nextLine();

        // 4. Agendamento
        if (hospital.agendarConsulta(paciente, medico, dataHora, local)) {
            System.out.println("\n[SUCESSO] Consulta agendada!");
            System.out.printf("Valor cobrado (já com descontos): R$%.2f\n", paciente.calcularCustoConsulta(medico.getCustoConsulta()));
        } else {
             System.out.println("[ERRO] Não foi possível agendar a consulta (verifique a disponibilidade do médico).");
        }
    }
    
    private static void agendarInternacao() {
        if (hospital.getPacientes().isEmpty() || hospital.getMedicos().isEmpty()) {
            System.out.println("[ERRO] É necessário ter Paciente e Médico cadastrados para agendar internação.");
            return;
        }

        // 1. Busca Paciente
        Paciente paciente = (Paciente) buscarPessoaPorDocumento("Paciente");
        if (paciente == null) return;
        
        // 2. Busca Médico
        Medico medico = (Medico) buscarPessoaPorDocumento("Médico");
        if (medico == null) return;

        // 3. Coleta dados da Internação
        System.out.print("Data da Entrada (dd/MM/yyyy): ");
        String dataEntradaStr = scanner.nextLine();
        LocalDate dataEntrada;
        try {
            dataEntrada = LocalDate.parse(dataEntradaStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("[ERRO] Formato de data inválido. Use dd/MM/yyyy.");
            return;
        }

        System.out.print("Número do Quarto: ");
        int numeroQuarto;
        try {
            numeroQuarto = scanner.nextInt();
            scanner.nextLine(); 
        } catch (InputMismatchException e) {
            System.out.println("[ERRO] Número de quarto inválido.");
            scanner.nextLine(); 
            return;
        }

        // 4. Agendamento
        if (hospital.agendarInternacao(paciente, medico, dataEntrada, numeroQuarto)) {
            System.out.println("\n[SUCESSO] Internação agendada!");
            
            // Exibição da regra de negócio (baseado no Internacao.java)
            if (paciente instanceof PacienteEspecial) {
                 PacienteEspecial pe = (PacienteEspecial) paciente;
                 if (pe.getPlano().isInternaGarantida()) {
                      System.out.println("[INFO] Plano VIP: Internação garantida sem custo nos primeiros 7 dias.");
                 }
            }
        } else {
            System.out.println("[ERRO] Não foi possível agendar a internação (verifique se o quarto está ocupado).");
        }
    }

    // ------------------------------------
    // MENU 3: RELATÓRIOS (ATUALIZADO)
    // ------------------------------------
    private static void menuRelatorios() {
        int opcao = -1;
        do {
            System.out.println("\n--- MENU DE RELATÓRIOS ---");
            System.out.println("1. Pacientes");
            System.out.println("2. Médicos");
            System.out.println("3. Consultas");
            System.out.println("4. Internações"); // Nova Opção
            System.out.println("0. Voltar ao Menu Principal");
            System.out.print("Escolha uma opção: ");

            try {
                opcao = scanner.nextInt();
                scanner.nextLine(); // Consome a quebra de linha
            } catch (InputMismatchException e) {
                System.out.println("\n[ERRO] Entrada inválida. Digite um número correspondente à opção.");
                scanner.nextLine(); // Limpa o buffer
                opcao = -1;
                continue;
            }

            switch (opcao) {
                case 1: hospital.relatorioPacientes(); break;
                case 2: hospital.relatorioMedicos(); break;
                case 3: hospital.relatorioConsultas(); break;
                case 4: hospital.relatorioInternacoes(); break; // Nova Chamada
                case 0: break;
                default: System.out.println("[ERRO] Opção inválida.");
            }
        } while (opcao != 0);
    }
}