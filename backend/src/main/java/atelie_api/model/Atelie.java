package atelie_api.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalTime;

/** Representa a tabela public.atelie (um ateliê por vendedor). */
@Entity
@Table(name = "atelie", schema = "public")
public class Atelie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_atelie")
    private Integer idAtelie;

    @Column(name = "id_usuario")
    private Integer idUsuario;

    private String nome;
    private String especialidade;
    private String cep;
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @Column(name = "aceita_visitas")
    private boolean aceitaVisitas = true;

    /** Dias da semana em que recebe visitas: "2,3,4,5,6" (1=segunda ... 7=domingo). */
    @Column(name = "dias_atendimento")
    private String diasAtendimento = "2,3,4,5,6";

    @Column(name = "hora_abertura")
    private LocalTime horaAbertura = LocalTime.of(10, 0);

    @Column(name = "hora_fechamento")
    private LocalTime horaFechamento = LocalTime.of(18, 0);

    public Integer getIdAtelie() { return idAtelie; }
    public Integer getIdUsuario() { return idUsuario; }
    public void setIdUsuario(Integer idUsuario) { this.idUsuario = idUsuario; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEspecialidade() { return especialidade; }
    public void setEspecialidade(String especialidade) { this.especialidade = especialidade; }
    public String getCep() { return cep; }
    public void setCep(String cep) { this.cep = cep; }
    public String getLogradouro() { return logradouro; }
    public void setLogradouro(String logradouro) { this.logradouro = logradouro; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getComplemento() { return complemento; }
    public void setComplemento(String complemento) { this.complemento = complemento; }
    public String getBairro() { return bairro; }
    public void setBairro(String bairro) { this.bairro = bairro; }
    public String getCidade() { return cidade; }
    public void setCidade(String cidade) { this.cidade = cidade; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public BigDecimal getLatitude() { return latitude; }
    public void setLatitude(BigDecimal latitude) { this.latitude = latitude; }
    public BigDecimal getLongitude() { return longitude; }
    public void setLongitude(BigDecimal longitude) { this.longitude = longitude; }
    public boolean isAceitaVisitas() { return aceitaVisitas; }
    public void setAceitaVisitas(boolean aceitaVisitas) { this.aceitaVisitas = aceitaVisitas; }
    public String getDiasAtendimento() { return diasAtendimento; }
    public void setDiasAtendimento(String diasAtendimento) { this.diasAtendimento = diasAtendimento; }
    public LocalTime getHoraAbertura() { return horaAbertura; }
    public void setHoraAbertura(LocalTime horaAbertura) { this.horaAbertura = horaAbertura; }
    public LocalTime getHoraFechamento() { return horaFechamento; }
    public void setHoraFechamento(LocalTime horaFechamento) { this.horaFechamento = horaFechamento; }
}
