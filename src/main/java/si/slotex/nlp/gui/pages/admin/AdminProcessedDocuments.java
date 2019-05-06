package si.slotex.nlp.gui.pages.admin;

import com.vaadin.flow.router.Route;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;

@Route(value = "processedDocuments", layout = SlotexMainLayout.class)
public class AdminProcessedDocuments extends ContentView {

    public AdminProcessedDocuments() {
        add("AdminProcessedDocuments");
    }
}
