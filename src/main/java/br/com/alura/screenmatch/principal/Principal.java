package br.com.alura.screenmatch.principal;

import br.com.alura.screenmatch.model.*;
import br.com.alura.screenmatch.repository.SerieRepository;
import br.com.alura.screenmatch.service.ConsumoApi;
import br.com.alura.screenmatch.service.ConverteDados;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Savepoint;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {

    private SerieRepository repository;

    private Scanner leitura = new Scanner(System.in);
    private ConsumoApi consumo = new ConsumoApi();
    private ConverteDados conversor = new ConverteDados();
    private final String ENDERECO = "https://www.omdbapi.com/?t=";
    private final String API_KEY = "&apikey=6585022c";
    private List<DadosSerie> dadosSeries = new ArrayList<>();
    private List <Serie> series = new ArrayList<>();

    public Principal(SerieRepository repository) {
        this.repository = repository;
    }

    public void exibeMenu() {
        var opcao = -1;
        while ( opcao != 0) {
            var menu = """
                    1 - Buscar séries
                    2 - Buscar episódios
                    3 - Listar séries buscadas
                    4 - Buscar serie por titulo
                    5 - Buscar serie por ator
                    6 - Listar top 5 Séries
                    7 - Buscar série por categoria
                    8 - Filtrar séries por numero de temporada
                    9 - Filtrar séries por avaliação
                    10 - Buscar episódio por trecho
                    11 - Buscar melhores episódios da série
                    
                    0 - Sair                                 
                    """;

            System.out.println(menu);
            opcao = leitura.nextInt();
            leitura.nextLine();

            switch (opcao) {
                case 1:
                    buscarSerieWeb();
                    break;
                case 2:
                    buscarEpisodioPorSerie();
                    break;
                case 3:
                    listarSeriesBuscadas();
                    break;
                case 4:
                    buscarSeriePorTitulo();
                    break;
                case 5:
                    buscarSeriesPorAtor();
                    break;
                case 6:
                    buscarTop5Series();
                    break;
                case 7:
                    buscarSeriePorGenero();
                    break;
                case 8:
                    filtrarSerieTemporada();
                    break;
                case 9:
                    filtrarSerieAvaliacao();
                    break;
                case 10:
                    buscarEpisodioTrecho();
                    break;
                case 11:
                    topEpisodiosSerie();
                    break;
                case 0:
                    System.out.println("Saindo...");
                    break;
                default:
                    System.out.println("Opção inválida");
            }
        }
    }

    private Optional<Serie> serieBusca;

    //metodo que capta nome digitado por usuario e busca no postgress o nome digitado
    private void buscarSeriePorTitulo() {
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();
        serieBusca = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serieBusca.isPresent()) {
            System.out.println("Dados da Serie: " + serieBusca.get());
        } else {
            System.out.println("Série não econtrada!");
        }
    }

    //metodo que salva serie buscada pelo usuario no banco de dados
    private void buscarSerieWeb() {
        DadosSerie dados = getDadosSerie();
        Serie serie = new Serie(dados);
        //dadosSeries.add(dados);
        repository.save(serie);
        System.out.println(dados);
    }

    //metodo que capta o nome digitado pelo usuario e faz requisicao na API buscano a serie escolhida pelo usuario
    private DadosSerie getDadosSerie() {
        System.out.println("Digite o nome da série para busca");
        var nomeSerie = leitura.nextLine();
        var json = consumo.obterDados(ENDERECO + nomeSerie.replace(" ", "+") + API_KEY);
        DadosSerie dados = conversor.obterDados(json, DadosSerie.class);
        return dados;
    }

    
    private void buscarEpisodioPorSerie(){
        listarSeriesBuscadas();
        System.out.println("Escolha uma série pelo nome: ");
        var nomeSerie = leitura.nextLine();


        Optional<Serie> serie = repository.findByTituloContainingIgnoreCase(nomeSerie);

        if (serie.isPresent()) {

            var serieEncontrada = serie.get();
            List<DadosTemporada> temporadas = new ArrayList<>();

            for (int i = 1; i <= serieEncontrada.getTotalTemporadas(); i++) {
                var json = consumo.obterDados(ENDERECO + serieEncontrada.getTitulo().replace(" ", "+") + "&season=" + i + API_KEY);
                DadosTemporada dadosTemporada = conversor.obterDados(json, DadosTemporada.class);
                temporadas.add(dadosTemporada);
            }
            temporadas.forEach(System.out::println);

            List<Episodio> episodios = temporadas.stream()
                    .flatMap(d -> d.episodios().stream()
                            .map(e -> new Episodio(d.numero(), e)))
                    .collect(Collectors.toList());

            serieEncontrada.setEpisodios(episodios);
            repository.save(serieEncontrada);

        } else {
            System.out.println("Série não encontrada!");
        }

    }

    private void buscarSeriesPorAtor() {
        System.out.println("Qual o nome para busca: ");
        var nomeAtor = leitura.nextLine();
        System.out.println("Avaliações a partir de que valor: ");
        var avaliacao = leitura.nextDouble();
        List<Serie> seriesEncontradas = repository.findByAtoresContainingIgnoreCaseAndAvaliacaoGreaterThanEqual(nomeAtor, avaliacao);
        System.out.println( "Séries em que " + nomeAtor + " Trabalhou:");
        seriesEncontradas.forEach( s ->
                System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
    }

    private void buscarTop5Series() {
        List<Serie> serieTopFive = repository.findTop5ByOrderByAvaliacaoDesc();
        serieTopFive.forEach(s ->
                System.out.println(s.getTitulo() + " avaliação: " + s.getAvaliacao()));
    }

    private void listarSeriesBuscadas(){

        series = repository.findAll();
        series = repository.findAll();
        series.stream()
                .sorted(Comparator.comparing(Serie::getGenero))
                .forEach(System.out::println);
    }

    //Método que busca todas as séries do genero digitado
    public void buscarSeriePorGenero(){
        System.out.println("Deseja buscar séries de qual gênero ?");
        var nomeGenero = leitura.nextLine();
        Categoria categoria = Categoria.fromPortugues(nomeGenero);
        List<Serie> seriesPorCategoria =  repository.findByGenero(categoria);
        System.out.println("Séries da categoria " +nomeGenero );
        seriesPorCategoria.forEach(System.out::println);
    }

    //Filtrar série por quantidade de temporada
    private void filtrarSerieTemporada() {
        System.out.println("Filtrar séries até quantas temporadas ?");
        var totalTemporadas = leitura.nextInt();
        List<Serie> serieTemporada = repository.seriesTemporada(totalTemporadas);
        serieTemporada.forEach(System.out::println);
    }

    //Buscar série por filtro de avaliação
    private void filtrarSerieAvaliacao() {
        System.out.println("Você quer ver séries a partir de qual avaliação ?");
        var numAvaliacao = leitura.nextDouble();
        List<Serie> serieAvaliacao = repository.findByAvaliacao(numAvaliacao);
        serieAvaliacao.forEach(System.out::println);
    }

    //Método que busca episódio quando digitado apenas uma palavra do nome do mesmo
    private void buscarEpisodioTrecho(){
        System.out.println("Qual nome para busca ?");
        var nameEpisodio = leitura.nextLine();
        List<Episodio> episodiosEncontrado = repository.episodiosPorTrecho(nameEpisodio);
        episodiosEncontrado.forEach(e -> System.out.printf("Série: %s Temporada %s - Episódio %s - %s\n",
                e.getSerie().getTitulo(), e.getTemporada(),
                e.getNumeroEpisodio(), e.getTitulo()));
    }

    private void topEpisodiosSerie() {
        buscarSeriePorTitulo();
        if (serieBusca.isPresent()) {
            Serie serie = serieBusca.get();
            List<Episodio> topEpisodios = repository.topEpisodiosSerie(serie);
            topEpisodios.forEach(e ->
                    System.out.printf("Série: %s Temporada %s - Episódio %s - %s Avaliação %s\n",
                    e.getSerie().getTitulo(), e.getTemporada(),
                    e.getNumeroEpisodio(), e.getTitulo(), e.getAvaliacao()));
        }

    }

}