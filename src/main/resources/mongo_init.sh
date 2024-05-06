#!/bin/bash

# Start MongoDB Docker container
docker run -d --name mongodb -p 27017:27017 mongo:4.2

# Wait for MongoDB to start
echo "Waiting for MongoDB to start..."
sleep 10

# Define MongoDB database and collection names
DB_NAME="glooko"
USER_COLLECTION="users"
DEVICE_COLLECTION="devices"
DEVICE_READING_COLLECTION="deviceReadings"

# Populate User documents
echo "Populating User documents..."
mongoimport --host localhost --port 27017 --db $DB_NAME --collection $USER_COLLECTION --type json --file users.json --jsonArray

# Populate Device documents
echo "Populating Device documents..."
mongoimport --host localhost --port 27017 --db $DB_NAME --collection $DEVICE_COLLECTION --type json --file devices.json --jsonArray

# Populate DeviceReading documents
echo "Populating DeviceReading documents..."
mongoimport --host localhost --port 27017 --db $DB_NAME --collection $DEVICE_READING_COLLECTION --type json --file deviceReadings.json --jsonArray

echo "Database population complete."