package si.slotex.nlp.gui.pages.learn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.jboss.logging.Logger;
import si.slotex.nlp.entity.ModelTrainInfo;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;
import si.slotex.nlp.gui.managers.Manager;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Route(value = "models", layout = SlotexMainLayout.class)
public class LearnModels extends ContentView {
    private Logger logger = Logger.getLogger(LearnModels.class);

    private JsonNode DocumentsNode;
    private Dialog documentDetails;
    private Dialog sentenceDetails;
    private final Manager infoManager;
    private ObjectMapper objectMapper;

    private TextField timeStamp;
    private TextField modelName;
    private Grid<String> modelGrid;




    public LearnModels(Manager infoManager) {this.infoManager = infoManager; }

    @PostConstruct
    protected void CreateMainContent(){
        objectMapper = new ObjectMapper();
        String docs = infoManager.getData("/models");
        try{
            DocumentsNode = objectMapper.readTree(docs);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        List<ModelTrainInfo> models = manageModelTags(DocumentsNode);
        Grid<ModelTrainInfo> modelsUsed = new Grid<>(ModelTrainInfo.class);
        documentDetails = setupDetails();
        modelsUsed.removeColumnByKey("trainDataFile");
        modelsUsed.removeColumnByKey("additionalTrainData");
        modelsUsed.setColumns("versionName","timeStamp","modelName");
        modelsUsed.setHeightByRows(true);
        modelsUsed.getColumnByKey("versionName").setWidth("40%");
        modelsUsed.getColumnByKey("timeStamp").setWidth("20%");
        modelsUsed.getColumnByKey("modelName").setWidth("40%");
        modelsUsed.addItemClickListener(eventTagItemClickEvent -> openDetails(eventTagItemClickEvent.getItem()));
        modelsUsed.setItems(models);
        add(modelsUsed);
    }
    private List<ModelTrainInfo> manageModelTags(JsonNode ModelTrainInfo){
        ArrayList<ModelTrainInfo> models = new ArrayList<>();

        if (ModelTrainInfo.isArray()){
            for (final JsonNode document: ModelTrainInfo) {
                try {
                    models.add(objectMapper.treeToValue(document, ModelTrainInfo.class));
                }
                catch (JsonProcessingException e){

                    e.printStackTrace();
                }
            }
        }
        return models;
    }
    private Dialog setupDetails(){
        logger.log(Logger.Level.INFO,"Setting up Details...");
        Dialog details = new Dialog();
        details.setWidth("90vw");
        details.setHeight("80vh");
        VerticalLayout allDetails = new VerticalLayout();
        allDetails.setId("all");
        HorizontalLayout dets = new HorizontalLayout();
        timeStamp = new TextField();
        timeStamp.setLabel("Timestamp of training:");
        timeStamp.setId("timestamp");
        timeStamp.setEnabled(false);
        modelName = new TextField();
        modelName.setLabel("Name of the model:");
        modelName.setId("modelName");
        modelName.setEnabled(false);
        dets.add(timeStamp, modelName);

        modelGrid = new Grid<>(String.class);
        modelGrid.setId("modelGrid");
        modelGrid.setSelectionMode(Grid.SelectionMode.NONE);
        modelGrid.setHeightByRows(true);
        modelGrid.removeColumnByKey("bytes");
        modelGrid.removeColumnByKey("empty");



        Button closeBtn = new Button("close");
        closeBtn.setId("closeBtn");
        closeBtn.addClickListener(click -> {details.close(); modelGrid.removeColumnByKey("Additional sentences:");});
        allDetails.add(new Html("<h2>Detailed review</h2>"),dets, modelGrid,closeBtn);
        allDetails.setHorizontalComponentAlignment(Alignment.END,closeBtn);
        details.add(allDetails);
        logger.log(Logger.Level.INFO,"Details setup!");
        return details;
    }

    private void openDetails(ModelTrainInfo document){
        logger.log(Logger.Level.INFO,"displaying document details...");
        timeStamp.setValue(document.getTimeStamp().toString());
        modelName.setValue(document.getModelName());
        modelGrid.setItems(document.getAdditionalTrainData());
        modelGrid.addColumn(e -> document.getAdditionalTrainData()).setHeader("Additional sentences:");
        documentDetails.open();
    }

}