package si.slotex.nlp.gui.pages.admin;

import com.vaadin.flow.router.Route;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;

@Route(value = "markedEntities", layout = SlotexMainLayout.class)
public class AdminMarkedEntities extends ContentView {

    public AdminMarkedEntities(){
        add("AdminMarkedEntities");
    }
}
