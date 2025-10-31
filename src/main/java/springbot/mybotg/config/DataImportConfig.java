package springbot.mybotg.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Configuration;
import springbot.mybotg.service.DataImportService;


@Configuration
public class DataImportConfig {

    @Resource
    private DataImportService dataImportService;

//    @Value("${csv.import.file:chest.csv}")
//    private String csvFilePath;

    @PostConstruct
    public void init() {
        try {
            dataImportService.importFromCsv("training.csv");
            System.out.println("Данные успешно загружены из CSV");
        } catch (Exception e) {
            System.err.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }
}
