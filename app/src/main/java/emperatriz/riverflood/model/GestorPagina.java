package emperatriz.riverflood.model;

public interface GestorPagina {

    public String getNombre();
    public int getId();
    public void parseData();
    public void parseDataRefresh();
    public void getLinks(int index);


}
