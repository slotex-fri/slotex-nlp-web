package si.slotex.nlp.gui.pages.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;
import si.slotex.nlp.gui.managers.Manager;
import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;


@Route(value = "info", layout = SlotexMainLayout.class)
public class AdminInfo extends ContentView {

    private JsonNode infoNode;
    private JsonNode healthNode;
    private JsonNode queueStatus;
    private final Manager infoManager;
    private ObjectMapper objectMapper;
    private Html waitQueueSize;
    private Html workQueueSize;
    private Html waitQueueIDs;
    private Html workQueueIDs;
    public AdminInfo(Manager infoManager){
        this.infoManager = infoManager;
    }

    @PostConstruct
    protected void createMainContent(){
        String infoJson = infoManager.getData("manage/info");
        String healthJson = infoManager.getData("manage/health");
        String queueJson = infoManager.getData("admin/status");
        objectMapper = new ObjectMapper();
        try{
            infoNode = objectMapper.readTree(infoJson);
            healthNode = objectMapper.readTree(healthJson);
            queueStatus = objectMapper.readTree(queueJson);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        waitQueueSize = new Html("<p>Number of documents in wait queue: "+queueStatus.get("waitQueueSize")+"</p>");
        workQueueSize = new Html("<p>Number of documents in work queue: "+queueStatus.get("workQueueSize")+"</p>");
        JsonNode waitIDs = queueStatus.get("waitDocumentIds");
        String IDsForWaitQueue = this.getIDs(waitIDs);
        JsonNode workIDs = queueStatus.get("workDocumentIds");
        String IDsForWorkQueue = this.getIDs(workIDs);
        waitQueueIDs = new Html("<p>Wait queue documents ID's: "+ IDsForWaitQueue +"</p>");
        workQueueIDs = new Html("<p>Wait queue documents ID's: "+ IDsForWorkQueue +"</p>");

        Button backToWaitQueue = new Button("Back to wait queue");
        backToWaitQueue.addClickListener(e-> {
            infoManager.getData("admin/return");
            this.update();
        });
        Button deleteWaitQueue = new Button("Delete wait queue");
        deleteWaitQueue.addClickListener(e-> {
            infoManager.getData("admin/remove-queue");
            this.update();
        });
        HorizontalLayout buttonLayout = new HorizontalLayout(backToWaitQueue,deleteWaitQueue);
        buttonLayout.setSpacing(true);

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

        add(waitQueueSize,workQueueSize,waitQueueIDs,workQueueIDs,buttonLayout,application,applicationName,applicationDesc,buildName,buildVersion,statusApp,statusRedis,statusMongo);
    }

    private void update(){
        String queueJson = infoManager.getData("admin/status");
        try{
            queueStatus = objectMapper.readTree(queueJson);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        waitQueueSize.getElement().setText(new Html("<p>Number of documents in wait queue: "+queueStatus.get("waitQueueSize")+"</p>").getInnerHtml());
        workQueueSize.getElement().setText(new Html("<p>Number of documents in work queue: "+queueStatus.get("workQueueSize")+"</p>").getInnerHtml());
        JsonNode waitIDs = queueStatus.get("waitDocumentIds");
        String IDsForWaitQueue = this.getIDs(waitIDs);
        JsonNode workIDs = queueStatus.get("workDocumentIds");
        String IDsForWorkQueue = this.getIDs(workIDs);
        waitQueueIDs.getElement().setText(new Html("<p>Wait queue documents ID's: "+ IDsForWaitQueue +"</p>").getInnerHtml());
        workQueueIDs.getElement().setText(new Html("<p>Work queue documents ID's: "+ IDsForWorkQueue +"</p>").getInnerHtml());
    }

    private String getIDs(JsonNode IDS){
        ArrayList<String> IDsForQueue = new ArrayList<>();
        if (IDS.isArray()) {
            for (final JsonNode ID: IDS) {
                IDsForQueue.add(ID.toString().substring(1,ID.toString().length()-1));
            }
        }
        return StringUtils.join(IDsForQueue,",");
    }
}
