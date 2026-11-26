package br.senai.sistema.config;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import br.senai.sistema.model.Agendamento;
import br.senai.sistema.model.Cliente;
import br.senai.sistema.model.Perfil;
import br.senai.sistema.model.Servico;
import br.senai.sistema.model.StatusAgendamento;
import br.senai.sistema.model.Usuario;
import br.senai.sistema.repository.AgendamentoRepository;
import br.senai.sistema.repository.ClienteRepository;
import br.senai.sistema.repository.ServicoRepository;
import br.senai.sistema.repository.UsuarioRepository;

/**
 * Carga inicial de dados: executa uma vez sempre que a aplicação sobe.
 * Cada bloco só insere os dados se a tabela ainda estiver vazia (por isso não duplica).
 */
@Component
public class DadosIniciais implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ServicoRepository servicoRepository;
    private final ClienteRepository clienteRepository;
    private final AgendamentoRepository agendamentoRepository;

    public DadosIniciais(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                         ServicoRepository servicoRepository, ClienteRepository clienteRepository,
                         AgendamentoRepository agendamentoRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.servicoRepository = servicoRepository;
        this.clienteRepository = clienteRepository;
        this.agendamentoRepository = agendamentoRepository;
    }

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() == 0) {
            usuarioRepository.save(new Usuario("Administrador", "admin",
                    passwordEncoder.encode("admin123"), Perfil.ADMIN));
            usuarioRepository.save(new Usuario("Operador", "operador",
                    passwordEncoder.encode("operador123"), Perfil.OPERADOR));
        }

        if (servicoRepository.count() == 0) {
            Servico servico1 = new Servico("Troca de óleo", "Troca de óleo e filtro", 30, new BigDecimal("120.00"));
            Servico servico2 = new Servico("Alinhamento e balanceamento", "Alinhamento das rodas dianteiras e balanceamento", 60, new BigDecimal("150.00"));
            Servico servico3 = new Servico("Revisão completa", "Revisão de 30 itens", 120, new BigDecimal("380.00"));
            servicoRepository.save(servico1);
            servicoRepository.save(servico2);
            servicoRepository.save(servico3);
        }

        if (clienteRepository.count() == 0) {
            Cliente cliente1 = new Cliente("Ana Paula Ramos", "(43) 99555-1010", "ana@email.com");
            Cliente cliente2 = new Cliente("Bruno Lima", "(43) 99666-2020", "bruno@email.com");
            Cliente cliente3 = new Cliente("Carla Nogueira", "(43) 99777-3030", "carla@email.com");
            clienteRepository.save(cliente1);
            clienteRepository.save(cliente2);
            clienteRepository.save(cliente3);
        }

        // Operação principal: cada registro aponta para registros que já existem (relacionamento).
        // O "%" (resto da divisão) evita erro se existirem menos de 3 registros cadastrados.
        if (agendamentoRepository.count() == 0) {
            List<Cliente> clientes = clienteRepository.findAll(Sort.by("id"));
            List<Servico> servicos = servicoRepository.findAll(Sort.by("id"));
            if (!clientes.isEmpty() && !servicos.isEmpty()) {
                // Datas a partir de hoje (horário de Brasília), para o painel sempre mostrar dados.
                LocalDate hoje = LocalDate.now(ZoneId.of("America/Sao_Paulo"));
                Cliente c1 = clientes.get(0);
                Cliente c2 = clientes.get(1 % clientes.size());
                Cliente c3 = clientes.get(2 % clientes.size());
                Servico s1 = servicos.get(0);
                Servico s2 = servicos.get(1 % servicos.size());
                Servico s3 = servicos.get(2 % servicos.size());

                // Atendimentos que já aconteceram
                agendamentoRepository.save(new Agendamento(hoje.minusDays(3), LocalTime.of(9, 0), StatusAgendamento.CONCLUIDO, c1, s1));
                agendamentoRepository.save(new Agendamento(hoje.minusDays(2), LocalTime.of(14, 0), StatusAgendamento.CONCLUIDO, c2, s3));
                agendamentoRepository.save(new Agendamento(hoje.minusDays(1), LocalTime.of(10, 30), StatusAgendamento.CANCELADO, c3, s2));

                // Hoje e próximos dias
                agendamentoRepository.save(new Agendamento(hoje, LocalTime.of(8, 0), StatusAgendamento.AGENDADO, c1, s2));
                agendamentoRepository.save(new Agendamento(hoje, LocalTime.of(16, 0), StatusAgendamento.AGENDADO, c2, s1));
                Agendamento revisao = new Agendamento(hoje.plusDays(2), LocalTime.of(14, 0), StatusAgendamento.AGENDADO, c3, s3);
                revisao.setObservacao("Avisar por telefone quando o carro estiver pronto");
                agendamentoRepository.save(revisao);
            }
        }
    }
}
