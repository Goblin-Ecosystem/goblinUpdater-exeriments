# Build and run docker
docker build -t update-dependencies-experimentation:0.0.0 .
docker run --network=host --name update-dependencies-experimentation update-dependencies-experimentation:0.0.0
# Copy outFiles files from docker
docker cp update-dependencies-experimentation:/home/app/outFiles ./
# Delete docker
docker rm update-dependencies-experimentation
