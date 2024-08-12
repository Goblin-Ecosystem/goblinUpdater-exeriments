inputFolder="$1"

mkdir largeProjectDataset
# Build and run docker
docker build -t update-dependencies-experimentation:0.0.0 .
if [ "$(uname)" == "Windows" ]; then
  rm -r ".\largeProjectDataset"
else
  rm -r ./largeProjectDataset
fi
docker run --network=host --name update-dependencies-experimentation -v "$inputFolder:/home/app/largeProjectDataset/" update-dependencies-experimentation:0.0.0
# Copy outFiles files from docker
docker cp update-dependencies-experimentation:/home/app/outFiles ./
# Delete docker
docker rm update-dependencies-experimentation
