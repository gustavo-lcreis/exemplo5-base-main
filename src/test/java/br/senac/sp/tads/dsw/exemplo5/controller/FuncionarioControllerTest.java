package br.senac.sp.tads.dsw.exemplo5.controller;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import br.senac.sp.tads.dsw.exemplo5.model.Departamento;
import br.senac.sp.tads.dsw.exemplo5.model.Funcionario;
import br.senac.sp.tads.dsw.exemplo5.repository.DepartamentoRepository;
import br.senac.sp.tads.dsw.exemplo5.repository.FuncionarioRepository;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FuncionarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FuncionarioRepository funcionarioRepository;

    @Autowired
    private DepartamentoRepository departamentoRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Departamento departamentoPadrao;

    @BeforeEach
    void setUp() {
        Departamento departamento = new Departamento();
        departamento.setNome("Desenvolvimento");
        departamento.setOrcamento(200000.00);

        departamentoPadrao = departamentoRepository.save(departamento);
    }

    @Test
    void deveCriarFuncionarioComSucesso() throws Exception {

        Funcionario funcionario = new Funcionario();

        funcionario.setNome("João Silva");
        funcionario.setDataContratacao(LocalDate.now());
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);

        mockMvc.perform(post("/api/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(funcionario)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nome").value("João Silva"));
    }

    @Test
    void deveRetornarErro400AoCriarFuncionarioComDataFutura() throws Exception {

        Funcionario funcionario = new Funcionario();

        funcionario.setNome("João Silva");

        // Data futura
        funcionario.setDataContratacao(LocalDate.now().plusDays(10));

        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);

        mockMvc.perform(post("/api/funcionarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(funcionario)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deveListarTodosFuncionarios() throws Exception {

        Funcionario funcionario1 = new Funcionario();
        funcionario1.setNome("João Silva");
        funcionario1.setDataContratacao(LocalDate.now());
        funcionario1.setTrabalhoRemoto(false);
        funcionario1.setDepartamento(departamentoPadrao);

        Funcionario funcionario2 = new Funcionario();
        funcionario2.setNome("Maria Souza");
        funcionario2.setDataContratacao(LocalDate.now());
        funcionario2.setTrabalhoRemoto(true);
        funcionario2.setDepartamento(departamentoPadrao);

        funcionarioRepository.save(funcionario1);
        funcionarioRepository.save(funcionario2);

        mockMvc.perform(get("/api/funcionarios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nome").value("João Silva"))
                .andExpect(jsonPath("$[1].nome").value("Maria Souza"));
    }

    @Test
    void deveBuscarFuncionarioPorId() throws Exception {

        Funcionario funcionario = new Funcionario();

        funcionario.setNome("João Silva");
        funcionario.setDataContratacao(LocalDate.now());
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);

        Funcionario funcionarioSalvo =
                funcionarioRepository.save(funcionario);

        mockMvc.perform(
                get("/api/funcionarios/" + funcionarioSalvo.getId())
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(funcionarioSalvo.getId()))
                .andExpect(jsonPath("$.nome")
                        .value("João Silva"));
    }

    @Test
    void deveAtualizarFuncionario() throws Exception {

        Funcionario funcionario = new Funcionario();

        funcionario.setNome("João Silva");
        funcionario.setDataContratacao(LocalDate.now());
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);

        Funcionario funcionarioSalvo =
                funcionarioRepository.save(funcionario);

        Funcionario funcionarioAtualizado = new Funcionario();

        funcionarioAtualizado.setNome("João Silva Atualizado");
        funcionarioAtualizado.setDataContratacao(LocalDate.now());
        funcionarioAtualizado.setTrabalhoRemoto(true);
        funcionarioAtualizado.setDepartamento(departamentoPadrao);

        mockMvc.perform(
                put("/api/funcionarios/" + funcionarioSalvo.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        funcionarioAtualizado
                                )
                        )
        )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome")
                        .value("João Silva Atualizado"))
                .andExpect(jsonPath("$.trabalhoRemoto")
                        .value(true));
    }

    @Test
    void deveApagarFuncionario() throws Exception {

        Funcionario funcionario = new Funcionario();

        funcionario.setNome("João Silva");
        funcionario.setDataContratacao(LocalDate.now());
        funcionario.setTrabalhoRemoto(false);
        funcionario.setDepartamento(departamentoPadrao);

        Funcionario funcionarioSalvo =
                funcionarioRepository.save(funcionario);

        mockMvc.perform(
                delete("/api/funcionarios/" + funcionarioSalvo.getId())
        )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                get("/api/funcionarios/" + funcionarioSalvo.getId())
        )
                .andExpect(status().isNotFound());
    }
}
