FROM openjdk:17-slim

# ✅ Install FFmpeg directly (smaller base image)
RUN apt-get update && apt-get install -y ffmpeg && \
    rm -rf /var/lib/apt/lists/*

# ✅ Set working directory
WORKDIR /app

# ✅ Copy the compiled JAR into the container
COPY target/audioservice-0.0.1-SNAPSHOT.jar app.jar

# ✅ Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]
