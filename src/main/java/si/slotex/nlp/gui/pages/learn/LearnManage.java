package si.slotex.nlp.gui.pages.learn;

import com.vaadin.flow.router.Route;
import si.slotex.nlp.gui.ContentView;
import si.slotex.nlp.gui.SlotexMainLayout;

@Route(value = "manage", layout = SlotexMainLayout.class)
public class LearnManage extends ContentView {

    public LearnManage(){
        add("LearnManage");
    }

}
