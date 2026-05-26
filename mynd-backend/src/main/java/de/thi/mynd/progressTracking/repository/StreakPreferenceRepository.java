package de.thi.mynd.progressTracking.repository;

import de.thi.mynd.common.entity.CreatorIdKey;
import de.thi.mynd.common.repository.MyndBaseCustomIdRepository;
import de.thi.mynd.progressTracking.entity.StreakPreference;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StreakPreferenceRepository extends MyndBaseCustomIdRepository<StreakPreference, CreatorIdKey> {

}
