package com.minicloud.controller;
import com.minicloud.model.AppUser;
import com.minicloud.model.ExecutionResult;
import com.minicloud.model.LambdaFunction;
import com.minicloud.service.AuthService;
import com.minicloud.service.LambdaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/lambda")
public class LambdaController {
    private final LambdaService lambdaService;
    private final AuthService authService;
    public LambdaController(LambdaService lambdaService, AuthService authService) {
        this.lambdaService = lambdaService;
        this.authService = authService;
    }

    @GetMapping("/functions")
    public ResponseEntity<List<LambdaFunction>> listFunctions(@RequestHeader("X-Auth-Token") String token) {
        AppUser user = authService.requireUser(token);
        return ResponseEntity.ok(lambdaService.getAllFunctions(user));
    }

    @PostMapping("/create")
    public ResponseEntity<LambdaFunction> create(@RequestHeader("X-Auth-Token") String token,
                                                 @RequestBody LambdaFunction function) {
        AppUser user = authService.requireUser(token);
        return ResponseEntity.ok(lambdaService.createFunction(user, function));
    }

    @PostMapping("/execute/{name}")
    public ResponseEntity<ExecutionResult> execute(@RequestHeader("X-Auth-Token") String token,
                                                   @PathVariable String name,
                                                   @RequestBody(required = false) Map<String,Object> input) throws Exception {
        AppUser user = authService.requireUser(token);
        CompletableFuture<ExecutionResult> fut = lambdaService.executeFunctionAsync(user, name, input);
        ExecutionResult res = fut.get();
        return ResponseEntity.ok(res);
    }
}
