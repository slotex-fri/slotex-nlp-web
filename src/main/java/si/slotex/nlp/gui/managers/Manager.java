package si.slotex.nlp.gui.managers;

import com.vaadin.flow.component.notification.Notification;
import org.jboss.logging.Logger;
import org.jboss.resteasy.plugins.providers.multipart.MultipartFormDataOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import si.slotex.nlp.entity.CorpusSentenceDiff;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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

    public Manager(){
        restTemplate = new RestTemplate();
    }

    // Makes get request to specified url and returns String object of response
    public String getData(String path){
        logger.info("fetching data from "+path+" ...");
        String url = scheme+"://"+host+":"+port+"/"+path;
        return restTemplate.getForObject(url,String.class);
    }


    // Retrieves all sentences of specified model restricted by parameters startLine and endLine
    public List<CorpusSentenceDiff> getAllInRange(String modelType, Integer startLine, Integer endLine)
    {
        logger.info(".getAllInRange() starting...");
        ResponseEntity<List<CorpusSentenceDiff>> response = restTemplate.exchange(
                scheme+"://"+host + ":" + port + "/corpus/" + modelType.toLowerCase() + "?startLine=" + startLine + "&endLine=" + endLine,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<CorpusSentenceDiff>>(){});
        return response.getBody();
    }

    //downloads specified corpus to local disk
    public void downloadCorpus(String entity)
    {
        logger.info(".downloadCorpus() starting...");
        try {
            String name = "slo-ner-" + entity + ".train";
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM));
            HttpEntity<String> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<byte[]> response = restTemplate.exchange(scheme+"://"+host + ":" + port + "/files/ner/" + name, HttpMethod.GET, httpEntity, byte[].class);
            String userDir = System.getProperty("user.home");
            logger.info("Saving the file to the user directory: " + userDir);
            Files.write(Paths.get(userDir+"/"+name), response.getBody());
            new Notification("Corpus successfully saved to " + userDir + " as " + name,3000).open();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //uploads new corpus to learn from
    public void uploadCorpus(File file, String modelType)
    {
        logger.info(".uploadCorpus() starting...");
        String url = scheme+"://"+host + ":" + port + "/files/upload";

        logger.info("Sending corrected corpus for the following model entity: " + modelType);
        MultipartFormDataOutput multipartData = new MultipartFormDataOutput();
        try
        {
            multipartData.addFormData("file",new FileInputStream(file), MediaType.APPLICATION_OCTET_STREAM_TYPE, "slo-ner-" + modelType + ".train");
        }
        catch (FileNotFoundException fnf)
        {
            logger.warn("File for upload was not found!");
        }


        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.MULTIPART_FORM_DATA);
        HttpEntity<Object> entity = new HttpEntity<>(multipartData,headers);
        ResponseEntity<String> response = restTemplate.postForEntity(url,entity,String.class);

        logger.info("Responded with: "+response.toString());
        logger.info("Sent the newly created corpus from statistical model and dictionary!");
    }

    //saves sentences to specified corpus
    public void saveSentences(String corpusName, List<CorpusSentenceDiff> CorpusSentenceDiff)
    {
        logger.info("Saving corrected corpus sentences...");
        String url = scheme+"://"+host + ":" + port + "/corpus/"+corpusName;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        HttpEntity<Object> requestEntity = new HttpEntity<Object>(CorpusSentenceDiff,headers);
        restTemplate.exchange(url, HttpMethod.POST, requestEntity,new ParameterizedTypeReference<List<CorpusSentenceDiff>>(){});
    }
}
