Mini Cloud Full - Combined Backend + Frontend

How to run:
1) Install Docker (recommended). Then:
   docker compose up -d
2) Build and run:
   mvn clean package
   java -jar target/mini-cloud-full-0.0.1-SNAPSHOT.jar
3) Open: http://localhost:8080/

Notes:
- MySQL credentials in application.properties: user=root, password=Rohit@123, DB=minicloud
- MinIO runs at http://localhost:9000 (minioadmin:minioadmin)
- If MinIO not available, uploaded files are stored in local storage/{bucketName}/
- Lambda execution demo requires python/node on PATH if you want to use those runtimes
