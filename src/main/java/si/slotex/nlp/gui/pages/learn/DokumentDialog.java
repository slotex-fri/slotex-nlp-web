package si.slotex.nlp.gui.pages.learn;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;

public  class DokumentDialog extends Dialog {
    private LearnManage.Dokument dokument;

    public LearnManage.Dokument getDokument(){return dokument;};
    public void setDokument(LearnManage.Dokument dokument){this.dokument=dokument;};



    public DokumentDialog() {

        super();

    }
    public void importDocument(LearnManage.Dokument dokument){
        this.dokument=dokument;
        this.setCloseOnEsc(false);
        this.setCloseOnOutsideClick(false);

        Div content = new Div();
        content.setText(dokument.getNaslovDokumenta());
        this.add(content);

        Button endButton = new Button("Končano", event -> {

            this.close();
            this.removeAll();
        });
        this.add(endButton);
    }
}