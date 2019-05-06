package si.slotex.nlp.gui;

import com.github.appreciated.app.layout.behaviour.Behaviour;
import com.github.appreciated.app.layout.builder.AppLayoutBuilder;
import com.github.appreciated.app.layout.component.menu.left.items.LeftNavigationItem;
import com.github.appreciated.app.layout.component.menu.left.builder.LeftAppMenuBuilder;
import com.github.appreciated.app.layout.component.menu.left.builder.LeftSubMenuBuilder;
import com.github.appreciated.app.layout.router.AppLayoutRouterLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import si.slotex.nlp.gui.pages.admin.AdminInProcessing;
import si.slotex.nlp.gui.pages.admin.AdminMarkedEntities;
import si.slotex.nlp.gui.pages.admin.AdminProcessedDocuments;
import si.slotex.nlp.gui.pages.admin.AdminInfo;
import si.slotex.nlp.gui.pages.learn.LearnCorpora;
import si.slotex.nlp.gui.pages.learn.LearnManage;
import si.slotex.nlp.gui.pages.learn.LearnModels;

@Push
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
public class SlotexMainLayout extends AppLayoutRouterLayout {

    public SlotexMainLayout() {
        init(AppLayoutBuilder
                .get(Behaviour.LEFT_RESPONSIVE)
                .withTitle("SloTex")
                .withAppMenu(LeftAppMenuBuilder
                        .get()
                        .add(LeftSubMenuBuilder.get("Administration",VaadinIcon.OPTIONS.create())
                                .add(new LeftNavigationItem("Processed documents",VaadinIcon.RECORDS.create(), AdminProcessedDocuments.class))
                                    .add(new LeftNavigationItem("Marked entities",VaadinIcon.BOOKMARK_O.create(), AdminMarkedEntities.class))
                                    .add(new LeftNavigationItem("In processing",VaadinIcon.FILE_PROCESS.create(), AdminInProcessing.class))
                                    .add(new LeftNavigationItem("Informations",VaadinIcon.INFO_CIRCLE_O.create(), AdminInfo.class))
                                    .build())
                        .add(LeftSubMenuBuilder.get("Learn",VaadinIcon.COG_O.create())
                                .add(new LeftNavigationItem("Manage",VaadinIcon.EDIT.create(), LearnManage.class))
                                .add(new LeftNavigationItem("Models",VaadinIcon.PACKAGE.create(), LearnModels.class))
                                .add(new LeftNavigationItem("Corpora",VaadinIcon.OPEN_BOOK.create(), LearnCorpora.class))
                                .build())
                        .build())
                .build());
    }
}