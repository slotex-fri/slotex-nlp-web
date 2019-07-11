package si.slotex.nlp.gui.managers;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Manager {

    private Logger logger = Logger.getLogger(Manager.class);

    @Value("${remote.scheme}")
    protected String scheme;
    @Value("${remote.host}")
    protected String host;
    @Value("${remote.port}")
    protected String port;

    private RestTemplate restTemplate;

    public Manager(){}

    public String getData(String path){
        logger.info("fetching data from "+path+" ...");
        String url = scheme+"://"+host+":"+port+"/"+path;
        restTemplate = new RestTemplate();
        return restTemplate.getForObject(url,String.class);
    }
}
