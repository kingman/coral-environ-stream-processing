# coral-environ-stream-processing
Sample code to run stream processing in Apache Beam with dynamica schema loading from Google Cloud IoT Core

To deploy the processing pipeline to Google Cloud DataFlow run:

        mvn compile exec:java \
        -Dexec.mainClass=com.google.cloud.solutions.IoTStreamAnalytics \
        -Dexec.cleanupDaemonThreads=false \
        -Dexec.args=" \
             --project=[PROJECT_ID] \
             --tempLocation=gs://[STAGE_BUCKET] \
             --streaming=true \
             --numWorkers=1 \
             --workerMachineType=n1-standard-1 \
             --inputTopic=[PUBSUB_TOPIC] \
             --windowSize=40 \
             --windowFrequency=15 \
             --outputTable=sliding_time_summary \
             --jobName=foglamp-stream \
             --runner=DataflowRunner" \
        -Pdataflow-runner


