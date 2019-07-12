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
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;
import org.jboss.logging.Logger;
import si.slotex.nlp.entity.DocTag;
import si.slotex.nlp.entity.Sentence;
import si.slotex.nlp.entity.Token;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;
import si.slotex.nlp.gui.managers.Manager;


import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Route(value = "manage", layout = SlotexMainLayout.class)
public class LearnManage extends ContentView {
    private Logger logger = Logger.getLogger(LearnManage.class);

    private JsonNode DocumentsNode;
    private Dialog documentDetails;
    private Dialog sentenceDetails;
    private Dialog correctionDetails;
    private final Manager infoManager;
    private ObjectMapper objectMapper;
    private Token correctionToken;
    private TextField id;
    private TextField language;
    private TextField title;
    private TextField sentenceNum;
    private Grid<Sentence> sentenceGrid;
    private Grid<Token> tokenGrid;
    private TextField word;
    private TextField posTag;
    private TextField lemma;
    private TextField nerTag;


    public LearnManage(Manager infoManager) {
        this.infoManager = infoManager;
    }

    @PostConstruct
    protected void CreateMainContent(){

        Dialog setNumberOfDocuments = new Dialog();
        TextField inputNumber= new TextField();
        objectMapper = new ObjectMapper();
        inputNumber.setLabel("Number of Documents:");
        Button okBtn = new Button("OK");

        Button cancelBtn = new Button("CANCEL");
        cancelBtn.addClickListener(e->setNumberOfDocuments.close());
        HorizontalLayout buttons = new HorizontalLayout(okBtn,cancelBtn);
        buttons.setSpacing(true);
        VerticalLayout inputWithButtons = new VerticalLayout(inputNumber,buttons);
        setNumberOfDocuments.add(new Html("<h3>Process documents</h3>"),new Html("<p><small>Enter the number of documents to process from queue (-1 for all).</small></p>"),inputWithButtons);

        String docs = infoManager.getData("/docs");
        try{
            DocumentsNode = objectMapper.readTree(docs);
        }
        catch (IOException e){
            e.printStackTrace();
        }
        List<DocTag> documents = manageDocTags(DocumentsNode);
        Grid<DocTag> processedDocuments = new Grid<>(DocTag.class);
        documentDetails = setupDetails();
        processedDocuments.removeColumnByKey("languageProb");
        processedDocuments.removeColumnByKey("modelsToTrain");
        processedDocuments.removeColumnByKey("sentences");
        processedDocuments.setColumns("documentId","title","language","numOfSentences");
        processedDocuments.setHeightByRows(true);
        processedDocuments.getColumnByKey("documentId").setWidth("15%");
        processedDocuments.getColumnByKey("title").setWidth("50%");
        processedDocuments.getColumnByKey("language").setWidth("15%");
        processedDocuments.getColumnByKey("numOfSentences").setWidth("20%");
        processedDocuments.addItemClickListener(docTagItemClickEvent -> openDetails(docTagItemClickEvent.getItem()));
        processedDocuments.setItems(documents);
        add(processedDocuments);
    }

    private List<DocTag> manageDocTags(JsonNode DocTags){
        ArrayList<DocTag> documents = new ArrayList<>();
        if (DocTags.isArray()){
            for (final JsonNode document: DocTags) {
                try {
                    documents.add(objectMapper.treeToValue(document, DocTag.class));
                }
                catch (JsonProcessingException e){
                    e.printStackTrace();
                }
            }
        }
        return documents;
    }



    private Dialog setupDetails(){
        logger.log(Logger.Level.INFO,"Setting up Details...");
        Dialog details = new Dialog();
        setupSentenceDetails();
        details.setWidth("90vw");
        details.setHeight("80vh");
        VerticalLayout allDetails = new VerticalLayout();
        allDetails.setId("all");
        HorizontalLayout basicDetails = new HorizontalLayout();
        basicDetails.setId("basic");
        VerticalLayout idLanguageDetails = new VerticalLayout();
        idLanguageDetails.setId("idLanguage");
        id = new TextField();
        id.setLabel("DocumentID:");
        id.setId("id");
        id.setEnabled(false);
        language = new TextField();
        language.setLabel("Language:");
        language.setId("language");
        language.setEnabled(false);
        idLanguageDetails.add(id,language);
        VerticalLayout titleSentenceNumDetails = new VerticalLayout();
        titleSentenceNumDetails.setId("titleSentenceNum");
        title = new TextField();
        title.setLabel("Title:");
        title.setId("title");
        title.setEnabled(false);
        sentenceNum = new TextField();
        sentenceNum.setLabel("number of sentences:");
        sentenceNum.setId("sentenceNum");
        sentenceNum.setEnabled(false);
        titleSentenceNumDetails.add(title,sentenceNum);
        basicDetails.add(idLanguageDetails,titleSentenceNumDetails);
        sentenceGrid = new Grid<>(Sentence.class);
        sentenceGrid.setId("sentenceGrid");
        sentenceGrid.removeColumnByKey("tokens");
        sentenceGrid.setColumns("sentence","numberOfTokens");
        sentenceGrid.setSelectionMode(Grid.SelectionMode.NONE);
        sentenceGrid.setHeightByRows(true);
        sentenceGrid.setItemDetailsRenderer(TemplateRenderer.<Sentence> of(
                "<div style='padding: 10px; width: 100%; box-sizing: border-box;'>"
                        + "<div>[[item.sentence]]</div>"
                        + "</div>")
                .withProperty("sentence", Sentence::getSentence)
                .withEventHandler("handleClick", sentence ->
                        sentenceGrid.getDataProvider().refreshItem(sentence)));
        sentenceGrid.getColumnByKey("sentence").setWidth("60%");
        sentenceGrid.getColumnByKey("numberOfTokens").setWidth("20%");
        sentenceGrid.addColumn(new NativeButtonRenderer<>("Correct and learn", this::tokenDetails));
        Button closeBtn = new Button("close");
        closeBtn.setId("closeBtn");
        closeBtn.addClickListener(click -> details.close());
        allDetails.add(new Html("<h2>Detailed review</h2>"),basicDetails,sentenceGrid,closeBtn);
        allDetails.setHorizontalComponentAlignment(Alignment.END,closeBtn);
        details.add(allDetails);
        return details;
    }

    private void setupSentenceDetails(){
        sentenceDetails = new Dialog();
        sentenceDetails.setWidth("80vw");
        sentenceDetails.setHeight("75vh");
        setupCorrection();
        VerticalLayout tokenDetails = new VerticalLayout();
        tokenGrid = new Grid<>(Token.class);
        tokenGrid.setColumns("word","posTag","lemma","nerTag");
        tokenGrid.setSelectionMode(Grid.SelectionMode.SINGLE);
        tokenGrid.setHeightByRows(true);
        tokenGrid.addItemClickListener(e -> correctItem(tokenGrid.asSingleSelect().getValue()));

        Button closeBtn = new Button("close");
        closeBtn.addClickListener(click -> sentenceDetails.close());
        tokenDetails.add(new Html("<h3>Tokens review</h3>"),tokenGrid,closeBtn);
        tokenDetails.setHorizontalComponentAlignment(Alignment.END,closeBtn);
        sentenceDetails.add(tokenDetails);
    }

    private void setupCorrection(){

        correctionDetails = new Dialog();
        correctionDetails.setHeight("60vh");
        correctionDetails.setWidth("40vh");
        VerticalLayout correctionLayout = new VerticalLayout();
        word = new TextField("Word:");
        word.setId("word");
        posTag = new TextField("Pos tag:");
        posTag.setId("posTag");
        lemma = new TextField("Lemma:");
        lemma.setId("lemma");
        nerTag = new TextField("Ner tag:");
        nerTag.setId("nerTag");
        Button closeBtn = new Button("close");
        closeBtn.addClickListener(click -> correctionDetails.close());
        Button saveBtn = new Button("save");
        saveBtn.addClickListener(click -> {
            saveCorrections(word.getValue(), posTag.getValue(), lemma.getValue());
            correctionDetails.close();
            sentenceDetails.close();
        });
        correctionLayout.add(word, posTag, lemma, nerTag);
        correctionDetails.add(correctionLayout, closeBtn, saveBtn);
    }

    private void correctItem(Token token){
        logger.log(Logger.Level.INFO, "Opened item for correcting...");
        word.setValue(token.getWord());
        posTag.setValue(token.getPosTag());
        lemma.setValue(token.getLemma());
        nerTag.setValue("");
        //nerTag.setValue(token.getNerTag());
        nerTag.setEnabled(false);
        setCorrectionToken(token);
        correctionDetails.open();

    }
    public Token getCorrectionToken(){return correctionToken;}
    public void setCorrectionToken(Token token){this.correctionToken=token;}

    private void saveCorrections(String word, String posTag, String lemma){
        logger.log(Logger.Level.INFO, "Saving corrections...");
        Token token = getCorrectionToken();
        token.setWord(word);
        token.setPosTag(posTag);
        token.setLemma(lemma);

    }
    private void openDetails(DocTag document){
        logger.log(Logger.Level.INFO,"displaying document details...");
        id.setValue(document.getDocumentId());
        language.setValue(document.getLanguage());
        title.setValue(document.getTitle());
        sentenceNum.setValue(document.getNumOfSentences().toString());
        sentenceGrid.setItems(document.getSentences());
        documentDetails.open();
    }

    private void tokenDetails(Sentence sentence){
        logger.log(Logger.Level.INFO,"displaying sentence tokens...");
        tokenGrid.setItems(sentence.getTokens());
        sentenceDetails.open();
    }
}