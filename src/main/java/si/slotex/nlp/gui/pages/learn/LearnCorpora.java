package si.slotex.nlp.gui.pages.learn;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logging.Logger;
import si.slotex.nlp.entity.CorpusSentenceDiff;
import si.slotex.nlp.entity.CorpusTokenDiff;
import si.slotex.nlp.entity.enums.DiffStatus;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;
import si.slotex.nlp.gui.managers.Manager;

import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Route(value = "corpora", layout = SlotexMainLayout.class)
public class LearnCorpora extends ContentView {

    private Logger logger = Logger.getLogger(LearnCorpora.class);

    private static VerticalLayout content;
    private static Select<String> collection;
    private static TextField fromLine;
    private static TextField toLine;
    private static Upload uploadFile;
    private static Button uploadBtn;
    private static MemoryBuffer buffer;
    private static Button getSentences;
    private static Button train;
    private static Grid<CorpusSentenceDiff> sentences;
    private static CorpusSentenceDiff activeSentence;
    private static List<CorpusSentenceDiff> list;
    private static Grid<CorpusTokenDiff> tokens;
    private static List<CorpusTokenDiff> extractedTokens;
    private static Dialog details;
    private static TextArea sentenceData;
    private final Manager corporaManager;

    private static File file;
    private static InputStream inputStream;

    public LearnCorpora(Manager corporaManager){
        this.corporaManager = corporaManager;
    }

    @PostConstruct
    protected void createMainContent(){
        content = new VerticalLayout();
        setupGrid();
        setupSentenceDetails();
        content.add(getCollectionsInfo(),getParametersInfo(),sentences);
        add(content);

    }

    //setup collection choosing layout
    private HorizontalLayout getCollectionsInfo(){
        HorizontalLayout collections = new HorizontalLayout();
        collections.setSpacing(true);
        Html description = new Html("<p>Select text collection:</p>");
        collection = new Select<>("Person","Location","Organization");
        collection.setValue("Person");
        collections.add(description,collection);
        return collections;
    }

    //setup parameters layout
    private HorizontalLayout getParametersInfo(){
        HorizontalLayout parameters = new HorizontalLayout();
        parameters.setSpacing(true);
        VerticalLayout lines = new VerticalLayout();
        lines.setMaxWidth("30%");
        VerticalLayout fileUpload = new VerticalLayout();
        fileUpload.setMaxWidth("40%");
        VerticalLayout collectionDownload = new VerticalLayout();
        collectionDownload.setMaxWidth("30%");
        HorizontalLayout linesInfo = new HorizontalLayout();

        fromLine = new TextField();
        fromLine.setMaxWidth("10%");

        toLine = new TextField();
        toLine.setMaxWidth("10%");

        getSentences = new Button("Get");
        getSentences.addClickListener(click -> getGrid());

        train = new Button("Train");
        train.addClickListener(click -> trainModel());
        train.setEnabled(false);

        linesInfo.add(fromLine,toLine,getSentences,train);
        lines.add(new Html("<p>Select lines from corpus between: </p>"),linesInfo);

        uploadBtn = new Button("Upload");
        uploadBtn.addClickListener(buttonClickEvent -> uploadCollection());
        uploadBtn.setEnabled(false);
        buffer = new MemoryBuffer();
        uploadFile = new Upload(buffer);
        uploadFile.addFinishedListener(event -> {
            try{
                file = new File("./slo-ner-"+collection.getValue().toLowerCase()+".train");
                logger.info(file.getAbsolutePath());
                inputStream = buffer.getInputStream();
                logger.info(inputStream.available());
                FileUtils.copyInputStreamToFile(inputStream,file);
            }catch (IOException e){
                e.printStackTrace();
            }
            uploadBtn.setEnabled(true);
        });
        uploadFile.getElement().addEventListener("file-remove", domEvent -> uploadBtn.setEnabled(false));
        HorizontalLayout uploads = new HorizontalLayout(uploadFile,uploadBtn);
        fileUpload.add(new Html("<p>Upload new Collection:</p>"),uploads);
        Button download = new Button("Download");
        download.addClickListener(buttonClickEvent -> corporaManager.downloadCorpus(collection.getValue().toLowerCase()));
        collectionDownload.add(new Html("<p>Download current text collection:</p>"),download);

        parameters.add(lines,fileUpload,collectionDownload);

        return parameters;
    }

    //executes upload of corpus
    private void uploadCollection(){
        Dialog uploadWarning = new Dialog();
        VerticalLayout layout = new VerticalLayout();
        HorizontalLayout Buttons = new HorizontalLayout();
        Button up = new Button("upload");
        up.addClickListener(buttonClickEvent -> {
                corporaManager.uploadCorpus(file,collection.getValue().toLowerCase());
                try {
                    Files.deleteIfExists(Paths.get(file.getAbsolutePath()));
                }catch(IOException e){
                    e.printStackTrace();
                }
                uploadWarning.close();
        });
        Button cancel = new Button("cancel");
        cancel.addClickListener(buttonClickEvent -> uploadWarning.close());
        Buttons.add(up,cancel);
        layout.add(new Html("<p>Uploading new collection will erase all previous data!</p>"),Buttons);
        uploadWarning.add(layout);
        uploadWarning.open();
    }

    // Sets grid items to correct sentences
    private void getGrid() {
        logger.info("getting Grid...");
        if(validateRange()){
            list = corporaManager.getAllInRange(collection.getValue(),Integer.parseInt(fromLine.getValue())-1,Integer.parseInt(toLine.getValue())+1);
            if(!list.isEmpty()) {
                sentences.setItems(list);
                collection.setEnabled(false);
                fromLine.setEnabled(false);
                toLine.setEnabled(false);
                getSentences.setEnabled(false);
                train.setEnabled(true);
            }else{
                Notification msg = new Notification("empty collection!",3000);
                msg.setPosition(Notification.Position.TOP_CENTER);
                msg.open();
            }
        }else{
            Notification wrongRange = new Notification("Wrong range or line parameters! Maximum range is 20, lines start at 1.",5000);
            wrongRange.setPosition(Notification.Position.TOP_CENTER);
            wrongRange.open();
        }
    }

    //checks if line parameters are valid
    private boolean validateRange(){
        int from = Integer.parseInt(fromLine.getValue());
        int to = Integer.parseInt(toLine.getValue());
        if (from > 0 && to > 0 && to-from < 20){
         return true;
        }
        return false;
    }

    //configures grid setup
    private void setupGrid(){
        sentences = new Grid<>(CorpusSentenceDiff.class);
        sentences.setColumns("corpusLine", "sentence");
        sentences.getColumnByKey("corpusLine").setWidth("15%");
        sentences.getColumnByKey("sentence").setWidth("65%");
        sentences.addColumn(new ComponentRenderer<>(CorpusSentenceDiff -> {
            Checkbox diff = new Checkbox();
            diff.addValueChangeListener(checkboxBooleanComponentValueChangeEvent -> CorpusSentenceDiff.setDiff(checkboxBooleanComponentValueChangeEvent.getValue()));
            if (CorpusSentenceDiff.getDiff()){
                diff.setValue(true);
                return diff;
            }else{
                diff.setValue(false);
                return diff;
            }
        })).setHeader("diff");
        sentences.addColumn(new ComponentRenderer<>(CorpusSentenceDiff -> {
            Checkbox edit = new Checkbox();
            edit.addValueChangeListener(checkboxBooleanComponentValueChangeEvent -> CorpusSentenceDiff.setEdit(checkboxBooleanComponentValueChangeEvent.getValue()));
            if (CorpusSentenceDiff.getEdit()){
                edit.setValue(true);
                return edit;
            }else{
                edit.setValue(false);
                return edit;
            }
        })).setHeader("edit");
        sentences.addItemClickListener(corpusSentenceDiffItemClickEvent -> openDetails(corpusSentenceDiffItemClickEvent.getItem()));
    }

    //executes saving of corrected sentences and trains model with new corpus
    private void trainModel(){
        logger.log(Logger.Level.INFO,"training Corpus...");
        //corporaManager.saveSentences(collection.getValue().toLowerCase(),changedList);// doesn't save properly
        corporaManager.getData("/retrain/"+collection.getValue().toLowerCase());
        sentences.setItems();
        collection.setEnabled(true);
        fromLine.setValue("");
        fromLine.setEnabled(true);
        toLine.setValue("");
        toLine.setEnabled(true);
        getSentences.setEnabled(true);
        train.setEnabled(false);
        Notification msg = new Notification("Tagging of corpus data has been successful!",3000);
        msg.setPosition(Notification.Position.TOP_CENTER);
        msg.open();
    }

    //opens specific sentence details that can be modified and saved
    private void openDetails(CorpusSentenceDiff sentence){
        logger.log(Logger.Level.INFO,"Opening sentence details...");
        sentenceData.setValue(sentence.getSentence());
        activeSentence = sentence;
        extractedTokens = extractTokens(sentence);
        LearnCorpora.tokens.setItems(extractedTokens);
        details.open();

    }

    //sets up display of sentences
    private void setupSentenceDetails(){
        logger.info("started details setup...");
        details = new Dialog();
        details.setHeight("80vh");
        details.setWidth("80vw");
        VerticalLayout AllDetails = new VerticalLayout();
        HorizontalLayout SentenceInfo = new HorizontalLayout();
        sentenceData = new TextArea();
        sentenceData.setEnabled(false);
        sentenceData.setWidth("70vw");
        SentenceInfo.add(new Html("<p>Sentence: </p>"), sentenceData);
        setupTokensGrid();
        Button closeBtn = new Button("close");
        Button saveBtn = new Button("save changes");
        saveBtn.addClickListener(buttonClickEvent -> {
            saveChanges();
            Notification saveInfo = new Notification("Changes saved!",3000);
            saveInfo.setPosition(Notification.Position.TOP_CENTER);
            details.close();
            saveInfo.open();
        });
        closeBtn.addClickListener(click -> details.close());
        AllDetails.add(new Html("<h2>Correct tagged sentences</h2>"),SentenceInfo, tokens,saveBtn,closeBtn);
        details.add(AllDetails);
    }

    //sets up display of tokens in grid
    private void setupTokensGrid(){
        tokens = new Grid<>(CorpusTokenDiff.class);
        tokens.setColumns("token");
        tokens.addColumn(new ComponentRenderer<>(CorpusTokenDiff -> {
            Select<DiffStatus> status = new Select<>(DiffStatus.values());
            status.addValueChangeListener(selectDiffStatusComponentValueChangeEvent -> CorpusTokenDiff.setStatus(selectDiffStatusComponentValueChangeEvent.getValue()));
            status.setValue(CorpusTokenDiff.getStatus());
            return status;
        })).setHeader("status");
        tokens.addColumn(new ComponentRenderer<>(CorpusTokenDiff -> {
            Checkbox modelCheck = new Checkbox();
            modelCheck.addValueChangeListener(checkboxBooleanComponentValueChangeEvent -> CorpusTokenDiff.setModelCheck(checkboxBooleanComponentValueChangeEvent.getValue()));
            if (CorpusTokenDiff.getModelCheck()!=null && CorpusTokenDiff.getModelCheck()){
                modelCheck.setValue(true);
            }else{
                modelCheck.setValue(false);
            }
            return modelCheck;
        })).setHeader("entity");
    }

    //extracts tokens from selected sentence
    private List<CorpusTokenDiff> extractTokens(CorpusSentenceDiff pojoForEditParam)
    {
        String[] sentenceTokens = pojoForEditParam.getSentence().split(" ");
        List<CorpusTokenDiff> tokenDiffs = new ArrayList<>();
        tokenDiffs.add(new CorpusTokenDiff());
        for (String sentenceToken : sentenceTokens)
        {
            if (sentenceToken.contains("STAT><END") || sentenceToken.contains("DICT><END"))
            {
                CorpusTokenDiff tempTokenDiff = tokenDiffs.get(tokenDiffs.size() - 1);
                tempTokenDiff.setStatus(DiffStatus.PENDING);
            }

            else if (sentenceToken.contains("<END>"))
            {
                CorpusTokenDiff tempTokenDiff = tokenDiffs.get(tokenDiffs.size() - 1);
                tempTokenDiff.setModelCheck(true);
            }
            else if (sentenceToken.contains("<START"))
            {
                // ignore - don't add to sentences
            }
            else
            {
                CorpusTokenDiff tempTokenDiff;
                if (tokenDiffs.get(0).getToken() == null)
                {
                    tempTokenDiff = tokenDiffs.get(0);
                    tempTokenDiff.setToken(sentenceToken);
                }
                else
                {
                    tempTokenDiff = new CorpusTokenDiff();
                    tempTokenDiff.setToken(sentenceToken);
                    tokenDiffs.add(tempTokenDiff);
                }
            }
        }
        return tokenDiffs;
    }

    //saves changes to tokens and applies them to selected sentence
    private void saveChanges(){
        List<String> sentenceWords = new ArrayList<>();
        for (CorpusTokenDiff corpusTokenDiff: extractedTokens) {
            if (corpusTokenDiff.getModelCheck() != null && corpusTokenDiff.getModelCheck())
            {
                sentenceWords.add("<START:" + activeSentence.getModel() + "> " + corpusTokenDiff.getToken() + " <END>");
            }
            else
            {
                sentenceWords.add(corpusTokenDiff.getToken());
            }
        }
        list.get(list.indexOf(activeSentence)).setSentence(StringUtils.join(sentenceWords, " "));
        list.get(list.indexOf(activeSentence)).setEdit(true);
        corporaManager.saveSentence(activeSentence);
        sentences.setItems(list);
    }
}
