package springbot.mybotg.service;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import springbot.mybotg.models.Exercise;
import springbot.mybotg.repository.ExerciseRepository;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DataImportService {

    @Autowired
    private ExerciseRepository exerciseRepository;

    public void importFromCsv(String filePath) throws IOException {
        List<Exercise> exercises = new ArrayList<>();

        ClassPathResource resource = new ClassPathResource(filePath);

        try (Reader reader = new InputStreamReader(
                resource.getInputStream(),
                StandardCharsets.UTF_8)) {

            CSVParser parser = CSVParser.parse(
                    reader,
                    CSVFormat.DEFAULT
                            .withFirstRecordAsHeader()
                            .withIgnoreHeaderCase()
                            .withTrim()
            );

            for (CSVRecord record : parser) {
                Exercise exercise = new Exercise();
                exercise.setMuscleGroup(record.get("muscle_group"));
                exercise.setExercise(record.get("exercise"));
                exercises.add(exercise);
            }
        }

        exerciseRepository.saveAll(exercises);
    }

    public List<Exercise> getTraining(String muscleGroup) {
        return exerciseRepository.getTraining(muscleGroup);
    }

    public Exercise findExerciseById(Long exerciseId) {
        return exerciseRepository.getId(exerciseId);
    }
}
