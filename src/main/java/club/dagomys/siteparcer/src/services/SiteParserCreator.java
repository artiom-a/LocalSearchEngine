package club.dagomys.siteparcer.src.services;

public class SiteParserCreator {

    private static SiteParserRunner SITE_PARSER;
    private static String url;
    private static PageService pageService;

    public SiteParserCreator(String url, PageService pageService){
    }

    public static SiteParserRunner getIstance(){
        if (SITE_PARSER == null){
            synchronized (SiteParserRunner.class) {
                SITE_PARSER = new SiteParserRunner(url, pageService);
            }
        }
        return SITE_PARSER;
    }
}
