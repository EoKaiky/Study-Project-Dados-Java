package br.com.alura.screenmatch.model;

public enum Categoria {
    ACAO("Action", "Ação"),
    COMEDIA("comedy", "Comedia"),
    ROMANCE("Romance", "Romance"),
    DRAMA("Drama", "Drama"),
    CRIME("Crime", "Crime"),
    FICCAO("Ficcao", "Ficção");

    private String categoriaOmdb;
    private String categoriaPortugues;

    Categoria(String categoriaOmdb, String categoriaPortugues){
        this.categoriaOmdb = categoriaOmdb;
        this.categoriaPortugues = categoriaPortugues;
    }

    public static Categoria fromString(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.categoriaOmdb.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada!");
    }

    public static Categoria fromPortugues(String text) {
        for (Categoria categoria : Categoria.values()) {
            if (categoria.categoriaPortugues.equalsIgnoreCase(text)) {
                return categoria;
            }
        }
        throw new IllegalArgumentException("Nenhuma categoria encontrada!");
    }
}
