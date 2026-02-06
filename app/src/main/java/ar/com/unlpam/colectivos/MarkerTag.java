package ar.com.unlpam.colectivos;

public class MarkerTag {
    private int id;
    private final String tipo;

    public MarkerTag(int id, String tipo) {
        this.id = id;
        this.tipo = tipo;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTipo() {
        return tipo;
    }

}
