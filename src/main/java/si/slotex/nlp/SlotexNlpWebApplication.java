package si.slotex.nlp;

import com.vaadin.flow.spring.annotation.EnableVaadin;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableVaadin("si.slotex.nlp")
@SpringBootApplication
public class SlotexNlpWebApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlotexNlpWebApplication.class, args);
	}

}
