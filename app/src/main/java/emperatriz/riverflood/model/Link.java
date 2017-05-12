package emperatriz.riverflood.model;

/**
 * Created by ramon on 24/04/2017.
 */

public class Link {

    public static final int ESP=0, ENG=1, POR=2, ITA=3, RU=4, DESCONOCIDO=5, GER=6, UCR=7, SUE=8, CRO=9, LET=10, FRA=11;
    public static final int FPS50=0, FPS25=1, NOFPS=2;
    public static final int HD1080=0, HD720=1, SD576=2, NORES=4;

    public String url, acestream;
    public int idioma, kbps, fps, resolution;
}
