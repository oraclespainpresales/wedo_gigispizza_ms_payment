
# Wedo_DevOps_Microservices

internal helidon microservice managing payments and accounting events in SQL pluggable db

## Prerequisites


Verify prerequisites
```
java -version
mvn --version
docker --version
minikube version
kubectl version --short
```

## Build

```
mvn package
```

## Start the application

```
java -jar target/microservice-payment.jar
```

## Exercise the application
Examples:
```
curl -X POST -H "Content-Type: application/json" -d '{"paymentid":""}' http://localhost:9002/helidon/selectpayment

curl -X POST -H "Content-Type: application/json" -d '{"paymentid":"1234"}' http://localhost:9002/helidon/selectpayment
```

## Try health and metrics

```
curl -s -X GET http://localhost:9002/health
{"outcome":"UP",...
. . .

# Prometheus Format
curl -s -X GET http://localhost:9002/metrics
# TYPE base:gc_g1_young_generation_count gauge
. . .

# JSON Format
curl -H 'Accept: application/json' -X GET http://localhost:9002/metrics
{"base":...
. . .

```

## Build the Docker Image

```
docker build -t microservice-payment target
```

## Start the application with Docker

```
docker run --rm -p 8080:8080 microservice-payment:latest
```

Exercise the application as described above

## Deploy the application to Kubernetes

```
kubectl cluster-info                # Verify which cluster
kubectl get pods                    # Verify connectivity to cluster
kubectl create -f target/app.yaml   # Deply application
kubectl get service microservice-payment  # Get service info
```
