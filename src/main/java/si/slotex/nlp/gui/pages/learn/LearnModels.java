package si.slotex.nlp.gui.pages.learn;

import com.vaadin.flow.router.Route;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;

@Route(value = "models", layout = SlotexMainLayout.class)
public class LearnModels extends ContentView {

    public LearnModels(){
        add("LearnModels");
    }
}
