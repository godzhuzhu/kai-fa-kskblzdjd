$env:PATH = "C:\Program Files\JetBrains\IntelliJ IDEA 2024.1.5\plugins\maven\lib\maven3\bin;$env:PATH"
mvn spring-boot:run -DskipTests *>&1 | Out-File -FilePath "server.log" -Encoding utf8
