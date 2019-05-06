package si.slotex.nlp.gui.pages.admin;

import com.vaadin.flow.router.Route;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;

@Route(value = "inProcessing", layout = SlotexMainLayout.class)
public class AdminInProcessing extends ContentView {

    public AdminInProcessing(){
        add("AdminInProcessing");
    }
}
