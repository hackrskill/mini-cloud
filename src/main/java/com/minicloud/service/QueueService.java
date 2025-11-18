package com.minicloud.service;
import com.minicloud.model.AppUser;
import com.minicloud.model.QueueMessage;
import com.minicloud.repository.QueueMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class QueueService {
    private final QueueMessageRepository repo;
    public QueueService(QueueMessageRepository repo) { this.repo = repo; }

    public QueueMessage sendMessage(AppUser owner, String queueName, String body, Integer priority) {
        QueueMessage m = new QueueMessage();
        m.setOwner(owner);
        m.setQueueName(queueName);
        m.setMessageBody(body);
        m.setPriority(priority == null ? 0 : priority);
        m.setStatus("PENDING");
        return repo.save(m);
    }

    @Transactional
    public Optional<QueueMessage> receiveMessage(AppUser owner, String queueName) {
        Optional<QueueMessage> opt = repo.findFirstByOwnerAndQueueNameAndStatusOrderByPriorityDescCreatedAtAsc(owner, queueName, "PENDING");
        opt.ifPresent(m -> {
            m.setStatus("PROCESSING");
            m.setProcessingStartedAt(LocalDateTime.now());
            repo.save(m);
        });
        return opt;
    }

    @Transactional
    public void acknowledgeMessage(AppUser owner, Long id, boolean success) {
        QueueMessage m = repo.findByIdAndOwner(id, owner).orElseThrow();
        if (success) {
            m.setStatus("COMPLETED");
            m.setCompletedAt(LocalDateTime.now());
        } else {
            m.setRetryCount(m.getRetryCount()+1);
            if (m.getRetryCount()>=m.getMaxRetries())
                m.setStatus("DEAD_LETTER");
            else
                m.setStatus("PENDING");
        }
        repo.save(m);
    }

    public List<QueueMessage> listMessages(AppUser owner, String queueName) {
        return repo.findByOwnerAndQueueName(owner, queueName);
    }
}
