package si.slotex.nlp.gui.pages.learn;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.NativeButton;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.data.renderer.NativeButtonRenderer;
import com.vaadin.flow.router.Route;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Route(value = "manage", layout = SlotexMainLayout.class)
public class LearnManage extends ContentView {

    public LearnManage() {


        add("Označeni dokumenti, ki so na voljo:");
        Dokument dokument1 = new Dokument( 2222, "", "Nobelovi nagrajenci", "slo", 3);
        Dokument dokument2 = new Dokument(2224, "", "Samurai", "slo", 1);

        List<Dokument> dokumentList = new ArrayList<>();// getItems();
        dokumentList.add(dokument1);
        dokumentList.add(dokument2);




        DokumentDialog dialog = new DokumentDialog();
        //init grid
        Grid<Dokument> grid = new Grid<>();
        grid.setItems(dokumentList);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.addItemClickListener(event ->{
            dialog.importDocument(event.getItem());
            dialog.open();
        }) ;
        grid.addColumn(Dokument::getIDDokumenta).setHeader("ID Dokumenta");
        grid.addColumn(Dokument::getModeliZaTrening).setHeader("Modeli za trening");
        grid.addColumn(Dokument::getNaslovDokumenta).setHeader("Naslov dokumenta");
        grid.addColumn(Dokument::getJezikVsebine).setHeader("Jezik vsebine");
        grid.addColumn(Dokument::getSteviloStavkovVBesedilu).setHeader("Število stavkov v besedilu");
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        add(grid);
    }


    public static class Dokument implements Cloneable{

        private int IDDokumenta;
        private String modeliZaTrening;
        private String naslovDokumenta;
        private String jezikVsebine;
        private int steviloStavkovVBesedilu;



        public int getIDDokumenta(){
            return IDDokumenta;
        }
        public void setIDDokumenta(int IDDokumenta){
            this.IDDokumenta = IDDokumenta;
        }
        public String getModeliZaTrening(){
            return modeliZaTrening;
        }
        public void setModeliZaTrening(String modeliZaTrening){
            this.modeliZaTrening = modeliZaTrening;
        }
        public String getNaslovDokumenta(){
            return naslovDokumenta;
        }
        public void setNaslovDokumenta(String naslovDokumenta){
            this.naslovDokumenta = naslovDokumenta;
        }
        public String getJezikVsebine(){
            return jezikVsebine;
        }
        public void setJezikVsebine(String jezikVsebine){
            this.jezikVsebine = jezikVsebine;
        }
        public int getSteviloStavkovVBesedilu(){
            return steviloStavkovVBesedilu;
        }
        public void setSteviloStavkovVBesedilu(int steviloStavkovVBesedilu){
            this.steviloStavkovVBesedilu = steviloStavkovVBesedilu;
        }
        public Dokument(int IDDokumenta, String modeliZaTrening, String naslovDokumenta, String jezikVsebine, int steviloStavkovVBesedilu){
            super();
            this.IDDokumenta = IDDokumenta;
            this.modeliZaTrening = modeliZaTrening;
            this.naslovDokumenta = naslovDokumenta;
            this.jezikVsebine = jezikVsebine;
            this.steviloStavkovVBesedilu = steviloStavkovVBesedilu;

        }
    }
}
