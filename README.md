# 🏨 Hotel_ML_Rooms_And_Hotels_Service - Hotel and room management

## 📌 Overview
Hotel_ML_Rooms_And_Hotels_Service is a backend microservice based on **Spring Boot**, that is responsible for creating and modifying both rooms and hotels. ts tasks also include nclude communicating with Hotel_ML_Reservation_Service to determine the cost of reservations and to send to the Hotel_ML_APIGateway_Service a list of hotels with available rooms in given time periods.
## ❗ Important information
> To launch an application using the described service, go to:
> ➡️ [Main README](https://github.com/NiczSpeed/HotelML?tab=readme-ov-file#%EF%B8%8F-how-to-run-the-entire-system)

📌 **Key features::**
- ✅ Creating and modifying hotels
- ✅ Creating and modifying rooms
- ✅ Exchanging information to update reservation prices  
- ✅ Searching for available rooms in hotels 
- ✅ Encrypting stored and brokered data with AES
---

## 🔧 Technologies
| Component       | Technology |
|----------------|------------|
| **JęzykLanguage**  | Java 21 |
| **Framework**  | Spring Boot 3 |
| **Build Tool**  | Gradle (Kotlin) |
| **Database** | PostgreSQL |
| **Communication** | Apache Kafka |
| **ORM** | Spring Data JPA (Hibernate) |
| **Orchestration** | Docker, Docker Compose |

---

## 📂 Structure of the Code
```plaintext
/backend-service
│── \src\main\java\com\ml\hotel_ml_apigateway_service\
│   ├── configuration/                                      # Microservice configuration layer
│   │   ├── KafkaConsumerConfiguration.java                     # Configuring Apache Kafka Consumer
│   │   ├── KafkaProducerConfiguration.java                     # Apache Kafka Producer Configuration
│   │   ├── KafkaTopicsConfiguration.java                       # Configuring Apache Kafka themes
│   │   ├── ObjectMapperConfiguration.java                      # ObjectMapper configuration
│   ├── dto/                                                # DTO layer
│   │   ├── FreeHotelDto.java                                   # Dto used to send for request for list of hotels with vacant rooms
│   │   ├── HotelDto.java                                       # Dto for Hotel entity
│   │   ├── RoomDto.java                                        # Dto for Room entity
│   ├── exceptions/                                         # Additional exceptions of the microservices
│   │   ├── ErrorWhileDecodeException.java                      # Exception signaling a decoding problem
│   │   ├── ErrorWhileEncodeException.java                      # Exception signaling an encoding problem
│   │   ├── ErrorWhileSaveHotelsException.java                  # Exception signaling an error when saving hotels to the database
│   │   ├── ErrorWhileSaveRoomException.java                # Exception signaling an error when saving rooms to the database
│   ├── mapper/                                             # Layer mapping of microservice entities and DTOs
│   │   ├── FreeHotelMapper.java                                # FreeHotelDto Mapper
│   │   ├── HotelMapper.java                                    # Hotel Mapper
│   │   ├── RoomMapper.java                                     # Room Mapper
│   ├── model/                                              # Entity classes
│   │   ├── Hotel.java                                          # Entity used to manage hotels
│   │   ├── Room.java                                           # Entity used to manage rooms
│   │   ├── RoomStatus.java                                     # Enum with room statuses
│   ├── repository/                                         # The layer of connection of entities to the database
│   │   ├── HotelRepository.java                                # Hotel repository
│   │   ├── RoomRepository.java                                 # Room repository
│   ├── service                                             # Business logic layer
│   │   ├── InitDataService.java                                # Logic for adding hotels and rooms if they don't already exist
│   │   ├── HotelService.java                                   # The logic of hotels
│   │   ├── RoomService.java                                    # The logic of rooms
│   ├── utils/                                              # Additional functionalities 
│   │   ├── encryptors/                                         # Encryptor layer
│   │   |   ├── DoubleConverter.java                                # Double converter
│   │   |   ├── IntegerConverter.java                               # Integer converter
│   │   |   ├── LongConverter.java                                  # Long converter
│   │   |   ├── StringConverter.java                                # String converter
|   |   |── Encryptor.java                                      # Class inheriting EncryptorUtil to provide data encryption
|   |   |── EncryptorUtil.java                                      # A class containing encryption and decryption methods
|   |── HotelMlApiGatewayServiceApplication.java            # Spring Boot main class
│── src/main/resources/application.yml                      # Application configuration
│──.env                                                 # Environment variables for the Docker container
│── Dockerfile                                          # Docker image definition