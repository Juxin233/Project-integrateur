package fr.insa.projectIntegrateur.DatabaseService.utils;

import static org.junit.jupiter.api.Assertions.*;
import java.io.FileInputStream;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class PostgreUpdateTest {

	@Test
	void test() throws Exception {
		try (InputStream is = new FileInputStream("src/main/resources/update.json")) {
            PostgreUpdate.UpdateReport report =
                    PostgreUpdate.updateFromJson(is, 0);

            System.out.println(report);
        }
	}

}
