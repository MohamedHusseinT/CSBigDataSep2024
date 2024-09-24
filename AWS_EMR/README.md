# Hello World for EMR & S3

This project demonstrates a basic **Word Count** application using **AWS EMR** (Elastic MapReduce) and **Amazon S3** for storage.

## Description

The **Word Count** application is a classic "Hello World" example in the Big Data world. It is written in **Java** and executed on **AWS EMR**, a managed Hadoop framework, with data stored and retrieved from **Amazon S3**. This project showcases how to set up, deploy, and run a distributed Word Count job in the cloud using EMR.

## Steps
- [Open AWS S3](https://aws.amazon.com/s3/)
- Create bucket, Add two folders (input and output folder) and add the input file to the input folder.
- Add the following package to Java Code.
    ```bash
    org.apache.hadoop.fs.s3a.S3AFileSystem
- Make sure all urls to input or output in your java code will navigate to S3.
- Export your code to JAR file.
- Upload the JAR file to AWS S3.
- [Open AWS EMR](https://aws.amazon.com/emr/)
- Click on "Create Cluster".
    1. In "Application bundle", choose custom, and add Hadoop 3.3.6 (you can choose any setup you would like).
    2. In "Cluster configuration", Choose EC2 instance type for Primary, Core and Tasks Nodes (for this demo you can choose the lower configuration).
    3. In "Identity and Access Management (IAM) roles", choose the service role for each one (you will need to create two roles, make sure to add S3 access to them both).
    4. In "Security configuration and EC2 key pair", make sure to "Create key pair", depending on your Operating System (Windows or MAC and linux), and save a copy in your local PC (this will help you to connect to the cluster through SSH from your terminal, in this demo, we are not going to use any terminal).
    4. Click on "Create Cluster".
    5. Wait until the status changes to waiting (this means it is ready).
- In Steps Section
    1. Click on "Add step".
    2. In step settings section in add step window, choose "Custom JAR".
    3. Add Name to this step.
    4. In JAR location, navigate to S3 to JAR location.
    5. In Argumrnts, write the name of the class (including package name).
    6. Click on "Add step".
    7. Wait until the status changes to Completed, then check your output folder.
- In case of an Error, you can check logs files of controller, syslog, stderr, stdout.




