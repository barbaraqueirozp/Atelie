package atelie_api.model;

import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Lê a VIEW public.vw_obras_disponiveis (somente leitura).
 * A view já junta obra + vendedor + imagem de capa, que é exatamente
 * o que as telas do app mostram.
 */
@Entity
@Immutable
@Table(name = "vw_obras_disponiveis", schema = "public")
public class ObraDisponivel {

    @Id
    @Column(name = "id_obra")
    private Integer idObra;

    private String titulo;
    private String descricao;

    /** No app, é a "categoria": Pintura, Escultura ou Fotografia. */
    private String tecnica;

    private String dimensoes;
    private BigDecimal preco;

    @Column(name = "data_cadastro")
    private LocalDateTime dataCadastro;

    private Integer visualizacoes;

    @Column(name = "id_vendedor")
    private Integer idVendedor;

    @Column(name = "nome_vendedor")
    private String nomeVendedor;

    private String cidade;
    private String estado;

    @Column(name = "imagem_capa")
    private String imagemCapa;

    public Integer getIdObra() { return idObra; }
    public String getTitulo() { return titulo; }
    public String getDescricao() { return descricao; }
    public String getTecnica() { return tecnica; }
    public String getDimensoes() { return dimensoes; }
    public BigDecimal getPreco() { return preco; }
    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public Integer getVisualizacoes() { return visualizacoes; }
    public Integer getIdVendedor() { return idVendedor; }
    public String getNomeVendedor() { return nomeVendedor; }
    public String getCidade() { return cidade; }
    public String getEstado() { return estado; }
    public String getImagemCapa() { return imagemCapa; }
}
