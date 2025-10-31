package springbot.mybotg.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import springbot.mybotg.models.Exercise;

import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

    @Query("SELECT e FROM Exercise e WHERE e.muscleGroup = :muscleGroup")
    List<Exercise> getTraining(@Param("muscleGroup") String muscleGroup);

    @Query("SELECT e FROM Exercise e WHERE e.Id = :Id")
    Exercise getId (@Param("Id") Long Id);

}
