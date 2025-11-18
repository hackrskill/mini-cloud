package com.minicloud.repository;
import com.minicloud.model.AppUser;
import com.minicloud.model.QueueMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
@Repository
public interface QueueMessageRepository extends JpaRepository<QueueMessage, Long> {
    List<QueueMessage> findByOwnerAndQueueName(AppUser owner, String queueName);
    Optional<QueueMessage> findFirstByOwnerAndQueueNameAndStatusOrderByPriorityDescCreatedAtAsc(AppUser owner, String queueName, String status);
    Optional<QueueMessage> findByIdAndOwner(Long id, AppUser owner);
}
