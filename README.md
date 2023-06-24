# Landmark App

**Objective:** Design and implement a system for submitting and executing cloud computing tasks, with the ability to adapt to workload variations (elasticity), utilizing Google Cloud Platform services for storage, communication, and computation in an integrated manner: Cloud Storage, Firestore, Pub/Sub, Compute Engine, Cloud Functions, Vision API, and Static Maps API.

## 1. Introduction

The system has been designed to process images (photographs) with the objective of identifying the presence of landmarks or renowned sites. Once the location is recognized, the system retrieves an associated map image of the identified monument's vicinity. Furthermore, the system offers scalability, allowing for seamless adjustments in its concurrent image processing capacity, whether to increase or decrease it as needed.

The system functionalities are available to client applications through a gRPC interface with the following operations:

- Submission of an image file (photo) for landmark detection. This operation receives the content of a file as a stream of blocks and stores it as a blob in Cloud Storage. Upon completion, the operation returns a request identifier (for example, a unique composition of the bucket name and blob name) that will be used later to retrieve the submission result.

- Using a previously returned identifier from the previous operation, it should be possible to retrieve:
  - The list of results, which includes the names of landmarks identified in the image, the geographic location (latitude and longitude in decimal degrees format), and the corresponding confidence level associated with the identification (a value between 0 and 1).
  - The image with the static map of one of the identified locations.

- Retrieve the names of all photos where a monument was identified with a confidence level above **t** (for example, above 0.6), along with the name of the identified location.

All submission and subsequent querying operations are available through a gRPC server, which acts as the system facade. This means that the client application does not use or need to know that the server utilizes GCP platform services. To increase system availability and load balancing, multiple replicas of the gRPC server should exist, each running on a VM within an instance group. The system architecture uses the following GCP services and the Google Static Maps API:
- The Cloud Storage service stores the images to be processed and the static maps.
- The Firestore service stores relevant information about image processing, including the request identifier, identification of stored blobs in Storage, the names and locations of identified landmarks in images, and any other relevant information.
- The Pub/Sub service is used for decoupled message exchange between system components.
- The Compute Engine service is used to host multiple virtual machines running replicas of the gRPC server and additional virtual machines running replicas of the application (Landmarks App) for landmark identification in photos.
- The Vision API service is utilized for landmark detection in images.
- The Static Maps API is used to retrieve map images based on a specific latitude and longitude.

The interactions between the different components of the system are depicted in Figure 1.


![Figura 1: System flow and architecture](https://github.com/franciscocostalap/landmarks/assets/64478921/c46545d2-90db-4017-bde5-dfdbdf3e30eb)

**Figura 1: System flow and architecture**

## 2. Operational Flow

Considering the sequence numbers of actions presented in Figure 1, the following list describes each functionality:

- The Lookup Function service, used by the client application (1 and 2) to obtain the IP addresses of the gRPC servers, should be developed as a Cloud Function that retrieves (2) the IP addresses of the VMs within the instance group. The client application randomly selects an IP address from the list returned by the function, and in case of connection failure to the gRPC server using the chosen IP, it tries another IP or repeats the lookup process to update the IP list and establish a new connection.

- After submitting an image, it is stored in Cloud Storage (3.1), and a unique identifier is returned to the gRPC client for later queries. Then, a message containing the request identifier, bucket name, and blob name is sent to a Pub/Sub topic (3.2) for landmark detection processing.

- Associated with the aforementioned topic, there is a shared subscription for multiple workers (work-queue pattern). A worker (Landmarks App) responsible for image analysis receives, in each message, the bucket name and blob name of the image to be processed (4.1), which allows obtaining a global reference (gs:// URI) from Cloud Storage (4.2). The worker then interacts with the Vision API service (5) for location identification and the Maps service (6) for obtaining static maps.

- After the photo processing, the maps of the identified locations are stored in Cloud Storage (7), and relevant information about the request and analysis results is saved in Firestore (8).

- The client application can request information about the submitted images at any time using the request descriptor, as described in Section 1. To retrieve this information, the gRPC server queries Firestore (9) and/or Storage (10).

