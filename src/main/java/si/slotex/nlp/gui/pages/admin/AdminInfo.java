package si.slotex.nlp.gui.pages.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.router.Route;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;
import si.slotex.nlp.gui.managers.RemoteApiManager;
import javax.annotation.PostConstruct;
import java.io.IOException;


@Route(value = "info", layout = SlotexMainLayout.class)
public class AdminInfo extends ContentView {

    private JsonNode infoNode;
    private JsonNode healthNode;

    private final RemoteApiManager infoManager;

    public AdminInfo(RemoteApiManager infoManager){
        this.infoManager = infoManager;
    }

    @PostConstruct
    protected void createMainContent(){
        String infoJson = infoManager.getData("manage/info");
        String healthJson = infoManager.getData("manage/health");
        ObjectMapper objectMapper = new ObjectMapper();
        try{
            infoNode = objectMapper.readTree(infoJson);
            healthNode = objectMapper.readTree(healthJson);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        Html application = new Html("<b> Application: </b>");
        Html applicationName = new Html("<p>"+infoNode.get("app").get("name").asText()+"</p>");
        Html applicationDesc = new Html("<p>"+infoNode.get("app").get("description").asText()+"</p>");

        Html buildName = new Html("<p>"+infoNode.get("build").get("name").asText()+"</p>");
        Html buildVersion = new Html("<p>"+infoNode.get("build").get("version").asText()+"</p>");

        String status = (healthNode.get("status").asText().equals("UP") ? "<span style=\"color:Green;\">UP</span>" : "<span style=\"color:Red;\">DOWN</span>");
        String redisStatus = (healthNode.get("details").get("redis").get("status").asText().equals("UP") ? "<span style=\"color:Green;\">UP</span>" : "<span style=\"color:Red;\">DOWN</span>");
        String mongoStatus = (healthNode.get("details").get("mongo").get("status").asText().equals("UP") ? "<span style=\"color:Green;\">UP</span>" : "<span style=\"color:Red;\">DOWN</span>");

        Html statusApp = new Html("<p><b> Status App: </b>" + status+"</p>");
        Html statusRedis = new Html( "<p>Status Redis: "
                + healthNode.get("details").get("redis").get("details").get("version").asText() + " | "
                + redisStatus+"</p>");
        Html statusMongo = new Html("<p> Status Mongo: "
                + healthNode.get("details").get("mongo").get("details").get("version").asText() + " | "
                + mongoStatus +"</p>");

        add(application,applicationName,applicationDesc,buildName,buildVersion,statusApp,statusRedis,statusMongo);
    }
}
