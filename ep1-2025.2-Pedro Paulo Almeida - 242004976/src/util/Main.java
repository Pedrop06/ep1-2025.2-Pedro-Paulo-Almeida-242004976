package util;

import modelos.*;
import servicos.*;

import java.util.Scanner;
import java.time.LocalDateTime;
import java.time.LocalDate; 
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.InputMismatchException;
import java.util.List;

public class Main {
    private static Hospital hospital;
    private static Scanner scanner = new Scanner(System.in);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy"); 

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
                case 4: menuConclusao(); break; 
                case 0:
                    System.out.println("\nSaindo do Sistema...");
                    hospital.salvarDadosCSV(); // Salva em CSV antes de sair
                    break;
                default:
                    System.out.println("[ERRO] Opção inválida.");
            }
        } while (opcao != 0);
    }
    
    // ------------------------------------
    // MENU PRINCIPAL
    // ------------------------------------
    private static void exibirMenuPrincipal() {
        System.out.println("\n--- MENU PRINCIPAL ---");
        System.out.println("1. Cadastro (Paciente, Médico)");
        System.out.println("2. Agendamento (Consulta, Internação)");
        System.out.println("3. Relatórios");
        System.out.println("4. Concluir Atendimento (Consulta/Internação)");
        System.out.println("0. Sair e Salvar Dados");
        System.out.print("Escolha uma opção: ");
    }
    
    // ------------------------------------
    // MENU 2: AGENDAMENTO (NOVO)
    // ------------------------------------
    private static void menuAgendamento() {
        int opcao = -1;
        do {
            System.out.println("\n--- MENU DE AGENDAMENTO ---");
            System.out.println("1. Agendar Nova Consulta (Por CPF/CRM)");
            System.out.println("2. Agendar Nova Internação (Por CPF/CRM)");
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
                case 1: agendarConsultaPorDocumento(); break;
                case 2: agendarInternacaoPorDocumento(); break;
                case 0: break;
                default: System.out.println("[ERRO] Opção inválida.");
            }
        } while (opcao != 0);
    }
    
    private static void agendarConsultaPorDocumento() {
        System.out.println("\n--- AGENDAR CONSULTA (Por CPF/CRM) ---");

        System.out.print("Digite o CPF do Paciente: ");
        String cpf = scanner.nextLine();

        System.out.print("Digite o CRM do Médico: ");
        String crm = scanner.nextLine();

        System.out.print("Digite a Data e Hora da Consulta (dd/MM/yyyy HH:mm): ");
        String dataHoraStr = scanner.nextLine();
        LocalDateTime dataHora;
        try {
            dataHora = LocalDateTime.parse(dataHoraStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("[ERRO] Formato de data/hora inválido. Use dd/MM/yyyy HH:mm.");
            return;
        }

        System.out.print("Digite o local da consulta (ex: Sala 3): ");
        String local = scanner.nextLine();

        // Chama o novo método do Hospital
        if (hospital.agendarConsulta(cpf, crm, dataHora, local)) {
            System.out.println("[SUCESSO] Consulta agendada com sucesso!");
        }
    }
    
    private static void agendarInternacaoPorDocumento() {
        System.out.println("\n--- AGENDAR INTERNAÇÃO (Por CPF/CRM) ---");

        System.out.print("Digite o CPF do Paciente: ");
        String cpf = scanner.nextLine();

        System.out.print("Digite o CRM do Médico Responsável: ");
        String crm = scanner.nextLine();

        System.out.print("Digite a Data de Entrada (dd/MM/yyyy): ");
        String dataEntradaStr = scanner.nextLine();
        LocalDate dataEntrada;
        try {
            dataEntrada = LocalDate.parse(dataEntradaStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("[ERRO] Formato de data inválido. Use dd/MM/yyyy.");
            return;
        }
        
        System.out.print("Digite o número do Quarto: ");
        int numeroQuarto;
        try {
            numeroQuarto = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("[ERRO] Número do Quarto deve ser um número inteiro.");
            scanner.nextLine();
            return;
        }

        // Chama o novo método do Hospital
        if (hospital.agendarInternacao(cpf, crm, dataEntrada, numeroQuarto)) {
            System.out.println("[SUCESSO] Internação agendada com sucesso!");
        }
    }
    
    // ------------------------------------
    // MENU 4: CONCLUSÃO
    // ------------------------------------
    private static void menuConclusao() {
        int opcao = -1;
        do {
            System.out.println("\n--- MENU DE CONCLUSÃO DE ATENDIMENTO ---");
            System.out.println("1. Concluir Consulta");
            System.out.println("2. Concluir Internação");
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
                case 1: concluirConsulta(); break;
                case 2: concluirInternacao(); break;
                case 0: break;
                default: System.out.println("[ERRO] Opção inválida.");
            }
        } while (opcao != 0);
    }

    private static void concluirConsulta() {
        if (hospital.getConsultas().isEmpty()) {
            System.out.println("\nNão há consultas agendadas para concluir.");
            return;
        }
        
        System.out.println("\n--- CONSULTAS AGENDADAS ---");
        hospital.relatorioConsultas(); // Exibe com índice
        
        System.out.print("\nDigite o índice da consulta a ser concluída: ");
        int indice;
        try {
            indice = scanner.nextInt();
            scanner.nextLine(); 
        } catch (InputMismatchException e) {
            System.out.println("[ERRO] Índice inválido.");
            scanner.nextLine();
            return;
        }
        
        System.out.print("Digite o Diagnóstico/Resultado da Consulta: ");
        String diagnostico = scanner.nextLine();

        if (hospital.concluirERemoverConsulta(indice, diagnostico)) {
            System.out.println("[SUCESSO] Consulta concluída e adicionada ao histórico do paciente.");
        } else {
            System.out.println("[ERRO] Índice da consulta fora do limite.");
        }
    }

    private static void concluirInternacao() {
         if (hospital.getInternacoes().isEmpty()) {
            System.out.println("\nNão há internações ativas para concluir.");
            return;
        }

        System.out.println("\n--- INTERNAÇÕES ATIVAS ---");
        hospital.relatorioInternacoes(); // Exibe com índice

        System.out.print("\nDigite o índice da internação a ser concluída: ");
        int indice;
        try {
            indice = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("[ERRO] Índice inválido.");
            scanner.nextLine();
            return;
        }

        System.out.print("Digite a Data de Saída (dd/MM/yyyy): ");
        String dataSaidaStr = scanner.nextLine();
        LocalDate dataSaida;
        try {
            dataSaida = LocalDate.parse(dataSaidaStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("[ERRO] Formato de data inválido. Use dd/MM/yyyy.");
            return;
        }

        if (hospital.concluirERemoverInternacao(indice, dataSaida)) {
            System.out.println("[SUCESSO] Internação finalizada e adicionada ao histórico do paciente.");
        } else {
            System.out.println("[ERRO] Índice da internação fora do limite.");
        }
    }
    
    // ------------------------------------
    // MENU 1: CADASTRO
    // ------------------------------------
    private static void menuCadastro() {
        int opcao = -1;
        do {
            System.out.println("\n--- MENU DE CADASTRO ---");
            System.out.println("1. Cadastrar Paciente Comum");
            System.out.println("2. Cadastrar Paciente Especial (Com Plano)");
            System.out.println("3. Cadastrar Médico");
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
                case 1: cadastrarPacienteComum(); break;
                case 2: cadastrarPacienteEspecial(); break;
                case 3: cadastrarMedico(); break;
                case 0: break;
                default: System.out.println("[ERRO] Opção inválida.");
            }
        } while (opcao != 0);
    }
    
    private static void cadastrarPacienteComum() {
        System.out.println("\n--- CADASTRAR PACIENTE COMUM ---");
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
            System.out.println("[ERRO] Idade deve ser um número inteiro.");
            scanner.nextLine();
            return;
        }

        Paciente p = new Paciente(nome, cpf, idade);
        hospital.cadastrarPaciente(p);
        System.out.println("[SUCESSO] Paciente Comum cadastrado.");
    }
    
    private static void cadastrarPacienteEspecial() {
        System.out.println("\n--- CADASTRAR PACIENTE ESPECIAL ---");
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
            System.out.println("[ERRO] Idade deve ser um número inteiro.");
            scanner.nextLine();
            return;
        }

        // Exibir planos disponíveis
        List<PlanoSaude> planos = hospital.getPlanos();
        if (planos.isEmpty()) {
            System.out.println("[ERRO] Não há planos de saúde cadastrados.");
            return;
        }

        System.out.println("\n--- PLANOS DISPONÍVEIS ---");
        for (int i = 0; i < planos.size(); i++) {
            System.out.printf("[%d] %s (Desconto: %.0f%%)%n", i, planos.get(i).getNome(), planos.get(i).getDescontoGeral() * 100);
        }

        System.out.print("Digite o índice do plano a ser associado: ");
        int indicePlano;
        try {
            indicePlano = scanner.nextInt();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("[ERRO] Índice inválido.");
            scanner.nextLine();
            return;
        }

        if (indicePlano >= 0 && indicePlano < planos.size()) {
            PlanoSaude plano = planos.get(indicePlano);
            PacienteEspecial pe = new PacienteEspecial(nome, cpf, idade, plano);
            hospital.cadastrarPaciente(pe);
            System.out.println("[SUCESSO] Paciente Especial cadastrado com o plano: " + plano.getNome());
        } else {
            System.out.println("[ERRO] Índice do plano fora do limite.");
        }
    }
    
    private static void cadastrarMedico() {
        System.out.println("\n--- CADASTRAR MÉDICO ---");
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
            System.out.println("[ERRO] Idade deve ser um número inteiro.");
            scanner.nextLine();
            return;
        }
        System.out.print("CRM: ");
        String crm = scanner.nextLine();
        System.out.print("Especialidade: ");
        String especialidade = scanner.nextLine();
        System.out.print("Custo Base da Consulta (ex: 150.00): ");
        double custoConsulta;
        try {
            custoConsulta = scanner.nextDouble();
            scanner.nextLine();
        } catch (InputMismatchException e) {
            System.out.println("[ERRO] Custo deve ser um valor numérico.");
            scanner.nextLine();
            return;
        }

        Medico m = new Medico(nome, cpf, idade, crm, especialidade, custoConsulta);
        hospital.cadastrarMedico(m);
        System.out.println("[SUCESSO] Médico cadastrado.");
    }


    // ------------------------------------
    // MENU 3: RELATÓRIOS
    // ------------------------------------
    private static void menuRelatorios() {
        int opcao = -1;
        do {
            System.out.println("\n--- MENU DE RELATÓRIOS ---");
            System.out.println("1. Pacientes (Inclui Históricos)");
            System.out.println("2. Médicos");
            System.out.println("3. Consultas (Ativas e Histórico)");
            System.out.println("4. Internações (Ativas e Histórico)");
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
                case 1: hospital.relatorioPacientes(); break;
                case 2: hospital.relatorioMedicos(); break;
                case 3: hospital.relatorioConsultas(); break;
                case 4: hospital.relatorioInternacoes(); break; 
                case 0: break;
                default: System.out.println("[ERRO] Opção inválida.");
            }
        } while (opcao != 0);
    }
}