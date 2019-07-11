package si.slotex.nlp.gui.pages.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.apache.commons.lang3.StringUtils;
import si.slotex.nlp.entity.Entity;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;
import si.slotex.nlp.gui.managers.Manager;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Route(value = "markedEntities", layout = SlotexMainLayout.class)
public class AdminMarkedEntities extends ContentView {

    private JsonNode entitiesNode;
    private Dialog entitiesDetails;
    private final Manager infoManager;
    private ObjectMapper objectMapper;

    private TextField entityId;
    private TextField wordForRecognition;
    private TextField entityType;
    private Details documentIds;

    public AdminMarkedEntities(Manager infoManager) {
        this.infoManager = infoManager;
    }

    @PostConstruct
    protected void CreateMainContent() {
        VerticalLayout entities = new VerticalLayout();
        entitiesDetails = new Dialog();
        objectMapper = new ObjectMapper();
        setupDetails();
        Grid<Entity> entityGrid = new Grid<>(Entity.class);
        entityGrid.setHeightByRows(true);
        entityGrid.removeColumnByKey("documentIds");
        entityGrid.setColumns("id","word","type");
        List<Entity> entityList = getEntities();
        entityGrid.setItems(entityList);
        entityGrid.addItemClickListener(entityItemClickEvent -> openDetails(entityItemClickEvent.getItem()));
        entities.add(entityGrid);
        add(entities);
    }

    private List<Entity> getEntities(){
        List<Entity> entities = new ArrayList<>();
        String entityData = infoManager.getData("/entities");
        try {
            entitiesNode = objectMapper.readTree(entityData);
        }catch (IOException e){
            e.printStackTrace();
        }
        if (entitiesNode.isArray()){
            for (final  JsonNode entity:entitiesNode) {
                try {
                    entities.add(objectMapper.treeToValue(entity, Entity.class));
                }
                catch (JsonProcessingException e){
                    e.printStackTrace();
                }
            }
        }
        return entities;
    }

    private void setupDetails(){
        entitiesDetails.setWidth("40vw");
        entitiesDetails.setHeight("60vh");
        VerticalLayout allDetails = new VerticalLayout();
        HorizontalLayout wrap = new HorizontalLayout();
        VerticalLayout info = new VerticalLayout();
        entityId = new TextField();
        entityId.setLabel("Entity ID:");
        entityId.setEnabled(false);
        wordForRecognition = new TextField();
        wordForRecognition.setLabel("Word for recognition:");
        wordForRecognition.setEnabled(false);
        entityType = new TextField();
        entityType.setLabel("Entity type:");
        entityType.setEnabled(false);
        documentIds = new Details(new Span("Contained by documents with IDs:"),new Span(""));
        documentIds.setOpened(true);
        Button closeBtn = new Button("close");
        closeBtn.addClickListener(buttonClickEvent -> entitiesDetails.close());
        info.add(entityId,wordForRecognition,entityType);
        wrap.add(info,documentIds);
        allDetails.add(new Html("<h2>Detailed review</h2>"),wrap,closeBtn);
        allDetails.setHorizontalComponentAlignment(Alignment.END, closeBtn);
        entitiesDetails.add(allDetails);
    }

    private void openDetails(Entity entity){
        entityId.setValue(entity.getId().toString());
        wordForRecognition.setValue(entity.getWord());
        entityType.setValue(entity.getType());
        documentIds.setContent(new Html("<p>"+StringUtils.join(entity.getDocumentIds(),", ")+"</p>"));
        entitiesDetails.open();
    }
}
