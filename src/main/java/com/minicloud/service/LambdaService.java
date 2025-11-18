package com.minicloud.service;
import com.minicloud.model.AppUser;
import com.minicloud.model.ExecutionResult;
import com.minicloud.model.LambdaFunction;
import com.minicloud.repository.LambdaFunctionRepository;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;
import java.util.Map;
import java.util.List;
import java.util.UUID;

@Service
public class LambdaService {
    public final LambdaFunctionRepository functionRepository;
    public LambdaService(LambdaFunctionRepository functionRepository) {
        this.functionRepository = functionRepository;
    }

    public List<LambdaFunction> getAllFunctions(AppUser owner) {
        return functionRepository.findByOwner(owner);
    }

    public LambdaFunction createFunction(AppUser owner, LambdaFunction function) {
        if (functionRepository.existsByFunctionNameAndOwner(function.getFunctionName(), owner)) {
            throw new RuntimeException("Function already exists");
        }
        function.setOwner(owner);
        return functionRepository.save(function);
    }
    public CompletableFuture<ExecutionResult> executeFunctionAsync(AppUser owner, String functionName, Map<String,Object> input) {
        return CompletableFuture.supplyAsync(() -> {
            ExecutionResult r = new ExecutionResult();
            r.setExecutionId(UUID.randomUUID().toString());
            LambdaFunction fn = functionRepository.findByFunctionNameAndOwner(functionName, owner)
                    .orElseThrow(() -> new RuntimeException("Function not found"));
            try {
                String output = "Execution not implemented in this demo for " + fn.getFunctionName();
                r.setSuccess(true);
                r.setOutput(output);
            } catch (Exception e) {
                r.setSuccess(false); r.setError(e.getMessage());
            }
            return r;
        });
    }
}
