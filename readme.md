# Update dependencies experimentation

The results of these experiments are presents here: https://zenodo.org/records/13285362

The dataset used is present on "datasets/finalDataset.csv" file.
You can regenerate the info dataset by follow the process describe on section "Run dataset Generator".

## Requirements
- Java 17
- Docker
- An active Neo4j database containing the Maven Central dependency graph available (dated April 12, 2024) here: https://zenodo.org/records/10605656
- An active Goblin Weaver server version 2.0.0 available here: https://github.com/Goblin-Ecosystem/goblinWeaver.
- Cve dataset (dated May 07, 2024) available here: TODO Zenodo

## Setup
Download the goblin-updater jar "goblinUpdater-1.0.0-jar-with-dependencies.jar", and put it on the "updater" folder at the root of the project.

https://github.com/Goblin-Ecosystem/goblinUpdater/releases/download/v1.0.0/goblinUpdater-1.0.0-jar-with-dependencies.jar

## Run Dataset Generator
1. Download the original dataset call "projects.zip" here: https://zenodo.org/record/4479015/
2. Unzip the archive, it will be passed by argument.
3. Choose "DatasetGenerator" at "experienceToRun" on the configuration.yml file at src/main/java/resources.
4. Make sure Docker is running.
5. run the "runDatasetGenerator.sh" script with the unziped projects folder path as argument.

## Run experiments
Among the experiments, "runUpdater" should be run first, with the others based on the results obtained by this one.
Execution results will be accessible in a new "outfiles" folder at project root.
To run one of the following experiments "compileAndTest", "naiveUpdate" or compareConfQualityAndCost, you need to put the results of the "runUpdater" experiment in a "results" folder in the project root.

1. Run your Neo4j Maven Central dependency graph
2. Run the Goblin Weaver API
3. Make sure Docker is running.
4. Choose the experiments to run on the configuration.yml file at src/main/java/resources.
5. Put your Goblin Weaver API url on the configuration.yml file at src/main/java/resources.
6. run the "run.sh" script.
