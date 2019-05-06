package si.slotex.nlp.gui.pages.learn;

import com.vaadin.flow.router.Route;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;

@Route(value = "corpora", layout = SlotexMainLayout.class)
public class LearnCorpora extends ContentView {

    public LearnCorpora(){
        add("LearnCorpora");
    }
}
