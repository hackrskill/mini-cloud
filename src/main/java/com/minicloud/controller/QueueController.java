package com.minicloud.controller;
import com.minicloud.model.AppUser;
import com.minicloud.model.QueueMessage;
import com.minicloud.service.AuthService;
import com.minicloud.service.QueueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/queue")
public class QueueController {
    private final QueueService queueService;
    private final AuthService authService;

    public QueueController(QueueService queueService, AuthService authService) {
        this.queueService = queueService;
        this.authService = authService;
    }

    @PostMapping("/send")
    public ResponseEntity<QueueMessage> send(
            @RequestHeader("X-Auth-Token") String token,
            @RequestParam String queue,
            @RequestBody(required = false) String payload) {
        if (payload == null) {
            payload = "";
        }
        AppUser user = authService.requireUser(token);
        return ResponseEntity.ok(queueService.sendMessage(user, queue, payload, null));
    }

    @GetMapping("/messages")
    public ResponseEntity<List<QueueMessage>> getMessages(@RequestHeader("X-Auth-Token") String token,
                                                          @RequestParam String queue) {
        AppUser user = authService.requireUser(token);
        return ResponseEntity.ok(queueService.listMessages(user, queue));
    }

    @GetMapping("/receive")
    public ResponseEntity<?> receive(@RequestHeader("X-Auth-Token") String token,
                                     @RequestParam String queueName) {
        AppUser user = authService.requireUser(token);
        return ResponseEntity.of(queueService.receiveMessage(user, queueName));
    }

    @PostMapping("/ack/{id}")
    public ResponseEntity<Void> ack(@RequestHeader("X-Auth-Token") String token,
                                    @PathVariable Long id,
                                    @RequestParam boolean success) {
        AppUser user = authService.requireUser(token);
        queueService.acknowledgeMessage(user, id, success);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public List<QueueMessage> list(@RequestHeader("X-Auth-Token") String token,
                                   @RequestParam String queueName) {
        AppUser user = authService.requireUser(token);
        return queueService.listMessages(user, queueName);
    }
}