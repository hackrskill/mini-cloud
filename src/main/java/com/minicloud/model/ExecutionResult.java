package com.minicloud.model;
import java.time.LocalDateTime;
public class ExecutionResult {
    private String executionId;
    private boolean success;
    private String output;
    private String error;
    private long executionTimeMs;
    private LocalDateTime timestamp = LocalDateTime.now();
    public String getExecutionId(){return executionId;} public void setExecutionId(String e){this.executionId=e;}
    public boolean isSuccess(){return success;} public void setSuccess(boolean s){this.success=s;}
    public String getOutput(){return output;} public void setOutput(String o){this.output=o;}
    public String getError(){return error;} public void setError(String e){this.error=e;}
    public long getExecutionTimeMs(){return executionTimeMs;} public void setExecutionTimeMs(long t){this.executionTimeMs=t;}
    public LocalDateTime getTimestamp(){return timestamp;} public void setTimestamp(LocalDateTime t){this.timestamp=t;}
}
