package ua.bevza.test_assignment;

import java.io.File;
import java.io.IOException;
import java.util.List;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class ResultWritingUtil {
    private static final String FILE_PATH = "parsed_products\\products.json";

    public static void saveToFileAsJson(List<Product> products) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            ObjectWriter writer = mapper.writer(new DefaultPrettyPrinter());
            writer.writeValue(new File(FILE_PATH), products);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
